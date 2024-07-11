package com.my.factory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.common.util.MachineConfig;
import com.common.util.Util;
import com.octopus.android.carsettingx.R;

public class TouchCalibrationSettings extends Activity {
	private static final String TAG = "TouchCalibrationSettings";
	
	private static final String KERNEL_TOUCH_CALIB_NODE = "/proc/tp_calib";

	private static final int TOUCH_CALIB_STATE_BEGIN = 1;
	private static final int TOUCH_CALIB_STATE_STEP_ONE = 2;
	private static final int TOUCH_CALIB_STATE_STEP_TWO = 3;
	private static final int TOUCH_CALIB_STATE_END = 4;
	
	private TextView mTouchNote;
	private ImageView mTouchStep1;
	private ImageView mTouchStep2;
	
	private int mTouchCalibState;
	
	private int mTX1, mTX2, mTY1, mTY2;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.touch_calibration_setting);
		super.onCreate(savedInstanceState);
		mTouchNote = (TextView)findViewById(R.id.touch_note);
		mTouchStep1 = (ImageView)findViewById(R.id.touch_step_1);
		mTouchStep2 = (ImageView)findViewById(R.id.touch_step_2);
		
		mTouchCalibState = TOUCH_CALIB_STATE_BEGIN;
	}

	@Override
	public void onResume() {
		updateViews();
		super.onResume();	
	}
	
	@Override
	public void onPause() {
		mTouchCalibState = TOUCH_CALIB_STATE_BEGIN;
		Util.setFileValue(KERNEL_TOUCH_CALIB_NODE, "stop_calib");
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	int x, y;
    	x = (int)event.getX();
    	y = (int)event.getY();
    	
        if ( (MotionEvent.ACTION_DOWN == event.getAction()) ||
        		(MotionEvent.ACTION_MOVE == event.getAction()) ){
        	switch (mTouchCalibState) {
        	case TOUCH_CALIB_STATE_BEGIN:
        		mTX1 = 0;
        		mTY1 = 0;
        		mTX2 = 0;
        		mTY2 = 0;
        		break;
        	case TOUCH_CALIB_STATE_STEP_ONE:
        		if (0!=mTX1) {
        			mTX1 = (mTX1+x)/2;
        		} else {
        			mTX1 = x;
        		}
        		if (0!=mTY1) {
        			mTY1 = (mTY1+y)/2;
        		} else {
        			mTY1 = y;
        		}
        		break;
        	case TOUCH_CALIB_STATE_STEP_TWO:
        		if (0!=mTX2) {
        			mTX2 = (mTX2+x)/2;
        		} else {
        			mTX2 = x;
        		}
        		if (0!=mTY2) {
        			mTY2 = (mTY2+y)/2;
        		} else {
        			mTY2 = y;
        		}
        		break;
        	case TOUCH_CALIB_STATE_END:
        		break;
        	}
        } else if (MotionEvent.ACTION_UP== event.getAction()) {
        	switch (mTouchCalibState) {
        	case TOUCH_CALIB_STATE_BEGIN:
        		mTouchCalibState = TOUCH_CALIB_STATE_STEP_ONE;
        		Util.setFileValue(KERNEL_TOUCH_CALIB_NODE, "start_calib");
        		updateViews();
        		break;
        	case TOUCH_CALIB_STATE_STEP_ONE:
        		Log.e(TAG, "step one done: "+mTX1+","+mTY1);
        		mTouchCalibState = TOUCH_CALIB_STATE_STEP_TWO;
        		updateViews();
        		break;
        	case TOUCH_CALIB_STATE_STEP_TWO:
        		Log.e(TAG, "step two done: "+mTX2+","+mTY2);
        		mTouchCalibState = TOUCH_CALIB_STATE_END;
        		updateViews();
        		break;
        	case TOUCH_CALIB_STATE_END:
        		doTouchCalibration();
        		finish();
        		break;
        	}
        }
        
        return super.onTouchEvent(event);
    }	
    
    public void updateViews() {
    	switch (mTouchCalibState) {
    	case TOUCH_CALIB_STATE_BEGIN:
    		mTouchNote.setText(R.string.touch_note_begin);
    		mTouchStep1.setVisibility(View.INVISIBLE);
    		mTouchStep2.setVisibility(View.INVISIBLE);
    		break;
    	case TOUCH_CALIB_STATE_STEP_ONE:
    		mTouchNote.setText(R.string.touch_note_1);
    		mTouchStep1.setVisibility(View.VISIBLE);
    		mTouchStep2.setVisibility(View.INVISIBLE);
    		break;
    	case TOUCH_CALIB_STATE_STEP_TWO:
    		mTouchNote.setText(R.string.touch_note_2);
    		mTouchStep1.setVisibility(View.INVISIBLE);
    		mTouchStep2.setVisibility(View.VISIBLE);
    		break;
    	case TOUCH_CALIB_STATE_END:
    		mTouchNote.setText(R.string.touch_note_end);
    		mTouchStep1.setVisibility(View.INVISIBLE);
    		mTouchStep2.setVisibility(View.INVISIBLE);
    		break;
    	}
    }
    
    private void doTouchCalibration() {
		Util.setFileValue(KERNEL_TOUCH_CALIB_NODE, "stop_calib");

		String kernel_touch_calib = String.format("%d,%dx%d,%d", mTX1, mTY1, mTX2, mTY2);
		
    	Util.setFileValue(KERNEL_TOUCH_CALIB_NODE, kernel_touch_calib);
 
    	MachineConfig.setProperty(MachineConfig.KEY_TP_CALIB, kernel_touch_calib);
    	Util.do_exec("sync");
    }
}
