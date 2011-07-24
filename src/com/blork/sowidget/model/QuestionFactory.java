package com.blork.sowidget.model;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;

import com.blork.sowidget.provider.QuestionsContentProvider;

public class QuestionFactory {
	public static List<Question> getSaved(Activity activity) {


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
		Cursor managedCursor = activity.managedQuery(questions,
				projection, // Which columns to return 
				null,       // Which rows to return (all rows)
				null,       // Selection arguments (none)
				// Put the results in ascending order by name
				QuestionsContentProvider.DEFAULT_SORT_ORDER);

		ArrayList<Question> questionList = new ArrayList<Question>();

		if (managedCursor.moveToFirst()) {

			int idColumn = managedCursor.getColumnIndex(QuestionsContentProvider.QUESTION_ID); 
			int titleColumn = managedCursor.getColumnIndex(QuestionsContentProvider.TITLE); 
			int tagsColumn = managedCursor.getColumnIndex(QuestionsContentProvider.TAGS); 
			int userNameColumn = managedCursor.getColumnIndex(QuestionsContentProvider.USER_NAME); 
			int siteColumn = managedCursor.getColumnIndex(QuestionsContentProvider.SITE); 
			int votesColumn = managedCursor.getColumnIndex(QuestionsContentProvider.VOTES); 
			int answerCountColumn = managedCursor.getColumnIndex(QuestionsContentProvider.ANSWER_COUNT); 


			do {

				Question question = new Question(
						managedCursor.getInt(idColumn),
						managedCursor.getString(titleColumn),
						managedCursor.getString(tagsColumn),
						managedCursor.getString(userNameColumn),
						managedCursor.getString(siteColumn),
						managedCursor.getInt(votesColumn),
						managedCursor.getInt(answerCountColumn)
				);
				
				questionList.add(question);

			} while (managedCursor.moveToNext());

		}

		return questionList;
	}
}
