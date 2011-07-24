package com.blork.sowidget.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.blork.sowidget.R;
import com.blork.sowidget.model.Question;


public class QuestionAdapter extends ArrayAdapter<Question> {

	private List<Question> questions;
	private Context context;

	public QuestionAdapter(Context context, int textViewResourceId, List<Question> items) {
		super(context, textViewResourceId, items);
		this.questions = items;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.question_list_item, null);
		}
		Question question = questions.get(position);
		if (question != null) {
			TextView title = (TextView) v.findViewById(R.id.title);
			TextView votes = (TextView) v.findViewById(R.id.votes);
			TextView answers = (TextView) v.findViewById(R.id.answers);
			TextView user = (TextView) v.findViewById(R.id.user);
			TextView tags = (TextView) v.findViewById(R.id.tags);
			TextView id = (TextView) v.findViewById(R.id.id);
			TextView site = (TextView) v.findViewById(R.id.site);

			if (title != null) {
				title.setText(question.getTitle());                           
			}
			if (votes != null) {
				votes.setText(question.getVotes() + " votes");                           
			}
			if (answers != null) {
				answers.setText(question.getAnswerCount() + " answers");                           
			}
			if (user != null) {
				user.setText(question.getUserName());                           
			}
			if (tags != null) {
				tags.setText(question.getTags());                           
			}
			if (id != null) {
				id.setText(question.getQuestionId().toString());                           
			}
			if (site != null) {
				site.setText(question.getSite());                           
			}
		}
		return v;
	}
}