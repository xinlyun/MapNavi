package com.gc.materialdesign.widgets;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gc.materialdesign.R;
import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;

public class ProgressDialog extends android.app.Dialog{

	Context context;
	View view;
	View backView;
	String title;
	TextView titleTextView;

	int progressColor = -1;
	ImageView imageView;
	RotateAnimation rotateAnimation;
	private ClickOutSideListener mClickOutSideListener;
	public ProgressDialog(Context context,String title) {
		super(context, android.R.style.Theme_Translucent);
		this.title = title;
		this.context = context;
	}

	public void setOnClickOutSideListener(ClickOutSideListener outSideListener){
		mClickOutSideListener = outSideListener;
	}

	public ProgressDialog(Context context,String title, int progressColor) {
		super(context, android.R.style.Theme_Translucent);
		this.title = title;
		this.progressColor = progressColor;
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.progress_dialog);

		view = (RelativeLayout) findViewById(R.id.contentDialog);
		backView = (RelativeLayout) findViewById(R.id.dialog_rootView);
		backView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getX() < view.getLeft()
						|| event.getX() > view.getRight()
						|| event.getY() > view.getBottom()
						|| event.getY() < view.getTop()) {
//					dismiss();
					if (mClickOutSideListener!=null){
						mClickOutSideListener.OnClickOutSide();
						return true;
					}
				}
				return false;
			}
		});

		this.titleTextView = (TextView) findViewById(R.id.title);
		setTitle(title);
//	    if(progressColor != -1){
//	    	ProgressBarCircularIndeterminate progressBarCircularIndeterminate = (ProgressBarCircularIndeterminate) findViewById(R.id.progressBarCircularIndetermininate);
//	    	progressBarCircularIndeterminate.setBackgroundColor(progressColor);
//		}
		imageView = (ImageView) findViewById(R.id.mimg);
		rotateAnimation = new RotateAnimation(0,359,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
		rotateAnimation.setDuration(1500);
		rotateAnimation.setRepeatCount(-1);
		rotateAnimation.setInterpolator(new LinearInterpolator());
		imageView.setAnimation(rotateAnimation);
		imageView.setVisibility(View.VISIBLE);


	}

	@Override
	public void show() {
		// TODO 自动生成的方法存根
		super.show();
		// set dialog enter animations
		try {
			view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.dialog_main_show_amination));
			backView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.dialog_root_show_amin));
			imageView.startAnimation(rotateAnimation);
		}catch (Exception e){
			e.printStackTrace();
		}

	}

	// GETERS & SETTERS

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
		if(title == null)
			titleTextView.setVisibility(View.GONE);
		else{
			titleTextView.setVisibility(View.VISIBLE);
			titleTextView.setText(title);
		}
	}

	public TextView getTitleTextView() {
		return titleTextView;
	}

	public void setTitleTextView(TextView titleTextView) {
		this.titleTextView = titleTextView;
	}

	@Override
	public void dismiss() {
		try {

			Animation anim = AnimationUtils.loadAnimation(context, R.anim.dialog_main_hide_amination);
			anim.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					view.post(new Runnable() {
						@Override
						public void run() {
							ProgressDialog.super.dismiss();
						}
					});

				}
			});
			Animation backAnim = AnimationUtils.loadAnimation(context, R.anim.dialog_root_hide_amin);

			view.startAnimation(anim);
			backView.startAnimation(backAnim);
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public interface ClickOutSideListener{
		void OnClickOutSide();
	}



}
