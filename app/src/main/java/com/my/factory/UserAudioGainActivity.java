package com.my.factory;

import com.common.view.VerticalSeekBar;
import com.octopus.android.carsettingx.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class UserAudioGainActivity extends Activity {
	private static final String TAG = "AudioGainActivity";
	private AudioGain mAudioGain;
	private TextView mTVTv, mTVAv, mTVDvd, mTVRadio, mTVSystem, mTVBt;
	private VerticalSeekBar mVSTv, mVSAv, mVSDvd, mVSRadio, mVSSystem, mVSBt;
	private Button mBtnRestore;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_user_audio_channel_gain);

		if (mAudioGain == null)
			mAudioGain = new AudioGain(this, false);
		mAudioGain.load();

		mTVTv = (TextView) findViewById(R.id.texttv);
		mTVAv = (TextView) findViewById(R.id.textavin);
		mTVDvd = (TextView) findViewById(R.id.textdvd);
		mTVRadio = (TextView) findViewById(R.id.textradio);
		mTVSystem = (TextView) findViewById(R.id.textsystem);
		mTVBt = (TextView) findViewById(R.id.textbluetooth);

		mBtnRestore = (Button) findViewById(R.id.restore);
		mBtnRestore.setOnClickListener(mOnClickListener);
		
		mVSTv = (VerticalSeekBar) findViewById(R.id.seekbar_tv);
		mVSAv = (VerticalSeekBar) findViewById(R.id.seekbar_av);
		mVSDvd = (VerticalSeekBar) findViewById(R.id.seekbar_dvd);
		mVSRadio = (VerticalSeekBar) findViewById(R.id.seekbar_radio);
		mVSSystem = (VerticalSeekBar) findViewById(R.id.seekbar_system);
		mVSBt = (VerticalSeekBar) findViewById(R.id.seekbar_bt);

		mVSTv.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		mVSAv.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		mVSDvd.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		mVSRadio.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		mVSSystem.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		mVSBt.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
	}

	private void updateUI(boolean update_progress) {
		mTVTv.setText("" + (mAudioGain.gain_tv - AudioGain.GAIN_DISP_OFFSET));
		mTVAv.setText("" + (mAudioGain.gain_auxin - AudioGain.GAIN_DISP_OFFSET));
		mTVDvd.setText("" + (mAudioGain.gain_dvd - AudioGain.GAIN_DISP_OFFSET));
		mTVRadio.setText("" + (mAudioGain.gain_radio - AudioGain.GAIN_DISP_OFFSET));
		mTVSystem.setText("" + (mAudioGain.gain_host - AudioGain.GAIN_DISP_OFFSET));
		mTVBt.setText("" + (mAudioGain.gain_bt - AudioGain.GAIN_DISP_OFFSET));

		if (update_progress) {
			mVSTv.setProgress(mAudioGain.gain_tv);
			mVSAv.setProgress(mAudioGain.gain_auxin);
			mVSDvd.setProgress(mAudioGain.gain_dvd);
			mVSRadio.setProgress(mAudioGain.gain_radio);
			mVSSystem.setProgress(mAudioGain.gain_host);
			mVSBt.setProgress(mAudioGain.gain_bt);
		}
	}

	private VerticalSeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new VerticalSeekBar.OnSeekBarChangeListener(){
		@Override
		public void onProgressChanged(VerticalSeekBar seekbar,
				int progress, boolean fromUser) {
			int rid = seekbar.getId();
			if (rid == R.id.seekbar_tv) {
				mAudioGain.gain_tv = progress;
			} else if (rid == R.id.seekbar_av) {
				mAudioGain.gain_auxin = progress;
			} else if (rid == R.id.seekbar_dvd) {
				mAudioGain.gain_dvd = progress;
			} else if (rid == R.id.seekbar_radio) {
				mAudioGain.gain_radio = progress;
			} else if (rid == R.id.seekbar_system) {
				mAudioGain.gain_host = progress;
			} else if (rid == R.id.seekbar_bt) {
				mAudioGain.gain_bt = progress;
			} else {
				return;
			}
			updateUI(false);
			
		}

		@Override
		public void onStartTrackingTouch(VerticalSeekBar seekbar) {
			
		}

		@Override
		public void onStopTrackingTouch(VerticalSeekBar seekbar) {
			mAudioGain.save();
			mAudioGain.notifyMcuChanged();			
		}
	};
	
	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			int rid = arg0.getId();
			
			if (rid == R.id.restore) {
				mAudioGain.reset();
				updateUI(true);
				mAudioGain.save();
				mAudioGain.notifyMcuChanged();
				return;
			}
		}
	};

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		updateUI(true);
	}
}
