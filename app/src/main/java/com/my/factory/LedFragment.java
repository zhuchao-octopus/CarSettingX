package com.my.factory;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.common.util.MachineConfig;
import com.common.util.Util;
import com.octopus.android.carsettingx.R;

public class LedFragment extends PreferenceFragment implements OnClickListener, OnSeekBarChangeListener {
	
	private static final String TAG = "LedFragment";
	private static final String MCU_LED_COLOR_NODE = "/sys/class/ak/source/led_color";
	
	private Activity mActivity;
	private int mInitRgb;
	private View mCurrentColorView;
	private SeekBar mSeekBarRed;
	private SeekBar mSeekBarGreen;
	private SeekBar mSeekBarBlue;
	private int mCurLedType = 0;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mCurLedType = MachineConfig.getPropertyIntOnce(MachineConfig.KEY_LED_TYPE);
		mActivity = getActivity();
		mInitRgb = getLedSetting();
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final Context context = inflater.getContext();
        final View view = inflater.inflate(R.layout.led_setting, container, false);

        mCurrentColorView = view.findViewById(R.id.cur_led_color);
        setCurrentColorView(mInitRgb);

        view.findViewById(R.id.color_red).setOnClickListener(this);
        view.findViewById(R.id.color_green).setOnClickListener(this);
        view.findViewById(R.id.color_blue).setOnClickListener(this);
        view.findViewById(R.id.color_fe500a).setOnClickListener(this);
        view.findViewById(R.id.color_80fe80).setOnClickListener(this);
        view.findViewById(R.id.color_8080fe).setOnClickListener(this);
        view.findViewById(R.id.color_808000).setOnClickListener(this);
        view.findViewById(R.id.color_white).setOnClickListener(this);
        view.findViewById(R.id.color_bar).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);

        mSeekBarRed = (SeekBar) view.findViewById(R.id.seekbar_red);
        mSeekBarRed.setOnSeekBarChangeListener(this);
        mSeekBarGreen = (SeekBar) view.findViewById(R.id.seekbar_green);
        mSeekBarGreen.setOnSeekBarChangeListener(this);
        mSeekBarBlue = (SeekBar) view.findViewById(R.id.seekbar_blue);
        mSeekBarBlue.setOnSeekBarChangeListener(this);
        updateSeekBar(mInitRgb);
        
        return view;
    }
    
	@Override
	public void onPause() {
		super.onPause();
//		ActionBar ab = mActivity.getActionBar();
//		if (null!=ab) {
			mActivity.setTitle(mOldTitle);
//		}
	}

	private String mOldTitle = "";
	@Override
	public void onResume() {
		super.onResume();
		mOldTitle = mActivity.getActionBar().getTitle().toString();
//		ActionBar ab = mActivity.getActionBar();
//		if (null!=ab) {
			mActivity.setTitle(R.string.led_settings_title);
//			ab.setTitle(R.string.led_settings_title);
//		}
	}

	private int getLedSetting() {
		int rgb = Util.getFileValue(MCU_LED_COLOR_NODE);
		if (mCurLedType == 2) { // BGR
			rgb = (rgb & 0xFF000000) | ((rgb & 0xFF) << 16 ) | (rgb & 0xFF00) | ((rgb >> 16) & 0xFF);
		}
		return rgb;
	}
	private void setLedSetting(int rgb) {
		if (mCurLedType == 2) { // BGR
			rgb = (rgb & 0xFF000000) | ((rgb & 0xFF) << 16 ) | (rgb & 0xFF00) | ((rgb >> 16) & 0xFF);
		}
		Util.setFileValue(MCU_LED_COLOR_NODE, rgb);
	}

	private void setCurrentColorView(int rgb) {
		if ((rgb & 0xffffff) == 0xffffff) {
			mCurrentColorView.setBackgroundResource(R.drawable.colorbar);
		} else {
			rgb = rgb & 0x00FFFFFF;
			mCurrentColorView.setBackgroundColor(0xFF000000 | rgb);
		}
	}

	@Override
	public void onClick(View arg0) {
		int rgb = 0;
        int id = arg0.getId();
        if (id == R.id.color_red) {
            rgb = 0xFE0000;
        } else if (id == R.id.color_green) {
            rgb = 0x00FE00;
        } else if (id == R.id.color_blue) {
            rgb = 0x0000FE;
        } else if (id == R.id.color_fe500a) {
            rgb = 0xFE2000;
        } else if (id == R.id.color_80fe80) {
            rgb = 0x80FE80;
        } else if (id == R.id.color_8080fe) {
            rgb = 0x8080FE;
        } else if (id == R.id.color_808000) {
            rgb = 0x804d01;
        } else if (id == R.id.color_white) {
            rgb = 0xFEFEFE;
        } else if (id == R.id.color_bar) {
            rgb = 0xFFFFFF;
        } else if (id == R.id.btn_cancel) {
            rgb = mInitRgb;
        }
		if (0 != rgb) {
			setLedSetting(rgb);
			setCurrentColorView(rgb);
			updateSeekBar(rgb);
		}
		if ((R.id.btn_cancel == arg0.getId()) || (R.id.btn_ok == arg0.getId())) {
			// mActivity.finish();
			mActivity.getFragmentManager().popBackStack(null,
					FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}
	}
	
	private Handler mHandler = new Handler();
	private boolean ignoreOnProgressChanged = false;
	private void updateSeekBar(int rgb) {
		ignoreOnProgressChanged = true;
		if ((rgb & 0xffffff) == 0xffffff) {
			mSeekBarRed.setProgress(0xfe);
			mSeekBarGreen.setProgress(0xfe);
			mSeekBarBlue.setProgress(0xfe);
		} else {
			mSeekBarRed.setProgress((rgb & 0xFF0000) >> 16);
			mSeekBarGreen.setProgress((rgb & 0xFF00) >> 8);
			mSeekBarBlue.setProgress((rgb & 0xFF) >> 0);
		}
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				ignoreOnProgressChanged = false;
			}
		}, 500);
	}

	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		if (ignoreOnProgressChanged)
			return;
		int r = mSeekBarRed.getProgress();
		int g = mSeekBarGreen.getProgress();
		int b = mSeekBarBlue.getProgress();
		if (r <= 0)
			r = 1;
		if (r >= 0xff)
			r = 0xfe;
		if (g <= 0)
			g = 1;
		if (g >= 0xff)
			g = 0xfe;
		if (b <= 0)
			b = 1;
		if (b >= 0xff)
			b = 0xfe;
		setCurrentColorView((r << 16) | (g << 8) | b);
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		int r = mSeekBarRed.getProgress();
		int g = mSeekBarGreen.getProgress();
		int b = mSeekBarBlue.getProgress();
		if (r <= 0)
			r = 1;
		if (r >= 0xff)
			r = 0xfe;
		if (g <= 0)
			g = 1;
		if (g >= 0xff)
			g = 0xfe;
		if (b <= 0)
			b = 1;
		if (b >= 0xff)
			b = 0xfe;
		setLedSetting((r << 16) | (g << 8) | b);
	}

}
