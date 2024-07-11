package com.my.update;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.util.Log;

public class JavaImplCommand implements CommandsInterface {
	private static final String TAG = "JavaImplCommand";
	private boolean isStop = false;

	public int delete(File file, CommandCallBack callBack) {
		if (!isStop) {
			if (file.isDirectory()) {
				// Log.e("JavaImplCommand",file.getPath()+" is Folder");
				File[] files = file.listFiles();
				if (files != null)
					// Log.e("JavaImplCommand",file.getPath()+" have "+files.length+" files");
					for (File item : files) {
						// Log.e("JavaImplCommand",file.getPath()+"/"+item.getName());
						if (item.isDirectory()) {
							delete(item, callBack);
						} else {
							if(isStop){
								callBack.cancel();
								return SUCCESS;
							}
							callBack.processItem(item, -1, -1);
							if (!item.delete()) {
								return FAILED;
							}
						}
					}
				return file.delete() ? SUCCESS : FAILED;
			} else {
				if(isStop){
					callBack.cancel();
					return SUCCESS;
				}
				callBack.processItem(file, -1, -1);
				return file.delete() ? SUCCESS : FAILED;
			}
		} else {
			callBack.cancel();
			return SUCCESS;
		}
	}

	public int copy(File source, File target, CommandCallBack callBack) {
		String fileName = source.getName();
		String names[] = target.list();
		if (names != null && callBack != null) {
			for (int t = 0; t < names.length; t++) {
				if (fileName.equals(names[t])) {
					int result = callBack.replace(fileName);
					Log.e(TAG, "replace result= " + result);
					if (result == SKIP || result == CANCEL) {
						return FAILED;
					} else {
						if(!source.getPath().equals(target.getPath()+"/"+source.getName())){
						if (delete(new File(target.getPath() + "/" + names[t]), callBack) == SUCCESS) 
							return implCopy(source, target, callBack);
						
						}else
							return SUCCESS;

					}
				}

			}
			return implCopy(source, target, callBack);
		} else {
			return implCopy(source, target, callBack);
		}
	}

	private int implCopy(File source, File target, CommandCallBack callBack) {
		if (!isStop) {
			if (source.isDirectory()) {
				File targetFolder = new File(target.getPath() + "/" + source.getName());
				if (targetFolder.mkdir()) {
					File[] files = source.listFiles();
					for (File tmp : files) {
						if (implCopy(tmp, targetFolder, callBack) != SUCCESS)
							return FAILED;
					}

				} else {
					return FAILED;
				}
			} else {
				try {
					int length = 2097152;
					FileInputStream in;

					in = new FileInputStream(source);
					long currentLength = 0;
					long total = source.length();
					File newFile = new File(target.getPath() + "/" + source.getName());
					FileOutputStream out = new FileOutputStream(newFile);
					byte[] buffer = new byte[length];
					while (true) {
						int ins = in.read(buffer);
						if (ins == -1) {
							in.close();
							out.flush();
							out.close();
							return SUCCESS;
						} else {
							currentLength += ins;
							callBack.processItem(source, total, currentLength);
							out.write(buffer, 0, ins);
						}
						if (isStop) {
							in.close();
							out.flush();
							out.close();
							newFile.delete();
							callBack.cancel();
							return SUCCESS;
						}
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return FAILED;
				} catch (IOException e) {
					e.printStackTrace();
					return FAILED;
				}
			}
			return SUCCESS;
		} else {
			callBack.cancel();
			return SUCCESS;
		}
	}

	public int mv(File source, File target, CommandCallBack callBack) {
		if (copy(source, target, callBack) != SUCCESS) {
			return FAILED;
		} else {
			return delete(source, callBack);
		}
	}

	public int rename(File source, String newName, CommandCallBack callBack) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int mkdir(File newFolder, CommandCallBack callBack) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int stop() {
		isStop = true;
		return 0;
	}

}
