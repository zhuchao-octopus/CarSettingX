package com.my.factory;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import com.common.util.MachineConfig;
import com.octopus.android.carsettingx.R;

public class ModelSettingsFragment extends PreferenceFragment implements
		Preference.OnPreferenceChangeListener, OnPreferenceClickListener {
	private static final String TAG = "ModelSettingsFragment";

	private static final String KEY_MODEL = "model";
	private static final String KEY_MCU_VERSION_PREFIX = "mcu_version_prefix";

	private static final String MCU_VERSION_NODE = "/sys/class/ak/version/mcu";

	private Activity mActivity;

	private EditTextPreference mModel;
	private EditTextPreference mMcuVersionPrefix;

	private Handler mHandler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.model_settings);

		mModel = (EditTextPreference) findPreference(KEY_MODEL);
		if (mModel != null) {
			mModel.setOnPreferenceChangeListener(this);
			getModel();
		}

		mMcuVersionPrefix = (EditTextPreference) findPreference(KEY_MCU_VERSION_PREFIX);
		getMCUVersion();
		if (mMcuVersionPrefix != null) {
			mMcuVersionPrefix.setOnPreferenceChangeListener(this);
		}
	}

	private void getModel() {
		String str = MachineConfig.getProperty(MachineConfig.KEY_MODEL);
		if (str == null || str.isEmpty())
			str = Build.MODEL;
		mModel.setText(str);
		mModel.setSummary(str);
	}

	private void getMCUVersion() {
		String str = MachineConfig.getProperty(MachineConfig.KEY_MCU_PREFIX);
		if (str == null || str.isEmpty()) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					getMcuVersionPrefix();
				}
			});
		} else {
			mMcuVersionPrefix.setText(str);
			mMcuVersionPrefix.setSummary(str);
		}
	}

	private void getMcuVersionPrefix() {
		String mcuVersion = com.common.util.Util
				.getFileString(MCU_VERSION_NODE);
		if (mcuVersion == null) {
			try {
				Thread.sleep(300);
				mcuVersion = com.common.util.Util.getFileString(MCU_VERSION_NODE);
			} catch (Exception e) {
			}
		}
		if (mcuVersion != null && !mcuVersion.isEmpty()) {
			String[] str = mcuVersion.split("_");
			if (str != null && str.length > 1) {
				mMcuVersionPrefix.setText(str[0]);
				mMcuVersionPrefix.setSummary(str[0]);
			}
		}
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mActivity = activity;
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		String key = preference.getKey();

		if (KEY_MODEL.equals(key)) {
			MachineConfig.setProperty(MachineConfig.KEY_MODEL, (String) newValue);
			getModel();
		} else if (KEY_MCU_VERSION_PREFIX.equals(key)) {
			MachineConfig.setProperty(MachineConfig.KEY_MCU_PREFIX, (String) newValue);
			getMCUVersion();
		}

		return false;
	}

	public boolean onPreferenceClick(Preference arg0) {
		if (arg0.getKey().equals(KEY_MODEL)) {
		} else if (arg0.getKey().equals(KEY_MCU_VERSION_PREFIX)) {
		}
		return false;
	}
}
