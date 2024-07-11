package com.my.hardware.setting;

import android.util.Log;

public class HWSetting {
	static {
		System.loadLibrary("MySetting");
	}
	private final static String TAG = "HWSetting";
	public final static boolean LOGV = false;
	
	public static final int SETTING_GET_AUXIN =1;
	public static final int SETTING_SET_AUXIN =2;
	public static final int SETTING_GET_DVD =3;
	public static final int SETTING_SET_DVD =4;
	public static final int SETTING_GET_REAKSW =5;
	public static final int SETTING_SET_REAKSW =6;
	public static final int SETTING_GET_ILLUMIN =7;
	public static final int SETTING_SET_ILLUMIN =8;
	public static final int SETTING_GET_RADIO =9;
	public static final int SETTING_SET_RADIO =10;
	public static final int SETTING_GET_FAN =11;
	public static final int SETTING_SET_FAN =12;
	public static final int SETTING_SECRET_RESET =13;
	public static final int SETTING_MCU_RESET =14;
	public static final int SETTING_SYSTEM_RESET =15;
	public static final int SETTING_GET_RADIO_REGION =16;
	public static final int SETTING_SET_RADIO_REGION =17;
	public static final int SETTING_GET_CAR_SUPPORT =18;
	public static final int SETTING_SET_CAR_SUPPORT =19;
	public static final int SETTING_UPDATE_FIRMWARE =20;
	public static final int SETTING_GET_LED =21;
	public static final int SETTING_SET_LED =22;
	private native final int nativeSendCommand(int cmd, int param1, int param2);

	public int sendBTCommand(int cmd) {
		return nativeSendCommand(cmd, 0, 0);
	}

	public int sendBTCommand(int cmd, int param) {
		return nativeSendCommand(cmd, param, 0);
	}

	public int sendBTCommand(int cmd, int param1, int param2) {
		return nativeSendCommand(cmd, param1, param2);
	}

}