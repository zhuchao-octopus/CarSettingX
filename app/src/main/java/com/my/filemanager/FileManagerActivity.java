package com.my.filemanager;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.common.util.MachineConfig;
import com.common.util.Util;
import com.octopus.android.carsettingx.R;

import android.os.UserHandle;

public class FileManagerActivity extends Activity implements OnItemClickListener, OnItemLongClickListener, OnTouchListener {
	private final static String TAG = "AppInstallActivity";

	private static final String APKFILESUFFIX = ".apk";
	private static final String PICFILESUFFIX = ".png.bmp";

	private String mExtFile = ".apk";
	private ListView mFileList;
	private List<String> mItems = null;
	private String mStartFilePath = SD0PATH;
	private String mCurrentFilePath = SD0PATH;
	private final static String MEDIADIRPATH = "/mnt/sdcard/media/";
	private final static String HOMEDIRPATH = "/storage/sdcard1/";
	private final static String UDISKPATH = "/storage/usbdisk1/";

	
	private static String SD0PATH = "/mnt/paramter/apk/";
	private  static String SD1PATH = "/storage/sdcard1/";
	private  static String SD2PATH = "/storage/sdcard2/";
	private  static String USB1PATH = "/storage/usbdisk1/";
	private  static String USB2PATH = "/storage/usbdisk2/";
	private  static String USB3PATH = "/storage/usbdisk3/";
	private  static String USB4PATH = "/storage/usbdisk4/";
	private IconicAdapter mFileListAdapter;
	private final static int FOLDERCHANG = 0x00;
	private final static int MULTISELECTION = 0x01;
	private final static int OPERATIONREJECT = 0x02;
	private final static int AVAILABLESPACE = 0x03;
	private final static int SHOWDIALOG = 0x04;
	private final static int STARTCOM = 0x05;
	private final static int DISMISSDIALOG = 0x06;
	private final static int REPLACEDIALOG = 0x07;
	private final static int NOSPACEDIALOG = 0x08;
	private final static int PROCESSFILE = 0x09;
	private final static int CANCEL = 0x10;
	private static final int FAULTYOPERATION = 0x11;
	private static final int OPERATIONERROR = 0x12;

	private TextView mCurrentPath;
	private boolean mIsMultiSelection = false;
	private List<Integer> mCheckedItem;
	private BroadcastReceiver mUnmountReceiver = null;
	private int mReplate = -1;
	private int mOperateItem = -1;

	private Command mCommand;

	private Button mSelect_all;

	private AlertDialog alertDialog;
	private ProgressDialog mpDialog;
	private CMDThread mThread;
	private String mNewFileName = null;

	private boolean mCanGetSpace = false;
	private boolean mGettingFiles = false;
	private int mCmdText;
	private String[] mCmdFiles;
	private FileFilter mFileFilter = new FileFilter() {

		public boolean accept(File pathname) {
			if (pathname.getName().startsWith(".")) {
				return false;
			} else if (pathname.isFile()) {
				if (pathname.getName().toLowerCase().endsWith(mExtFile)) {
					return true;
				} else
					return false;
			} else
				return true;
		}

	};

	public static final String SERVICECMD = "com.android.music.musicservicecommand";

	private final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case FOLDERCHANG:
				
				mItems.clear();
				File[] files = new File(mCurrentFilePath).listFiles(mFileFilter);
				if (files != null) {
					for (File file : files) {
						if (file.isDirectory()){
							
							Log.d("abc", SD0PATH+":"+mCurrentFilePath);
							if (SD0PATH.equals(mCurrentFilePath)){
								Log.d("abc", SD0PATH+":"+file.getPath().toString());
								if(!file.getPath().toString().equals(SD0PATH+"apk")){
									continue;
								}
							} 
								mItems.add(file.getPath().toString());
							
						}
					}
					for (File file : files) {
						
						
						if (!file.isDirectory())
							mItems.add(file.getPath().toString());
					}
				}
				if (mCheckedItem != null)
					mCheckedItem.clear();
				mIsMultiSelection = false;
				mSelect_all.setEnabled(false);
				mCurrentPath.setText(mCurrentFilePath);
				mFileListAdapter.notifyDataSetChanged();
				if (mCanGetSpace)
					((TextView) findViewById(R.id.space)).setText(getAvailableSpace(getCurrentPath()));

				break;
			case MULTISELECTION:
				mFileListAdapter.notifyDataSetChanged();
				if (mCanGetSpace)
					((TextView) findViewById(R.id.space)).setText(getAvailableSpace(getCurrentPath()));
				mFileList.invalidate();
				break;
			case OPERATIONREJECT:
				Toast.makeText(getBaseContext(), R.string.operate_failed, Toast.LENGTH_SHORT).show();
				break;
			case AVAILABLESPACE:
				((TextView) findViewById(R.id.space)).setText(getAvailableSpace(getCurrentPath()));
				break;
			case SHOWDIALOG:
				mpDialog = new ProgressDialog(FileManagerActivity.this);
				mpDialog.setOnCancelListener(new OnCancelListener() {

					public void onCancel(DialogInterface dialog) {
						Log.e(TAG, "setOnCancelListener");
						mCommand.stop();
					}
				});
				mpDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 鐠佸墽鐤嗘搴㈢壐娑撳搫娓捐ぐ銏ｇ箻鎼达附娼�
				mpDialog.setTitle(null);// 鐠佸墽鐤嗛弽鍥暯
				mpDialog.setIcon(null);// 鐠佸墽鐤嗛崶鐐垼
				mpDialog.setMax(100);
				mpDialog.setMessage(getString(R.string.start) + getResources().getStringArray(R.array.operation_array)[msg.arg1]);
				mpDialog.show();
				break;
			case DISMISSDIALOG:
				if (mpDialog != null && mpDialog.isShowing())
					mpDialog.dismiss();
				break;

			case REPLACEDIALOG:
				AlertDialog.Builder builder = new AlertDialog.Builder(FileManagerActivity.this).setIcon(android.R.drawable.ic_dialog_info)
						.setTitle(String.format(getString(R.string.replace_file), msg.obj))
						.setPositiveButton(R.string.replace, new OnClickListener() {

							public void onClick(DialogInterface dialog, int which) {
								mReplate = CommandsInterface.REPLACE;
								mHandler.obtainMessage(SHOWDIALOG, mCmdText, 0).sendToTarget();
							}
						}).setNegativeButton(R.string.cancel, new OnClickListener() {

							public void onClick(DialogInterface dialog, int which) {
								mReplate = CommandsInterface.CANCEL;

							}
						}).setCancelable(false);
				if (Command.operate_files.length > 1) {
					builder.setNeutralButton(R.string.skip, new OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							mReplate = CommandsInterface.SKIP;
							mHandler.obtainMessage(SHOWDIALOG, mCmdText, 0).sendToTarget();
						}
					});
				}
				alertDialog = builder.create();
				alertDialog.show();
				break;
			case STARTCOM:
				mThread = new CMDThread(mCmdText, mCmdFiles);
				mThread.start();
				break;
			case NOSPACEDIALOG:
				alertDialog = new AlertDialog.Builder(FileManagerActivity.this).setIcon(android.R.drawable.ic_dialog_info)
						.setTitle(getString(R.string.no_space_title)).setMessage(String.format(getString(R.string.no_space), msg.obj))
						.setPositiveButton(R.string.ok, new OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
							}
						}).create();
				alertDialog.show();
				break;
			case PROCESSFILE:
				Bundle bundle = msg.getData();
				if (mpDialog != null && mpDialog.isShowing()){
				mpDialog.setMessage(getString(R.string.start) + getResources().getStringArray(R.array.operation_array)[msg.arg1]
						 + "  " + bundle.getString("name"));
				int current = (int) ((float) ((float) bundle.getLong("current") / (float) bundle.getLong("total")) * 100);
				mpDialog.setProgress(current);
				}
				break;
			case CANCEL:
				if (mpDialog != null && mpDialog.isShowing())
					mpDialog.dismiss();
				break;
			case FAULTYOPERATION:
				Toast.makeText(getBaseContext(), R.string.faulty_operation, Toast.LENGTH_SHORT).show();
				break;
			case OPERATIONERROR:
				Toast.makeText(getBaseContext(), R.string.operation_error, Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.file_browse);
		initDiskPath();
		
		String value = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_SYSTEM_UI);
		if (MachineConfig.VALUE_SYSTEM_UI20_RM10_1.equals(value)
				|| MachineConfig.VALUE_SYSTEM_UI21_RM10_2.equals(value)) {	
			SD0PATH = "/mnt/paramter/";
		}

		mStartFilePath = SD0PATH;
		mCurrentFilePath = mStartFilePath;
		udpateOpneFileType(this.getIntent());
		
		mFileList = (ListView) findViewById(R.id.fileList);
		mCurrentPath = (TextView) findViewById(R.id.current_path);
		mCurrentPath.setText(mStartFilePath);
		new GetAvailableSpaceThread().start();
		mItems = new ArrayList<String>();
		File[] files = new File(mStartFilePath).listFiles(mFileFilter);
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()){
					
					Log.d("abc", SD0PATH+":"+mCurrentFilePath);
					if (SD0PATH.equals(mCurrentFilePath)){
						Log.d("abc", SD0PATH+":"+file.getPath().toString());
						if(!file.getPath().toString().equals(SD0PATH+"apk")){
							continue;
						}
					} 
					
					mItems.add(file.getPath().toString());
				}
			}
			for (File file : files) {
				if (!file.isDirectory())
					mItems.add(file.getPath().toString());
			}
		}
		mFileListAdapter = new IconicAdapter(R.layout.file_list_view, mItems);

		mFileList.setAdapter(mFileListAdapter);
		mFileList.setOnItemClickListener(this);
		mFileList.setOnItemLongClickListener(this);
		// mFileList.setOnItemSelectedListener(this);
		mSelect_all = (Button) findViewById(R.id.select_all);
		mSelect_all.setEnabled(false);
		mFileList.setOnTouchListener(this);
		mFileList.setItemsCanFocus(false);

		
		super.onCreate(savedInstanceState);
		
	}

	private void udpateOpneFileType(Intent intent) {
		if (intent != null) {
			mOpenFileType = intent.getStringExtra("type");
			if(mOpenFileType!=null){
				mExtFile = mOpenFileType;
//				mStartFilePath = "/mnt/paramter/logo/";

				if (checkStorageIsMounted(SD1PATH)) {
					mStartFilePath = SD1PATH;
				} else if (checkStorageIsMounted(SD2PATH)) {

					mStartFilePath = SD2PATH;
				} else if (checkStorageIsMounted(SD1PATH)) {

					mStartFilePath = SD1PATH;
				} else if (checkStorageIsMounted(USB1PATH)) {

					mStartFilePath = USB1PATH;
				} else if (checkStorageIsMounted(USB2PATH)) {

					mStartFilePath = USB2PATH;
				} else if (checkStorageIsMounted(USB3PATH)) {

					mStartFilePath = USB3PATH;
				} else if (checkStorageIsMounted(USB4PATH)) {

					mStartFilePath = USB4PATH;
				}
			}
		}
	}
	@Override
	protected void onNewIntent(Intent intent) {
		
	};
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	class ViewWrapper {
		View base;
		TextView label = null;
		TextView fileSizeLabel = null;
		ImageView icon = null;
		String filePath = null;
		String fileSize = null;
		int id = -1;

		ViewWrapper(View base) {
			this.base = base;
			id = -1;
		}

		TextView getLabel() {
			if (label == null) {
				label = (TextView) base.findViewById(R.id.file_name);
			}
			return (label);
		}

		ImageView getIcon() {
			if (icon == null) {
				icon = (ImageView) base.findViewById(R.id.icon);
			}

			return (icon);
		}

		int getId() {
			return id;
		}

		void setId(int deviceId) {
			id = deviceId;
		}

		String getFilePath() {
			return filePath;
		}

		void setFilePath(String path) {
			filePath = path;
		}

		String getFileSize() {
			return fileSize;
		}

		void setFileSize(String size) {
			fileSize = size;

			if (fileSizeLabel == null) {
				fileSizeLabel = (TextView) base.findViewById(R.id.file_size);
			}
			fileSizeLabel.setText(size);
		}

		TextView getFileSizeLabel() {
			if (fileSizeLabel == null) {
				fileSizeLabel = (TextView) base.findViewById(R.id.file_size);
			}
			return fileSizeLabel;
		}
	}

	public PackageInfo getPackageInfo(Context context, String apkFilepath) {
		PackageManager pm = context.getPackageManager();
		PackageInfo pkgInfo = null;
		try {
			pkgInfo = pm.getPackageArchiveInfo(apkFilepath, 0);
		} catch (Exception e) {
			// should be something wrong with parse
			e.printStackTrace();
		}
		return pkgInfo;
	}

	public Drawable getAppIcon(Context context, String apkFilepath) {
		PackageManager pm = context.getPackageManager();
		PackageInfo pkgInfo = getPackageInfo(context, apkFilepath);
		if (pkgInfo == null) {
			return null;
		}

		ApplicationInfo appInfo = pkgInfo.applicationInfo;
		if (Build.VERSION.SDK_INT >= 8) {
			appInfo.sourceDir = apkFilepath;
			appInfo.publicSourceDir = apkFilepath;
		}
		return pm.getApplicationIcon(appInfo);
	}

	class IconicAdapter extends ArrayAdapter<String> {
		private List<String> listItems;
		private int layout;

		IconicAdapter(int layout, List<String> listItems) {
			super(FileManagerActivity.this, layout, listItems);
			this.listItems = listItems;
			this.layout = layout;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			ViewWrapper wrapper = null;

			if (row == null) {
				LayoutInflater inflater = getLayoutInflater();

				row = inflater.inflate(layout, parent, false);
				wrapper = new ViewWrapper(row);
				row.setTag(wrapper);
			} else {
				wrapper = (ViewWrapper) row.getTag();
			}
			CheckBox cb = (CheckBox) row.findViewById(R.id.checkBox);
			if (mIsMultiSelection) {
				cb.setClickable(false);
				if (mCheckedItem.contains(position)) {
					cb.setChecked(true);
				} else
					cb.setChecked(false);
				cb.setVisibility(View.VISIBLE);
			} else {
				cb.setVisibility(View.GONE);
			}
			// if (BluetoothApplication.getConnectType()>0 && wrapper.getIcon()
			// != null && position ==
			// BluetoothApplication.getConnectedDevice()-1) {
			// wrapper.getIcon().setVisibility(ImageView.VISIBLE);
			// }else{
			// wrapper.getIcon().setVisibility(ImageView.GONE);
			// }
			File file = new File(listItems.get(position));
			if (file.isDirectory()) {
				wrapper.getIcon().setImageResource(R.drawable.resource_folder);
				wrapper.getFileSizeLabel().setVisibility(View.GONE);
			} else {
				//Log.e(TAG,"file name = "+file.getName());
				String suffix ="";
				if(file.getName().lastIndexOf(".")>0){
					suffix = file.getName().toLowerCase().substring(file.getName().lastIndexOf("."), file.getName().length());
				}
				//Log.e(TAG,"suffix = "+suffix);
				if (suffix.equals(APKFILESUFFIX)) {
					Drawable drawable = getAppIcon(FileManagerActivity.this, listItems.get(position));
					if (drawable != null)
						wrapper.getIcon().setImageDrawable(drawable);
					else
						wrapper.getIcon().setImageResource(R.drawable.file_ext_apk);
				} else if (PICFILESUFFIX.contains(suffix)) {
					wrapper.getIcon().setImageResource(R.drawable.file_ext_pic);
				}
				else {
					wrapper.getIcon().setImageResource(R.drawable.resource_unknown_file);
				}
				wrapper.getFileSizeLabel().setVisibility(View.VISIBLE);
				wrapper.setFileSize(formatSize(Command.getFileSize(file)));
			}
			wrapper.setFilePath(listItems.get(position));
			wrapper.getLabel().setText(file.getName());
			return (row);
		}
	}
	private String mOpenFileType = null;
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		ViewWrapper tag = (ViewWrapper) view.getTag();
		File file = new File(tag.getFilePath());
		if (!mIsMultiSelection) {
			if (file.isDirectory()) {
				mCurrentFilePath = file.getPath();
				mCurrentFilePath += "/";
				mHandler.obtainMessage(FOLDERCHANG).sendToTarget();
			} else {
				if(mOpenFileType==null){
					openFile(file);
				}else{
					returnFile(file);
				}
			}
		} else {
			if (mCheckedItem.contains(position)) {
				mCheckedItem.remove((Object) position);
			} else
				mCheckedItem.add(position);
			mHandler.obtainMessage(MULTISELECTION).sendToTarget();
		}
		// mHandler.obtainMessage(FOLDERCHANG).sendToTarget();
	}

	public void processBtnFun(View view) {
        int id = view.getId();
        if (id == R.id.media_folder) {
            mCurrentFilePath = MEDIADIRPATH;
            mHandler.obtainMessage(FOLDERCHANG).sendToTarget();
        } else if (id == R.id.u_disk) {
            mCurrentFilePath = UDISKPATH;
            mHandler.obtainMessage(FOLDERCHANG).sendToTarget();
        } else if (id == R.id.home_folder) {
            mCurrentFilePath = HOMEDIRPATH;
            mHandler.obtainMessage(FOLDERCHANG).sendToTarget();
        } else if (id == R.id.multi_selection) {
            if (mIsMultiSelection) {
                mCheckedItem.clear();
                mCheckedItem = null;
                mIsMultiSelection = false;
                mSelect_all.setEnabled(false);
            } else {
                mCheckedItem = new ArrayList<Integer>();
                mIsMultiSelection = true;
                mSelect_all.setEnabled(true);
            }
            mHandler.obtainMessage(MULTISELECTION).sendToTarget();
        } else if (id == R.id.select_all) {
            if (mIsMultiSelection) {
                if (mCheckedItem.size() == mFileList.getCount()) {
                    mCheckedItem.clear();
                } else {
                    mCheckedItem.clear();
                    for (int i = 0; i < mFileList.getCount(); i++) {
                        mCheckedItem.add(i);
                    }
                }
            }
            mHandler.obtainMessage(MULTISELECTION).sendToTarget();
        } else if (id == R.id.super_dir) {
            if (SD0PATH.equals(mCurrentFilePath) || SD1PATH.equals(mCurrentFilePath) || SD2PATH.equals(mCurrentFilePath) || USB1PATH.equals(mCurrentFilePath) || USB2PATH.equals(mCurrentFilePath) || USB3PATH.equals(mCurrentFilePath) || USB4PATH.equals(mCurrentFilePath)) {
                // do nothing here. because we donot want user see more dir than media storage
            } else {
                if (mCurrentFilePath.length() > 1) mCurrentFilePath = mCurrentFilePath.substring(0, mCurrentFilePath.length() - 2);
                mCurrentFilePath = mCurrentFilePath.substring(0, mCurrentFilePath.lastIndexOf("/") == 0 ? 1 : mCurrentFilePath.lastIndexOf("/") + 1);
            }
            mHandler.obtainMessage(FOLDERCHANG).sendToTarget();
        } else if (id == R.id.btn_sd) {
            mCurrentFilePath = SD0PATH;
            mHandler.obtainMessage(FOLDERCHANG).sendToTarget();
        } else if (id == R.id.btn_sd1) {
            mCurrentFilePath = SD1PATH;
            mHandler.obtainMessage(FOLDERCHANG).sendToTarget();
        } else if (id == R.id.btn_sd2) {
            mCurrentFilePath = SD2PATH;
            mHandler.obtainMessage(FOLDERCHANG).sendToTarget();
        } else if (id == R.id.btn_usb1) {
            mCurrentFilePath = USB1PATH;
            mHandler.obtainMessage(FOLDERCHANG).sendToTarget();
        } else if (id == R.id.btn_usb2) {
            mCurrentFilePath = USB2PATH;
            mHandler.obtainMessage(FOLDERCHANG).sendToTarget();
        } else if (id == R.id.btn_usb3) {
            mCurrentFilePath = USB3PATH;
            mHandler.obtainMessage(FOLDERCHANG).sendToTarget();
        } else if (id == R.id.btn_usb4) {
            mCurrentFilePath = USB4PATH;
            mHandler.obtainMessage(FOLDERCHANG).sendToTarget();
        }

	}
	

	@Override
	public void onResume() {
		super.onResume();
		mUnmountReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				mHandler.postDelayed(new Runnable() {
					public void run() {
						if(action.equals(Intent.ACTION_MEDIA_MOUNTED)){
							initDiskPath();
						}
						
						mCanGetSpace = false;
						new GetAvailableSpaceThread().start();
						updateStorageView();
						mHandler.obtainMessage(FOLDERCHANG).sendToTarget();
					}
				}, 250);
				
			}
		};
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		iFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		iFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
		iFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		iFilter.addDataScheme("file");
		registerReceiver(mUnmountReceiver, iFilter);
		updateStorageView();
	}
	@Override
	public void onPause() {
		super.onPause();
//		mOpenFileType=null;
//		finish();
		Util.sudoExecNoCheck("sync");
	}
	private static boolean checkStorageIsMounted(String path) {

		File sdcardDir = new File(path);
		if (sdcardDir.exists()) {
			String state = Environment.getExternalStorageState(sdcardDir);
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				return true;
			}
		}
		return false;
	}
	
	private void checkStorageIsMountedView(String path, int id) {
		View v = findViewById(id);
		if (v != null) {

			if (path.contains("MediaCard")){
				((Button)v).setText("SD");
			} else if (path.contains("GPS")){
				((Button)v).setText("GPS");
			}
			
			if (checkStorageIsMounted(path)) {
				v.setVisibility(View.VISIBLE);
			} else {
				v.setVisibility(View.GONE);
			}
		}
	}

	private void updateStorageView() {
		checkStorageIsMountedView(SD1PATH, R.id.btn_sd1);
		checkStorageIsMountedView(SD2PATH, R.id.btn_sd2);
		checkStorageIsMountedView(USB1PATH, R.id.btn_usb1);
		checkStorageIsMountedView(USB2PATH, R.id.btn_usb2);
		checkStorageIsMountedView(USB3PATH, R.id.btn_usb3);
		checkStorageIsMountedView(USB4PATH, R.id.btn_usb4);
		// checkStorageIsMountedView(MusicPlayer.MEDIA_USB2_PATH,
		// R.id.btn_usb2);

	}


	// OnCheckedChangeListener listener = new OnCheckedChangeListener(){
	//
	// public void onCheckedChanged(CompoundButton buttonView, boolean
	// isChecked) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// };
	// OnClickListener onclick = new OnClickListener(){
	//
	// public void onClick(View v) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// };

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 0:
			alertDialog = new AlertDialog.Builder(FileManagerActivity.this).setIcon(null)
					.setItems(R.array.operation_array, new OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							String files[];
							if (mIsMultiSelection && mCheckedItem.size() > 0) {
								files = new String[mCheckedItem.size()];
								for (int i = 0; i < mCheckedItem.size(); i++) {
									files[i] = mItems.get(mCheckedItem.get(i).intValue());
								}
							} else {
								if (mOperateItem != -1) {
									files = new String[1];
									files[0] = mItems.get(mOperateItem);
								} else {
									files = new String[0];
								}
							}
							alertDialog.dismiss();
							mCmdText = which;
							mCmdFiles = files;

							if (which == Command.DELETE) {
								FileManagerActivity.this.showDialog(2);
							} else if (which == Command.RENAME) {
								if (mCmdFiles.length != 1) {
									mHandler.obtainMessage(FAULTYOPERATION).sendToTarget();
									return;
								}
								FileManagerActivity.this.showDialog(3);
							} else {
								if (which == Command.PASTE)
									mHandler.obtainMessage(SHOWDIALOG, which, 0).sendToTarget();
								// CMDThread thread = new CMDThread(which,
								// files);
								// thread.start();

								mHandler.obtainMessage(STARTCOM).sendToTarget();
							}
						}

					}).setTitle(null).setNegativeButton(getString(R.string.cancel), new OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();

						}
					}).create();
			return alertDialog;

		case 2:
			alertDialog = new AlertDialog.Builder(FileManagerActivity.this).setIcon(android.R.drawable.ic_dialog_info)
					.setTitle(R.string.sure_delete).setNegativeButton(R.string.cancel, new OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();

						}
					}).setPositiveButton(R.string.ok, new OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// mHandler.obtainMessage(SHOWDIALOG, mCmdText,
							// 0).sendToTarget();
							mHandler.obtainMessage(STARTCOM).sendToTarget();

						}
					}).create();
			return alertDialog;
		case 3:
			LayoutInflater factory = LayoutInflater.from(this);
			final View textEntryView = factory.inflate(R.layout.rename_dialog_view, null);
			return new AlertDialog.Builder(FileManagerActivity.this).setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.new_name).setView(textEntryView)
					.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {

							/* User clicked OK so do some stuff */
						}
					}).setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {

							/* User clicked cancel so do some stuff */
						}
					}).create();
		}

		return alertDialog;
	}

	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
/*
		if (mCurrentFilePath.startsWith(MEDIADIRPATH) || mCurrentFilePath.startsWith(HOMEDIRPATH) || mCurrentFilePath.startsWith("/extsd/")
				|| mCurrentFilePath.startsWith("/sdcard/media/") || mCurrentFilePath.startsWith(UDISKPATH)
				|| mCurrentFilePath.startsWith("/udisk/")) {
			mOperateItem = position;
			showDialog(0);
		} else {
			mHandler.obtainMessage(OPERATIONREJECT).sendToTarget();
		}
*/
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
/*
		if (mCurrentFilePath.startsWith(MEDIADIRPATH) || mCurrentFilePath.startsWith(HOMEDIRPATH) || mCurrentFilePath.startsWith("/extsd/")
				|| mCurrentFilePath.startsWith("/sdcard/media/") || mCurrentFilePath.startsWith(UDISKPATH)
				|| mCurrentFilePath.startsWith("/udisk/")) {
			if (keyCode == KeyEvent.KEYCODE_MENU) {
				mOperateItem = -1;
				showDialog(0);
			}
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			mHandler.obtainMessage(OPERATIONREJECT).sendToTarget();

		}
*/
		if(keyCode==KeyEvent.KEYCODE_BACK){
			if ( SD0PATH.equals(mCurrentFilePath) ||
				SD1PATH.equals(mCurrentFilePath) ||
				SD2PATH.equals(mCurrentFilePath) ||
				USB1PATH.equals(mCurrentFilePath) ||
				USB2PATH.equals(mCurrentFilePath) ||
				USB3PATH.equals(mCurrentFilePath) ||
				USB4PATH.equals(mCurrentFilePath) ) {
				// do nothing here. because we donot want user see more dir than media storage
			} else {
				if (mCurrentFilePath.length() > 1){
					mCurrentFilePath = mCurrentFilePath.substring(0, mCurrentFilePath.length() - 2);
					mCurrentFilePath = mCurrentFilePath.substring(0, mCurrentFilePath.lastIndexOf("/") == 0 ? 1
						: mCurrentFilePath.lastIndexOf("/") + 1);
					mHandler.obtainMessage(FOLDERCHANG).sendToTarget();
					return true;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	class CMDThread extends Thread {

		private int cmd;
		private String[] files;

		CMDThread(int command, String[] cmdFiles) {
			super();
			cmd = command;
			files = cmdFiles;

		}

		public void stopCMD() {

		}

		@Override
		public void run() {
			mCommand = new Command(cmd, files, new CommandCallBack() {

				public int replace(String source) {
					mHandler.obtainMessage(DISMISSDIALOG).sendToTarget();
					mHandler.obtainMessage(REPLACEDIALOG, source).sendToTarget();
					while (mReplate == -1) {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					int result = mReplate;
					mReplate = -1;
					return result;
				}

				public void noSpace(String source) {

					mHandler.obtainMessage(DISMISSDIALOG).sendToTarget();
					mHandler.obtainMessage(NOSPACEDIALOG, source).sendToTarget();
				}

				public void processItem(File file, long total, long current) {
					Log.e(TAG, "Process File =" + file.toString());
					Message message = new Message();
					message.what = PROCESSFILE;
					Bundle bundle = new Bundle();
					bundle.putLong("total", total);
					bundle.putLong("current", current);
					bundle.putString("name", file.getName());
					message.setData(bundle);
					mHandler.sendMessage(message);
				}

				public void cancel() {
					Log.e(TAG, "command canceled");
					mHandler.obtainMessage(CANCEL).sendToTarget();
				}

				public void error(int code) {
					if (code == CommandsInterface.FAULTYOPERATION) {
						mHandler.obtainMessage(FAULTYOPERATION).sendToTarget();
					} else if (code == CommandsInterface.OPERATIONERROR) {
						mHandler.obtainMessage(OPERATIONERROR).sendToTarget();
					}

				}

			});
			if (cmd == Command.RENAME) {
				mCommand.setNewFileName(mNewFileName);
			}
			if (Command.SUCCESS != mCommand._exec(mCurrentFilePath)) {
				Log.e(TAG, "exec failed which=" + cmd);
			}

			/*
			 * if (cmd == Command.PASTE) { if (Command.operate_files != null) {
			 * for (int i = 0; i < Command.operate_files.length; i++) { if (new
			 * File(Command.operate_files[i]).isFile()) { Log.e(TAG,
			 * "sendBroadcast ACTION_MEDIA_SCANNER_SCAN_FILE path=" +
			 * Command.operate_files[i]); Intent intent = new
			 * Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"
			 * + Command.operate_files[i])); sendBroadcast(intent); } } } }
			 */
//			Intent musicCMD = new Intent(SERVICECMD);
//			musicCMD.putExtra("command", "pause");
//			sendBroadcast(musicCMD);
//			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
//			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + "/mnt/extsd")));
//			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + "/mnt/udisk")));
			if (mpDialog != null && mpDialog.isShowing())
				mpDialog.dismiss();
			mHandler.obtainMessage(FOLDERCHANG).sendToTarget();
			Log.e(TAG, "exec over");
			super.run();
		}

	};

	// 閺嶇厧绱￠崠锟芥潪顒�娑擄拷MB閺嶇厧绱�
	public String formatSize(long size) {
		return Formatter.formatFileSize(this, size);
	}

	public String getAvailableSpace(final File file) {
		// File path = Environment.getExternalStorageDirectory();
		if (file != null) {
			try{
			String path = file.getPath();
			StatFs stat = new StatFs(path);
			long blockSize = stat.getBlockSize();
			long totalBlocks = stat.getBlockCount();
			long availableBlocks = stat.getAvailableBlocks();
			String result = "";
			if (path.equals("/mnt/sdcard")) {
				result = getString(R.string.total_space);
			} else if (path.equals("/mnt/udisk")) {
				result = getString(R.string.udisd_total_space);
			} else {
				result = getString(R.string.micro_sd_total_space);
			}

			// SD閸椻剝锟界�褰掑櫤
			result += formatSize(totalBlocks * blockSize);
			result += "/";
			result += getString(R.string.available_space);
			// SD閸椻�澧挎担娆忣啇闁诧拷
			result += formatSize(availableBlocks * blockSize);
			Log.i(TAG, result);
			return result;
			}catch (Exception e){
				Log.e(TAG, ""+e);
			}
		} 

		
			return "";
	}

	class GetAvailableSpaceThread extends Thread {

		@Override
		public void run() {
			getAvailableSpace(Environment.getExternalStorageDirectory());
			getAvailableSpace(new File(HOMEDIRPATH));
			getAvailableSpace(new File(UDISKPATH));
			mHandler.obtainMessage(AVAILABLESPACE).sendToTarget();
			mCanGetSpace = true;
			super.run();
		}

	}

	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			ListView p = (ListView) v;
			if (p.pointToPosition((int) event.getX(), (int) event.getY()) < 0) {
				if (mCurrentFilePath.startsWith(MEDIADIRPATH) || mCurrentFilePath.startsWith(HOMEDIRPATH)
						|| mCurrentFilePath.startsWith("/extsd/") || mCurrentFilePath.startsWith("/sdcard/media/")
						|| mCurrentFilePath.startsWith(UDISKPATH) || mCurrentFilePath.startsWith("/udisk/")) {
					if (Command.preCommand != -1) {
						mOperateItem = -1;
						showDialog(0);
					}
				} else {
					mHandler.obtainMessage(OPERATIONREJECT).sendToTarget();

				}
			}
		}
		return false;
	}

	public static final String BROADCAST_RETURN_SELECT_FILE = "com.my.filemanager.BROADCAST_RETURN_SELECT_FILE";
	private void returnFile(File f) {
		Intent it = new Intent(BROADCAST_RETURN_SELECT_FILE);
		it.putExtra("path", f.getPath());
		it.setPackage(getPackageName());
				sendBroadcast(it);
		finish();
	}
	
	private void openFile(File f) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);

		if (Util.isAndroidLaterP()) {
			String type = getMIMEType(f);
			intent.setDataAndType(Uri.fromFile(f), type);		
//			intent.putExtra(Intent.EXTRA_AUTO_LAUNCH_SINGLE_CHOICE, false);
			intent.setComponent(new ComponentName("com.android.packageinstaller", "com.android.packageinstaller.InstallStart"));
			try {
				startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			/* 鐠嬪啰鏁etMIMEType()閺夈儱褰囧妗礽meType */
			String type = getMIMEType(f);
			int caritSource = 0;
			
			/* 鐠佸墽鐤唅ntent閻ㄥ垿ile娑撳懂imeType */
			intent.setDataAndType(Uri.fromFile(f), type);
			startActivity(intent);
		}
	}

	/* 閸掋倖鏌囬弬鍥︽MimeType閻ㄥ埓ethod */
	private String getMIMEType(File f) {
		String type = "";
		String fName = f.getName();
		/* 閸欐牕绶遍幍鈺佺潔閸氾拷*/
		String end = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toLowerCase();

		/* 娓氭繃澧跨仦鏇炴倳閻ㄥ嫮琚崹瀣枀鐎规瓉imeType */
		if (end.equals("m4a") || end.equals("mp3") || end.equals("mid") || end.equals("xmf") || end.equals("ogg") || end.equals("wav")|| end.equals("wma")|| end.equals("aac")|| end.equals("flac")) {
			type = "audio";
		} else if (end.equals("3gp") || end.equals("mp4")) {
			type = "video";
		} else if (end.equals("jpg") || end.equals("gif") || end.equals("png") || end.equals("jpeg") || end.equals("bmp")) {
			type = "image";
		} else if (end.equals("apk")) {
			/* android.permission.INSTALL_PACKAGES */
			type = "application/vnd.android.package-archive";
		} else {
			type = "*";
		}
		/* 婵″倹鐏夐弮鐘崇《閻╁瓨甯撮幍鎾崇磻閿涘苯姘ㄧ捄鍐插毉鏉烆垯娆㈤崚妤勩�缂佹瑧鏁ら幋鐑斤拷閹凤拷*/
		if (end.equals("apk")) {
		} else {
			type += "/*";
		}
		return type;
	}

	private File getCurrentPath() {
		if (mCurrentFilePath.startsWith("/sdcard/") || mCurrentFilePath.startsWith("/mnt/sdcard/")) {
			return Environment.getExternalStorageDirectory();
		}
		if (mCurrentFilePath.startsWith(HOMEDIRPATH) || mCurrentFilePath.startsWith("/extsd/")) {
			return new File(HOMEDIRPATH);
		}

		if (mCurrentFilePath.startsWith(UDISKPATH) || mCurrentFilePath.startsWith("/udisk/")) {
			return new File(UDISKPATH);
		}
		return null;

	}

	@Override
	protected void onStop() {
		if (mUnmountReceiver != null) {
			unregisterReceiver(mUnmountReceiver);
			mUnmountReceiver = null;
		}
		super.onStop();
	}
	
	//for 7.0
	
		public static class StorageInfo {
			public String mPath;
			public String mState;
			public int mType;

			public final static int TYPE_INTERAL = 0;
			public final static int TYPE_SD = 1;
			public final static int TYPE_USB = 2;
			

			public StorageInfo(String path, int type) {
				mPath = path;
				mType = type;
			}

//			public boolean isMounted() {
//				return "mounted".equals(state);
//			}
			
		}
		private static List<StorageInfo> listAllStorage(Context context) {
			ArrayList<StorageInfo> storages = new ArrayList<StorageInfo>();
			StorageManager storageManager = (StorageManager) context
					.getSystemService(Context.STORAGE_SERVICE);
			try {
				Class<?>[] paramClasses = {};
				Method getVolumeList = StorageManager.class.getMethod(
						"getVolumes", paramClasses);
				Object[] params = {};
				List<Object> VolumeInfo = (List<Object>) getVolumeList.invoke(storageManager,
						params);

				if (VolumeInfo != null) {
					for (Object volumeinfo: VolumeInfo) {				
						
						Method getPath = volumeinfo.getClass().getMethod(
								"getPath", new Class[0]);

						File path = (File) getPath.invoke(volumeinfo,
								new Object[0]);
						
						Method getDisk = volumeinfo.getClass().getMethod("getDisk",
								new Class[0]);

						Object diskinfo = getDisk.invoke(volumeinfo, new Object[0]);
						int type = StorageInfo.TYPE_INTERAL;
						if (diskinfo != null) {
							Method isSd = diskinfo.getClass().getMethod("isSd",
									new Class[0]);

							type = ((Boolean) isSd.invoke(diskinfo, new Object[0]))?
									StorageInfo.TYPE_SD:StorageInfo.TYPE_USB;

						}
						StorageInfo si = new StorageInfo (path.toString(), type);
						storages.add (si);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			storages.trimToSize();
			return storages;
		}

//		public  static String MEDIA_USB_PATH = "/storage/usbdisk1/";
//		public  static String MEDIA_USB2_PATH = "/storage/usbdisk2/";
//		public  static String MEDIA_USB3_PATH = "/storage/usbdisk3/";
//		public  static String MEDIA_USB4_PATH = "/storage/usbdisk4/";
//		public  static String MEDIA_SD_PATH = "/storage/sdcard1/";
//		public  static String MEDIA_SD2_PATH = "/storage/sdcard2/";
		private void initDiskPath(){
			List<StorageInfo> list = listAllStorage(this);

			int sd = 0;
			int usb = 2;
			for(int i = 0; i< list.size();++i){
				
				StorageInfo si = list.get(i);
				if (si.mType == StorageInfo.TYPE_SD) {
					++sd;
					switch (sd) {
					case 1:
						SD1PATH = si.mPath;
						break;
					case 2:
						SD2PATH = si.mPath;
						break;
					}
				} else if (si.mType == StorageInfo.TYPE_USB) {
					usb++;
					switch(usb){				
					case 3:
						USB1PATH = si.mPath;
						break;
					case 4:
						USB2PATH = si.mPath;
						break;
					case 5:
						USB3PATH = si.mPath;
						break;
					case 6:
						USB4PATH = si.mPath;
						break;
					}
				}
//				if(!si.path.equals(MEDIA_LOCAL_PATH)){
//					switch(i){
//					case 1:
//						MEDIA_SD_PATH = si.path;
//						break;
//					case 2:
//						MEDIA_SD2_PATH = si.path;
//						break;
//					case 3:
//						MEDIA_USB_PATH = si.path;
//						break;
//					case 4:
//						MEDIA_USB2_PATH = si.path;
//						break;
//					case 5:
//						MEDIA_USB3_PATH = si.path;
//						break;
//					case 6:
//						MEDIA_USB4_PATH = si.path;
//						break;
//					}
//				}
				
			}
		}


}
