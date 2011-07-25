package com.blork.sowidget.model;

import com.blork.sowidget.provider.FavouritesContentProvider;
import com.blork.sowidget.provider.QuestionsContentProvider;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

public class Question {
	private Integer questionId;
	private String title;
	private String tags;
	private String userName;
	private String site;
	private Integer votes;
	private Integer answerCount;

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
}
