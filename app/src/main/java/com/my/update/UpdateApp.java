package com.my.update;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.octopus.android.carsettingx.R;

public class UpdateApp extends Activity {
    private static final String APK_UPDATE_PATH = "/extsd/update_apk/";
    private static final String APK_SYSTEM_PATH = "/system/app/";
    private PackageManager mPm;
    private TextView mTV;

    private final String[] mUpdateApkName = new String[]{"OUT.apk", "CarApp.apk", "Audio.apk", "Video.apk", "Radio.apk", "BT.apk", "DVD.apk", "EQ.apk", "iPod.apk", "TV.apk", "Settings.apk", "Launcher2.apk", "FileManager.apk", "KeyStudy.apk", "AuxIn.apk"};

    private int rebootTime = -1;
    private final Handler mRebootHandler = new Handler(Objects.requireNonNull(Looper.myLooper())) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (rebootTime == 0) {
                do_exec("sudo ak47ak47 sync");
                do_exec("sudo ak47ak47 reboot");
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_app);
        mPm = getPackageManager();
        mTV = (TextView) findViewById(R.id.update_app_info);

        ((Button) findViewById(R.id.update_mcu)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                update();
                if (mIfUpdate == 2) {
                    rebootTime = 3;
                    mRebootHandler.sendEmptyMessage(0);
                } else if (mIfUpdate == 0) {
                    mTV.setText(getString(R.string.no_update_app_text));
                } else if (mIfUpdate == 1) {
                    mTV.setText(getString(R.string.update_new_text));
                }
            }
        });

        ((Button) findViewById(R.id.back)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        /// update(); // test
        /// getUpdateApkVersion(APK_SYSTEM_PATH+"Music.apk");
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

    private boolean isUpdateApk(String name) {
        for (String file : mUpdateApkName) {
            if (file.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNeedUdate(String apk) {
        int updateVerion = getUpdateApkVersion(APK_UPDATE_PATH + apk);
        int systemVerion = getUpdateApkVersion(APK_SYSTEM_PATH + apk);
        return (updateVerion > systemVerion);
    }

    private String apkToOdex(String name) {
        String s = name.replace(".apk", ".odex");
        return s;
    }

    private int mIfUpdate = 0;

    private void updateApk(String name) {
        if (isUpdateApk(name) && isNeedUdate(name)) {
            if (mIfUpdate == 0) {
                mIfUpdate = 1;
            }

            String pn = getUpdateApkPackageName(APK_SYSTEM_PATH + name);
            if (pn != null) {
                if (mIfUpdate == 1) {
                    mIfUpdate = 2;
                }
                do_exec("sudo ak47ak47 busybox mount -o remount,rw /system");
                do_exec("sudo ak47ak47 busybox cp " + APK_UPDATE_PATH + name + " " + APK_SYSTEM_PATH + name);
                do_exec("sudo ak47ak47 busybox cp " + APK_UPDATE_PATH + apkToOdex(name) + " " + APK_SYSTEM_PATH + apkToOdex(name));

                //delete cache
                do_exec("sudo ak47ak47 busybox rm -r /data/data/" + pn);
                do_exec("sudo ak47ak47 busybox rm -r /data/dalivk-cache/*");
            }
        }
    }

    private void update() {
        File f = new File(APK_UPDATE_PATH);
        if (f.exists()) {
            File[] files = f.listFiles();
            if(files == null) return;
            for (File file : files) {
                updateApk(file.getName());
            }
        }
    }

    private int getUpdateApkVersion(String path) {
        PackageInfo info = mPm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            return info.versionCode;
        }
        return -1;
    }

    private String getUpdateApkPackageName(String path) {
        PackageInfo info = mPm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            return info.packageName;
        }
        return null;
    }

    // private int getSystemApkVersion(String packageName) {
    // PackageInfo info;
    // try {
    // info = mPm.getPackageInfo(packageName, 0);
    // if (info != null) {
    // return info.versionCode;
    // }
    // } catch (Exception e) {
    //
    // }
    // return -1;
    // }

}