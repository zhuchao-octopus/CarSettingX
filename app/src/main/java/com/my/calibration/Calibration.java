package com.my.calibration;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;

import com.octopus.android.carsettingx.R;

public class Calibration extends Activity {
	private static final String TAG = "Calibration";
	int width;
	int height;
	int	barHeight = 0;
	
	MyView mView;
	Bitmap mBitmap;

	Thread mThread;
	boolean tAlive = false;
	private boolean checkTouchName(String name){
		try {
			Process p = Runtime.getRuntime().exec(name);
			InputStream is = p.getInputStream();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(is));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("Goodix")){
					return true;
				}
			}
			p.waitFor();
			is.close();
			reader.close();
			p.destroy();

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	private boolean checkTouchScreen() {
		int i = 0;
		for (i = 0; i < 4; ++i){
			if (checkTouchName("sudo ak47ak47 cat /sys/class/input/input"+i+"/name")){
				break;
			}
		}		
		return (i<4);
	}
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);   
        if (checkTouchScreen()){
//        	Toast.makeText(this,
//                    R.string.no_need_calibration, Toast.LENGTH_LONG).show();
        	finish();
        	
        }
        Display display = getWindowManager().getDefaultDisplay();
        width = display.getWidth();
      //  if(android.os.Build.MODEL.equals("SABRESD-MX6DQ")){//61
        	height = 480;
//        	barHeight = getApplicationContext().getResources().getDimensionPixelSize(
//        		com.android.internal.R.dimen.status_bar_height) - 3;
       // }else{
       // 	height = display.getHeight();
      //  }
        mView = new MyView(this);        
        setContentView(mView);
    }

    class MyView extends View {
		Context mContext;

		public MyView(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
			mContext = context;
		}

    	@Override
		protected void onDraw(Canvas canvas) {
			// TODO Auto-generated method stub
    		if(mBitmap != null) {
    			canvas.drawBitmap(mBitmap, 0, 0, null);
    		}
			super.onDraw(canvas);
		}
    }
    
    class MyThread extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			boolean isGood = false;			
			String event;	
			Log.d(TAG,"android.os.Build.MODEL="+android.os.Build.MODEL);
			//if(android.os.Build.MODEL.equals("SABRESD-MX6DQ")){//61
				event = "event1";
			//}else{//53
			//	event = "event2";
			//}
			
			
			do_exec("sudo ak47ak47 chmod 777 /dev/input/"+event);
	    	do_exec("sudo ak47ak47 chmod 777 /sys/module/mxc_ts/parameters/calibration");
	    	do_exec("sudo ak47ak47 touch /data/system/calibration");
	    	do_exec("sudo ak47ak47 chmod 777 /data/system/calibration");

			FileInputStream input;
	        try {
	        	input = new FileInputStream("/dev/input/"+event);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			}			
			while(tAlive){
				clearConfig();
				
				int dx[] = new int [] {width / 4, width / 2, width * 3 / 4, width * 5 / 8};
				int dy[] = new int [] {height / 2, height / 4, height * 3 / 4, height * 5 / 8};
				int tx[] = new int [4];
				int ty[] = new int [4];
				
				for(int i = 0; i < 4; ) {
					drawCalibration(dx[i], dy[i]-barHeight);
					mView.postInvalidate();
					Point p = getInput(input);
					if(p.x == -1 || p.y == -1) {
						break;
					}
					if(p.x > 0 && p.y > 0) {
						tx[i] = p.x;
						ty[i] = p.y;
						i++;
					}
				}
				
				int delta;
				int delta_x[] = new int [3];
				int delta_y[] = new int [3];
				
				delta = (tx[0] - tx[2]) * (ty[1] - ty[2]) - (tx[1] - tx[2]) * (ty[0] - ty[2]);
				delta_x[0] = (dx[0] - dx[2]) * (ty[1] - ty[2]) - (dx[1] - dx[2]) * (ty[0] - ty[2]);
				delta_x[1] = (tx[0] - tx[2]) * (dx[1] - dx[2]) - (tx[1] - tx[2]) * (dx[0] - dx[2]);
				delta_x[2] = dx[0] * (tx[1] * ty[2] - tx[2] * ty[1]) - dx[1] * (tx[0] * ty[2] - tx[2] * ty[0]) + dx[2] * (tx[0] * ty[1] - tx[1] * ty[0]);
				delta_y[0] = (dy[0] - dy[2]) * (ty[1] - ty[2]) - (dy[1] - dy[2]) * (ty[0] - ty[2]);
				delta_y[1] = (tx[0] - tx[2]) * (dy[1] - dy[2]) - (tx[1] - tx[2]) * (dy[0] - dy[2]);
				delta_y[2] = dy[0] * (tx[1] * ty[2] - tx[2] * ty[1]) - dy[1] * (tx[0] * ty[2] - tx[2] * ty[0]) + dy[2] * (tx[0] * ty[1] - tx[1] * ty[0]);
				
				if(delta != 0) {
					int x = (delta_x[0] * tx[3] + delta_x[1] * ty[3] + delta_x[2]) / delta;
					int y = (delta_y[0] * tx[3] + delta_y[1] * ty[3] + delta_y[2]) / delta;
					Log.i(TAG, "(" + dx[3] + "," + dy[3] + ") -> (" + x + "," + y + ")");
					if(Math.abs(dx[3] - x) <= 5 && Math.abs(dy[3] - y) <= 3) {
						writeConfig(delta, delta_x, delta_y);
						isGood = true;
						break;
					}
				}
			}
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(!isGood) {
				resetConfig();
			}
	    	do_exec("sudo ak47ak47 chmod 660 /dev/input/"+event);
	    	do_exec("sudo ak47ak47 chmod 644 /sys/module/mxc_ts/parameters/calibration");
	    	do_exec("sudo ak47ak47 chmod 644 /data/system/calibration");
			if(isGood) {
				finish();
			}
			super.run();
		}
    }
    
	private int []readConfig() {
		int cfg[] = new int [7];
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream("/data/system/calibration")));
		} catch (FileNotFoundException e) {
			return null;
		}
		for(int i = 0; i < 7; i++) {
			try {
				cfg[i] = Integer.valueOf(br.readLine());
			} catch (IOException e) {
				break;
			} catch (NumberFormatException e) {

			}
		}
		try {
			br.close();
		} catch (IOException e) {

		}
		return cfg;
	}

	private void resetConfig() {
		int cfg[] = readConfig();
		if(cfg != null) {
			writeCalibration("/sys/module/mxc_ts/parameters/calibration", new String(cfg[0] + "," + cfg[1] + "," + cfg[2] + "," + cfg[3] + "," + cfg[4] + "," + cfg[5] + "," + cfg[6]).getBytes());
		}
	}

    private int writeCalibration(String file, byte buffer[]) {
    	FileOutputStream fw;
    	try {
        	fw = new FileOutputStream(file);    		
    	} catch (FileNotFoundException e) {
    		return -1;
    	}
    	try {
        	fw.write(buffer);
    	} catch (IOException e) {
    		
    	}
    	try {
        	fw.close();
    	} catch (IOException e) {
    		
    	}
    	return 0;
    }

    private void writeConfig(int delta, int delta_x[], int delta_y[]) {
    	writeCalibration("/sys/module/mxc_ts/parameters/calibration", new String(delta_x[0] + "," + delta_x[1] + "," + delta_x[2] + "," + delta_y[0] + "," + delta_y[1] + "," + delta_y[2] + "," + delta).getBytes());
    	writeCalibration("/data/system/calibration", new String(delta_x[0] + "\n" + delta_x[1] + "\n" + delta_x[2] + "\n" + delta_y[0] + "\n" + delta_y[1] + "\n" + delta_y[2] + "\n" + delta).getBytes());
    }
    
    private void clearConfig() {
    	writeCalibration("/sys/module/mxc_ts/parameters/calibration", new String("0,0,0,0,0,0,0").getBytes());
    }
    
	private static int do_exec(String cmd) {
    	try {
    		int err = Runtime.getRuntime().exec(cmd).waitFor();
    		Log.e("!!","11:"+err);
    		return 0;
    	} catch (InterruptedException e) {
    		e.printStackTrace();
    		return -4;
    	} catch (IOException e) {
    		e.printStackTrace();
    		Log.e("!!","11");
    		return -1;
    	}
    }

    private void drawCalibration(int x, int y) {
		mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		Paint paint = new Paint();
		paint.setTextSize(20);
		paint.setColor(Color.BLUE);
		Canvas canvas = new Canvas();
		canvas.setBitmap(mBitmap);
		canvas.drawLine(x - 10, y, x + 10, y, paint);
		canvas.drawLine(x, y - 10, x, y + 10, paint);
		canvas.drawPoint(x, y, paint);
		canvas.drawText(getString(R.string.calibrattion_text), (float) width / 2, ((float) height / 2)-barHeight, paint);
    }
    
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		if(mThread != null){
			if(mThread.isAlive()) {
				tAlive = false;
				try{
					mThread.join();
				} catch (InterruptedException ignored) {
				}
			}
			mThread = null;
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		mThread = new MyThread();
		tAlive = true;
		mThread.start();
		super.onResume();
	}

	private Point getInput(FileInputStream input)
	{
		int x = 0;
		int y = 0;
		byte ev[] = new byte [16 * 64];
		boolean step = false;
		while(tAlive){
			int rd;
			try{
				rd = input.read(ev, 0, 16 * 64);
			} catch (IOException e) {
				break;
			}
			if(rd < 16)
				continue;
			for(int i = 0; i < rd / 16; i++) {
				int type = (ev[i * 16 + 8] & 0xff) | (ev[i * 16 + 9] & 0xff)<<8;
				int code = (ev[i * 16 + 10] & 0xff) | (ev[i * 16 + 11] & 0xff)<<8;
				int value = (ev[i * 16 + 12] & 0xff) | (ev[i * 16 + 13] & 0xff)<<8 | (ev[i * 16 + 14] & 0xff)<<16 | (ev[i * 16 + 15] & 0xff)<<24;
				switch(type) {
				case 0x00: {
					if(step) {
						return new Point(x, y);
					}
					break;
				}
				case 0x01: {
					if(code == 0x14a && value == 0) {
						step = true;
					}
					break;
				}
				case 0x03: {
					if(code == 0x30 && value == 0) {
						step = true;
					} else if(code == 0x00 || code == 0x35) {
						x = value;
					} else if(code == 0x01 || code == 0x36) {
						y = value;						
					}
					break;
				}
				}
			}
		}
		return new Point(-1, -1);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch(keyCode) {
		case 0: {
			break;
		}
		case KeyEvent.KEYCODE_BACK: {
			break;
		}
		default:
			return super.onKeyDown(keyCode, event);
		}
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch(keyCode) {
		case 0: {
			break;
		}
		case KeyEvent.KEYCODE_BACK: {
			break;
		}
		default:
			return super.onKeyUp(keyCode, event);
		}
		return true;
	}
}