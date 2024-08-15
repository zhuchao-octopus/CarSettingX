package com.my.factory;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.preference.SwitchPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.widget.TimePicker;

import com.common.util.BroadcastUtil;
import com.common.util.Kernel;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.common.util.MyCmd.Keycode;
import com.common.util.log.LogFile;
import com.common.util.shell.ShellUtils;
import com.my.factory.SeaNavigationChoice.NavigationChangeListener;
import com.my.factory.SettingsControllor.GeneralSettingListener;

import com.my.filemanager.FileManagerActivity.StorageInfo;
import com.octopus.android.carsettingx.R;
import com.zhuchao.android.fbase.MMLog;

import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import java.util.Iterator;

public class GeneralSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, OnPreferenceClickListener {
    private static final String TAG = "GeneralSettingsFragment";

    private static final String MCU_RECOVERY_FILE = "/sys/class/ak/source/factory";

    /**
     * 閻庝絻澹堥崺鍛姜椤栨瑦顐介悹浣稿⒔閻わ拷
     */
    private final static String KEY_NAVIGATION_CHOICE = "key_navigation_choice";
    private SeaNavigationChoice mSeaNavigationChoice;

    /**
     * 缂佹鍏涚粭渚�寮悷甯矗闂傚﹤鍘栫槐顓㈠礂閿燂拷
     */
    private final static String KEY_THIRD_APP_SOUND_FIRST = "key_third_app_sound_first";
    // private SwitchPreference mThirdAppSoundFirstCheckbox;
    private static final String MCU_ARM_SOUND_SW_FILE = "/sys/class/ak/source/arm_sound_switch";

    private static final String KEY_VIDEO_ON_DRIVING = "video_on_driving";
    private static final String KEY_VIDEO_ON_GPS = "video_on_gps";
    private static final String MCU_BRAKE_DET_NODE = "/sys/class/ak/source/reaksw";
    private SwitchPreference mVideoOnDrivingPreference;
    private SwitchPreference mVideoOnStopGPSPreference;

    private static final String KEY_STATIC_TRACK_BACK = "back_static_track";
    private SwitchPreference mTrackStatic;
    private static final String KEY_DYNAMIC_TRACK_BACK = "back_dynamic_track";
    private SwitchPreference mTrackDynamic;
    private static final String KEY_REVERSE_MIRROR = "reverse_mirror";
    private SwitchPreference mReverseMirror;

    private static final String KEY_DOOR_VOICE = "canbox_door_voice";
    private SwitchPreference mDoorVoice;

    private static final String KEY_FRONT_RADAR_CAMERA = "canbox_front_radar_camera";
    private SwitchPreference mRadarCamera;

    private static final String KEYPLAY_MUSIC = "playing_music_setting";
    private static final String KEY_SWITCH_DARK_MODE = "auto_switch_drak_mode";
    private static final String KEY_LAUNCHER_UI = "key_launcher_ui";
    private static final String KEY_RESET_VOLUME = "reset_volume";
    private static final String KEY_CAR_INFO_SCREENSAVER = "car_info_screensaver";
    private static final String KEY_MIC_TYPE = "mic_type";
    private SwitchPreference mPlayMusic;
    private SwitchPreference mSwitchDark;
    private static final String KEY_LED = "led_setting";
    private static final String MCU_LED_COLOR_NODE = "/sys/class/ak/source/led_color";
    private LedFragment mLedFragment = new LedFragment();

    private final static String KEY_RESET_SYSTEM = "reset_system";

    private final static String KEY_AUTO_ILL = "auto_ill";
    private final static String AUTO_ILL_ENABLE = "auto_ill_enable";
    private SwitchPreference mAutoIllPref;
    private int mAutoIllEn;

    private final static String KEY_CARCELL = "car_cell";
    private SwitchPreference mCarCellPref;
    private String mCarCellEn = "0";

    private final static String KEY_BTCELL = "bt_cell";
    private SwitchPreference mBTCellPref;
    private String mBTCellEn = "0";

    private final static String KEY_VIDEO_OUT = "video_out";
    private SwitchPreference mVideoOutPref;
    private String mVideoOutEn = "0";

    private final static String KEY_TEMP_DIS = "temp_dis_k";

    private ListPreference mTempDis;

    private final static String KEY_MILEAGE_UNIT = "mileage_unit";

    private ListPreference mMileageUnit;

    private final static String KEY_ILL_START_TIME = "ill_start_time";
    private final static String AUTO_ILL_START_HOUR = "auto_ill_start_hour";
    private final static String AUTO_ILL_START_MINUTE = "auto_ill_start_minute";
    private Preference mIllStartTimePref;
    private TimePickerDialog mIllStartTimePicker;
    private int mIllStartHour;
    private int mIllStartMinute;

    private final static String KEY_ILL_STOP_TIME = "ill_stop_time";
    private final static String AUTO_ILL_STOP_HOUR = "auto_ill_stop_hour";
    private final static String AUTO_ILL_STOP_MINUTE = "auto_ill_stop_minute";
    private Preference mIllStopTimePref;
    private TimePickerDialog mIllStopTimePicker;
    private int mIllStopHour;
    private int mIllStopMinute;

    private final static String KEY_UPDATE_ANDROID = "update_android";
    private final static String ANDROID_UPDATE_GUIDE_FILE_NAME = "ak48_update_guide.txt";
    private String mAndroidUpdatePath;

    private static final String KEY_TOUCH_SOUND = "touch_sound";
    private static final String MCU_BEEP_NODE = "/sys/class/ak/source/beep";
    private SwitchPreference mTouchSoundPreference;

    private static final String KEY_NAVI_MONITOR = "key_navi_monitor";
    private static final String KEY_NAVI_MIX = "navi_mix_setting";
    private ListPreference mNaviMixPreference;
    private static final String MCU_NAVI_MIX_NODE = "/sys/class/ak/source/navi_mix";
    private static final String KEY_NAVI_AUTO_DISPAUX = "key_navi_auto_dispaux";
    private SwitchPreference mNaviAutoDispAuxPreference;

    private static final String KEY_REVERSE_VOLUME = "reverse_volume";
    private ListPreference mRevserVolume;
    private static final String KEY_ACC_DELAY_POWEROFF = "acc_delay_poweroff";
    private ListPreference mAccDelayPoweroff;
    private static final String MCU_REVERSE_VOLUME = "/sys/class/ak/source/reverse_volume";

    private static final String KEY_DVR = "dvr_path_set";
    private ListPreference mDvrPath;

    private static final String KEY_VIDEO_DRIVING = "video_on_driving";
    private static final String KEY_WHEEL = "wheel_settigs";
    private static final String KEY_FACTORY = "factory_settigs";
    private static final String KEY_UPDATE_MCU = "update_mcu";

    private static final String KEY_TOUCH3_SETTINGS = "touch3_settings";
    private static final String KEY_TOUCH3_SWITCH = "touch3_settings_switch";
    private static final String KEY_TOUCH3_UPKEY = "touch3_settings_upkey";
    private static final String KEY_TOUCH3_DOWNKEY = "touch3_settings_downkey";
    private static final String KEY_TOUCH3_LEFTKEY = "touch3_settings_leftkey";
    private static final String KEY_TOUCH3_RIGHTKEY = "touch3_settings_rightkey";

    private static final String KEY_AD_STD_SETTINGS = "ad_std";
    private static final String KEY_AD_STD_APPLY = "ad_std_apply";
    //private static final String KEY_AD_STD_ENABLED = "ad_std_enabled";
    private static final String KEY_AD_STD_CH0 = "ad_channel0";
    private static final String KEY_AD_STD_CH1 = "ad_channel1";
    private static final String KEY_AD_STD_CH2 = "ad_channel2";
    private static final String KEY_AD_STD_CH3 = "ad_channel3";

    private static final String KEY_AUDIO_GAIN = "audio_gain";

    private static final String KEY_NISSIAN_360_SYSTEM = "nissian_360_system";
    private static final String KEY_NISSIAN_360_SYSTEM_SHOW_BUTTON = "nissian_360_system_show_button";

    private Preference mTouch3IdentifyPreference;
    private SwitchPreference mTouch3Switch;
    private ListPreference mTouch3UpKey;
    private ListPreference mTouch3DownKey;
    private ListPreference mTouch3LeftKey;
    private ListPreference mTouch3RightKey;
    private Touch3Config mTouch3Info = new Touch3Config();

    private Preference mADStdPreference;
    private Preference mADStdApply;
    //private SwitchPreference mADStdSwitch;
    private ListPreference mADStdCh0;
    private ListPreference mADStdCh1;
    private ListPreference mADStdCh2;
    private ListPreference mADStdCh3;
    private int[] adstd = new int[]{1, 0, 6, 6, 0};

    // update
    private static final int MSG_UPDATE_THIRD_APP_SOUND_FIRST = 5;
    private boolean mIsThirdAppSoundFirst;
    private static final int MSG_UPDATE_NAVIGATION_PACKAGE = 6;
    private String mNavigationPackageName, mNavigationClazzName;

    private static final int MSG_CHECK_RM_DATA = 8;
    private static final int MSG_CHECK_SYNC = 9;

    private Activity mActivity;
    private SettingsControllor mSettingControllor = null;
    // private ISettingsService mSettingsService = null;
    private SettingsDataHelper mSettingsDataHelper;

    private FragmentManager mFragmentManager;

    public static boolean mupdateNaviChoice = false;

    public void updateNaviChoice() {

        mupdateNaviChoice = true;
        if (mSeaNavigationChoice != null) {
            mSeaNavigationChoice.onClick();
        } else {
            // mupdateNaviChoice = true;
        }
    }

    private boolean hasDisplayAux() {
        DisplayManager displayManager = (DisplayManager) mActivity.getSystemService(Context.DISPLAY_SERVICE);
        Display[] display = displayManager.getDisplays();
        if (display.length <= 1) {
            return false;
        } else {
            return true;
        }
    }

    private void hideDisplayAuxSettings() {
        if (!(Util.isAndroidQ() && (Util.isPX30() || Util.isPX6()) && hasDisplayAux() && MachineConfig.getPropertyIntReadOnly(MachineConfig.KEY_DISPAUX_ENABLE) == 1)) {
            if (mNaviAutoDispAuxPreference != null) getPreferenceScreen().removePreference(mNaviAutoDispAuxPreference);
        }
    }

    private Preference Finger3SettingsPreferenceSaver = null;

    private void hideFinger3Settings() {
        if (!touch3UserConfigurable || !Util.isRKSystem()) {
            Finger3SettingsPreferenceSaver = findPreference(KEY_TOUCH3_SETTINGS);
            if (Finger3SettingsPreferenceSaver != null) getPreferenceScreen().removePreference(Finger3SettingsPreferenceSaver);
        } else {
            if (Finger3SettingsPreferenceSaver != null) getPreferenceScreen().addPreference(Finger3SettingsPreferenceSaver);
        }
    }

    private void initOldSettingView() {
        if (GeneralSettings.mSettingType == 1) {
            if (!GeneralSettings.mSystemUI.equals(MachineConfig.VALUE_SYSTEM_UI22_1050)) {
                Preference p;
                if (Util.isNexellSystem()) {
                    p = findPreference(KEY_ACC_DELAY_POWEROFF);
                    getPreferenceScreen().removePreference(p);
                }

                p = findPreference(KEY_AUDIO_GAIN);
                getPreferenceScreen().removePreference(p);
            }
            hideFinger3Settings();
            hideDisplayAuxSettings();
        } else {
            if (!GeneralSettings.mSystemUI.equals(MachineConfig.VALUE_SYSTEM_UI22_1050)) {
                Preference p;
                p = findPreference(KEY_AUDIO_GAIN);
                getPreferenceScreen().removePreference(p);
            }
        }
    }

    private HashMap<String, String> mPerferenceMap = new HashMap();

    private void categorizePreferenceScreen() {
        if (mPerferenceMap.isEmpty()) {
            mPerferenceMap.put(KEY_NAVIGATION_CHOICE, GeneralSettings.KEY_NAVI);
            mPerferenceMap.put(KEY_NAVI_MIX, GeneralSettings.KEY_NAVI);
            mPerferenceMap.put(KEY_NAVI_MONITOR, GeneralSettings.KEY_NAVI);
            if (Util.isAndroidQ() && (Util.isPX30() || Util.isPX6()) && hasDisplayAux() && MachineConfig.getPropertyIntReadOnly(MachineConfig.KEY_DISPAUX_ENABLE) == 1) {
                mPerferenceMap.put(KEY_NAVI_AUTO_DISPAUX, GeneralSettings.KEY_NAVI);
            } else {
                mPerferenceMap.put(KEY_NAVI_AUTO_DISPAUX, GeneralSettings.KEY_NONE);
            }

            mPerferenceMap.put(KEY_REVERSE_VOLUME, GeneralSettings.KEY_DRIVE);
            mPerferenceMap.put(KEY_VIDEO_DRIVING, GeneralSettings.KEY_DRIVE);
            mPerferenceMap.put(KEY_VIDEO_ON_GPS, GeneralSettings.KEY_DRIVE);
            mPerferenceMap.put(KEY_STATIC_TRACK_BACK, GeneralSettings.KEY_DRIVE);
            mPerferenceMap.put(KEY_DYNAMIC_TRACK_BACK, GeneralSettings.KEY_DRIVE);
            mPerferenceMap.put(KEY_REVERSE_MIRROR, GeneralSettings.KEY_DRIVE);
            mPerferenceMap.put(KEY_DOOR_VOICE, GeneralSettings.KEY_DRIVE);
            mPerferenceMap.put(KEY_FRONT_RADAR_CAMERA, GeneralSettings.KEY_DRIVE);
            mPerferenceMap.put(KEYPLAY_MUSIC, GeneralSettings.KEY_PERSONAL);
            mPerferenceMap.put(KEY_SWITCH_DARK_MODE, GeneralSettings.KEY_PERSONAL);
            mPerferenceMap.put(KEY_LAUNCHER_UI, GeneralSettings.KEY_PERSONAL);
            mPerferenceMap.put(KEY_RESET_VOLUME, GeneralSettings.KEY_PERSONAL);
            mPerferenceMap.put(KEY_CAR_INFO_SCREENSAVER, GeneralSettings.KEY_PERSONAL);
            mPerferenceMap.put(KEY_MIC_TYPE, GeneralSettings.KEY_PERSONAL);

            mPerferenceMap.put(KEY_TOUCH_SOUND, GeneralSettings.KEY_PERSONAL);
            mPerferenceMap.put(KEY_LED, GeneralSettings.KEY_PERSONAL);
            mPerferenceMap.put(KEY_AUTO_ILL, GeneralSettings.KEY_PERSONAL);
            mPerferenceMap.put(KEY_CARCELL, GeneralSettings.KEY_PERSONAL);
            mPerferenceMap.put(KEY_BTCELL, GeneralSettings.KEY_PERSONAL);
            mPerferenceMap.put(KEY_VIDEO_OUT, isMS9120Plugin(getActivity()) ? GeneralSettings.KEY_PERSONAL : GeneralSettings.KEY_NONE);
            mPerferenceMap.put(KEY_TEMP_DIS, GeneralSettings.KEY_PERSONAL);
            mPerferenceMap.put(KEY_MILEAGE_UNIT, GeneralSettings.KEY_PERSONAL);
            mPerferenceMap.put(KEY_ILL_START_TIME, GeneralSettings.KEY_PERSONAL);
            mPerferenceMap.put(KEY_ILL_STOP_TIME, GeneralSettings.KEY_PERSONAL);

            mPerferenceMap.put(KEY_WHEEL, GeneralSettings.KEY_DRIVE);

            mPerferenceMap.put(KEY_FACTORY, GeneralSettings.KEY_FACTORY);

            mPerferenceMap.put(KEY_UPDATE_MCU, GeneralSettings.KEY_UPGRADE);
            mPerferenceMap.put(KEY_UPDATE_ANDROID, GeneralSettings.KEY_UPGRADE);
            mPerferenceMap.put(KEY_RESET_SYSTEM, GeneralSettings.KEY_UPGRADE);

            mPerferenceMap.put(KEY_ACC_DELAY_POWEROFF, GeneralSettings.KEY_PERSONAL);
            mPerferenceMap.put(KEY_AUDIO_GAIN, GeneralSettings.KEY_NONE);
            if (GeneralSettings.mSystemUI.equals(MachineConfig.VALUE_SYSTEM_UI22_1050)) {
                mPerferenceMap.put(KEY_ACC_DELAY_POWEROFF, GeneralSettings.KEY_NONE);
                mPerferenceMap.put(KEY_AUDIO_GAIN, GeneralSettings.KEY_PERSONAL);
                mPerferenceMap.put(KEY_CARCELL, GeneralSettings.KEY_NONE);
                mPerferenceMap.put(KEY_BTCELL, GeneralSettings.KEY_NONE);
                mPerferenceMap.put(KEY_NAVI_MONITOR, GeneralSettings.KEY_NONE);
                mPerferenceMap.put(KEY_DYNAMIC_TRACK_BACK, GeneralSettings.KEY_NONE);
                mPerferenceMap.put(KEY_FRONT_RADAR_CAMERA, GeneralSettings.KEY_NONE);
                mPerferenceMap.put(KEY_DOOR_VOICE, GeneralSettings.KEY_NONE);
                mPerferenceMap.put(KEY_RESET_SYSTEM, GeneralSettings.KEY_NONE);
            } else if (GeneralSettings.mSystemUI.equals(MachineConfig.VALUE_SYSTEM_UI33_IXB)) {
                mPerferenceMap.put(KEY_ACC_DELAY_POWEROFF, GeneralSettings.KEY_NONE);
                mPerferenceMap.put(KEY_AUDIO_GAIN, GeneralSettings.KEY_NONE);
                mPerferenceMap.put(KEY_STATIC_TRACK_BACK, GeneralSettings.KEY_NONE);
                mPerferenceMap.put(KEY_DYNAMIC_TRACK_BACK, GeneralSettings.KEY_NONE);
                mPerferenceMap.put(KEY_REVERSE_MIRROR, GeneralSettings.KEY_NONE);
            } else if (GeneralSettings.mSystemUI.equals(MachineConfig.VALUE_SYSTEM_UI20_RM10_1) || GeneralSettings.mSystemUI.equals(MachineConfig.VALUE_SYSTEM_UI21_RM10_2) || GeneralSettings.mSystemUI.equals(MachineConfig.VALUE_SYSTEM_UI21_RM12)) {
                mPerferenceMap.put(KEY_BTCELL, GeneralSettings.KEY_NONE);
            } else {
                mPerferenceMap.put(KEY_ACC_DELAY_POWEROFF, GeneralSettings.KEY_PERSONAL);
                mPerferenceMap.put(KEY_AUDIO_GAIN, GeneralSettings.KEY_NONE);
            }

            if (supportADStd()) mPerferenceMap.put(KEY_AD_STD_SETTINGS, GeneralSettings.KEY_DRIVE);
            else mPerferenceMap.put(KEY_AD_STD_SETTINGS, GeneralSettings.KEY_NONE);
            //check gps monitor KEY_HIDE_LAUNCHER
            String s = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_HIDE_LAUNCHER);
            if (s != null && s.contains("gpstest")) {
                mPerferenceMap.put(KEY_NAVI_MONITOR, GeneralSettings.KEY_NONE);
            }
        }
        if (touch3UserConfigurable && Util.isRKSystem()) mPerferenceMap.put(KEY_TOUCH3_SETTINGS, GeneralSettings.KEY_PERSONAL);
        else mPerferenceMap.put(KEY_TOUCH3_SETTINGS, GeneralSettings.KEY_NONE);
    }

    private void togglePreferenceScreen() {
        try {
            Bundle b = getArguments();
            if (b != null) {
                String preference_type = b.getString("preference_type");
                // Log.d(TAG, ">>>>>>>>>>>>>preference_type = " +
                // preference_type + " ," + mPerferenceMap.size());
                if (preference_type != null && !preference_type.isEmpty()) {
                    for (HashMap.Entry<String, String> entry : mPerferenceMap.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();
                        // Log.d(TAG, "key=" + key + " ,value=" + value);
                        if (value != null && !value.equals(preference_type)) {
                            Preference p = findPreference(key);
                            if (p != null) {
                                getPreferenceScreen().removePreference(p);
                                // Log.d(TAG, "remove key=" + key);
                            }
                        }
                    }

                    if (preference_type.equals(GeneralSettings.KEY_PERSONAL)) {
                        addSpecialPreference();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addSpecialPreference() {
        try {
            if (GeneralSettings.mSystemUI != null && MachineConfig.VALUE_SYSTEM_UI22_1050.equals(GeneralSettings.mSystemUI)) {
                Preference preference = new Preference(getActivity());
                // preference.setTitle(getResources().getString(R.string.title_wheel_control));
                // preference.setLayoutResource(R.layout.preference_car_settings_submenu);
                // getPreferenceScreen().addPreference(preference);
                Intent intent = new Intent();
                // intent.setClassName("com.SwcApplication","com.SwcApplication.SwcActivity");
                // preference.setIntent(intent);
                //
                // preference = new Preference(getActivity());
                preference.setTitle(getResources().getString(R.string.title_eq));
                preference.setLayoutResource(R.layout.preference_car_settings_submenu);
                getPreferenceScreen().addPreference(preference);
                intent = new Intent();
                intent.setClassName("com.eqset", "com.eqset.EQActivity");
                preference.setIntent(intent);

                // preference = new Preference(getActivity());
                // preference.setTitle(getResources().getString(R.string.title_gps_search));
                // preference.setLayoutResource(R.layout.preference_car_settings_submenu);
                // getPreferenceScreen().addPreference(preference);
                // intent = new Intent();
                // intent.setClassName("com.chartcross.gpstest","com.chartcross.gpstest.GPSTest");
                // preference.setIntent(intent);

                String mCanboxType = MachineConfig.getPropertyOnce(MachineConfig.KEY_CAN_BOX);
                if (mCanboxType != null) {
                    String[] ss = mCanboxType.split(",");
                    mCanboxType = ss[0];
                    int mCarType = 0;
                    if (MachineConfig.VALUE_CANBOX_NISSAN2013.equals(mCanboxType)) {
                        try {
                            for (int i = 1; i < ss.length; ++i) {
                                if (ss[i].startsWith(MachineConfig.KEY_SUB_CANBOX_CAR_TYPE)) {
                                    mCarType = Integer.valueOf(ss[i].substring(1));
                                }
                            }
                        } catch (Exception e) {

                        }
                    }

                    if (mCarType == 3) {
                        preference = new SwitchPreference(getActivity());
                        preference.setKey(KEY_NISSIAN_360_SYSTEM);
                        preference.setTitle(getResources().getString(R.string.degrees_360));
                        preference.setLayoutResource(R.layout.preference_car_settings_submenu);
                        getPreferenceScreen().addPreference(preference);

                        preference.setOnPreferenceChangeListener(this);

                        preference = new SwitchPreference(getActivity());
                        preference.setKey(KEY_NISSIAN_360_SYSTEM_SHOW_BUTTON);
                        preference.setTitle(getResources().getString(R.string.show_360_btn));
                        preference.setLayoutResource(R.layout.preference_car_settings_submenu);
                        getPreferenceScreen().addPreference(preference);
                        preference.setOnPreferenceChangeListener(this);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean touch3UserConfigurable = false;
    boolean touch3Enabled = false;

    private boolean getTouch3ConfigValue(Context context) {
        boolean changed = false;
        // String value = SystemConfig.getProperty(context,
        // MachineConfig.KEY_TOUCH3_IDENTIFY);
        String value = MachineConfig.getProperty(MachineConfig.KEY_TOUCH3_IDENTIFY);
        // Log.d(TAG, "getTouch3ConfigValue: " + value);
        if (value != null && !value.isEmpty()) {
            JSONObject jobj;
            try {
                jobj = new JSONObject(value);
            } catch (JSONException e1) {
                e1.printStackTrace();
                return changed;
            }
            boolean temp = false;
            try {
                temp = jobj.getBoolean(Touch3Config.KEY_USER_CONFIGABLE);
            } catch (JSONException e1) {
                temp = false;
            }
            if (touch3UserConfigurable != temp) changed = true;
            touch3UserConfigurable = temp;
            try {
                temp = jobj.getBoolean(Touch3Config.KEY_SWITCH);
            } catch (JSONException e1) {
                temp = false;
            }
            if (touch3Enabled != temp) changed = true;
            touch3Enabled = temp;
            updateTouch3SwitchPreference(touch3Enabled);
            if (touch3Enabled && touch3UserConfigurable) {
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

        return changed;
    }

    private void updateTouch3SwitchPreference(boolean value) {
        if (value) {
            mTouch3Switch.setChecked(true);
            mTouch3Switch.setSummary(getResources().getString(R.string.enable));
        } else {
            mTouch3Switch.setChecked(false);
            mTouch3Switch.setSummary(getResources().getString(R.string.disable));
        }
    }

    private void updateTouch3Preference(ListPreference preference, String value, boolean save) {
        preference.setValue((String) value);
        preference.setSummary(preference.getEntry());
        if (save) {
            mTouch3Info.saveConfigJSON(false, getActivity(), mTouch3Switch.isChecked(), mTouch3UpKey.getValue(), mTouch3DownKey.getValue(), mTouch3LeftKey.getValue(), mTouch3RightKey.getValue(), mTouch3UpKey.getEntry(), mTouch3DownKey.getEntry(), mTouch3LeftKey.getEntry(), mTouch3RightKey.getEntry());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Do dummy get.
        // Some setting need to communicate with MCU.
        // Make sure they are valid at next get.
        getBrakeSetting();
        getLedSetting();
        getBeepSetting();
        getNaviMix();

        mSettingsDataHelper = SettingsDataHelper.init(getActivity().getApplicationContext());
        mActivity = getActivity();
        addPreferencesFromResource(R.xml.general_settings);
        mSettingControllor = SettingsControllor.getInstance(getActivity().getApplicationContext());
        // mSettingsService = mSettingControllor.getFeature();

        // 閻庝絻澹堥崺鍛姜椤栨瑦顐介悹浣稿⒔閻わ拷
        mSeaNavigationChoice = ((SeaNavigationChoice) findPreference(KEY_NAVIGATION_CHOICE));
        if (mupdateNaviChoice) {
            mSeaNavigationChoice.onClick();
        }

        mNaviMixPreference = (ListPreference) findPreference(KEY_NAVI_MIX);
        mNaviMixPreference.setOnPreferenceChangeListener(this);

        String value = SystemConfig.getProperty(getActivity(), MachineConfig.KEY_DISPAUX_APPAUTO_DISABLE);
        final boolean autoDispMapToAux = (value == null || !value.equals("1"));
        mNaviAutoDispAuxPreference = (SwitchPreference) findPreference(KEY_NAVI_AUTO_DISPAUX);
        mNaviAutoDispAuxPreference.setOnPreferenceChangeListener(this);
        mNaviAutoDispAuxPreference.setChecked(!autoDispMapToAux);

        mRevserVolume = (ListPreference) findPreference(KEY_REVERSE_VOLUME);
        mRevserVolume.setOnPreferenceChangeListener(this);

        mAccDelayPoweroff = (ListPreference) findPreference(KEY_ACC_DELAY_POWEROFF);
        mAccDelayPoweroff.setOnPreferenceChangeListener(this);
        mDvrPath = (ListPreference) findPreference(KEY_DVR);
        if (mDvrPath != null) {
            mDvrPath.setOnPreferenceChangeListener(this);
        }

        // 缂佹鍏涚粭渚�寮悷甯矗闂傚﹤鍘栫槐顓㈠礂閿燂拷
        // mThirdAppSoundFirstCheckbox = (SwitchPreference)
        // findPreference(KEY_THIRD_APP_SOUND_FIRST);
        // mThirdAppSoundFirstCheckbox.setOnPreferenceChangeListener(this);

        mTrackStatic = (SwitchPreference) findPreference(KEY_STATIC_TRACK_BACK);
        mTrackStatic.setOnPreferenceChangeListener(this);
        mTrackDynamic = (SwitchPreference) findPreference(KEY_DYNAMIC_TRACK_BACK);
        mTrackDynamic.setOnPreferenceChangeListener(this);
        mReverseMirror = (SwitchPreference) findPreference(KEY_REVERSE_MIRROR);
        mReverseMirror.setOnPreferenceChangeListener(this);

        mDoorVoice = (SwitchPreference) findPreference(KEY_DOOR_VOICE);
        mDoorVoice.setOnPreferenceChangeListener(this);
        boolean b = Settings.Global.getInt(mActivity.getContentResolver(), SystemConfig.CANBOX_DOOR_VOICE, 0) == 1;
        mDoorVoice.setChecked(b);

        mRadarCamera = (SwitchPreference) findPreference(KEY_FRONT_RADAR_CAMERA);
        mRadarCamera.setOnPreferenceChangeListener(this);
        b = Settings.Global.getInt(mActivity.getContentResolver(), SystemConfig.CANBOX_FRONT_RADAR_OPEN_CAMERA, 0) == 1;
        mRadarCamera.setChecked(b);

        mPlayMusic = (SwitchPreference) findPreference(KEYPLAY_MUSIC);
        mPlayMusic.setOnPreferenceChangeListener(this);

        mSwitchDark = (SwitchPreference) findPreference(KEY_SWITCH_DARK_MODE);
        mSwitchDark.setOnPreferenceChangeListener(this);

        findPreference(KEY_LED).setOnPreferenceClickListener(this);

        findPreference(KEY_UPDATE_ANDROID).setOnPreferenceClickListener(this);
        findPreference(KEY_RESET_SYSTEM).setOnPreferenceClickListener(this);
        findPreference(KEY_RESET_VOLUME).setOnPreferenceChangeListener(this);
        findPreference(KEY_CAR_INFO_SCREENSAVER).setOnPreferenceChangeListener(this);
        findPreference(KEY_MIC_TYPE).setOnPreferenceChangeListener(this);

        mVideoOnDrivingPreference = (SwitchPreference) findPreference(KEY_VIDEO_ON_DRIVING);
        mVideoOnDrivingPreference.setOnPreferenceChangeListener(this);

        mVideoOnStopGPSPreference = (SwitchPreference) findPreference(KEY_VIDEO_ON_GPS);
        String s = MachineConfig.getPropertyReadOnly(SystemConfig.GPS_BRAKE);
        if ("1".equals(s)) {
            mVideoOnStopGPSPreference.setOnPreferenceChangeListener(this);
        } else {
            getPreferenceScreen().removePreference(mVideoOnStopGPSPreference);
        }
        updateBrakeGPSSetting(SystemConfig.getIntProperty(mActivity, SystemConfig.GPS_BRAKE));

        mAutoIllPref = (SwitchPreference) findPreference(KEY_AUTO_ILL);
        mAutoIllPref.setOnPreferenceChangeListener(this);
        mAutoIllEn = Settings.Global.getInt(mActivity.getContentResolver(), AUTO_ILL_ENABLE, 0);

        mCarCellPref = (SwitchPreference) findPreference(KEY_CARCELL);
        mCarCellPref.setOnPreferenceChangeListener(this);
        mCarCellEn = SystemConfig.getProperty(mActivity, SystemConfig.KEY_CAR_CELL);
        updateCarCell();

        mBTCellPref = (SwitchPreference) findPreference(KEY_BTCELL);
        mBTCellPref.setOnPreferenceChangeListener(this);
        mBTCellEn = SystemConfig.getProperty(mActivity, SystemConfig.KEY_BT_CELL);
        updateBTCell();

        mVideoOutPref = (SwitchPreference) findPreference(KEY_VIDEO_OUT);
        mVideoOutPref.setOnPreferenceChangeListener(this);
        mVideoOutEn = SystemConfig.getProperty(mActivity, SystemConfig.KEY_VIDEO_OUT);
        updateVideoOut();

        mIllStartHour = Settings.Global.getInt(mActivity.getContentResolver(), AUTO_ILL_START_HOUR, 19);
        mIllStartMinute = Settings.Global.getInt(mActivity.getContentResolver(), AUTO_ILL_START_MINUTE, 0);
        mIllStartTimePref = findPreference(KEY_ILL_START_TIME);
        mIllStartTimePref.setOnPreferenceClickListener(this);
        mIllStartTimePicker = new TimePickerDialog(mActivity, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mIllStartHour = hourOfDay;
                mIllStartMinute = minute;
                Settings.Global.putInt(mActivity.getContentResolver(), AUTO_ILL_START_HOUR, mIllStartHour);
                Settings.Global.putInt(mActivity.getContentResolver(), AUTO_ILL_START_MINUTE, mIllStartMinute);
                updateAutoIll();
            }
        }, 0, 0, true);

        mIllStopHour = Settings.Global.getInt(mActivity.getContentResolver(), AUTO_ILL_STOP_HOUR, 7);
        mIllStopMinute = Settings.Global.getInt(mActivity.getContentResolver(), AUTO_ILL_STOP_MINUTE, 0);
        mIllStopTimePref = findPreference(KEY_ILL_STOP_TIME);
        mIllStopTimePref.setSummary(DateFormat.getTimeFormat(mActivity).format(new Date(2017, 1, 6, 7, 0)));
        mIllStopTimePref.setOnPreferenceClickListener(this);
        mIllStopTimePicker = new TimePickerDialog(mActivity, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mIllStopHour = hourOfDay;
                mIllStopMinute = minute;
                Settings.Global.putInt(mActivity.getContentResolver(), AUTO_ILL_STOP_HOUR, mIllStopHour);
                Settings.Global.putInt(mActivity.getContentResolver(), AUTO_ILL_STOP_MINUTE, mIllStopMinute);
                updateAutoIll();
            }
        }, 0, 0, true);

        updateAutoIll();
        if (GeneralSettings.mSystemUI.equals(MachineConfig.VALUE_SYSTEM_UI22_1050)) {
            findPreference(KEY_WHEEL).setSummary("");
        }

        mTouchSoundPreference = (SwitchPreference) findPreference(KEY_TOUCH_SOUND);
        mTouchSoundPreference.setOnPreferenceChangeListener(this);

        mFragmentManager = getFragmentManager();

        // touch to andorid Settings.apk
        // if(Util.isNexellSystem()){
        getPreferenceScreen().removePreference(mTouchSoundPreference);
        // }

        mTouch3IdentifyPreference = findPreference(KEY_TOUCH3_SETTINGS);
        if (mTouch3IdentifyPreference != null) {
            mTouch3Switch = (SwitchPreference) findPreference(KEY_TOUCH3_SWITCH);
            mTouch3UpKey = (ListPreference) findPreference(KEY_TOUCH3_UPKEY);
            mTouch3DownKey = (ListPreference) findPreference(KEY_TOUCH3_DOWNKEY);
            mTouch3LeftKey = (ListPreference) findPreference(KEY_TOUCH3_LEFTKEY);
            mTouch3RightKey = (ListPreference) findPreference(KEY_TOUCH3_RIGHTKEY);
            if (mTouch3Switch != null) mTouch3Switch.setOnPreferenceChangeListener(this);
            if (mTouch3UpKey != null) mTouch3UpKey.setOnPreferenceChangeListener(this);
            if (mTouch3DownKey != null) mTouch3DownKey.setOnPreferenceChangeListener(this);
            if (mTouch3LeftKey != null) mTouch3LeftKey.setOnPreferenceChangeListener(this);
            if (mTouch3RightKey != null) mTouch3RightKey.setOnPreferenceChangeListener(this);
            mTouch3Info.loadTouch3Entries(getActivity());
            mTouch3UpKey.setEntries(mTouch3Info.mEntries);
            mTouch3UpKey.setEntryValues(mTouch3Info.mEntriesVlue);
            mTouch3DownKey.setEntries(mTouch3Info.mEntries);
            mTouch3DownKey.setEntryValues(mTouch3Info.mEntriesVlue);
            mTouch3LeftKey.setEntries(mTouch3Info.mEntries);
            mTouch3LeftKey.setEntryValues(mTouch3Info.mEntriesVlue);
            mTouch3RightKey.setEntries(mTouch3Info.mEntries);
            mTouch3RightKey.setEntryValues(mTouch3Info.mEntriesVlue);
        }
        getTouch3ConfigValue(getActivity());

        mADStdPreference = findPreference(KEY_AD_STD_SETTINGS);
        if (mADStdPreference != null) {
            mADStdApply = findPreference(KEY_AD_STD_APPLY);
            //mADStdSwitch = (SwitchPreference) findPreference(KEY_AD_STD_ENABLED);
            mADStdCh0 = (ListPreference) findPreference(KEY_AD_STD_CH0);
            mADStdCh1 = (ListPreference) findPreference(KEY_AD_STD_CH1);
            mADStdCh2 = (ListPreference) findPreference(KEY_AD_STD_CH2);
            mADStdCh3 = (ListPreference) findPreference(KEY_AD_STD_CH3);

            mADStdApply.setOnPreferenceClickListener(this);
            //mADStdSwitch.setOnPreferenceChangeListener(this);
            mADStdCh0.setOnPreferenceChangeListener(this);
            mADStdCh1.setOnPreferenceChangeListener(this);
            mADStdCh2.setOnPreferenceChangeListener(this);
            mADStdCh3.setOnPreferenceChangeListener(this);
        }

        categorizePreferenceScreen();
        initOldSettingView();
        togglePreferenceScreen();
        customUI();
        updateResetVolume();
        updateCarSreenSaver();
        updateMicType();
    }

    private void customUI() {
        if (!GeneralSettings.isExtShow(GeneralSettings.SETTINGS_SHOW_OBD_SCREEN)) {
            try {
                Preference p = findPreference(KEY_CAR_INFO_SCREENSAVER);
                if (p != null) {
                    getPreferenceScreen().removePreference(p);
                }
            } catch (Exception ignored) {
            }
        }

        if (!GeneralSettings.isExtShow(GeneralSettings.SETTINGS_SHOW_MIC_TYPE)) {
            try {
                Preference p = findPreference(KEY_MIC_TYPE);
                if (p != null) {
                    getPreferenceScreen().removePreference(p);
                }
            } catch (Exception ignored) {
            }
        }

        if (GeneralSettings.isExtHide(GeneralSettings.SETTINGS_HIDE_STATIC_TRACK)) {
            try {
                Preference p = findPreference(KEY_STATIC_TRACK_BACK);
                if (p != null) {
                    getPreferenceScreen().removePreference(p);
                }
            } catch (Exception ignored) {
            }
        }

        if (GeneralSettings.isExtHide(GeneralSettings.SETTINGS_HIDE_DYNAMIC_TRACK)) {
            try {
                Preference p = findPreference(KEY_DYNAMIC_TRACK_BACK);
                if (p != null) {
                    getPreferenceScreen().removePreference(p);
                }
            } catch (Exception ignored) {
            }
        }

        if (GeneralSettings.isExtHide(GeneralSettings.SETTINGS_HIDE_VIDEO_ON_DRIVER)) {
            try {
                Preference p = findPreference(KEY_VIDEO_ON_DRIVING);
                if (p != null) {
                    getPreferenceScreen().removePreference(p);
                }
            } catch (Exception ignored) {
            }
        }
        // if
        // (!GeneralSettings.isExtShow(GeneralSettings.SETTINGS_EXT_SHOW_RADAR_CAMERA)){
        // try{
        // getPreferenceScreen().removePreference(mRadarCamera);
        // }catch(Exception e){
        //
        // }
        // }

        // value = MachineConfig.VALUE_SYSTEM_UI20_RM10_1; // test
        Preference p = findPreference(KEY_LAUNCHER_UI);
        if (p != null) {

            // if
            // (MachineConfig.VALUE_SYSTEM_UI20_RM10_1.equals(GeneralSettings.mSystemUI)
            // ||
            // MachineConfig.VALUE_SYSTEM_UI21_RM10_2.equals(GeneralSettings.mSystemUI)
            // ||
            // MachineConfig.VALUE_SYSTEM_UI21_RM12.equals(GeneralSettings.mSystemUI))
            // {
            // ListPreference lp = (ListPreference) p;
            // lp.setOnPreferenceChangeListener(this);
            // updateLauncherUI();
            // } else {
            getPreferenceScreen().removePreference(p);
            // }
        }

        p = findPreference(KEY_SWITCH_DARK_MODE);
        if (p != null) {
            String s = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_SHOW_DARK_MODE);
            // if (MachineConfig.VALUE_SYSTEM_UI40_KLD90.equals(s)
            // || MachineConfig.VALUE_SYSTEM_UI44_KLD007
            // .equals(GeneralSettings.mSystemUI)) {
            if ("1".equals(s)) {
                mAutoDark = SystemConfig.getIntProperty(mActivity, SystemConfig.KEY_DARK_MODE_SWITCH);
                if (mSwitchDark != null) {
                    mSwitchDark.setChecked(mAutoDark == 1);
                }
            } else {
                getPreferenceScreen().removePreference(p);
            }
        }

        // if (mTempDis == null){
        ListPreference lp;
        lp = (ListPreference) findPreference(KEY_TEMP_DIS);
        if (lp != null) {
            mTempDis = lp;
            mTempDis.setOnPreferenceChangeListener(this);
        }
        // }
        // if (mMileageUnit == null){
        lp = (ListPreference) findPreference(KEY_MILEAGE_UNIT);
        if (lp != null) {
            mMileageUnit = lp;
            mMileageUnit.setOnPreferenceChangeListener(this);
        }
        // }
        if (GeneralSettings.mSettingType != 1) {
            Bundle b = getArguments();
            if (b != null) {
                String preference_type = b.getString("preference_type");
                if (preference_type.equals(GeneralSettings.KEY_PERSONAL)) {
                    String canbox = MachineConfig.getProperty(MachineConfig.KEY_CAN_BOX);
                    try {
                        if (canbox != null && (canbox.startsWith(MachineConfig.VALUE_CANBOX_HY) || canbox.startsWith(MachineConfig.VALUE_CANBOX_GM_SIMPLE) || canbox.startsWith(MachineConfig.VALUE_CANBOX_ACCORD7_CHANGYUANTONG))) {
                            if (findPreference(KEY_TEMP_DIS) == null) {
                                if (mTempDis != null) {

                                    getPreferenceScreen().addPreference(mTempDis);
                                }
                            }
                            if (findPreference(KEY_MILEAGE_UNIT) == null) {
                                if (mMileageUnit != null) {
                                    getPreferenceScreen().addPreference(mMileageUnit);
                                }
                            }

                            // mMileageUnit.setOnPreferenceChangeListener(this);
                            // mTempDis.setOnPreferenceChangeListener(this);
                        } else {
                            if (findPreference(KEY_TEMP_DIS) != null) {
                                getPreferenceScreen().removePreference(mTempDis);
                            }
                            if (findPreference(KEY_MILEAGE_UNIT) != null) {
                                getPreferenceScreen().removePreference(mMileageUnit);
                            }

                        }
                    } catch (Exception e) {

                    }

                }
            }
        } else {
            String canbox = MachineConfig.getProperty(MachineConfig.KEY_CAN_BOX);
            try {
                if (canbox == null || !canbox.startsWith(MachineConfig.VALUE_CANBOX_HY)) {
                    if (findPreference(KEY_TEMP_DIS) != null) {
                        getPreferenceScreen().removePreference(mTempDis);
                    }
                    if (findPreference(KEY_MILEAGE_UNIT) != null) {
                        getPreferenceScreen().removePreference(mMileageUnit);
                    }
                }
            } catch (Exception e) {

            }
        }
    }

    private void updateLauncherUI() {
        String value = SystemConfig.getProperty(getActivity(), SystemConfig.KEY_LAUNCHER_UI_RM10);
        if (value == null) {
            value = "0";
        }
        if (value != null) {
            Preference p = findPreference(KEY_LAUNCHER_UI);
            if (p != null) {
                ListPreference lp = (ListPreference) p;

                lp.setValue(value);
                lp.setSummary(lp.getEntry());
            }
        }
    }

    private void updateResetVolume() {
        Preference p = findPreference(KEY_RESET_VOLUME);
        int value = SystemConfig.getIntProperty2(getActivity(), SystemConfig.KEY_DEFAULT_RESET_VOLUME_LEVEL);
        if (value == -1) {
            value = 12;
        }
        if (p != null) {
            ListPreference lp = (ListPreference) p;

            lp.setValue(value + "");
            lp.setSummary(lp.getEntry());
        }

    }

    private void setResetVolume(Preference p, String value) {
        ListPreference lp = (ListPreference) p;

        lp.setValue(value);
        lp.setSummary(lp.getEntry());
        int v = -1;
        try {
            v = Integer.valueOf(value);
            SystemConfig.setIntProperty(getActivity(), SystemConfig.KEY_DEFAULT_RESET_VOLUME_LEVEL, v);
        } catch (Exception e) {

        }

    }

    private void updateMicType() {
        Preference p = findPreference(KEY_MIC_TYPE);
        int value = SystemConfig.getIntProperty2(getActivity(), SystemConfig.KEY_MIC_TYPE);
        if (value < 0 || value > 1) {
            value = 1;
        }

        if (p != null) {
            ListPreference lp = (ListPreference) p;

            lp.setValue(value + "");
            lp.setSummary(lp.getEntry());
        }

    }

    private final static String MIC_CTL = "/sys/class/ak/source/mic_ctrl";

    private void setMicType(Preference p, String value) {
        ListPreference lp = (ListPreference) p;

        lp.setValue(value);
        lp.setSummary(lp.getEntry());
        int v = 1;
        try {
            v = Integer.valueOf(value);
            SystemConfig.setIntProperty(getActivity(), SystemConfig.KEY_MIC_TYPE, v);

        } catch (Exception e) {

        }
        Util.setFileValue(MIC_CTL, v);

        Log.d(TAG, MIC_CTL + ":setMicType:" + v);
    }

    private void updateCarSreenSaver() {
        Preference p = findPreference(KEY_CAR_INFO_SCREENSAVER);
        int value = SystemConfig.getIntProperty(getActivity(), SystemConfig.KEY_SCREEN_SAVE_STYLE);
        if (value != 0) {
            value = Settings.System.getInt(getActivity().getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, Integer.MAX_VALUE);
        } else {
            value = Integer.MAX_VALUE;
        }

        if (p != null) {
            ListPreference lp = (ListPreference) p;

            lp.setValue(value + "");
            lp.setSummary(lp.getEntry());
        }

    }

    private void setCarSreenSaver(Preference p, String value) {
        ListPreference lp = (ListPreference) p;

        lp.setValue(value);
        lp.setSummary(lp.getEntry());
        int v = -1;
        try {
            v = Integer.parseInt(value);
            if (v != Integer.MAX_VALUE) {
                SystemConfig.setIntProperty(getActivity(), SystemConfig.KEY_SCREEN_SAVE_STYLE, 1);
            } else {
                SystemConfig.setIntProperty(getActivity(), SystemConfig.KEY_SCREEN_SAVE_STYLE, 0);
            }

            Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
            it.putExtra(MyCmd.EXTRA_COMMON_CMD, SystemConfig.KEY_SCREEN_SAVE_STYLE);
            it.putExtra(MyCmd.EXTRA_COMMON_DATA, value);
            mActivity.sendBroadcast(it);

            BroadcastUtil.sendToCarService(getActivity(), MyCmd.Cmd.SET_SPECTRUM_SCREEN_SAVE, v);
        } catch (Exception ignored) {
        }
    }

    private void setLauncherUI(Preference p, String value) {

        String old = SystemConfig.getProperty(getActivity(), SystemConfig.KEY_LAUNCHER_UI_RM10);
        Log.d("fkk", "ffffffffffffffff");
        if (!value.equals(old)) {
            SystemConfig.setIntProperty(getActivity(), SystemConfig.KEY_LAUNCHER_UI_RM10_WORKSPACE_RELOAD, 1);
            SystemConfig.setProperty(getActivity(), SystemConfig.KEY_LAUNCHER_UI_RM10, value);
            ListPreference lp = (ListPreference) p;

            lp.setValue(value);
            lp.setSummary(lp.getEntry());

            Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
            it.putExtra(MyCmd.EXTRA_COMMON_CMD, SystemConfig.KEY_LAUNCHER_UI_RM10);
            it.putExtra(MyCmd.EXTRA_COMMON_DATA, value);
            mActivity.sendBroadcast(it);

            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Kernel.doKeyEvent(Kernel.KEY_HOMEPAGE);
                }
            }, 600);
        }
    }

    private void replaceFragment(int layoutId, PreferenceFragment fragment, boolean isAddStack) {
        if (fragment != null) {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.replace(layoutId, fragment);
            if (isAddStack) {
                fragmentTransaction.addToBackStack(null);
            }
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onPause() {
        mSettingControllor.removeGeneralSettingListener(mGeneralSettingListener);
        mSeaNavigationChoice.setNavigationChangeListener(null);

        mupdateNaviChoice = false;
        unregisterMountListener();
        super.onPause();
        Util.sudoExecNoCheck("sync");
    }

    @Override
    public void onResume() {
        super.onResume();

        if (GeneralSettings.mSettingType == 1 && getTouch3ConfigValue(getActivity())) {
            hideFinger3Settings();
        }

        mSeaNavigationChoice.setNavigationChangeListener(mNavigationChangeListener);
        String appName = mSeaNavigationChoice.getChoosedAppName();
        if (!appName.isEmpty()) {
            mSeaNavigationChoice.setSummary(appName);
        }
        mSettingControllor.addGeneralSettingListener(mGeneralSettingListener);
        mIsThirdAppSoundFirst = MachineConfig.VALUE_ON.equals(MachineConfig.getProperty(MachineConfig.KEY_THIRD_APP_SOUND_FIRST));
        mHandler.sendEmptyMessage(MSG_UPDATE_THIRD_APP_SOUND_FIRST);
        mNavigationPackageName = mSettingsDataHelper.getNavigationPackageName();
        mNavigationClazzName = mSettingsDataHelper.getNavigationClazzName();
        mHandler.sendEmptyMessage(MSG_UPDATE_NAVIGATION_PACKAGE);

        updateBrakeSetting(getBrakeSetting());
        updateBeepSetting(getBeepSetting());
        updateNaviMix(getNaviMix());

        updateReverseVolume(getRevserseVolume());
        updateAccPoweroffDelay();

        updateNissan360System();
        try {
            mStaticTrackExist = Settings.Global.getInt(mActivity.getContentResolver(), SystemConfig.REVERSE_STATIC_TRACK);
        } catch (SettingNotFoundException snfe) {

        }
        try {
            mDyncTrackExist = Settings.Global.getInt(mActivity.getContentResolver(), SystemConfig.REVERSE_DYNC_TRACK);
        } catch (SettingNotFoundException snfe) {

        }
        try {
            mReverseMirrorValue = Settings.Global.getInt(mActivity.getContentResolver(), SystemConfig.MIRROR_PREVIEW);
        } catch (SettingNotFoundException snfe) {
        }

        if (mTrackStatic != null) {
            mTrackStatic.setChecked(mStaticTrackExist == 1);
        }
        if (mTrackDynamic != null) {
            mTrackDynamic.setChecked(mDyncTrackExist == 1);
        }
        if (mReverseMirror != null) {
            mReverseMirror.setChecked(mReverseMirrorValue == 1);
        }

        try {
            mAutoPlayMusic = Settings.Global.getInt(mActivity.getContentResolver(), SystemConfig.AUTO_PLAY_MUSIC_DEVICES_MOUNTED);
        } catch (SettingNotFoundException ignored) {
        }

        if (mPlayMusic != null) {
            mPlayMusic.setChecked(mAutoPlayMusic == 1);
        }

        if (mDvrPath != null) {
            updateStoragePath();
            registerExternalStorageListener();
        }

        Preference p = findPreference(KEY_LED);
        if (p != null) {
            int ledType = MachineConfig.getPropertyIntOnce(MachineConfig.KEY_LED_TYPE);
            if (ledType != 0 && ledType != 2) {
                getPreferenceScreen().removePreference(p);
            }
        }

        // customUI();

        updateTempUnit();
        updateMileageUnit();
        // if ("1".equals(MachineConfig
        // .getPropertyOnce(MachineConfig.KEY_OTG_TEST))) {
        // findPreference(KEY_LED).setOnPreferenceClickListener(this);
        // mScrennSizePreference.setOnPreferenceChangeListener(this);
        // } else {
        // getPreferenceScreen().removePreference(mScrennSizePreference);
        // mScrennSizePreference = null;
        // }
        loadAdStd();

        Util.sudoExecNoCheck("sync");
    }

    int mStaticTrackExist = 0;
    int mDyncTrackExist = 1;
    int mReverseMirrorValue = 0;
    int mAutoPlayMusic = 1;
    int mAutoDark = 0;

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        MMLog.d(TAG,"onPreferenceChange key="+key+",newValue="+newValue);
        if (KEY_THIRD_APP_SOUND_FIRST.equals(key)) {// 缂佹鍏涚粭渚�寮悷甯矗闂傚﹤鍘栫槐顓㈠礂閿燂拷
            // boolean mIsThirdAppSoundFirst = (Boolean) newValue;
            // MachineConfig.setProperty(MachineConfig.KEY_THIRD_APP_SOUND_FIRST,
            // mIsThirdAppSoundFirst ? MachineConfig.VALUE_ON
            // : MachineConfig.VALUE_OFF);
            // Util.setFileValue(MCU_ARM_SOUND_SW_FILE, mIsThirdAppSoundFirst ?
            // 1
            // : 0);
            // if (mThirdAppSoundFirstCheckbox != null) {
            // mThirdAppSoundFirstCheckbox.setChecked(mIsThirdAppSoundFirst);
            // }
        } else if (KEY_VIDEO_ON_DRIVING.equals(key)) {
            boolean allowed = (Boolean) newValue;
            int brake = allowed ? 0 : 1;
            updateBrakeSetting(brake);
            setBrakeSetting(brake);
        } else if (KEY_VIDEO_ON_GPS.equals(key)) {
            boolean allowed = (Boolean) newValue;
            int brake = allowed ? 1 : 0;
            updateBrakeGPSSetting(brake);
            setBrakeGPSSetting(brake);
        } else if (KEY_AUTO_ILL.equals(key)) {
            mAutoIllEn = ((Boolean) newValue) ? 1 : 0;
            Settings.Global.putInt(mActivity.getContentResolver(), AUTO_ILL_ENABLE, mAutoIllEn);
            if (0 != mAutoIllEn) {
                Settings.Global.putInt(mActivity.getContentResolver(), AUTO_ILL_START_HOUR, mIllStartHour);
                Settings.Global.putInt(mActivity.getContentResolver(), AUTO_ILL_START_MINUTE, mIllStartMinute);
                Settings.Global.putInt(mActivity.getContentResolver(), AUTO_ILL_STOP_HOUR, mIllStopHour);
                Settings.Global.putInt(mActivity.getContentResolver(), AUTO_ILL_STOP_MINUTE, mIllStopMinute);
            }
            updateAutoIll();
        } else if (KEY_CARCELL.equals(key)) {
            mCarCellEn = ((Boolean) newValue) ? "1" : "0";
            SystemConfig.setProperty(mActivity, SystemConfig.KEY_CAR_CELL, mCarCellEn);
            updateCarCell();
            try {
                Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
                it.putExtra(MyCmd.EXTRA_COMMON_CMD, SystemConfig.KEY_CAR_CELL);
                mActivity.sendBroadcast(it);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (KEY_BTCELL.equals(key)) {
            mBTCellEn = ((Boolean) newValue) ? "1" : "0";
            SystemConfig.setProperty(mActivity, SystemConfig.KEY_BT_CELL, mBTCellEn);
            updateBTCell();
            try {
                Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
                it.putExtra(MyCmd.EXTRA_COMMON_CMD, SystemConfig.KEY_BT_CELL);
                mActivity.sendBroadcast(it);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (KEY_VIDEO_OUT.equals(key)) {
            mVideoOutEn = ((Boolean) newValue) ? "1" : "0";
            SystemConfig.setProperty(mActivity, SystemConfig.KEY_VIDEO_OUT, (Boolean) newValue ? mVideoOutEn : null);
            updateVideoOut();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Util.sudoExec("sync");
                    Util.doSleep(1000);
                    //Util.mcuReboot();
                    Util.sudoExec("reboot");
                }
            }, 500);
        } else if (KEY_TOUCH_SOUND.equals(key)) {
            int en = ((Boolean) newValue) ? 1 : 0;
            updateBeepSetting(en);
            setBeepSetting(en);
        } else if (KEY_NAVI_MIX.equals(key)) {
            int mix = Integer.parseInt((String) newValue);
            updateNaviMix(mix);
            setNaviMix(mix);
        } else if (KEY_NAVI_AUTO_DISPAUX.equals(key)) {
            updateNaviAutoDispAux((Boolean) newValue);
        } else if (KEY_REVERSE_VOLUME.equals(key)) {
            int mix = Integer.parseInt((String) newValue);
            updateReverseVolume(mix);
            setRevserseVolume(mix);
        } else if (KEY_ACC_DELAY_POWEROFF.equals(key)) {
            setAccPoweroffDelay((String) newValue);
        } else if (KEY_TEMP_DIS.equals(key)) {
            setTempUnit((String) newValue);
        } else if (KEY_MILEAGE_UNIT.equals(key)) {
            setMileage((String) newValue);
        } else if (KEY_LAUNCHER_UI.equals(key)) {
            setLauncherUI(preference, (String) newValue);
        } else if (KEY_RESET_VOLUME.equals(key)) {
            setResetVolume(preference, (String) newValue);
        } else if (KEY_CAR_INFO_SCREENSAVER.equals(key)) {
            setCarSreenSaver(preference, (String) newValue);
        } else if (KEY_MIC_TYPE.equals(key)) {
            setMicType(preference, (String) newValue);
        } else if (KEY_DVR.equals(key)) {

            updateDVR((String) newValue);
        } else if (KEY_STATIC_TRACK_BACK.equals(key)) {
            boolean b = (Boolean) newValue;
            Settings.Global.putInt(mActivity.getContentResolver(), SystemConfig.REVERSE_STATIC_TRACK, b ? 1 : 0);
            mTrackStatic.setChecked(b);
        } else if (KEY_DYNAMIC_TRACK_BACK.equals(key)) {
            boolean b = (Boolean) newValue;
            Settings.Global.putInt(mActivity.getContentResolver(), SystemConfig.REVERSE_DYNC_TRACK, b ? 1 : 0);

            mTrackDynamic.setChecked(b);
        } else if (KEY_REVERSE_MIRROR.equals(key)) {
            boolean b = (Boolean) newValue;
            Settings.Global.putInt(mActivity.getContentResolver(), SystemConfig.MIRROR_PREVIEW, b ? 1 : 0);
            mReverseMirror.setChecked(b);
        } else if (KEY_DOOR_VOICE.equals(key)) {
            boolean b = (Boolean) newValue;
            Settings.Global.putInt(mActivity.getContentResolver(), SystemConfig.CANBOX_DOOR_VOICE, b ? 1 : 0);
            Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
            it.putExtra(MyCmd.EXTRA_COMMON_CMD, SystemConfig.CANBOX_DOOR_VOICE);
            it.putExtra(MyCmd.EXTRA_COMMON_DATA, b ? 1 : 0);
            mActivity.sendBroadcast(it);
            mDoorVoice.setChecked(b);
        } else if (KEY_FRONT_RADAR_CAMERA.equals(key)) {
            boolean b = (Boolean) newValue;
            Settings.Global.putInt(mActivity.getContentResolver(), SystemConfig.CANBOX_FRONT_RADAR_OPEN_CAMERA, b ? 1 : 0);

            Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
            it.putExtra(MyCmd.EXTRA_COMMON_CMD, SystemConfig.CANBOX_FRONT_RADAR_OPEN_CAMERA);
            it.putExtra(MyCmd.EXTRA_COMMON_DATA, b ? 1 : 0);
            mActivity.sendBroadcast(it);
            mRadarCamera.setChecked(b);
        } else if (KEYPLAY_MUSIC.equals(key)) {
            boolean b = (Boolean) newValue;
            Settings.Global.putInt(mActivity.getContentResolver(), SystemConfig.AUTO_PLAY_MUSIC_DEVICES_MOUNTED, b ? 1 : 0);

            mPlayMusic.setChecked(b);
        } else if (KEY_SWITCH_DARK_MODE.equals(key)) {
            boolean b = (Boolean) newValue;
            SystemConfig.setIntProperty(mActivity, SystemConfig.KEY_DARK_MODE_SWITCH, b ? 1 : 0);

            mSwitchDark.setChecked(b);
            Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
            it.putExtra(MyCmd.EXTRA_COMMON_CMD, SystemConfig.KEY_DARK_MODE_SWITCH);
            getActivity().sendBroadcast(it);
        } else if (KEY_NISSIAN_360_SYSTEM.equals(key)) {
            boolean b = (Boolean) newValue;

            SystemConfig.setIntProperty(mActivity, SystemConfig.KEY_NISSIAN_360_SYSTEM, b ? 1 : 0);

            ((SwitchPreference) preference).setChecked(b);

            final byte[] buf = new byte[]{0x20, 0x02, (byte) 0x60, 0x0};
            if (b) {
                buf[2] = (byte) 0x61;
                buf[3] = (byte) 0x1;
            }
            for (int i = 0; i < 3; ++i) {
                BroadcastUtil.sendCanboxInfo(mActivity, buf);
                Util.doSleep(20);
            }
        } else if (KEY_NISSIAN_360_SYSTEM_SHOW_BUTTON.equals(key)) {

            boolean b = (Boolean) newValue;
            SystemConfig.setIntProperty(mActivity, SystemConfig.KEY_NISSIAN_360_SYSTEM_BUTTON, b ? 1 : 0);

            ((SwitchPreference) preference).setChecked(b);

            Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
            it.putExtra(MyCmd.EXTRA_COMMON_CMD, SystemConfig.KEY_NISSIAN_360_SYSTEM_BUTTON);
            it.putExtra(MyCmd.EXTRA_COMMON_DATA, b);
            mActivity.sendBroadcast(it);
        } else if (KEY_TOUCH3_SWITCH.equals(key)) {
            updateTouch3SwitchPreference((Boolean) newValue);
            mTouch3Info.saveConfigJSON(false, getActivity(), mTouch3Switch.isChecked(), mTouch3UpKey.getValue(), mTouch3DownKey.getValue(), mTouch3LeftKey.getValue(), mTouch3RightKey.getValue(), mTouch3UpKey.getEntry().toString(), mTouch3DownKey.getEntry().toString(), mTouch3LeftKey.getEntry().toString(), mTouch3RightKey.getEntry().toString());
            if (mTouch3Switch.isChecked()) {
                Finger3HelpFloatView.getInstanse(getActivity()).show();
            }
        } else if (KEY_TOUCH3_UPKEY.equals(key)) {
            updateTouch3Preference(mTouch3UpKey, (String) newValue, true);
        } else if (KEY_TOUCH3_DOWNKEY.equals(key)) {
            updateTouch3Preference(mTouch3DownKey, (String) newValue, true);
        } else if (KEY_TOUCH3_LEFTKEY.equals(key)) {
            updateTouch3Preference(mTouch3LeftKey, (String) newValue, true);
        } else if (KEY_TOUCH3_RIGHTKEY.equals(key)) {
            updateTouch3Preference(mTouch3RightKey, (String) newValue, true);
            //} else if (KEY_AD_STD_ENABLED.equals(key)) {
            //	onAdStdChanged(0, (Boolean) newValue ? "1" : "0");
        } else if (KEY_AD_STD_CH0.equals(key)) {
            onAdStdChanged(1, (String) newValue);
        } else if (KEY_AD_STD_CH1.equals(key)) {
            onAdStdChanged(2, (String) newValue);
        } else if (KEY_AD_STD_CH2.equals(key)) {
            onAdStdChanged(3, (String) newValue);
        } else if (KEY_AD_STD_CH3.equals(key)) {
            onAdStdChanged(4, (String) newValue);
        }
        return false;
    }

    private NavigationChangeListener mNavigationChangeListener = new NavigationChangeListener() {

        @Override
        public void notifyNavigationChange(String appName, String pkgName, String clzName) {
            if (mSeaNavigationChoice != null) {
                mSeaNavigationChoice.setSummary(appName);
            }
        }
    };

    // mcu閺夆晜鏌ㄥú鏍磹閿燂拷
    private GeneralSettingListener mGeneralSettingListener = new GeneralSettingListener() {

        @Override
        public void notifyLedColorChange(byte color) {
        }

        @Override
        public void notifyBrakeDetection(byte detectionType) {
        }

        @Override
        public void notifyBrightness(byte brightness) {
            // TODO Auto-generated method stub

        }

        @Override
        public void notifyBuzzer(boolean isOpen) {
        }

        @Override
        public void notifyLightControlModel(byte controlModel) {
            // TODO Auto-generated method stub

        }

        @Override
        public void notifyLightDetection(byte detectionType) {
            // TODO Auto-generated method stub

        }

        @Override
        public void notifyNaviVoiceChannelType(byte channel) {
        }

        @Override
        public void notifyReverseMute(boolean isMute) {
        }

    };

    public boolean onPreferenceClick(Preference arg0) {
        if (arg0.getKey().equals(KEY_RESET_SYSTEM)) {
            new AlertDialog.Builder(getActivity()).setIcon(android.R.drawable.ic_dialog_alert).setTitle(getString(R.string.sure_reset_system)).setMessage(R.string.reset_system_message).setPositiveButton(R.string.dlg_ok, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Util.sudoExec("rm:-r:/data/");

                    mHandler.removeMessages(MSG_CHECK_RM_DATA);
                    mHandler.sendEmptyMessageDelayed(MSG_CHECK_RM_DATA, 1000);

                    Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
                    it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.EXTRA_COMMON_CMD);
                    getActivity().sendBroadcast(it);// for some app
                    // reset itself

                    AlertDialog ad = new AlertDialog.Builder(mActivity).create();
                    LinearLayout container = new LinearLayout(mActivity);
                    container.setOrientation(LinearLayout.VERTICAL);
                    container.setGravity(Gravity.CENTER);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 20, 0, 40);
                    final ProgressBar pb = new ProgressBar(mActivity);
                    container.addView(pb, params);
                    ad.setIcon(R.drawable.dialog_alert_icon);
                    ad.setTitle(R.string.reboot_dialog_title);
                    ad.setView(container);
                    ad.setCancelable(false);
                    ad.show();
                }
            }).setNegativeButton(R.string.dlg_cancel, null).show();
        } else if (arg0.getKey().equals(KEY_LED)) {
            replaceFragment(R.id.id_genernal_setting_fragment, mLedFragment, true);
        } else if (arg0.getKey().equals(KEY_ILL_START_TIME)) {
            mIllStartTimePicker.show();
        } else if (arg0.getKey().equals(KEY_ILL_STOP_TIME)) {
            mIllStopTimePicker.show();
        } else if (arg0.getKey().equals(KEY_UPDATE_ANDROID)) {
            mAndroidUpdatePath = getAndroidImagePath();
            if ((null == mAndroidUpdatePath) || (mAndroidUpdatePath.isEmpty())) {
                if (mToast != null) {
                    mToast.cancel();
                }
                mToast = Toast.makeText(getActivity(), R.string.no_valid_android_image, Toast.LENGTH_SHORT);
                mToast.show();

                mCountShowFactory++;
                if (mCountShowFactory >= 15) {
                    mCountShowFactory = 0;
                    showFactory();
                }

            } else {
                mCountShowFactory = 0;
                AlertDialog ad = new AlertDialog.Builder(mActivity).setIcon(R.drawable.dialog_alert_icon).setTitle(R.string.update_android_title).setMessage(String.format(getString(R.string.update_android_message), mAndroidUpdatePath)).setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        doAndroidUpdate(mAndroidUpdatePath);
                    }
                }).setNegativeButton(R.string.alert_dialog_cancel, null).create();
                ;
                ad.show();
            }
        } else if (arg0.getKey().equals(KEY_AD_STD_APPLY)) {
            saveADStd();
        }

        return false;
    }

    private int mCountShowFactory;
    private Toast mToast;

    private void showFactory() {
        Intent it = new Intent("com.my.factory.intent.action.FactorySettings");
        getActivity().startActivity(it);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_THIRD_APP_SOUND_FIRST:
                    // if (mThirdAppSoundFirstCheckbox != null) {
                    // mThirdAppSoundFirstCheckbox
                    // .setChecked(mIsThirdAppSoundFirst);
                    // }
                    break;
                case MSG_UPDATE_NAVIGATION_PACKAGE:
                    if (mSeaNavigationChoice != null && mNavigationPackageName != null && mNavigationClazzName != null) {
                        mSeaNavigationChoice.setPackageName(mNavigationPackageName, mNavigationClazzName);
                    }
                    break;

                case MSG_CHECK_RM_DATA:
                    if (ShellUtils.getAkdRunStatus()) {
                        mHandler.removeMessages(MSG_CHECK_RM_DATA);
                        mHandler.sendEmptyMessageDelayed(MSG_CHECK_RM_DATA, 500);
                    } else {
                        Util.sudoExec("sync");
                        mHandler.removeMessages(MSG_CHECK_RM_DATA);
                        mHandler.sendEmptyMessageDelayed(MSG_CHECK_SYNC, 500);
                    }
                    break;
                case MSG_CHECK_SYNC:
                    if (ShellUtils.getAkdRunStatus()) {
                        mHandler.removeMessages(MSG_CHECK_RM_DATA);
                        mHandler.sendEmptyMessageDelayed(MSG_CHECK_SYNC, 100);
                    } else {
                        Util.setFileValue(MCU_RECOVERY_FILE, new byte[]{0x55, (byte) 0xaa, 0x00});
                    }
                    break;
                default:
                    break;
            }
        }

        ;
    };

    private int getBrakeSetting() {
        return Util.getFileValue(MCU_BRAKE_DET_NODE);
    }

    private void setBrakeSetting(int brake) {
        Util.setFileValue(MCU_BRAKE_DET_NODE, brake);
    }

    private void updateBrakeSetting(int brake) {
        if (brake != 0) {
            mVideoOnDrivingPreference.setChecked(false);
        } else {
            mVideoOnDrivingPreference.setChecked(true);
        }
    }

    private void setBrakeGPSSetting(int brake) {
        SystemConfig.setIntProperty(mActivity, SystemConfig.GPS_BRAKE, brake);

        Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, SystemConfig.GPS_BRAKE);
        it.putExtra(MyCmd.EXTRA_COMMON_DATA, brake);
        mActivity.sendBroadcast(it);
    }

    private void updateBrakeGPSSetting(int brake) {
        if (brake != 0) {
            mVideoOnStopGPSPreference.setChecked(true);
        } else {
            mVideoOnStopGPSPreference.setChecked(false);
        }
    }

    private int getLedSetting() {
        return Util.getFileValue(MCU_LED_COLOR_NODE);
    }

    private void updateCarCell() {
        if (mCarCellEn != null && (mCarCellEn.equals("1") || mCarCellEn.equals("true"))) mCarCellPref.setChecked(true);
        else mCarCellPref.setChecked(false);
    }

    private void updateBTCell() {
        if (mBTCellEn != null && (mBTCellEn.equals("1") || mBTCellEn.equals("true"))) mBTCellPref.setChecked(true);
        else mBTCellPref.setChecked(false);
    }

    private void updateVideoOut() {
        boolean b = "1".equals(mVideoOutEn) || "true".equals(mVideoOutEn);
        mVideoOutPref.setChecked(b ? true : false);
        mVideoOutPref.setSummary(b ? getActivity().getResources().getString(R.string.on) : getActivity().getResources().getString(R.string.off));
    }

    private void updateAutoIll() {
        mIllStartTimePicker.updateTime(mIllStartHour, mIllStartMinute);
        mIllStartTimePref.setSummary(DateFormat.getTimeFormat(mActivity).format(new Date(2017, 1, 6, mIllStartHour, mIllStartMinute)));
        mIllStopTimePicker.updateTime(mIllStopHour, mIllStopMinute);
        mIllStopTimePref.setSummary(DateFormat.getTimeFormat(mActivity).format(new Date(2017, 1, 6, mIllStopHour, mIllStopMinute)));

        if (0 == mAutoIllEn) {
            mAutoIllPref.setChecked(false);
            if (mIllStartTimePref.isEnabled()) {
                mIllStartTimePref.setEnabled(false);
                getPreferenceScreen().removePreference(mIllStartTimePref);
            }
            if (mIllStopTimePref.isEnabled()) {
                mIllStopTimePref.setEnabled(false);
                getPreferenceScreen().removePreference(mIllStopTimePref);
            }
        } else {
            mAutoIllPref.setChecked(true);
            if (!mIllStartTimePref.isEnabled()) {
                mIllStartTimePref.setEnabled(true);
                getPreferenceScreen().addPreference(mIllStartTimePref);
            }
            if (!mIllStopTimePref.isEnabled()) {
                mIllStopTimePref.setEnabled(true);
                getPreferenceScreen().addPreference(mIllStopTimePref);
            }
        }

        if ((mIllStartHour == mIllStopHour) && (mIllStartMinute == mIllStopMinute)) {
            Toast.makeText(mActivity, R.string.auto_ill_error_same, Toast.LENGTH_LONG).show();
        }

    }

    private int getBeepSetting() {
        return Util.getFileValue(MCU_BEEP_NODE);
    }

    private void setBeepSetting(int en) {
        Util.setFileValue(MCU_BEEP_NODE, en);
    }

    private void updateBeepSetting(int en) {
        mTouchSoundPreference.setChecked((1 == en));
    }

    private int getNaviMix() {
        return Util.getFileValue(MCU_NAVI_MIX_NODE);
    }

    private void setNaviMix(int mix) {
        Util.setFileValue(MCU_NAVI_MIX_NODE, mix);
        SystemConfig.setIntProperty(mActivity, SystemConfig.KEY_NAVI_MIX_SOUND, mix);
    }

    private void updateNaviMix(int mix) {
        if (mNaviMixPreference.findIndexOfValue(String.valueOf(mix)) < 0) {
            return;
        }
        mNaviMixPreference.setValue(String.valueOf(mix));
        mNaviMixPreference.setSummary(mNaviMixPreference.getEntry());
    }

    private void updateNaviAutoDispAux(boolean b) {
        mNaviAutoDispAuxPreference.setChecked(b);
        SystemConfig.setProperty(getActivity(), MachineConfig.KEY_DISPAUX_APPAUTO_DISABLE, b ? "1" : "0");
    }

    private void setRevserseVolume(int mix) {
        Util.setFileValue(MCU_REVERSE_VOLUME, mix);
        SystemConfig.setIntProperty(mActivity, SystemConfig.KEY_REVERSE_VOLUME, mix);
    }

    private int getRevserseVolume() {
        return Util.getFileValue(MCU_REVERSE_VOLUME);
    }

    private void updateReverseVolume(int mix) {
        if (mRevserVolume.findIndexOfValue(String.valueOf(mix)) < 0) {
            return;
        }
        mRevserVolume.setValue(String.valueOf(mix));
        String s = mRevserVolume.getEntry().toString();

        if (mix > 0 && mix < 100) {
            s = "" + mix;
        }
        mRevserVolume.setSummary(s);
    }

    private void setAccPoweroffDelay(String value) {
        SystemConfig.setProperty(mActivity, MachineConfig.KEY_ACC_DELAY_OFF, value);

        Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, MachineConfig.KEY_ACC_DELAY_OFF);
        it.putExtra(MyCmd.EXTRA_COMMON_DATA, value);
        mActivity.sendBroadcast(it);

        mAccDelayPoweroff.setValue(value);
        mAccDelayPoweroff.setSummary(mAccDelayPoweroff.getEntry());

    }

    private void setTempUnit(String value) {
        Log.d("fkc", "setTempUnit:" + value);
        SystemConfig.setProperty(mActivity, SystemConfig.CANBOX_TEMP_UNIT, value);

        Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, SystemConfig.CANBOX_TEMP_UNIT);
        it.putExtra(MyCmd.EXTRA_COMMON_DATA, value);
        mActivity.sendBroadcast(it);

        mTempDis.setValue(value);
        mTempDis.setSummary(mTempDis.getEntry());

    }

    private void setMileage(String value) {
        SystemConfig.setProperty(mActivity, SystemConfig.CANBOX_MILEAGE_UNIT, value);

        Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, SystemConfig.CANBOX_MILEAGE_UNIT);
        it.putExtra(MyCmd.EXTRA_COMMON_DATA, value);
        mActivity.sendBroadcast(it);

        mMileageUnit.setValue(value);
        mMileageUnit.setSummary(mMileageUnit.getEntry());

    }

    private String getAccPoweroffDelay() {
        return SystemConfig.getProperty(mActivity, MachineConfig.KEY_ACC_DELAY_OFF);
    }

    private void updateAccPoweroffDelay() {
        String value = getAccPoweroffDelay();
        if (value == null) {
            value = "0";
        }
        mAccDelayPoweroff.setValue(value);
        mAccDelayPoweroff.setSummary(mAccDelayPoweroff.getEntry());
    }

    private void updateTempUnit() {
        if (mTempDis == null) {
            return;
        }
        String value = SystemConfig.getProperty(mActivity, SystemConfig.CANBOX_TEMP_UNIT);
        if (value == null) {
            value = "0";
        }
        mTempDis.setValue(value);
        mTempDis.setSummary(mTempDis.getEntry());
    }

    private void updateMileageUnit() {
        if (mMileageUnit == null) {
            return;
        }
        String value = SystemConfig.getProperty(mActivity, SystemConfig.CANBOX_MILEAGE_UNIT);
        if (value == null) {
            value = "0";
        }
        mMileageUnit.setValue(value);
        mMileageUnit.setSummary(mMileageUnit.getEntry());
    }

    private void updateNissan360System() {

        Preference preference = findPreference(KEY_NISSIAN_360_SYSTEM);
        if (preference != null) {

            int i = SystemConfig.getIntProperty(mActivity, SystemConfig.KEY_NISSIAN_360_SYSTEM);
            ((SwitchPreference) preference).setChecked(i != 0);

        }

        preference = findPreference(KEY_NISSIAN_360_SYSTEM_SHOW_BUTTON);
        if (preference != null) {
            int i = SystemConfig.getIntProperty(mActivity, SystemConfig.KEY_NISSIAN_360_SYSTEM_BUTTON);
            ((SwitchPreference) preference).setChecked(i != 0);
        }
    }

    private void updateDVR(String value) {

        MachineConfig.setProperty(MachineConfig.KEY_DVR_PATH, value);
        mActivity.sendBroadcast(new Intent(MyCmd.BROADCAST_DVR_PATH_UPDATE));
        mDvrPath.setSummary(value);
        mDvrPath.setValue(value);
    }


    private boolean supportADStd() {
        if ("1".equals(MachineConfig.getPropertyReadOnly(MachineConfig.KEY_SUPPORT_FIXED_AD_STD))) return true;
        else return false;
    }

    private void loadAdStd() {
        try {
            String str = MachineConfig.getProperty(MachineConfig.KEY_AD_STD);
            if (str != null && !str.isEmpty()) {
                String[] s = str.split(",");
                if (s != null && s.length == 5) {
                    for (int i = 0; i < 5; i++) {
                        adstd[i] = Integer.valueOf(s[i]);
                    }
                }
            }
            updateAdStdUi();
        } catch (Exception e) {
            Log.e(TAG, "onAdStdChanged failed: " + e);
            e.printStackTrace();
        }
    }

    private void updateAdStdUi() {
        adstd[0] = 1;
        //mADStdSwitch.setChecked(adstd[0] == 1 ? true : false);
        //mADStdSwitch.setSummary(adstd[0] == 1 ? getActivity().getResources().getString(R.string.on) : getActivity().getResources().getString(R.string.off));
        mADStdCh0.setValue("" + adstd[1]);
        mADStdCh0.setSummary(mADStdCh0.getEntry());
        mADStdCh1.setValue("" + adstd[2]);
        mADStdCh1.setSummary(mADStdCh1.getEntry());
        mADStdCh2.setValue("" + adstd[3]);
        mADStdCh2.setSummary(mADStdCh2.getEntry());
        mADStdCh3.setValue("" + adstd[4]);
        mADStdCh3.setSummary(mADStdCh3.getEntry());
    }

    private void onAdStdChanged(int index, String val) {
        if (index >= 0 && index < 5) {
            int v = Integer.valueOf(val);
            adstd[index] = v;

            updateAdStdUi();
        }
    }

    private void saveADStd() {
        String str = String.format(Locale.ENGLISH, "%d,%d,%d,%d,%d", adstd[0], adstd[1], adstd[2], adstd[3], adstd[4]);
        MachineConfig.setProperty(MachineConfig.KEY_AD_STD, str);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Util.mcuReboot();
            }
        }, 1000);
    }

    public final static String PATH_DVR_INTERNAL = "/storage/sdcard0";

    private void updateStoragePath() {

        String[] result = null;
        StorageManager storageManager = (StorageManager) this.mActivity.getSystemService(Context.STORAGE_SERVICE);
        try {
            Method method = StorageManager.class.getMethod("getVolumePaths");
            method.setAccessible(true);
            try {
                result = (String[]) method.invoke(storageManager);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < result.length; i++) {
                System.out.println("path----> " + result[i] + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        int num = 0;
        int i;
        boolean[] mount = new boolean[result.length];
        for (i = 0; i < result.length; ++i) {
            mount[i] = checkSDCard(result[i]);
            if (mount[i]) {
                num++;
            }
        }
        int select = 0;
        result[0] = PATH_DVR_INTERNAL;
        String cur_path = MachineConfig.getProperty(MachineConfig.KEY_DVR_PATH);

        if (num > 0) {
            String[] disk = new String[num];

            int j = 0;
            for (i = 0; i < result.length; ++i) {
                if (mount[i]) {
                    if (j < disk.length) {
                        disk[j] = result[i] + "/";

                        if (disk[j].equals(cur_path)) {
                            select = j;
                        }

                        ++j;

                    }

                }
            }

            mDvrPath.setEntries(disk);
            mDvrPath.setEntryValues(disk);
        }

        if (cur_path == null) {
            cur_path = PATH_DVR_INTERNAL + "/";
        }
        mDvrPath.setValue(cur_path);
        mDvrPath.setSummary(cur_path);
    }

    public static boolean checkSDCard(String path) {
        boolean ret = false;
        try {
            File sdcardDir = new File(path);
            if (sdcardDir.exists()) {
                String state = Environment.getExternalStorageState(sdcardDir);
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    ret = true;
                }
            }
        } catch (Exception e) {

        }
        return ret;
    }

    private static List<StorageInfo> listAllStorage(Context context) {
        ArrayList<StorageInfo> storages = new ArrayList<StorageInfo>();
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            Class<?>[] paramClasses = {};
            Method getVolumeList = StorageManager.class.getMethod("getVolumes", paramClasses);
            Object[] params = {};
            List<Object> VolumeInfo = (List<Object>) getVolumeList.invoke(storageManager, params);

            if (VolumeInfo != null) {
                for (Object volumeinfo : VolumeInfo) {

                    Method getPath = volumeinfo.getClass().getMethod("getPath", new Class[0]);

                    File path = (File) getPath.invoke(volumeinfo, new Object[0]);

                    Method getDisk = volumeinfo.getClass().getMethod("getDisk", new Class[0]);

                    Object diskinfo = getDisk.invoke(volumeinfo, new Object[0]);
                    int type = StorageInfo.TYPE_INTERAL;
                    if (diskinfo != null) {
                        Method isSd = diskinfo.getClass().getMethod("isSd", new Class[0]);

                        type = ((Boolean) isSd.invoke(diskinfo, new Object[0])) ? StorageInfo.TYPE_SD : StorageInfo.TYPE_USB;

                    }
                    StorageInfo si = new StorageInfo(path.toString(), type);
                    storages.add(si);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        storages.trimToSize();
        return storages;
    }

    private String getAndroidImagePath71() {
        List<StorageInfo> list = listAllStorage(getActivity());
        String path = null;
        for (int i = 0; i < list.size(); ++i) {

            StorageInfo si = list.get(i);
            // if (si.mType == StorageInfo.TYPE_SD) {
            //
            // } else if (si.mType == StorageInfo.TYPE_USB) {
            File f;
            if (Util.isRKSystem()) {
                if (!si.mPath.contains("USBdisk")) {
                    continue;
                }
                f = new File(si.mPath + "/kupdate.zip");
            } else {
                f = new File(si.mPath + "/" + ANDROID_UPDATE_GUIDE_FILE_NAME);
            }

            if (f.exists()) {
                path = si.mPath;
                break;
            }
            // }
        }
        return path;
    }

    private String getAndroidImagePath() {
        if (Build.VERSION.SDK_INT >= 25) {
            return getAndroidImagePath71();
        } else {
            return getAndroidImagePath51();
        }
    }

    private String getAndroidImagePath51() {
        String path = "";
        StorageManager sm;
        sm = (StorageManager) mActivity.getSystemService(Activity.STORAGE_SERVICE);
        if (sm != null) {
            StorageVolume[] allExtVol = sm.getStorageVolumes().toArray(new StorageVolume[0]);//.getVolumeList();
            for (StorageVolume v : allExtVol) {
                if (v.isEmulated()) {
                    // internal sdcard, ignore it.
                    continue;
                }
                //if (v.getPath().contains("usbdisk4")) {
                if (v.getDirectory().getPath().contains("usbdisk4")) {
                    // usbdisk4 is an OTG usb port, ignore it
                    continue;
                }
                ///if (Environment.MEDIA_MOUNTED.equals(sm.getVolumeState(v.getPath())))
                if (Environment.MEDIA_MOUNTED.equals(v.getState())) {
                    File f = new File(v.getDirectory().getPath() + "/" + ANDROID_UPDATE_GUIDE_FILE_NAME);
                    if (f.exists()) {
                        path = v.getDirectory().getPath();
                        break;
                    }
                }
            }
        }
        return path;
    }

    private void doAndroidUpdate(String path) {
        // if (path.contains("usbdisk")) {
        // copy the update guide file into internal sdcard
        // Util.copyFile(path + "/" + ANDROID_UPDATE_GUIDE_FILE_NAME,
        // "/sdcard/" + ANDROID_UPDATE_GUIDE_FILE_NAME);
        if (Util.isRKSystem()) {
            Util.sudoExec("rm:/cache/recovery/command");
            // Util.sudoExec("echo:\"--ak_update\">/cache/recovery/command");

            String sd_path = Environment.getExternalStorageDirectory().toString();
            sd_path += "/command_to_recovery";
            File file = new File(sd_path);
            try {
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                FileOutputStream out = new FileOutputStream(file);
                out.write("--ak_update".getBytes());
                // dis.write(value.getBytes());
                out.flush();
                out.close();
                Util.sudoExec("sync");
            } catch (Exception e) {
                e.printStackTrace();
            }
            Util.doSleep(500);
            // Util.sudoExec("cp:" + path + "/" + ANDROID_UPDATE_GUIDE_FILE_NAME
            // + ":/cache/recovery/command");

            Util.sudoExec("cp:" + sd_path + ":/cache/recovery/command");
            Util.sudoExec("sync");

            Util.doSleep(100);
            Util.sudoExec("chmod:666:/cache/recovery/command");

            Util.doSleep(500);
            // }
            Util.sudoExec("reboot:recovery");
        } else {
            Util.sudoExec("cp:" + path + "/" + ANDROID_UPDATE_GUIDE_FILE_NAME + ":/sdcard/" + ANDROID_UPDATE_GUIDE_FILE_NAME);

            Util.sudoExec("sync");
            Util.doSleep(500);
            // }
            Util.sudoExec("reboot");
        }
    }

    private BroadcastReceiver mUnmountReceiver = null;

    private void registerExternalStorageListener() {
        if (mUnmountReceiver == null) {
            mUnmountReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    String path = intent.getData().toString().substring("file://".length());
                    LogFile.d(TAG, action + "----" + path);
                    if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                        updateStoragePath();
                    } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                        updateStoragePath();
                    }
                }
            };
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction(Intent.ACTION_MEDIA_EJECT);
            iFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);

            iFilter.addDataScheme("file");
            getActivity().registerReceiver(mUnmountReceiver, iFilter);
        }
    }

    private void unregisterMountListener() {
        if (mUnmountReceiver != null) {
            getActivity().unregisterReceiver(mUnmountReceiver);
            mUnmountReceiver = null;

        }
    }

    private boolean isMS9120Plugin(Context context) {
        if (context == null) return false;
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        if (deviceList != null) {
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            while (deviceIterator.hasNext()) {
                UsbDevice device = deviceIterator.next();
                if (device != null) {
                    String product = device.getProductName();
                    int vid = device.getVendorId();
                    int pid = device.getProductId();
                    Log.d(TAG, "isMS9120Plugin: " + vid + ":" + pid + " " + product);
                    if (21325 == vid && 24609 == pid) {
                        Log.d(TAG, "MS9120(" + vid + ":" + pid + " " + product + ") plug-in");
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
