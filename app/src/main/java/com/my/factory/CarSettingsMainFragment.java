package com.my.factory;

import static com.my.factory.GeneralSettings.KEY_DRIVE;
import static com.my.factory.GeneralSettings.KEY_HOME;
import static com.my.factory.GeneralSettings.KEY_NAVI;
import static com.my.factory.GeneralSettings.KEY_PERSONAL;
import static com.my.factory.GeneralSettings.KEY_UPGRADE;
import static com.my.factory.GeneralSettings.PW_DRIVE_SETTING;
import static com.my.factory.GeneralSettings.mTitleOrg;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import com.common.util.Kernel;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.octopus.android.carsettingx.R;

public class CarSettingsMainFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
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
                                ((GeneralSettings)getActivity()).replaceFragment(R.id.id_genernal_setting_fragment, ((GeneralSettings)getActivity()).getGeneralSettingsFragment(), true, bundle);
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
        ((GeneralSettings)getActivity()).replaceFragment(R.id.id_genernal_setting_fragment, ((GeneralSettings)getActivity()).getGeneralSettingsFragment(), true, bundle);
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
        if (preference_type.equals(KEY_PERSONAL)) {
            title = getResources().getString(R.string.title_car_settings_personal);
        } else if (preference_type.equals(KEY_NAVI)) {
            title = getResources().getString(R.string.title_car_settings_navi);
        } else if (preference_type.equals(KEY_DRIVE)) {
            title = getResources().getString(R.string.title_car_settings_drive);
        } else if (preference_type.equals(GeneralSettings.KEY_WHELL)) {
            title = getResources().getString(R.string.title_car_settings_wheel);
        } else if (preference_type.equals(KEY_HOME)) {
            title = getResources().getString(R.string.launcher_ui_title);
        } else if (preference_type.equals(GeneralSettings.KEY_FACTORY)) {
            title = getResources().getString(R.string.title_car_settings_factory);
        } else if (preference_type.equals(KEY_UPGRADE)) {
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
        Preference p = findPreference(KEY_HOME);
        if (p != null) {
            ListPreference lp = (ListPreference) p;

            lp.setValue(value);
            lp.setSummary(lp.getEntry());
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