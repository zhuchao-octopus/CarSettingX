package com.my.factory;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.my.hardware.setting.HWSetting;
import com.octopus.android.carsettingx.R;

public class Settings extends Activity {
	private final static String TAG = "MYSettings";

	private final static int PRODUCT_SETTING_ = 0x03;
	private final static int REAK_SETTING_ = 0x04;
	private final static int RESET_MCU = 0x05;
	private final static int RESET_SYSTEM = 0x06;
	private final static int AUXIN_SETTING_ = 0x07;
	private final static int DVD_SETTING_ = 0x08;
	private final static int ILLUMIN_SETTING_ = 0x09;
	private final static int RADIO_SETTING = 0x0A;
	private final static int FAN_SETTING_ = 0x0B;
	private final static int RESET_MCU_SECRET = 0x0C;
	private final static int RADIO_PARAM_SETTING_ = 0x0D;
	private final static int RESET_RADIO = 0x0E;
	private final static int LED_SETTING = 0x0F;
	private final static int RADIO_REGION_SETTING_ = 0x10;
	private final static int CAR_SUPPORT_SETTING_ = 0x11;
	private final static int CAR_SUPPORT_PARAM_SETTING_ = 0x12;
	private final static int CANBOX_SETTING_ = 0x13;
	private final static int RDS_SETTING_ = 0x14;
	private final static int SPEC_FUNCTION_SETTING_ = 0x15;
	private final static int HIDE_SETTING_ = 0x16;

	private final HWSetting mHWSetting = new HWSetting() ;
	private void doRunActivity(String name){
		try {
    		Intent it = new Intent(name);
    		it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		startActivity(it);
		} catch (Exception e) {
    		Log.e(getString(R.string.tag_name), e.getMessage());
		}
	}
	
	Dialog productSettingDialog() {
		CharSequence[] items = { getString(R.string.auxin_setting),
				getString(R.string.reak_setting),
				getString(R.string.illumin_setting),
				getString(R.string.radio_setting),
				getString(R.string.radio_region_setting),
				getString(R.string.steering_wheel_control_settings_title),
				getString(R.string.pannel_control_settings_title),
				getString(R.string.fan_setting),
				getString(R.string.run_script),
				getString(R.string.logo_settings_title),
				getString(R.string.led_settings_title),	
				getString(R.string.canbox_settings_title),
				getString(R.string.rds_settings_title),
				getString(R.string.spec_function_title),
				getString(R.string.hide_app_title),
				getString(R.string.reset_system_title),
				
		// getString(R.string.mcu_reset),getString(R.string.system_reset)
		};
		// mHWSetting.sendBTCommand(HWSetting.SETTING_GET_AUXIN);

		return new AlertDialog.Builder(this)
				.setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						finish();
					}
				})
				.setTitle(R.string.product_setting)
				.setItems(items, new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						switch (which) {
						case 0:
							showDialog(AUXIN_SETTING_);
							break;
						case 1:
							showDialog(REAK_SETTING_);
							break;
						case 2:
							showDialog(ILLUMIN_SETTING_);
							break;
						case 3:
							showDialog(RADIO_SETTING);
							break;
						case 4:
							showDialog(RADIO_REGION_SETTING_);
							break;
						case 5:
							doRunActivity("com.SwcApplication.intent.action.SwcActivity");
							break;
						case 6:
							doRunActivity("com.SwcApplication.intent.action.PanelActivity");
							break;
						case 7:
							showDialog(FAN_SETTING_);
							break;
						case 8:
							runScript();
							finish();
						case 9:
							doRunActivity("com.my.logo.intent.action.LogoActivity");
							break;
						case 10:
							showDialog(LED_SETTING);
							break;
						case 11:
							showDialog(CANBOX_SETTING_);
							break;
						case 12:
							showDialog(RDS_SETTING_);
							break;
						case 13:
							showDialog(SPEC_FUNCTION_SETTING_);
							break;
						case 14:
							showDialog(HIDE_SETTING_);
							break;
						case 15:
							showDialog(RESET_SYSTEM);
							break;
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
	}
	private void runScript(){
		int err;
		if(new File("/extsd/ak47_script").exists() == true){
			Toast.makeText(this,"find script file in extsd", Toast.LENGTH_SHORT).show();
			err = do_exec("sudo ak47ak47 busybox cp /extsd/ak47_script /data");
		}else  if(new File("/extsd2/ak47_script").exists() == true){
			Toast.makeText(this,"find script file in extsd2", Toast.LENGTH_SHORT).show();
			err = do_exec("sudo ak47ak47 busybox cp /extsd2/ak47_script /data");
		}else{
			Toast.makeText(this,"didn't find script file", Toast.LENGTH_SHORT).show();
			return;
		}
						
		Toast.makeText(this, "\"cp\"" + " = " + err, Toast.LENGTH_SHORT).show();
		err = do_exec("sudo ak47ak47 busybox chmod +x /data/ak47_script");
		Toast.makeText(this, "\"chmod\"" + " = " + err, Toast.LENGTH_SHORT).show();
		err = do_exec("sudo ak47ak47 /data/ak47_script");
		Toast.makeText(this, "\".\"" + " = " + err, Toast.LENGTH_SHORT).show();
		err = do_exec("sudo ak47ak47 busybox rm /data/ak47_script");
		Toast.makeText(this, "\"rm\"" + " = " + err, Toast.LENGTH_SHORT).show();
	}

	private int do_exec(String cmd) {
		try {
			int err = Runtime.getRuntime().exec(cmd).waitFor();
			return err;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return -4;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		switch (id) {
		
		case CANBOX_SETTING_:
		 return canboxSettingDialog();
		
		case LED_SETTING:
		 return ledSettingDialog();

		case RDS_SETTING_:
			return rdsSettingDialog();

		case PRODUCT_SETTING_:
			return productSettingDialog();

		case AUXIN_SETTING_:
			return auxInSettingDialog();

		case DVD_SETTING_:
			return dvdSettingDialog();

		case REAK_SETTING_:
			return reakSettingDialog();

		case ILLUMIN_SETTING_:
			return illuminSettingDialog();

		case RADIO_SETTING:
			return radioSettingDialog();

		case FAN_SETTING_:
			return fanSettingDialog();

		case RESET_MCU_SECRET:
			return resetMcuSecretDialog();

		case RESET_MCU:
			return resetMcuDialog();

		case RESET_SYSTEM:
			return resetSystemDialog();

		case RADIO_PARAM_SETTING_:
			return radioParamSettingDialog();

		case RESET_RADIO:
			return resetRadioDialog();

		case RADIO_REGION_SETTING_:
			return radioRegionSettingDialog();
			
		case HIDE_SETTING_:
			return hideAppDialog();
		case SPEC_FUNCTION_SETTING_:
			return specFunctionDialog();
		case CAR_SUPPORT_SETTING_:
			return carSupportSettingDialog();

			// case CAR_SUPPORT_PARAM_SETTING_:
			// return carSupportParamSettingDialog();
			//
			// case UPDATE_FIRMWARE:
			// return updateFirmwareDialog();
		}
		return super.onCreateDialog(id, args);
	}

	int radioParam;
	int radioParamDefault;
	View radioParamView = null;
	Dialog radioParamSettingDialog(){			
		LayoutInflater inflater = getLayoutInflater(); 
		radioParamView = inflater.inflate(R.layout.radio_param_setting, null);
		((TextView)radioParamView.findViewById(R.id.radio_param)).setText(String.valueOf(radioParam));
		((Button)radioParamView.findViewById(R.id.radio_default)).setOnClickListener(new android.view.View.OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d(TAG,"radioParamSettingDialog select = " + radioSelect+",radio =" +radioParam);
				radioParam = radioParamDefault;
				((TextView)radioParamView.findViewById(R.id.radio_param)).setText(String.valueOf(radioParamDefault));
				mHWSetting.sendBTCommand(HWSetting.SETTING_SET_RADIO,radioSelect+1,radioParamDefault);
			}
		});
		Button decBtn = (Button)radioParamView.findViewById(R.id.radio_dec);
		decBtn.setOnClickListener(new android.view.View.OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d(TAG,"radioParamSettingDialog select = " + radioSelect+",radio =" +radioParam);	
				--radioParam;
				radioParam = radioParam < 0 ? 0:radioParam;
				((TextView)radioParamView.findViewById(R.id.radio_param)).setText(String.valueOf(radioParam));
				mHWSetting.sendBTCommand(HWSetting.SETTING_SET_RADIO,radioSelect+1,radioParam);
			}
		});
		
		decBtn.setLongClickable(true);
		decBtn.setOnLongClickListener(new OnLongClickListener() {	
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				radioParam-=30;
				radioParam = radioParam < 0 ? 0:radioParam;
				((TextView)radioParamView.findViewById(R.id.radio_param)).setText(String.valueOf(radioParam));
				mHWSetting.sendBTCommand(HWSetting.SETTING_SET_RADIO,radioSelect+1,radioParam);
				return true;
			}
		});
		
		Button incBtn = (Button)radioParamView.findViewById(R.id.radio_inc);
		incBtn.setOnClickListener(new android.view.View.OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d(TAG,"radioParamSettingDialog select = " + radioSelect+",radio =" +radioParam);
				++radioParam;
				radioParam = radioParam > 255? 255:radioParam;
				((TextView)radioParamView.findViewById(R.id.radio_param)).setText(String.valueOf(radioParam));
				mHWSetting.sendBTCommand(HWSetting.SETTING_SET_RADIO,radioSelect+1,radioParam);
			}
		});
		incBtn.setLongClickable(true);
		incBtn.setOnLongClickListener(new OnLongClickListener() {	
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				radioParam+=30;
				radioParam = radioParam > 255? 255:radioParam;
				((TextView)radioParamView.findViewById(R.id.radio_param)).setText(String.valueOf(radioParam));
				mHWSetting.sendBTCommand(HWSetting.SETTING_SET_RADIO,radioSelect+1,radioParam);
				return true;
			}
		});	
		
		((Button)radioParamView.findViewById(R.id.radio_listening)).setOnClickListener(new android.view.View.OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				try{
//					Intent it= new Intent(getString(R.string.app_radio));
//					it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//					startActivity(it);
//				}catch(Exception e){
//					Log.e(TAG, e.getMessage());
//				}			
			}
		});
			
		return new AlertDialog.Builder(this)
		
		.setTitle(getResources().getStringArray(R.array.radio_select)[radioSelect])
		.setView(radioParamView)
		.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					showDialog(RADIO_SETTING);
				}
			})
		.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {	
					mHWSetting.sendBTCommand(HWSetting.SETTING_SET_RADIO,radioSelect+1,radioParamDefault);
					showDialog(RADIO_SETTING);
				}
			})
		.setOnCancelListener(new OnCancelListener(){
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					mHWSetting.sendBTCommand(HWSetting.SETTING_SET_RADIO,radioSelect+1,radioParamDefault);
					showDialog(RADIO_SETTING);
				}
			})
		.create();
	}
	
	Dialog auxInSettingDialog() {
		int select = mHWSetting.sendBTCommand(HWSetting.SETTING_GET_AUXIN);
		if (select < 0) {
			select = mHWSetting.sendBTCommand(HWSetting.SETTING_GET_AUXIN);
		}

		return new AlertDialog.Builder(this)
			.setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						showDialog(PRODUCT_SETTING_);
					}
				})
				.setTitle(R.string.auxin_setting)
				.setSingleChoiceItems(
						getResources().getStringArray(R.array.auxin_select),
						select, new OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								mHWSetting.sendBTCommand(
										HWSetting.SETTING_SET_AUXIN, which);
								removeDialog(AUXIN_SETTING_);
								showDialog(PRODUCT_SETTING_);
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								showDialog(PRODUCT_SETTING_);
							}
						}).create();
	}

	Dialog ledSettingDialog() {
		int select = mHWSetting.sendBTCommand(HWSetting.SETTING_GET_LED);
		if (select < 0) {
			select = mHWSetting.sendBTCommand(HWSetting.SETTING_GET_LED);
		}

		return new AlertDialog.Builder(this)
			.setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						showDialog(PRODUCT_SETTING_);
					}
				})
				.setTitle(R.string.led_settings_title)
				.setSingleChoiceItems(
						getResources().getStringArray(R.array.led_select),
						select, new OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								mHWSetting.sendBTCommand(
										HWSetting.SETTING_SET_LED, which);
								removeDialog(AUXIN_SETTING_);
								showDialog(PRODUCT_SETTING_);
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								showDialog(PRODUCT_SETTING_);
							}
						}).create();
	}
	
	private int mHideApp = 0;
	private int mSpecFunction = 0;
	private int mCanboxSelect = -1;
	private int mRdsSelect = -1;
	private static final String CONIF_BOOT_FILE = "/data/.config_boot";
    private static int CONIF_BOOT_FILE_BUF = 10;
    private static int CONIF_BOOT_APP_START = 1024;
    
    private int readConfigBootFile(){
    	int read = -1;
        File file = new File(CONIF_BOOT_FILE);
        if(file.exists()){
            try {
                FileInputStream is = new FileInputStream(file);
                DataInputStream dis=new DataInputStream(is);

                byte []b = new byte[CONIF_BOOT_FILE_BUF];
                dis.skip(CONIF_BOOT_APP_START);
                dis.read(b);

                mHideApp = ((int)(b[0] & 0xff));
                mCanboxSelect = ((int)(b[1] & 0xff));
                mRdsSelect = ((int)(b[2] & 0x1));
                mSpecFunction = ((int)(b[3] & 0x1));
                
                dis.close();
                is.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return read;
    }
	Dialog canboxSettingDialog() {
	//	readConfigBootFile();

		return new AlertDialog.Builder(this)
			.setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						showDialog(PRODUCT_SETTING_);
					}
				})
				.setTitle(R.string.canbox_settings_title1)
				.setSingleChoiceItems(
						getResources().getStringArray(R.array.canbox_select),
						mCanboxSelect, new OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								mCanboxSelect = which;
								
							}
						})
						.setPositiveButton(R.string.alert_dialog_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						if (mCanboxSelect != -1){
//							do_exec("sudo ak47ak47 read_boot_config 1 "+mCanboxSelect);
//							do_exec("sudo ak47ak47 read_boot_config");
							mCanboxSelect = -1;
						}
						showDialog(PRODUCT_SETTING_);
					}
				})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								mCanboxSelect = -1;
								showDialog(PRODUCT_SETTING_);
							}
						}).create();
	}
	
	Dialog rdsSettingDialog() {
		//mCanboxSelect = readConfigBootFile();

		return new AlertDialog.Builder(this)
			.setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						showDialog(PRODUCT_SETTING_);
					}
				})
				.setTitle(R.string.rds_settings_title)
				.setSingleChoiceItems(
						getResources().getStringArray(R.array.rds_select),
						mRdsSelect, new OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								mRdsSelect = which;
								
							}
						})
						.setPositiveButton(R.string.alert_dialog_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						if (mRdsSelect != -1){
							do_exec("sudo ak47ak47 read_boot_config 2 "+mRdsSelect);
							do_exec("sudo ak47ak47 read_boot_config");
							mRdsSelect = -1;
						}
						showDialog(PRODUCT_SETTING_);
					}
				})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								mCanboxSelect = -1;
								showDialog(PRODUCT_SETTING_);
							}
						}).create();
	}
	
	View mHideView = null;
	Dialog hideAppDialog() {
		LayoutInflater inflater = getLayoutInflater();
		mHideView = inflater.inflate(R.layout.hide_app, null);

		CheckBox cb;
		cb = ((CheckBox)mHideView.findViewById(R.id.checkBox1));
		if((mHideApp & (0x1 << 0)) != 0){
			cb.setChecked(true);
		}
		cb = ((CheckBox)mHideView.findViewById(R.id.checkBox2));
		if((mHideApp & (0x1 << 1)) != 0){
			cb.setChecked(true);
		}
		cb = ((CheckBox)mHideView.findViewById(R.id.checkBox3));
		if((mHideApp & (0x1 << 2)) != 0){
			cb.setChecked(true);
		}
		cb = ((CheckBox)mHideView.findViewById(R.id.checkBox4));
		if((mHideApp & (0x1 << 3)) != 0){
			cb.setChecked(true);
		}
		cb = ((CheckBox)mHideView.findViewById(R.id.checkBox5));
		if((mHideApp & (0x1 << 4)) != 0){
			cb.setChecked(true);
		}
		cb = ((CheckBox)mHideView.findViewById(R.id.checkBox6));
		if((mHideApp & (0x1 << 5)) != 0){
			cb.setChecked(true);
		}
		cb = ((CheckBox)mHideView.findViewById(R.id.checkBox7));
		if((mHideApp & (0x1 << 6)) != 0){
			cb.setChecked(true);
		}

		return new AlertDialog.Builder(this)
				.setTitle(R.string.hide_app_title)
				.setView(mHideView)
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								int hideStatus = 0;
								CheckBox cb;
								cb = ((CheckBox)mHideView.findViewById(R.id.checkBox1));
								if(cb.isChecked()){
									hideStatus |= (0x1 << 0);
								}
								cb = ((CheckBox)mHideView.findViewById(R.id.checkBox2));
								if(cb.isChecked()){
									hideStatus |= (0x1 << 1);
								}

								cb = ((CheckBox)mHideView.findViewById(R.id.checkBox3));
								if(cb.isChecked()){
									hideStatus |= (0x1 << 2);
								}
								cb = ((CheckBox)mHideView.findViewById(R.id.checkBox4));
								if(cb.isChecked()){
									hideStatus |= (0x1 << 3);
								}
								cb = ((CheckBox)mHideView.findViewById(R.id.checkBox5));
								if(cb.isChecked()){
									hideStatus |= (0x1 << 4);
								}
								cb = ((CheckBox)mHideView.findViewById(R.id.checkBox6));
								if(cb.isChecked()){
									hideStatus |= (0x1 << 5);
								}
								cb = ((CheckBox)mHideView.findViewById(R.id.checkBox7));
								if(cb.isChecked()){
									hideStatus |= (0x1 << 6);
								}
								do_exec("sudo ak47ak47 read_boot_config 0 "+ hideStatus);
								do_exec("sudo ak47ak47 read_boot_config");
								
								showDialog(PRODUCT_SETTING_);
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {


								showDialog(PRODUCT_SETTING_);
							}
						}).setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {


						showDialog(PRODUCT_SETTING_);
					}
				}).create();
	}
	View mSpecView = null;
	Dialog specFunctionDialog() {
		LayoutInflater inflater = getLayoutInflater();
		mSpecView = inflater.inflate(R.layout.spec_function_settings, null);

		CheckBox cb;
		cb = ((CheckBox)mSpecView.findViewById(R.id.checkBox1));
		if((mSpecFunction & (0x1 << 0)) != 0){
			cb.setChecked(true);
		}
		return new AlertDialog.Builder(this)
				.setTitle(R.string.spec_function_title)
				.setView(mSpecView)
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								int specFunc = 0;
								CheckBox cb;
								cb = ((CheckBox)mSpecView.findViewById(R.id.checkBox1));
								if(cb.isChecked()){
									specFunc |= (0x1 << 0);
								}
								do_exec("sudo ak47ak47 read_boot_config 3 "+ specFunc);
								do_exec("sudo ak47ak47 read_boot_config");
								
								showDialog(PRODUCT_SETTING_);
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {


								showDialog(PRODUCT_SETTING_);
							}
						}).setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {


						showDialog(PRODUCT_SETTING_);
					}
				}).create();
	}
	Dialog dvdSettingDialog() {
		int select = mHWSetting.sendBTCommand(HWSetting.SETTING_GET_DVD);
		if (select < 0) {
			select = mHWSetting.sendBTCommand(HWSetting.SETTING_GET_DVD);
		}

		return new AlertDialog.Builder(this)
				.setTitle(R.string.dvd_setting)
				.setSingleChoiceItems(
						getResources().getStringArray(R.array.dvd_select),
						select, new OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								mHWSetting.sendBTCommand(
										HWSetting.SETTING_SET_DVD, which);
								removeDialog(DVD_SETTING_);
								showDialog(PRODUCT_SETTING_);
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								showDialog(PRODUCT_SETTING_);
							}
						}).create();
	}

	Dialog reakSettingDialog() {
		int select = mHWSetting.sendBTCommand(HWSetting.SETTING_GET_REAKSW);
		if (select < 0) {
			select = mHWSetting.sendBTCommand(HWSetting.SETTING_GET_REAKSW);
		}

		return new AlertDialog.Builder(this)
		.setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						showDialog(PRODUCT_SETTING_);
					}
				})
				.setTitle(R.string.reak_setting)
				.setSingleChoiceItems(
						getResources().getStringArray(R.array.reak_select),
						select, new OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								mHWSetting.sendBTCommand(
										HWSetting.SETTING_SET_REAKSW, which);
								removeDialog(REAK_SETTING_);
								showDialog(PRODUCT_SETTING_);

								// if (mHWSetting.mICaritService != null) {
								// int status = -1;
								// switch (which) {
								// case 0:
								// status = CaritCmd.PLAYSTATUS_BRAKE_DECT_OFF;
								// break;
								// case 1:
								// status =
								// CaritCmd.PLAYSTATUS_BRAKE_DECT_LEVEL;
								// break;
								// case 2:
								// status =
								// CaritCmd.PLAYSTATUS_BRAKE_DECT_PULSE;
								// break;
								// }
								// if (status != -1) {
								// try {
								// mHWSetting.mICaritService.reportPlayStatus(
								// CaritCmd.SOURCE_BT, status);
								// } catch (Exception e) {
								// Log.e(TAG, " " + e.getMessage());
								// }
								// }
								// }
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								showDialog(PRODUCT_SETTING_);
							}
						}).create();
	}

	Dialog illuminSettingDialog() {
		int select = mHWSetting.sendBTCommand(HWSetting.SETTING_GET_ILLUMIN);
		if (select < 0) {
			select = mHWSetting.sendBTCommand(HWSetting.SETTING_GET_ILLUMIN);
		}

		return new AlertDialog.Builder(this)
		.setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						showDialog(PRODUCT_SETTING_);
					}
				})
				.setTitle(R.string.illumin_setting)
				.setSingleChoiceItems(
						getResources().getStringArray(R.array.illumin_select),
						select, new OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								mHWSetting.sendBTCommand(
										HWSetting.SETTING_SET_ILLUMIN, which);
								removeDialog(ILLUMIN_SETTING_);
								showDialog(PRODUCT_SETTING_);
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								showDialog(PRODUCT_SETTING_);
							}
						}).create();
	}

	int radioSelect = 0;

	Dialog radioSettingDialog() {
		return new AlertDialog.Builder(this)
		.setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						showDialog(PRODUCT_SETTING_);
					}
				})
				.setTitle(R.string.radio_setting)
				.setItems(getResources().getStringArray(R.array.radio_select),
						new OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								radioSelect = which;
								showDialog(which == 4 ? RESET_RADIO
										: RADIO_PARAM_SETTING_);
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								showDialog(PRODUCT_SETTING_);
							}
						}).create();
	}

	Dialog resetRadioDialog() {
		return new AlertDialog.Builder(this)
				.setIcon(R.drawable.dialog_alert_icon)
				.setTitle(
						getResources().getStringArray(R.array.radio_select)[radioSelect])
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								mHWSetting.sendBTCommand(
										HWSetting.SETTING_SET_RADIO, 0xff, 0);
								showDialog(RADIO_SETTING);
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								showDialog(RADIO_SETTING);
							}
						}).setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						showDialog(RADIO_SETTING);
					}
				}).create();
	}

	Dialog radioRegionSettingDialog() {
		int select = mHWSetting
				.sendBTCommand(HWSetting.SETTING_GET_RADIO_REGION);
		if (select < 0) {
			select = mHWSetting
					.sendBTCommand(HWSetting.SETTING_GET_RADIO_REGION);
		}

		return new AlertDialog.Builder(this)
		.setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						showDialog(PRODUCT_SETTING_);
					}
				})
				.setTitle(R.string.radio_region_setting)
				.setSingleChoiceItems(
						getResources().getStringArray(
								R.array.radio_region_select), select,
						new OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								mHWSetting.sendBTCommand(
										HWSetting.SETTING_SET_RADIO_REGION,
										which);
								removeDialog(RADIO_REGION_SETTING_);
								showDialog(PRODUCT_SETTING_);
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								showDialog(PRODUCT_SETTING_);
							}
						}).create();
	}

	Dialog carSupportSettingDialog() {
		return new AlertDialog.Builder(this)
				.setTitle(R.string.car_support_setting)
				.setItems(
						getResources().getStringArray(
								R.array.car_support_select),
						new OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								getIntent()
										.putExtra(
												getString(R.string.car_support_setting),
												getResources()
														.getStringArray(
																R.array.car_support_select)[which]);

								removeDialog(CAR_SUPPORT_SETTING_);
								showDialog(CAR_SUPPORT_PARAM_SETTING_);
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								showDialog(PRODUCT_SETTING_);
							}
						}).create();
	}

	Dialog fanSettingDialog() {
		int select = mHWSetting.sendBTCommand(HWSetting.SETTING_GET_FAN);
		if (select < 0) {
			select = mHWSetting.sendBTCommand(HWSetting.SETTING_GET_FAN);
		}

		return new AlertDialog.Builder(this)
				.setTitle(R.string.fan_setting)
				.setSingleChoiceItems(
						getResources().getStringArray(R.array.fan_select),
						select, new OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								mHWSetting.sendBTCommand(
										HWSetting.SETTING_SET_FAN, which);
								removeDialog(FAN_SETTING_);
								showDialog(PRODUCT_SETTING_);
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								showDialog(PRODUCT_SETTING_);
							}
						}).create();
	}

	Dialog resetMcuSecretDialog() {
		return new AlertDialog.Builder(this)
				.setIcon(R.drawable.dialog_alert_icon)
				.setTitle(R.string.mcu_secret_reset)
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								mHWSetting
										.sendBTCommand(HWSetting.SETTING_SECRET_RESET);
								showDialog(PRODUCT_SETTING_);
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								showDialog(PRODUCT_SETTING_);
							}
						}).create();
	}

	Dialog resetMcuDialog() {
		return new AlertDialog.Builder(this)
				.setIcon(R.drawable.dialog_alert_icon)
				.setTitle(R.string.mcu_reset)
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								mHWSetting
										.sendBTCommand(HWSetting.SETTING_MCU_RESET);
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								showDialog(PRODUCT_SETTING_);
							}
						}).create();
	}

	Dialog resetSystemDialog() {
		return new AlertDialog.Builder(this)
				.setIcon(R.drawable.dialog_alert_icon)
				.setTitle(R.string.system_reset)
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								do_exec("sudo ak47ak47 rm -r /data/*");
							//	android.os.SystemProperties.set("ctl.start", "clear_data");
								mHWSetting
										.sendBTCommand(HWSetting.SETTING_SYSTEM_RESET);
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								showDialog(PRODUCT_SETTING_);
							}
						}).create();
	}

	int CAR_SUPPORT_PARKING = 0x02;
	int CAR_SUPPORT_RADAR = 0x03;

	@Override
	protected void onPause() {
		finish();
		super.onPause();
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		showDialog(PRODUCT_SETTING_);
		final EditText ev = new EditText(this);
		final Toast t = Toast.makeText(this, "password error!", Toast.LENGTH_SHORT);
		AlertDialog ad = new AlertDialog.Builder(this)
		.setIcon(R.drawable.dialog_alert_icon)
		.setTitle(R.string.input_code)
		.setPositiveButton(R.string.alert_dialog_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						String str = ev.getText().toString();
						if ("1234".equals(str)){
							readConfigBootFile();
							showDialog(PRODUCT_SETTING_);
						} else {
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
				}).create();;
		ad.setView(ev);
		
		ad.setOnCancelListener(new OnCancelListener(){
			public void onCancel(DialogInterface dialog){
				finish();
			}
		});
		
		ad.show();
	}
}
