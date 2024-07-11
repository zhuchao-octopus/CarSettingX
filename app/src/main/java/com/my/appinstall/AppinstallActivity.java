package com.my.appinstall;

import java.io.File;
import java.io.FileFilter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.octopus.android.carsettingx.R;

public class AppinstallActivity extends Activity {
	/** Called when the activity is first created. */
	private InstallService mBoundService;

	private ProgressBar mProgressBar;
	private int mAppCount;
	private TextView mAppName;

	private final static int INSTALL_INIT = 0x00;
	private final static int START_INSTALL = 0x01;
	private final static int INSTALLED = 0x02;

	private final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case INSTALL_INIT:
				Log.e("TAG", "count = " + msg.arg1);
				// mProgressBar.setIndeterminate(true);
				mProgressBar.setMax(msg.arg1);
				break;
			case START_INSTALL:
				Log.e("TAG", "START_INSTALL = " + msg.obj.toString());
				mAppName.setText(getString(R.string.start_install) + msg.obj.toString());
				break;
			case INSTALLED:
				if (msg.arg1 != 1) {
					mAppName.setText(msg.obj.toString() + getString(R.string.install_failed));
				} else {
					mAppName.setText(msg.obj.toString() + getString(R.string.install_success));
				}
				int count = mProgressBar.getProgress() + 1;
				Log.e("TAG", "Progress = " + count);
				mProgressBar.setProgress(count);
				if (count == mAppCount) {
					mAppName.setText(getString(R.string.install_over));
				}
				break;
			}
		}
	};

	FileFilter mFileFilter = new FileFilter() {

		public boolean accept(File pathname) {
			Log.i("Inatall apk", pathname.getName());
			if (pathname.getName().endsWith(".apk")) {
				return true;
			} else
				return false;
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.auto_install_app);
		Button button = (Button) findViewById(R.id.install);
		button.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				
				mStrSdcard = "/mnt/extsd/apk/";
				mStrSdcard2 = "/mnt/extsd2/apk/";
				startService(new Intent(AppinstallActivity.this, InstallService.class));
				bindService(new Intent(AppinstallActivity.this, InstallService.class), mConnection, Context.BIND_AUTO_CREATE);
			}
		});
		
		button = (Button) findViewById(R.id.install2);
		button.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				mStrSdcard = "/mnt/sdcard/apk/";
				mStrSdcard2 = null;
				startService(new Intent(AppinstallActivity.this, InstallService.class));
				bindService(new Intent(AppinstallActivity.this, InstallService.class), mConnection, Context.BIND_AUTO_CREATE);
			}
		});

		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
		mAppName = (TextView) findViewById(R.id.textView1);
		TextView view = (TextView) findViewById(R.id.textView2);
		view.setText(R.string.install_warning);
	}
	private String mStrSdcard = null;
	private String mStrSdcard2 = null;
	public static String mShortCut = null;
	
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. Because we have bound to a explicit
			// service that we know is running in our own process, we can
			// cast its IBinder to a concrete class and directly access it.
			Log.i("activity", "bindService");
			mBoundService = ((InstallService.LocalBinder) service).getService();
			String filename = mStrSdcard;
			Log.i("Inatall apk", filename+"onClick");
			File dir = new File(filename);
			if (dir.exists()) {
				Log.i("Inatall apk", "dir exists");
				Log.i("Inatall apk", "dir =" + dir.getPath());
			} else if((mStrSdcard2 != null) && (new File(mStrSdcard2).exists())) { 
				dir = new File(mStrSdcard2);
			}else{
				Log.i("Inatall apk", "dir not exists");
			}
			File[] files = dir.listFiles(mFileFilter);
			if(files!=null){
			mAppCount = files.length;
			mHandler.obtainMessage(INSTALL_INIT, files.length, 0).sendToTarget();
			mBoundService.setListener(listener);
			mBoundService.installApks(files);
			}else{
			    Toast.makeText(AppinstallActivity.this,
                        R.string.no_files, Toast.LENGTH_LONG).show();
			}
			// Tell the user about this for our demo.
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			// Because it is running in our same process, we should never
			// see this happen.
			mBoundService = null;
		}
	};

	@Override
	protected void onDestroy() {
	    if(mBoundService!=null)
		unbindService(mConnection);
		super.onDestroy();
	}

	InstallListener listener = new InstallListener() {

		public void startInstall(String name) {
			Log.e("TAG", "startInstall " + name);
			Message msg = new Message();
			msg.obj = name;
			msg.what = START_INSTALL;
			mHandler.sendMessage(msg);
			// mHandler.obtainMessage(INSTALLED,0, 0).sendToTarget();
		}

		public void installed(String name, int code) {
			Log.e("TAG", "Installed " + name);
			Message msg = new Message();
			msg.obj = name;
			msg.what = INSTALLED;
			msg.arg1 = code;
			mHandler.sendMessage(msg);
			// mHandler.obtainMessage(INSTALLED,0, 0).sendToTarget();

		}
	};

}