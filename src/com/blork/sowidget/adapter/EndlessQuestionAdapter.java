package com.blork.sowidget.adapter;

import android.app.Activity;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import com.blork.sowidget.R;
import com.commonsware.cwac.endless.EndlessAdapter;

public class EndlessQuestionAdapter extends EndlessAdapter {
	private RotateAnimation rotate=null;
	private Activity activity;

	EndlessQuestionAdapter(Activity activity, ListAdapter adapter) {
		super(adapter);
		
		this.activity = activity;

		rotate=new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rotate.setDuration(600);
		rotate.setRepeatMode(Animation.RESTART);
		rotate.setRepeatCount(Animation.INFINITE);
	}

	@Override
	protected View getPendingView(ViewGroup parent) {
		View row = activity.getLayoutInflater().inflate(R.layout.question_list_item, null);

//		View child=row.findViewById(android.R.id.text1);
//
//		child.setVisibility(View.GONE);
//
//		child=row.findViewById(R.id.throbber);
//		child.setVisibility(View.VISIBLE);
//		child.startAnimation(rotate);

		return(row);
	}

	@Override
	protected boolean cacheInBackground() {
		SystemClock.sleep(10000);				// pretend to do work

		return(getWrappedAdapter().getCount()<75);
	}

	@Override
	protected void appendCachedData() {
		if (getWrappedAdapter().getCount()<75) {
			@SuppressWarnings("unchecked")
			ArrayAdapter<Integer> a=(ArrayAdapter<Integer>)getWrappedAdapter();

			for (int i=0;i<25;i++) { a.add(a.getCount()); }
		}
	}
}