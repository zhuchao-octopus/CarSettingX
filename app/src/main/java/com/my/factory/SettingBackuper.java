package com.my.factory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.Util;

import android.os.Environment;
import android.os.FileUtils;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.provider.Settings;
import android.provider.Settings.Secure;

public class SettingBackuper {
    public final static String MCUKEY_RADIO_REGION = "mcukey_radio_region";
    public final static String MCUKEY_BRAKE = "mcukey_brake";
    public final static String MCUKEY_ILLUM = "mcukey_illum";
    public final static String MCUKEY_LED_COLOR = "mcukey_led_color";
    public final static String MCUKEY_BEEP = "mcukey_beep";
    public final static String MCUKEY_NAVI_MIX = "mcukey_navi_mix";
    public final static String MCUKEY_VCOM = "mcukey_vcom";
    public final static String MCUKEY_AMP = "mcukey_amp";
    public final static String MCUKEY_ACCILLUMIN = "mcukey_accIllumin";

    private final static String AK_SETTINGS_FILENAME = "ak_settings.txt";
    private final static String AK_MCU_KEY_INFOS_FILENAME = "mcu_key_info.txt";
    private final static String AK_MCU_KEY_INFO_NODE = "/sys/class/ak/source/key_infos";
    private final static String TOUCH_KEY_MAPPING_FILE = MachineConfig.VENDOR_DIR + ".touch_key_mapping";////MyCmd.VENDOR_DIR

    private Activity mActivity;
    private StorageManager mStorageManager;
    private Properties mProperties = new Properties();
    private int mRadioRegion;
    private int mBrake;
    private int mIllumination;
    private int mLedColor;
    private int mBeep;
    private int mNaviMix;
    private int mVcom;
    private int mAmp;
    private int mAccIllumin;

    public SettingBackuper(Activity act) {
        mActivity = act;
        mStorageManager = (StorageManager) mActivity.getSystemService(Activity.STORAGE_SERVICE);
    }

    public boolean doBackup() {
        boolean ret;
        mProperties = MachineConfig.exportProperties();
        mProperties.setProperty(MCUKEY_RADIO_REGION, Integer.toString(mRadioRegion));
        mProperties.setProperty(MCUKEY_BRAKE, Integer.toString(mBrake));
        mProperties.setProperty(MCUKEY_ILLUM, Integer.toString(mIllumination));
        mProperties.setProperty(MCUKEY_LED_COLOR, Integer.toString(mLedColor));
        mProperties.setProperty(MCUKEY_BEEP, Integer.toString(mBeep));
        mProperties.setProperty(MCUKEY_NAVI_MIX, Integer.toString(mNaviMix));
        mProperties.setProperty(MCUKEY_VCOM, Integer.toString(mVcom));
        mProperties.setProperty(MCUKEY_AMP, Integer.toString(mAmp));
        mProperties.setProperty(MCUKEY_ACCILLUMIN, Integer.toString(mAccIllumin));
        ret = backupProp();

        Util.do_exec("sync");
        return ret;
    }

    public boolean doRestore() {
        if (restoreProp()) {
            try {
                mRadioRegion = Integer.valueOf(mProperties.getProperty(MCUKEY_RADIO_REGION));
                mProperties.remove(MCUKEY_RADIO_REGION);
            } catch (Exception e) {
                return false;
            }
            try {
                mBrake = Integer.valueOf(mProperties.getProperty(MCUKEY_BRAKE));
                mProperties.remove(MCUKEY_BRAKE);
            } catch (Exception e) {
                return false;
            }
            try {
                mIllumination = Integer.valueOf(mProperties.getProperty(MCUKEY_ILLUM));
                mProperties.remove(MCUKEY_ILLUM);
            } catch (Exception e) {
                return false;
            }
            try {
                mLedColor = Integer.valueOf(mProperties.getProperty(MCUKEY_LED_COLOR));
                mProperties.remove(MCUKEY_LED_COLOR);
            } catch (Exception e) {
                return false;
            }
            try {
                mBeep = Integer.valueOf(mProperties.getProperty(MCUKEY_BEEP));
                mProperties.remove(MCUKEY_BEEP);
            } catch (Exception e) {
                return false;
            }
            try {
                mNaviMix = Integer.valueOf(mProperties.getProperty(MCUKEY_NAVI_MIX));
                mProperties.remove(MCUKEY_NAVI_MIX);
            } catch (Exception e) {
                return false;
            }
            try {
                mVcom = Integer.valueOf(mProperties.getProperty(MCUKEY_VCOM));
                mProperties.remove(MCUKEY_VCOM);
            } catch (Exception e) {
                return false;
            }
            try {
                mAmp = Integer.valueOf(mProperties.getProperty(MCUKEY_AMP));
                mProperties.remove(MCUKEY_AMP);
            } catch (Exception e) {
                return false;
            }
            try {
                mAccIllumin = Integer.valueOf(mProperties.getProperty(MCUKEY_ACCILLUMIN));
                mProperties.remove(MCUKEY_ACCILLUMIN);
            } catch (Exception e) {
                return false;
            }
            MachineConfig.importProperties(mProperties);
            Util.do_exec("sync");
            return true;
        } else {
            return false;
        }
    }

    public void setRadioRegion(int value) {
        mRadioRegion = value;
    }

    public int getRadioRegion() {
        return mRadioRegion;
    }

    public void setBrake(int value) {
        mBrake = value;
    }

    public int getBrake() {
        return mBrake;
    }

    public void setIllum(int value) {
        mIllumination = value;
    }

    public int getIllum() {
        return mIllumination;
    }

    public void setLedColor(int value) {
        mLedColor = value;
    }

    public int getLedColor() {
        return mLedColor;
    }

    public void setBeep(int value) {
        mBeep = value;
    }

    public int getBeep() {
        return mBeep;
    }

    public void setNaviMix(int value) {
        mNaviMix = value;
    }

    public int getNaviMix() {
        return mNaviMix;
    }

    public void setVcom(int value) {
        mVcom = value;
    }

    public int getVcom() {
        return mVcom;
    }

    public void setAmp(int value) {
        mAmp = value;
    }

    public int getAmp() {
        return mAmp;
    }

    public void setAccIllumin(int value) {
        mAccIllumin = value;
    }

    public int getAccIllumin() {
        return mAccIllumin;
    }

    private String getValidExternalStorage() {
        String path = "";
        ///StorageVolume[] allExtVol = mStorageManager.getVolumeList();
        StorageVolume[] allExtVol = mStorageManager.getStorageVolumes().toArray(new StorageVolume[0]);
        for (StorageVolume v : allExtVol) {
            if (v.isEmulated()) {
                // internal sdcard, ignore it.
                continue;
            }

            ///if (Environment.MEDIA_MOUNTED.equals(mStorageManager.getVolumeState(v.getPath()))) {
            if (Environment.MEDIA_MOUNTED.equals(v.getState())) {
                //path = v.getPath();
                path = Objects.requireNonNull(v.getDirectory()).getPath();
                break;
            }
        }
        return path;
    }

    private boolean backupProp() {


        String path = getValidExternalStorage();
        if ((null == path) || (path.isEmpty())) {
            return false;
        }
        Util.copyFile(TOUCH_KEY_MAPPING_FILE, path + "/.touch_key_mapping");

        boolean ret = false;
        String mcuKeyInfoPath = path + "/" + AK_MCU_KEY_INFOS_FILENAME;
        path = path + "/" + AK_SETTINGS_FILENAME;
        File file = new File(path);
        try {
            if (!file.exists()) {
                file.createNewFile();
                ///FileUtils.setPermissions(path, FileUtils.S_IRWXU | FileUtils.S_IRWXG | FileUtils.S_IRWXO, -1, -1);
            } else {
            }
            ///FileUtils.setPermissions(path, FileUtils.S_IRWXU | FileUtils.S_IRWXG | FileUtils.S_IRWXO, -1, -1);

            FileOutputStream out = new FileOutputStream(file);
            mProperties.store(out, "");
            out.close();
            Util.copyFile(AK_MCU_KEY_INFO_NODE, mcuKeyInfoPath);
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
            ret = false;
        }
        return ret;
    }

    private boolean restoreProp() {
        String path = getValidExternalStorage();
        if ((null == path) || (path.isEmpty())) {
            return false;
        }

        boolean ret = false;
        String mcuKeyInfoPath = path + "/" + AK_MCU_KEY_INFOS_FILENAME;

        Util.copyFile(mcuKeyInfoPath, AK_MCU_KEY_INFO_NODE);

        Util.copyFile(path + "/.touch_key_mapping", TOUCH_KEY_MAPPING_FILE);

        path = path + "/" + AK_SETTINGS_FILENAME;
        InputStream inputStream = null;
        File configFile = new File(path);
        if (configFile.exists()) {
            try {
                inputStream = new FileInputStream(configFile);
                mProperties.clear();
                mProperties.load(inputStream);
                inputStream.close();
                ret = true;
            } catch (Exception e) {
                e.printStackTrace();
                ret = false;
            }
        }
        return ret;
    }
}
