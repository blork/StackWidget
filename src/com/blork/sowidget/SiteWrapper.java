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
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.text.TextUtils;
import android.util.Log;

import com.blork.sowidget.model.Question;


public class SiteWrapper {
	public static final String KEY = "XSuxd2vSOkedfwPtYYJBYA";

	private URL url;
	private String site;

	public static final Double VERSION = 1.1;
	
	public SiteWrapper(String site){
		this.site = site; 
	}
	
	public void setURL(String sort, Boolean body, Boolean answers, String[] tags, int num) throws MalformedURLException, UnsupportedEncodingException{
		String tagged = TextUtils.join(";", tags);
		Log.d("sowidget", sort+" "+body+" "+answers+" "+tagged+" "+num+" "+SiteWrapper.VERSION);

		String baseUrl = site+"/"+SiteWrapper.VERSION+"/questions" 
														+"?key="+SiteWrapper.KEY
														+"&sort="+sort
														+"&body="+body
														+"&answers="+answers
														+"&tagged="+URLEncoder.encode(tagged, "UTF-8")
														+"&pagesize="+num;
		Log.d("sowidget", baseUrl);
		this.url = new URL(baseUrl);
	}
	
	public List<Question> fetchQuestions() throws JSONException, IOException{
		List<Question> questions = new ArrayList<Question>();
		
        JSONObject json = (JSONObject) new JSONTokener(getJSON(this.url)).nextValue();
        JSONArray questionsJson = json.getJSONArray("questions");
        
        
        for(int x = 0; x < questionsJson.length(); x++){ 
			try {
							
				JSONObject questionObj =  questionsJson.getJSONObject(x);
												
				JSONArray tags = questionObj.getJSONArray("tags");
				
				String niceTags = "";
				
				for(int i = 0; i < tags.length(); i++){
					if(i != 0){
						niceTags += ", ";
					}
					niceTags += tags.getString(i);
				}
									
				int votes = questionObj.getInt("up_vote_count") - questionObj.getInt("down_vote_count");
				
				JSONObject owner = questionObj.getJSONObject("owner");
				
				String userName = "Asked by "+owner.getString("display_name");

				Question question = new Question(
						questionObj.getInt("question_id"),
						questionObj.getString("title"),
						niceTags,
						userName,
						this.site,
						votes,
						questionObj.getInt("answer_count")
				);
				
				//TODO: delete old questions
				
				questions.add(question);
				
			} catch (JSONException e) {
				Log.e("sowidget", e.toString());
			}
		}
        
        return questions;
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
}

