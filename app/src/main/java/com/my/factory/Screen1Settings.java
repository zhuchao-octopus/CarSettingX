/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.my.factory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.log.LogFile;
import com.octopus.android.carsettingx.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

/**
 * This activity plays a video from a specified URI.
 */
public class Screen1Settings extends PreferenceActivity implements
		Preference.OnPreferenceChangeListener, OnPreferenceClickListener,
		OnClickListener {

	private static final String TAG = "CanboxSettings";

	private static final String KEY_CANBOX = "canbox_setting";
	private static final String KEY_CANBOX_KEY = "canbox_key_mode";

	private ListPreference mCanboxSettingPreference;
	private ListPreference mCanboxKeyPreference;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.canbox_setting);
		addPreferencesFromResource(R.xml.canbox_settings);

		mCanboxSettingPreference = (ListPreference) findPreference(KEY_CANBOX);
		String[] canbox = { MachineConfig.VALUE_CANBOX_NONE,
				MachineConfig.VALUE_CANBOX_FORD_SIMPLE,
				MachineConfig.VALUE_CANBOX_TOYOTA,
				MachineConfig.VALUE_CANBOX_MAZDA,
				MachineConfig.VALUE_CANBOX_BESTURN_X80,
				MachineConfig.VALUE_CANBOX_TEANA_2013,
				MachineConfig.VALUE_CANBOX_OPEL, MachineConfig.VALUE_CANBOX_VW,
				MachineConfig.VALUE_CANBOX_TOYOTA_LOW,
				MachineConfig.VALUE_CANBOX_HY,
				MachineConfig.VALUE_CANBOX_PSA_BAGOO,
				MachineConfig.VALUE_CANBOX_GM_SIMPLE };
		mCanboxSettingPreference.setEntryValues(canbox);
		mCanboxSettingPreference.setOnPreferenceChangeListener(this);

		mCanboxKeyPreference = (ListPreference) findPreference(KEY_CANBOX_KEY);
		mCanboxKeyPreference.setOnPreferenceChangeListener(this);

		findViewById(R.id.btn_cancel).setOnClickListener(this);
		findViewById(R.id.btn_ok).setOnClickListener(this);

	}

	@Override
	public void onResume() {
		super.onResume();
		mCanboxValue = getCanboxSetting();
		updateCanboxSetting(getCanboxType());
		updateCanboxKey();
	}

	@Override
	public void onPause() {
		finish();
		super.onPause();
	}
	
	private void updateCanboxKeyValue(String which) {

		mCanboxKeyPreference.setValue(which);

		mCanboxKeyPreference.setSummary(mCanboxKeyPreference.getEntry());
	}

	private String getCanboxType(){
		if(mCanboxValue!=null){
			String ss[]= mCanboxValue.split(",");			
			return ss[0];
		}		
		return null;
	}
	
	private String getKeyType(String s){
		String ss[]= s.split(",");
		if (ss.length>1){
			return ss[1];
		}
		return null;
	}
	private void updateCanboxKey() {
		String s = mCanboxValue;

		boolean show = false;
		if (s!=null && s.startsWith(MachineConfig.VALUE_CANBOX_GM_SIMPLE)) {
			show = true;

			String[] entry = { "normal", "envision_low", "GL8" };
			String[] value = { "0", "1", "2" };
			mCanboxKeyPreference.setEntries(entry);
			mCanboxKeyPreference.setEntryValues(value);			
			
			s = getKeyType(s);
			if (s == null) {
				s = value[0];
			}
			updateCanboxKeyValue(s);

		}

		if (show) {
			if (findPreference(KEY_CANBOX_KEY) == null) {
				getPreferenceScreen().addPreference(mCanboxKeyPreference);
				// updateCanboxKeyValue(null);
			}
		} else {
			if (findPreference(KEY_CANBOX_KEY) != null) {
				getPreferenceScreen().removePreference(mCanboxKeyPreference);
			}
		}
	}

	private String mCanboxValue;

	private void setCanboxKeySetting(String which) {
		if (mCanboxValue != null) {
			String ss[] = mCanboxValue.split(",");
			if (ss.length > 1) {
				ss[1] = which;
				mCanboxValue = ss[0];
				for (int i = 1; i < ss.length; ++i) {
					mCanboxValue += "," + ss[i];
				}
			} else {
				mCanboxValue += "," + which;
			}
		}
	}


	private String getCanboxSetting() {
		return MachineConfig.getProperty(MachineConfig.KEY_CAN_BOX);
	}

	private void setCanboxSetting(String which) {
		mCanboxValue = which;
		updateCanboxKey();
	}

	private void updateCanboxSetting(String which) {
		if ((null == which) || (which.isEmpty())) {
			mCanboxSettingPreference.setValue(MachineConfig.VALUE_CANBOX_NONE);
		} else {
			mCanboxSettingPreference.setValue(which);
		}
		mCanboxSettingPreference
				.setSummary(mCanboxSettingPreference.getEntry());
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		String key = preference.getKey();

		if (KEY_CANBOX.equals(key)) {
			updateCanboxSetting((String) newValue);
			setCanboxSetting((String) newValue);
			
		} else if (KEY_CANBOX_KEY.equals(key)) {
			updateCanboxKeyValue((String) newValue);
			setCanboxKeySetting((String) newValue);
		}
		return false;
	}

	public boolean onPreferenceClick(Preference arg0) {
		// if (arg0.getKey().equals(KEY_RESET_SYSTEM)) {
		//
		// }

		return false;
	}
	
	private void updateMachineConfig() {
		if (mCanboxValue != null && !mCanboxValue.equals(getCanboxSetting())) {
			MachineConfig.setProperty(MachineConfig.KEY_CAN_BOX,
					mCanboxValue);

			Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
			it.putExtra(MyCmd.EXTRA_COMMON_CMD, MachineConfig.KEY_CAN_BOX);
			sendBroadcast(it);
		}
	}

	@Override
	public void onClick(View arg0) {
        int id = arg0.getId();
        if (id == R.id.btn_ok) {
            updateMachineConfig();
            finish();
        } else if (id == R.id.btn_cancel) {
            finish();
        }
	}
}
