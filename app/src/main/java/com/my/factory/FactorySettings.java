package com.my.factory;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Build;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.text.method.Touch;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.common.util.AppConfig;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.common.util.UtilSystem;

import com.my.filemanager.FileManagerActivity.StorageInfo;
import com.octopus.android.carsettingx.R;

public class FactorySettings extends Activity {
    private static final String TAG = "FactorySettings";
    private static final boolean DBG = false;

    private ModelSettingsFragment mModelSettingsFragment = new ModelSettingsFragment();
    private SuperSettingsFragment mSuperSettingsFragment = new SuperSettingsFragment();
    private FactoryAudioGainFragment mAudioChannelGainFragment = new FactoryAudioGainFragment();
    private LogoFragment mLogoFragment = new LogoFragment();
    //	private VCOMFragment mVCOMFragment = new VCOMFragment();
    private AppHideFragment mAppHideFragment = new AppHideFragment();
    private FactorySettingsFragment[] mFactorySettingsFragment = new FactorySettingsFragment[2];
    private FragmentManager mFragmentManager;
    private InputMethodManager mInputManager;

    public static boolean mIsTest = false;
    public Activity mThis;

    //Tab
    public final static int TAB_TYPE_NONE = 100;
    public final static int TAB_TYPE_FUNC = 0;
    public final static int TAB_TYPE_RADIO = 1;
    public final static int TAB_TYPE_KEY = 2;
    public final static int TAB_TYPE_CANBUS = 3;
    public final static int TAB_TYPE_SOUND = 4;
    public final static int TAB_TYPE_APP = 5;
    public final static int TAB_TYPE_OTHER = 6;
    public final static int TAB_TYPE_SUPER = 9;    //special type, deal same as TAB_TYPE_OTHER

    private static final int MAX_TABS = TAB_TYPE_OTHER + 1;
    private RelativeLayout mTabLayout;
    private LinearLayout mButtonLayout;
    private LinearLayout mTab;
    private View mTabItem[];
    private TextView mTabText[];
    private ImageView mTabLine[];
    private Button mBtnExit, mBtnBackup, mBtnRestore;
    private int mCurrentTabIndex = TAB_TYPE_FUNC;
    private int mCurrentPreferenceIndex = TAB_TYPE_FUNC;

    private int iFactorySettingsFragmentIndex = 0;

    private int getFactorySettingsFragmentIndex() {
        if (iFactorySettingsFragmentIndex == 0) {
            iFactorySettingsFragmentIndex = 1;
            return 0;
        } else {
            iFactorySettingsFragmentIndex = 0;
            return 1;
        }
    }

    private void initTab(boolean hide) {
        mCurrentPreferenceIndex = mCurrentTabIndex = TAB_TYPE_FUNC;
        mButtonLayout = (LinearLayout) findViewById(R.id.id_button_bar);
        mTabLayout = (RelativeLayout) findViewById(R.id.tab_layout);
        if (hide) {
            mTabLayout.setVisibility(View.GONE);
            if (mButtonLayout != null) mButtonLayout.setVisibility(View.GONE);
            return;
        }
        mTab = (LinearLayout) findViewById(R.id.main_tab_layout);
        mTab.removeAllViews();
        mTab.setWeightSum(MAX_TABS);
        mTabItem = new View[MAX_TABS];
        mTabText = new TextView[MAX_TABS];
        mTabLine = new ImageView[MAX_TABS];
        for (int i = 0; i < MAX_TABS; i++) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.factory_settings_tab_item_left, mTab, false);
            view.setTag("" + i);
            mTab.addView(view);
            mTabItem[i] = (View) view.findViewById(R.id.tab_item);
            mTabText[i] = (TextView) view.findViewById(R.id.tab_textview);
            mTabLine[i] = (ImageView) view.findViewById(R.id.tab_selected_line);
            mTabItem[i].setOnClickListener(mTabOnClickListener);
        }
        String[] tabName = getResources().getStringArray(R.array.factory_settings_tab_item_name);
        if (tabName != null && tabName.length == MAX_TABS) {
            for (int i = 0; i < MAX_TABS; i++)
                mTabText[i].setText(tabName[i]);
        }

        mBtnExit = (Button) findViewById(R.id.exit);
        mBtnBackup = (Button) findViewById(R.id.backup_settings);
        mBtnRestore = (Button) findViewById(R.id.restore_settings);
        if (mBtnExit != null) mBtnExit.setOnClickListener(mButtonOnClickListener);
        if (mBtnBackup != null) mBtnBackup.setOnClickListener(mButtonOnClickListener);
        if (mBtnRestore != null) mBtnRestore.setOnClickListener(mButtonOnClickListener);

        if (GeneralSettings.isExtHide(GeneralSettings.SETTINGS_HIDE_FACTORY_VOLUME)) {
            mTab.setWeightSum(MAX_TABS - 1);
            mTab.removeViewAt(4);
        }
    }

    private void highlightTab(int index) {
        //		int color = Color.rgb(0x66,0x99,0x99);
        mCurrentPreferenceIndex = index;    //record canbus and super don't be finish() in onPause
        if (index == TAB_TYPE_SUPER) index = TAB_TYPE_OTHER;
        mCurrentTabIndex = index;

        if (index != TAB_TYPE_CANBUS) {
            for (int i = 0; i < MAX_TABS; i++) {
                if (i == mCurrentTabIndex) {
                    mTabLine[i].setVisibility(View.VISIBLE);
                    //					mTabLine[i].setBackgroundColor(color);
                    //				mTabText[i].setTextColor(color);
                    //					mTabItem[i].setBackgroundColor(color);
                } else {
                    //					mTabItem[i].setBackgroundColor(Color.rgb(0x33,0x33,0x33));
                    //					mTabLine[i].setBackgroundColor(Color.WHITE);
                    //				mTabText[i].setTextColor(Color.WHITE);
                    mTabLine[i].setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private OnClickListener mTabOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            try {
                String tag = (String) v.getTag();
                int index = Integer.valueOf(tag);
                if (index >= 0 && index < MAX_TABS) {
                    highlightTab(index);

                    if (index == TAB_TYPE_FUNC) {
                    } else if (index == TAB_TYPE_RADIO) {
                    } else if (index == TAB_TYPE_KEY) {
                    } else if (index == TAB_TYPE_CANBUS) {
                        UtilSystem.doRunActivity(FactorySettings.this, "com.my.factory.intent.action.CanboxSettings");
                        return;
                    } else if (index == TAB_TYPE_SOUND) {
                    } else if (index == TAB_TYPE_APP) {
                        replaceFragment(R.id.id_genernal_setting_fragment, (Fragment) mAppHideFragment, false, -1);
                        //						onSuperSettingsClicked(index);
                        return;
                    } else if (index == TAB_TYPE_OTHER) {
                    } else {
                        return;
                    }
                    //					mFactorySettingsFragment = new FactorySettingsFragment();
                    replaceFragment(R.id.id_genernal_setting_fragment, (Fragment) mFactorySettingsFragment[getFactorySettingsFragmentIndex()], false, index);
                }
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
    };

    private OnClickListener mButtonOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            int id = v.getId();
            if (id == R.id.exit) {
                finish();
            } else if (id == R.id.backup_settings) {
                FactorySettingsFragment.doBackup(FactorySettings.this);
            } else if (id == R.id.restore_settings) {
                FactorySettingsFragment.doRestore(FactorySettings.this);
            }
        }
    };

    void onSuperSettingsClicked(final int index) {
        final Toast toast = Toast.makeText(this, getResources().getString(R.string.password_error), Toast.LENGTH_SHORT);
        final EditText ev = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.dialog_alert_icon);
        builder.setTitle(R.string.input_code);
        builder.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // TODO Auto-generated method stub
                String str = ev.getText().toString().toLowerCase();
                if (str != null && str.equals(PW_SUPER_SETTINGS)) {
                    highlightTab(index);
                    //							mFactorySettingsFragment = new FactorySettingsFragment();
                    replaceFragment(R.id.id_genernal_setting_fragment, (Fragment) mFactorySettingsFragment[getFactorySettingsFragmentIndex()], false, index);
                } else {
                    toast.setText(R.string.password_error);
                    toast.show();
                }
            }
        });
        builder.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).create();
        builder.setView(ev);
        builder.create().show();
        ev.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        ev.setInputType(InputType.TYPE_CLASS_NUMBER);
        ev.setTransformationMethod(PasswordTransformationMethod.getInstance());
        builder.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface arg0) {
                mInputManager.hideSoftInputFromWindow(ev.getWindowToken(), 0);
            }
        });
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		/*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        mThis = this;

        mFactorySettingsFragment[0] = new FactorySettingsFragment();
        mFactorySettingsFragment[1] = new FactorySettingsFragment();


        String value = MachineConfig.getPropertyReadOnly("factroy_passwd");
        if (value != null && value.length() > 0) {
            PW_REQUEST_FACTORY = value;
        }

        PW_REQUEST_CUSTOMER_SERVICE_PASS = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_PASSWD_CUSTOMER_SERVICE_MODE);
        PW_REQUEST_CUSTOMER_SERVICE_PASS_EX = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_PASSWD_CUSTOMER_SERVICE_MODE_EX);

        //		if (MachineConfig.VALUE_SYSTEM_UI_KLD7_1992.equals(value)) {
        //			PW_REQUEST_FACTORY = PW_REQUEST_FACTORY_UI1992;
        //		}
        mInputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (mIsTest) {
            setContentView(R.layout.factory_settings_activity_left);
            initTab(false);
            highlightTab(mCurrentTabIndex);
            replaceFragment(R.id.id_genernal_setting_fragment, (Fragment) mFactorySettingsFragment[getFactorySettingsFragmentIndex()], false, mCurrentTabIndex);
            // test
        } else {

            if (DBG) {
                setContentView(R.layout.factory_settings_activity_left);
                initTab(false);
                highlightTab(mCurrentTabIndex);
                replaceFragment(R.id.id_genernal_setting_fragment, (Fragment) mFactorySettingsFragment[getFactorySettingsFragmentIndex()], false, mCurrentTabIndex);
            } else {
                doPasswordDialog();
            }
        }
    }

    private void doPasswordDialog() {
        final EditText ev = new EditText(this);
        final Toast t = Toast.makeText(this, getResources().getString(R.string.password_error), Toast.LENGTH_SHORT);
        AlertDialog ad = new AlertDialog.Builder(this).setIcon(R.drawable.dialog_alert_icon).setTitle(R.string.input_code).setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String str = ev.getText().toString().toLowerCase();
                if (PW_REQUEST_FACTORY.equals(str) || PW_REQUEST_FACTORY.equals(str)) {
                    setContentView(R.layout.factory_settings_activity_left);
                    initTab(false);
                    highlightTab(mCurrentTabIndex);
                    replaceFragment(R.id.id_genernal_setting_fragment, (Fragment) mFactorySettingsFragment[getFactorySettingsFragmentIndex()], false, mCurrentTabIndex);
                } else if ("888".equals(str)) {
                    setContentView(R.layout.factory_settings_activity_left);
                    initTab(true);
                    replaceFragment(R.id.id_genernal_setting_fragment, (Fragment) (new LogoFragment())/*mLogoFragment*/, false, -1);
                } else if ("8888".equals(str)) {
                    setContentView(R.layout.factory_settings_activity_left);
                    initTab(true);
                    replaceFragment(R.id.id_genernal_setting_fragment, (Fragment) (new LogoFragment())/*mLogoFragment*/, false, -1);
                } else if (PW_SUPER_SETTINGS.equals(str)) {
                    if (Util.isRKSystem()) {
                        setContentView(R.layout.factory_settings_activity_left);
                        initTab(true);
                        replaceFragment(R.id.id_genernal_setting_fragment, (Fragment) mSuperSettingsFragment, false, -1);
                    } else {
                        finish();
                    }
                } else if (PW_MODEL_MCU.equals(str)) {
                    setContentView(R.layout.factory_settings_activity_left);
                    initTab(true);
                    replaceFragment(R.id.id_genernal_setting_fragment, (Fragment) mModelSettingsFragment, false, -1);
                } else if ("927".equals(str)) {
									/*setContentView(R.layout.factory_settings_activity);
									initTab(true);
									replaceFragment(R.id.id_genernal_setting_fragment,
											(Fragment)mVCOMFragment, false, -1);*/
                    VCOMFloatView.getInstanse(getApplicationContext()).show();
                    VCOMFloatView.getInstanse(getApplicationContext()).onHome();
                    finish();
                } else if ("0927200".equals(str)) {
                    TouchInfoFloatView.getIntance(getApplicationContext()).show();
                    finish();
                } else if ("1866".equals(str)) {
                    toggleOtgToDevice(false);
                    finish();
                } else if ("2866".equals(str)) {
                    toggleOtgToDevice(true);
                    finish();
                } else if ("236".equals(str)) {
                    UtilSystem.doRunActivity(mThis, "com.ak.update", "com.ak.update.VehicleUpdate");
                } else if (str.equals(PW_REQUEST_CUSTOMER_SERVICE_PASS)) {
                    toggleCustomServiceMode();
                    finish();
                } else if (PW_REQUEST_CUSTOMER_SERVICE_PASS_EX != null && PW_REQUEST_CUSTOMER_SERVICE_PASS_EX.startsWith(str)) {
                    toggleCustomServiceModeEx(str);
                    finish();
                } else if ("111999".equals(str)) {
                    int id;
                    String s = MachineConfig.getProperty(MachineConfig.KEY_SAVE_DRIVER);
                    if ("1".equals(s)) {
                        MachineConfig.setProperty(MachineConfig.KEY_SAVE_DRIVER, "0");
                        MachineConfig.setProperty(MachineConfig.KEY_SAVE_DRIVER_PACKAGE, "");
                        id = R.string.save_driver_off;
                    } else {
                        MachineConfig.setProperty(MachineConfig.KEY_SAVE_DRIVER, "1");
                        id = R.string.save_driver_on;
                    }
                    Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
                    it.putExtra(MyCmd.EXTRA_COMMON_CMD, MachineConfig.KEY_SAVE_DRIVER);
                    sendBroadcast(it);

                    t.setText(id);
                    t.show();
                    finish();
                } else if ("10000".equals(str)) {
                    sendBroadcast(new Intent(MyCmd.BROADCAST_BT_UPDATE));

                } else if (PW_REQUEST_LOGCAT.equals(str)) {
                    logcat();
                    // AlertDialog d = new
                    // AlertDialog.Builder(
                    // mThis)
                    // .setTitle("BT Update?")
                    // .setPositiveButton(
                    // R.string.alert_dialog_ok,
                    // new
                    // DialogInterface.OnClickListener()
                    // {
                    // public void onClick(
                    // DialogInterface dialog,
                    // int whichButton) {
                    // sendBroadcast(new Intent(
                    // MyCmd.BROADCAST_BT_UPDATE));
                    // finish();
                    // }
                    // })
                    // .setNegativeButton(
                    // R.string.alert_dialog_cancel,
                    // new
                    // DialogInterface.OnClickListener()
                    // {
                    // public void onClick(
                    // DialogInterface dialog,
                    // int whichButton) {
                    // finish();
                    // }
                    // }).create();
                    //
                    // d.show();

                } else if (PW_REQUEST_LOGCAT.equals(str)) {
                    // logcat();
                } else if (PW_REQUEST_AUTO_TEST.equals(str)) {
                    Intent it = new Intent(MyCmd.BROADCAST_CMD_TO_CAR_SERVICE);
                    it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.AUTO_TEST_SHOW_UI);
                    it.setPackage(AppConfig.getCarServicePackageName(mThis));
                    sendBroadcast(it);
                    finish();
                } else if (PW_REQUEST_BT_VERSION.equals(str)) {
                    sendBroadcast(new Intent(MyCmd.BROADCAST_BT_VERSION));
                    finish();
                } else if (PW_REQUEST_UPDATE_IVT_LIB.equals(str)) {


                    UtilSystem.doRunActivity(mThis, "com.my.filemanager", "com.my.btupdate.ivt.MainActivity");
                    finish();
                } else if (PW_REQUEST_UPDATE_SYSTEM_APK.equals(str)) {


                    UtilSystem.doRunActivity(mThis, "com.my.filemanager", "com.my.updateapk.MainActivity");
                    finish();
                } else if (PW_TEST_TIMEZONE_AND_LOCAL.equals(str)) {

                    Locale locale = Locale.getDefault();
                    Log.d("ddc", "" + locale);
                    String s = locale.toString();

                    TimeZone t = TimeZone.getDefault();
                    Log.d("abc", "t:" + t);
                    s = s + "\r\n" + t.toString();

                    Toast.makeText(mThis, s, Toast.LENGTH_LONG).show();

                    finish();
                } else if (PW_UPDATE_PARAMTER.equals(str)) {
                    UtilSystem.doRunActivity(mThis, "com.my.update.intent.action.UpdateParamter");
                } else if (PW_SHOW_TRUE_VERSION.equals(str)) {

                    String true_version = Util.getFileString(MachineConfig.getParameterPath() + "ak47_update_hold.txt");
                    Toast.makeText(getApplicationContext(), true_version, Toast.LENGTH_LONG).show();
                    finish();

                } else if (PW_SHOW_SCREEN_SHOT.equals(str)) {
                    toggleScreenShot();

                } else if (PW_REQUEST_CANBOX_VERSION.equals(str)) {
                    Intent it = new Intent(MyCmd.BROADCAST_CMD_TO_CAR_SERVICE);
                    it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.REQUEST_CANBOX_VERSION);
                    it.setPackage(AppConfig.getCarServicePackageName(mThis));
                    sendBroadcast(it);
                    finish();
                } else if (PW_REQUEST_CANBOX_UPDATE.equals(str)) {
                    Intent it = new Intent(MyCmd.BROADCAST_CMD_TO_CAR_SERVICE);
                    it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.UPDATE_CANBOX);
                    it.setPackage(AppConfig.getCarServicePackageName(mThis));
                    sendBroadcast(it);
                    finish();
                } else if (PW_SHOW_DEBUGMSG.equals(str)) {
                    Intent it = new Intent(MyCmd.BROADCAST_CMD_TO_CAR_SERVICE);
                    it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.SHOW_DEBUG_MSG);
                    it.setPackage(AppConfig.getCarServicePackageName(mThis));
                    sendBroadcast(it);
                    finish();
                } else if (PW_VIOCE_ASSISTANT1.equals(str) || PW_VIOCE_ASSISTANT2.equals(str)) {
                    String enable = SystemConfig.getProperty(getApplicationContext(), SystemConfig.KEY_ENABLE_AK_VIOCE_ASSISTANT);
                    if (enable != null && enable.equals("1")) {
                        SystemConfig.setProperty(getApplicationContext(), SystemConfig.KEY_ENABLE_AK_VIOCE_ASSISTANT, "0");
                        Toast.makeText(getApplicationContext(), "VoiceAssistant disable", Toast.LENGTH_SHORT).show();
                    } else {
                        SystemConfig.setProperty(getApplicationContext(), SystemConfig.KEY_ENABLE_AK_VIOCE_ASSISTANT, "1");
                        Toast.makeText(getApplicationContext(), "VoiceAssistant enabled", Toast.LENGTH_SHORT).show();
                    }
                    try {
                        Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
                        it.putExtra(MyCmd.EXTRA_COMMON_CMD, "com.ak.speechrecog");
                        it.setPackage("com.ak.speechrecog");
                        sendBroadcast(it);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    finish();
                } else {
                    t.setText(R.string.password_error);
                    t.show();
                    finish();
                }
                // KEY_SAVE_DRIVER
            }
        }).setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        }).create();
        ;
        ad.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });

        ad.setView(ev);
        ad.show();

        ev.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        ev.setInputType(InputType.TYPE_CLASS_NUMBER);
        ev.setTransformationMethod(PasswordTransformationMethod.getInstance());
        // ev.getViewTreeObserver().addOnGlobalLayoutListener(new
        // ViewTreeObserver.OnGlobalLayoutListener() {
        // @Override
        // public void onGlobalLayout() {
        // mInputManager.showSoftInput(ev, 0);
        // }
        // });
        ad.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface arg0) {
                mInputManager.hideSoftInputFromWindow(ev.getWindowToken(), 0);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        Log.d("abc", "onNewIntent");
        doPasswordDialog();
    }

    private List<StorageInfo> listAllStorage(Context context) {
        ArrayList<StorageInfo> storages = new ArrayList<StorageInfo>();
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            Class<?>[] paramClasses = {};
            Method getVolumeList = StorageManager.class.getMethod("getVolumes", paramClasses);
            Object[] params = {};
            List<Object> VolumeInfo = (List<Object>) getVolumeList.invoke(storageManager, params);

            if (VolumeInfo != null) {
                for (Object volumeinfo : VolumeInfo) {

                    Method getPath = volumeinfo.getClass().getMethod("getPath", new Class[0]);

                    File path = (File) getPath.invoke(volumeinfo, new Object[0]);

                    Method getDisk = volumeinfo.getClass().getMethod("getDisk", new Class[0]);

                    Object diskinfo = getDisk.invoke(volumeinfo, new Object[0]);
                    int type = StorageInfo.TYPE_INTERAL;
                    if (diskinfo != null) {
                        Method isSd = diskinfo.getClass().getMethod("isSd", new Class[0]);

                        type = ((Boolean) isSd.invoke(diskinfo, new Object[0])) ? StorageInfo.TYPE_SD : StorageInfo.TYPE_USB;

                    }
                    StorageInfo si = new StorageInfo(path.toString(), type);
                    storages.add(si);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        storages.trimToSize();
        return storages;
    }

    private void toggleScreenShot() {
        int hide_screenshot = Settings.Global.getInt(getContentResolver(), "SETTINGS_HIDE_SCREENSHOT", -1);

        if (hide_screenshot == 0) {
            hide_screenshot = 1;
            Toast.makeText(this, "hide screenshot!", Toast.LENGTH_SHORT).show();
        } else {
            hide_screenshot = 0;
            Toast.makeText(this, "show screenshot!", Toast.LENGTH_SHORT).show();
        }
        Settings.Global.putInt(getContentResolver(), "SETTINGS_HIDE_SCREENSHOT", hide_screenshot);
        finish();
    }

    private void logcat() {
        // Util.do_exec("logcat > /sdcard/logcat.txt");
        // Util.setProperty("ctl.start", "logcat_service");

        List<StorageInfo> list = listAllStorage(this);
        String path = null;
        for (int i = 0; i < list.size(); ++i) {

            StorageInfo si = list.get(i);
            if (si.mType == StorageInfo.TYPE_USB && (si.mPath != null && si.mPath.indexOf("cdrom") < 0)) {
                // Util.sudoExec("logcat>"+si.mPath+"/logcat.txt");
                // Util.sudoExec("dmesg>"+si.mPath+"/dmesg.txt");
                // Util.do_exec("logcat>"+si.mPath+"/logcat.txt");
                Util.sudoExec("logtosdcard.sh");

                Util.doSleep(2000);
                File f = new File("/sdcard/logcat.txt");

                if (f.exists()) {
                    copyFile("/sdcard/dmesg.txt", si.mPath + "/dmesg.txt");
                    Util.doSleep(1000);
                    copyFile("/sdcard/logcat.txt", si.mPath + "/logcat.txt");
                    Toast.makeText(this, "Logcat OK!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Logcat Fail!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }

        Toast.makeText(this, "No USB Disk!", Toast.LENGTH_SHORT).show();

    }

    private boolean copyFile(String oldPath, String newPath) {
        try {
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { // 閺傚洣娆㈢�涙ê婀弮锟�
                Util.sudoExec("cp:" + oldPath + ":" + newPath);
                return true;
            }
        } catch (Exception e) {
            System.out.println("婢跺秴鍩楅崡鏇氶嚋閺傚洣娆㈤幙宥勭稊閸戞椽鏁�");
            e.printStackTrace();

        }
        return false;

    }


    private static final String PW_REQUEST_UPDATE_IVT_LIB = "09271111";
    private static final String PW_REQUEST_UPDATE_SYSTEM_APK = "09272222";

    private static final String PW_REQUEST_FACTORY_NARMAL = "126";
    private static final String PW_REQUEST_FACTORY_UI1992 = "1234";

    private static final String PW_TEST_TIMEZONE_AND_LOCAL = "1689";

    private static String PW_REQUEST_FACTORY = PW_REQUEST_FACTORY_NARMAL;
    private static String PW_REQUEST_CUSTOMER_SERVICE_PASS = null;
    private static String PW_REQUEST_CUSTOMER_SERVICE_PASS_EX = null;

    private final static String PW_REQUEST_LOGCAT = "0000";
    private final static String PW_REQUEST_BT_VERSION = "1111";
    private final static String PW_REQUEST_CANBOX_VERSION = "2222";
    private final static String PW_SHOW_SCREEN_SHOT = "3333";


    private final static String PW_SHOW_TRUE_VERSION = "4444";

    private final static String PW_REQUEST_CANBOX_UPDATE = "09274444";

    private final static String PW_SHOW_DEBUGMSG = "444455556666";
    private final static String PW_UPDATE_PARAMTER = "9999";


    private final static String PW_SUPER_SETTINGS = "0927999";

    private final static String PW_MODEL_MCU = "092788";

    private final static String PW_REQUEST_AUTO_TEST = "0927666";

    private final static String PW_VIOCE_ASSISTANT1 = "9981688";
    private final static String PW_VIOCE_ASSISTANT2 = "9040";

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //		if (mCurrentPreferenceIndex != TAB_TYPE_CANBUS
        //				&& mCurrentPreferenceIndex != TAB_TYPE_SUPER)
        //			finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onChangeFragment(int id) {
        if (id == 0) {
            replaceFragment(R.id.id_genernal_setting_fragment, mAudioChannelGainFragment, false, -1);
        }
    }

    private void replaceFragment(int layoutId, Fragment fragment, boolean isAddStack, int preference_type) {
        if (fragment != null) {
            if (preference_type >= 0) {
                Bundle bundle = new Bundle();
                bundle.putInt("preference_type", preference_type);
                fragment.setArguments(bundle);
            }
            if (mFragmentManager == null) mFragmentManager = getFragmentManager();
            FragmentTransaction transation = mFragmentManager.beginTransaction();
            transation.replace(layoutId, fragment);
            if (isAddStack) {
                transation.addToBackStack(null);
            }
            transation.commit();
        }
    }

    private final static String OTG_71 = "/sys/class/ak/source/otg_id";
    private final static String OTG_51 = "/sys/devices/platform/dwc_otg/otg_mode";

    private String OTG;

    private static boolean mOTG = false;

    public void toggleOtgToDevice(boolean save) {
        /// String s = Util.getFileString(OTG);
        /// Util.setBeepToMcu();
        if (Util.isNexellSystem())
        {
            if (Build.VERSION.SDK_INT <= 23) {
                OTG = OTG_51;
            } else {
                OTG = OTG_71;
            }
        }
        else
        {
            String s;
            if (mOTG) {
                s = "device";
            } else {
                s = "host";
            }
            mOTG = !mOTG;
            String otg;
            if (s.equals("host")) {
                s = (Util.isPX6() || Util.isPX30()) ? "peripheral" : "device";
                otg = "2";
            } else {
                s = "host";
                otg = null;
            }
            if (Util.isPX6()) {
                if (Util.isAndroidP()) {
                    Util.setFileValue("/sys/devices/platform/usb0/dwc3_mode", s);
                } else if (Util.isAndroidQ()) {
                    Util.setFileValue("/sys/devices/platform/ff770000.syscon/ff770000.syscon:usb2-phy@e450/otg_mode", s);
                }
            } else if (Util.isPX30()) {
                Util.setFileValue("/sys/devices/platform/ff2c0000.syscon/ff2c0000.syscon:usb2-phy@100/otg_mode", s);
            } else {
                if (Util.isAndroidQ()) {
                    Util.setFileValue("/sys/devices/platform/ff770000.syscon/ff770000.syscon:usb2-phy@700/otg_mode", s);
                } else {
                    Util.sudoExec("chmod:666:/sys/bus/platform/drivers/usb20_otg/force_usb_mode");
                    Util.setFileValue("/sys/bus/platform/drivers/usb20_otg/force_usb_mode", s);
                }
            }
            Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
            if (save) {
                MachineConfig.setProperty(MachineConfig.KEY_OTG_TEST, otg);
            }
            return;
        }
        String s;
        if (mOTG) {
            s = "device";
        } else {
            s = "host";
        }
        mOTG = !mOTG;

        String otg;
        if (s.equals("host")) {
            s = "device";
            otg = "1";
        } else {
            s = "host";
            otg = "0";
        }

        Util.setFileValue(OTG, s);
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
        if (save) {
            MachineConfig.setProperty(MachineConfig.KEY_OTG_TEST, otg);
        }
    }

    public void toggleCustomServiceMode() {

        String msg;
        String s = SystemConfig.getProperty(this, MachineConfig.KEY_CUSTOMER_SERVICE_MODE_SWITCH);
        if ("1".equals(s)) {
            s = "0";
            msg = "Disable customer service mode!";
        } else {
            s = "1";
            msg = "Enable customer service mode!";
        }
        SystemConfig.setProperty(this, MachineConfig.KEY_CUSTOMER_SERVICE_MODE_SWITCH, s);

        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, MachineConfig.KEY_CUSTOMER_SERVICE_MODE_SWITCH);
        sendBroadcast(it);

    }

    public void toggleCustomServiceModeEx(String config) {

        String msg;
        String s = null;


        s = SystemConfig.getProperty(this, MachineConfig.KEY_PASSWD_CUSTOMER_SERVICE_MODE_EX);
        if ("1".equals(s)) {
            s = "0";
            msg = "Disable customer service mode!!";
        } else {
            s = "1";
            msg = "Enable customer service mode!!";
        }
        SystemConfig.setProperty(this, MachineConfig.KEY_PASSWD_CUSTOMER_SERVICE_MODE_EX, s);

        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, MachineConfig.KEY_APP_HIDE);
        sendBroadcast(it);

    }
}
