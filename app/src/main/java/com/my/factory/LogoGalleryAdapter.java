package com.my.factory;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashSet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery.LayoutParams;
import android.widget.ImageView;

import com.octopus.android.carsettingx.R;

public class LogoGalleryAdapter extends BaseAdapter {
	public static String LOGOS_PATH = "/mnt/paramter/logo";
	
	private Context mContext;
	private ArrayList<ImageView> mImageViews = new ArrayList<ImageView>();
	private ArrayList<String> mNames = new ArrayList<String>();
	private ArrayList<String> mPaths = new ArrayList<String>();
	private LayoutParams mLayoutParams;
	
	private String mStrFilter = ".png";
	
	private FileFilter mFileFilter = new FileFilter() {
		public boolean accept(File file) {
			if (file.getName().startsWith(".")) {
				return false;
			} else if (file.exists()) {
				if (file.getName().toLowerCase().endsWith(mStrFilter)) {
					return true;
//				} else if (file.getName().endsWith(".jpg")) {
//					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	};
	

	private Bitmap[] mBitmap;
	
	public LogoGalleryAdapter(Context c, String filter, String path, String first_file) {
		mContext = c;
		mLayoutParams = new LayoutParams(
				c.getResources().getDimensionPixelSize(R.dimen.logo_width),
				c.getResources().getDimensionPixelSize(R.dimen.logo_height));

		if(first_file!=null){			
			addFile(first_file);			
		}
		mStrFilter = filter;
		File[] files = new File(path).listFiles(mFileFilter);
		int i =0;
		if (files != null) {
			mBitmap = new Bitmap[files.length];
			for (File file : files) {
				addFile(file.getPath());
//				Bitmap bm = BitmapFactory.decodeFile(file.getPath());
//				if (bm == null) {
//					continue;
//				}
//				
//				ImageView iv = new ImageView(mContext);
//				iv.setLayoutParams(mLayoutParams);
//				iv.setBackgroundColor(Color.BLACK);
//				iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
//				iv.setImageBitmap(bm);
//				
//				mImageViews.add(iv);
//				mPaths.add(file.getPath());
//				mBitmap[i] = bm;
//				++i;
			}
		}

	}
	
	private void addFile(String path){
		Bitmap bm = BitmapFactory.decodeFile(path);
		if (bm == null) {
			return;
		}
		
		ImageView iv = new ImageView(mContext);
		iv.setLayoutParams(mLayoutParams);
		iv.setBackgroundColor(Color.BLACK);
		iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
		iv.setImageBitmap(bm);
		
		mImageViews.add(iv);
		mPaths.add(path);
	}
	
	public void release() {
//		for (int i = 0; i < mBitmap.length; ++i) {
//			if (mBitmap[i] != null && !mBitmap[i].isRecycled()) {
//				mBitmap[i].recycle();
//				mBitmap[i] = null;
//			}
//		}
//		mBitmap =null;
	}

	@Override
	public int getCount() {
		return mImageViews.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View arg1, ViewGroup arg2) {
		if (mImageViews.isEmpty()) {
			return null;
		}
		return mImageViews.get(position);
	}
	
	public String getName(int position) {
		if (mNames.isEmpty()) {
			return "";
		}
		return mNames.get(position);
	}
	
	public String getPath(int position) {
		if (mPaths.isEmpty()) {
			return "";
		}
		return mPaths.get(position);
	}
	
	public int getPosition(String path) {
		int position = -1;
		if (mPaths.contains(path)) {
			position = mPaths.indexOf(path);
		}
		return position;
	}
	
}
