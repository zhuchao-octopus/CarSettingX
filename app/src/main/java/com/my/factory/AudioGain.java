package com.my.factory;

import java.util.Locale;

import com.common.util.AppConfig;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AudioGain {
	private final String TAG = "AudioGain";
	private Context mContext;
	private boolean mIsFactory = true;
	/**
	 * accord with mcu 0 - 20
	 */
	public static final int GAIN_MAX = 20;
	public static final int GAIN_MIN = 0;
	public static final int GAIN_DISP_OFFSET = 10;

	public int gain_host = 4 + GAIN_DISP_OFFSET;
	public int gain_radio = 0 + GAIN_DISP_OFFSET;
	public int gain_dvd = 2 + GAIN_DISP_OFFSET;
	public int gain_bt = -3 + GAIN_DISP_OFFSET;
	public int gain_auxin = -7 + GAIN_DISP_OFFSET;
	public int gain_tv = 0 + GAIN_DISP_OFFSET;

	public AudioGain(Context context, boolean is_factory) {
		mContext = context;
		mIsFactory = is_factory;
		reset();
	}

	public boolean reset() {
		if (mIsFactory) {
			return loadDefault();
		} else {
			gain_host = GAIN_DISP_OFFSET;
			gain_radio = GAIN_DISP_OFFSET;
			gain_dvd = GAIN_DISP_OFFSET;
			gain_bt = GAIN_DISP_OFFSET;
			gain_auxin = GAIN_DISP_OFFSET;
			gain_tv = GAIN_DISP_OFFSET;
			return true;
		}
	}

	private boolean loadDefault() {
		if (mIsFactory) {
			String value = com.common.util.Util
					.getFileString("/sys/class/ak/source/audio_ch_gain");
			if (value != null && !value.isEmpty()) {
				String[] item = value.split(",");
				if (item.length >= 6) {
					try {
						if (item[0] != null)
							gain_host = Integer.valueOf(item[0]);
						else
							return false;
						if (item[1] != null)
							gain_radio = Integer.valueOf(item[1]);
						else
							return false;
						if (item[2] != null)
							gain_dvd = Integer.valueOf(item[2]);
						else
							return false;
						if (item[3] != null)
							gain_bt = Integer.valueOf(item[3]);
						else
							return false;
						if (item[4] != null)
							gain_auxin = Integer.valueOf(item[4]);
						else
							return false;
						if (item[5] != null)
							gain_tv = Integer.valueOf(item[5]);
						else
							return false;
						return true;
					} catch (Exception e) {
						Log.e(TAG, e.toString());
						return false;
					}
				}
			}
		}
		return false;
	}

	public boolean load() {
		String value = null;
		if (mIsFactory)
			value = MachineConfig.getProperty(MachineConfig.KEY_FACTORY_AUDIO_GAIN);
		else
			value = MachineConfig.getProperty(MachineConfig.KEY_USER_AUDIO_GAIN);
		Log.d(TAG, "audioChannelGain " + (mIsFactory ? "factory" : "user") + value);
		if (value != null && !value.isEmpty()) {
			String[] item = value.split(",");
			if (item.length >= 6) {
				if (item[0] != null) {
					try {
						gain_host = Integer.valueOf(item[0]);
					} catch (Exception e) {
						Log.e(TAG, e.toString());
						if (mIsFactory)
							return false;
						else
							gain_host = GAIN_DISP_OFFSET;
					}
				}
				if (item[1] != null) {
					try {
						gain_radio = Integer.valueOf(item[1]);
					} catch (Exception e) {
						Log.e(TAG, e.toString());
						if (mIsFactory)
							return false;
						else
							gain_radio = GAIN_DISP_OFFSET;
					}
				}
				if (item[2] != null) {
					try {
						gain_dvd = Integer.valueOf(item[2]);
					} catch (Exception e) {
						Log.e(TAG, e.toString());
						if (mIsFactory)
							return false;
						else
							gain_dvd = GAIN_DISP_OFFSET;
					}
				}
				if (item[3] != null) {
					try {
						gain_bt = Integer.valueOf(item[3]);
					} catch (Exception e) {
						Log.e(TAG, e.toString());
						if (mIsFactory)
							return false;
						else
							gain_bt = GAIN_DISP_OFFSET;
					}
				}
				if (item[4] != null) {
					try {
						gain_auxin = Integer.valueOf(item[4]);
					} catch (Exception e) {
						Log.e(TAG, e.toString());
						if (mIsFactory)
							return false;
						else
							gain_auxin = GAIN_DISP_OFFSET;
					}
				}
				if (item[5] != null) {
					try {
						gain_tv = Integer.valueOf(item[5]);
					} catch (Exception e) {
						Log.e(TAG, e.toString()); 
						if (mIsFactory)
							return false;
						else
							gain_tv = GAIN_DISP_OFFSET;
					}
				}
				return true;
			}
		}
		return false;
	}

	public void save() {
		String value = String.format(Locale.ENGLISH,"%d,%d,%d,%d,%d,%d", gain_host,
				gain_radio, gain_dvd, gain_bt, gain_auxin, gain_tv);
		if (mIsFactory)
			MachineConfig.setProperty(MachineConfig.KEY_FACTORY_AUDIO_GAIN, value);
		else
			MachineConfig.setProperty(MachineConfig.KEY_USER_AUDIO_GAIN, value);
	}

	public void notifyMcuChanged() {
		try {
			byte[] data = new byte[] { (byte) (mIsFactory ? 0x1 : 0x2),
					(byte) gain_host, (byte) gain_radio, (byte) gain_dvd,
					(byte) gain_bt, (byte) gain_auxin, (byte) gain_tv };
			Intent it = new Intent(MyCmd.BROADCAST_CMD_TO_CAR_SERVICE);

			it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.SET_AUDIO_GAIN);
			it.putExtra(MyCmd.EXTRA_COMMON_OBJECT, data);
			it.setPackage(AppConfig.getCarServicePackageName(mContext));
			mContext.sendBroadcast(it);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}
}
