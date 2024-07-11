package com.my.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.common.util.Util;

import com.my.filemanager.FileManagerActivity.StorageInfo;
import com.my.hardware.mud.IMUDCallback;
import com.my.hardware.mud.MubUpdate;
import com.octopus.android.carsettingx.R;

public class UpdateManager extends Activity {
    public static MubUpdate mMubUpdate = new MubUpdate();

    private static final int UPDATE_NAV_MAX = 100;
    private static final int UPDATE_MCU = 0x0;
    private static final int PROCESS = 0x1;
    private static final int UPDATE_NAV = 0x2;
    private static final int PROCESS_NAV = 0x3;
    private static final int POS = 0x0;
    private static final int FLAG = 0x1;
    private static final int RESET = 0x2;
    private static final int SOURCEFILENOTEXIST = 0x03;
    private static final int SPACENOTENOUGH = 0x04;
    private static final int UPDATEPROCESS = 0x05;
    private ProgressDialog mProgressDialog;
    public static final String TAG = "UpdateManager";
    private static String SOURCEPATH = null;
    private static String TARGETPATH = null;
    private static long MINSPACE = -1;

    private File[][] mChildren;
    private int mStep = 0;
    private Timer mTimer;
    private boolean mIsOver = false;
    private AlertDialog mAlertDialog;

    private String mTitle;
    private FileFilter mFileFilter = new FileFilter() {

        public boolean accept(File pathname) {
            if (pathname.getName().startsWith(".") || !pathname.isDirectory()) {
                return false;
            }/*
             * else if(pathname.isFile()){
             * if(pathname.getName().endsWith(".apk")){ return true; }else
             * return false; }
             */ else return true;
        }

    };

    private Handler mProgressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case POS: {
                    if (mProgressDialog != null) {
                        mProgressDialog.setProgress(msg.arg1);
                    }
                    break;
                }
                case FLAG: {
                    if (mProgressDialog != null) {
                        mProgressDialog.setMessage(getString(msg.arg1));
                    }
                    break;
                }
                case RESET: {
                    mMubUpdate.setMUDCallback(null);
                    mMubUpdate.sendMUDCommand(MubUpdate.MUD_RESET);
                    break;
                }
                case SOURCEFILENOTEXIST:
                    Toast.makeText(getBaseContext(), R.string.source_not_exist, Toast.LENGTH_LONG).show();
                    break;

                case UPDATEPROCESS:

                    mStep++;
                    Log.e(TAG, "mStep=" + mStep + " over=" + mIsOver);
                    if (mStep < 100) mProgressDialog.setProgress(mStep);
                    else if (mIsOver) {
                        Log.e(TAG, "is over mStep=" + mStep);
                        mProgressDialog.setProgress(UPDATE_NAV_MAX);
                        if (mTimer != null) {
                            mTimer.cancel();
                            mTimer.purge();
                            mTimer = null;
                        }
                        TimerTask task = new TimerTask() {
                            public void run() {
                                mProgressDialog.dismiss();
                            }
                        };

                        mTimer = new Timer(true);
                        mTimer.schedule(task, 800);
                        Log.e(TAG, "is over mStep=" + mStep);

                    }
                    break;
            }
        }
    };

    IMUDCallback mMUDCallback = new IMUDCallback() {
        public void mudCallback(int value) {
            if ((value & MubUpdate.MUD_POS) == MubUpdate.MUD_POS) {
                int pos = (value & 0xffff0000) >> 16;
                Log.i("udpate:", "POS=" + pos);
                mProgressHandler.obtainMessage(POS, pos, 0).sendToTarget();
            }
            if ((value & MubUpdate.MUD_FLAG) == MubUpdate.MUD_FLAG) {
                int flag = (value & 0x0000ff00) >> 8;
                Log.i("udpate:", "FLAG=" + flag);
                switch (flag) {
                    case MubUpdate.MUD_FLAG_START:
                        mProgressHandler.obtainMessage(FLAG, R.string.update_mcu_start, 0).sendToTarget();
                        break;
                    case MubUpdate.MUD_FLAG_UNLOCK:
                        mProgressHandler.obtainMessage(FLAG, R.string.update_mcu_unlock, 0).sendToTarget();
                        break;
                    case MubUpdate.MUD_FLAG_DATA:
                        mProgressHandler.obtainMessage(FLAG, R.string.update_mcu_data, 0).sendToTarget();
                        break;
                    case MubUpdate.MUD_FLAG_CHECKSUM:
                        mProgressHandler.obtainMessage(FLAG, R.string.update_mcu_checksum, 0).sendToTarget();
                        break;
                    case MubUpdate.MUD_FLAG_RESET:
                        mProgressHandler.obtainMessage(FLAG, R.string.update_mcu_reset, 0).sendToTarget();
                        mProgressHandler.sendEmptyMessageDelayed(RESET, 2000);
                        break;
                }
            }
        }
    };


    private static List<StorageInfo> listAllStorage(Context context) {
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

    private static final String MCU_UPDATE_GUIDE_FILE_NAME = "update.bin";
    private static final String MCU_UPDATE_GUIDE_FILE_NAME_RK = "update2.bin";
    private static final String MCU_UPDATE_GUIDE_FILE_NAME3 = "update3.bin";

    private String getMcuImagePath() {
        List<StorageInfo> list = listAllStorage(this);
        String path = null;
        for (int i = 0; i < list.size(); ++i) {

            StorageInfo si = list.get(i);
            // if (si.mType == StorageInfo.TYPE_SD) {
            //
            // } else if (si.mType == StorageInfo.TYPE_USB) {
            if (Util.isRKSystem()) {
                path = si.mPath + "/" + MCU_UPDATE_GUIDE_FILE_NAME_RK;
            } else {
                path = si.mPath + "/" + MCU_UPDATE_GUIDE_FILE_NAME;
            }
            File f = new File(path);
            if (f.exists()) {
                return path;
            } else {
                path = si.mPath + "/" + MCU_UPDATE_GUIDE_FILE_NAME3;
                f = new File(path);
                if (f.exists()) {
                    return path;
                }
            }
            // }
        }


        return null;
    }

    private boolean updateMcu() {

        if (Build.VERSION.SDK_INT >= 25) {
            mMubUpdate.mPath = getMcuImagePath();
            if (mMubUpdate.mPath != null) {
                if (mMubUpdate.sendMUDCommand2(mMubUpdate.mPath) >= 0) {
                    return true;
                }
            }
        } else {
            if (mMubUpdate.sendMUDCommand(MubUpdate.MUD_START) >= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_manager);
        mMubUpdate.setMUDMask(MubUpdate.MUD_POS | MubUpdate.MUD_FLAG);
        mMubUpdate.setMUDCallback(mMUDCallback);

        ((Button) findViewById(R.id.update_mcu)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // showDialog(UPDATE_MCU);
                if (!updateMcu()) {
                    Toast.makeText(getBaseContext(), "open file error!", Toast.LENGTH_LONG).show();
                } else {
                    showDialog(PROCESS);
                }
            }
        });

        ((Button) findViewById(R.id.back)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case UPDATE_MCU: {
                //			return new AlertDialog.Builder(UpdateManager.this)
                //					.setTitle(getString(R.string.sure_update_mcu))
                //					.setPositiveButton(R.string.alert_dialog_ok,
                //							new DialogInterface.OnClickListener() {
                //								public void onClick(DialogInterface dialog,
                //										int whichButton) {
                //									if (mMubUpdate
                //											.sendMUDCommand(MubUpdate.MUD_START) < 0) {
                //										Toast.makeText(getBaseContext(),
                //												"open file error!",
                //												Toast.LENGTH_LONG).show();
                //									} else {
                //										showDialog(PROCESS);
                //									}
                //
                //								}
                //							})
                //					.setNegativeButton(R.string.alert_dialog_cancel,
                //							new DialogInterface.OnClickListener() {
                //								public void onClick(DialogInterface dialog,
                //										int whichButton) {
                //								}
                //							}).create();
            }
            break;
            case PROCESS: {
                mProgressDialog = new ProgressDialog(UpdateManager.this);
                mProgressDialog.setTitle(R.string.update_mcu_title);
                mProgressDialog.setMessage(getString(R.string.update_mcu_load));
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setMax(2047);
                return mProgressDialog;
            }

        }
        return null;
    }

    class CMDThread extends Thread {

        private int cmd;
        private String[] files;

        CMDThread(int command, String[] cmdFiles) {
            super();
            cmd = command;
            files = cmdFiles;

        }

        @Override
        public void run() {
            Command mCommand = new Command(cmd, files, new CommandCallBack() {

                public int replace(String source) {
                    // TODO Auto-generated method stub
                    return 0;
                }

                public void noSpace(String source) {
                    mProgressHandler.obtainMessage(SPACENOTENOUGH).sendToTarget();
                    mProgressDialog.dismiss();
                }

                public void processItem(File file, long total, long current) {
                    // TODO Auto-generated method stub

                }

                public void cancel() {
                    // TODO Auto-generated method stub

                }

                public void error(int code) {
                    // TODO Auto-generated method stub

                }
            });
            Log.e(TAG, "copy " + files[0] + " to " + TARGETPATH);
            if (Command.SUCCESS != mCommand.exec(TARGETPATH)) {
                new Command(Command.DELETE, new String[]{TARGETPATH}, null).exec(TARGETPATH);

            }

            if (mStep < 100) {
                TimerTask task = new TimerTask() {
                    public void run() {
                        mProgressHandler.obtainMessage(UPDATEPROCESS).sendToTarget();
                    }
                };
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer.purge();
                }
                mTimer = new Timer(true);
                mTimer.schedule(task, 100, 100);
            }

            mIsOver = true;
            Log.e(TAG, "copy over mIsOver=" + mIsOver);
            super.run();
        }

    }

    ;


    public String formatSize(long size) {
        return Formatter.formatFileSize(this, size);
    }

    public long getAvailableSpace() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        long availableBlocks = stat.getAvailableBlocks();

        String sdSize = formatSize(totalBlocks * blockSize);
        Log.i(TAG, "SD size: " + sdSize + "  " + totalBlocks * blockSize);

        String sdAvail = formatSize(availableBlocks * blockSize);
        Log.i(TAG, "SD space:" + sdAvail);
        return availableBlocks * blockSize;
    }

    class GetAvailableSpaceThread extends Thread {

        @Override
        public void run() {
            File targetFile = new File(TARGETPATH);
            if (MINSPACE == -1) {
                MINSPACE = Command.getFileSize(new File(SOURCEPATH));
            }
            /*
             * if (!targetFile.exists()) { Log.e(TAG,
             * "target path file not exist"); targetFile.mkdir(); if
             * (!(getAvailableSpace() > MINSPACE)) {
             * mProgressHandler.obtainMessage(SPACENOTENOUGH).sendToTarget();
             * mProgressDialog.dismiss(); MINSPACE=-1; return; } } else
             */
            {
                Log.e(TAG, "target path file exist");
                // new Command(Command.DELETE, new String[] { BACKUPPATH },
                // null).exec(BACKUPPATH);
                if (targetFile.exists()) new Command(Command.DELETE, new String[]{TARGETPATH}, null).exec(TARGETPATH);
                if (!(getAvailableSpace() > MINSPACE)) {
                    mProgressHandler.obtainMessage(SPACENOTENOUGH).sendToTarget();
                    mProgressDialog.dismiss();
                    MINSPACE = -1;
                    return;
                }
            }
            new Command(Command.COPY, new String[]{SOURCEPATH}, null).exec(TARGETPATH);

            new CMDThread(Command.PASTE, new String[]{SOURCEPATH}).start();
            super.run();
        }

    }

    private String[] getParameter(final File file) {
        BufferedReader buf;
        String result[];
        String source = null;
        try {
            buf = new BufferedReader(new FileReader(file));
            source = buf.readLine();
            buf.close();
            Log.e(TAG, source);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (source != null) {
            result = source.split("#");
            return result;
        }
        return null;
    }

    public class MyExpandableListAdapter extends BaseExpandableListAdapter {
        // Sample data set. mChildren[i] contains the mChildren (String[]) for
        // groups[i].
        // private String[] groups = { "People Names", "Dog Names", "Cat Names",
        // "Fish Names" };

        public MyExpandableListAdapter() {
            mChildren = new File[3][];
            mChildren[0] = new File("/mnt/extsd/").listFiles(mFileFilter);
            mChildren[1] = new File("/mnt/extsd2/").listFiles(mFileFilter);
            mChildren[2] = new File("/mnt/udisk/").listFiles(mFileFilter);
        }

        public Object getChild(int groupPosition, int childPosition) {
            return mChildren[groupPosition][childPosition].getName();
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            if (mChildren[groupPosition] != null) {
                return mChildren[groupPosition].length;
            } else {
                return 0;
            }
        }

        public TextView getGenericView() {
            // Layout parameters for the ExpandableListView
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 64);

            TextView textView = new TextView(UpdateManager.this);
            textView.setLayoutParams(lp);
            // Center the text vertically
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            // Set the text starting position
            textView.setPadding(36, 0, 0, 0);
            return textView;
        }

        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            TextView textView = getGenericView();
            textView.setText(getChild(groupPosition, childPosition).toString());
            return textView;
        }

        public Object getGroup(int groupPosition) {
            if (groupPosition == 0) {
                return getString(R.string.ext_disk1);
            } else if (groupPosition == 1) {
                return getString(R.string.ext_disk2);
            } else {
                return getString(R.string.u_disk);
            }
        }

        public int getGroupCount() {
            return 3;
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            TextView textView = getGenericView();
            textView.setText(getGroup(groupPosition).toString());
            return textView;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean hasStableIds() {
            return true;
        }

    }

    static class MCUFirmwareVerify {

        static boolean MCUFirmwareVerify() {
            String thumbs;
            String dataFile = "/mnt/extsd2/update.bin";

            if (Build.DEVICE.compareTo("imx53_smd") == 0 || Build.MODEL.compareTo("imx53_smd") == 0) thumbs = "www.my.com.cn.imx53";
            else return true;

            String dataMD5 = MD5File(thumbs, dataFile);
            String digest = readDigest(dataFile + ".md5");
            if (digest.length() > 32) digest = digest.substring(0, 32);
            Log.e("MCUFWCHK", "[" + dataMD5 + "]" + "[" + digest + "]");
            if (dataMD5.length() == 0 || digest.length() == 0 || dataMD5.compareToIgnoreCase(digest) != 0) {
                Log.e("MCUFWCHK", "MCUFirmware for " + Build.DEVICE + " is invalid");
                return false;
            } else {
                Log.e("MCUFWCHK", "MCUFirmware for " + Build.DEVICE + " is valid");
                return true;
            }
        }

        private static String MD5File(String thumbs, String fileName) {

            File file = new File(fileName);
            if (file.exists()) {
                byte[] bytes = new byte[1024];
                FileInputStream in;
                MessageDigest digester = null;
                try {
                    digester = MessageDigest.getInstance("MD5");
                } catch (NoSuchAlgorithmException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                try {
                    in = new FileInputStream(file);
                    int byteCount;
                    while ((byteCount = in.read(bytes)) > 0) {
                        digester.update(bytes, 0, byteCount);
                    }
                    digester.update(thumbs.getBytes(), 0, thumbs.length());
                    byte[] digest = digester.digest();
                    return hex2String(digest);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("MCUFWCHK", fileName + " not exist!!!");
            }
            return "";
        }

        private static String hex2String(byte[] toencode) {
            StringBuilder sb = new StringBuilder(toencode.length * 2);
            for (byte b : toencode) {
                sb.append(Integer.toHexString((b & 0xf0) >>> 4));
                sb.append(Integer.toHexString(b & 0x0f));
            }
            return sb.toString().toLowerCase();
        }

        public static String readDigest(String fileName) {
            byte[] buff = new byte[32];
            File file = new File(fileName);
            StringBuffer str = new StringBuffer();

            if (file.exists()) {
                FileInputStream in = null;
                try {
                    int realSize;
                    in = new FileInputStream(file);
                    realSize = in.read(buff);
                    str.append(new String(buff, 0, realSize));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (in != null) try {
                        in.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } else {
                Log.e("MCUFWCHK", fileName + " not exist!!!");
            }
            return str.toString();
        }
    }

}