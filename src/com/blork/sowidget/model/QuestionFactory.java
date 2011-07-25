package com.blork.sowidget.model;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.blork.sowidget.provider.FavouritesContentProvider;
import com.blork.sowidget.provider.QuestionsContentProvider;

public class QuestionFactory {
	public static List<Question> getSaved(Context context) {
		String[] projection = new String[] {
				QuestionsContentProvider.QUESTION_ID,
				QuestionsContentProvider.TITLE,
				QuestionsContentProvider.TAGS,
				QuestionsContentProvider.SITE,
				QuestionsContentProvider.USER_NAME,
				QuestionsContentProvider.VOTES,
				QuestionsContentProvider.ANSWER_COUNT
		};

		Uri questions = QuestionsContentProvider.CONTENT_URI;

		//Make the query. 
		Cursor cursor = context.getContentResolver().query(questions,
				projection, // Which columns to return 
				null,       // Which rows to return (all rows)
				null,       // Selection arguments (none)
				// Put the results in ascending order by name
				QuestionsContentProvider.DEFAULT_SORT_ORDER);

		ArrayList<Question> questionList = new ArrayList<Question>();

		if (cursor.moveToFirst()) {

			int idColumn = cursor.getColumnIndex(QuestionsContentProvider.QUESTION_ID); 
			int titleColumn = cursor.getColumnIndex(QuestionsContentProvider.TITLE); 
			int tagsColumn = cursor.getColumnIndex(QuestionsContentProvider.TAGS); 
			int userNameColumn = cursor.getColumnIndex(QuestionsContentProvider.USER_NAME); 
			int siteColumn = cursor.getColumnIndex(QuestionsContentProvider.SITE); 
			int votesColumn = cursor.getColumnIndex(QuestionsContentProvider.VOTES); 
			int answerCountColumn = cursor.getColumnIndex(QuestionsContentProvider.ANSWER_COUNT); 


			do {

				Question question = new Question(
						cursor.getInt(idColumn),
						cursor.getString(titleColumn),
						cursor.getString(tagsColumn),
						cursor.getString(userNameColumn),
						cursor.getString(siteColumn),
						cursor.getInt(votesColumn),
						cursor.getInt(answerCountColumn)
				);
				
				questionList.add(question);

			} while (cursor.moveToNext());

		} else {
			Log.e("sowidget", "No results!!!");
		}
		
		cursor.close();

		return questionList;
	}
	
	public static List<Question> getFavourites(Context context) {
		String[] projection = new String[] {
				QuestionsContentProvider.QUESTION_ID,
				QuestionsContentProvider.TITLE,
				QuestionsContentProvider.TAGS,
				QuestionsContentProvider.SITE,
				QuestionsContentProvider.USER_NAME,
				QuestionsContentProvider.VOTES,
				QuestionsContentProvider.ANSWER_COUNT
		};

		Uri questions = FavouritesContentProvider.CONTENT_URI;

		//Make the query. 
		Cursor cursor = context.getContentResolver().query(questions,
				projection, // Which columns to return 
				null,       // Which rows to return (all rows)
				null,       // Selection arguments (none)
				// Put the results in ascending order by name
				FavouritesContentProvider.DEFAULT_SORT_ORDER);

		ArrayList<Question> favouritesList = new ArrayList<Question>();

		if (cursor.moveToFirst()) {

			int idColumn = cursor.getColumnIndex(FavouritesContentProvider.QUESTION_ID); 
			int titleColumn = cursor.getColumnIndex(QuestionsContentProvider.TITLE); 
			int tagsColumn = cursor.getColumnIndex(FavouritesContentProvider.TAGS); 
			int userNameColumn = cursor.getColumnIndex(FavouritesContentProvider.USER_NAME); 
			int siteColumn = cursor.getColumnIndex(FavouritesContentProvider.SITE); 
			int votesColumn = cursor.getColumnIndex(FavouritesContentProvider.VOTES); 
			int answerCountColumn = cursor.getColumnIndex(FavouritesContentProvider.ANSWER_COUNT); 


			do {

				Question favourite = new Question(
						cursor.getInt(idColumn),
						cursor.getString(titleColumn),
						cursor.getString(tagsColumn),
						cursor.getString(userNameColumn),
						cursor.getString(siteColumn),
						cursor.getInt(votesColumn),
						cursor.getInt(answerCountColumn)
				);
				
				favouritesList.add(favourite);

			} while (cursor.moveToNext());

		} else {
			Log.e("sowidget", "No results!!!");
		}
		
		cursor.close();

		return favouritesList;
	}
	
	/**
	 * Delete all.
	 *
	 * @param ctx the ctx
	 */
	public static void deleteAll(Context ctx) {
		ContentResolver resolver = ctx.getContentResolver();
		resolver.delete(QuestionsContentProvider.CONTENT_URI, null, null);
	}

}
