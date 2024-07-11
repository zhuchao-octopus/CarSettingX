/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.my.wallpaper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;

import com.octopus.android.carsettingx.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class WallpaperChooserDialogFragment extends Dialog implements
		AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {

	private static final String TAG = "Launcher.WallpaperChooserDialogFragment";
	private static final String EMBEDDED_KEY = "com.android.launcher2."
			+ "WallpaperChooserDialogFragment.EMBEDDED_KEY";

	private boolean mEmbedded;

	private ArrayList<String> mThumbs = new ArrayList<String>();
	private WallpaperLoader mLoader;
	private WallpaperDrawable mWallpaperDrawable = new WallpaperDrawable();

	private Context mContext;

	public WallpaperChooserDialogFragment(Context context) {
		super(context);
		mContext = context;
	}

	// public static WallpaperChooserDialogFragment newInstance() {
	// WallpaperChooserDialogFragment fragment = new
	// WallpaperChooserDialogFragment();
	// fragment.setCancelable(true);
	// return fragment;
	// }
	View mView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// if (savedInstanceState != null &&
		// savedInstanceState.containsKey(EMBEDDED_KEY)) {
		// mEmbedded = savedInstanceState.getBoolean(EMBEDDED_KEY);
		// } else {
		// mEmbedded = isInLayout();
		// }

		setContentView(R.layout.wallpaper_chooser);

		findWallpapers();

		/*
		 * If this fragment is embedded in the layout of this activity, then we
		 * should generate a view to display. Otherwise, a dialog will be
		 * created in onCreateDialog()
		 */
		mView = findViewById(R.id.wallpaper_background);// inflater.inflate(R.layout.wallpaper_chooser,
														// container,
														// false);
		mView.setBackground(mWallpaperDrawable);

		final Gallery gallery = (Gallery) findViewById(R.id.gallery);
		gallery.setCallbackDuringFling(false);
		gallery.setOnItemSelectedListener(this);
		gallery.setAdapter(new ImageAdapter(mContext));

		View setButton = findViewById(R.id.set);
		setButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectWallpaper(gallery.getSelectedItemPosition());
			}
		});

	}

	// @Override
	// public void onSaveInstanceState(Bundle outState) {
	// outState.putBoolean(EMBEDDED_KEY, mEmbedded);
	// }

	private void cancelLoader() {
		if (mLoader != null
				&& mLoader.getStatus() != WallpaperLoader.Status.FINISHED) {
			mLoader.cancel(true);
			mLoader = null;
		}
	}

	// @Override
	// public void onDetach() {
	// super.onDetach();
	//
	// cancelLoader();
	// }
	//
	// @Override
	// public void onDestroy() {
	// super.onDestroy();
	//
	// cancelLoader();
	// }

	// @Override
	// public void onDismiss(DialogInterface dialog) {
	// super.onDismiss(dialog);
	// /* On orientation changes, the dialog is effectively "dismissed" so this
	// is called
	// * when the activity is no longer associated with this dying dialog
	// fragment. We
	// * should just safely ignore this case by checking if getActivity()
	// returns null
	// */
	// Activity activity = getActivity();
	// if (activity != null) {
	// activity.finish();
	// }
	// }

	/*
	 * This will only be called when in XLarge mode, since this Fragment is
	 * invoked like a dialog in that mode
	 */
	// @Override
	// public Dialog onCreateDialog(Bundle savedInstanceState) {
	// findWallpapers();
	//
	// return null;
	// }

	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container,
	// Bundle savedInstanceState) {
	// findWallpapers();
	//
	// /* If this fragment is embedded in the layout of this activity, then we
	// should
	// * generate a view to display. Otherwise, a dialog will be created in
	// * onCreateDialog()
	// */
	// if (mEmbedded) {
	// View view = inflater.inflate(R.layout.wallpaper_chooser, container,
	// false);
	// view.setBackground(mWallpaperDrawable);
	//
	// final Gallery gallery = (Gallery) view.findViewById(R.id.gallery);
	// gallery.setCallbackDuringFling(false);
	// gallery.setOnItemSelectedListener(this);
	// gallery.setAdapter(new ImageAdapter(getActivity()));
	//
	// View setButton = view.findViewById(R.id.set);
	// setButton.setOnClickListener(new OnClickListener() {
	// @Override
	// public void onClick(View v) {
	// selectWallpaper(gallery.getSelectedItemPosition());
	// }
	// });
	// return view;
	// }
	// return null;
	// }

	@SuppressLint("ServiceCast")
	private void selectWallpaper(int position) {
		try {
			if (mContext instanceof Activity) {
				Activity mActivity = (Activity) mContext;

				WallpaperManager wpm = (WallpaperManager) mActivity
						.getSystemService(Context.WALLPAPER_SERVICE);

				wpm.setBitmap(mWallpaperDrawable.getBitmap());
				Activity activity = mActivity;
				activity.setResult(Activity.RESULT_OK);
				activity.finish();
			} else {
			}
		} catch (Exception e) {
			Log.e(TAG, "Failed to set wallpaper: " + e);
		}
	}

	// Click handler for the Dialog's GridView
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		selectWallpaper(position);
	}

	// Selection handler for the embedded Gallery view
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		if (mLoader != null
				&& mLoader.getStatus() != WallpaperLoader.Status.FINISHED) {
			mLoader.cancel();
		}
		mLoader = (WallpaperLoader) new WallpaperLoader().execute(position);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	private final static int MAX_WALLPAPER = 50;
	private final static String PATH_WALLPAPER = "/mnt/paramter/wallpaper/";
	private final static String PATH_WALLPAPER_SMALL = "/mnt/paramter/wallpaper/small/";

	private void findWallpapers() {

		File filepath = new File(PATH_WALLPAPER_SMALL);
		if (filepath.exists()) {

			File[] files = filepath.listFiles();
			for (File file : files) {
				if (!file.isDirectory()) {
					File f = new File(PATH_WALLPAPER + file.getName());
					if (f.exists()) {
						mThumbs.add(file.getName());
					}
				}
			}
		}

		// final Resources resources = getResources();
		// Context.getPackageName() may return the "original" package name,
		// com.android.launcher2; Resources needs the real package name,
		// com.android.launcher. So we ask Resources for what it thinks the
		// package name should be.
		// final String packageName =
		// resources.getResourcePackageName(R.array.wallpapers);

		// addWallpapers(resources, packageName, R.array.wallpapers);
		// addWallpapers(resources, packageName, R.array.extra_wallpapers);
	}

	// private void addWallpapers(Resources resources, String packageName, int
	// list) {
	// final String[] extras = resources.getStringArray(list);
	// for (String extra : extras) {
	// int res = resources.getIdentifier(extra, "drawable", packageName);
	// if (res != 0) {
	// final int thumbRes = resources.getIdentifier(extra + "_small",
	// "drawable", packageName);
	//
	// if (thumbRes != 0) {
	// mThumbs.add(thumbRes);
	// mImages.add(res);
	// // Log.d(TAG, "add: [" + packageName + "]: " + extra + " (" + res + ")");
	// }
	// }
	// }
	// }

	private class ImageAdapter extends BaseAdapter implements ListAdapter,
			SpinnerAdapter {
		private LayoutInflater mLayoutInflater;

		ImageAdapter(Context activity) {
			mLayoutInflater = LayoutInflater.from(activity);// activity.getLayoutInflater();
		}

		public int getCount() {
			return mThumbs.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view;

			if (convertView == null) {
				view = mLayoutInflater.inflate(R.layout.wallpaper_item, parent,
						false);
			} else {
				view = convertView;
			}

			ImageView image = (ImageView) view
					.findViewById(R.id.wallpaper_image);

			// int thumbRes = mThumbs.get(position);
			// image.setImageResource(thumbRes);

			Drawable thumbDrawable = Drawable
					.createFromPath(PATH_WALLPAPER_SMALL
							+ mThumbs.get(position));

			image.setImageDrawable(thumbDrawable);

			// Drawable thumbDrawable = image.getDrawable();
			if (thumbDrawable != null) {
				thumbDrawable.setDither(true);
			} else {
				Log.e(TAG,
						"Error decoding thumbnail resId="
								+ mThumbs.get(position) + " for wallpaper #"
								+ position);
			}

			return view;
		}
	}

	class WallpaperLoader extends AsyncTask<Integer, Void, Bitmap> {
		WallpaperLoader() {
		}

		@Override
		protected Bitmap doInBackground(Integer... params) {
			if (isCancelled())
				return null;
			try {
				return BitmapFactory.decodeFile(PATH_WALLPAPER
						+ mThumbs.get(params[0]));
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(Bitmap b) {
			if (b == null)
				return;

			if (!isCancelled()) {
				if(mWallpaperDrawable.getBitmap()!=null){
					mWallpaperDrawable.getBitmap().recycle();
				}
				
				mWallpaperDrawable.setBitmap(b);
				mView.postInvalidate();
				mLoader = null;
			} else {
				b.recycle();
			}
		}

		void cancel() {
			super.cancel(true);
		}
	}

	/**
	 * Custom drawable that centers the bitmap fed to it.
	 */
	static class WallpaperDrawable extends Drawable {

		Bitmap mBitmap;
		int mIntrinsicWidth;
		int mIntrinsicHeight;
		Matrix mMatrix;

		public Bitmap getBitmap() {
			return mBitmap;
		}

		/* package */void setBitmap(Bitmap bitmap) {
			mBitmap = bitmap;
			if (mBitmap == null)
				return;
			mIntrinsicWidth = mBitmap.getWidth();
			mIntrinsicHeight = mBitmap.getHeight();
			mMatrix = null;
		}

		@Override
		public void draw(Canvas canvas) {
			if (mBitmap == null)
				return;

			if (mMatrix == null) {
				final int vwidth = canvas.getWidth();
				final int vheight = canvas.getHeight();
				final int dwidth = mIntrinsicWidth;
				final int dheight = mIntrinsicHeight;

				float scale = 1.0f;

				if (dwidth < vwidth || dheight < vheight) {
					scale = Math.max((float) vwidth / (float) dwidth,
							(float) vheight / (float) dheight);
				}

				float dx = (vwidth - dwidth * scale) * 0.5f + 0.5f;
				float dy = (vheight - dheight * scale) * 0.5f + 0.5f;

				mMatrix = new Matrix();
				mMatrix.setScale(scale, scale);
				mMatrix.postTranslate((int) dx, (int) dy);
			}

			canvas.drawBitmap(mBitmap, mMatrix, null);
		}

		@Override
		public int getOpacity() {
			return android.graphics.PixelFormat.OPAQUE;
		}

		@Override
		public void setAlpha(int alpha) {
			// Ignore
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
			// Ignore
		}
	}
}
