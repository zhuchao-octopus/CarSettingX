package com.my.factory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.preference.SwitchPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import com.common.util.AppConfig;
import com.common.util.BroadcastUtil;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.common.util.MyCmd.Keycode;
import com.common.view.MyPreferenceEdit;
import com.common.view.MyPreferenceEdit.IButtonCallBack;
import com.octopus.android.carsettingx.R;

import android.hardware.display.DisplayManager;

public class FactorySettingsFragment extends PreferenceFragment implements
		Preference.OnPreferenceChangeListener, OnPreferenceClickListener {
	private static final String TAG = "FactorySettingsFragment";

	private static final String KEY_RADIO_REGION = "radio_region_setting";
	// private static final String KEY_ILLUM = "illum_setting";
	private static final String KEY_LOGO = "logo_setting";
	private static final String KEY_CANBOX = "canbox_setting";
	private static final String KEY_APP_HIDE = "app_hide_setting";
	private static final String KEY_SCREEN_SIZE = "screen_size_setting";
	private static final String KEY_BT_MODE = "bt_mode";
	private static final String KEY_TV_MODE = "tv_mode";
	private static final String KEY_LED_TYPE = "led_type";
	private static final String KEY_TPMS = "tpms";
	private static final String KEY_FTJ = "ftj";
	private static final String KEY_CAMERA_MODE = "camera_mode";
	private static final String KEY_SHOW_SCREEN1 = "show_screen1_view";
	private static final String KEY_BACKUP = "backup_settings";
	private static final String KEY_RESTORE = "restore_settings";
	private static final String KEY_RDS = "rds_switch";
	private static final String KEY_RADIO_ANT_POWER = "radio_ant_power";
	private static final String KEY_NO_REVERSE = "no_reverse";
	private static final String KEY_RUDDER = "rudder";
	private static final String KEY_ILLUM_ACC_NOD = "pannel_led_control";
	private static final String KEY_VCOM = "vcom";
	private static final String KEY_PANEL_KEY_DEFCFG = "panel_key_defconfig";
	private static final String KEY_SWC_KEY_DEFCFG = "swc_key_defconfig";
	private static final String KEY_AMP_VOLUME = "amp_volume";
	private static final String KEY_BT_MIC_GAIN = "bt_mic_gain";
	private static final String KEY_AUIDO_CHANNEL_GAIN = "audio_channel_gain";
	private static final String KEY_PANEL_KEY_STUDY = "panel_key_study";
	private static final String KEY_TOUCH_KEY_STUDY = "touch_key_study";
	private static final String KEY_CAN_KEY_STUDY = "can_key_study";
	private static final String KEY_TOUCH_CALIBRATION = "touch_calibration";
	private static final String KEY_MODEL = "model";
	private static final String KEY_MCU_VERSION_PREFIX = "mcu_version_prefix";

	private static final String KEY_SUPER_SETTINGS_MENU = "super_settings_menu";
	private static final String KEY_SWITCH_TO_FRONT_CAMER = "switch_to_front_camera";
	private static final String KEY_ADD_LANGUAGE = "add_a_language";
//	private static final String KEY_TOUCH3_SETTINGS = "touch3_settings";
//	private static final String KEY_TOUCH3_SWITCH = "touch3_settings_switch";
//	private static final String KEY_TOUCH3_UPKEY = "touch3_settings_upkey";
//	private static final String KEY_TOUCH3_DOWNKEY = "touch3_settings_downkey";
//	private static final String KEY_TOUCH3_LEFTKEY = "touch3_settings_leftkey";
//	private static final String KEY_TOUCH3_RIGHTKEY = "touch3_settings_rightkey";

	private static final String MCU_RADIO_REGION_NODE = "/sys/class/ak/source/radioregion";
	private static final String MCU_BRAKE_DET_NODE = "/sys/class/ak/source/reaksw";
	private static final String MCU_ILLUM_DET_NODE = "/sys/class/ak/source/illumintr";
	private static final String MCU_LED_COLOR_NODE = "/sys/class/ak/source/led_color";
	private static final String MCU_BEEP_NODE = "/sys/class/ak/source/beep";
	private static final String MCU_NAVI_MIX_NODE = "/sys/class/ak/source/navi_mix";
	private static final String MCU_ILLUM_ACC_NODE = "/sys/class/ak/source/accillumin";
	private static final String MCU_PANEL_KEY_DEFCFG_NODE = "/sys/class/ak/source/panel_key_defcfg";
	private static final String MCU_AMP_VOLUME_NODE = "/sys/class/ak/source/audio_output_gain";
	private static final String MCU_VERSION_NODE = "/sys/class/ak/version/mcu";

	private SwitchPreference mRdsCheckbox;
	private SwitchPreference mRadioAntPowerCheckbox;
	private SwitchPreference mNoReverseCheckbox;

	private SwitchPreference mRudder;
	private Activity mActivity;
	private FragmentManager mFragmentManager;
	private LogoFragment mLogoFragment = new LogoFragment();
	// private CanboxFragment mCanFragment = new CanboxFragment();

	private ListPreference mRadioRegionPreference;
	private ListPreference mAccIllumPreference;
	private ListPreference mPanelKeyDefConfigPreference;
	private ListPreference mSwcKeyDefConfigPreference;
	private ListPreference mAmpVolumePreference;
	private ListPreference mBTMicPreference;
	private Preference mAudioChannelGainPreference;
	// private ListPreference mIllumSettingPreference;
	private ListPreference mCanboxSettingPreference;
	private MultiSelectListPreference mAppHidePreference;

	private ListPreference mScrennSizePreference;
	private ListPreference mSwitchToFrontCamera;
	private ListPreference mBTMode;
	private ListPreference mTVMode;
	private ListPreference mLEDType;
	private ListPreference mTpms;
	private ListPreference mFtj;
	private ListPreference mCameraMode;
	private Preference mScreen1Preference;
	private MyPreferenceEdit mVCOM;
	
	private Preference mAddLanguage;
	
	private EditTextPreference mModel;
	private EditTextPreference mMcuVersionPrefix;

	private Preference mSuperSettingsPreferenceMenu;
//	private Preference mTouch3IdentifyPreference;
//	private SwitchPreference mTouch3Switch;
//	private ListPreference mTouch3UpKey;
//	private ListPreference mTouch3DownKey;
//	private ListPreference mTouch3LeftKey;
//	private ListPreference mTouch3RightKey;
//	
//	private Touch3Config mTouch3Info = new Touch3Config();

	//category
	private HashMap<String, Integer> mPerferenceMap = new HashMap();
	private void categorizePreferenceScreen() {
		if (mPerferenceMap.isEmpty()) {
			mPerferenceMap.put(KEY_RADIO_REGION, FactorySettings.TAB_TYPE_RADIO);
			mPerferenceMap.put(KEY_RDS, FactorySettings.TAB_TYPE_RADIO);
			mPerferenceMap.put(KEY_RADIO_ANT_POWER, FactorySettings.TAB_TYPE_RADIO);
			mPerferenceMap.put(KEY_NO_REVERSE, FactorySettings.TAB_TYPE_FUNC);
			mPerferenceMap.put(KEY_RUDDER, FactorySettings.TAB_TYPE_FUNC);
			mPerferenceMap.put(KEY_ILLUM_ACC_NOD, FactorySettings.TAB_TYPE_FUNC);
			mPerferenceMap.put(KEY_PANEL_KEY_DEFCFG, FactorySettings.TAB_TYPE_KEY);
			mPerferenceMap.put(KEY_SWC_KEY_DEFCFG, FactorySettings.TAB_TYPE_KEY);
			mPerferenceMap.put(KEY_PANEL_KEY_STUDY, FactorySettings.TAB_TYPE_KEY);
			mPerferenceMap.put(KEY_TOUCH_KEY_STUDY, FactorySettings.TAB_TYPE_KEY);
			mPerferenceMap.put(KEY_CAN_KEY_STUDY, FactorySettings.TAB_TYPE_KEY);
			if (GeneralSettings.mSystemUI.equals(MachineConfig.VALUE_SYSTEM_UI20_RM10_1)
				|| GeneralSettings.mSystemUI.equals(MachineConfig.VALUE_SYSTEM_UI21_RM10_2)
				|| GeneralSettings.mSystemUI.equals(MachineConfig.VALUE_SYSTEM_UI21_RM12)) {
				mPerferenceMap.put(KEY_LOGO, FactorySettings.TAB_TYPE_FUNC);
			} else {
				mPerferenceMap.put(KEY_LOGO, FactorySettings.TAB_TYPE_NONE);	//deal with individual
			}
			mPerferenceMap.put(KEY_CANBOX, FactorySettings.TAB_TYPE_CANBUS);
			mPerferenceMap.put(KEY_CAMERA_MODE, FactorySettings.TAB_TYPE_FUNC);
			mPerferenceMap.put(KEY_BT_MODE, FactorySettings.TAB_TYPE_FUNC);
			mPerferenceMap.put(KEY_TV_MODE, FactorySettings.TAB_TYPE_FUNC);
			mPerferenceMap.put(KEY_LED_TYPE, FactorySettings.TAB_TYPE_FUNC);
			mPerferenceMap.put(KEY_TPMS, FactorySettings.TAB_TYPE_FUNC);
			mPerferenceMap.put(KEY_FTJ, FactorySettings.TAB_TYPE_FUNC);
			mPerferenceMap.put(KEY_VCOM, FactorySettings.TAB_TYPE_NONE);	//deal with individual
			mPerferenceMap.put(KEY_APP_HIDE, FactorySettings.TAB_TYPE_APP);
			mPerferenceMap.put(KEY_MODEL, FactorySettings.TAB_TYPE_NONE/*FactorySettings.TAB_TYPE_SUPER*/);
			mPerferenceMap.put(KEY_MCU_VERSION_PREFIX, FactorySettings.TAB_TYPE_NONE/*FactorySettings.TAB_TYPE_SUPER*/);
			mPerferenceMap.put(KEY_SCREEN_SIZE, FactorySettings.TAB_TYPE_OTHER);
			mPerferenceMap.put(KEY_SHOW_SCREEN1, FactorySettings.TAB_TYPE_OTHER);
			mPerferenceMap.put(KEY_TOUCH_CALIBRATION, FactorySettings.TAB_TYPE_KEY);
			mPerferenceMap.put(KEY_AMP_VOLUME, FactorySettings.TAB_TYPE_SOUND);
			mPerferenceMap.put(KEY_BT_MIC_GAIN, FactorySettings.TAB_TYPE_SOUND);
			mPerferenceMap.put(KEY_AUIDO_CHANNEL_GAIN, FactorySettings.TAB_TYPE_SOUND);
			mPerferenceMap.put(KEY_BACKUP, FactorySettings.TAB_TYPE_NONE);
			mPerferenceMap.put(KEY_RESTORE, FactorySettings.TAB_TYPE_NONE);
			mPerferenceMap.put(KEY_SUPER_SETTINGS_MENU, FactorySettings.TAB_TYPE_NONE/*FactorySettings.TAB_TYPE_OTHER*/);
			mPerferenceMap.put(KEY_SWITCH_TO_FRONT_CAMER, FactorySettings.TAB_TYPE_OTHER/*FactorySettings.TAB_TYPE_OTHER*/);
			if (isHideAddLanguage())
				mPerferenceMap.put(KEY_ADD_LANGUAGE, FactorySettings.TAB_TYPE_OTHER);
			else
				mPerferenceMap.put(KEY_ADD_LANGUAGE, FactorySettings.TAB_TYPE_NONE);
//			if (Util.isRKSystem())
//				mPerferenceMap.put(KEY_TOUCH3_SETTINGS, FactorySettings.TAB_TYPE_NONE/*FactorySettings.TAB_TYPE_SUPER*/);
//			else
//				mPerferenceMap.put(KEY_TOUCH3_SETTINGS, FactorySettings.TAB_TYPE_NONE);
		}
	}

	private void removeNonePreference() {
		try {
			Bundle b = getArguments();
			if (b != null) {
				int preference_type = b.getInt("preference_type");
//				Log.d(TAG, ">>>>>>>>>>>>>preference_type = " + preference_type + " ," + mPerferenceMap.size());
				for (HashMap.Entry<String, Integer> entry : mPerferenceMap.entrySet()) {
					String key = entry.getKey();
					int value = entry.getValue();
//					Log.d(TAG, "key=" + key + " ,value=" + value);
					if (value != preference_type) {
						Preference p = findPreference(key);
						if (p != null) {
							getPreferenceScreen().removePreference(p);
//							Log.d(TAG, "remove key=" + key);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void onSuperSettingsClicked() {
		((FactorySettings) getActivity()).onSuperSettingsClicked(FactorySettings.TAB_TYPE_SUPER);
	}

	private Handler mHandler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		// Do dummy get.
		// Some setting need to communicate with MCU.
		// Make sure they are valid at next get.
		getRadioRegion();
		getBrakeSetting();
		// getIllumSetting();
		getLedSetting();
		getBeepSetting();
		getNaviMix();

//		mActivity = getActivity();
		addPreferencesFromResource(R.xml.factory_settings);

		mRadioRegionPreference = (ListPreference) findPreference(KEY_RADIO_REGION);
		mRadioRegionPreference.setOnPreferenceChangeListener(this);

		mAccIllumPreference = (ListPreference) findPreference(KEY_ILLUM_ACC_NOD);
		mAccIllumPreference.setOnPreferenceChangeListener(this);

		mPanelKeyDefConfigPreference = (ListPreference) findPreference(KEY_PANEL_KEY_DEFCFG);
		mPanelKeyDefConfigPreference.setOnPreferenceChangeListener(this);

		mSwcKeyDefConfigPreference = (ListPreference) findPreference(KEY_SWC_KEY_DEFCFG);
		mSwcKeyDefConfigPreference.setOnPreferenceChangeListener(this);

		mAmpVolumePreference = (ListPreference) findPreference(KEY_AMP_VOLUME);
		mAmpVolumePreference.setOnPreferenceChangeListener(this);
		mBTMicPreference = (ListPreference) findPreference(KEY_BT_MIC_GAIN);
		mBTMicPreference.setOnPreferenceChangeListener(this);
		mAudioChannelGainPreference = (Preference) findPreference(KEY_AUIDO_CHANNEL_GAIN);		
		mAudioChannelGainPreference.setOnPreferenceClickListener(this);

		mRdsCheckbox = (SwitchPreference) findPreference(KEY_RDS);
		mRdsCheckbox.setOnPreferenceChangeListener(this);
		mRadioAntPowerCheckbox = (SwitchPreference) findPreference(KEY_RADIO_ANT_POWER);
		mRadioAntPowerCheckbox.setOnPreferenceChangeListener(this);
		
		mNoReverseCheckbox = (SwitchPreference) findPreference(KEY_NO_REVERSE);
		mNoReverseCheckbox.setOnPreferenceChangeListener(this);
		mRudder = (SwitchPreference) findPreference(KEY_RUDDER);
		mRudder.setOnPreferenceChangeListener(this);
		// mIllumSettingPreference = (ListPreference) findPreference(KEY_ILLUM);
		// mIllumSettingPreference.setOnPreferenceChangeListener(this);

		// mCanboxSettingPreference = (ListPreference)
		// findPreference(KEY_CANBOX);
		// String[] canbox = { MachineConfig.VALUE_CANBOX_NONE,
		// MachineConfig.VALUE_CANBOX_FORD_SIMPLE,
		// MachineConfig.VALUE_CANBOX_TOYOTA,
		// MachineConfig.VALUE_CANBOX_MAZDA,
		// MachineConfig.VALUE_CANBOX_BESTURN_X80,
		// MachineConfig.VALUE_CANBOX_TEANA_2013,
		// MachineConfig.VALUE_CANBOX_OPEL, MachineConfig.VALUE_CANBOX_VW,
		// MachineConfig.VALUE_CANBOX_TOYOTA_LOW,
		// MachineConfig.VALUE_CANBOX_HY,
		// MachineConfig.VALUE_CANBOX_PSA_BAGOO,
		// MachineConfig.VALUE_CANBOX_GM_SIMPLE };
		// mCanboxSettingPreference.setEntryValues(canbox);
		// mCanboxSettingPreference.setOnPreferenceChangeListener(this);

		// findPreference(KEY_CANBOX).setOnPreferenceClickListener(this);

		mAppHidePreference = (MultiSelectListPreference) findPreference(KEY_APP_HIDE);
		mAppHidePreference.setOnPreferenceChangeListener(this);

		mSwitchToFrontCamera = (ListPreference) findPreference(KEY_SWITCH_TO_FRONT_CAMER);
		if (mSwitchToFrontCamera != null) {		
			mSwitchToFrontCamera.setOnPreferenceChangeListener(this);			
		}

		mAddLanguage = (Preference) findPreference(KEY_ADD_LANGUAGE);
		if (mAddLanguage != null) {		
			mAddLanguage.setOnPreferenceClickListener(this);			
		}
		
		mScrennSizePreference = (ListPreference) findPreference(KEY_SCREEN_SIZE);
		if (mScrennSizePreference != null) {
			if ("1".equals(MachineConfig
					.getPropertyOnce(MachineConfig.KEY_OTG_TEST))) {

				mScrennSizePreference.setOnPreferenceChangeListener(this);
			} else {
				getPreferenceScreen().removePreference(mScrennSizePreference);
				mScrennSizePreference = null;
			}
		}
		mBTMode = (ListPreference) findPreference(KEY_BT_MODE);
		if (mBTMode != null) {
			mBTMode.setOnPreferenceChangeListener(this);
		}
		mTVMode = (ListPreference) findPreference(KEY_TV_MODE);
		if (mTVMode != null) {
			mTVMode.setOnPreferenceChangeListener(this);
		}
		mLEDType = (ListPreference) findPreference(KEY_LED_TYPE);
		if (mLEDType != null) {
			mLEDType.setOnPreferenceChangeListener(this);
		}
		mTpms = (ListPreference) findPreference(KEY_TPMS);
		if (mTpms != null) {
			mTpms.setOnPreferenceChangeListener(this);
		}
		mFtj = (ListPreference) findPreference(KEY_FTJ);
		if (mFtj != null) {
			mFtj.setOnPreferenceChangeListener(this);
		}
		mCameraMode = (ListPreference) findPreference(KEY_CAMERA_MODE);
		if (mCameraMode != null) {
			if (Util.isNexellSystem()){
				mCameraMode.setOnPreferenceChangeListener(this);
			} else {
				getPreferenceScreen().removePreference(mCameraMode);
			}
		}
		mScreen1Preference = findPreference(KEY_SHOW_SCREEN1);

		if (mScreen1Preference != null) {
			DisplayManager displayManager = (DisplayManager) mActivity
					.getSystemService(Context.DISPLAY_SERVICE);
			Display[] display = displayManager.getDisplays();
			if (display.length <= 1) {

				getPreferenceScreen().removePreference(mScreen1Preference);
				mScreen1Preference = null;
			} else {

				mScreen1Preference.setOnPreferenceChangeListener(this);
			}

		}

		findPreference(KEY_BACKUP).setOnPreferenceClickListener(this);
		findPreference(KEY_RESTORE).setOnPreferenceClickListener(this);
		findPreference(KEY_LOGO).setOnPreferenceClickListener(this);

		mFragmentManager = getFragmentManager();

		if (FactorySettings.mIsTest) {
			// replaceFragment(R.id.id_genernal_setting_fragment, mCanFragment,
			// true);
			replaceFragment(R.id.id_genernal_setting_fragment, mLogoFragment,
					true);
		}
		
		mVCOM = (MyPreferenceEdit) findPreference(KEY_VCOM);
		if(Util.isRKSystem()){
			mVCOM.setCallback(mButtonCallBack);
		} else {
			getPreferenceScreen().removePreference(mVCOM);
		}
		
		mModel = (EditTextPreference) findPreference(KEY_MODEL);
		getModel();
		if (mModel != null) {
			mModel.setOnPreferenceChangeListener(this);
		}
		
		mMcuVersionPrefix = (EditTextPreference) findPreference(KEY_MCU_VERSION_PREFIX);
		getMCUVersion();
		if (mMcuVersionPrefix != null) {
			mMcuVersionPrefix.setOnPreferenceChangeListener(this);
		}
		
		mSuperSettingsPreferenceMenu = findPreference(KEY_SUPER_SETTINGS_MENU);
		if (mSuperSettingsPreferenceMenu != null) {
			mSuperSettingsPreferenceMenu.setOnPreferenceClickListener(this);
		}
		/*mTouch3IdentifyPreference = findPreference(KEY_TOUCH3_SETTINGS);
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
		}*/

		categorizePreferenceScreen();
		removeNonePreference();
	}

	private boolean isHideAddLanguage() {
		String value = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_HIDE_ADD_LANGUAGE);
		if (value != null && (value.equals("1") || value.equals("true")))
			return true;
		else
			return false;
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
	
	/*private void getTouch3ConfigValue(Context context) {
//		String value = SystemConfig.getProperty(context, MachineConfig.KEY_TOUCH3_IDENTIFY);
		String value = MachineConfig.getProperty(MachineConfig.KEY_TOUCH3_IDENTIFY);
		Log.d(TAG, "getTouch3ConfigValue: " + value);
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
	}*/

	private void getMcuVersionPrefix() {
		String mcuVersion = com.common.util.Util.getFileString(MCU_VERSION_NODE);
		if (mcuVersion == null) {
			try {
				Thread.sleep(300);
				mcuVersion = com.common.util.Util.getFileString(MCU_VERSION_NODE);
			} catch (Exception e) {
			}
		}
		if (mcuVersion != null && !mcuVersion.isEmpty()) {
			String [] str = mcuVersion.split("_");
			if (str != null && str.length > 1) {
				mMcuVersionPrefix.setText(str[0]);
				mMcuVersionPrefix.setSummary(str[0]);
			}
		}
	}

	private void replaceFragment(int layoutId, PreferenceFragment fragment,
			boolean isAddStack) {
		if (fragment != null) {
			FragmentTransaction transation = mFragmentManager
					.beginTransaction();
			transation.replace(layoutId, fragment);
			if (isAddStack) {
				transation.addToBackStack(null);
			}
			transation.commit();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterListener();
		Util.sudoExecNoCheck("sync");
	}

	@Override
	public void onResume() {
		super.onResume();

		ActionBar ab = mActivity.getActionBar();
		if (null != ab) {
			ab.setTitle(R.string.factory_settings_title);
		}

		mRds = !MachineConfig.VALUE_OFF.equals(MachineConfig
				.getProperty(MachineConfig.KEY_RDS));
		mRdsCheckbox.setChecked(mRds);
		
		int ant = MachineConfig
				.getPropertyInt(MachineConfig.KEY_RADIO_ANT_POWER);
		mRadioAntPowerCheckbox.setChecked(ant==1?true:false);
		boolean no_reverse = MachineConfig.VALUE_ON.equals(MachineConfig
				.getProperty(MachineConfig.KEY_NO_REVERSE));
		mNoReverseCheckbox.setChecked(no_reverse);

		boolean rudder = MachineConfig.VALUE_ON.equals(MachineConfig
				.getProperty(MachineConfig.KEY_RUDDER));
		mRudder.setChecked(rudder);

		updateRadioRegion(getRadioRegion());
		// updateIllumSetting(getIllumSetting());
		// updateCanboxSetting(getCanboxSetting());
		updateAppHideSetting(getAppHideSetting());

		updateScreen1Setting(getScreen1Setting());
		updateAccIllumin(getAccIllumin());
		updatePanelKeyDefConfig(MachineConfig.getProperty(MachineConfig.KEY_PANEL_KEY_DEF_CONFIG));
		updateScreenSize();
		updateBTMode();
		updateTVMode();
		updateCameraMode();
		updateLEDType();
		updateTpms();
		updateFtj();
		updateAmpVolume(getAmpVolume());
		updateBTMicGain(getBTMicGain());
		
		registerListener();
		queryVcomValue();
		updateSwitchToFrontCamera();
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mActivity = activity;
	}

	private boolean mRds;

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		String key = preference.getKey();

		if (KEY_RADIO_REGION.equals(key)) {
			int region = Integer.parseInt((String) newValue);
			updateRadioRegion(region);
			setRadioRegion(region);
			// } else if (KEY_ILLUM.equals(key)) {
			// int illum = Integer.parseInt((String) newValue);
			// updateIllumSetting(illum);
			// setIllumSetting(illum);
		}
		if (KEY_ILLUM_ACC_NOD.equals(key)) {
			int region = Integer.parseInt((String) newValue);

			updateAccIllumin(region);
			setAccIllumin(region);
		} else if (KEY_PANEL_KEY_DEFCFG.equals(key)) {
			updatePanelKeyDefConfig((String) newValue);
			MachineConfig.setProperty(MachineConfig.KEY_PANEL_KEY_DEF_CONFIG, (String) newValue);
			int v = Integer.parseInt((String) newValue);
			
			Intent it = new Intent(
					MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
			it.putExtra(
					MyCmd.EXTRA_COMMON_CMD,
					MachineConfig.KEY_PANEL_KEY_DEF_CONFIG);
			it.putExtra(
					MyCmd.EXTRA_COMMON_DATA,
					v);
			mActivity.sendBroadcast(it);
			
			
			v = (v & 0xff);
			Util.setFileValue(MCU_PANEL_KEY_DEFCFG_NODE, v);
			
		} else if (KEY_SWC_KEY_DEFCFG.equals(key)) {
			updateSwcKeyDefConfig((String) newValue);
			MachineConfig.setProperty(MachineConfig.KEY_SWC_KEY_DEF_CONFIG, (String) newValue);
			Util.setFileValue(MCU_PANEL_KEY_DEFCFG_NODE, Integer.parseInt((String) newValue));
		} else if (KEY_AMP_VOLUME.equals(key)) {
			int value = Integer.parseInt((String) newValue);
			updateAmpVolume(value);
			setAmpVolume(value);
		} else if (KEY_BT_MIC_GAIN.equals(key)) {
			updateBTMicGain((String) newValue);
			setBTMicGain((String) newValue);
			
		} else if (KEY_CANBOX.equals(key)) {
			updateCanboxSetting((String) newValue);
			setCanboxSetting((String) newValue);
		} else if (KEY_APP_HIDE.equals(key)) {
			updateAppHideSetting((HashSet<String>) newValue);
			setAppHideSetting((HashSet<String>) newValue);
		} else if (KEY_SCREEN_SIZE.equals(key)) {

			setScreenSize((String) newValue);

		} else if (KEY_SWITCH_TO_FRONT_CAMER.equals(key)) {

			setSwitchSize((String) newValue);
		} else if (KEY_BT_MODE.equals(key)) {

			setBTMode((String) newValue);

		}  else if (KEY_TV_MODE.equals(key)) {

			setTVMode((String) newValue);

		}  else if (KEY_LED_TYPE.equals(key)) {
			
			setLEDType((String) newValue);
		}  else if (KEY_TPMS.equals(key)) {
			
			setTpms((String) newValue);
		}  else if (KEY_FTJ.equals(key)) {
			
			setFtj((String) newValue);
			
		} else if (KEY_CAMERA_MODE.equals(key)) {

			setCameraMode((String) newValue);

		} else if (KEY_SHOW_SCREEN1.equals(key)) {
			updateScreen1Setting((HashSet<String>) newValue);
			setScreen1Setting((HashSet<String>) newValue);
		} else if (KEY_RDS.equals(key)) {// rds
			mRds = (Boolean) newValue;
			MachineConfig.setProperty(MachineConfig.KEY_RDS,
					mRds ? MachineConfig.VALUE_ON : MachineConfig.VALUE_OFF);

			Intent it = new Intent(
					MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
			it.putExtra(
					MyCmd.EXTRA_COMMON_CMD,
					MachineConfig.KEY_RDS);
			mActivity.sendBroadcast(it);
			
			if (mRdsCheckbox != null) {
				mRdsCheckbox.setChecked(mRds);
			}
		}  else if (KEY_RADIO_ANT_POWER.equals(key)) {		

			Intent it = new Intent(
					MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
			it.putExtra(
					MyCmd.EXTRA_COMMON_CMD,
					MachineConfig.KEY_RADIO_ANT_POWER);
			it.putExtra(
					MyCmd.EXTRA_COMMON_DATA,
					((Boolean) newValue )?1: 0);
			mActivity.sendBroadcast(it);
			

			MachineConfig.setProperty(MachineConfig.KEY_RADIO_ANT_POWER,
					((Boolean) newValue) ? MachineConfig.VALUE_ON
							: MachineConfig.VALUE_OFF);
			
			if (mRadioAntPowerCheckbox != null) {
				mRadioAntPowerCheckbox.setChecked((Boolean) newValue);
			}
		} else if (KEY_NO_REVERSE.equals(key)) {// rds
			final boolean no_reverse = (Boolean) newValue;

			AlertDialog ad = new AlertDialog.Builder(mActivity)
					.setTitle(R.string.reboot_warning)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

									MachineConfig.setProperty(
											MachineConfig.KEY_NO_REVERSE,
											no_reverse ? MachineConfig.VALUE_ON
													: MachineConfig.VALUE_OFF);

									BroadcastUtil.sendToCarService(
											getActivity(),
											MyCmd.Cmd.APP_REQUEST_SEND_KEY,
											MyCmd.Keycode.POWER);

									Util.doSleep(300);

									Util.sudoExec("sync");

									Util.doSleep(1000);
									Util.setFileValue("/sys/class/ak/source/factory", new byte[] { 0x55,
											(byte) 0xaa, 0x00 });
								}
							})
					.setNegativeButton(R.string.alert_dialog_cancel, null)
					.create();
			ad.show();

			// Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
			// it.putExtra(MyCmd.EXTRA_COMMON_CMD,
			// MachineConfig.KEY_NO_REVERSE);
			// it.putExtra(MyCmd.EXTRA_COMMON_DATA, no_reverse);
			// mActivity.sendBroadcast(it);
			//
			// if (mNoReverseCheckbox != null) {
			// mNoReverseCheckbox.setChecked(no_reverse);
			// }
		} else if (KEY_RUDDER.equals(key)) {

			MachineConfig.setProperty(MachineConfig.KEY_RUDDER,
					(Boolean) newValue ? MachineConfig.VALUE_ON
							: MachineConfig.VALUE_OFF);

			Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
			it.putExtra(MyCmd.EXTRA_COMMON_CMD, MachineConfig.KEY_RUDDER);
			it.putExtra(MyCmd.EXTRA_COMMON_DATA, (Boolean)newValue);
			mActivity.sendBroadcast(it);
			
			if (mRdsCheckbox != null) {
				mRudder.setChecked((Boolean) newValue );
			}
		} else if (KEY_MODEL.equals(key)) {
			MachineConfig.setProperty(MachineConfig.KEY_MODEL,(String)newValue);
			getModel();
		} else if (KEY_MCU_VERSION_PREFIX.equals(key)) {
			MachineConfig.setProperty(MachineConfig.KEY_MCU_PREFIX,(String)newValue);
			getMCUVersion();
		/*} else if (KEY_TOUCH3_SWITCH.equals(key)) {
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
			updateTouch3Preference(mTouch3RightKey, (String) newValue, true);*/
		}

		return false;
	}
	
	public static void doBackup(final Activity activity) {
		AlertDialog ad = new AlertDialog.Builder(activity)
				.setIcon(R.drawable.dialog_alert_icon)
				.setTitle(R.string.backup_title)
				.setMessage(R.string.backup_message)
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								SettingBackuper sb = new SettingBackuper(
										activity);
								sb.setRadioRegion(getRadioRegion());
								sb.setBrake(getBrakeSetting());
								sb.setIllum(getIllumSetting());
								sb.setLedColor(getLedSetting());
								sb.setBeep(getBeepSetting());
								sb.setNaviMix(getNaviMix());
								sb.setVcom(FactorySettingsFragment.mVcomValue);
								sb.setAmp(getAmpVolume());
								sb.setAccIllumin(getAccIllumin());
								if (sb.doBackup()) {
									Toast.makeText(activity,
											R.string.backup_success,
											Toast.LENGTH_SHORT).show();
								} else {
									Toast.makeText(activity,
											R.string.no_valid_ext_storage,
											Toast.LENGTH_SHORT).show();
								}
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel, null).create();
		;
		ad.show();
	}
	
	public static void doRestore(final Activity activity) {
		AlertDialog ad = new AlertDialog.Builder(activity)
				.setIcon(R.drawable.dialog_alert_icon)
				.setTitle(R.string.restore_title)
				.setMessage(R.string.restore_message)
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								SettingBackuper sb = new SettingBackuper(
										activity);
								if (sb.doRestore()) {
									Toast.makeText(activity,
											R.string.restore_success,
											Toast.LENGTH_SHORT).show();
									setRadioRegion(sb.getRadioRegion());
									setBrakeSetting(sb.getBrake());
									setIllumSetting(sb.getIllum());
									setLedSetting(sb.getLedColor());
									setBeepSetting(sb.getBeep());
									setNaviMix(sb.getNaviMix());
									BroadcastUtil.sendToCarService(activity, MyCmd.Cmd.SET_VCOM, sb.getVcom());
									setAmpVolume(sb.getAmp());
									setAccIllumin(sb.getAccIllumin());
									AlertDialog adReboot = new AlertDialog.Builder(
											activity)
											.setIcon(
													R.drawable.dialog_alert_icon)
											.setTitle(R.string.restore_title)
											.setMessage(R.string.reboot_warning)
											.setPositiveButton(
													R.string.alert_dialog_i_know,
													new DialogInterface.OnClickListener() {
														public void onClick(
																DialogInterface dialog,
																int whichButton) {

														}
													}).create();
									adReboot.show();
								} else {
									Toast.makeText(activity,
											R.string.no_backuped_setting_found,
											Toast.LENGTH_SHORT).show();
								}
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel, null).create();
		ad.show();
	}
	
	public boolean onPreferenceClick(Preference arg0) {
		if (arg0.getKey().equals(KEY_BACKUP)) {
			doBackup(mActivity);
			/*AlertDialog ad = new AlertDialog.Builder(mActivity)
					.setIcon(R.drawable.dialog_alert_icon)
					.setTitle(R.string.backup_title)
					.setMessage(R.string.backup_message)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									SettingBackuper sb = new SettingBackuper(
											mActivity);
									sb.setRadioRegion(getRadioRegion());
									sb.setBrake(getBrakeSetting());
									sb.setIllum(getIllumSetting());
									sb.setLedColor(getLedSetting());
									sb.setBeep(getBeepSetting());
									sb.setNaviMix(getNaviMix());
									if (sb.doBackup()) {
										Toast.makeText(getActivity(),
												R.string.backup_success,
												Toast.LENGTH_SHORT).show();
									} else {
										Toast.makeText(getActivity(),
												R.string.no_valid_ext_storage,
												Toast.LENGTH_SHORT).show();
									}
								}
							})
					.setNegativeButton(R.string.alert_dialog_cancel, null)
					.create();
			;
			ad.show();*/
		} else if (arg0.getKey().equals(KEY_RESTORE)) {
			doRestore(mActivity);
			/*AlertDialog ad = new AlertDialog.Builder(mActivity)
					.setIcon(R.drawable.dialog_alert_icon)
					.setTitle(R.string.restore_title)
					.setMessage(R.string.restore_message)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									SettingBackuper sb = new SettingBackuper(
											mActivity);
									if (sb.doRestore()) {
										Toast.makeText(getActivity(),
												R.string.restore_success,
												Toast.LENGTH_SHORT).show();
										setRadioRegion(sb.getRadioRegion());
										setBrakeSetting(sb.getBrake());
										setIllumSetting(sb.getIllum());
										setLedSetting(sb.getLedColor());
										setBeepSetting(sb.getBeep());
										setNaviMix(sb.getNaviMix());
										AlertDialog adReboot = new AlertDialog.Builder(
												mActivity)
												.setIcon(
														R.drawable.dialog_alert_icon)
												.setTitle(
														R.string.restore_title)
												.setMessage(
														R.string.reboot_warning)
												.setPositiveButton(
														R.string.alert_dialog_i_know,
														new DialogInterface.OnClickListener() {
															public void onClick(
																	DialogInterface dialog,
																	int whichButton) {

															}
														}).create();
										adReboot.show();
									} else {
										Toast.makeText(
												getActivity(),
												R.string.no_backuped_setting_found,
												Toast.LENGTH_SHORT).show();
									}
								}
							})
					.setNegativeButton(R.string.alert_dialog_cancel, null)
					.create();
			ad.show();*/
		} else if (arg0.getKey().equals(KEY_LOGO)) {
			try {
				Intent intent = new Intent();
				intent.setClass(getActivity(), ActivityLogo.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
//			replaceFragment(R.id.id_genernal_setting_fragment, mLogoFragment,true);
			// } else if (arg0.getKey().equals(KEY_CANBOX)) {
			// replaceFragment(R.id.id_genernal_setting_fragment, mCanFragment,
			// true);
		} else if (arg0.getKey().equals(KEY_SUPER_SETTINGS_MENU)) {
			onSuperSettingsClicked();
		} else if (arg0.getKey().equals(KEY_AUIDO_CHANNEL_GAIN)) {
			((FactorySettings)getActivity()).onChangeFragment(0);
		} else if (arg0.getKey().equals(KEY_ADD_LANGUAGE)) {
			try {
				Intent intent = new Intent("android.settings.LOCALE_SETTINGS");
				intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("factory_add_language", true);
				startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return false;

	}
	
	public String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll(",");
        }
        return dest;
    }

	private static int getRadioRegion() {
		return Util.getFileValue(MCU_RADIO_REGION_NODE);
	}

	private static void setRadioRegion(int region) {
		Util.setFileValue(MCU_RADIO_REGION_NODE, region);
	}

	private void updateRadioRegion(int region) {
		try{
			mRadioRegionPreference.setValue(String.valueOf(region));
			mRadioRegionPreference.setSummary(replaceBlank(mRadioRegionPreference.getEntry().toString()));
		}catch(Exception e){
			
		}
	}

	private static int getAccIllumin() {
		return Util.getFileValue(MCU_ILLUM_ACC_NODE);
	}

	private static void setAccIllumin(int region) {
		Util.setFileValue(MCU_ILLUM_ACC_NODE, region);
	}

	private void updateAccIllumin(int region) {
		mAccIllumPreference.setValue(String.valueOf(region));
		mAccIllumPreference.setSummary(mAccIllumPreference.getEntry());
	}

	private String getBTMicGain() {
		String s = MachineConfig.getPropertyOnce(MachineConfig.KEY_BT_MIC_GAIN);
	
		
		return s;
	}

	private void setBTMicGain(String value) {
		MachineConfig.setProperty(MachineConfig.KEY_BT_MIC_GAIN, value);
		
		Intent it = new Intent(MyCmd.BROADCAST_CMD_LAUNCHER_TO_BT);
		it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.BT_REQUEST_MIC_GAIN);
		it.putExtra(MyCmd.EXTRA_COMMON_DATA, value);

		mActivity.sendBroadcast(it);
	}

	private void updateBTMicGain(String value) {
		mBTMicPreference.setValue(value);
		mBTMicPreference.setSummary(mBTMicPreference.getEntry());
	}
	
	private static int getAmpVolume() {
		int val = Util.getFileValue(MCU_AMP_VOLUME_NODE);
		if (-1==val) {
			// try again
			val = Util.getFileValue(MCU_AMP_VOLUME_NODE);
		}
		return val;
	}
	
	private static void setAmpVolume(int value) {
		Util.setFileValue(MCU_AMP_VOLUME_NODE, value);
	}

	private void updateAmpVolume(int value) {
		mAmpVolumePreference.setValue(String.valueOf(value));
		mAmpVolumePreference.setSummary(mAmpVolumePreference.getEntry());
	}
	
	private void updatePanelKeyDefConfig(String cfg) {
		mPanelKeyDefConfigPreference.setValue(cfg);
		mPanelKeyDefConfigPreference.setSummary(mPanelKeyDefConfigPreference.getEntry());
	}

	private void updateSwcKeyDefConfig(String cfg) {
		mSwcKeyDefConfigPreference.setValue(cfg);
		mSwcKeyDefConfigPreference.setSummary(mSwcKeyDefConfigPreference.getEntry());
	}

	private void updateBTMode() {

		String s = MachineConfig.getPropertyOnce(MachineConfig.KEY_BT_TYPE);
		if (s == null) {
			s = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_BT_TYPE);
			if (s == null){
				s = "0";
			}
		}
		if (s.equals("0")) {
			if (getCurrenIVTBt() == 1) {
				s = "-1";
			}
		}
		mBTMode.setValue(s);
		mBTMode.setSummary(mBTMode.getEntry());
	}

	private void updateTVMode() {

		String s = MachineConfig.getPropertyOnce(MachineConfig.KEY_TV_TYPE);
		if (s == null) {
			s = "0";
		}
		mTVMode.setValue(s);
		mTVMode.setSummary(mTVMode.getEntry());
	}
	
	private void updateLEDType() {

		String s = MachineConfig.getPropertyOnce(MachineConfig.KEY_LED_TYPE);
		if (s == null) {
			s = "0";
		}
		mLEDType.setValue(s);
		mLEDType.setSummary(mLEDType.getEntry());
		if ("1".equals(s)){
			Util.setFileValue(MCU_LED_COLOR_NODE, 0xfefefe);
		}
	}
	private void updateTpms() {

		String s = MachineConfig.getPropertyOnce(MachineConfig.KEY_TPMS_TYPE);
		if (s == null) {
			s = "0";
		}
		mTpms.setValue(s);
		mTpms.setSummary(mTpms.getEntry());
	}
	private void updateFtj() {

		String s = MachineConfig.getPropertyOnce(MachineConfig.KEY_FTJ_TYPE);
		if (s == null) {
			s = "0";
		}
		mFtj.setValue(s);
		mFtj.setSummary(mFtj.getEntry());
	}
	private void setCurrenIVTBt(int type) {
		Util.sudoExec("rm:/oem/lib/blueletd");
		Util.checkAKDRunning();
		Util.sudoExec("rm:/oem/lib/libbluelet.so");
		Util.checkAKDRunning();
		Util.sudoExec("rm:/oem/lib/libbt_platform.so");
		Util.checkAKDRunning();
		
		String s;
		if (type == 1) {
			s = "i145";
		} else {
			s = "i140";
		}

		Util.sudoExec("ln:-s:./" + s + "/blueletd:/oem/lib/blueletd");
		Util.checkAKDRunning();
		Util.sudoExec("ln:-s:./" + s + "/libbluelet.so:/oem/lib/libbluelet.so");
		Util.checkAKDRunning();
		Util.sudoExec("ln:-s:./" + s + "/libbt_platform.so:/oem/lib/libbt_platform.so");
		Util.checkAKDRunning();
	}
	private int getCurrenIVTBt() {
		int ivt_type = 0;
		String str = null;
		try {
			Process psProcess = Runtime.getRuntime().exec("ls -l /oem/lib");

			psProcess.waitFor();

			InputStream inputStream = psProcess.getInputStream();
			InputStreamReader buInputStreamReader = new InputStreamReader(
					inputStream);
			BufferedReader bufferedReader = new BufferedReader(
					buInputStreamReader);

			for (int i = 0; i < 10; ++i) {
				str = bufferedReader.readLine();
				if (str == null) {
					break;
				}
				if (str.contains("blueletd -> ./i140/blueletd")) {
					ivt_type = 0;
					break;
				} else if (str.contains("blueletd -> ./i145/blueletd")) {
					ivt_type = 1;
					break;
				}
			}
			inputStream.close();
			buInputStreamReader.close();
			bufferedReader.close();
			Log.d("abcd", "" + ivt_type);
		} catch (Exception e) {

		} 
		return ivt_type;
	}
	private void setBTMode(String value) {
		final String btValue = value;
		AlertDialog ad = new AlertDialog.Builder(mActivity)
				.setTitle(R.string.reboot_warning)
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								
								String v = btValue;
								if ("-1".equals(btValue)) {
									v = "0";
								}
								File f = new File("/oem/lib/i140");
								if (f.exists()) {
									if ("-1".equals(btValue)) {
										if(getCurrenIVTBt() == 0){
											setCurrenIVTBt(1);
										}
									} else if ("0".equals(btValue)) {
										if (getCurrenIVTBt() == 1) {
											setCurrenIVTBt(0);
										}
									}
								}
								
								MachineConfig.setProperty(
										MachineConfig.KEY_BT_TYPE, v);

								BroadcastUtil.sendToCarService(getActivity(),
										MyCmd.Cmd.APP_REQUEST_SEND_KEY,
										MyCmd.Keycode.POWER);

								Util.doSleep(300);

								Util.sudoExec("sync");

								Util.doSleep(1000);
								Util.setFileValue("/sys/class/ak/source/factory", new byte[] { 0x55,
										(byte) 0xaa, 0x00 });
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel, null).create();
		ad.show();

		// updateBTMode();
	}
	
	private void setTVMode(String value) {
		MachineConfig.setProperty(MachineConfig.KEY_TV_TYPE, value);
		updateTVMode();
	}
	private void setLEDType(String value) {
		MachineConfig.setProperty(MachineConfig.KEY_LED_TYPE, value);
		updateLEDType();
	}

	private void setTpms(final String value) {
		AlertDialog ad = new AlertDialog.Builder(mActivity)
				.setTitle(R.string.reboot_warning)
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								MachineConfig.setProperty(
										MachineConfig.KEY_TPMS_TYPE, value);
								if (!value.equals("0")) {
									// mutual exclusion with FTJ
									MachineConfig.setProperty(
											MachineConfig.KEY_FTJ_TYPE, "0");
								}
								
								String appHide = MachineConfig.getProperty(
										MachineConfig.KEY_APP_HIDE);
								if ("2".equals(value)){									
									if (appHide == null){
										appHide = MachineConfig.getPropertyReadOnly(
												MachineConfig.KEY_APP_HIDE);
									}
									
									if (appHide == null){
										appHide = "DTV,JOYSTUDY,"+AppConfig.HIDE_TPMS;
									} else {
										if (!appHide.contains(AppConfig.HIDE_TPMS)){
											if (!appHide.endsWith(",")){
												appHide += ",";
											}
											appHide += AppConfig.HIDE_TPMS;
										}
									}
									MachineConfig.setProperty(MachineConfig.KEY_APP_HIDE, appHide);
								} else {
									if (appHide != null && appHide.contains(AppConfig.HIDE_TPMS)){
										appHide = appHide.replace(AppConfig.HIDE_TPMS, "");
										if (appHide.length()<=1){
											appHide = null;
										}										
										MachineConfig.setProperty(MachineConfig.KEY_APP_HIDE, appHide);
									}
								}
								

								BroadcastUtil.sendToCarService(getActivity(),
										MyCmd.Cmd.APP_REQUEST_SEND_KEY,
										MyCmd.Keycode.POWER);

								Util.doSleep(300);

								Util.sudoExec("sync");

								Util.doSleep(1000);
								Util.setFileValue("/sys/class/ak/source/factory", new byte[] { 0x55,
										(byte) 0xaa, 0x00 });
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel, null).create();
		ad.show();
	}
	
	private void setFtj(final String value) {
		AlertDialog ad = new AlertDialog.Builder(mActivity)
				.setTitle(R.string.reboot_warning)
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								MachineConfig.setProperty(
										MachineConfig.KEY_FTJ_TYPE, value);
								if (!value.equals("0")) {
									// mutual exclusion with TPMS
									MachineConfig.setProperty(
											MachineConfig.KEY_TPMS_TYPE, "0");
								}
								BroadcastUtil.sendToCarService(getActivity(),
										MyCmd.Cmd.APP_REQUEST_SEND_KEY,
										MyCmd.Keycode.POWER);

								Util.doSleep(300);

								Util.sudoExec("sync");

								Util.doSleep(1000);
								Util.setFileValue("/sys/class/ak/source/factory", new byte[] { 0x55,
										(byte) 0xaa, 0x00 });
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel, null).create();
		ad.show();
	}

	private void updateCameraMode() {

		String s = MachineConfig.getPropertyOnce(MachineConfig.KEY_CAMERA_TYPE);
		if (s == null) {
			s = "0";
		}
		mCameraMode.setValue(s);
		mCameraMode.setSummary(mCameraMode.getEntry());
	}

	private void setCameraMode(String value) {
		MachineConfig.setProperty(MachineConfig.KEY_CAMERA_TYPE, value);
		updateCameraMode();
		Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
		it.putExtra(MyCmd.EXTRA_COMMON_CMD, MachineConfig.KEY_APP_HIDE);
		mActivity.sendBroadcast(it);
	}

	String old_w;
	String old_h;

	private void updateScreenSize() {
		if (mScrennSizePreference != null) {
			old_w = MachineConfig.getPropertyOnce(MachineConfig.KEY_SCREEN_W);
			if (old_w == null) {
				old_w = "1024";
			}
			old_h = MachineConfig.getPropertyOnce(MachineConfig.KEY_SCREEN_H);
			if (old_h == null) {
				old_h = "600";
			}

			mScrennSizePreference.setValue(old_w + "," + old_h);
			mScrennSizePreference.setSummary(mScrennSizePreference.getEntry());
		}
	}
	private void updateSwitchToFrontCamera() {
		if (mSwitchToFrontCamera != null) {
			
			String s = MachineConfig.getPropertyOnce(MachineConfig.KEY_SWITCH_TO_FRONT_CAMER);
			if (s == null){
				s = "0";
			}
			mSwitchToFrontCamera.setValue(s);
			mSwitchToFrontCamera.setSummary(mSwitchToFrontCamera.getEntry());
		}
	}

	private void setSwitchSize(String value) {

		MachineConfig.setProperty(MachineConfig.KEY_SWITCH_TO_FRONT_CAMER, value);		
		mSwitchToFrontCamera.setValue(value);
		mSwitchToFrontCamera.setSummary(mSwitchToFrontCamera.getEntry());
		
		Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
		it.putExtra(MyCmd.EXTRA_COMMON_CMD, MachineConfig.KEY_SWITCH_TO_FRONT_CAMER);
		mActivity.sendBroadcast(it);

	}
	
	String w = null;
	String h = null;

	private void setScreenSize(String value) {

		w = null;
		h = null;
		String[] ss = value.split(",");

		if (ss.length > 1) {
			w = ss[0];
			h = ss[1];
		}

		if (w != null && h != null) {
			if (!w.equals(old_w) || !h.equals(old_h)) {

				AlertDialog ad = new AlertDialog.Builder(mActivity)
						.setTitle(R.string.screen_size_reboot)
						.setPositiveButton(R.string.alert_dialog_ok,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {

										MachineConfig.setProperty(
												MachineConfig.KEY_SCREEN_W, w);
										MachineConfig.setProperty(
												MachineConfig.KEY_SCREEN_H, h);
										Util.doSleep(300);

										Util.sudoExec("sync");

										Util.doSleep(400);
										Util.setFileValue("/sys/class/ak/source/factory", new byte[] { 0x55,
												(byte) 0xaa, 0x00 });
									}
								})
						.setNegativeButton(R.string.alert_dialog_cancel, null)
						.create();
				ad.show();

			}
		}
	}

	private static int getBrakeSetting() {
		return Util.getFileValue(MCU_BRAKE_DET_NODE);
	}

	private static void setBrakeSetting(int brake) {
		Util.setFileValue(MCU_BRAKE_DET_NODE, brake);
	}

	private static int getIllumSetting() {
		return Util.getFileValue(MCU_ILLUM_DET_NODE);
	}

	private static void setIllumSetting(int illum) {
		Util.setFileValue(MCU_ILLUM_DET_NODE, illum);
	}

	private void updateIllumSetting(int illum) {
		// mIllumSettingPreference.setValue(String.valueOf(illum));
		// mIllumSettingPreference.setSummary(mIllumSettingPreference.getEntry());
	}

	private static int getLedSetting() {
		return Util.getFileValue(MCU_LED_COLOR_NODE);
	}

	private static void setLedSetting(int rgb) {
		Util.setFileValue(MCU_LED_COLOR_NODE, rgb);
	}

	private String getCanboxSetting() {
		return MachineConfig.getProperty(MachineConfig.KEY_CAN_BOX);
	}

	private void setCanboxSetting(String which) {
		MachineConfig.setProperty(MachineConfig.KEY_CAN_BOX, which);

		// MachineConfig.notifyAll(mActivity);
		// if(MachineConfig.VALUE_CANBOX_FORD_SIMPLE.equals(which)){ //install
		// apk
		// String []ss = new String[]{"FocusSync.apk","CanboxSetting.apk"};
		// inistallApk(ss);
		// } else {
		//
		// }

		Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
		it.putExtra(MyCmd.EXTRA_COMMON_CMD, MachineConfig.KEY_CAN_BOX);
		mActivity.sendBroadcast(it);
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

	private static int getBeepSetting() {
		return Util.getFileValue(MCU_BEEP_NODE);
	}

	private static void setBeepSetting(int en) {
		Util.setFileValue(MCU_BEEP_NODE, en);
	}

	private static int getNaviMix() {
		return Util.getFileValue(MCU_NAVI_MIX_NODE);
	}

	private static void setNaviMix(int mix) {
		Util.setFileValue(MCU_NAVI_MIX_NODE, mix);
	}

	private HashSet<String> getAppHideSetting() {
		HashSet<String> set = new HashSet<String>();
		String value = MachineConfig.getProperty(MachineConfig.KEY_APP_HIDE);
		if (null != value) {
			if (value.contains("DTV")) {
				set.add("DTV");
			}
			if (value.contains("AUX")) {
				set.add("AUX");
			}
			if (value.contains("BT")) {
				set.add("BT");
			}
			if (value.contains(AppConfig.HIDE_APP_DVD)) {
				set.add(AppConfig.HIDE_APP_DVD);
			}
			if (value.contains(AppConfig.HIDE_APP_FRONT_CMAERA)) {
				set.add(AppConfig.HIDE_APP_FRONT_CMAERA);
			}
			if (value.contains(AppConfig.HIDE_APP_VIDEO_OUT)) {
				set.add(AppConfig.HIDE_APP_VIDEO_OUT);
			}
			if (value.contains(AppConfig.HIDE_APP_DVR)) {
				set.add(AppConfig.HIDE_APP_DVR);
			}
			if (value.contains(AppConfig.HIDE_APP_VIOCE_CONTROL)) {
				set.add(AppConfig.HIDE_APP_VIOCE_CONTROL);
			}
			if (value.contains(AppConfig.HIDE_APP_JOYSTUDY)) {
				set.add(AppConfig.HIDE_APP_JOYSTUDY);
			}
			if (value.contains(AppConfig.HIDE_APP_WHEELKEYSTUDY)) {
				set.add(AppConfig.HIDE_APP_WHEELKEYSTUDY);
			}
		} else {
			set.add("DTV");
			set.add(AppConfig.HIDE_APP_JOYSTUDY);
		}
		return set;
	}

	private void setAppHideSetting(HashSet<String> which) {
		String value = "";
		for (String s : which) {
			if (!value.isEmpty()) {
				value += ",";
			}
			value += s;
		}
		
		String tpms = MachineConfig.getProperty(
				MachineConfig.KEY_TPMS_TYPE);
		if ("2".equals(tpms)) {
			value += "," + AppConfig.HIDE_TPMS;
		}
		
		
		MachineConfig.setProperty(MachineConfig.KEY_APP_HIDE, value);
		// MachineConfig.notifyAll(mActivity);

		Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
		it.putExtra(MyCmd.EXTRA_COMMON_CMD, MachineConfig.KEY_APP_HIDE);
		mActivity.sendBroadcast(it);

	}

	private void updateAppHideSetting(HashSet<String> which) {
		mAppHidePreference.setValues(which);
		String summary = "";
		CharSequence[] entries = mAppHidePreference.getEntries();
		for (String s : which) {
			if (!summary.isEmpty())
				summary += ", ";
			int index = mAppHidePreference.findIndexOfValue(s);
			if (index >= 0) {
				summary += entries[mAppHidePreference.findIndexOfValue(s)];
			}
		}
		if (summary.isEmpty()) {
			summary = getResources().getString(R.string.no_app_hide);
		}
		mAppHidePreference.setSummary(summary);

	}

	private HashSet<String> getScreen1Setting() {
		HashSet<String> set = new HashSet<String>();
		String value = MachineConfig
				.getProperty(MachineConfig.KEY_SCREEN1_VIEW);
		if (null != value) {
			// String []ss = value.split(",");
			// for (String s:ss){
			// if()
			// }
			if (value.contains(MachineConfig.VALUE_SCREEN1_VIEW_BT)) {
				set.add(MachineConfig.VALUE_SCREEN1_VIEW_BT);
			}
			if (value.contains(MachineConfig.VALUE_SCREEN1_VIEW_REVERSE)) {
				set.add(MachineConfig.VALUE_SCREEN1_VIEW_REVERSE);
			}
		}
		return set;
	}

	private void setScreen1Setting(HashSet<String> which) {
		String value = "";
		for (String s : which) {
			if (!value.isEmpty()) {
				value += ",";
			}
			value += s;
		}
		MachineConfig.setProperty(MachineConfig.KEY_SCREEN1_VIEW, value);
		// MachineConfig.notifyAll(mActivity);

		Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
		it.putExtra(MyCmd.EXTRA_COMMON_CMD, MachineConfig.KEY_SCREEN1_VIEW);
		mActivity.sendBroadcast(it);

	}

	private void updateScreen1Setting(HashSet<String> which) {
		// if(mScreen1Preference==null){
		// return;
		// }
		// mScreen1Preference.setValues(which);
		// String summary = "";
		// CharSequence[] entries = mScreen1Preference.getEntries();
		// for (String s : which) {
		// if (!summary.isEmpty())
		// summary += ", ";
		// int index = mScreen1Preference.findIndexOfValue(s);
		// if (index >= 0) {
		// summary += entries[mScreen1Preference.findIndexOfValue(s)];
		// }
		// }
		// if (summary.isEmpty()) {
		// summary = getResources().getString(R.string.no_app_hide);
		// }
		// mScreen1Preference.setSummary(summary);
	}
	// ///////////
	// private int mInstallIndex = 0;
	// private String[] mInstallApk;
	//
	// private void inistallApk(String []name){
	// mInstallApk = name;
	// mInstallIndex = 0;
	// inistallApkNext();
	// }
	//
	// private void inistallApkNext(){
	//
	// if(mInstallIndex<mInstallApk.length){
	// SilentInstall(mInstallApk[mInstallIndex], "/mnt/paramter/apk");
	// ++mInstallIndex;
	// } else {//finish
	//
	// }
	// }
	//
	// private Handler mHandler = new Handler() {
	// public void handleMessage(Message msg) {
	// switch (msg.what) {
	// case 0:
	// inistallApkNext();
	// break;
	// }
	// super.handleMessage(msg);
	// }
	// };
	// class PackageInstallObserver extends IPackageInstallObserver.Stub {
	// public void packageInstalled(String packageName, int returnCode) {
	// // 闂佽崵濮村ú銏ゅ磿閺屻儱钃熼柣鏂跨殱閺嬫牠鏌￠崶銉ョ仼婵炲拑绻濆濠氬醇濞戞浠奸悷婊勬緲瀵墎绮嬮幒妤�唯闁挎梹鍎抽ˉ鎴︽⒑閻熺増璐￠柡鍜佹櫔turnCode闂備焦鐪归崝宀�锟芥凹鍓熼崺锟芥い鎺戝�哥敮鍫曟煕閵娿倗鐭欑�殿噮鍓熼幃褰掋�呴弶鎺楁煟鎼达絾鍤�妞わ附濞婅棢濠电姴娲ょ粻锝夋煙闁箑澧い顐ゅ█閺岀喓锟芥稒顭囨晶鎶芥煃瑜滈崜娆撳箠鎼淬劌桅闁瑰濮电�氾拷?
	// // 1闂佽崵鍋炵粙蹇涘礉鎼淬劌桅婵娉涚粻锝夋煙闁箑澧い顐嫹 0闂佽崵鍋炵粙蹇涘礉鎼淬劌桅婵﹩鍘肩欢鐐哄级閸偄浜悮锟�
	// if (returnCode == 1) {
	// Log.e(TAG, packageName + " install Success");
	// // initSettings();
	// } else {
	// Log.e(TAG, packageName + "install fail ");
	// }
	// mHandler.sendEmptyMessage(0);
	// }
	// }
	//
	// public void SilentInstall(String packageName, String path) {
	// Log.d(TAG, "SilentInstall:"+packageName + ":" +path);
	// File file = new File(path);
	// if (!file.exists()){
	// return;
	// }
	// Uri uri = Uri.fromFile(file);
	// int installFlags = 0;
	// PackageManager pm = getActivity().getPackageManager();
	// try {
	// PackageInfo packageInfo = pm.getPackageInfo(packageName,
	// PackageManager.GET_UNINSTALLED_PACKAGES);
	// if (packageInfo != null) {
	// installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
	// }
	// } catch (NameNotFoundException e) {
	// Log.e(TAG, "NameNotFoundException = " + e.getMessage());
	// }
	// PackageInstallObserver observer = new PackageInstallObserver(); //
	// PackageInstallObserver
	// // 闂佽绻愮换妤併仈缁嬭锝夋晸閿燂拷1濠电偞鍨堕幖鈺呭储婵傚憡鍋柛鏇ㄥ灡閸嬫繈鏌熷▓鍨灍闁伙綁浜堕幃宄扳枎韫囨搫鎷烽幖浣瑰剬闁稿瞼鍋為崵锟介梺鍛婁緱閸ㄧ敻锝為敓锟�
	// pm.installPackage(uri, observer, installFlags, packageName);
	// }
	
	private BroadcastReceiver mReceiver = null;

	public void registerListener() {
		if (mReceiver == null) {
			mReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();

					if (action.equals(MyCmd.BROADCAST_CAR_SERVICE_SEND)) {

						int cmd = intent.getIntExtra(MyCmd.EXTRA_COMMON_CMD, 0);

						switch (cmd) {
						case MyCmd.Cmd.SET_VCOM:
							mVcomValue = intent
									.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
							updateVcomValue();
							break;
						}

					}

				}
			};
			IntentFilter iFilter = new IntentFilter();
			iFilter.addAction(MyCmd.BROADCAST_CAR_SERVICE_SEND);

			getActivity().registerReceiver(mReceiver, iFilter);
		}
	}

	public void unregisterListener() {
		if (mReceiver != null) {
			getActivity().unregisterReceiver(mReceiver);
			mReceiver = null;
		}

	}
	
	private static int mVcomValue = 0;

	private void queryVcomValue() {
		BroadcastUtil.sendToCarService(getActivity(), MyCmd.Cmd.SET_VCOM, 0x100);
	}

	private void updateVcomValue() {
		if (mVCOM != null) {
			mVCOM.setSummary("" + mVcomValue);
		}
	}

	private IButtonCallBack mButtonCallBack = new IButtonCallBack() {
		@Override
		public void callback(String key, boolean add) {
			// TODO Auto-generated method stub
			if (KEY_VCOM.equals(key)) {
				if (add) {
					if (mVcomValue < 100) {
						++mVcomValue;
						BroadcastUtil.sendToCarService(getActivity(),
								MyCmd.Cmd.SET_VCOM, mVcomValue);
					}
				} else {
					if (mVcomValue > 0) {
						--mVcomValue;
						BroadcastUtil.sendToCarService(getActivity(),
								MyCmd.Cmd.SET_VCOM, mVcomValue);
					}
				}
			}
		}
	};
}
