package com.my.factory;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings.Secure;




public class SettingsDataHelper {

	private final static String TAG = "SettingsDataHelper";

	/** 面板LED颜色*/
	public final static String KEY_LED_COLOR = "key_led_color";
	public final static byte DEFAULT_LED_COLOR = SettingsConstant.LedColor.NO_COLOR;//默认是无颜色,可从配置文件中读取
	/** 按键声开关状态*/
	public final static String KEY_BEEP_STATUS = "key_beep_status";
	public final static boolean DEFAULT_BEEP_STATUS;//默认按键声是关闭的,可从配置文件中读取
	/** 导航软件包名*/
	public final static String KEY_NAVIGATION_PACKAGE_NAME = "key_navigation_package_name";
	/** 导航软件包名*/
	public final static String KEY_NAVIGATION_PACKAGE_NAME_ADAPTER = "key_navigation_package_name_adapter";
	/** 导航软件类名*/
	public final static String KEY_NAVIGATION_CLAZZ_NAME = "key_navigation_clazz_name";
	/** 导航声音通道*/
	public final static String KEY_NAVIGATION_CHANNEL = "key_navigation_channel";
	public final static byte DEFAULT_NAVIGATION_CHANNEL = SettingsConstant.GeneralSettings.NAVIGATION_CHANNEL_FRONT_LEFT;//默认导航声音通道-前左,可从配置文件中读取
	/** 倒车静音状态*/
	public final static String KEY_REVERSE_MUTE_STATUS = "key_reverse_mute_status";
	public final static boolean DEFAULT_REVERSE_MUTE_STATUS;//默认倒车是静音的,可从配置文件中读取
	/** 第三方声音优先*/
	public final static String KEY_THIRD_APP_SOUND_FIRST = "key_third_app_sound_first";
	public final static boolean DEFAULT_THIRD_APP_SOUND_FIRST;//默认第三方声音优先,可从配置文件中读取
	/** 屏亮度*/
	public final static String KEY_BRIGHTNESS = "key_brightness";
	public final static byte DEFAULT_BRIGHTNESS;//默认是亮度,可从配置文件中读取
	/** 手刹检测方式*/
	public final static String KEY_BRAKE_DETECTION = "key_brake_detection";
	public final static byte DEFAULT_BRAKE_DETECTION = SettingsConstant.GeneralSettings.BRAKE_DETECTION_LEVEL;
	/** 大灯检测方式*/
	public final static String KEY_LIGHT_DETECTION = "key_light_detection";
	public final static byte DEFAULT_LIGHT_DETECTION = SettingsConstant.GeneralSettings.LIGHT_DETECTION_LEVEL;
	/** 大灯控制模式*/
	public final static String KEY_LIGHT_CONTROL_MODEL = "key_light_control_model";
	public final static byte DEFAULT_LIGHT_CONTROL_MODEL = SettingsConstant.GeneralSettings.LIGHT_CONTROL_MODEL_AUTO;
	/** AUX IN设置*/
	public final static String KEY_AUX_IN_MODEL = "key_aux_in_model";
	public final static byte DEFAULT_AUX_IN_MODEL = SettingsConstant.GeneralSettings.AUX_IN_FRONT;
	/** 收音机区域设置*/
	public final static String KEY_RADIO_AREA = "key_radio_area";
	public final static byte DEFAULT_RADIO_AREA = SettingsConstant.RadioArea.CHINA;
	/** 设置风扇启动阈值*/
	public final static String KEY_FAN_START_THRESHOLD = "key_fan_start_threshold";
	public final static byte DEFAULT_FAN_START_THRESHOLD = 0;//0:始终开启	-1:始终关闭
	/** 隐藏的app*/
	public final static String KEY_HIDE_APP = "key_hide_app";
	public final static int DEFAULT_HIDE_APP = 0;//CommonConstant.DeviceType.DEVICE_TYPE_DVD | CommonConstant.DeviceType.DEVICE_TYPE_IPOD | CommonConstant.DeviceType.DEVICE_TYPE_DTV;
	/** 是否有RDS*/
	public final static String KEY_HAVE_RDS = "key_have_rds";
	public final static boolean DEFAULT_HAVE_RDS;
	
	
	//其他设置内容
	/** 是否静音*/
	public final static String KEY_MCU_MUTE = "key_mcu_mute";
	public final static boolean DEFAULT_MCU_MUTE = false;
	/** 当前音量值*/
	public final static String KEY_MCU_VOLUME_VALUME = "key_mcu_volume_valume";
	public final static int DEFAULT_MCU_VOLUME_VALUME = 10;
	/** 导航工作状态*/
	public final static String KEY_NAVIGATION_WORK_STATE = "key_navigation_work_state";
	public final static boolean DEFAULT_NAVIGATION_WORK_STATE = false;
	/** 屏配置文件存储*/
	public final static String KEY_SCREEN_CONFIG_FILE_PATH = "key_screen_config_file_path";
	/** adb enable*/
	public final static String KEY_ADB_ENABLE = "key_adb_enable";
	/** 倒车状态*/
	public final static String KEY_REVERSE_STATUS = "key_reverse_status";
	
	private Context mContext;
	private ContentResolver mContentResolver;
	private static SettingsDataHelper mSettingDataHelper;
	
	static{
//		Config config = Config.getInstance(null);
//		if(config == null){//
//			LOG.print("Cann't config....", JLog.TYPE_ERROR);
			DEFAULT_BEEP_STATUS = false;
			DEFAULT_REVERSE_MUTE_STATUS = true;
			DEFAULT_THIRD_APP_SOUND_FIRST = true;
			DEFAULT_BRIGHTNESS = 18;
			DEFAULT_HAVE_RDS = false;
//		}else{
//			LOG.print("------------default settings");
//			ConfigEntity ce = config.getConfig();
//			if(ce != null){
//				DEFAULT_BEEP_STATUS = ce.beepStatus;
//				DEFAULT_REVERSE_MUTE_STATUS = ce.reverseMuteStatus;
//				DEFAULT_THIRD_APP_SOUND_FIRST = ce.thirdAppSoundFirst;
//				DEFAULT_BRIGHTNESS = (byte) ce.brightness;
//				DEFAULT_HAVE_RDS = ce.haveRds;
//			}else{
//				DEFAULT_BEEP_STATUS = false;
//				DEFAULT_REVERSE_MUTE_STATUS = true;
//				DEFAULT_THIRD_APP_SOUND_FIRST = true;
//				DEFAULT_BRIGHTNESS = 18;
//				DEFAULT_HAVE_RDS = false;
//			}
//		}
	}
	
	private SettingsDataHelper(Context context) {
		mContext = context;
		mContentResolver = context.getContentResolver();
	}

	public final static SettingsDataHelper getInstance() {
		if (mSettingDataHelper == null) {
			new Exception("SettingsDataHelper Method init() to be called at least once must!");
		}
		return mSettingDataHelper;
	}

	public synchronized static SettingsDataHelper init(Context context) {
		if (mSettingDataHelper == null && context != null) {
			mSettingDataHelper = new SettingsDataHelper(context);
		}
		return mSettingDataHelper;
	}
	
	public void saveLedColor(int colorValue){
		Secure.putInt(mContentResolver, KEY_LED_COLOR, colorValue);
	}
	
	public int getLedColor(){
		int value = Secure.getInt(mContentResolver, KEY_LED_COLOR, DEFAULT_LED_COLOR);
		return value;
	}
	
	public boolean getBeepStatus(){
		int value = Secure.getInt(mContentResolver, KEY_BEEP_STATUS, -1);
		if(value == -1){
			return DEFAULT_BEEP_STATUS;
		}
		return value == 1;
	}
	
	public void saveBeepStatus(boolean isOpen){
		Secure.putInt(mContentResolver, KEY_BEEP_STATUS, isOpen ? 1 : 0);
	}
	
	public void saveScreenConfigurationFile(String configPath){
		Secure.putString(mContentResolver, KEY_SCREEN_CONFIG_FILE_PATH, configPath);
	}
	
	public String getScreenConfigurationFile(){
		return Secure.getString(mContentResolver, KEY_SCREEN_CONFIG_FILE_PATH);
	}
	
	public void saveAdbEnable(int enable){
		Secure.putInt(mContentResolver, KEY_ADB_ENABLE, enable);
	}
	
	public int getAdbEnable(){
		return Secure.getInt(mContentResolver, KEY_ADB_ENABLE, 0);
	}
	
	private final static List<String> NAVIGATION_PACKAGE_ADAPTER = new ArrayList<String>();
	static{
		NAVIGATION_PACKAGE_ADAPTER.add("com.autonavi.minimap,com.goodocom.gocsdk");
		//..and so on
	}
	
	public void saveNavigation(String pkgName, String clzName){
		for(String adapterPkg: NAVIGATION_PACKAGE_ADAPTER){
			if(adapterPkg.contains(pkgName)){
				Secure.putString(mContentResolver, KEY_NAVIGATION_PACKAGE_NAME_ADAPTER, adapterPkg);
				Secure.putString(mContentResolver, KEY_NAVIGATION_PACKAGE_NAME, pkgName);
				Secure.putString(mContentResolver, KEY_NAVIGATION_CLAZZ_NAME, clzName);
				return;
			}
		}
		Secure.putString(mContentResolver, KEY_NAVIGATION_PACKAGE_NAME_ADAPTER, pkgName);
		Secure.putString(mContentResolver, KEY_NAVIGATION_PACKAGE_NAME, pkgName);
		Secure.putString(mContentResolver, KEY_NAVIGATION_CLAZZ_NAME, clzName);
	}
	
	public String getNavigationPackageName(){
		return Secure.getString(mContentResolver, KEY_NAVIGATION_PACKAGE_NAME);
	}
	
	public String getNavigationClazzName(){
		return Secure.getString(mContentResolver, KEY_NAVIGATION_CLAZZ_NAME);
	}
	
	
	public void saveNavigationChannel(int channel){
		Secure.putInt(mContentResolver, KEY_NAVIGATION_CHANNEL, channel);
	}
	
	public int getNavigationChannel(){
		int value = Secure.getInt(mContentResolver, KEY_NAVIGATION_CHANNEL, DEFAULT_NAVIGATION_CHANNEL);
		return value;
	}
	
	public void saveReverseMute(boolean isMute){
		Secure.putInt(mContentResolver, KEY_REVERSE_MUTE_STATUS, isMute ? 1 : 0);
	}
	
	public boolean getReverseMute(){
		int value = Secure.getInt(mContentResolver, KEY_REVERSE_MUTE_STATUS, -1);
		if(value == -1){
			return DEFAULT_REVERSE_MUTE_STATUS;
		}
		return value == 1;
	}
	
	public void saveReverse(boolean isReverse){
		Secure.putInt(mContentResolver, KEY_REVERSE_STATUS, isReverse ? 1 : 0);
	}
	
	public boolean getReverse(){
		int value = Secure.getInt(mContentResolver, KEY_REVERSE_STATUS, 0);
		return value == 1;
	}
	
	public void saveThirdAppSoundFirst(boolean isMute){
		Secure.putInt(mContentResolver, KEY_THIRD_APP_SOUND_FIRST, isMute ? 1 : 0);
	}
	
	public boolean getThirdAppSoundFirst(){
		int value = Secure.getInt(mContentResolver, KEY_THIRD_APP_SOUND_FIRST, -1);
		if(value == -1){
			return DEFAULT_THIRD_APP_SOUND_FIRST;
		}
		return value == 1;
	}
	
	public void saveBrightness(int brightness){
		Secure.putInt(mContentResolver, KEY_BRIGHTNESS, brightness);
	}
	
	public int getBrightness(){
		int value = Secure.getInt(mContentResolver, KEY_BRIGHTNESS, DEFAULT_BRIGHTNESS);
		return value;
	}
	
	public void saveBrakeDetection(int detectionType){
		Secure.putInt(mContentResolver, KEY_BRAKE_DETECTION, detectionType);
	}
	
	public int getBrakeDetection(){
		int value = Secure.getInt(mContentResolver, KEY_BRAKE_DETECTION, DEFAULT_BRAKE_DETECTION);
		return value;
	}
	
	public void saveLightDetection(int detectionType){
		Secure.putInt(mContentResolver, KEY_LIGHT_DETECTION, detectionType);
	}
	
	public int getLightDetection(){
		int value = Secure.getInt(mContentResolver, KEY_LIGHT_DETECTION, DEFAULT_LIGHT_DETECTION);
		return value;
	}
	
	public void saveLightControlModel(int controlModel){
		Secure.putInt(mContentResolver, KEY_LIGHT_CONTROL_MODEL, controlModel);
	}
	
	public int getLightControlModel(){
		int value = Secure.getInt(mContentResolver, KEY_LIGHT_CONTROL_MODEL, DEFAULT_LIGHT_CONTROL_MODEL);
		return value;
	}
	
	public void saveAuxIn(int witch){
		Secure.putInt(mContentResolver, KEY_AUX_IN_MODEL, witch);
	}
	
	public int getAuxIn(){
		int value = Secure.getInt(mContentResolver, KEY_AUX_IN_MODEL, DEFAULT_AUX_IN_MODEL);
		return value;
	}
	
	public void saveRadioArea(int area){
		Secure.putInt(mContentResolver, KEY_RADIO_AREA, area);
	}
	
	public int getRadioArea(){
		int value = Secure.getInt(mContentResolver, KEY_RADIO_AREA, DEFAULT_RADIO_AREA);
		return value;
	}
	
	public void saveFanStartThreshold(int witch){
		Secure.putInt(mContentResolver, KEY_FAN_START_THRESHOLD, witch);
	}
	
	public int getFanStartThreshold(){
		int value = Secure.getInt(mContentResolver, KEY_FAN_START_THRESHOLD, DEFAULT_FAN_START_THRESHOLD);
		return value;
	}
	
	public void saveHideApp(int hideApp){
		Secure.putInt(mContentResolver, KEY_HIDE_APP, hideApp);
	}
	
	public int getHideApp(){
		int value = Secure.getInt(mContentResolver, KEY_HIDE_APP, DEFAULT_HIDE_APP);
		return value;
	}
	
	public void saveHaveRds(boolean haveRds){
		Secure.putInt(mContentResolver, KEY_HAVE_RDS, haveRds ? 1 : 0);
	}
	
	public boolean getHaveRds(){
		int value = Secure.getInt(mContentResolver, KEY_HAVE_RDS, -1);
		if(value == -1){
			return DEFAULT_HAVE_RDS;
		}
		return value == 1;
	}
	
	//----------其他
	public void saveMute(boolean isMute){
		Secure.putInt(mContentResolver, KEY_MCU_MUTE, isMute ? 1 : 0);
	}
	
	public boolean getMute(){
		int value = Secure.getInt(mContentResolver, KEY_MCU_MUTE, -1);
		if(value == -1){
			return DEFAULT_MCU_MUTE;
		}
		return value == 1;
	}
	
	public void saveMcuVolume(int hideApp){
		Secure.putInt(mContentResolver, KEY_MCU_VOLUME_VALUME, hideApp);
	}
	
	public int getMcuVolume(){
		int value = Secure.getInt(mContentResolver, KEY_MCU_VOLUME_VALUME, DEFAULT_MCU_VOLUME_VALUME);
		return value;
	}
	
	public void saveNavigationWorkState(boolean isWorking){
		Secure.putInt(mContentResolver, KEY_NAVIGATION_WORK_STATE, isWorking ? 1 : 0);
	}
	
	public boolean getNavigationWorkState(){
		int value = Secure.getInt(mContentResolver, KEY_NAVIGATION_WORK_STATE, -1);
		if(value == -1){
			return DEFAULT_NAVIGATION_WORK_STATE;
		}
		return value == 1;
	}
}
