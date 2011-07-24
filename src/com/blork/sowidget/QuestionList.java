package com.blork.sowidget;

import static android.provider.BaseColumns._ID;

import com.blork.sowidget.adapter.EndlessQuestionAdapter;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;



public class QuestionList extends ListActivity {
	
	private SQLiteDatabase db;
	private ListView lv;
	private Cursor questionCursor;
	BroadcastReceiver updateReceiver;
	String url;
	String site;

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.questions);
               

        
        lv = getListView();
        registerForContextMenu(lv); 
        
		lv.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view,
		        int position, long id) {
		    	onClick(position, ((TextView) view.findViewById(R.id.id)).getText());

		    }

			private void onClick(int position, CharSequence qID) {
				//Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://"+stackExchangeSite+".com/questions/"+qID+"/"));  
				
				Intent viewIntent;
		        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(QuestionList.this);	
		        Boolean droidstack = prefs.getBoolean("droidstack", false);
		        
				
				if(droidstack){
					viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("droidstack://question?endpoint="+Uri.encode(url)+"&qid="+qID));
				}else{
			        String method = prefs.getString("method", "full");

			        Uri uri = null;
			        
			        if(method.equals("mobile_classic")) {
				        uri = Uri.parse("http://stackmobile.com/old_version/view_question.php?site=" + site + "&id=" + qID);
			        } else if(method.equals("mobile_touch")) {
			        	uri = Uri.parse("http://stackmobile.com/" + site + ".com/questions/view/?id=" + qID);
			        } else {
			        	uri = Uri.parse("http://" + site + ".com/questions/" + qID);
			        }
			        
					viewIntent = new Intent("android.intent.action.VIEW", uri);  
				}
				
				startActivity(viewIntent);
			}
		});
        
		QuestionData qs = new QuestionData(this);
	    db = qs.getReadableDatabase();
		questionCursor = db.query("questions", new String[] {_ID, "q_id, title, tags, votes, answer_count, user_name, site"}, 
	                null, null, null, null, null); 
		
		startManagingCursor(questionCursor);
		
        int[] displayViews = new int[] { R.id.id, R.id.title, R.id.tags, R.id.votes, R.id.answers, R.id.user, R.id.site };

        String[] displayFields = new String[] { "q_id", "title", "tags", "votes", "answer_count", "user_name", "site"};  
        
        
        setListAdapter(new SimpleCursorAdapter(this, 
                       R.layout.question_list_item, questionCursor, 
                       displayFields, displayViews));
              
        
        updateReceiver = new UpdateReceiver();
        registerReceiver(updateReceiver, new IntentFilter(
            SoService.ACTION_NEW_STACKWIDGET_QUESTIONS));
    }
    
    public void onResume(){
    	super.onResume();
    	setVisible(true);
    	
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);	
        
        url = prefs.getString("sites", "http://api.stackoverflow.com");
        site = url.replaceAll("http://api.", "").replaceAll(".com", "");

        
		String tags = prefs.getString("tags", "all");
		String sort = prefs.getString("sort", "hot");
		
        TextView tagged = (TextView)findViewById(R.id.tags);
        TextView sorting = (TextView)findViewById(R.id.sort);
        TextView siteName = (TextView)findViewById(R.id.site);
        
        tagged.setText(tags);
        sorting.setText(sort);
        siteName.setText(site);
    }
    public void onPause(){
    	super.onPause();
    	setVisible(false);
    }
    
    public void onDestroy(){
    	super.onDestroy();
    	db.close();
    	unregisterReceiver(updateReceiver);
    }
    
    class UpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
	          Log.d("sowidget", "QuestionList got broadcast");
	            questionCursor.requery();
	          
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(QuestionList.this);	
				  
				String site = prefs.getString("sites", "http://api.stackoverflow.com").replaceAll("http://api.", "").replaceAll(".com", "");
				String tags = prefs.getString("tags", "all");
				String sort = prefs.getString("sort", "hot");				
				TextView tagged = (TextView)findViewById(R.id.tags);
				TextView sorting = (TextView)findViewById(R.id.sort);
				TextView siteName = (TextView)findViewById(R.id.site);
				  
				tagged.setText(tags);
				sorting.setText(sort);
				siteName.setText(site);
		}
     }
    
    /* Creates the menu items */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.menu, menu); 
        return true;
    }

    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	if(item.getTitle().equals("Favourites")){
    		startActivity(new Intent(this, FavouritesList.class));
    	}else if(item.getTitle().equals("Settings")){
    		startActivity(new Intent(this, Settings.class));
    	}else if(item.getTitle().equals("Refresh")){
            ConnectivityManager conMgr =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            //Check to see if the device is connected or connecting to the Internet
           
    		if (conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()   ||  conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
        		Toast.makeText(this, "Updating...", Toast.LENGTH_SHORT).show(); 
        		startService(new Intent(this, SoService.class));
    		}else{
    			Toast.makeText(this, "Offline.", Toast.LENGTH_SHORT).show(); 
    		}

    	}
        return true;
    }
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Options");
		menu.add(0, 1, 0, "Save to favorites");
		menu.add(0, 2, 0, "Share");
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		RelativeLayout rl = (RelativeLayout) info.targetView;
		TextView id = (TextView)rl.getChildAt(5);
		TextView site = (TextView)rl.getChildAt(6);
		TextView title = (TextView)rl.getChildAt(0);
		
		int questionId = Integer.parseInt(id.getText().toString());
		String name = site.getText().toString();
		name = ((String) name).replaceAll("http://api.", "").replaceAll(".com", "");
		String titleText = title.getText().toString();

			
		switch (item.getItemId()) {
			case 1:
				new FavouriteTask().execute(questionId);				
				return true;
			case 2:
				Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
				shareIntent.setType("text/plain");
				shareIntent.putExtra(android.content.Intent.EXTRA_TITLE, titleText);
				shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "http://"+name+".com/questions/"+questionId);
				startActivity(Intent.createChooser(shareIntent, "Share question")); 
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}
	
	
	 private class FavouriteTask extends AsyncTask<Integer, Void, Integer> {
			protected Integer doInBackground(Integer... params) {
				int questionId = params[0];
				
				Cursor favCursor = db.query("favourites", new String[] {_ID, "q_id, title, tags, votes, answer_count, user_name, site"}, 
		                "q_id = "+questionId, null, null, null, null);
				startManagingCursor(favCursor);
				
				favCursor.moveToFirst();
				int dupe = favCursor.getCount();
				
				if(dupe == 0){
					Cursor questionCursor = db.query("questions", new String[] {_ID, "q_id, title, tags, votes, answer_count, user_name, site"}, 
			                "q_id = "+questionId, null, null, null, null);
					startManagingCursor(questionCursor);
					
					questionCursor.moveToFirst();
					
					
					ContentValues values = new ContentValues();
			
					
					values.put("q_id", questionId);
					values.put("title", questionCursor.getString(questionCursor.getColumnIndex("title")));
					values.put("tags", questionCursor.getString(questionCursor.getColumnIndex("tags")));
					values.put("votes", questionCursor.getString(questionCursor.getColumnIndex("votes")));
					values.put("answer_count", questionCursor.getString(questionCursor.getColumnIndex("answer_count")));
					values.put("user_name", questionCursor.getString(questionCursor.getColumnIndex("user_name")));
					values.put("site", questionCursor.getString(questionCursor.getColumnIndex("site")));
					try { 
						db.insertOrThrow("favourites", null, values);
						return 0;
					} catch (SQLException e) { 
						Log.e("sowidget", e.toString());
						return 1;
					}
				}else{
					return 2;
				}
				
				
			}


	     protected void onPostExecute(Integer result) {
	    	 if(result == 0){
	    		 Toast.makeText(QuestionList.this, "Saved.", Toast.LENGTH_LONG).show();
	    	 }else if(result == 2){
	    		 Toast.makeText(QuestionList.this, "Already favourited.", Toast.LENGTH_LONG).show();
	    	 }else{
	    		 Toast.makeText(QuestionList.this, "Unable to save this question.", Toast.LENGTH_LONG).show();
	    	 }
	     }
	     
	 

	 }
}

