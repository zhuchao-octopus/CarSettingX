package com.my.factory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;

import android.preference.PreferenceFragment;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.common.util.AppConfig;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.octopus.android.carsettingx.R;

public class SaveDriveSettings extends Activity implements OnClickListener {
	private static final String TAG = "SaveDriveSettings";

	// private KeyControllor mKeyControllor = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final EditText ev = new EditText(this);
		final Toast t = Toast.makeText(this,
				getResources().getString(R.string.password_error),
				Toast.LENGTH_SHORT);
		AlertDialog ad = new AlertDialog.Builder(this)
				.setIcon(R.drawable.dialog_alert_icon)
				.setTitle(R.string.input_code)
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String str = ev.getText().toString();
								if ("111999".equals(str)) {
									init();
								} else {
									t.setText(R.string.password_error);
									t.show();
									finish();
								}
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
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
		ev.setTransformationMethod(PasswordTransformationMethod
				.getInstance());
		
		ad.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
//				finish();
			}
		});
		
	}
	
	private void init(){
		setContentView(R.layout.save_drive_settings);
		mContext = this;
		update(this);

		mAppList = (ListView) findViewById(R.id.appList);
		mAppListAdapter = new IconicAdapter(R.layout.app_list_view, mItems);
		mAppList.setAdapter(mAppListAdapter);

		mAppList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				ViewWrapper tag = (ViewWrapper) view.getTag();

				updateConfig(tag.getPackageName(), tag.getClassName(), !tag
						.getLabel().isChecked());
				tag.getLabel().setChecked(!tag.getLabel().isChecked());
			}

		});

		findViewById(R.id.btn_cancel).setOnClickListener(this);
		findViewById(R.id.btn_ok).setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
        int id = arg0.getId();
        if (id == R.id.btn_ok) {
            updateConfig();
            finish();
        } else if (id == R.id.btn_cancel) {
            finish();
        }
	}

	private ListView mAppList;
	private List<String> mItems = null;
	private IconicAdapter mAppListAdapter;
	private PackageManager mPackageManager;
	private List<ResolveInfo> mApps;
	private List<View> mViews;
	// private static final String PROPERTIESFILE =
	// "/data/system/.properties_file";
	private String mAppName = "";

	private void update(Context context) {
		if (context == null) {
			return;
		}
		mPackageManager = context.getPackageManager();
		mItems = new ArrayList<String>();
		mViews = new ArrayList<View>();
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		getSaveDriveConfig();
		mApps = mPackageManager.queryIntentActivities(mainIntent, 0);
		// mApps = new ArrayList<ResolveInfo>();
		// for (ResolveInfo appInfo : apps) {
		// boolean flag = false;
		// if ((appInfo.activityInfo.applicationInfo.flags &
		// ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
		// // Updated system app
		// flag = true;
		// } else if ((appInfo.activityInfo.applicationInfo.flags &
		// ApplicationInfo.FLAG_SYSTEM) == 0) {
		// // Non-system app
		// flag = true;
		// }
		// if (flag) {
		// mApps.add(appInfo);
		// }
		// }
		LayoutInflater inflater = LayoutInflater.from(this);
		for (ResolveInfo app : mApps) {
			
			if(app.activityInfo.packageName.equals("com.android.launcher")){
				continue;
			}
			String appName = app.loadLabel(mPackageManager).toString();
			
		
			mItems.add(appName);

			View row = inflater.inflate(R.layout.app_list_view, null, false);
			ViewWrapper wrapper = new ViewWrapper(row);
			wrapper.appName = appName;
			row.setTag(wrapper);
			mViews.add(row);

		}

	}

	private Context mContext;

	public String getChoosedAppName() {
		return mAppName;
	}

	class IconicAdapter extends ArrayAdapter<String> {
		private List<String> listItems;
		private int layout;

		IconicAdapter(int layout, List<String> listItems) {
			super(mContext, layout, listItems);
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

			if (isSet(pkgName , clzName)) {
				wrapper.getLabel().setChecked(true);
			} else {
				wrapper.getLabel().setChecked(false);
			}

			return (row);
		}
	}

	private boolean isSet(String pn, String cn) {
		
		if (AppConfig.isPackageSaveSpecApp(pn)) {
			pn = pn + "/" + cn;
		}
		
		for (String s : mPackageSet) {
			
			if (pn.equals(s)) {
				return true;
			}
		}
		return false;
	}

	private void removeSet(String packageName) {
		for (String s : mPackageSet) {
			if (packageName.equals(s)) {
				mPackageSet.remove(packageName);
				break;
			}
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

	private final ArrayList<String> mPackageSet = new ArrayList<String>();

	private void getSaveDriveConfig() {
		mPackageSet.clear();
		String s = MachineConfig
				.getProperty(MachineConfig.KEY_SAVE_DRIVER_PACKAGE);
		if (s != null) {
			String[] ss = s.split(":");
			for (int i = 0; i < ss.length; ++i) {
				if (ss[i].length() > 1) {
					mPackageSet.add(ss[i]);
				}
			}
		}
	}

	private void updateConfig(String pn, String cn, boolean add) {

		String s = pn;
		if (AppConfig.isPackageSaveSpecApp(pn)) {
			s = pn + "/" + cn;
		}
		if (add) {
			if (!isSet(pn, cn)) {
				mPackageSet.add(s);
			}
		} else {
			removeSet(s);
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}

	private void updateConfig() {

		String s = "";
		try {
			for (int i = 0; i < mPackageSet.size(); ++i) {
				s += mPackageSet.get(i) + ":";
			}

			MachineConfig.setProperty(MachineConfig.KEY_SAVE_DRIVER_PACKAGE, s);

			Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
			it.putExtra(MyCmd.EXTRA_COMMON_CMD, MachineConfig.KEY_SAVE_DRIVER);
			sendBroadcast(it);
		} catch (Exception e) {

		}
	}

}
