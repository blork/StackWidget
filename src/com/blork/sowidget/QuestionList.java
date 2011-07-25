package com.blork.sowidget;

import java.util.Date;
import java.util.List;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.blork.sowidget.adapter.QuestionAdapter;
import com.blork.sowidget.model.Question;
import com.blork.sowidget.model.QuestionFactory;
import com.markupartist.android.widget.PullToRefreshListView;
import com.markupartist.android.widget.PullToRefreshListView.OnRefreshListener;
import com.ocpsoft.pretty.time.PrettyTime;



public class QuestionList extends ListActivity {

	private ListView lv;
	BroadcastReceiver updateReceiver;
	private List<Question> questionList;
	private ListAdapter listAdapter;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.questions);

		lv = getListView();
		registerForContextMenu(lv);

		((PullToRefreshListView) lv).setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				startService(new Intent(QuestionList.this, SoService.class));
			}
		});

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(QuestionList.this);	

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Intent viewIntent;
				Boolean droidstack = prefs.getBoolean("droidstack", false);
				Question clickedQuestion = questionList.get(position-1);
					
				Log.e("sowidget", clickedQuestion.getSite());
				
				if(droidstack){
					viewIntent = new Intent(Intent.ACTION_VIEW, 
							Uri.parse("droidstack://question?endpoint="+Uri.encode(clickedQuestion.getSite())+"&qid="+clickedQuestion.getQuestionId()));
				}else{
					String method = prefs.getString("method", "full");

					Uri uri = null;

					if(method.equals("mobile_classic")) {
						uri = Uri.parse("http://stackmobile.com/old_version/view_question.php?site=" 
								+ clickedQuestion.getSiteName() 
								+ "&id=" 
								+ clickedQuestion.getQuestionId());
					} else if(method.equals("mobile_touch")) {
						uri = Uri.parse("http://stackmobile.com/" 
								+ clickedQuestion.getSiteName()
								+ ".com/questions/view/?id=" 
								+ clickedQuestion.getQuestionId());
					} else {
						uri = Uri.parse("http://" + clickedQuestion.getSiteName() + ".com/questions/" + clickedQuestion.getQuestionId());
					}

					viewIntent = new Intent("android.intent.action.VIEW", uri);  
				}

				startActivity(viewIntent);
			}
		});

		questionList = QuestionFactory.getSaved(this);
		//listAdapter = new EndlessQuestionAdapter(this, questionList);
		listAdapter = new QuestionAdapter(this, R.layout.question_list_item, questionList);
		setListAdapter(listAdapter);

		updateReceiver = new UpdateReceiver();
		registerReceiver(updateReceiver, new IntentFilter(
				SoService.ACTION_NEW_STACKWIDGET_QUESTIONS));
	}

	public void onResume(){
		super.onResume();
		setVisible(true);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);	

		Date lastUpdate = new Date(prefs.getLong("time", new Date().getTime()));
		PrettyTime pt = new PrettyTime();
		String timeString = "Last updated: " + pt.format(lastUpdate);
		((PullToRefreshListView) lv).setLastUpdated(timeString);

		String url = prefs.getString("sites", "http://api.stackoverflow.com");
		String site = url.replaceAll("http://api.", "").replaceAll(".com", "");


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
		unregisterReceiver(updateReceiver);
	}

	class UpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("sowidget", "QuestionList got broadcast");
			questionList.clear();

			questionList.addAll(QuestionFactory.getSaved(QuestionList.this));

			//listAdapter.notifyDataSetChanged();

			((PullToRefreshListView) lv).onRefreshComplete();

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(QuestionList.this);	

			Date lastUpdate = new Date(prefs.getLong("time", new Date().getTime()));
			PrettyTime pt = new PrettyTime();
			String timeString = "Last updated: " + pt.format(lastUpdate);
			((PullToRefreshListView) lv).setLastUpdated(timeString);

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

		int index = info.position-1;

		Question clickedQuestion = questionList.get(index);

		String site = ((String) clickedQuestion.getSite()).replaceAll("http://api.", "").replaceAll(".com", "");

		switch (item.getItemId()) {
		case 1:
			boolean faved = clickedQuestion.addToFavorites(this);

			if (faved) {
				Toast.makeText(QuestionList.this, "Saved.", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(QuestionList.this, "Already favourited.", Toast.LENGTH_LONG).show();
			}

			return true;
		case 2:
			Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(android.content.Intent.EXTRA_TITLE, clickedQuestion.getTitle());
			shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "http://"+site+".com/questions/"+clickedQuestion.getQuestionId());
			startActivity(Intent.createChooser(shareIntent, "Share question")); 
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}


	//	 private class FavouriteTask extends AsyncTask<Integer, Void, Integer> {
	//			protected Integer doInBackground(Integer... params) {
	//				int questionId = params[0];
	//				
	//				Cursor favCursor = db.query("favourites", new String[] {_ID, "q_id, title, tags, votes, answer_count, user_name, site"}, 
	//		                "q_id = "+questionId, null, null, null, null);
	//				startManagingCursor(favCursor);
	//				
	//				favCursor.moveToFirst();
	//				int dupe = favCursor.getCount();
	//				
	//				if(dupe == 0){
	//					Cursor questionCursor = db.query("questions", new String[] {_ID, "q_id, title, tags, votes, answer_count, user_name, site"}, 
	//			                "q_id = "+questionId, null, null, null, null);
	//					startManagingCursor(questionCursor);
	//					
	//					questionCursor.moveToFirst();
	//					
	//					
	//					ContentValues values = new ContentValues();
	//			
	//					
	//					values.put("q_id", questionId);
	//					values.put("title", questionCursor.getString(questionCursor.getColumnIndex("title")));
	//					values.put("tags", questionCursor.getString(questionCursor.getColumnIndex("tags")));
	//					values.put("votes", questionCursor.getString(questionCursor.getColumnIndex("votes")));
	//					values.put("answer_count", questionCursor.getString(questionCursor.getColumnIndex("answer_count")));
	//					values.put("user_name", questionCursor.getString(questionCursor.getColumnIndex("user_name")));
	//					values.put("site", questionCursor.getString(questionCursor.getColumnIndex("site")));
	//					try { 
	//						db.insertOrThrow("favourites", null, values);
	//						return 0;
	//					} catch (SQLException e) { 
	//						Log.e("sowidget", e.toString());
	//						return 1;
	//					}
	//				}else{
	//					return 2;
	//				}
	//				
	//				
	//			}
	//
	//
	//	     protected void onPostExecute(Integer result) {
	//	    	 if(result == 0){
	//	    		 Toast.makeText(QuestionList.this, "Saved.", Toast.LENGTH_LONG).show();
	//	    	 }else if(result == 2){
	//	    		 Toast.makeText(QuestionList.this, "Already favourited.", Toast.LENGTH_LONG).show();
	//	    	 }else{
	//	    		 Toast.makeText(QuestionList.this, "Unable to save this question.", Toast.LENGTH_LONG).show();
	//	    	 }
	//	     }
	//	     
	//	 
	//
	//	 }
}

