package com.my.update;

import java.io.File;

public interface CommandCallBack {

	public int replace(String source);
	public void noSpace(String source);
	public void processItem(File file,long total,long current);
	public void cancel();
	public void error(int code);
	
}
