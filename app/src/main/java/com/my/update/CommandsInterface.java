package com.my.update;

import java.io.File;

public interface CommandsInterface {
	public static final int SUCCESS=-1;
	public static final int EXCEPTION=1;
	public static final int FAILED=0;
	
	public static final int REPLACE = 0x00;
	public static final int SKIP = 0x01;
	public static final int CANCEL = 0x02;
	
	public static final int FAULTYOPERATION=0x00;
	public static final int OPERATIONERROR=0x01;
	
	public int stop();
	public int delete(File file,CommandCallBack callBack);
	public int copy(File source,File target,CommandCallBack callBack);
	public int mv(File source, File target,CommandCallBack callBack);
	public int rename(File source,String newName,CommandCallBack callBack);
	public int mkdir(File newFolder,CommandCallBack callBack);

}
