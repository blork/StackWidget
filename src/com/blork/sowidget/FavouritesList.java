package com.blork.sowidget;

import static android.provider.BaseColumns._ID;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
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



public class FavouritesList extends ListActivity {
	
	private SQLiteDatabase db;
	private ListView lv;
	private Cursor questionCursor;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.questions);
                
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		mNotificationManager.cancelAll();
        
        String site = "Favourites";

        TextView siteName = (TextView)findViewById(R.id.site);
  
        siteName.setText(site);
        

        
        lv = getListView();
        registerForContextMenu(lv); 
                
		lv.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view,
		        int position, long id) {
		    	onClick(position, ((TextView) view.findViewById(R.id.id)).getText(), ((TextView) view.findViewById(R.id.site)).getText());
 
		    }

			private void onClick(int position, CharSequence qID, CharSequence site) {
				
				
				
				Intent viewIntent;
		        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(FavouritesList.this);	
		        Boolean droidstack = prefs.getBoolean("droidstack", false);
		        
				if(droidstack){
					viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("droidstack://question?endpoint="+Uri.encode((String) site)+"&qid="+qID));
				}else{
					site = ((String) site).replaceAll("http://api.", "").replaceAll(".com", "");
					viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://stackmobile.com/view_question.php?site="+site+"&id="+qID));  
				}
			
				
				
				//Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://"+stackExchangeSite+".com/questions/"+qID+"/"));  
				startActivity(viewIntent); 
			}
		});
         
		QuestionData qs = new QuestionData(this);  
	    db = qs.getReadableDatabase();
		questionCursor = db.query("favourites", new String[] {_ID, "q_id, title, tags, votes, answer_count, user_name, site"}, 
	                null, null, null, null, null);
		
		startManagingCursor(questionCursor);
		
        int[] displayViews = new int[] { R.id.id, R.id.title, R.id.tags, R.id.votes, R.id.answers, R.id.user, R.id.site };

        String[] displayFields = new String[] { "q_id", "title", "tags", "votes", "answer_count", "user_name", "site"};  
        
        setListAdapter(new SimpleCursorAdapter(this, 
                       R.layout.question_list_item, questionCursor, 
                       displayFields, displayViews));
              
    }
    
    public void onDestroy(){
    	super.onDestroy();
    	db.close();
    }
    
    
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Options");
		menu.add(0, 1, 0, "Remove from favorites");
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		RelativeLayout rl = (RelativeLayout) info.targetView;
		TextView tv = (TextView)rl.getChildAt(5);
		int questionId = Integer.parseInt(tv.getText().toString());
			
		switch (item.getItemId()) {
			case 1:
				new DeleteTask().execute(questionId);				
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}
	
	
	 private class DeleteTask extends AsyncTask<Integer, Void, Boolean> {
			protected Boolean doInBackground(Integer... params) {
				int questionId = params[0];
				
				db.delete("favourites", "q_id = '"+questionId+"'", null);
				return true;				
				
			}


	     protected void onPostExecute(Boolean result) {
	    	 if(result == true){
	    		 questionCursor.requery();
	    		 Toast.makeText(FavouritesList.this, "Removed.", Toast.LENGTH_LONG).show();
	    	 }else{
	    		 Toast.makeText(FavouritesList.this, "Unable to remove this question.", Toast.LENGTH_LONG).show();
	    	 }
	     }

	 }
}

