package com.my.logo;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.octopus.android.carsettingx.R;

public class LogoActivity extends Activity {
	private final static String TAG = "LogoActivity";
	private final static String SYS_LOGO_DIRECTORY = "/udisk/logo/";
	private final static String SYS_LOGO_DIRECTORY2 = "/system/etc/logo/";
	private final static String USER_LOGO_DIRECTORY = "/extsd/logo/";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		//to do more better....
//		int ret = do_exec("sudo2 74ka write_logo " + "extsd/logo.ppm");
//		if (ret == 0){
//			Toast.makeText(this, "update extsd/logo.ppm OK!", Toast.LENGTH_SHORT).show();
//		} else {
//			Toast.makeText(this, "update extsd/logo.ppm FAIL!", Toast.LENGTH_SHORT).show();
//		}
//		finish();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.logo);
		dispalyLogoScreen();
	}
//
	public void processOnClick(View view) {
		int id = view.getId();
		LogoImageView iv = (LogoImageView) findViewById(id);
		final String logoPath = (String) iv.getTag();
		if (logoPath != null) {
			LayoutInflater inflater = getLayoutInflater();
			View downloadView = inflater.inflate(R.layout.download, null);
			((LogoImageView) downloadView.findViewById(R.id.logo))
					.setImageDrawable(iv.getDrawable());
			((TextView) downloadView.findViewById(R.id.message))
					.setText(R.string.download_warning);

			new AlertDialog.Builder(this)
					.setTitle(R.string.download_title)
					.setView(downloadView)
					.setNeutralButton(R.string.ok, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (logoPath != null) {
								int ret = 1;
								if("SABRESD-MX6DQ".equals(Build.MODEL)){
									Log.d(TAG, "logoPath =" + logoPath);
									//ret = do_exec("sudo ak47ak47 dd if=" +logoPath + " of=/dev/block/mmcblk0 bs=1M seek=4");	//for test	
									ret = do_exec("sudo ak47ak47 write_logo "
											+ logoPath);		
								} else {
									String ppmPath = logoPath.replace("png", "ppm");
									Log.d(TAG, "logoPath =" + logoPath);
									Log.d(TAG, "ppmPath =" + ppmPath);

									ret = do_exec("sudo ak47ak47 write_logo "
											+ ppmPath);									
								}
								Log.d(TAG, "do_exec ret =" + ret);
								if (ret == 0) {
									Toast.makeText(LogoActivity.this,
											R.string.download_success,
											Toast.LENGTH_SHORT).show();
								} else {
									Toast.makeText(LogoActivity.this,
											R.string.download_fail,
											Toast.LENGTH_SHORT).show();
								}
								finish();
							}
						}
					})
					.setNegativeButton(R.string.cancel, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
						}
					}).create().show();
		}
	}

	private int do_exec(String cmd) {
		try {
			int err = Runtime.getRuntime().exec(cmd).waitFor();
			return err;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return -4;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}
//
	private void dispalyLogoScreen() {
		if (displaySystemLogoScreen()){
			displayUserLogoScreen();
		}
	}

	private boolean displaySystemLogoScreen() {
		boolean ret = true;
		String path = SYS_LOGO_DIRECTORY2;
		String logoFile = null;
		String[] logoList = getLogoFile(path);
		if (logoList == null) {
			logoList = getLogoFile(SYS_LOGO_DIRECTORY);
			path = SYS_LOGO_DIRECTORY;
			if (logoList == null){
				ret = false;
				logoList = getLogoFile(USER_LOGO_DIRECTORY);
				path = USER_LOGO_DIRECTORY;
				if (logoList == null){
					return ret;
				}
			}
		}

		for (int i = 0; i < logoList.length && i < 24; i++) {
			Log.d(TAG, "logo" + i + "=" + logoList[i]);
			LogoImageView iv = (LogoImageView) findViewById(R.id.logo1_1 + i);
			if (iv != null) {
				logoFile = path + logoList[i];
				iv.setImageDrawable(Drawable.createFromPath(logoFile));
				iv.setTag(logoFile);
			} else {
				Log.e(TAG, "vi == null,i=" + i);
			}
		}
		return ret;
	}

	private void displayUserLogoScreen() {
		String logoFile = null;
		String[] logoList = getLogoFile(USER_LOGO_DIRECTORY);
		if (logoList == null)
			return;
		for (int i = 0; i < logoList.length && i < 6; i++) {
			Log.d(TAG, "logo" + i + "=" + logoList[i]);
			LogoImageView iv = (LogoImageView) findViewById(R.id.logo5_1 + i);
			if (iv != null) {
				logoFile = USER_LOGO_DIRECTORY + logoList[i];
				iv.setImageDrawable(Drawable.createFromPath(logoFile));
				iv.setTag(logoFile);
			} else {
				Log.e(TAG, "vi == null,i=" + i);
			}
		}
	}

	private String[] getLogoFile(String directory) {
		File file = new File(directory);
		if (file.exists() == false || file.isDirectory() == false)
			return null;

		String[] logoList = file.list(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				
				if("SABRESD-MX6DQ".equals(Build.MODEL)){
				
					if (filename.toLowerCase().endsWith(".bmp")) {
						return true;
					}
				} else {
					if (filename.endsWith(".png")) {
						String ppmFile = (dir + "/" + filename).replace("png",
								"ppm");
						if (isExist(ppmFile)) {
							return true;
						}
					}
				}
				
				return false;
			}
		});
		return logoList;
	}

	public boolean isExist(String path) {
		File file = new File(path);
		return file.exists();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}
