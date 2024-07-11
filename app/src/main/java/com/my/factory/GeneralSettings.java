package com.my.factory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.common.util.Kernel;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.octopus.android.carsettingx.R;

public class GeneralSettings extends Activity {
    private static final String TAG = "GeneralSettings";
    public static String mExtUI;
    public static String mSystemUI;// = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_SYSTEM_UI);
    private FragmentCarSettingsMain mFragmentCarSettingsMain;// = new FragmentCarSettingsMain();
    private GeneralSettingsFragment mGeneralSettingsFragment;// = new GeneralSettingsFragment();
    private FragmentManager mFragmentManager;
    private SettingsControllor mSettingsControllor = null;
    private String mTitleOrg = "";

    public final static String SETTINGS_EXT_SHOW_DOOR_VOICE = "1";  //default is show
    public final static String SETTINGS_EXT_SHOW_RADAR_CAMERA = "2";
    public final static String SETTINGS_EXT_HIDE_FACTORY = "3";
    public final static String SETTINGS_SHOW_OBD_SCREEN = "4";
    public final static String SETTINGS_SHOW_LAUNCHER_UI = "5";
    public final static String SETTINGS_SHOW_MIC_TYPE = "6";

    public static String mSettingsExtShow = null;
    public final static String SETTINGS_HIDE_VIDEO_ON_DRIVER = "1";
    public final static String SETTINGS_HIDE_FACTORY_VOLUME = "2";
    public final static String SETTINGS_HIDE_STATIC_TRACK = "3";
    public final static String SETTINGS_HIDE_DYNAMIC_TRACK = "4";
    public static String mSettingsExtHide = null;

    static {
        mSystemUI = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_SYSTEM_UI);
        if (mSystemUI == null) {
            mSystemUI = "";
        }
        mExtUI = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_EXT_UI);

        mSettingsExtShow = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_SETTINGS_SHOW_EXT);
        mSettingsExtHide = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_SETTINGS_HIDE_EXT);
    }

    public static boolean isExtShow(String extIndex) {
        if (mSettingsExtShow != null) {
            String[] ss = mSettingsExtShow.split(",");
            for (String s : ss) {
                if (s.equals(extIndex)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isExtHide(String extIndex) {
        if (mSettingsExtHide != null) {
            String[] ss = mSettingsExtHide.split(",");
            for (String s : ss) {
                if (s.equals(extIndex)) {
                    return true;
                }
            }
        }
        return false;
    }

    private FragmentCarSettingsMain getFragmentCarSettingsMain() {
        if (mFragmentCarSettingsMain == null) {
            mFragmentCarSettingsMain = new FragmentCarSettingsMain();
        }
        return mFragmentCarSettingsMain;
    }

    private GeneralSettingsFragment getGeneralSettingsFragment() {
        if (mGeneralSettingsFragment == null) {
            mGeneralSettingsFragment = new GeneralSettingsFragment();
        }
        return mGeneralSettingsFragment;
    }

    private boolean mPasswdHold = false;

    // private KeyControllor mKeyControllor = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettingsControllor = SettingsControllor.getInstance(getApplicationContext());
        mTitleOrg = getTitle().toString();
        // mSettingsControllor.connectService(new
        // OnServiceConnectSuccessListener() {
        //
        // @Override
        // public void onServiceConnectSuccess() {
        // mSettingsService = mSettingsControllor.getFeature();
        // LOG.print("setting service connect success~ service = " +
        // mSettingsService);
        // }
        // });
        // mKeyControllor = KeyControllor.getInstance(getApplicationContext());
        // if(mKeyControllor.getFeature() == null){
        // mKeyControllor.connectService(new OnServiceConnectSuccessListener() {
        //
        // @Override
        // public void onServiceConnectSuccess() {
        // LOG.print("key service connect success~ service = " +
        // mKeyControllor.getFeature());
        // }
        // });
        // }
        if (MachineConfig.VALUE_SYSTEM_UI_KLD7_1992.equals(mSystemUI)) {
            mPasswdHold = true;
            final EditText ev = new EditText(this);
            final Toast t = Toast.makeText(this, getResources().getString(R.string.password_error), Toast.LENGTH_SHORT);
            AlertDialog ad = new AlertDialog.Builder(this).setIcon(R.drawable.dialog_alert_icon).setTitle(R.string.input_code).setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String str = ev.getText().toString().toLowerCase();
                    if ("1234".equals(str)) {
                        setContentView(R.layout.general_settings_activity);
                        mFragmentManager = getFragmentManager();
                        replaceFragment(R.id.id_genernal_setting_fragment, getGeneralSettingsFragment(), false, null);

                        mPasswdHold = false;
                        updateIntent();
                    } else {
                        t.setText(R.string.password_error);
                        t.show();
                        finish();
                    }
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

            ad.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface arg0) {
                    InputMethodManager mInputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    mInputManager.hideSoftInputFromWindow(ev.getWindowToken(), 0);
                }
            });

        } else {
            setContentView(R.layout.general_settings_activity);
            mFragmentManager = getFragmentManager();
            mSettingType = MachineConfig.getPropertyIntReadOnly(MachineConfig.KEY_CAR_SETTING_TYPE);
            if (mSettingType == 1) { //old type
                replaceFragment(R.id.id_genernal_setting_fragment, getGeneralSettingsFragment(), false, null);
            } else {
                replaceFragment(R.id.id_genernal_setting_fragment, getFragmentCarSettingsMain(), false, null);
            }
        }

        String value = MachineConfig.getPropertyReadOnly("driving_set_passwd");
        if (value != null && !value.isEmpty()) {
            PW_DRIVE_SETTING = value;
        }


        updateIntent();
    }

    public static int mSettingType = 0;

    void updateIntent() {
        if (mPasswdHold) {
            return;
        }
        Intent it = getIntent();
        if (it != null) {
            if (it.getIntExtra("navi", 0) == 1) {
                Bundle bundle = new Bundle();
                bundle.putString("preference_type", KEY_NAVI);
                replaceFragment(R.id.id_genernal_setting_fragment, getGeneralSettingsFragment(), true, bundle);
                getGeneralSettingsFragment().updateNaviChoice();
            }

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);
        this.setIntent(intent);
        updateIntent();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        if (mSettingsControllor != null) {
            // mSettingsControllor.disconnectService();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mSettingsControllor != null) {
            // mSettingsControllor.release();
        }
        // if(mKeyControllor != null){
        // mKeyControllor.release();
        // }
        // Settings.isPasswordAlreadyRight = false;
        super.onDestroy();
    }

    private void replaceFragment(int layoutId, PreferenceFragment fragment, boolean isAddStack, Bundle bundle) {
        try {
            if (fragment != null) {
                if (bundle != null) fragment.setArguments(bundle);
                FragmentTransaction transation = mFragmentManager.beginTransaction();
                transation.replace(layoutId, fragment);
                if (isAddStack) {
                    transation.addToBackStack(null);
                }
                transation.commit();
            }
        } catch (Exception ignored) {
        }
    }

    private String PW_DRIVE_SETTING = null;

    public final static String KEY_NONE = "key_none";
    public final static String KEY_PERSONAL = "key_car_settings_personal";
    public final static String KEY_NAVI = "key_car_settings_navi";
    public final static String KEY_DRIVE = "key_car_settings_drive";
    public final static String KEY_WHELL = "key_car_settings_wheel";
    public final static String KEY_HOME = "key_car_settings_launcher_ui";
    public final static String KEY_FACTORY = "key_car_settings_factory";
    public final static String KEY_UPGRADE = "key_car_settings_update";

    @SuppressLint("ValidFragment")
    class FragmentCarSettingsMain extends PreferenceFragment implements OnPreferenceClickListener, OnPreferenceChangeListener {
        private static final String TAG = "CarSettingsMainFragment";

        private final Handler mHandler = new Handler();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.car_settings_main);

            findPreference(KEY_PERSONAL).setOnPreferenceClickListener(this);
            findPreference(KEY_NAVI).setOnPreferenceClickListener(this);
            findPreference(KEY_DRIVE).setOnPreferenceClickListener(this);
            // findPreference(KEY_WHELL).setOnPreferenceClickListener(this);
            // findPreference(KEY_FACTORY).setOnPreferenceClickListener(this);
            // findPreference(KEY_HOME).setOnPreferenceClickListener(this);
            findPreference(KEY_UPGRADE).setOnPreferenceClickListener(this);

            customUI();
        }

        @Override
        public void onResume() {
            // TODO Auto-generated method stub
            super.onResume();
            getActivity().setTitle(mTitleOrg);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            // TODO Auto-generated method stub
            final String key = preference.getKey();
            Bundle bundle = null;
            switch (key) {
                case KEY_PERSONAL:
                case KEY_NAVI:
                case KEY_UPGRADE:
                    break;
                case KEY_DRIVE:
                    // add by allen
                    if (PW_DRIVE_SETTING != null) {
                        final EditText ev = new EditText(getActivity());
                        final Toast t = Toast.makeText(getActivity(), getResources().getString(R.string.password_error), Toast.LENGTH_SHORT);
                        AlertDialog ad = new AlertDialog.Builder(getActivity()).setIcon(R.drawable.dialog_alert_icon).setTitle(R.string.input_code).setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String str = ev.getText().toString().toLowerCase();
                                if (PW_DRIVE_SETTING.equals(str)) {
                                    setTitle(key);

                                    Bundle bundle = new Bundle();
                                    bundle.putString("preference_type", key);
                                    replaceFragment(R.id.id_genernal_setting_fragment, getGeneralSettingsFragment(), true, bundle);
                                } else {
                                    t.show();
                                }
                            }
                        }).setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        }).create();

                        ad.setView(ev);
                        ad.show();

                        ev.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                        ev.setInputType(InputType.TYPE_CLASS_NUMBER);
                        ev.setTransformationMethod(PasswordTransformationMethod.getInstance());

                        return false;
                    }
                    // } else if (key.equals(KEY_WHELL)) {
                    // } else if (key.equals(KEY_FACTORY)) {
                    break;
                default:
                    return false;
            }

            setTitle(key);

            bundle = new Bundle();
            bundle.putString("preference_type", key);
            replaceFragment(R.id.id_genernal_setting_fragment, getGeneralSettingsFragment(), true, bundle);
            return true;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String key = preference.getKey();
            if (KEY_HOME.equals(key)) {
                setLauncherUI(preference, (String) newValue);
            }
            return false;
        }

        private void setTitle(String preference_type) {
            String title = "";
            if (preference_type.equals(GeneralSettings.KEY_PERSONAL)) {
                title = getResources().getString(R.string.title_car_settings_personal);
            } else if (preference_type.equals(GeneralSettings.KEY_NAVI)) {
                title = getResources().getString(R.string.title_car_settings_navi);
            } else if (preference_type.equals(GeneralSettings.KEY_DRIVE)) {
                title = getResources().getString(R.string.title_car_settings_drive);
            } else if (preference_type.equals(GeneralSettings.KEY_WHELL)) {
                title = getResources().getString(R.string.title_car_settings_wheel);
            } else if (preference_type.equals(GeneralSettings.KEY_HOME)) {
                title = getResources().getString(R.string.launcher_ui_title);
            } else if (preference_type.equals(GeneralSettings.KEY_FACTORY)) {
                title = getResources().getString(R.string.title_car_settings_factory);
            } else if (preference_type.equals(GeneralSettings.KEY_UPGRADE)) {
                title = getResources().getString(R.string.title_car_settings_update);
            }
            getActivity().setTitle(title);
        }

        private void customUI() {
            // value = MachineConfig.VALUE_SYSTEM_UI20_RM10_1; // test
            Preference p = findPreference(KEY_HOME);
            if (p != null) {

                if (MachineConfig.VALUE_SYSTEM_UI20_RM10_1.equals(GeneralSettings.mSystemUI) || MachineConfig.VALUE_SYSTEM_UI21_RM10_2.equals(GeneralSettings.mSystemUI) || MachineConfig.VALUE_SYSTEM_UI21_RM12.equals(GeneralSettings.mSystemUI) || GeneralSettings.isExtShow(GeneralSettings.SETTINGS_SHOW_LAUNCHER_UI) || GeneralSettings.mExtUI != null) {
                    p.setOnPreferenceChangeListener(this);
                    updateLauncherUI();
                } else {
                    getPreferenceScreen().removePreference(p);
                }
            }

            p = findPreference("key_car_settings_factory");
            if (p != null) {
                if (GeneralSettings.isExtShow(GeneralSettings.SETTINGS_EXT_HIDE_FACTORY)) {
                    getPreferenceScreen().removePreference(p);
                }

            }
        }

        private void updateLauncherUI() {
            String value = SystemConfig.getProperty(getActivity(), SystemConfig.KEY_LAUNCHER_UI_RM10);
            if (value == null) {
                value = "0";
            }
            if (value != null) {
                Preference p = findPreference(KEY_HOME);
                if (p != null) {
                    ListPreference lp = (ListPreference) p;

                    lp.setValue(value);
                    lp.setSummary(lp.getEntry());
                }
            }
        }

        private void setLauncherUI(Preference p, String value) {

            String old = SystemConfig.getProperty(getActivity(), SystemConfig.KEY_LAUNCHER_UI_RM10);

            if (!value.equals(old)) {
                SystemConfig.setIntProperty(getActivity(), SystemConfig.KEY_LAUNCHER_UI_RM10_WORKSPACE_RELOAD, 1);
                SystemConfig.setProperty(getActivity(), SystemConfig.KEY_LAUNCHER_UI_RM10, value);
                ListPreference lp = (ListPreference) p;

                lp.setValue(value);
                lp.setSummary(lp.getEntry());

                Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
                it.putExtra(MyCmd.EXTRA_COMMON_CMD, SystemConfig.KEY_LAUNCHER_UI_RM10);
                it.putExtra(MyCmd.EXTRA_COMMON_DATA, value);
                getActivity().sendBroadcast(it);

                mHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        Kernel.doKeyEvent(Kernel.KEY_HOMEPAGE);
                    }
                }, 800);
            }
        }
    }
}
