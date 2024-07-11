package com.my.factory;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

import com.octopus.android.carsettingx.R;

public class Finger3HelpFloatView {
	private static final String TAG = "Finger3Help";

	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mLayoutParams;
	private Context mContext;
	private View mView;
	private boolean isShow = false;

	private static Finger3HelpFloatView mFinger3HelpView;

	private ImageView mIVHelp;

	private int curPic = 0;

	private static final int MSG_ID_EXIT = 200;
	private static final int MSG_ID_CHANGE_PIC = 201;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case MSG_ID_CHANGE_PIC:
				curPic++;
				updateImage();
				break;
			case MSG_ID_EXIT:
				hide();
				break;
			}
		}
	};

	public static Finger3HelpFloatView getInstanse(Context c) {
		if (mFinger3HelpView == null) {
			mFinger3HelpView = new Finger3HelpFloatView(c,
					R.layout.finger3_help, 0, 0,
					WindowManager.LayoutParams.TYPE_PHONE, true, 1.0f);
		}
		return mFinger3HelpView;
	}

	private Finger3HelpFloatView(Context context, int layoutRid, int weight,
			int y, int type, boolean touchEnable, float alpha) {
		// TODO Auto-generated method stub
		mContext = context;
		if (mWindowManager == null)
			mWindowManager = (WindowManager) mContext
					.getSystemService(Context.WINDOW_SERVICE);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mView = inflater.inflate(layoutRid, null);
		int flag = 0;

		flag = LayoutParams.FLAG_LAYOUT_NO_LIMITS
				| LayoutParams.FLAG_NOT_FOCUSABLE
				| LayoutParams.FLAG_NOT_TOUCH_MODAL;
		if (!touchEnable) {
			flag |= LayoutParams.FLAG_NOT_TOUCHABLE;
		}
		if (type == LayoutParams.TYPE_SYSTEM_ERROR) {
			flag |= LayoutParams.FLAG_FULLSCREEN
					| LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		}

		mLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT, 0, 0, type, flag,
				PixelFormat.RGBA_8888);
		mLayoutParams.alpha = alpha;
		mLayoutParams.gravity = Gravity.CENTER | Gravity.RIGHT;

		mIVHelp = (ImageView) mView.findViewById(R.id.imageView);
		mIVHelp.setOnClickListener(mOnClickListener);
	}

	private void updateImage() {
		if (mIVHelp != null && mContext != null) {
			if (curPic == 0) {
				mIVHelp.setImageDrawable(mContext.getResources().getDrawable(
						R.drawable.finger3_help0));
			} else if (curPic == 1) {
				mIVHelp.setImageDrawable(mContext.getResources().getDrawable(
						R.drawable.finger3_help1));
			}
		}
	}

	public void show() {
		if (!isShow) {
			isShow = true;
			mWindowManager.addView(mView, mLayoutParams);
		}
		curPic = 0;
		updateImage();
		mHandler.sendEmptyMessageDelayed(MSG_ID_CHANGE_PIC, 3000);
		mHandler.sendEmptyMessageDelayed(MSG_ID_EXIT, 6000);
	}

	public boolean isShowing() {
		return isShow;
	}

	public void hide() {
		if (isShow) {
			isShow = false;
			mWindowManager.removeView(mView);
		}
		mHandler.removeMessages(MSG_ID_CHANGE_PIC);
		mHandler.removeMessages(MSG_ID_EXIT);
	}

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			int id = arg0.getId();
			if (id == R.id.imageView) {
				mHandler.removeMessages(MSG_ID_CHANGE_PIC);
				mHandler.removeMessages(MSG_ID_EXIT);
				if (curPic == 0) {
					curPic++;
					updateImage();
				} else {
					hide();
				}
			}
		}
	};
}