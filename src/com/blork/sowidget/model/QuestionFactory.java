package com.blork.sowidget.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

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

		}
		
		cursor.close();

		return questionList;
	}
}
