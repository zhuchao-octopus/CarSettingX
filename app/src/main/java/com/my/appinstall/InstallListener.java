package com.my.appinstall;

public interface InstallListener {
	public void startInstall(String name);
	public void installed(String name,int code);

}
