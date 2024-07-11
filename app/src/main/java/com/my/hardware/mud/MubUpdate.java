package com.my.hardware.mud;

public class MubUpdate {
	static {
		System.loadLibrary("Update");
	}
	
	public MubUpdate() {
		
	}

	public static final int MUD_START = 0x80;
	public static final int MUD_RESET = 0xff;
	
	public static final int MUD_POS = 0x00000001;
	public static final int MUD_FLAG = 0x00000002;
	
	public static final int MUD_FLAG_START = 0x0;
	public static final int MUD_FLAG_UNLOCK = 0x1;
	public static final int MUD_FLAG_DATA = 0x2;
	public static final int MUD_FLAG_CHECKSUM = 0x3;
	public static final int MUD_FLAG_RESET = 0x4;

	public String mPath;

	private native final int nativeSendMUDCommand(int value);
	private native final int nativeSendMUDCommand2(String value);
	public int sendMUDCommand(int value) {
		return nativeSendMUDCommand(value);
	}
	
	public int sendMUDCommand2(String value) {
		return nativeSendMUDCommand2(value);
	}
	
	private IMUDCallback mMUDCallback;
	private void mudCallback(int value) {
		if(mMUDCallback != null) {
			mMUDCallback.mudCallback(value);
		}
	}
	
	public void setMUDCallback(IMUDCallback MUDCallback) {
		mMUDCallback = MUDCallback;
	}
	
	private int mMUDMask = 0xff;
	public void setMUDMask(int value) {
		if(value == 0) {
			value = 0xff;
		}
		mMUDMask = value;
	}
}
