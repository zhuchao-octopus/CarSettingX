package com.my.factory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.my.hardware.setting.HWSetting;
import com.octopus.android.carsettingx.R;

public class SettingsPublic extends Activity {
	private final static String TAG = "MYSettingsPublic";
	private static final String VCHANNEL = "/sys/class/ak/volume/vchannel";
	@Override
	protected void onPause() {
		finish();
		super.onPause();
	}
	
	private int getValue(String file) {
		FileReader fw;
		int value = 0;
		try {
			fw = new FileReader(file);
			value = fw.read();
			Log.e(TAG, " channel value = " + value);
			value = value - 48;
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return value;
	}

	private void setValue(String value, String file) {
		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
			out.write(Byte.valueOf(value) + 48);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	Dialog radioRegionSettingDialog() {
		int select = getValue(VCHANNEL);;
		return new AlertDialog.Builder(this)
				.setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						finish();
					}
				})
				.setTitle(R.string.navigation_channel_choice)
				.setSingleChoiceItems(
						getResources().getStringArray(
								R.array.navigation_sound_channel_entries), select,
						new OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								setValue(String.valueOf(which), VCHANNEL);
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								finish();
							}
						}).create();
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		return radioRegionSettingDialog();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//showDialog(0);
		radioRegionSettingDialog().show();
	}
}
