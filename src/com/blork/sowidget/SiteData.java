package com.blork.sowidget;

import static android.provider.BaseColumns._ID;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SiteData extends SQLiteOpenHelper{
	
	private static final String DATABASE_NAME = "sites.db";
	private static final int DATABASE_VERSION = 14; 
	
	/** Create a helper object for the Events database */
	public SiteData(Context ctx){
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db){
		
		db.execSQL("CREATE TABLE sites (" 
				+_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
						"name TEXT," +
						"site_url TEXT);");
	}
	

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		db.execSQL("DROP TABLE IF EXISTS sites");
		onCreate(db);
	}
}