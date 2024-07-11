package com.my;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;

import com.common.util.MyCmd;
import com.common.util.MachineConfig;
import com.common.util.Util;
import com.common.util.AKProperty;

import android.os.Environment;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.my.factory.FactoryAudioGainFragment;
import com.octopus.android.carsettingx.R;

public class BR extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
            //			Log.d("BR", "filemanager ACTION_MEDIA_MOUNTED");
            String path = intent.getData().toString().substring("file://".length());
            mThis = context;
            updateTouchScreenConfig(path);
            if (path != null && path.contains("USBdisk")) {
                writeCPUID(context, path);
                androidUpdate(path);
            }
        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
            String path = intent.getData().toString().substring("file://".length());
            //			Log.d("BR", "filemanager UpdatePath=" + mUpdatePath + ", path=" + path);
            try {
                if (mUpdatePath != null && mUpdatePath.startsWith(path)) {
                    if (mUpdateDialog != null && mUpdateDialog.isShowing()) mUpdateDialog.dismiss();
                }
                if (mTouchPath != null && mTouchPath.startsWith(path)) {
                    if (mTouchDialog != null && mTouchDialog.isShowing()) mTouchDialog.dismiss();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (intent.getAction().equals("com.my.car.service.BROADCAST_CAR_REVERSE_SEND")) {
            try {
                int cmd = intent.getIntExtra(MyCmd.EXTRA_COMMON_CMD, -1);
                int data = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, -1);
                if (cmd == MyCmd.Cmd.REVERSE_STATUS && data == 1) {
                    if (mUpdateDialog != null && mUpdateDialog.isShowing()) mUpdateDialog.dismiss();
                    if (mTouchDialog != null && mTouchDialog.isShowing()) mTouchDialog.dismiss();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            initSystemSetting(context);
        }
    }

    private void initSystemSetting(Context c) {
        Log.d("BR", "initSystemSetting!!");
        FactoryAudioGainFragment.initMicGain(c);
    }
    //	private Toast makeModeToast(Context mContext, String s) {
    //		Toast t = new Toast(mContext);
    //
    //		LayoutInflater inflate = (LayoutInflater) mContext
    //				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    //		View v = inflate.inflate(R.layout.my_dialog_info, null);
    //
    //		((TextView) v.findViewById(R.id.dialog_title)).setText(s);
    //
    //		t.setView(v);
    //		t.setDuration(Toast.LENGTH_LONG);
    //		t.setGravity(Gravity.CENTER, 0, 0);
    //		t.show();
    //		return t;
    //	}

    private void writeCPUID(Context context, String path) {
        String configPath = path + "/android_cpuid.txt";
        Util.doSleep(1000);
        File f = new File(configPath);

        AlertDialog.Builder builder = new AlertDialog.Builder(mThis);
        String title = String.format(mThis.getResources().getString(R.string.update_system), configPath);

        builder.setNegativeButton("Cancel", null);


        AlertDialog dialog = null;


        //		/*final AlertDialog */mUpdateDialog = builder.create();
        // 在dialog show前添加此代码，表示该dialog属于系统dialog。
        //		mUpdateDialog.getWindow().setType(
        //				(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));

        //		mMyDialogUpdate.setStyle(MyDialog.MYDIALOG_STYLE_TYPE_SYSTEM_ALERT);
        //		mUpdateDialog.getWindow().setType(
        //				(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        Log.d("FileManager", "writeCPUID:" + configPath + ":" + f.exists());
        try {
            if (!f.exists()) {
                //				Log.d("FileManager", "writeCPUID: not id file");
                return;
                //				f.createNewFile();
            }
            String deviceId = Util.getProperty("ro.serialno");

            ;//Settings.Secure.getString(
            //context.getContentResolver(), Settings.Secure.ANDROID_ID);

            FileInputStream inputStream = new FileInputStream(configPath);
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            String str = dataInputStream.readLine();
            int i = 0;
            while (str != null) {
                if (str.equalsIgnoreCase(deviceId)) {
                    Log.d("FileManager", "writeCPUID: sucess already exist!!");

                    dataInputStream.close();
                    inputStream.close();
                    //					makeModeToast(context, deviceId);

                    builder.setTitle("already exist");
                    builder.setMessage(deviceId);
                    dialog = builder.create();
                    dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
                    dialog.show();
                    //					mMyDialogUpdate.setText("already exist", deviceId);
                    //					mMyDialogUpdate.show();
                    return;
                }
                ++i;
                Log.e("FileManager", i + "writeCPUID: check!!" + str);
                str = dataInputStream.readLine();

            }
            dataInputStream.close();
            inputStream.close();


            //			Util.setFileValue(configPath, deviceId);

            FileOutputStream is = new FileOutputStream(configPath, true);
            DataOutputStream dis = new DataOutputStream(is);

            if (i != 0) {
                deviceId = "\r\n" + deviceId;
            }

            dis.write(deviceId.getBytes());
            dis.flush();
            is.flush();
            is.getFD().sync();
            dis.close();
            is.close();
            Log.d("FileManager", "writeCPUID: sucess!!");
            //			makeModeToast(context, deviceId);
            builder.setTitle("new id");
            builder.setMessage(deviceId);
            dialog = builder.create();
            dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
            dialog.show();
            //			mMyDialogUpdate.setText("new id", deviceId);
            //			mMyDialogUpdate.show();
        } catch (Exception e) {

            Log.d("FileManager", "writeCPUID: er!!" + e);
        }

    }

    private static String mUpdateAndroidPath;
    private static AlertDialog mUpdateDialog = null;
    private static String mUpdatePath = null;

    private String getUpdateZipName() {
        String name = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_UPDATE_NAME);
        if (name == null) {
            name = "kupdate.zip";
        }
        return name;
    }

    private void androidUpdate(String path) {
        if (Util.isRKSystem()) {
            Util.doSleep(1000);
            if (Util.isPX6()) {
                String reverse = Util.getProperty(AKProperty.REVERSE);
                if (reverse != null && reverse.equals("1")) return;
            }
            //			String configPath = path + "/kupdate.zip";
            String configPath = path + "/" + getUpdateZipName();
            File f = new File(configPath);
            Log.d("FileManager", "update:" + configPath + ":" + f.exists());
            if (!f.exists()) {
                configPath = path + "/parameter.txt";
                if (!f.exists()) {
                    return;
                }
                return;
            }
            mUpdateAndroidPath = path;
            AlertDialog.Builder builder = new AlertDialog.Builder(mThis);
            String title = String.format(mThis.getResources().getString(R.string.update_system), configPath);
            builder.setTitle(title);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    doAndroidUpdate(mUpdateAndroidPath);
                }
            });

            builder.setNegativeButton("Cancel", null);

            try {
                if (mUpdateDialog != null && mUpdateDialog.isShowing()) mUpdateDialog.dismiss();
            } catch (Exception ignored) {
            }

            /*final AlertDialog */
            mUpdateDialog = builder.create();
            // 在dialog show前添加此代码，表示该dialog属于系统dialog。
            mUpdateDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));

            mUpdateDialog.show();

            mUpdatePath = configPath;
        }

    }


    private void doAndroidUpdate(String path) {
        if (Util.isRKSystem()) {
            Util.sudoExec("rm:/cache/recovery/command");
            // Util.sudoExec("echo:\"--ak_update\">/cache/recovery/command");
            String sd_path = Environment.getExternalStorageDirectory().toString();
            sd_path += "/command_to_recovery";
            File file = new File(sd_path);
            try {
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                FileOutputStream out = new FileOutputStream(file);
                out.write("--ak_update".getBytes());
                // dis.write(value.getBytes());
                out.flush();
                out.close();
                Util.sudoExec("sync");
            } catch (Exception e) {
                e.printStackTrace();
            }
            Util.doSleep(500);
            // Util.sudoExec("cp:" + path + "/" + ANDROID_UPDATE_GUIDE_FILE_NAME
            // + ":/cache/recovery/command");

            Util.sudoExec("cp:" + sd_path + ":/cache/recovery/command");
            Util.sudoExec("sync");

            Util.doSleep(100);
            Util.sudoExec("chmod:666:/cache/recovery/command");

            Util.doSleep(500);
            // }
            Util.sudoExec("reboot:recovery");
        }
    }

    private Context mThis;

    private final String PROC_TOUCH_CONFIG = "/proc/gt9xx_config";
    private final String TOUCH_CONFIG_FILE = "/touch_config.cfg";
    private static AlertDialog mTouchDialog = null;
    private static String mTouchPath = null;

    private void updateTouchScreenConfig(String path) {
        Util.doSleep(1000);
        final String configPath = path + TOUCH_CONFIG_FILE;
        File f = new File(configPath);
        if (!f.exists()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mThis);
        String title = String.format(mThis.getResources().getString(R.string.update_touch_config), configPath);
        builder.setTitle(title);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                FileReader fr = null;
                boolean ok = false;
                try {
                    fr = new FileReader(configPath);
                    BufferedReader reader = new BufferedReader(fr, 1280);
                    String config = reader.readLine();
                    Util.setFileValue(PROC_TOUCH_CONFIG, config);
                    reader.close();
                    fr.close();
                    ok = true;
                } catch (Exception e) {
                }

                if (ok) {
                    Toast.makeText(mThis, "Update Ok!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mThis, "Update Fail!", Toast.LENGTH_LONG).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);

        try {
            if (mTouchDialog != null && mTouchDialog.isShowing()) mTouchDialog.dismiss();
        } catch (Exception e) {
        }

        /*final AlertDialog */
        mTouchDialog = builder.create();
        // 在dialog show前添加此代码，表示该dialog属于系统dialog。
        mTouchDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));

        mTouchDialog.show();

        mTouchPath = configPath;
    }
}
