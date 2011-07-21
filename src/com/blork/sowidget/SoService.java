package com.blork.sowidget;

import static android.provider.BaseColumns._ID;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class SoService extends Service implements Runnable{
	static final String ACTION_NEW_STACKWIDGET_QUESTIONS = "ACTION_NEW_STACKWIDGET_QUESTIONS";
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	public void onCreate() {
		Log.d("sowidget", "Service created."); 
		startService();      
	}
	
	private void startService(){
		Log.d("sowidget", "Service started.");
		Thread thread = new Thread(this);
		thread.start();
	}
	
	public void run(){ 
		Log.d("sowidget", "Service thread running.");
		
        Intent intent = new Intent(this, Widget.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0); 
               
        long firstTime = SystemClock.elapsedRealtime();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		
		int updates = Integer.parseInt(prefs.getString("updates", "3"));
		
		long interval = AlarmManager.INTERVAL_HALF_HOUR;
		
		switch(updates){
			case 1:
				interval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
				Log.d("sowidget","Update interval: 15 minutes");
				break;
			case 2:
				interval = AlarmManager.INTERVAL_HALF_HOUR;
				Log.d("sowidget","Update interval: 30 minutes");
				break;
			case 3:
				interval = AlarmManager.INTERVAL_HOUR;
				Log.d("sowidget","Update interval: 1 hour");
				break;
			case 4:
				interval = AlarmManager.INTERVAL_HALF_DAY;
				Log.d("sowidget","Update interval: Half day");
				break;
			case 5:
				interval = AlarmManager.INTERVAL_DAY;
				Log.d("sowidget","Update interval: Whole day");
				break;
		}
		        
		am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, firstTime, interval, sender);  	
		
		String stackExchangeSite = prefs.getString("sites", "http://api.stackoverflow.com");
		String tags = prefs.getString("tags", "");
		String[] splitTags = tags.trim().split(",");
		

		for(int i = 0, length = splitTags.length; i < length; i++){
			splitTags[i] = splitTags[i].trim();
		}
		
		int num = Integer.parseInt(prefs.getString("num", "10"));
		
		String sort = prefs.getString("sort", "hot");
		
		SiteWrapper site = new SiteWrapper(stackExchangeSite, this); 
      
		QuestionData qs = new QuestionData(this);
	    SQLiteDatabase db = qs.getReadableDatabase();

		
		try{			
			site.setURL(sort, false, false, splitTags, num);
			
			site.fetchQuestions(); 
			
			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
			
			
			if (site.questionCount > 0){
				site.saveQuestions();  

				mNotificationManager.cancelAll();
			} else {
		
				int icon = android.R.drawable.stat_notify_error;
				long when = System.currentTimeMillis();

				Intent notificationIntent;

				notificationIntent  = new Intent(this, Settings.class); 
				
				PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
				
				Notification notification = new Notification(icon, "StackWidget: No questions found!", when);
				
				Context context = getApplicationContext();
				
				notification.setLatestEventInfo(context, "StackWidget: No questions found!", "Try removing some of your tags.", contentIntent);
				
				mNotificationManager.notify(2, notification);

			}
			   
			
			sendBroadcast(new Intent(ACTION_NEW_STACKWIDGET_QUESTIONS));    
			

			Cursor questionCursor = db.query("questions", new String[] {_ID, "q_id, title, tags, votes, answer_count, user_name"}, 
	                null, null, null, null, null);
	        
	        questionCursor.moveToFirst(); 
	        
	        RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.widget);
	        
	        ComponentName thisWidget = new ComponentName( this, Widget.class );
	        
	        views.setViewVisibility(R.id.content, View.VISIBLE);
	        views.setViewVisibility(R.id.loading, View.GONE);
	        
	        views.setTextViewText(R.id.title, questionCursor.getString(questionCursor.getColumnIndex("title")));
	        views.setTextViewText(R.id.votes, questionCursor.getString(questionCursor.getColumnIndex("votes")));
	        views.setTextViewText(R.id.answers, questionCursor.getString(questionCursor.getColumnIndex("answer_count")));
	       
	        String[] tag = questionCursor.getString(questionCursor.getColumnIndex("tags")).split(",");
	        views.setTextViewText(R.id.tags, tag[0]); 
	        
	        Intent qIntent = new Intent(this, QuestionList.class);
	        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, qIntent, 0);
	        views.setOnClickPendingIntent(R.id.content, pendingIntent);
	        
	        AppWidgetManager.getInstance(this).updateAppWidget(thisWidget, views); 
	        
	        questionCursor.close();
	        
        }catch(MalformedURLException e){
        	Log.e("sowidget", e.toString());	        	
        }catch(IOException e){
        	Log.e("sowidget", e.toString());	
        } catch (JSONException e) { 
        	Log.e("sowidget", e.toString());
		} catch (Exception e){
			Log.e("sowidget", e.toString());
		}
		
        

       
		Cursor favouriteCursor = db.query("favourites", new String[] {_ID, "q_id, title, tags, votes, answer_count, user_name, site"}, 
                null, null, null, null, null);
		favouriteCursor.moveToFirst();
		
		int count = 0;
        while (favouriteCursor.isAfterLast() == false) {
        	
        	int qId = Integer.parseInt(favouriteCursor.getString(favouriteCursor.getColumnIndex("q_id")));
        	String wAnswers = favouriteCursor.getString(favouriteCursor.getColumnIndex("answer_count"));
        	int answers = Integer.parseInt(wAnswers.substring(0, wAnswers.length()-8));
        	
        	String siteName = favouriteCursor.getString(favouriteCursor.getColumnIndex("site"));
        	
        	try {
				URL url = new URL(siteName+"/"+SiteWrapper.version+"/questions/"+qId+"?key="+SiteWrapper.KEY); 
				Log.d("sowidget", url.toString());
				
				JSONObject json = (JSONObject) new JSONTokener(SiteWrapper.getJSON(url)).nextValue();
				JSONArray questions = json.getJSONArray("questions");
				
				JSONObject question =  questions.getJSONObject(0);
				

				
				int newAnswers = question.getInt("answer_count");
				

				
				if(newAnswers > answers){
					
					SQLiteDatabase db2 = qs.getWritableDatabase();
					ContentValues values = new ContentValues();
					values.put("answer_count", newAnswers+" answers");
					db2.update("favourites", values, "q_id ="+qId, null);
					count++; 
					
					String ns = Context.NOTIFICATION_SERVICE;
					
					NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
					
					String title = favouriteCursor.getString(favouriteCursor.getColumnIndex("title"));
					
					Log.d("sowidget", "New answers: "+newAnswers+" "+title);
					
					
					Boolean notifications = prefs.getBoolean("notifications_enabled", true);
					
					if(notifications){
						int icon = R.drawable.notification;
						CharSequence tickerText = title;
						long when = System.currentTimeMillis();
	
						
						
						CharSequence contentTitle;
						CharSequence contentText;
						Intent notificationIntent;
						
						if(count == 1){
							notificationIntent  = new Intent("android.intent.action.VIEW", Uri.parse("http://stackmobile.com/view_question.php?site="+siteName+"&id="+qId)); 						
							
							contentTitle = "StackWidget - New Answer";
							contentText = title;
						}else{
							notificationIntent  = new Intent(this, FavouritesList.class); 
							
							contentTitle = "StackWidget - New Answers";
							contentText = count+" new answers.";
						}
						
						PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
						
						Notification notification = new Notification(icon, tickerText, when);
						
						Context context = getApplicationContext();
						
						notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
						
						mNotificationManager.notify(1, notification);
					}
			
				}else{
					Log.d("sowidget", qId+": no change");
				}
				
            }catch(MalformedURLException e){
            	Log.e("sowidget", e.toString());	        	
            }catch(IOException e){
            	Log.e("sowidget", e.toString());	
            } catch (JSONException e) { 
            	Log.e("sowidget", e.toString());
    		} catch (Exception e){
    			Log.e("sowidget", e.toString());
    		}
             
        	
        	favouriteCursor.moveToNext();
        }
        favouriteCursor.close();
		
        db.close();
        stopSelf();
        
	}

    

}
  