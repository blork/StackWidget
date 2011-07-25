package com.blork.sowidget;

import java.util.List;

import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.blork.sowidget.adapter.QuestionAdapter;
import com.blork.sowidget.model.Question;
import com.blork.sowidget.model.QuestionFactory;



public class FavouritesList extends ListActivity {
	
	private ListView lv;
	private List<Question> questionList;
	private QuestionAdapter listAdapter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.favourites);
                
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		mNotificationManager.cancelAll();
        

        TextView siteName = (TextView)findViewById(R.id.site);
  
        siteName.setText("Favourites");
        
        lv = getListView();
        registerForContextMenu(lv); 
                
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(FavouritesList.this);	

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Intent viewIntent;
				Boolean droidstack = prefs.getBoolean("droidstack", false);
				Question clickedQuestion = questionList.get(position);
					
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
         
		questionList = QuestionFactory.getFavourites(this);
		Log.e("sowidget", questionList.toString());
		//listAdapter = new EndlessQuestionAdapter(this, questionList);
		listAdapter = new QuestionAdapter(this, R.layout.question_list_item, questionList);
		setListAdapter(listAdapter);
              
    }
    
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Options");
		menu.add(0, 1, 0, "Remove from favorites");
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		int index = info.position;

		Question clickedQuestion = questionList.get(index);
		
		switch (item.getItemId()) {
			case 1:
				clickedQuestion.removeFromFavourites(getApplicationContext());
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

}

