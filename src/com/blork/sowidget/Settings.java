package com.blork.sowidget;

import static android.provider.BaseColumns._ID;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.util.Log;

public class Settings extends PreferenceActivity implements Runnable {

	ListPreference sites;
	Boolean sitelist = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(null);
		addPreferencesFromResource(R.xml.settings);
      
		final CheckBoxPreference droidstack = (CheckBoxPreference) findPreference("droidstack");
		
		final ListPreference method = (ListPreference) findPreference("method");

		if(droidstack.isChecked()) {
			method.setEnabled(false);
		}
		
		droidstack.setEnabled(true);
		
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("droidstack://questions/all"));
		List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent,
		    PackageManager.MATCH_DEFAULT_ONLY);
		if (list.size() == 0) {
			droidstack.setEnabled(false);
			droidstack.setChecked(false);
			method.setEnabled(true);
		} else {
			droidstack.setEnabled(true);
		}
		
		droidstack.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {	
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				method.setEnabled(droidstack.isChecked());
				return true;
			}
		});
		
		Thread thread = new Thread(this);
        thread.start();
	}
	

	@Override
	protected void onResume(){
		super.onResume();
		
		QuestionData qs = new QuestionData(this);
		SQLiteDatabase db = qs.getReadableDatabase();
		try {
			sites = (ListPreference) findPreference("sites");

			Cursor siteCursor = db.query("sites", new String[] {_ID, "name, site_url"}, 
			        null, null, null, null, null);
			startManagingCursor(siteCursor);
			siteCursor.moveToFirst();
			
			int rows = siteCursor.getCount(); 

			
			CharSequence[] names = new CharSequence[rows];
			CharSequence[] urls = new CharSequence[rows];

			int count = 0;
			while (siteCursor.isAfterLast() == false) {
				names[count] = siteCursor.getString(siteCursor.getColumnIndex("name"));
				urls[count] = siteCursor.getString(siteCursor.getColumnIndex("site_url"));
				count++;
				
				siteCursor.moveToNext();
			}
     
			sites.setEntries(names);
			sites.setEntryValues(urls);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			db.close();
		}
	}
	

	 	
	protected void onPause(){
	   super.onPause();
	   Intent intent = new Intent(this, SoService.class);
	   this.startService(intent);
   }





	@Override
	public void run() {
		URL url;
		
    	QuestionData qData = new QuestionData(Settings.this);
		SQLiteDatabase db = qData.getWritableDatabase();
		
		try {
			Log.i("sowidget", "Fetching site list.");
			url = new URL("http://stackauth.com/1.0/sites");

			JSONObject json = (JSONObject) new JSONTokener(SiteWrapper.getJSON(url)).nextValue();
			JSONArray sites = json.getJSONArray("api_sites");
			
			
		
			db.delete("sites", null, null);
	
			
			String siteList = "";
			
			for(int x = 0; x < sites.length(); x++){ 
				JSONObject site =  sites.getJSONObject(x);
				
				ContentValues values = new ContentValues();

				values.put("name", site.getString("name"));
				values.put("site_url", site.getString("api_endpoint"));

				siteList += site.getString("name")+"   ";
				
				db.insertOrThrow("sites", null, values);
			}
			
			Log.i("sowidget", siteList);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			db.close();
			Settings.this.onResume();
		}
	}

}