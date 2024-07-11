package com.my.factory;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.preference.SwitchPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.util.Log;
import com.common.util.MachineConfig;
import com.common.util.MyCmd.Keycode;
import com.octopus.android.carsettingx.R;

public class SuperSettingsFragment extends PreferenceFragment implements
		Preference.OnPreferenceChangeListener, OnPreferenceClickListener {
	private static final String TAG = "SuperSettingsFragment";

	private static final String KEY_TOUCH3_SETTINGS = "touch3_settings";
	private static final String KEY_TOUCH3_SWITCH = "touch3_settings_switch";
	private static final String KEY_TOUCH3_UPKEY = "touch3_settings_upkey";
	private static final String KEY_TOUCH3_DOWNKEY = "touch3_settings_downkey";
	private static final String KEY_TOUCH3_LEFTKEY = "touch3_settings_leftkey";
	private static final String KEY_TOUCH3_RIGHTKEY = "touch3_settings_rightkey";

	private Activity mActivity;
	private Preference mTouch3IdentifyPreference;
	private SwitchPreference mTouch3Switch;
	private ListPreference mTouch3UpKey;
	private ListPreference mTouch3DownKey;
	private ListPreference mTouch3LeftKey;
	private ListPreference mTouch3RightKey;

	private Touch3Config mTouch3Info = new Touch3Config();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
//		mActivity = getActivity();
		addPreferencesFromResource(R.xml.super_settings);

		mTouch3IdentifyPreference = findPreference(KEY_TOUCH3_SETTINGS);
		if (mTouch3IdentifyPreference != null) {
			mTouch3Switch = (SwitchPreference) findPreference(KEY_TOUCH3_SWITCH);
			mTouch3UpKey = (ListPreference) findPreference(KEY_TOUCH3_UPKEY);
			mTouch3DownKey = (ListPreference) findPreference(KEY_TOUCH3_DOWNKEY);
			mTouch3LeftKey = (ListPreference) findPreference(KEY_TOUCH3_LEFTKEY);
			mTouch3RightKey = (ListPreference) findPreference(KEY_TOUCH3_RIGHTKEY);
			if (mTouch3Switch != null)
				mTouch3Switch.setOnPreferenceChangeListener(this);
			if (mTouch3UpKey != null)
				mTouch3UpKey.setOnPreferenceChangeListener(this);
			if (mTouch3DownKey != null)
				mTouch3DownKey.setOnPreferenceChangeListener(this);
			if (mTouch3LeftKey != null)
				mTouch3LeftKey.setOnPreferenceChangeListener(this);
			if (mTouch3RightKey != null)
				mTouch3RightKey.setOnPreferenceChangeListener(this);
			mTouch3Info.loadTouch3Entries(getActivity());
			mTouch3UpKey.setEntries(mTouch3Info.mEntries);
			mTouch3UpKey.setEntryValues(mTouch3Info.mEntriesVlue);
			mTouch3DownKey.setEntries(mTouch3Info.mEntries);
			mTouch3DownKey.setEntryValues(mTouch3Info.mEntriesVlue);
			mTouch3LeftKey.setEntries(mTouch3Info.mEntries);
			mTouch3LeftKey.setEntryValues(mTouch3Info.mEntriesVlue);
			mTouch3RightKey.setEntries(mTouch3Info.mEntries);
			mTouch3RightKey.setEntryValues(mTouch3Info.mEntriesVlue);
			getTouch3ConfigValue(getActivity());
		}
	}

	private void getTouch3ConfigValue(Context context) {
//		String value = SystemConfig.getProperty(context, MachineConfig.KEY_TOUCH3_IDENTIFY);
		String value = MachineConfig.getProperty(MachineConfig.KEY_TOUCH3_IDENTIFY);
//		Log.d(TAG, "getTouch3ConfigValue: " + value);
		if (value == null || value.isEmpty()) {
			updateTouch3SwitchPreference(false);
			updateTouch3Preference(mTouch3UpKey, "keycode:" + String.valueOf(Keycode.NONE), false);
			updateTouch3Preference(mTouch3DownKey, "keycode:" + String.valueOf(Keycode.NONE), false);
			updateTouch3Preference(mTouch3LeftKey, "keycode:" + String.valueOf(Keycode.PREVIOUS), false);
			updateTouch3Preference(mTouch3RightKey, "keycode:" + String.valueOf(Keycode.NEXT), false);
		} else {
			JSONObject jobj;
			try {
				jobj = new JSONObject(value);
			} catch (JSONException e1) {
				e1.printStackTrace();
				return;
			}
			boolean enabled = false;
			boolean userConfigurable = false;
			try {
				enabled = jobj.getBoolean(Touch3Config.KEY_SWITCH);
			} catch (JSONException e1) {
				enabled = false;
			}
			try {
				userConfigurable = jobj.getBoolean(Touch3Config.KEY_USER_CONFIGABLE);
			} catch (JSONException e1) {
				userConfigurable = false;
			}
			updateTouch3SwitchPreference(enabled || userConfigurable ? true : false);
			try {
				value = jobj.getString(Touch3Config.KEY_UP);
			} catch (JSONException e1) {
				value = "keycode:" + String.valueOf(Keycode.NONE);
			}
			updateTouch3Preference(mTouch3UpKey, value, false);
			try {
				value = jobj.getString(Touch3Config.KEY_DOWN);
			} catch (JSONException e1) {
				value = "keycode:" + String.valueOf(Keycode.NONE);
			}
			updateTouch3Preference(mTouch3DownKey, value, false);
			try {
				value = jobj.getString(Touch3Config.KEY_LEFT);
			} catch (JSONException e1) {
				value = "keycode:" + String.valueOf(Keycode.NONE);
			}
			updateTouch3Preference(mTouch3LeftKey, value, false);
			try {
				value = jobj.getString(Touch3Config.KEY_RIGHT);
			} catch (JSONException e1) {
				value = "keycode:" + String.valueOf(Keycode.NONE);
			}
			updateTouch3Preference(mTouch3RightKey, value, false);
		}
	}
	
	private void updateTouch3SwitchPreference(boolean value){
		if (value) {
			mTouch3Switch.setChecked(true);
			mTouch3Switch.setSummary(getResources().getString(R.string.enable));
		} else {
			mTouch3Switch.setChecked(false);
			mTouch3Switch.setSummary(getResources().getString(R.string.disable));
		}
	}
	private void updateTouch3Preference(ListPreference preference,
			String value, boolean save) {
		preference.setValue((String) value);
		preference.setSummary(preference.getEntry());
		if (save) {
			mTouch3Info.saveConfigJSON(true, getActivity(), mTouch3Switch.isChecked(), 
					mTouch3UpKey.getValue(), mTouch3DownKey.getValue(),
					mTouch3LeftKey.getValue(), mTouch3RightKey.getValue(),
					mTouch3UpKey.getEntry(), mTouch3DownKey.getEntry(),
					mTouch3LeftKey.getEntry(), mTouch3RightKey.getEntry());
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mActivity = activity;
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		String key = preference.getKey();

		if (KEY_TOUCH3_SWITCH.equals(key)) {
			updateTouch3SwitchPreference((Boolean) newValue);
			mTouch3Info.saveConfigJSON(true, getActivity(), mTouch3Switch.isChecked(), 
					mTouch3UpKey.getValue(), mTouch3DownKey.getValue(),
					mTouch3LeftKey.getValue(), mTouch3RightKey.getValue(),
					mTouch3UpKey.getEntry().toString(), mTouch3DownKey.getEntry().toString(),
					mTouch3LeftKey.getEntry().toString(), mTouch3RightKey.getEntry().toString());
		} else if (KEY_TOUCH3_UPKEY.equals(key)) {
			updateTouch3Preference(mTouch3UpKey, (String) newValue, true);
		} else if (KEY_TOUCH3_DOWNKEY.equals(key)) {
			updateTouch3Preference(mTouch3DownKey, (String) newValue, true);
		} else if (KEY_TOUCH3_LEFTKEY.equals(key)) {
			updateTouch3Preference(mTouch3LeftKey, (String) newValue, true);
		} else if (KEY_TOUCH3_RIGHTKEY.equals(key)) {
			updateTouch3Preference(mTouch3RightKey, (String) newValue, true);
		}

		return false;
	}

	public boolean onPreferenceClick(Preference arg0) {
		if (arg0.getKey().equals(KEY_TOUCH3_SWITCH)) {
		} else if (arg0.getKey().equals(KEY_TOUCH3_UPKEY)) {
		} else if (arg0.getKey().equals(KEY_TOUCH3_DOWNKEY)) {
		} else if (arg0.getKey().equals(KEY_TOUCH3_LEFTKEY)) {
		} else if (arg0.getKey().equals(KEY_TOUCH3_RIGHTKEY)) {
		}
		return false;
	}
}
