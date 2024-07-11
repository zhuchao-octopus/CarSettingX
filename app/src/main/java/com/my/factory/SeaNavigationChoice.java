package com.my.factory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.common.util.MachineConfig;
import com.common.util.SystemConfig;
import com.common.util.SystemProperties;
import com.common.util.Util;
import com.common.util.UtilSystem;
import com.octopus.android.carsettingx.R;

import static android.provider.Settings.Secure.TTS_DEFAULT_SYNTH;

public class SeaNavigationChoice extends Preference {
	private ListView mAppList;
	private List<String> mItems = null;
	private IconicAdapter mAppListAdapter;
	private PackageManager mPackageManager;
	private ActivityManager mActivityManager;
	private List<ResolveInfo> mApps;
	private List<View> mViews;
	private AlertDialog mAlertDialog;
	private static final String TAG = "SeaNavigationChoice";
	// private static final String PROPERTIESFILE =
	// "/data/system/.properties_file";
	private String mPackageName = "";
	private String mClassName = "";
	private String mAppName = "";
	private NavigationChangeListener mNavigationChangeListener;
	private static final String PACK_NAME_IGO_PRIMO = "com.navngo.igo.javaclient";

	private String[] mGPSConfig = null;

	private boolean isConfigList(String name) {
		if (mGPSConfig != null) {
			for (int i = 0; i < mGPSConfig.length; ++i) {
				if (mGPSConfig[i].contains(name)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}
	private void update(Context context){
		if(context==null){
			return;
		}

		if (mGPSConfig == null) {
			String s = MachineConfig
					.getPropertyReadOnly(MachineConfig.KEY_GPS_CONFIG_LIST);
			if (s != null){
				mGPSConfig = s.split(";");
			}
		}
		
		mPackageManager = context.getPackageManager();
		mActivityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		mItems = new ArrayList<String>();
		mViews = new ArrayList<View>();
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		getNavConfig();
		final List<ResolveInfo> apps = mPackageManager.queryIntentActivities(
				mainIntent, 0);
		mApps = new ArrayList<ResolveInfo>();
		for (ResolveInfo appInfo : apps) {
			boolean flag = false;
			if ((appInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
				// Updated system app
				flag = true;
			} else if ((appInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				// Non-system app
				flag = true;
			} else {
//				Log.d("fc", "p:"+appInfo.activityInfo.applicationInfo.packageName); 
//				Log.d("fc", "c:"+appInfo.activityInfo.applicationInfo.className); 
				if(/*"com.google.android.apps.gmm.base.app.GoogleMapsApplication".equals(appInfo.activityInfo.applicationInfo.className)&&*/
						"com.google.android.apps.maps".equals(appInfo.activityInfo.applicationInfo.packageName)){
					mApps.add(appInfo);
				}
			}
			if (flag) {
//				Log.d("fd", "p:"+appInfo.activityInfo.applicationInfo.packageName); 
//				Log.d("fd", "c:"+appInfo.activityInfo.applicationInfo.className); 
				if(!isNoMap(appInfo.activityInfo.applicationInfo.packageName)){
					if (isConfigList(appInfo.activityInfo.applicationInfo.packageName)){
						mApps.add(appInfo);
					}
				}
			}
		}
		LayoutInflater inflater = LayoutInflater.from(this.getContext());
		for (ResolveInfo app : mApps) {
			String appName = app.loadLabel(mPackageManager).toString();
			mItems.add(appName);

			View row = inflater.inflate(R.layout.app_list_view, null, false);
			ViewWrapper wrapper = new ViewWrapper(row);
			wrapper.appName = appName;
			row.setTag(wrapper);
			mViews.add(row);

			if (app.activityInfo.applicationInfo.packageName
					.equals(mPackageName)) {
				mAppName = appName;
			}
		}
		this.notifyChanged();
	}

	private final static String[] NO_MAPS_APP = {"com.adobe.reader",
		"com.estrongs.android.pop","net.easyconn","com.my.instructions"};

	private boolean isNoMap(String p) {
		if (p == null) {
			return false;
		}
		for (String s : NO_MAPS_APP) {
			if (s.equals(p)) {
				return true;
			}
		}
		return false;
	}
	private Context mContext;
	public SeaNavigationChoice(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		update(context);
	}

	public String getChoosedAppName() {
		return mAppName;
	}

	@Override
	protected void onBindView(View view) {
		// TODO Auto-generated method stub
		super.onBindView(view);
	}

	private void setValue(String value, String file) {
		if (value == null || file == null)
			return;
		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
			out.write(value.getBytes());
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void killProcessIfExist(String name) {
		List<ActivityManager.RunningAppProcessInfo> mRunningProcess = mActivityManager.getRunningAppProcesses();
		int pid=-1;  
        for (ActivityManager.RunningAppProcessInfo amProcess : mRunningProcess){  
            if(amProcess.processName.equals(name)){  
                pid=amProcess.pid;  
                break;  
            }  
        }
        
        if (-1 != pid) {
        	Log.i(TAG, "## killProcessIfExist: packageName=["+name+"] pid="+pid);
        	Util.sudoExecNoCheck("kill:"+pid);
        }
	}
	
	@Override
	protected void onClick() {
		if (mAlertDialog!=null && mAlertDialog.isShowing()){
			super.onClick();
			return;
		}
		LinearLayout appLayout = (LinearLayout) LayoutInflater.from(
				this.getContext()).inflate(R.layout.app_list, null);
		mAppList = (ListView) appLayout.findViewById(R.id.appList);
		mAppListAdapter = new IconicAdapter(R.layout.app_list_view, mItems);
		mAppList.setAdapter(mAppListAdapter);
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext())
				.setIcon(android.R.drawable.ic_dialog_map)
				.setTitle(
						getContext()
								.getString(R.string.navigation_choice_title))
				.setView(appLayout);
		// .setPositiveButton(R.string.navigation_choice_sure,
		// null).setView(appLayout);
		mAlertDialog = builder.create();
		mAlertDialog.show();
		mAppList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.w(TAG, "onItemClick");
				ViewWrapper tag = (ViewWrapper) view.getTag();
				String appName = tag.appName;
				String pkgName = tag.getPackageName();
				String clzName = tag.getClassName();
				if (mNavigationChangeListener != null) {
					mNavigationChangeListener.notifyNavigationChange(appName,
							pkgName, clzName);
					
					String killNaviApk = "";
					if ( !pkgName.equals(mPackageName) ) { 
						// we are changing navi to/from iGO primo, so we
						// will kill it later, if it's running.
						if ( PACK_NAME_IGO_PRIMO.equals(pkgName) ) {
							killNaviApk = pkgName;
						} else if (	PACK_NAME_IGO_PRIMO.equals(mPackageName) ) {
							killNaviApk = mPackageName;
						}
					}
					
					mPackageName = pkgName;
					mClassName = clzName;
					SystemConfig.setProperty(getContext(), MachineConfig.KEY_GPS_PACKAGE,
							mPackageName);
					SystemConfig.setProperty(getContext(), MachineConfig.KEY_GPS_CLASS,
							mClassName);
//					MachineConfig.setProperty(MachineConfig.KEY_GPS_UID,
//							Integer.toString(tag.getUid()));
					SystemProperties.set("ak.af.navi.uid",Integer.toString(tag.getUid()));
					
					int tts_uid = 0;
					if(mPackageName.startsWith(PACKAGE_IGO)){
						final Intent intent = new Intent("android.intent.action.TTS_SERVICE", null);
						final List<ResolveInfo> apps1 = mPackageManager.queryIntentServices(intent, 0);
						if (apps1 != null) {
							String ttsPkgname = android.provider.Settings.Secure
									.getString(mContext.getContentResolver(),
											TTS_DEFAULT_SYNTH);
							// Log.d(TAG, "default TTS " + ttsPkgname);
							if (ttsPkgname != null) {
								for (ResolveInfo appInfo : apps1) {
									if (appInfo != null	&& appInfo.serviceInfo != null) {
										if (appInfo.serviceInfo.packageName != null
												&& ttsPkgname.equals(appInfo.serviceInfo.packageName)) {
											if (appInfo.serviceInfo.applicationInfo != null) {
												tts_uid = appInfo.serviceInfo.applicationInfo.uid;
												// Log.d(TAG, "found tts uid=" +
												// tts_uid);
											}
											break;
										}
									}
								}
							}
						}

						if (tts_uid == 0) {
							final Intent mainIntent = new Intent("android.intent.action.START_TTS_ENGINE", null);
							final List<ResolveInfo> apps2 = mPackageManager.queryIntentActivities(
									mainIntent, 0);
							
							for (ResolveInfo appInfo : apps2) {
								if (appInfo.activityInfo != null) {
									if (PACKAGE_TTS
											.equals(appInfo.activityInfo.packageName)) {
										tts_uid = appInfo.activityInfo.applicationInfo.uid;
	
										break;
									}
								}
							}
						}
					}
					SystemProperties.set("ak.af.tts.uid", "" + tts_uid);
					Log.d(TAG, mPackageName+":set uid:" + Integer.toString(tag.getUid())+"tts uid"+tts_uid);
					
					if ( (null!=killNaviApk) && (!killNaviApk.equals("")) ) {
						killProcessIfExist(killNaviApk);
					}
					
					if(GeneralSettingsFragment.mupdateNaviChoice){
						UtilSystem.doRunActivity(getContext(), pkgName, clzName);
					}
				}
				mAlertDialog.dismiss();
			}

		});
		super.onClick();
	}

	private static final String PACKAGE_TTS = "com.svox.pico";
	private static final String PACKAGE_IGO = "com.nng.igo";
	class IconicAdapter extends ArrayAdapter<String> {
		private List<String> listItems;
		private int layout;

		IconicAdapter(int layout, List<String> listItems) {
			super(SeaNavigationChoice.this.getContext(), layout, listItems);
			this.listItems = listItems;
			this.layout = layout;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			/*
			 * View row = convertView; ViewWrapper wrapper = null;
			 * 
			 * if (row == null) { LayoutInflater inflater =
			 * LayoutInflater.from(this.getContext());
			 * 
			 * row = inflater.inflate(layout, parent, false); wrapper = new
			 * ViewWrapper(row); row.setTag(wrapper); } else { wrapper =
			 * (ViewWrapper) row.getTag(); }
			 */

			View row = mViews.get(position);
			ViewWrapper wrapper = (ViewWrapper) row.getTag();
			// if (BluetoothApplication.getConnectType()>0 && wrapper.getIcon()
			// != null && position ==
			// BluetoothApplication.getConnectedDevice()-1) {
			// wrapper.getIcon().setVisibility(ImageView.VISIBLE);
			// }else{
			// wrapper.getIcon().setVisibility(ImageView.GONE);
			// }
			// File file = new File(listItems.get(position));
			wrapper.getIcon().setImageDrawable(
					mApps.get(position).loadIcon(mPackageManager));
			String pkgName = mApps.get(position).activityInfo.applicationInfo.packageName;
			String clzName = mApps.get(position).activityInfo.name;
			// Log.e(TAG,
			// "position = "+position+"pkgName = "+mApps.get(position).activityInfo.applicationInfo.packageName+
			// "mPackageName ="+mPackageName);
			wrapper.setPackageName(pkgName);
			wrapper.setClassName(clzName);
			wrapper.setUid(mApps.get(position).activityInfo.applicationInfo.uid);

			wrapper.getLabel().setText(listItems.get(position));
			// Log.d(TAG, "pkgName = " + pkgName + ", mPackageName = " +
			// mPackageName);
			if ( (pkgName.equals(mPackageName)) &&
					(clzName.equals(mClassName)) ) {
				wrapper.getLabel().setChecked(true);
			} else {
				wrapper.getLabel().setChecked(false);
			}
			return (row);
		}
	}

	class ViewWrapper {
		String appName = "";
		View base;
		CheckedTextView label = null;
		ImageView icon = null;
		String packageName = null;
		String className = null;
		int uid = -1;
		int id = -1;

		ViewWrapper(View base) {
			this.base = base;
			id = -1;
		}

		CheckedTextView getLabel() {
			if (label == null) {
				label = (CheckedTextView) base
						.findViewById(R.id.packageInfoName);
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

		public String getPackageName() {
			return packageName;
		}

		public void setPackageName(String packageName) {
			this.packageName = packageName;
		}

		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}
		
		public void setUid(int value) {
			this.uid = value;
		}
		public int getUid() {
			return this.uid;
		}

	}

	private void getNavConfig() {		
		mPackageName = SystemConfig
				.getProperty(getContext(), MachineConfig.KEY_GPS_PACKAGE);
		mClassName = SystemConfig
				.getProperty(getContext(), MachineConfig.KEY_GPS_CLASS);
		
	}

	public void setPackageName(String pkgName, String clzName) {
		// String name = Util.getActivityNameByPkgNameAndClzName(getContext(),
		// pkgName, clzName);
		// if(name != null){
		// setSummary(name);
		// }
	}

	public void setNavigationChangeListener(NavigationChangeListener listener) {
		if(listener!=null){
			update(mContext);
		}
		mNavigationChangeListener = listener;
	}

	public interface NavigationChangeListener {
		public void notifyNavigationChange(String name, String pkgName,
				String clzName);
	}

}
