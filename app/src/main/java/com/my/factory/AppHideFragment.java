package com.my.factory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.common.util.AppConfig;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.octopus.android.carsettingx.R;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

public class AppHideFragment extends Fragment {
	private static final String TAG = "FragmentFactoryApp";
	private List<Data> mData = new ArrayList<Data>();
	private FactoryAppListAdapter mAdapter;
	private ListView mListView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		loadData();
		mAdapter = new FactoryAppListAdapter(getActivity(), mData);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_app_hide, container,
				false);
		mListView = (ListView) view.findViewById(R.id.listView_factory_app);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(mOnItemClickListener);
		return view;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}



	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			ViewHolder holder = (ViewHolder) view.getTag();
			if (holder.checked.isChecked()) {
				mData.get(position).checked = false;
			} else {
				mData.get(position).checked = true;
			}
			mAdapter.notifyDataSetChanged();
			/*for (int i = 0; i < mData.size(); i++) {
				Log.d(TAG, mData.get(i).name + " = " + mData.get(i).checked);
			}*/
			setAppHideSetting();
		}
	};

	class Data {
		String name;
		String value;
		boolean checked;

		Data(String n, String v, boolean c) {
			name = n;
			value = v;
			checked = c;
		}
	}

	private void loadData() {
		mData.clear();
		String[] name = getResources().getStringArray(R.array.app_hide_select);
		String[] value = getResources().getStringArray(
				R.array.app_hide_select_value);
		if (name != null && value != null && name.length == value.length) {
			for (int i = 0; i < name.length; i++) {
				Data data = new Data(name[i], value[i], false);
				mData.add(data);
			}
		}

		HashSet<String> hideSet = getAppHideSetting();
		for (String hide : hideSet) {
			for (int i = 0; i < mData.size(); i++) {
				Data data = mData.get(i);
				if (data != null && data.value != null && data.value.equals(hide)) {
					data.checked = true;
				}
			}
		}
	}
	
	private HashSet<String> getAppHideSetting() {
		HashSet<String> set = new HashSet<String>();
		String value = MachineConfig.getProperty(MachineConfig.KEY_APP_HIDE);
		boolean hideUSBDvd = false;
		if (null != value) {
			if (value.contains("DTV")) {
				set.add("DTV");
			}
			if (value.contains("AUX")) {
				set.add("AUX");
			}
			if (value.contains("BT")) {
				set.add("BT");
			}
			if (value.contains(AppConfig.HIDE_APP_DVD)) {
				set.add(AppConfig.HIDE_APP_DVD);
			}
			if (value.contains(AppConfig.HIDE_APP_FRONT_CMAERA)) {
				set.add(AppConfig.HIDE_APP_FRONT_CMAERA);
			}
			if (value.contains(AppConfig.HIDE_APP_VIDEO_OUT)) {
				set.add(AppConfig.HIDE_APP_VIDEO_OUT);
			}
			if (value.contains(AppConfig.HIDE_APP_DVR)) {
				set.add(AppConfig.HIDE_APP_DVR);
			}
			if (value.contains(AppConfig.HIDE_APP_VIOCE_CONTROL)) {
				set.add(AppConfig.HIDE_APP_VIOCE_CONTROL);
			}
			if (value.contains(AppConfig.HIDE_APP_JOYSTUDY)) {
				set.add(AppConfig.HIDE_APP_JOYSTUDY);
			}
			if (value.contains(AppConfig.HIDE_APP_WHEELKEYSTUDY)) {
				set.add(AppConfig.HIDE_APP_WHEELKEYSTUDY);
			}
			if (value.contains(AppConfig.HIDE_USB_DVD)) {
				set.add(AppConfig.HIDE_USB_DVD);
				hideUSBDvd = true;
			}
		} else {
			set.add("DTV");
			set.add(AppConfig.HIDE_APP_JOYSTUDY);
		}
		// else { // default hide
		// set.add("DVD");
		// }
		if(!hideUSBDvd){
			String USBDvd = MachineConfig.getProperty(MachineConfig.KEY_USB_DVD);
			if (!"1".equals(USBDvd)){
				set.add(AppConfig.HIDE_USB_DVD);
			}
		}
		return set;
	}

	private void setAppHideSetting() {
		String value = "";
		boolean hideUSBDvd = false;
		for (int i = 0; i < mData.size(); i++) {
			Data data = mData.get(i);
			if (data.checked) {
				if (!value.isEmpty()) {
					value += ",";
				}
				value += data.value;
				if (data.value.equals(AppConfig.HIDE_USB_DVD)) {
					hideUSBDvd = true;
				}				
			}
		}
		String USBDvd = MachineConfig.getProperty(MachineConfig.KEY_USB_DVD);
		
		if (hideUSBDvd) {
			if ("1".equals(USBDvd)) {
				MachineConfig.setProperty(MachineConfig.KEY_USB_DVD, "0");
			}
		} else {
			MachineConfig.setProperty(MachineConfig.KEY_USB_DVD, "1");
		}
		
		
		String tpms = MachineConfig.getProperty(
				MachineConfig.KEY_TPMS_TYPE);
		if ("2".equals(tpms)) {
			value += "," + AppConfig.HIDE_TPMS;
		}
		
		MachineConfig.setProperty(MachineConfig.KEY_APP_HIDE, value);
		// MachineConfig.notifyAll(mActivity);

		Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
		it.putExtra(MyCmd.EXTRA_COMMON_CMD, MachineConfig.KEY_APP_HIDE);
		getActivity().sendBroadcast(it);
	}



	class ViewHolder {
		public TextView title;
		public Switch checked;
	}

	class FactoryAppListAdapter extends BaseAdapter {
		private LayoutInflater mInflater = null;
		List<Data> mData;

		public FactoryAppListAdapter(Context context, List<Data> data) {
			super();
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mData = data;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (mData != null)
				return mData.size();
			else
				return 0;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			if (mData != null)
				return mData.get(position);
			else
				return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.listitem_app_hide,	null);
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.checked = (Switch) convertView.findViewById(R.id.checked);
				
				RelativeLayout ll = (RelativeLayout)convertView.findViewById(R.id.listview_item);
				if (ll != null) {
					LayoutParams layoutParams = (LayoutParams) ll.getLayoutParams();
//					if (layoutParams != null && mListView != null
//							&& mData != null && mData.size() > 0) {
						layoutParams.height = mListView.getHeight() / 6;//mData.size();
						ll.setLayoutParams(layoutParams);
//					}
				}
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if (mData != null && position < mData.size()) {
				Data data = mData.get(position);
				if (data != null) {
					holder.title.setText(data.name == null ? "" : data.name);
					holder.checked.setChecked(data.checked ? true : false);
				}
			}
			return convertView;
		}
	}
}
