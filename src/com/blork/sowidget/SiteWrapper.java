package com.blork.sowidget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;


public class SiteWrapper {
	public static final String KEY = "";

	private static URL url;
	private static JSONArray questions;
	private static Context context;
	private static String site;

	public int questionCount;
	static final Double version = 1.0;
	
	public SiteWrapper(String site, Context ctx){
		SiteWrapper.context = ctx;
		SiteWrapper.site = site; 
	}
	
	public void setURL(String sort, Boolean body, Boolean answers, String[] tags, int num) throws MalformedURLException, UnsupportedEncodingException{
		String tagged = TextUtils.join(";", tags);
		Log.d("sowidget", sort+" "+body+" "+answers+" "+tagged+" "+num+" "+SiteWrapper.version);

		String baseUrl = site+"/"+SiteWrapper.version+"/questions" 
														+"?key="+SiteWrapper.KEY
														+"&sort="+sort
														+"&body="+body
														+"&answers="+answers
														+"&tagged="+URLEncoder.encode(tagged, "UTF-8")
														+"&pagesize="+num;
		Log.d("sowidget", baseUrl);
		SiteWrapper.url = new URL(baseUrl);
	}
	
	public void fetchQuestions() throws JSONException, IOException{
        JSONObject json = (JSONObject) new JSONTokener(getJSON(SiteWrapper.url)).nextValue();
        SiteWrapper.questions = json.getJSONArray("questions");
        this.questionCount = questions.length();
	}
	
	
    public static String getJSON(URL url) throws IOException {
        final URLConnection connection = url.openConnection();
        connection.connect();
        InputStream stream = null;
        
        Log.d("sowidget", "Getting questions...");
        
        try {
			if (connection.getContentEncoding().equals("gzip")) {
				Log.d("sowidget", "File is compressed.");
				stream = new GZIPInputStream(connection.getInputStream()); 
			}else{
				stream = connection.getInputStream();
			}
		} catch (Exception e) {
			stream = connection.getInputStream();
		}
		
        String ct = connection.getContentType();
        final BufferedReader reader;
        if (ct.indexOf("charset=") != -1) {
        		ct = ct.substring(ct.indexOf("charset=") + 8);
                reader = new BufferedReader(new InputStreamReader(stream, ct));
        }else {
        		ct = null;
                reader = new BufferedReader(new InputStreamReader(stream));
        }
        final StringBuilder sb = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
        	sb.append(line + '\n');
            line = reader.readLine();
        }
        return sb.toString();
    }
    
    
	
    public void saveQuestions(){
    	Log.d("sowidget", "Saving questions to DB...");
    	Log.d("sowidget","Questions: "+SiteWrapper.questions.length());
    	if(this.questionCount > 0){
 	    	
	    	QuestionData qData = new QuestionData(SiteWrapper.context);
	    	
			SQLiteDatabase db = qData.getWritableDatabase();
			
			db.delete("questions", null, null);
			
			
		
			for(int x = 0; x < SiteWrapper.questions.length(); x++){ 
				try {
					JSONObject question =  SiteWrapper.questions.getJSONObject(x);
					
					ContentValues values = new ContentValues();
					Log.d("sowidget", "Question ID: "+question.getString("question_id"));
									
					JSONArray tags = question.getJSONArray("tags");
					
					String niceTags = "";
					
					for(int i = 0; i < tags.length(); i++){
						if(i != 0){
							niceTags += ", ";
						}
						niceTags += tags.getString(i);
					}
										
					int votes = question.getInt("up_vote_count") - question.getInt("down_vote_count");
					
					JSONObject owner = question.getJSONObject("owner");
					String userName = "Asked by "+owner.getString("display_name");
					
					
					values.put("q_id", question.getString("question_id"));
					values.put("title", question.getString("title"));
					values.put("tags", niceTags);
					values.put("votes", votes+" votes");
					values.put("answer_count", question.getInt("answer_count")+" answers");
					values.put("user_name", userName);
					values.put("site", SiteWrapper.site);
					
					db.insertOrThrow("questions", null, values);
					
				} catch (JSONException e) {
					Log.e("sowidget", e.toString());
				}
			}
			
			db.close();
    	}
    }
}

