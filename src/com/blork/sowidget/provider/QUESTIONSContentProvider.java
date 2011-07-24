/*
 ******************************************************************************
 * Parts of this code sample are licensed under Apache License, Version 2.0   *
 * Copyright (c) 2009, Android Open Handset Alliance. All rights reserved.    *
 *																			  *																			*
 * Except as noted, this code sample is offered under a modified BSD license. *
 * Copyright (C) 2010, Motorola Mobility, Inc. All rights reserved.           *
 * 																			  *
 * For more details, see MOTODEV_Studio_for_Android_LicenseNotices.pdf        * 
 * in your installation folder.                                               *
 ******************************************************************************
 */
package com.blork.sowidget.provider;

import java.util.*;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.net.*;
import android.text.*;

import com.blork.sowidget.sql.*;

public class QuestionsContentProvider extends ContentProvider {

	private QuestionsSQLHelper dbHelper;
	private static HashMap<String, String> QUESTIONS_PROJECTION_MAP;
	private static final String TABLE_NAME = "questions";
	private static final String AUTHORITY = "com.blork.sowidget.provider.questionscontentprovider";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + TABLE_NAME);
	public static final Uri TITLE_FIELD_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE_NAME.toLowerCase() + "/title");
	public static final Uri QUESTION_ID_FIELD_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE_NAME.toLowerCase() + "/question_id");
	public static final Uri TAGS_FIELD_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE_NAME.toLowerCase() + "/tags");
	public static final Uri VOTES_FIELD_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE_NAME.toLowerCase() + "/votes");
	public static final Uri ANSWER_COUNT_FIELD_CONTENT_URI = Uri
			.parse("content://" + AUTHORITY + "/" + TABLE_NAME.toLowerCase()
					+ "/answer_count");
	public static final Uri USER_NAME_FIELD_CONTENT_URI = Uri
			.parse("content://" + AUTHORITY + "/" + TABLE_NAME.toLowerCase()
					+ "/user_name");
	public static final Uri SITE_FIELD_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE_NAME.toLowerCase() + "/site");

	public static final String DEFAULT_SORT_ORDER = "question_id desc";

	private static final UriMatcher URL_MATCHER;

	private static final int QUESTIONS = 1;
	private static final int QUESTIONS_QUESTION_ID = 2;
	private static final int QUESTIONS_TITLE = 3;
	private static final int QUESTIONS_TAGS = 4;
	private static final int QUESTIONS_VOTES = 5;
	private static final int QUESTIONS_ANSWER_COUNT = 6;
	private static final int QUESTIONS_USER_NAME = 7;
	private static final int QUESTIONS_SITE = 8;

	// Content values keys (using column names)
	public static final String QUESTION_ID = "question_id";
	public static final String TITLE = "title";
	public static final String TAGS = "tags";
	public static final String VOTES = "votes";
	public static final String ANSWER_COUNT = "answer_count";
	public static final String USER_NAME = "user_name";
	public static final String SITE = "site";

	public boolean onCreate() {
		dbHelper = new QuestionsSQLHelper(getContext(), true);
		return (dbHelper == null) ? false : true;
	}

	public Cursor query(Uri url, String[] projection, String selection,
			String[] selectionArgs, String sort) {
		SQLiteDatabase mDB = dbHelper.getReadableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		switch (URL_MATCHER.match(url)) {
		case QUESTIONS:
			qb.setTables(TABLE_NAME);
			qb.setProjectionMap(QUESTIONS_PROJECTION_MAP);
			break;
		case QUESTIONS_QUESTION_ID:
			qb.setTables(TABLE_NAME);
			qb.appendWhere("question_id='" + url.getPathSegments().get(2) + "'");
			break;
		case QUESTIONS_TITLE:
			qb.setTables(TABLE_NAME);
			qb.appendWhere("title='" + url.getPathSegments().get(2) + "'");
			break;
		case QUESTIONS_TAGS:
			qb.setTables(TABLE_NAME);
			qb.appendWhere("tags='" + url.getPathSegments().get(2) + "'");
			break;
		case QUESTIONS_VOTES:
			qb.setTables(TABLE_NAME);
			qb.appendWhere("votes='" + url.getPathSegments().get(2) + "'");
			break;
		case QUESTIONS_ANSWER_COUNT:
			qb.setTables(TABLE_NAME);
			qb.appendWhere("answer_count='" + url.getPathSegments().get(2)
					+ "'");
			break;
		case QUESTIONS_USER_NAME:
			qb.setTables(TABLE_NAME);
			qb.appendWhere("user_name='" + url.getPathSegments().get(2) + "'");
			break;
		case QUESTIONS_SITE:
			qb.setTables(TABLE_NAME);
			qb.appendWhere("site='" + url.getPathSegments().get(2) + "'");
			break;

		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}
		String orderBy = "";
		if (TextUtils.isEmpty(sort)) {
			orderBy = DEFAULT_SORT_ORDER;
		} else {
			orderBy = sort;
		}
		Cursor c = qb.query(mDB, projection, selection, selectionArgs, null,
				null, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), url);
		return c;
	}

	public String getType(Uri url) {
		switch (URL_MATCHER.match(url)) {
		case QUESTIONS:
			return "vnd.android.cursor.dir/vnd.com.blork.sowidget.provider.questions";
		case QUESTIONS_QUESTION_ID:
			return "vnd.android.cursor.item/vnd.com.blork.sowidget.provider.questions";
		case QUESTIONS_TITLE:
			return "vnd.android.cursor.item/vnd.com.blork.sowidget.provider.questions";
		case QUESTIONS_TAGS:
			return "vnd.android.cursor.item/vnd.com.blork.sowidget.provider.questions";
		case QUESTIONS_VOTES:
			return "vnd.android.cursor.item/vnd.com.blork.sowidget.provider.questions";
		case QUESTIONS_ANSWER_COUNT:
			return "vnd.android.cursor.item/vnd.com.blork.sowidget.provider.questions";
		case QUESTIONS_USER_NAME:
			return "vnd.android.cursor.item/vnd.com.blork.sowidget.provider.questions";
		case QUESTIONS_SITE:
			return "vnd.android.cursor.item/vnd.com.blork.sowidget.provider.questions";

		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}
	}

	public Uri insert(Uri url, ContentValues initialValues) {
		SQLiteDatabase mDB = dbHelper.getWritableDatabase();
		long rowID;
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}
		if (URL_MATCHER.match(url) != QUESTIONS) {
			throw new IllegalArgumentException("Unknown URL " + url);
		}

		rowID = mDB.insert("questions", "questions", values);
		if (rowID > 0) {
			Uri uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		}
		throw new SQLException("Failed to insert row into " + url);
	}

	public int delete(Uri url, String where, String[] whereArgs) {
		SQLiteDatabase mDB = dbHelper.getWritableDatabase();
		int count;
		String segment = "";
		switch (URL_MATCHER.match(url)) {
		case QUESTIONS:
			count = mDB.delete(TABLE_NAME, where, whereArgs);
			break;
		case QUESTIONS_QUESTION_ID:
			segment = "'" + url.getPathSegments().get(2) + "'";
			count = mDB.delete(TABLE_NAME,
					"question_id="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case QUESTIONS_TITLE:
			segment = "'" + url.getPathSegments().get(2) + "'";
			count = mDB.delete(TABLE_NAME,
					"title="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case QUESTIONS_TAGS:
			segment = "'" + url.getPathSegments().get(2) + "'";
			count = mDB.delete(TABLE_NAME,
					"tags="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case QUESTIONS_VOTES:
			segment = "'" + url.getPathSegments().get(2) + "'";
			count = mDB.delete(TABLE_NAME,
					"votes="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case QUESTIONS_ANSWER_COUNT:
			segment = "'" + url.getPathSegments().get(2) + "'";
			count = mDB.delete(TABLE_NAME,
					"answer_count="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case QUESTIONS_USER_NAME:
			segment = "'" + url.getPathSegments().get(2) + "'";
			count = mDB.delete(TABLE_NAME,
					"user_name="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case QUESTIONS_SITE:
			segment = "'" + url.getPathSegments().get(2) + "'";
			count = mDB.delete(TABLE_NAME,
					"site="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}
		getContext().getContentResolver().notifyChange(url, null);
		return count;
	}

	public int update(Uri url, ContentValues values, String where,
			String[] whereArgs) {
		SQLiteDatabase mDB = dbHelper.getWritableDatabase();
		int count;
		String segment = "";
		switch (URL_MATCHER.match(url)) {
		case QUESTIONS:
			count = mDB.update(TABLE_NAME, values, where, whereArgs);
			break;
		case QUESTIONS_QUESTION_ID:
			segment = "'" + url.getPathSegments().get(2) + "'";
			count = mDB.update(TABLE_NAME, values,
					"question_id="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case QUESTIONS_TITLE:
			segment = "'" + url.getPathSegments().get(2) + "'";
			count = mDB.update(TABLE_NAME, values,
					"title="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case QUESTIONS_TAGS:
			segment = "'" + url.getPathSegments().get(2) + "'";
			count = mDB.update(TABLE_NAME, values,
					"tags="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case QUESTIONS_VOTES:
			segment = "'" + url.getPathSegments().get(2) + "'";
			count = mDB.update(TABLE_NAME, values,
					"votes="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case QUESTIONS_ANSWER_COUNT:
			segment = "'" + url.getPathSegments().get(2) + "'";
			count = mDB.update(TABLE_NAME, values,
					"answer_count="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case QUESTIONS_USER_NAME:
			segment = "'" + url.getPathSegments().get(2) + "'";
			count = mDB.update(TABLE_NAME, values,
					"user_name="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case QUESTIONS_SITE:
			segment = "'" + url.getPathSegments().get(2) + "'";
			count = mDB.update(TABLE_NAME, values,
					"site="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}
		getContext().getContentResolver().notifyChange(url, null);
		return count;
	}

	static {
		URL_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URL_MATCHER.addURI(AUTHORITY, TABLE_NAME.toLowerCase(), QUESTIONS);
		URL_MATCHER.addURI(AUTHORITY, TABLE_NAME.toLowerCase() + "/question_id"
				+ "/*", QUESTIONS_QUESTION_ID);
		URL_MATCHER.addURI(AUTHORITY, TABLE_NAME.toLowerCase() + "/title"
				+ "/*", QUESTIONS_TITLE);
		URL_MATCHER.addURI(AUTHORITY,
				TABLE_NAME.toLowerCase() + "/tags" + "/*", QUESTIONS_TAGS);
		URL_MATCHER.addURI(AUTHORITY, TABLE_NAME.toLowerCase() + "/votes"
				+ "/*", QUESTIONS_VOTES);
		URL_MATCHER.addURI(AUTHORITY, TABLE_NAME.toLowerCase()
				+ "/answer_count" + "/*", QUESTIONS_ANSWER_COUNT);
		URL_MATCHER.addURI(AUTHORITY, TABLE_NAME.toLowerCase() + "/user_name"
				+ "/*", QUESTIONS_USER_NAME);
		URL_MATCHER.addURI(AUTHORITY,
				TABLE_NAME.toLowerCase() + "/site" + "/*", QUESTIONS_SITE);

		QUESTIONS_PROJECTION_MAP = new HashMap<String, String>();
		QUESTIONS_PROJECTION_MAP.put(QUESTION_ID, "question_id");
		QUESTIONS_PROJECTION_MAP.put(TITLE, "title");
		QUESTIONS_PROJECTION_MAP.put(TAGS, "tags");
		QUESTIONS_PROJECTION_MAP.put(VOTES, "votes");
		QUESTIONS_PROJECTION_MAP.put(ANSWER_COUNT, "answer_count");
		QUESTIONS_PROJECTION_MAP.put(USER_NAME, "user_name");
		QUESTIONS_PROJECTION_MAP.put(SITE, "site");

	}
}
