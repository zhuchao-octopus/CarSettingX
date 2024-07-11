package com.my.videoout;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.common.util.AppConfig;
import com.common.util.MachineConfig;
import com.common.util.Util;
import com.octopus.android.carsettingx.R;

public class VideoOutActivity extends Activity {
    private static final String TAG = "VideoOutActivity";
    private static final String MCU_VIDEO_OUT_L_NODE = "/sys/class/ak/source/lindex";
    private static final String MCU_VIDEO_OUT_R_NODE = "/sys/class/ak/source/rindex";
    private static final int MCU_VIDEO_OUT_SOURCE_OFF = 16;
    private static final int MCU_VIDEO_OUT_SOURCE_ARM = 7;
    private static final int MCU_VIDEO_OUT_SOURCE_DVD = 1;
    private static final int MCU_VIDEO_OUT_SOURCE_TV = 3;
    private static final int MCU_VIDEO_OUT_SOURCE_AV = 5;

    private RadioGroup mRadioGroupL;
    private RadioGroup mRadioGroupR;

    private int getVideoOutLSetting() {
        return Util.getFileValue(MCU_VIDEO_OUT_L_NODE);
    }

    private void setVideoOutLSetting(int value) {
        int string = 0;
        switch (value) {
            case MCU_VIDEO_OUT_SOURCE_OFF:
                string = R.string.video_out_off;
                break;
            case MCU_VIDEO_OUT_SOURCE_ARM:
                string = R.string.video_out_arm;
                break;
            case MCU_VIDEO_OUT_SOURCE_DVD:
                string = R.string.video_out_dvd;
                break;
            case MCU_VIDEO_OUT_SOURCE_TV:
                string = R.string.video_out_tv;
                break;
            case MCU_VIDEO_OUT_SOURCE_AV:
                string = R.string.video_out_av;
                break;
        }
        if (string != 0) {
            TextView tv = (TextView) findViewById(R.id.VideoOutRadioL);
            if (tv != null) {
                tv.setText(string);
            }
        }

        Util.setFileValue(MCU_VIDEO_OUT_L_NODE, value);
    }

    private int getVideoOutRSetting() {
        return Util.getFileValue(MCU_VIDEO_OUT_R_NODE);
    }

    private void setVideoOutRSetting(int value) {
        int string = 0;
        switch (value) {
            case MCU_VIDEO_OUT_SOURCE_OFF:
                string = R.string.video_out_off;
                break;
            case MCU_VIDEO_OUT_SOURCE_ARM:
                string = R.string.video_out_arm;
                break;
            case MCU_VIDEO_OUT_SOURCE_DVD:
                string = R.string.video_out_dvd;
                break;
            case MCU_VIDEO_OUT_SOURCE_TV:
                string = R.string.video_out_tv;
                break;
            case MCU_VIDEO_OUT_SOURCE_AV:
                string = R.string.video_out_av;
                break;
        }
        if (string != 0) {
            TextView tv = (TextView) findViewById(R.id.VideoOutRadioR);
            if (tv != null) {
                tv.setText(string);
            }
        }
        Util.setFileValue(MCU_VIDEO_OUT_R_NODE, value);
    }

    private void updateRadioViews() {
        int value;
        int id;
        RadioButton rb;
        TextView tv;

        value = getVideoOutLSetting();
        switch (value) {
            case MCU_VIDEO_OUT_SOURCE_ARM:
                id = R.id.radioLArm;
                break;
            case MCU_VIDEO_OUT_SOURCE_DVD:
                id = R.id.radioLDvd;
                break;
            case MCU_VIDEO_OUT_SOURCE_TV:
                id = R.id.radioLTv;
                break;
            case MCU_VIDEO_OUT_SOURCE_AV:
                id = R.id.radioLAv;
                break;
            case MCU_VIDEO_OUT_SOURCE_OFF:
            default:
                id = R.id.radioLOff;
                break;
        }
        rb = (RadioButton) findViewById(id);
        rb.setChecked(true);

        tv = (TextView) findViewById(R.id.VideoOutRadioL);
        if (tv != null) {
            tv.setText(rb.getText());
        }

        value = getVideoOutRSetting();
        switch (value) {
            case MCU_VIDEO_OUT_SOURCE_ARM:
                id = R.id.radioRArm;
                break;
            case MCU_VIDEO_OUT_SOURCE_DVD:
                id = R.id.radioRDvd;
                break;
            case MCU_VIDEO_OUT_SOURCE_TV:
                id = R.id.radioRTv;
                break;
            case MCU_VIDEO_OUT_SOURCE_AV:
                id = R.id.radioRAv;
                break;
            case MCU_VIDEO_OUT_SOURCE_OFF:
            default:
                id = R.id.radioROff;
                break;
        }
        rb = (RadioButton) findViewById(id);
        rb.setChecked(true);

        tv = (TextView) findViewById(R.id.VideoOutRadioR);
        if (tv != null) {
            tv.setText(rb.getText());
        }

    }

    private void setVisibility(int id, int visible) {
        View v = findViewById(id);
        if (v != null) {
            v.setVisibility(visible);
        }
    }

    private void upateVisible() {
        String value = MachineConfig.getPropertyOnce(MachineConfig.KEY_APP_HIDE);
        if (null != value) {
            if (value.contains("DTV")) {
                setVisibility(R.id.radioRTv, View.GONE);
                setVisibility(R.id.radioLTv, View.GONE);
            } else {
                setVisibility(R.id.radioRTv, View.VISIBLE);
                setVisibility(R.id.radioLTv, View.VISIBLE);
            }
            if (value.contains(AppConfig.HIDE_APP_DVD)) {
                setVisibility(R.id.radioRDvd, View.GONE);
                setVisibility(R.id.radioLDvd, View.GONE);
            } else {
                setVisibility(R.id.radioRDvd, View.VISIBLE);
                setVisibility(R.id.radioLDvd, View.VISIBLE);
            }

        } else {
            setVisibility(R.id.radioRTv, View.GONE);
            setVisibility(R.id.radioLTv, View.GONE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.video_out);
        upateVisible();
        updateRadioViews();

        TextView tv = (TextView) findViewById(R.id.VideoOutRadioL);
        if (tv != null) {
            tv.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    if (mRadioGroupL != null) {
                        if (mRadioGroupL.getVisibility() == View.VISIBLE) {
                            mRadioGroupL.setVisibility(View.GONE);
                        } else {

                            mRadioGroupL.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }

        tv = (TextView) findViewById(R.id.VideoOutRadioR);
        if (tv != null) {
            tv.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    if (mRadioGroupR != null) {
                        if (mRadioGroupR.getVisibility() == View.VISIBLE) {
                            mRadioGroupR.setVisibility(View.GONE);
                        } else {

                            mRadioGroupR.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }

        mRadioGroupL = (RadioGroup) findViewById(R.id.radioGroupVideoOutL);
        mRadioGroupL.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                // TODO Auto-generated method stub
                if (arg1 == R.id.radioLArm) {
                    setVideoOutLSetting(MCU_VIDEO_OUT_SOURCE_ARM);
                } else if (arg1 == R.id.radioLDvd) {
                    setVideoOutLSetting(MCU_VIDEO_OUT_SOURCE_DVD);
                } else if (arg1 == R.id.radioLTv) {
                    setVideoOutLSetting(MCU_VIDEO_OUT_SOURCE_TV);
                } else if (arg1 == R.id.radioLAv) {
                    setVideoOutLSetting(MCU_VIDEO_OUT_SOURCE_AV);
                } else if (arg1 == R.id.radioLOff) {
                    setVideoOutLSetting(MCU_VIDEO_OUT_SOURCE_OFF);
                }

                mRadioGroupL.setVisibility(View.GONE);
            }

        });

        mRadioGroupR = (RadioGroup) findViewById(R.id.radioGroupVideoOutR);
        mRadioGroupR.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                // TODO Auto-generated method stub
                if (arg1 == R.id.radioRArm) {
                    setVideoOutRSetting(MCU_VIDEO_OUT_SOURCE_ARM);
                } else if (arg1 == R.id.radioRDvd) {
                    setVideoOutRSetting(MCU_VIDEO_OUT_SOURCE_DVD);
                } else if (arg1 == R.id.radioRTv) {
                    setVideoOutRSetting(MCU_VIDEO_OUT_SOURCE_TV);
                } else if (arg1 == R.id.radioRAv) {
                    setVideoOutRSetting(MCU_VIDEO_OUT_SOURCE_AV);
                } else if (arg1 == R.id.radioROff) {
                    setVideoOutRSetting(MCU_VIDEO_OUT_SOURCE_OFF);
                }
                mRadioGroupR.setVisibility(View.GONE);
            }

        });
    }

    @Override
    protected void onResume() {
        // AppConfig.updateSystemBackground(this,
        // getWindow().getDecorView().findViewById(android.R.id.content));
        super.onResume();
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

}
