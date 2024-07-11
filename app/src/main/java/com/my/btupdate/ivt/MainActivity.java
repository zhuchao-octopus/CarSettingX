package com.my.btupdate.ivt;

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

	final static String TAG = "MainActivity";
	
	public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.update_bt_vol) {
            updateBtVol();
        } else if (id == R.id.update_bt) {
            updateBtLib();
        } else if (id == R.id.update_bt_145) {
            updateBtLib145();
        } else if (id == R.id.update_bt_8761) {
            updateBtLibGOC();
        } else if (id == R.id.update_bt_210) {
            updateBtLibGOC210();
        } else if (id == R.id.update_bt_sd816) {
            updateBtLibSD816();
        }
	}


	private void updateBtLibGOC() {
		Log.d("abc", Build.VERSION.SDK_INT+":");		
		
		
		List<StorageInfo> ls = UtilSystem.listAllStorage(this);
		File f;
		for (StorageInfo si : ls) {
			if (si.mType == StorageInfo.TYPE_USB
					|| si.mType == StorageInfo.TYPE_SD) {
				String file = si.mPath + "/gocsdk_8761";
				f = new File(file);
				if (f.exists()) {
					
					Util.setProperty("ctl.stop", "goc_btd_8761");
					Util.doSleep(500);
					
					if (Build.VERSION.SDK_INT <= 27){
						Util.sudoExec("mount:-o:remount,rw:/system");
						Util.doSleep(500);
						Util.sudoExec("busybox:mount:-o:remount,rw:/dev/block/mmcblk0p11:/system");
					} else {
						Util.sudoExec("blockdev:--setrw:/dev/block/by-name/system");
						Util.doSleep(500);
						Util.sudoExec("mount:-a:-o:remount,rw:/");
					}
					
					Util.doSleep(500);
					Util.sudoExec("cp:" + file + ":/system/bin/gocsdk_8761");
					
					Util.sudoExec("sync");
					Util.doSleep(300);
					Util.sudoExec("reboot");

					 Toast.makeText(this, getString(R.string.bt_updating), Toast.LENGTH_LONG).show();
					return;
				}
			}
		}
		Toast.makeText(this, getString(R.string.bt_not_found), Toast.LENGTH_LONG).show();
	}


	private void updateBtLibSD816() {
		Log.d("abc", Build.VERSION.SDK_INT+":");		
		
		
		List<StorageInfo> ls = UtilSystem.listAllStorage(this);
		File f;
		for (StorageInfo si : ls) {
			if (si.mType == StorageInfo.TYPE_USB
					|| si.mType == StorageInfo.TYPE_SD) {
				String file = si.mPath + "/sdsdk816";
				f = new File(file);
				if (f.exists()) {
					
					Util.setProperty("ctl.stop", "sdsdk816");
					Util.doSleep(500);
					
					if (Build.VERSION.SDK_INT <= 27){
						Util.sudoExec("mount:-o:remount,rw:/system");
						Util.doSleep(500);
						Util.sudoExec("busybox:mount:-o:remount,rw:/dev/block/mmcblk0p11:/system");
					} else {
						Util.sudoExec("blockdev:--setrw:/dev/block/by-name/system");
						Util.doSleep(500);
						Util.sudoExec("mount:-a:-o:remount,rw:/");
					}
					
					Util.doSleep(500);
					Util.sudoExec("cp:" + file + ":/system/bin/sdsdk816");
					
					Util.sudoExec("sync");
					Util.doSleep(300);
					Util.sudoExec("reboot");

					 Toast.makeText(this, getString(R.string.bt_updating), Toast.LENGTH_LONG).show();
					return;
				}
			}
		}
		Toast.makeText(this, getString(R.string.bt_not_found), Toast.LENGTH_LONG).show();
	}
	
	private void updateBtLibGOC210() {
		Log.d("abc", Build.VERSION.SDK_INT+":");		
		
		
		List<StorageInfo> ls = UtilSystem.listAllStorage(this);
		File f;
		for (StorageInfo si : ls) {
			if (si.mType == StorageInfo.TYPE_USB
					|| si.mType == StorageInfo.TYPE_SD) {
				String file = si.mPath + "/gocsdk_210";
				f = new File(file);
				if (f.exists()) {
					
					Util.setProperty("ctl.stop", "goc_btd_210");
					Util.doSleep(500);
					
					if (Build.VERSION.SDK_INT <= 27){
						Util.sudoExec("mount:-o:remount,rw:/system");
						Util.doSleep(500);
						Util.sudoExec("busybox:mount:-o:remount,rw:/dev/block/mmcblk0p11:/system");
					} else {
						Util.sudoExec("blockdev:--setrw:/dev/block/by-name/system");
						Util.doSleep(500);
						Util.sudoExec("mount:-a:-o:remount,rw:/");
					}
					
					Util.doSleep(500);
					Util.sudoExec("cp:" + file + ":/system/bin/gocsdk_210");
					
					Util.sudoExec("sync");
					Util.doSleep(300);
					Util.sudoExec("reboot");

					 Toast.makeText(this, getString(R.string.bt_updating), Toast.LENGTH_LONG).show();
					return;
				}
			}
		}
		Toast.makeText(this, getString(R.string.bt_not_found), Toast.LENGTH_LONG).show();
	}
	
	private void updateBtLib145() {
		List<StorageInfo> ls = UtilSystem.listAllStorage(this);
		File f;
		for (StorageInfo si : ls) {
			if (si.mType == StorageInfo.TYPE_USB
					|| si.mType == StorageInfo.TYPE_SD) {
				String file = si.mPath + "/libbt_platform.so";
				f = new File(file);
				if (f.exists()) {
					Util.sudoExec("cp:" + file + ":/oem/lib/i145/");
					Util.doSleep(10);
					file = si.mPath + "/libbluelet.so";
					Util.sudoExec("cp:" + file + ":/oem/lib/i145/");
					Util.doSleep(10);
					file = si.mPath + "/blueletd";
					Util.sudoExec("cp:" + file + ":/oem/lib/i145/");

					Util.sudoExec("sync");
					Util.doSleep(200);
					Util.setFileValue("/sys/class/ak/source/factory", new byte[] { 0x55,
							(byte) 0xaa, 0x00 });

					// Toast.makeText(this, getString(R.string.bt_updating), Toast.LENGTH_LONG).show();
					return;
				}
			}
		}
		Toast.makeText(this, getString(R.string.bt_not_found), Toast.LENGTH_LONG).show();
	}
	
	private void updateBtLib() {
		List<StorageInfo> ls = UtilSystem.listAllStorage(this);
		File f;
		for (StorageInfo si : ls) {
			if (si.mType == StorageInfo.TYPE_USB
					|| si.mType == StorageInfo.TYPE_SD) {
				String file = si.mPath + "/libbt_platform.so";
				f = new File(file);
				if (f.exists()) {
					Util.sudoExec("cp:" + file + ":/oem/lib/i140/");
					Util.doSleep(10);
					file = si.mPath + "/libbluelet.so";
					Util.sudoExec("cp:" + file + ":/oem/lib/i140/");
					Util.doSleep(10);
					file = si.mPath + "/blueletd";
					Util.sudoExec("cp:" + file + ":/oem/lib/i140/");

					Util.sudoExec("sync");
					Util.doSleep(200);
					Util.setFileValue("/sys/class/ak/source/factory", new byte[] { 0x55,
							(byte) 0xaa, 0x00 });

					// Toast.makeText(this, getString(R.string.bt_updating), Toast.LENGTH_LONG).show();
					return;
				}
			}
		}
		Toast.makeText(this, getString(R.string.bt_not_found), Toast.LENGTH_LONG).show();
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
					Toast.makeText(this, getString(R.string.bt_updating), Toast.LENGTH_LONG).show();
					return;
				}
			}
		}
		Toast.makeText(this, getString(R.string.bt_not_sound_file), Toast.LENGTH_LONG).show();
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

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update_ivt_btlib);
		
	}
}
