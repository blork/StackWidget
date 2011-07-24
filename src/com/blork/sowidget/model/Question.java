package com.blork.sowidget.model;

public class Question {
	private Integer questionId;
	private String title;
	private String tags;
	private String userName;
	private String site;
	private Integer votes;
	private Integer answerCount;
	
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
	
}
