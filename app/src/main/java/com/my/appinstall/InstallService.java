package com.my.appinstall;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
//import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class InstallService extends Service {

	private final IBinder mBinder = new LocalBinder();
	private List<String[]> shortcuts;
	private InstallListener listener;
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public void installApks(final File files[]) {
		/*PackageManager packageManager = getPackageManager();
		PackageInstallObserver observer = new PackageInstallObserver();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				Log.i("Inatall apk", files[i].getName());
				listener.startInstall(files[i].getName());
				packageManager.installPackage(Uri.fromFile(files[i]), observer, 0, "");
			}
		}
		
		shortcuts = getShortcut(new File("/mnt/extsd/apk/shortcut.txt"));*/
	}
/*
	class PackageInstallObserver extends IPackageInstallObserver.Stub {
		public void packageInstalled(String packageName, int returnCode) {
			Log.i("Inatall apk", packageName+" returnCode="+returnCode);
			listener.installed(packageName, returnCode);
			for (String[] shortcutinfo : shortcuts) {
				if(packageName.equals(shortcutinfo[0].trim()))
				addShortcut(InstallService.this.getBaseContext(), shortcutinfo);
			}
		}
	}
*/
	public boolean addShortcut(Context context, String[] pkg) {
		String title = "unknown";
		String mainAct = null;
		int iconIdentifier = 0;
		PackageManager pkgMag = context.getPackageManager();
		Intent queryIntent = new Intent(Intent.ACTION_MAIN, null);
		queryIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> list = pkgMag.queryIntentActivities(queryIntent, PackageManager.GET_ACTIVITIES);
		for (int i = 0; i < list.size(); i++) {
			ResolveInfo info = list.get(i);
			if (info.activityInfo.packageName.equals(pkg[0])) {
				title = info.loadLabel(pkgMag).toString();
				mainAct = info.activityInfo.name;
				iconIdentifier = info.activityInfo.applicationInfo.icon;
				break;
			}
		}
		if (mainAct == null) {
			return false;
		}
		Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
		shortcut.putExtra("duplicate", false);
		shortcut.putExtra("screen", Integer.valueOf(pkg[1]));
		shortcut.putExtra("cell_x", Integer.valueOf(pkg[2]));
		shortcut.putExtra("cell_y", Integer.valueOf(pkg[3]));
		ComponentName comp = new ComponentName(pkg[0], mainAct);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(Intent.ACTION_MAIN).setComponent(comp));
		Context pkgContext = null;
		if (context.getPackageName().equals(pkg[0])) {
			pkgContext = context;
		} else {
			try {
				pkgContext = context.createPackageContext(pkg[0], Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (pkgContext != null) {
			ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(pkgContext, iconIdentifier);
			shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
		}
		context.sendBroadcast(shortcut);
		return true;
	}

	private List<String[]> getShortcut(final File file) {
		BufferedReader buf;
		String result[];
		String source = null;
		List<String[]> list = new ArrayList<String[]>();
		try {
			buf = new BufferedReader(new FileReader(file));
			do {
				source = buf.readLine();
				if (source != null) {
					result = source.split(" ");
					if (result.length < 4)
						continue;
					else
						list.add(result);
					Log.e("Installservice", source);
				}
			} while (source != null);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return list;
	}

	public class LocalBinder extends Binder {
		InstallService getService() {
			return InstallService.this;
		}
	}
	public void setListener(InstallListener listener){
		this.listener = listener;
	}
}
