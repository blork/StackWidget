package com.blork.sowidget;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.json.JSONException;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.blork.sowidget.model.Question;
import com.blork.sowidget.model.QuestionFactory;

public class SoService extends Service implements Runnable{
	static final String ACTION_NEW_STACKWIDGET_QUESTIONS = "ACTION_NEW_STACKWIDGET_QUESTIONS";
	private static final String LOG_TAG = "StackWidget";

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

		int num = prefs.getInt("num", 20);

		String sort = prefs.getString("sort", "hot");

		SiteWrapper site = new SiteWrapper(stackExchangeSite);


		try{			
			site.setURL(sort, false, false, splitTags, num);

			List<Question> questions = site.fetchQuestions(); 

			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);


			if (questions.size() > 0){
				QuestionFactory.deleteAll(this);

				for (Question q : questions) {
					Log.d(LOG_TAG, "Saving question: " + q.getTitle());
					q.save(this);
				}

				mNotificationManager.cancelAll();
			} else {

				int icon = android.R.drawable.stat_notify_error;
				long when = System.currentTimeMillis();

				Intent notificationIntent  = new Intent(this, Settings.class); 
				PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
				Notification notification = new Notification(icon, "StackWidget: No questions found!", when);
				Context context = getApplicationContext();
				notification.setLatestEventInfo(context, "StackWidget: No questions found!", "Try removing some of your tags.", contentIntent);
				mNotificationManager.notify(2, notification);

			}

			long time = System.currentTimeMillis();
			Editor editor = prefs.edit();
			editor.putLong("time", time);
			editor.commit();

			sendBroadcast(new Intent(ACTION_NEW_STACKWIDGET_QUESTIONS));    

			Question topQuestion = QuestionFactory.getSaved(this).get(0);

			RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.widget);

			ComponentName thisWidget = new ComponentName( this, Widget.class );

			views.setViewVisibility(R.id.content, View.VISIBLE);
			views.setViewVisibility(R.id.loading, View.GONE);

			views.setTextViewText(R.id.title, topQuestion.getTitle());
			views.setTextViewText(R.id.votes, topQuestion.getVotes().toString() + " votes");
			views.setTextViewText(R.id.answers, topQuestion.getAnswerCount().toString() + " answers");

			String[] tag = topQuestion.getTags().split(",");
			views.setTextViewText(R.id.tags, tag[0]); 

			Intent qIntent = new Intent(this, QuestionList.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, qIntent, 0);
			views.setOnClickPendingIntent(R.id.content, pendingIntent);

			AppWidgetManager.getInstance(this).updateAppWidget(thisWidget, views); 

		}catch(MalformedURLException e){
			Log.e("sowidget", e.toString());	        	
		}catch(IOException e){
			Log.e("sowidget", e.toString());	
		} catch (JSONException e) { 
			Log.e("sowidget", e.toString());
		} catch (Exception e){
			Log.e("sowidget", e.toString());
		}




		List<Question> favourites = QuestionFactory.getFavourites(this);

		int count = 0;
		for (Question fave : favourites) {
			if (fave.hasNewAnswers(getApplicationContext()))
				count++;
		}

		Boolean notifications = prefs.getBoolean("notifications_enabled", true);

		if(count > 0 && notifications){
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

			int icon = R.drawable.notification;
			long when = System.currentTimeMillis();


			Intent notificationIntent = new Intent(this, FavouritesList.class); 

			String contentTitle = "StackWidget - New Answers";
			String contentText = count + " new answer" + ((count > 1) ? "s" : "") + ".";


			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

			Notification notification = new Notification(icon, contentTitle, when);

			Context context = getApplicationContext();

			notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

			mNotificationManager.notify(1, notification);
		}
		
		stopSelf();

	}



}
