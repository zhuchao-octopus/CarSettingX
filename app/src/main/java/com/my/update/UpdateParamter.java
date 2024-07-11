package com.my.update;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.common.util.Util;
import com.common.util.UtilSystem;

import com.my.hardware.mud.MubUpdate;
import com.common.util.UtilSystem.StorageInfo;
import com.octopus.android.carsettingx.R;

public class UpdateParamter extends Activity {

	private int rebootTime = 5;
	private Handler mRebootHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (rebootTime == 0) {
				Util.setFileValue("/sys/class/ak/source/factory", new byte[] { 0x55,
						(byte) 0xaa, 0x00 });
			} else {
				if (rebootTime < 0 || rebootTime > 6) {
					rebootTime = 1;
				}
				mTV.setText("System will reboot after: " + rebootTime + "s !!");
				--rebootTime;
				mRebootHandler.sendEmptyMessageDelayed(0, 1000);
			}
		}
	};

	private final static int MSG_CHECK = 0;

	private final static String TAG = "UpdateParamter";
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case MSG_CHECK:
				String s = Util.getProperty("init.svc.akd");
				Log.d(TAG, "MSG_CHECK:" + s);
				mHandler.sendEmptyMessageDelayed(MSG_CHECK, 200);
				break;
			}
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		mHandler.removeMessages(MSG_CHECK);
	};

	private final static String PARAMTER_PATH = "paramter";
	private final static String PARAMTER_TARGAT = "/mnt/paramter";

	private void checkAKDRunning(){
		int i;
		String s;
		Util.doSleep(2);
		for (i = 0; i < 9999; ++i) {
			
			s = Util.getProperty("init.svc.akd");
			if (!"running".equals(s)) {
				break;
			}
			Util.doSleep(20);
		}
		Util.doSleep(2);
	}
	private void doUpdateParamter6818() {
		boolean ret = false;
		String path = "/storage/usbdisk1/" + PARAMTER_PATH;
		File f = new File(path);
		if (!f.exists()) {
			path = "/storage/usbdisk2/" + PARAMTER_PATH;
			f = new File(path);
			if (!f.exists()) {
				path = "/storage/usbdisk3/" + PARAMTER_PATH;
				f = new File(path);
				if (!f.exists()) {
					path = "/storage/usbdisk4/" + PARAMTER_PATH;
					f = new File(path);
					if (!f.exists()) {
						path = "/storage/sdcard1/" + PARAMTER_PATH;
						f = new File(path);
						if (!f.exists()) {
							path = "/storage/sdcard2/" + PARAMTER_PATH;
							f = new File(path);
						} else {
							ret = true;
						}
					} else {
						ret = true;
					}
				} else {
					ret = true;
				}
			} else {
				ret = true;
			}
		} else {
			ret = true;
		}

		if (ret) {

			// mHandler.sendEmptyMessageDelayed(MSG_CHECK, 200);

			mPath = path;
			AsyncTask.execute(new Runnable() {
				public void run() {
					rebootTime = 3;
					Log.d(TAG, "AsyncTask11111:");
					int i = 0;
					String s;
					Util.sudoExec("mount:-o:remount,rw:/mnt/paramter");
					checkAKDRunning();
					Util.sudoExec("rm:-rf:/mnt/paramter/.*");
					checkAKDRunning();
					Util.sudoExec("rm:-rf:/mnt/paramter/*");
					checkAKDRunning();

					Util.sudoExec("cp:-rf:" + mPath + "/.*:/mnt/paramter/");

					checkAKDRunning();
					mRebootHandler.post(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							mTV.setText("coping files.... please wait");
						}
					});
					Util.sudoExec("cp:-rf:" + mPath + "/*:/mnt/paramter/");					
					checkAKDRunning();
					mRebootHandler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							mTV.setText("clean data.... please wait");
						}
					});
					
					 Util.sudoExec("rm:-rf:/data/*");
					for (i = 0; i < 9999; ++i) {
						Util.doSleep(20);
						s = Util.getProperty("init.svc.akd");
						if (!"running".equals(s)) {
							break;
						}
					}
					Util.sudoExec("sync");
					checkAKDRunning();
					mRebootHandler.sendEmptyMessage(0);
				}
			});
			// UtilSystem.doRunActivity(this,
			// "com.my.update.intent.action.UpdateParamter");

			// Util.sudoExec("reboot");

			// Toast.makeText(this, "Paramter udpate ok!", Toast.LENGTH_SHORT)
			// .show();
		} else {
			Toast.makeText(this, "Paramter data not found!", Toast.LENGTH_SHORT)
					.show();
		}
		// finish();
	}

	private int mUpdateIndex = 0;
	private String mPath;

	private void doUpdateParamter() {
		if (Build.VERSION.SDK_INT <= 23) {
			doUpdateParamter6818();

		}
	}

	private void doUpdateParamterRK() {

		boolean ret = false;
		List<StorageInfo> list = UtilSystem.listAllStorage(this);
		for (int i = 0; i < list.size(); ++i) {
			StorageInfo si = list.get(i);
			if (si.mType == StorageInfo.TYPE_USB
					|| si.mType == StorageInfo.TYPE_SD) {
				String path = si.mPath;
				if (!si.mPath.endsWith("/")) {
					path += "/";
				}
				path = path + PARAMTER_PATH;

				File f = new File(path);
				if (f.exists()) {
					Util.sudoExec("mount:-o:remount,rw:/mnt/paramter");
					Util.doSleep(10);
					Util.doSleep(20);
					Util.sudoExec("rm:-rf:/mnt/paramter/*");
					Util.sudoExec("cp:-rf:" + path + "*/mnt/paramter");
					Util.doSleep(2000);
					// Util.sudoExec("rm:-rf:/data/*");
					Util.doSleep(20);
					Util.sudoExec("sync");
					Util.doSleep(20);
					// Util.sudoExec("reboot");
					ret = true;
				}

				break;
			}
		}

		if (ret) {
			Toast.makeText(this, "Paramter udpate ok!", Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(this, "Paramter data not found!", Toast.LENGTH_SHORT)
					.show();
		}
		finish();

	}

	TextView mTV;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update_paramter);
		// mPm = getPackageManager();
		mTV = (TextView) findViewById(R.id.update_app_info);

		((Button) findViewById(R.id.update_mcu))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						doUpdateParamter();
					}
				});

		((Button) findViewById(R.id.back))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						finish();
					}
				});

	}

}