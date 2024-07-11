package com.my.factory;

import com.common.util.BroadcastUtil;
import com.common.util.MyCmd;
import com.common.view.RepeatingButton;
import com.octopus.android.carsettingx.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

public class VCOMFloatView {
	private static final String TAG = "VCOMFloatView";

	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mLayoutParams;
	private Context mContext;
	private View mView;
	private boolean isShow = false;

	private static VCOMFloatView mVCOMFloatView;
	
	private RepeatingButton mBTNInc, mBTNDec, mBTNCancel, mBTNApply;
	private TextView mTextView;
	private final static int REPEAT_INTERVAL = 100;

	private static final int MSG_ID_VOLUME_LOOP = 201;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case MSG_ID_VOLUME_LOOP:
				break;
			}
		}
	};

	public static VCOMFloatView getInstanse(Context c) {
		if (mVCOMFloatView == null) {
			mVCOMFloatView = new VCOMFloatView(c, R.layout.vcom_adjust_floatview, 150, 0,
					WindowManager.LayoutParams.TYPE_PHONE, true, 1.0f);
		}
		return mVCOMFloatView;
	}

	private VCOMFloatView(Context context, int layoutRid, int weight, int y,
			int type, boolean touchEnable, float alpha) {
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

		mLayoutParams = new LayoutParams(
				weight,
				LayoutParams.WRAP_CONTENT, 0, 0, type, flag,
				PixelFormat.RGBA_8888);
		mLayoutParams.alpha = alpha;
		mLayoutParams.gravity = Gravity.CENTER | Gravity.RIGHT;
		
		mBTNCancel = (RepeatingButton) mView.findViewById(R.id.button_cancel);
		mBTNApply = (RepeatingButton) mView.findViewById(R.id.button_apply);
		mTextView = (TextView) mView.findViewById(R.id.textView);
		mBTNInc = (RepeatingButton) mView.findViewById(R.id.button_inc);
		mBTNDec = (RepeatingButton) mView.findViewById(R.id.button_dec);
		mBTNInc.setText("+");
		mBTNDec.setText("-");
		mBTNCancel.setText(mContext.getResources().getString(R.string.cancel));
		mBTNApply.setText(mContext.getResources().getString(R.string.apply));

		mBTNInc.setOnClickListener(mOnClickListener);
		mBTNDec.setOnClickListener(mOnClickListener);
		mBTNCancel.setOnClickListener(mOnClickListener);
		mBTNApply.setOnClickListener(mOnClickListener);
		mBTNInc.setRepeatListener(onRepeatListener, REPEAT_INTERVAL);
		mBTNDec.setRepeatListener(onRepeatListener, REPEAT_INTERVAL);
	}

	public void show() {
		if (!isShow) {
			isShow = true;
			mWindowManager.addView(mView, mLayoutParams);
			registerListener();
			queryVcomValue();
		}
	}

	public boolean isShowing() {
		return isShow;
	}

	public void hide() {
		if (isShow) {
			isShow = false;
			mWindowManager.removeView(mView);
			unregisterListener();
		}
	}

	RepeatingButton.RepeatListener onRepeatListener = new RepeatingButton.RepeatListener(){
		@Override
		public void onRepeat(View v, long duration, int repeatcount) {
			// TODO Auto-generated method stub	
			if (repeatcount < 0){
				setVCOM(mVcomValue);
				return;
			}
			int id = v.getId();
			if (id == R.id.button_inc) {
				adjustVCOM(true);
			} else if (id == R.id.button_dec) {
				adjustVCOM(false);
			}
		}
    };

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			int id = arg0.getId();
			if (id == R.id.button_inc) {
				adjustVCOM(true);
				setVCOM(mVcomValue);
			} else if (id == R.id.button_dec) {
				adjustVCOM(false);
				setVCOM(mVcomValue);
			} else if (id == R.id.button_cancel) {
				setVCOM(mVcomValueOrg);
				hide();
			} else if (id == R.id.button_apply) {
				hide();
			}
		}
	};

	public void onHome() {
		try {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addCategory(Intent.CATEGORY_HOME);
			mContext.startActivity(intent);
		} catch (Exception e) {
		}
	}

	private int mVcomValue = 0, mVcomValueOrg = 0;;

	private void adjustVCOM(boolean add) {
		if (add) {
			if (mVcomValue < 100) {
				++mVcomValue;
//				BroadcastUtil.sendToCarService(mContext,
//						MyCmd.Cmd.SET_VCOM, mVcomValue);
			}
		} else {
			if (mVcomValue > 0) {
				--mVcomValue;
//				BroadcastUtil.sendToCarService(mContext,
//						MyCmd.Cmd.SET_VCOM, mVcomValue);
			}
		}
		mTextView.setText("VCOM\n" + mVcomValue);
	}
	
	private void setVCOM(int value) {
		mVcomValue = value;
		BroadcastUtil.sendToCarService(mContext, MyCmd.Cmd.SET_VCOM, mVcomValue);
		mTextView.setText("VCOM\n" + mVcomValue);
	}

	private void queryVcomValue() {
		BroadcastUtil.sendToCarService(mContext, MyCmd.Cmd.SET_VCOM, 0x100);
	}

	private BroadcastReceiver mReceiver = null;
	private void registerListener() {
		if (mReceiver == null) {
			mReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();

					if (action.equals(MyCmd.BROADCAST_CAR_SERVICE_SEND)) {

						int cmd = intent.getIntExtra(MyCmd.EXTRA_COMMON_CMD, 0);

						switch (cmd) {
						case MyCmd.Cmd.SET_VCOM:
							mVcomValueOrg = mVcomValue = intent.getIntExtra(
									MyCmd.EXTRA_COMMON_DATA, 0);
							mTextView.setText("VCOM\n" + mVcomValue);
							break;
						}

					}

				}
			};
			IntentFilter iFilter = new IntentFilter();
			iFilter.addAction(MyCmd.BROADCAST_CAR_SERVICE_SEND);

			mContext.registerReceiver(mReceiver, iFilter);
		}
	}

	private void unregisterListener() {
		if (mReceiver != null) {
			mContext.unregisterReceiver(mReceiver);
			mReceiver = null;
		}

	}
}