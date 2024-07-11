package com.my.factory;

import java.io.File;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Spinner;

import com.common.util.MachineConfig;
import com.common.util.Util;
import com.my.filemanager.FileManagerActivity;
import com.octopus.android.carsettingx.R;

public class LogoFragment extends PreferenceFragment implements OnClickListener, OnItemClickListener {

    private static final String TAG = "LogoFragment";

    private static final String DEFAULT_ANDROID_LOGO_PATH = "/mnt/paramter/logo/default_logo2.png";
    private static final String DEFAULT_KERNEL_LOGO_PATH = "/mnt/paramter/logo/default_logo1.bmp";

    private Activity mActivity;
    private Gallery mGallery;
    private LogoGalleryAdapter mLogoGalleryAdapter;
    private ImageView mCurrentLogoImage;
    private String mStartPath;
    private String mCurrentPath;

    private static final String CUSTOM_LOGO_PATH = "/sdcard/.aklogo/";
    private static final String CUSTOM_RK_LOGO_PATH = "/oem/logo/.aklogo/";
    private String CUSTOM_KERNEL_LOGO = CUSTOM_LOGO_PATH + "kernel_logo.bmp";
    private String CUSTOM_ANDROID_LOGO = CUSTOM_LOGO_PATH + "android_logo.png";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();

        registerListener();
        initPath();
    }

    private void initPath() {
        File f = new File("/oem/logo");
        if (f.exists()) {
            CUSTOM_KERNEL_LOGO = CUSTOM_RK_LOGO_PATH + "kernel_logo.bmp";
            CUSTOM_ANDROID_LOGO = CUSTOM_RK_LOGO_PATH + "android_logo.png";
        }
    }

    Spinner mSpinnerType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Context context = inflater.getContext();
        final View view = inflater.inflate(R.layout.logo_setting, container, false);

        mCurrentLogoImage = (ImageView) view.findViewById(R.id.cur_logo_image);

        mGallery = (Gallery) view.findViewById(R.id.gallery_logo);

        mGallery.setFadingEdgeLength(0);
        mGallery.setSpacing(mActivity.getResources().getDimensionPixelSize(R.dimen.logo_spacing));
        mGallery.setUnselectedAlpha((float) 0.5);
        mGallery.setOnItemClickListener(this);

        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);

        view.findViewById(R.id.btn_custom).setOnClickListener(this);
        mStartPath = getLogoSetting();

        mSpinnerType = (Spinner) view.findViewById(R.id.spinner_logo_type);
        mSpinnerType.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(android.widget.AdapterView arg0, android.view.View arg1, int arg2, long arg3) {
                mPath = LOGOS_PATH;
                updateLogoType(arg2);
            }
            public void onNothingSelected(android.widget.AdapterView arg0) {
            }
        });
        ///		mPath = LOGOS_PATH;
        //		updateLogoType(mType);
        return view;
    }

    private int mType = 0;
    public static final String LOGOS_PATH = "/mnt/paramter/logo";
    private String mPath = LOGOS_PATH;

    private void updateLogoType(int type) {
        mType = type;
        String def;
        if (mLogoGalleryAdapter != null) {
            mGallery.setAdapter(null);
            mLogoGalleryAdapter.release();
            mLogoGalleryAdapter = null;
        }
        String fisrt = null;
        if (type == 0) {

            def = getLogoSetting();
            if (!def.startsWith(LOGOS_PATH)) {
                fisrt = def;
            }
            mLogoGalleryAdapter = new LogoGalleryAdapter(mActivity, ".png", mPath, fisrt);
        } else {

            def = getLogoKernelSetting();
            if (!def.startsWith(LOGOS_PATH)) {
                fisrt = def;
            }
            mLogoGalleryAdapter = new LogoGalleryAdapter(mActivity, ".bmp", mPath, fisrt);

        }

        mGallery.setAdapter(mLogoGalleryAdapter);

        updateCurrentLogo(def);
    }

    private void updateLogoTypeEx(String def) {
        if (mLogoGalleryAdapter != null) {
            mGallery.setAdapter(null);
            mLogoGalleryAdapter.release();
            mLogoGalleryAdapter = null;
        }
        if (mType == 0) {
            mLogoGalleryAdapter = new LogoGalleryAdapter(mActivity, ".png", mPath, null);
        } else {
            mLogoGalleryAdapter = new LogoGalleryAdapter(mActivity, ".bmp", mPath, null);
        }

        mGallery.setAdapter(mLogoGalleryAdapter);
        updateCurrentLogo(def);
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterListener();

    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar ab = mActivity.getActionBar();
        if (null != ab) {
            ab.setTitle(R.string.logo_settings_title);
        }

    }

    @Override
    public void onClick(View arg0) {
        int id = arg0.getId();
        if (id == R.id.btn_ok) {
            if (null != mCurrentPath) {
                if (!mCurrentPath.equals(mStartPath)) {
                    mStartPath = mCurrentPath;
                    setLogoSetting(mCurrentPath);
                }
            }
        } else if (id == R.id.btn_custom) {
            Intent intent = new Intent("com.my.filemanager.intent.action.FileManagerActivity");
            String type = ".png";
            if (mType != 0) {
                type = ".bmp";
            }
            intent.putExtra("type", type);
            startActivity(intent);
        }

        if ((R.id.btn_cancel == arg0.getId())/* || (R.id.btn_ok == arg0.getId()) */) {
            // mActivity.finish();
            mActivity.getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        updateCurrentLogo(position);
    }

    private String getLogoSetting() {
        String s = MachineConfig.getProperty(MachineConfig.KEY_LOGO_PATH);
        if ((null == s) || (s.isEmpty())) {
            return DEFAULT_ANDROID_LOGO_PATH;
        } else {
            return s;
        }
    }

    private String getLogoKernelSetting() {
        String s = MachineConfig.getProperty(MachineConfig.KEY_LOGO_PATH_K);
        if ((null == s) || (s.isEmpty())) {
            return DEFAULT_KERNEL_LOGO_PATH;
        } else {
            return s;
        }
    }

    private void checkDir() {
        try {
            File f = (new File(CUSTOM_LOGO_PATH));
            if (!f.exists()) {
                f.mkdirs();
            }
        } catch (Exception e) {

        }
    }

    private void setLogoSetting(String which) {
        if ((null != which) && (!which.isEmpty())) {
            if (mType == 0) {
                if (DEFAULT_ANDROID_LOGO_PATH.equals(which) || which.endsWith("android.png")) {
                    MachineConfig.setProperty(MachineConfig.KEY_LOGO_PATH, null);
                } else {
                    if (which.startsWith(LOGOS_PATH)) {
                        MachineConfig.setProperty(MachineConfig.KEY_LOGO_PATH, which);
                    } else {
                        checkDir();
                        if (CUSTOM_ANDROID_LOGO.startsWith(CUSTOM_RK_LOGO_PATH)) {
                            Util.sudoExec("cp:" + which + ":" + CUSTOM_ANDROID_LOGO);
                            Util.doSleep(400);
                            Util.sudoExec("chmod:666:" + CUSTOM_ANDROID_LOGO);
                            Util.doSleep(100);
                            Util.sudoExec("sync");
                        } else {
                            Util.copyFile(which, CUSTOM_ANDROID_LOGO);
                        }
                        MachineConfig.setProperty(MachineConfig.KEY_LOGO_PATH, CUSTOM_ANDROID_LOGO);
                    }
                }
            } else {
                if (DEFAULT_KERNEL_LOGO_PATH.equals(which)) {
                    MachineConfig.setProperty(MachineConfig.KEY_LOGO_PATH_K, "");
                } else {
                    if (which.startsWith(LOGOS_PATH)) {
                        MachineConfig.setProperty(MachineConfig.KEY_LOGO_PATH_K, which);
                    } else {

                        checkDir();
                        if (CUSTOM_KERNEL_LOGO.startsWith(CUSTOM_RK_LOGO_PATH)) {
                            Util.sudoExec("cp:" + which + ":" + CUSTOM_KERNEL_LOGO);
                        } else {
                            Util.copyFile(which, CUSTOM_KERNEL_LOGO);
                        }
                        if ((Util.isAndroidLaterP()) && CUSTOM_KERNEL_LOGO != null && CUSTOM_KERNEL_LOGO.endsWith(".bmp")) {
                            Util.sudoExec("/system/bin/ak_bmpconvert:" + CUSTOM_KERNEL_LOGO);
                        }

                        //						Util.copyFile(which, CUSTOM_KERNEL_LOGO);
                        MachineConfig.setProperty(MachineConfig.KEY_LOGO_PATH_K, CUSTOM_KERNEL_LOGO);
                    }
                    Util.sudoExec("sync");
                }
            }
        }
    }

    private void updateCurrentLogo(String path) {
        mCurrentPath = path;
        if (null != mCurrentPath) {
            int pos = mLogoGalleryAdapter.getPosition(mCurrentPath);
            if (pos >= 0) {
                ImageView image;
                image = (ImageView) mLogoGalleryAdapter.getView(pos, null, null);
                mCurrentLogoImage.setImageDrawable(image.getDrawable());
                mGallery.setSelection(pos);
            }
        }
    }

    private void updateCurrentLogo(int pos) {
        if (pos >= 0) {
            mCurrentPath = mLogoGalleryAdapter.getPath(pos);
            ImageView image;
            image = (ImageView) mLogoGalleryAdapter.getView(pos, null, null);
            mCurrentLogoImage.setImageDrawable(image.getDrawable());
        }
    }

    private BroadcastReceiver mReceiver = null;

    private void registerListener() {
        if (mReceiver == null) {
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    Log.d(TAG, "onReceive" + action);
                    if (action.equals(FileManagerActivity.BROADCAST_RETURN_SELECT_FILE)) {
                        String s = intent.getStringExtra("path");
                        if (s != null) {
                            mPath = s.substring(0, s.lastIndexOf("/"));
                            updateLogoTypeEx(s);
                        }

                    }

                }
            };
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction(FileManagerActivity.BROADCAST_RETURN_SELECT_FILE);

            mActivity.registerReceiver(mReceiver, iFilter);
        }
    }

    private void unregisterListener() {
        if (mReceiver != null) {
            mActivity.unregisterReceiver(mReceiver);
            mReceiver = null;
        }

    }

}
