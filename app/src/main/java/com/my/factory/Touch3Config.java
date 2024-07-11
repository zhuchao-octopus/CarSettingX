package com.my.factory;

import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.octopus.android.carsettingx.R;

public class Touch3Config {
	private static final String TAG = "Touch3Info";
	private final boolean DBG = false;
	public String[] mEntries = null;
	public String[] mEntriesVlue = null;

	public void loadTouch3Entries(Context context) {
		HashMap<String, String> thirdAppMap = getThirdAppList(context);
		String[] key_entries = context.getResources().getStringArray(
				R.array.touch3_settings_keycode_entries);
		String[] key_entriesVlue = context.getResources().getStringArray(
				R.array.touch3_settings_keycode_entries_values);
		if (key_entries != null && key_entriesVlue != null
				&& key_entries.length == key_entriesVlue.length) {
			int count = key_entries.length + (thirdAppMap == null ? 0 : thirdAppMap.size());
			mEntries = new String[count];
			mEntriesVlue = new String[count];
			for (int i = 0; i < key_entries.length; i++) {
				//add "KEY: " prefix for entry, such as "KEY: NEXT"
				mEntries[i] = context.getResources().getString(R.string.touch3_key_type) + key_entries[i];
				//add "keycode:" prefix for entryvalue, such as "keycode:3"
				mEntriesVlue[i] = "keycode:" + key_entriesVlue[i];
			}
			if (thirdAppMap != null) {
				int i = 0;
				for (HashMap.Entry<String, String> entry : thirdAppMap.entrySet()) {
					mEntries[key_entries.length + i] = entry.getValue();
					mEntriesVlue[key_entriesVlue.length + i] = entry.getKey();
					i++;
				}
			}
			
			/*if (DBG && mEntries != null && mEntriesVlue != null) {
				Log.d(TAG, "" + mEntries.length + "," + mEntriesVlue.length);
				for (int i = 0; i < mEntries.length; i++)
					Log.d(TAG, mEntries[i]);
				for (int i = 0; i < mEntriesVlue.length; i++)
					Log.d(TAG, mEntriesVlue[i]);
			}*/
		}
	}

	private HashMap<String, String> getThirdAppList(Context context) {
		PackageManager packageManager = context.getPackageManager();
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		final List<ResolveInfo> appInfoList = packageManager.queryIntentActivities(mainIntent, 0);

		HashMap<String, String> thirdAPP = new HashMap<String, String>();
		for (ResolveInfo appInfo : appInfoList) {
			if ((appInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
				if (appInfo.activityInfo.applicationInfo.packageName != null &&
					appInfo.activityInfo.name != null) {
					String appName = appInfo.loadLabel(packageManager).toString();
					if (appName != null) {
						//add "APP: " prefix for entry, such as "APP: ES File Explorer"
						//add "app:" prefix for entryvalue, such as "app:com.estrongs.android.pop,com.estrongs.android.pop.FexApplication"
						thirdAPP.put("app:" + appInfo.activityInfo.applicationInfo.packageName + "," + 
												appInfo.activityInfo.name,
								context.getResources().getString(R.string.touch3_app_type) + appName.toString());
					}
					if (DBG)
						Log.d(TAG, appName.toString() + ","
							+ appInfo.activityInfo.applicationInfo.packageName + ","
							+ appInfo.activityInfo.name);
				}
			}
		}
		return thirdAPP;
	}

	public static final String KEY_SWITCH = "switch";
	public static final String KEY_USER_CONFIGABLE = "user_configurable";
	public static final String KEY_UP = "up";
	public static final String KEY_DOWN = "down";
	public static final String KEY_LEFT = "left";
	public static final String KEY_RIGHT = "right";
	/**
	 * {"switch":true,"user_configurable":"true","up":"keycode:37","down":"keycode:36","left":"app:com.adobe.reader,com.adobe.reader.misc.ARApp","right":"app:net.easyconn,net.easyconn.WelcomeActivity","up_name":"BACK","down_name":"HOME","left_name":"Adobe Acrobat","right_name":"EasyConnection"}
	 * {"switch":true,"user_configurable":"true","up":"keycode:37","down":"keycode:36","left":"app:com.adobe.reader,com.adobe.reader.misc.ARApp","right":"app:net.easyconn,net.easyconn.WelcomeActivity","up_name":"返回","down_name":"HOME","left_name":"Adobe Acrobat","right_name":"亿连手机互联"}
	 */
	public boolean saveConfigJSON(boolean isFactory, Context context, boolean enabled, 
			String valueUp,	String valueDown, String valueLeft, String valueRight,
			CharSequence nameUp,CharSequence nameDown, CharSequence nameLeft, CharSequence nameRight) {
		try {
			JSONObject jobj = new JSONObject();
			jobj.put(KEY_SWITCH, enabled);
			if (isFactory)
				jobj.put(KEY_USER_CONFIGABLE, enabled);
			else
				jobj.put(KEY_USER_CONFIGABLE, true);
			jobj.put(KEY_UP, valueUp);
			jobj.put(KEY_DOWN, valueDown);
			jobj.put(KEY_LEFT, valueLeft);
			jobj.put(KEY_RIGHT, valueRight);
//			jobj.put(KEY_UP + "_name", removePrefix(nameUp));
//			jobj.put(KEY_DOWN + "_name", removePrefix(nameDown));
//			jobj.put(KEY_LEFT + "_name", removePrefix(nameLeft));
//			jobj.put(KEY_RIGHT + "_name", removePrefix(nameRight));
			if (DBG)
				Log.d(TAG, "saveConfigJSON: " + jobj.toString());
//			boolean ret = SystemConfig.setProperty(context, MachineConfig.KEY_TOUCH3_IDENTIFY, jobj.toString());
			MachineConfig.setProperty(MachineConfig.KEY_TOUCH3_IDENTIFY, jobj.toString());
			notifyChanged(context);
			return true;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	private String removePrefix(CharSequence seq) {
		if (seq != null) {
			String value = seq.toString();
			int separator = value.indexOf(":");
			if (separator >= 0)
				return value.substring(separator + 2);	//+2 is skip a space after ":"
		}
		return "";
	}

	private void notifyChanged(Context context) {
		final String BROADCAST_SETTINGS_SEND_SETTINGS_CHANGED = "com.my.car.settings.BROADCAST_SEND_settings_changed";
		try{
			Intent intent = new Intent(BROADCAST_SETTINGS_SEND_SETTINGS_CHANGED);
//			intent.setPackage("com.android.internal.widget");
			context.sendBroadcast(intent);
			
			Intent it = new Intent(
					MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
			it.putExtra(
					MyCmd.EXTRA_COMMON_CMD,
					MachineConfig.KEY_TOUCH3_IDENTIFY);
			context.sendBroadcast(it);
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}
