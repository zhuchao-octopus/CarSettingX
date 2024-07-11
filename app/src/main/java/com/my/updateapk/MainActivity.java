package com.my.updateapk;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

import com.common.util.AppConfig;
import com.common.util.BroadcastUtil;
import com.common.util.EditDistance;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;

import com.common.util.SystemConfig;
import com.common.util.Util;
import com.common.util.UtilSystem;
import com.common.util.UtilSystem.StorageInfo;
import com.common.util.shell.ShellUtils;
import com.common.util.shell.ShellUtils.CommandResult;
import com.octopus.android.carsettingx.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.gesture.GestureOverlayView;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private int mTest = 10;
	private final static int MSG_REBOOT_TEST = 111111111;
	private Handler mMcuHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REBOOT_TEST:
				--mTest;

				// ((TextView) findViewById(R.id.text_test)).setText("" + mTest
				// + "s will reboot!!");
				Log.d("allen", "MSG_REBOOT_TEST:" + mTest);
				if (mTest <= 0) {
					Log.d("allen", "MSG_REBOOT_TEST system");
					Util.sudoExec("sync");// test
					Util.doSleep(1000);

					// Util.setFileValue(MCU_RECOVERY_FILE, new byte[] { 0x55,
					// (byte) 0xaa, 0x00 });

					try {
						PowerManager pManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
						pManager.reboot("");
					} catch (Exception e) {

						Log.d("allen", "MSG_REBOOT_TEST err" + e);
						finish();
					}
				}
				mMcuHandler.sendEmptyMessageDelayed(MSG_REBOOT_TEST, 1000);
				break;
			default:
				break;
			}
		}
	};

	private static final String MCU_RECOVERY_FILE = "/sys/class/ak/source/factory";

	private void setDefaultInput() {

	}

	private DevicePolicyManager mDpm;
	private InputMethodManager mImm;

	private void updateDefaultInput() {
		InputMethodManager mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mImm.setInputMethod(null, "com.taypo.android.trskb/.TRSoftKeyboard");
	}

	private void updateDefaultInput2() {

		mDpm = (DevicePolicyManager) (getSystemService(Context.DEVICE_POLICY_SERVICE));
		mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		final List<InputMethodInfo> imis = mImm.getEnabledInputMethodList();
		final int N = (imis == null ? 0 : imis.size());
		for (int i = 0; i < N; ++i) {
			final InputMethodInfo imi = imis.get(i);

			int count = imi.getSubtypeCount();
			Log.d("abc", imi.getId() + ":" + imi.toString());
			if ("com.taypo.android.trskb".equals(imi.getPackageName())) {

				Log.d("abc",
						imi.getId() + ":!!!!!!!!!!!" + imi.getPackageName());

				mImm.setInputMethodAndSubtype(null, imi.getId(), null);
				// Intent intent = new
				// Intent(Intent.ACTION_INPUT_METHOD_CHANGED);
				// intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
				// intent.putExtra("input_method_id", imi.getId());
				// sendBroadcastAsUser(intent, UserHandle.CURRENT);
			}

		}

	}

	private void updateDefaultInputff() {
		// InputMethodManager mImm = (InputMethodManager)
		// getSystemService(Context.INPUT_METHOD_SERVICE);
		// mImm.setInputMethod(null, "com.taypo.android.trskb/.TRSoftKeyboard");
		//
		// // ActivityManager mActivityManager = (ActivityManager)
		// getSystemService(Context.ACTIVITY_SERVICE);
		Locale l = new Locale("TR", "tr");

		String[] ids = TimeZone.getAvailableIDs();

		// LocalePicker.updateLocale(l);
	}

	private void updateDefaultInput33() {
		InputMethodManager mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mImm.setInputMethod(null, "com.taypo.android.trskb/.TRSoftKeyboard");

		// ActivityManager mActivityManager = (ActivityManager)
		// getSystemService(Context.ACTIVITY_SERVICE);
		// Locale l = new Locale("TR", "tr");
		//
		// LocalePicker.updateLocale(l);
		final AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		TimeZone tz = TimeZone.getTimeZone("Etc/GMT+3");
		alarm.setTimeZone("Europe/Athens");
		TimeZone t = TimeZone.getDefault();
		Log.d("abc", "t:" + t);

	}

	public static String getFileString(String path) {

		String topPackageName = null;

		FileReader fr = null;
		try {
			fr = new FileReader(path);
			BufferedReader reader = new BufferedReader(fr, 256);
			topPackageName = reader.readLine();

			reader.close();
			fr.close();
		} catch (Exception e) {
		}

		return topPackageName;

	}

	public static String decode(String unicodeStr) {
		if (unicodeStr == null) {
			return null;
		}
		StringBuffer retBuf = new StringBuffer();
		int maxLoop = unicodeStr.length();
		for (int i = 0; i < maxLoop; i++) {
			if (unicodeStr.charAt(i) == '\\') {
				if ((i < maxLoop - 5)
						&& ((unicodeStr.charAt(i + 1) == 'u') || (unicodeStr
								.charAt(i + 1) == 'U')))
					try {
						retBuf.append((char) Integer.parseInt(
								unicodeStr.substring(i + 2, i + 6), 16));
						i += 5;
					} catch (NumberFormatException localNumberFormatException) {
						retBuf.append(unicodeStr.charAt(i));
					}
				else
					retBuf.append(unicodeStr.charAt(i));
			} else {
				retBuf.append(unicodeStr.charAt(i));
			}
		}
		return retBuf.toString();
	}

	public static String decode2(String unicodeStr) {
		if (unicodeStr == null) {
			return null;
		}
		StringBuffer stringBuffer = new StringBuffer();
		int maxLoop = unicodeStr.length();
		for (int i = 0; i < maxLoop; i++) {
			if (unicodeStr.charAt(i) == '&' && unicodeStr.charAt(i + 1) == '#') {
				int endNode = -1; // 结束节点.
				for (int j = i + 2; j < i + 10; j++) {
					if (unicodeStr.charAt(j) == ';') {
						endNode = j;
						break;
					}
				}
				if (endNode != -1) {
					char c = (char) Integer.parseInt(
							unicodeStr.substring(i + 2, endNode), 10);
					stringBuffer.append(c);
					i = endNode;
				}
			}
		}
		return stringBuffer.toString();
	}

	static String mRecordingFile;
	static String TAG = "abc";

	public static String getPrepareRecordingFile() {
		String file;

		long time = System.currentTimeMillis();
		Date d1 = new Date(time);
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String ymd = format.format(d1);

		format = new SimpleDateFormat("HHmmss");
		String t2 = format.format(d1);
		file = ymd + "_" + t2;
		mRecordingFile = file;
		Log.d(TAG, "getPrepareRecordingFile" + mRecordingFile);
		return mRecordingFile;
	}

	public static final String KEY_NAME = "n";
	public static final String KEY_NUM = "m";

	private void queryPhoneBook() {
		try {
			Cursor cursor = getContentResolver().query(
					Uri.parse("content://com.my.bt.BtPhoneBookProvider"), null,
					null, null, null);
			if (cursor != null) {
				Log.d("abc", "" + cursor.getCount());
				Cursor c = cursor;
				if (c != null) {
					c.moveToFirst();
					for (int i = 0; i < c.getCount(); ++i) {
						String name = c.getString(c.getColumnIndex(KEY_NAME));
						String number = c.getString(c.getColumnIndex(KEY_NUM));
						c.moveToNext();
						Log.d("abc", name + ":" + number);

					}
					Toast.makeText(this, "phone book num:" + c.getCount(),
							Toast.LENGTH_LONG).show();
				}

			}
		} catch (Exception e) {
			Toast.makeText(this, "fail", Toast.LENGTH_LONG).show();
		}
	}

	private void dialNum() {
		try {
			Intent it = new Intent(Intent.ACTION_VIEW);
			it.setClassName("com.my.bt", "com.my.bt.ATBluetoothActivity");
			String phoneNum = "10086";
			Uri data = Uri.parse("tel:" + phoneNum);
			it.setData(data);

			it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(it);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	private void openRadioFMFreq() {
		try {
			Intent it = new Intent(Intent.ACTION_VIEW);
			it.setClassName("com.car.ui", "com.my.radio.RadioActivity");

			it.putExtra("amfm", (byte) 0); // 0 is fm
			it.putExtra("freq", 104.0f);

			it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(it);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	private void openRadioAMFreq() {
		try {
			Intent it = new Intent(Intent.ACTION_VIEW);
			it.setClassName("com.car.ui", "com.my.radio.RadioActivity");

			it.putExtra("amfm", (byte) 3); // 3 is am
			it.putExtra("freq", 1224.0f);

			it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(it);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	public void onClick(View v) {
        if (v.getId() == R.id.update_bt) {
            updateBtLib();
        }
	}

	private void updateBtLib() {
		Log.d("abc", Build.VERSION.SDK_INT + ":");

		List<StorageInfo> ls = UtilSystem.listAllStorage(this);
		File f,f2,f3;
		for (StorageInfo si : ls) {
			if (si.mType == StorageInfo.TYPE_USB
					|| si.mType == StorageInfo.TYPE_SD) {
				String file = si.mPath + "/MyCarService.apk";
				String file2 = si.mPath + "/CanboxSetting.apk";
				String file3 = si.mPath + "/FileManager.apk";
				f = new File(file);
				f2 = new File(file2);
				f3 = new File(file3);
				if (f.exists() || f2.exists()|| f3.exists()) {

					Util.setProperty("ctl.stop", "goc_btd_8761");
					Util.doSleep(500);

					if (Build.VERSION.SDK_INT <= 27) {
						Util.sudoExec("mount:-o:remount,rw:/system");
						Util.doSleep(500);
						Util.sudoExec("busybox:mount:-o:remount,rw:/dev/block/mmcblk0p11:/system");

					} else {
						Util.sudoExec("blockdev:--setrw:/dev/block/by-name/system");
						Util.doSleep(500);
						Util.sudoExec("mount:-a:-o:remount,rw:/");
					}

					if (f.exists() ) {

					Util.doSleep(500);
					Util.sudoExec("cp:" + file
							+ ":/system/priv-app/MyCarService/");
					}
					
					if (f2.exists() ) {
					Util.doSleep(500);
					Util.sudoExec("cp:" + file2
							+ ":/system/app/CanboxSetting/");
					}
					if (f3.exists() ) {
					Util.doSleep(500);
					Util.sudoExec("cp:" + file3
							+ ":/system/app/FileManager/");
					}
					
//					Toast.makeText(this, "OK!", Toast.LENGTH_LONG).show();
					Util.sudoExec("sync");
					Util.doSleep(500);
					Util.sudoExec("reboot");

					return;
				}
			}
		}
		Toast.makeText(this, "APK not found!", Toast.LENGTH_LONG).show();
	}

	private void doUpdateIVT() {

		List<StorageInfo> ls = UtilSystem.listAllStorage(this);
		File f;
		for (StorageInfo si : ls) {
			if (si.mType == StorageInfo.TYPE_USB
					|| si.mType == StorageInfo.TYPE_SD) {
				String file = si.mPath + "/DSP_parameter.txt";
				f = new File(file);
				if (f.exists()) {
					Util.sudoExec("cp:" + file + ":/data/bluesoleil/");
					Util.sudoExec("sync");
					Util.doSleep(200);
					Toast.makeText(this, "更新中", Toast.LENGTH_LONG).show();
					return;
				}
			}
		}
		Toast.makeText(this, "没找到声音文件", Toast.LENGTH_LONG).show();
	}

	private void updateBtVol() {
		doUpdateIVT();
		try {
			Intent it = new Intent(Intent.ACTION_VIEW);
			it.setClassName("com.my.bt", "com.my.bt.ATBluetoothActivity");
			String phoneNum = "**000**";
			Uri data = Uri.parse("tel:" + phoneNum);
			it.setData(data);

			it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(it);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}

	}

	private void showLangAndTime() {

		Locale locale = Locale.getDefault();
		Log.d("ddc", "" + locale);

		TimeZone t = TimeZone.getDefault();
		Log.d("abc", "t:" + t);
	}

	private String mHideLauncher;
	private final static String KEY_HIDE_LAUNCHER = "hide_launcher";
	private final static String KEY_PARAMTER_PATH = "paramter_path";
	private final static String PROJECT_CONFIG = ".config_properties";

	// private static void getConfigProperties(String file, String name) {
	// InputStream inputStream = null;
	// File configFile = new File(file);
	// if (configFile.exists()) {
	// try {
	// Properties mPoperties = new Properties();
	// inputStream = new FileInputStream(configFile);
	// mPoperties.clear();
	// mPoperties.load(inputStream);
	// inputStream.close();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// }

	private final static String KEY_TPMS_TYPE = "tpms_type";

	private ArrayList<String> mAppHide = new ArrayList<String>();

	private void initHideLauncher() {
		InputStream inputStream = null;
		File configFile = new File("/mnt/paramter/" + PROJECT_CONFIG);
		if (configFile.exists()) {
			try {
				inputStream = new FileInputStream(configFile);
				Properties pt = new Properties();
				pt.load(inputStream);
				inputStream.close();
				String s = pt.getProperty(KEY_PARAMTER_PATH);
				Log.d("allen", "initHideLauncher:" + s);
				if (s != null) {
					pt.clear();
					configFile = new File(s + PROJECT_CONFIG);
					pt.load(inputStream);
					inputStream.close();
				}
				mHideLauncher = pt.getProperty(KEY_HIDE_LAUNCHER);
				pt = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Log.d("allen", "initHideLauncher:" + mHideLauncher);
		if (mHideLauncher == null) {
			mHideLauncher = "Launcher3";
		}
		String[] ss1 = mHideLauncher.split(",");
		for (String s : ss1) {
			mAppHide.add(s);
		}
		String tpms = null;
		configFile = new File("/mnt/vendor/" + PROJECT_CONFIG);
		if (configFile.exists()) {
			try {
				inputStream = new FileInputStream(configFile);
				Properties pt = new Properties();
				pt.load(inputStream);
				inputStream.close();

				mHideLauncher = pt.getProperty(KEY_HIDE_LAUNCHER);
				tpms = pt.getProperty(KEY_TPMS_TYPE);
				pt = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (mHideLauncher != null) {
			ss1 = mHideLauncher.split(",");
			for (String s : ss1) {
				mAppHide.add(s);
			}
		}
		if (tpms != null && !tpms.equals("0")) {
			mAppHide.add("AKTpms");
		}
	}

	private boolean isHideApp(String name) {
		for (String s : mAppHide) {
			if (s.equals(name)) {
				return true;
			}
		}
		return false;
	}

	public final static int RETURN_CURRENT_VOLUME = 0x78;

	private BroadcastReceiver mReceiver = null;

	public void registerListener() {
		if (mReceiver == null) {
			mReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();
					if (action
							.equals("com.my.car.service.BROADCAST_CAR_SERVICE_SEND")) {
						int cmd = intent.getIntExtra("cmd", 0);
						switch (cmd) {
						case RETURN_CURRENT_VOLUME:
							int volume = intent.getIntExtra("data", -1);
							Log.d("test", "" + volume);
							break;
						case 0x26:
							// if (vv != null) {
							// byte[] b = intent.getByteArrayExtra("data");
							// vv.updateWaveFormData(b);
							// }
							break;

						}
					}
				}
			};
			IntentFilter iFilter = new IntentFilter();
			iFilter.addAction("com.my.car.service.BROADCAST_CAR_SERVICE_SEND");
			registerReceiver(mReceiver, iFilter);
		}
	}

	public void sendToCarService(Context context, int cmd, int data) {
		Intent it;
		it = new Intent("com.my.car.service.BROADCAST_CMD_TO_CAR_SERVICE");
		it.putExtra("cmd", cmd);
		it.putExtra("data", data);
		it.setPackage("com.my.out");
		context.sendBroadcast(it);
	}

	// LineGraphicView tu;
	// VisualizerView vv;
	ArrayList<Double> yList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

}
