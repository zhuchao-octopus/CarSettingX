package com.my.factory;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.util.BroadcastUtil;
import com.common.util.MyCmd;
import com.common.view.RepeatingButton;
import com.octopus.android.carsettingx.R;

public class VCOMFragment extends PreferenceFragment {

    private static final String TAG = "VCOMFragment";

    private Activity mActivity;
    private ImageView mImageView;
    private RepeatingButton mBTNInc, mBTNDec;
    private TextView mTextView;
    private final static int REPEAT_INTERVAL = 500;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        registerListener();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.vcom_adjust, container, false);
        mImageView = (ImageView) view.findViewById(R.id.imageView);
        mImageView.setOnClickListener(mOnClickListener);
        mTextView = (TextView) view.findViewById(R.id.textView);
        mBTNInc = (RepeatingButton) view.findViewById(R.id.button_inc);
        mBTNDec = (RepeatingButton) view.findViewById(R.id.button_dec);
        mBTNInc.setText("+");
        mBTNDec.setText("-");
        mBTNInc.setOnClickListener(mOnClickListener);
        mBTNDec.setOnClickListener(mOnClickListener);
        mBTNInc.setRepeatListener(onRepeatListener, REPEAT_INTERVAL);
        mBTNDec.setRepeatListener(onRepeatListener, REPEAT_INTERVAL);


        queryVcomValue();

        displayBmp();

        return view;
    }

    RepeatingButton.RepeatListener onRepeatListener = new RepeatingButton.RepeatListener() {
        @Override
        public void onRepeat(View v, long duration, int repeatcount) {
            // TODO Auto-generated method stub
            if (repeatcount < 0) {
                return;
            }
            int id = v.getId();
            if (id == R.id.imageView) {
                displayBmp();
            } else if (id == R.id.button_inc) {
                adjustVCOM(true);
            } else if (id == R.id.button_dec) {
                adjustVCOM(false);
            }
        }
    };

    OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            int id = arg0.getId();
            if (id == R.id.imageView) {
                displayBmp();
            } else if (id == R.id.button_inc) {
                adjustVCOM(true);
            } else if (id == R.id.button_dec) {
                adjustVCOM(false);
            }
        }
    };

    private int bmpIndex = 0;

    private void displayBmp() {
        AssetManager assets = getActivity().getAssets();
        InputStream is = null;
        try {
            is = assets.open("vcombmp/" + "vcom_test" + (bmpIndex + 1) + ".bmp");
        } catch (IOException e) {
            e.printStackTrace();
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
        mImageView.setImageBitmap(bitmap);
        bmpIndex++;
        if (bmpIndex >= 4) bmpIndex = 0;
    }

    private int mVcomValue = 0;

    private void adjustVCOM(boolean add) {
        if (add) {
            if (mVcomValue < 100) {
                ++mVcomValue;
                BroadcastUtil.sendToCarService(getActivity(), MyCmd.Cmd.SET_VCOM, mVcomValue);
            }
        } else {
            if (mVcomValue > 0) {
                --mVcomValue;
                BroadcastUtil.sendToCarService(getActivity(), MyCmd.Cmd.SET_VCOM, mVcomValue);
            }
        }
        mTextView.setText("" + mVcomValue);
    }

    private void queryVcomValue() {
        BroadcastUtil.sendToCarService(getActivity(), MyCmd.Cmd.SET_VCOM, 0x100);
    }

    private BroadcastReceiver mReceiver = null;

    public void registerListener() {
        if (mReceiver == null) {
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();

                    if (action.equals(MyCmd.BROADCAST_CAR_SERVICE_SEND)) {

                        int cmd = intent.getIntExtra(MyCmd.EXTRA_COMMON_CMD, 0);

                        switch (cmd) {
                            case MyCmd.Cmd.SET_VCOM:
                                mVcomValue = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                                mTextView.setText("" + mVcomValue);
                                break;
                        }

                    }

                }
            };
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction(MyCmd.BROADCAST_CAR_SERVICE_SEND);

            getActivity().registerReceiver(mReceiver, iFilter);
        }
    }

    public void unregisterListener() {
        if (mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
            mReceiver = null;
        }

    }

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
        unregisterListener();
    }
}
