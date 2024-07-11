package com.my.factory;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;


public class SettingsControllor {

	private final static String TAG = "SettingsControllor";

	private List<GeneralSettingListener> mGeneralSettingListenerList;

	private static SettingsControllor instance;

	private SettingsControllor(Context aContext) {

		mGeneralSettingListenerList = new ArrayList<SettingsControllor.GeneralSettingListener>();
	}

	public static final SettingsControllor getInstance(Context aContext) {
		if (instance == null) {
			instance = new SettingsControllor(aContext);
		}
		return instance;
	}

	public void addGeneralSettingListener(GeneralSettingListener listener) {
		if (!mGeneralSettingListenerList.contains(listener)) {
			mGeneralSettingListenerList.add(listener);
		}
	}

	public void removeGeneralSettingListener(GeneralSettingListener listener) {
		if (mGeneralSettingListenerList.contains(listener)) {
			mGeneralSettingListenerList.remove(listener);
		}
	}

	public interface GeneralSettingListener {

		void notifyLedColorChange(byte color);

		void notifyBrakeDetection(byte detectionType);

		void notifyBrightness(byte brightness);

		void notifyBuzzer(boolean isOpen);

		void notifyLightControlModel(byte controlModel);

		void notifyLightDetection(byte detectionType);

		void notifyNaviVoiceChannelType(byte channel);

		void notifyReverseMute(boolean isMute);

	}

//	private ISettingsRemoteCallback.Stub mSettingsCallback = new ISettingsRemoteCallback.Stub() {
//
//		@Override
//		public void onLedColor(byte color) throws RemoteException {
//			// LOG.print("onLedColor color = " + color);
//			for (int i = 0; i < mGeneralSettingListenerList.size(); i++) {
//				mGeneralSettingListenerList.get(i).notifyLedColorChange(color);
//			}
//		}
//
//		@Override
//		public void onGetBrakeDetection(byte detectionType)
//				throws RemoteException {
//			for (int i = 0; i < mGeneralSettingListenerList.size(); i++) {
//				mGeneralSettingListenerList.get(i).notifyBrakeDetection(
//						detectionType);
//			}
//		}
//
//		@Override
//		public void onGetBrightness(byte brightness) throws RemoteException {
//			for (int i = 0; i < mGeneralSettingListenerList.size(); i++) {
//				mGeneralSettingListenerList.get(i).notifyBrightness(brightness);
//			}
//		}
//
//		@Override
//		public void onGetBuzzer(boolean isOpen) throws RemoteException {
//			for (int i = 0; i < mGeneralSettingListenerList.size(); i++) {
//				mGeneralSettingListenerList.get(i).notifyBuzzer(isOpen);
//			}
//		}
//
//		@Override
//		public void onGetLightControlModel(byte controlModel)
//				throws RemoteException {
//			for (int i = 0; i < mGeneralSettingListenerList.size(); i++) {
//				mGeneralSettingListenerList.get(i).notifyLightControlModel(
//						controlModel);
//			}
//		}
//
//		@Override
//		public void onGetLightDetection(byte detectionType)
//				throws RemoteException {
//			for (int i = 0; i < mGeneralSettingListenerList.size(); i++) {
//				mGeneralSettingListenerList.get(i).notifyLightDetection(
//						detectionType);
//			}
//		}
//
//		@Override
//		public void onGetNaviVoiceChannelType(byte channel)
//				throws RemoteException {
//			for (int i = 0; i < mGeneralSettingListenerList.size(); i++) {
//				mGeneralSettingListenerList.get(i).notifyNaviVoiceChannelType(
//						channel);
//			}
//		}
//
//		@Override
//		public void onGetReverseMute(boolean isMute) throws RemoteException {
//			for (int i = 0; i < mGeneralSettingListenerList.size(); i++) {
//				mGeneralSettingListenerList.get(i).notifyReverseMute(isMute);
//			}
//		}
//
//	};
//
//	@Override
//	protected void doRelease() {
//		mSettingsCallback = null;
//		instance = null;
//	}
//
//	@Override
//	protected void doServiceDisconnected(ComponentName componentname) {
//		try {
//			if (mFeature != null)
//				mFeature.unRegistCallback(mSettingsCallback);
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
//	}
//
//	@Override
//	protected void doServiceConnected(ComponentName componentname,
//			IBinder ibinder) {
//		mFeature = ISettingsService.Stub.asInterface(ibinder);
//		try {
//			mFeature.registCallback(mSettingsCallback);
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
//	}
//
//	@Override
//	protected Intent getServiceIntent() {
//		Intent settingService = new Intent(
//				"com.seanovo.android.action.ACTION_SETTINGS_SERVICE");
//		settingService.setPackage("com.car.main");
//		settingService.putExtra(EXTRA_BINDER_TYPE, BINDER_TYPE_SETTINGS);
//		return settingService;
//	}

}
