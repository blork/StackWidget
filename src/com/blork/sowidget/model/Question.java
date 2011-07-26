package com.blork.sowidget.model;

import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.blork.sowidget.SiteWrapper;
import com.blork.sowidget.provider.FavouritesContentProvider;
import com.blork.sowidget.provider.QuestionsContentProvider;

public class Question {
	private Integer questionId;
	private String title;
	private String tags;
	private String userName;
	private String site;
	private Integer votes;
	private Integer answerCount;
	private String siteName;

	/**
	 * @param questionId
	 * @param title
	 * @param tags
	 * @param userName
	 * @param site
	 * @param votes
	 * @param answerCount
	 */
	public Question(Integer questionId, String title, String tags,
			String userName, String site, Integer votes, Integer answerCount) {
		super();
		this.questionId = questionId;
		this.title = title;
		this.tags = tags;
		this.userName = userName;
		this.site = site;
		this.votes = votes;
		this.answerCount = answerCount;
	}

	public Integer getQuestionId() {
		return questionId;
	}

	public String getTitle() {
		return title;
	}

	public String getTags() {
		return tags;
	}

	public String getUserName() {
		return userName;
	}

	public String getSite() {
		return site;
	}

	public String getSiteName() {
		if (this.siteName == null) {
			this.siteName = this.site.replaceAll("http://api.", "").replaceAll(".com", "");
		}

		return this.siteName;
	}

	public Integer getVotes() {
		return votes;
	}

	public Integer getAnswerCount() {
		return answerCount;
	}

	public Uri save(Context context) {
		ContentValues values = new ContentValues();

		values.put(QuestionsContentProvider.QUESTION_ID, this.questionId);
		values.put(QuestionsContentProvider.TITLE, this.title);
		values.put(QuestionsContentProvider.TAGS, this.tags);
		values.put(QuestionsContentProvider.USER_NAME, this.userName);
		values.put(QuestionsContentProvider.SITE, this.site);
		values.put(QuestionsContentProvider.VOTES, this.votes);
		values.put(QuestionsContentProvider.ANSWER_COUNT, this.answerCount);

		Uri uri = context.getContentResolver().insert(QuestionsContentProvider.CONTENT_URI, values);
		return uri;
	}

	public boolean addToFavorites(Context context) {
		ContentValues values = new ContentValues();

		values.put(FavouritesContentProvider.QUESTION_ID, this.questionId);
		values.put(FavouritesContentProvider.TITLE, this.title);
		values.put(FavouritesContentProvider.TAGS, this.tags);
		values.put(FavouritesContentProvider.USER_NAME, this.userName);
		values.put(FavouritesContentProvider.SITE, this.site);
		values.put(FavouritesContentProvider.VOTES, this.votes);
		values.put(FavouritesContentProvider.ANSWER_COUNT, this.answerCount);

		try {
			context.getContentResolver().insert(FavouritesContentProvider.CONTENT_URI, values);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public boolean removeFromFavourites(Context context) {
		int deletedRows = context.getContentResolver().delete(
				FavouritesContentProvider.CONTENT_URI, 
				FavouritesContentProvider.QUESTION_ID + " = " + this.questionId, 
				null
		);
		return deletedRows == 1;
	}

	@Override
	public String toString() {
		return "Question [questionId=" + questionId + ", title=" + title + "]";
	}

	public Boolean hasNewAnswers(Context context) {
		try { 
			URL url = new URL(
					this.site
					+ "/"
					+ SiteWrapper.VERSION
					+ "/questions/"
					+ this.questionId
					+ "?key="
					+ SiteWrapper.KEY
			);


			JSONObject json = (JSONObject) new JSONTokener(SiteWrapper.getJSON(url)).nextValue();
			JSONArray questions = json.getJSONArray("questions");

			JSONObject question =  questions.getJSONObject(0);

			int newAnswerCount = question.getInt("answer_count");

			if(newAnswerCount > this.answerCount) {
				this.updateAnswerCount(newAnswerCount, context);
				return true;
			}

		} catch (Exception e) {}

		return false;
	}

	public void updateAnswerCount(int newAnswerCount, Context context) {
		ContentValues values = new ContentValues();
		values.put(FavouritesContentProvider.ANSWER_COUNT, this.answerCount);

		try {
			context.getContentResolver().update(
					FavouritesContentProvider.CONTENT_URI, 
					values,
					FavouritesContentProvider.QUESTION_ID + " = " + this.questionId, 
					null
			);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
