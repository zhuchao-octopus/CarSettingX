package com.my.factory;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Window;

import com.octopus.android.carsettingx.R;

public class ActivityLogo extends Activity {
	private FragmentManager mFragmentManager;
	private LogoFragment mLogoFragment = new LogoFragment();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_logo);
		mFragmentManager = getFragmentManager();
		replaceFragment(R.id.id_genernal_setting_fragment, mLogoFragment, false);
	}

	private void replaceFragment(int layoutId, Fragment fragment,
			boolean isAddStack) {
		if (fragment != null) {
			FragmentTransaction transation = mFragmentManager
					.beginTransaction();
			transation.replace(layoutId, fragment);
			if (isAddStack) {
				transation.addToBackStack(null);
			}
			transation.commit();
		}
	}
}

