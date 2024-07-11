package com.my.factory;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import com.octopus.android.carsettingx.R;

public class TouchInfoFloatView {
	private static final String TAG = "TouchScreenInfoView";
	private static TouchInfoFloatView mThis = null;
	private Context mContext;
	private View mView;
	private WindowManager mWindowManager = null;	
	WindowManager.LayoutParams mLayoutParams = null;
	private boolean isShow = false;
	protected TextView mTextView = null;
	
	private static final int MSG_REFRESH_TS_INFO = 100;
	
	private String x_max = null;
	private String y_max = null;
	private String last_x = ""; 
	private String last_y = "";
	private static final String TS_PARAM_PATH = "/sys/module/goodix_gt9xx/parameters/"; 
	
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REFRESH_TS_INFO:
				if (isShow) {
					if (mTextView != null) {
						if (x_max == null)
							x_max = com.common.util.Util.getFileString(TS_PARAM_PATH + "x_max");
						if (y_max == null)
							y_max = com.common.util.Util.getFileString(TS_PARAM_PATH + "y_max");
						String x = com.common.util.Util.getFileString(TS_PARAM_PATH + "x");
						String y = com.common.util.Util.getFileString(TS_PARAM_PATH + "y");
						boolean down = !(x.equals("-1") && y.equals("-1"));
						if (down) {
							last_x = x;
							last_y = y;
						}
						mTextView.setText(
								(down ? "down" : "up") + "                  \n" +
								"max [" + x_max + " , " + y_max + "]\n" +
								" xy [" + (down ? x : last_x) + " , " + (down ? y : last_y) + "]");
					}
					mHandler.sendEmptyMessageDelayed(MSG_REFRESH_TS_INFO, 100);
				}
				break;
			}
		}
	};	


	public static TouchInfoFloatView getIntance(Context context) {
		if (mThis == null) {
			mThis = new TouchInfoFloatView(context);
		}
		return mThis;
	}

	TouchInfoFloatView(Context context) {
		mContext = context;
		if (mWindowManager == null) {
			mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			mLayoutParams = new WindowManager.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, LayoutParams.TYPE_PHONE,
					LayoutParams.FLAG_KEEP_SCREEN_ON
							| LayoutParams.FLAG_NOT_TOUCH_MODAL
							| LayoutParams.FLAG_NOT_FOCUSABLE
							| LayoutParams.FLAG_NOT_TOUCH_MODAL,
					PixelFormat.RGBA_8888);
		}
		mLayoutParams.gravity = Gravity.TOP | Gravity.RIGHT;

		mView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
				.inflate(R.layout.touch_info, null);

		mTextView = (TextView) mView.findViewById(R.id.info);
		mTextView.setTextColor(Color.RED);

		mView.findViewById(R.id.close).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (v.getId() == R.id.close) {
					hide();
				}
			}
		});
	}
	
	public void startMonitor(){
		mHandler.sendEmptyMessage(MSG_REFRESH_TS_INFO);
	}
	
	public void stopMonitor(){
		mHandler.removeMessages(MSG_REFRESH_TS_INFO);
	}
	
	public void show() {
		if (!isShow) {
			isShow = true;
			mWindowManager.addView(mView, mLayoutParams);
			startMonitor();
		}
	}
	
	public boolean isShowing(){
		return isShow;
	}

	public void hide() {
		if (isShow) {
			isShow = false;
			stopMonitor();
			mWindowManager.removeView(mView);
		}
	}
}
