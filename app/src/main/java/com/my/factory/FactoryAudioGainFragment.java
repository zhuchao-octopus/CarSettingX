package com.my.factory;

import com.common.util.SystemConfig;
import com.common.util.Util;
import com.octopus.android.carsettingx.R;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

public class FactoryAudioGainFragment extends Fragment {
	private static final String TAG = "AudioGainFragment";
	private AudioGain mAudioGain;
	private TextView mTVTv, mTVAv, mTVDvd, mTVRadio, mTVSystem, mTVBt, mTVMic;
	private Button mBtnTVL, mBtnAvL, mBtnDvdL, mBtnRadioL, mBtnSystemL, mBtnBtL, mBtnMicL;
	private Button mBtnTVR, mBtnAvR, mBtnDvdR, mBtnRadioR, mBtnSystemR, mBtnBtR, mBtnMicR;
	private Button mBtnRestore;
	
	private Handler mHandler = new Handler();
	
	private int mMicGain;
	private final static String MIC_GAIN_PATH = "/sys/class/ak/source/mic_gain";
	
	public static void initMicGain(Context c) {
		int v = SystemConfig.getIntProperty2(c, SystemConfig.KEY_MIC_GAIN);
		if (v >= 0 && v <= 10) {
			Util.setFileValue(MIC_GAIN_PATH, v);
			Log.d(TAG,"mMicGain:"+v);
		}
		else
		{
		        Util.setFileValue(MIC_GAIN_PATH, 10);
		        Log.d(TAG,"mMicGain:10");
		}
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (mAudioGain == null)
		    mAudioGain = new AudioGain(getActivity(), true);
		initMicGain(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_factory_audio_channel_gain, container, false);
		mTVTv = (TextView) view.findViewById(R.id.texttv);
		mTVAv = (TextView) view.findViewById(R.id.textavin);
		mTVDvd = (TextView) view.findViewById(R.id.textdvd);
		mTVRadio = (TextView) view.findViewById(R.id.textradio);
		mTVSystem = (TextView) view.findViewById(R.id.textsystem);
		mTVBt = (TextView) view.findViewById(R.id.textbluetooth);
		mTVMic = (TextView) view.findViewById(R.id.textmic);
		
		mBtnTVL = (Button) view.findViewById(R.id.tvsub);
		mBtnAvL = (Button) view.findViewById(R.id.avinsub);
		mBtnDvdL = (Button) view.findViewById(R.id.dvdsub);
		mBtnRadioL = (Button) view.findViewById(R.id.radiosub);
		mBtnSystemL = (Button) view.findViewById(R.id.systemsub);
		mBtnBtL = (Button) view.findViewById(R.id.bluetoothsub);
		mBtnMicL = (Button) view.findViewById(R.id.micsub);
		
		mBtnTVR = (Button) view.findViewById(R.id.tvadd);
		mBtnAvR = (Button) view.findViewById(R.id.avinadd);
		mBtnDvdR = (Button) view.findViewById(R.id.dvdadd);
		mBtnRadioR = (Button) view.findViewById(R.id.radioadd);
		mBtnSystemR = (Button) view.findViewById(R.id.systemadd);
		mBtnBtR = (Button) view.findViewById(R.id.bluetoothadd);
		mBtnMicR = (Button) view.findViewById(R.id.micadd);
		
		mBtnRestore = (Button) view.findViewById(R.id.restore);
		
		mBtnTVL.setOnClickListener(mOnClickListener);
		mBtnAvL.setOnClickListener(mOnClickListener);
		mBtnDvdL.setOnClickListener(mOnClickListener);
		mBtnRadioL.setOnClickListener(mOnClickListener);
		mBtnSystemL.setOnClickListener(mOnClickListener);
		mBtnBtL.setOnClickListener(mOnClickListener);
		mBtnMicL.setOnClickListener(mOnClickMicListener);
		
		mBtnTVR.setOnClickListener(mOnClickListener);
		mBtnAvR.setOnClickListener(mOnClickListener);
		mBtnDvdR.setOnClickListener(mOnClickListener);
		mBtnRadioR.setOnClickListener(mOnClickListener);
		mBtnSystemR.setOnClickListener(mOnClickListener);
		mBtnBtR.setOnClickListener(mOnClickListener);
		mBtnMicR.setOnClickListener(mOnClickMicListener);
		
		mBtnRestore.setOnClickListener(mOnClickListener);

		if (!mAudioGain.load()) {
			if (!mAudioGain.reset()) {
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						mAudioGain.reset();
						updateUI();
					}
				}, 200);
			}
		}

		return view;
	}

	private void updateUI() {
		mTVTv.setText("" + (mAudioGain.gain_tv - AudioGain.GAIN_DISP_OFFSET));
		mTVAv.setText("" + (mAudioGain.gain_auxin - AudioGain.GAIN_DISP_OFFSET));
		mTVDvd.setText("" + (mAudioGain.gain_dvd - AudioGain.GAIN_DISP_OFFSET));
		mTVRadio.setText("" + (mAudioGain.gain_radio - AudioGain.GAIN_DISP_OFFSET));
		mTVSystem.setText("" + (mAudioGain.gain_host - AudioGain.GAIN_DISP_OFFSET));
		mTVBt.setText("" + (mAudioGain.gain_bt - AudioGain.GAIN_DISP_OFFSET));

		mMicGain = Util.getFileValue(MIC_GAIN_PATH);
		//int v = SystemConfig.getIntProperty2(getActivity(), SystemConfig.KEY_MIC_GAIN);
		//Log.d(TAG,"mMicGain:"+mMicGain+ " value:"+v);
		mTVMic.setText("" + mMicGain);		
	}
	

	OnClickListener mOnClickMicListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			int v = mMicGain;
            int id = arg0.getId();
            if (id == R.id.micsub) {
                v--;
            } else if (id == R.id.micadd) {
                v++;
            }
			
			if (v >= 0 && v <= 10) {
				mMicGain = v;
				Util.setFileValue(MIC_GAIN_PATH, v);
				SystemConfig.setIntProperty(getActivity(), SystemConfig.KEY_MIC_GAIN, v);
				mTVMic.setText("" + mMicGain);
			}
		}
	};

	OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			int rid = arg0.getId();
			
			if (rid == R.id.restore) {
				if (mAudioGain.reset()) {
					updateUI();
					mAudioGain.save();
					mAudioGain.notifyMcuChanged();
				} else {
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							mAudioGain.reset();
							updateUI();
							mAudioGain.save();
							mAudioGain.notifyMcuChanged();
						}
					}, 200);
				}
				return;
			} else if (rid == R.id.tvsub) {
				if (mAudioGain.gain_tv > AudioGain.GAIN_MIN)
					mAudioGain.gain_tv--;
				else
					return;
			} else if (rid == R.id.avinsub) {
				if (mAudioGain.gain_auxin > AudioGain.GAIN_MIN)
					mAudioGain.gain_auxin--;
				else
					return;
			} else if (rid == R.id.dvdsub) {
				if (mAudioGain.gain_dvd > AudioGain.GAIN_MIN)
					mAudioGain.gain_dvd--;
				else
					return;
			} else if (rid == R.id.radiosub) {
				if (mAudioGain.gain_radio > AudioGain.GAIN_MIN)
					mAudioGain.gain_radio--;
				else
					return;
			} else if (rid == R.id.systemsub) {
				if (mAudioGain.gain_host > AudioGain.GAIN_MIN)
					mAudioGain.gain_host--;
				else
					return;
			} else if (rid == R.id.bluetoothsub) {
				if (mAudioGain.gain_bt > AudioGain.GAIN_MIN)
					mAudioGain.gain_bt--;
				else
					return;
			} else if (rid == R.id.tvadd) {
				if (mAudioGain.gain_tv < AudioGain.GAIN_MAX)
					mAudioGain.gain_tv++;
				else
					return;
			} else if (rid == R.id.avinadd) {
				if (mAudioGain.gain_auxin < AudioGain.GAIN_MAX)
					mAudioGain.gain_auxin++;
				else
					return;
			} else if (rid == R.id.dvdadd) {
				if (mAudioGain.gain_dvd < AudioGain.GAIN_MAX)
					mAudioGain.gain_dvd++;
				else
					return;
			} else if (rid == R.id.radioadd) {
				if (mAudioGain.gain_radio < AudioGain.GAIN_MAX)
					mAudioGain.gain_radio++;
				else
					return;
			} else if (rid == R.id.systemadd) {
				if (mAudioGain.gain_host < AudioGain.GAIN_MAX)
					mAudioGain.gain_host++;
				else
					return;
			} else if (rid == R.id.bluetoothadd) {
				if (mAudioGain.gain_bt < AudioGain.GAIN_MAX)
					mAudioGain.gain_bt++;
				else
					return;
			} else {
				return;
			}

			updateUI();
			mAudioGain.save();
			mAudioGain.notifyMcuChanged();
		}
	};

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		updateUI();
	}
}
