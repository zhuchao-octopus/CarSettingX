package com.my.filemanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import android.os.StatFs;
import android.util.Log;

public class Command {

	public static final String TAG = "Command";

	public static final String COMMANDPREFIX = "/system/bin/busybox";

	public static final String CMD_COPY = "cp -r -f ";
	public static final String CMD_MV = "mv -f ";
	public static final String CMD_DELETE = "rm -r ";
	public static final String CMD_SYNC = "sync";

	public static final int COPY = 0x00;
	public static final int PASTE = 0x01;
	public static final int MV = 0x02;
	public static final int DELETE = 0x03;
	public static final int RENAME = 0x04;
	public static final int NEWFOLDER = 0x05;
	


	public static int preCommand = -1;
	public static int postCommand = -1;
	public static String[] operate_files;

	public static final int SUCCESS = -1;
	public static final int FAILED = 0;
	// public static final int PERMISSIONFAILED = 0x02;
	private CommandsInterface mExec;
    private CommandCallBack mCallBack;
	private String mNewFileName;
	public Command(int command, String[] files,CommandCallBack callBack) {
		if(command==DELETE||command==MV||command==COPY){
			cleanCommand();
		}
		
		if (preCommand == -1) {
			preCommand = command;
			operate_files = files;
		} else {
			postCommand = command;
		}
		
		mCallBack = callBack;
	}

	public int exec(String parameter) {

		Log.e(TAG, "preCommand = " + preCommand + "postCommand = " + postCommand + " parameter = " + parameter + " preCommand="
				+ preCommand);
		
		if (parameter != null && !parameter.endsWith("/"))
			parameter += "/";
		int cmd = (postCommand != -1) ? postCommand : preCommand;
		switch (cmd) {
		case DELETE:
			for (int i = 0; i < operate_files.length; i++) {
				//String path = "\""+operate_files[i]+"\"";
				 ArrayList<String> commandLine = new ArrayList<String>(); 
				    commandLine.add( COMMANDPREFIX);
				    commandLine.add( "rm");   
				    commandLine.add( "-r");   
				    commandLine.add( operate_files[i]);  
				Log.e(TAG, operate_files[i]);
				if (SUCCESS != do_exec(commandLine.toArray(new String[commandLine.size()]))) {
					do_exec(new String[]{COMMANDPREFIX,CMD_SYNC});
					return i;
				}

			}
			do_exec(new String[]{COMMANDPREFIX,CMD_SYNC});
			preCommand = -1;
			break;
		case COPY:
			preCommand = COPY;
			break;
		case MV:
			preCommand = MV;
			break;
		case PASTE:
			String names[] = new File(parameter).list();
			if (preCommand == COPY) {
				for (int i = 0; i < operate_files.length; i++) {
					Log.e(TAG,"filesize = "+getFileSize(new File(operate_files[i]))+ " names="+operate_files[i]+" ava="+get_Available_Space(new File(parameter)));
					if(getFileSize(new File(operate_files[i]))>get_Available_Space(new File(parameter))){
						if(mCallBack!=null)
						mCallBack.noSpace(operate_files[i]);
						break;
					}
					 ArrayList<String> commandLine = new ArrayList<String>();   
					 commandLine.add( COMMANDPREFIX);    
					 commandLine.add( "cp");   
					    commandLine.add( "-r");  
					    commandLine.add( "-f");
					    commandLine.add( operate_files[i]); 
					    commandLine.add( parameter);
					 String fileName = new File(operate_files[i]).getName().toLowerCase();
					 fileName=fileName.toLowerCase();
					 if(names!=null&&mCallBack!=null){
					 for(int t = 0;t<names.length;t++){
						 Log.e(TAG,"filename = "+fileName+ " names="+names[t]);
						if( fileName.toLowerCase().equals(names[t].toLowerCase())){
							int result = mCallBack.replace(fileName);
							 Log.e(TAG,"replace result= "+result);
							if(result==CommandsInterface.SKIP){
								continue;
							}else if(result==CommandsInterface.CANCEL){
								break;
							}
						}
						 
					 }
					 }
					if (SUCCESS != do_exec(commandLine.toArray(new String[commandLine.size()]))) {
						do_exec(new String[]{COMMANDPREFIX,CMD_SYNC});
						return i;
					}
				}
			} else if (preCommand == MV) {
				for (int i = 0; i < operate_files.length; i++) {
					if(getFileSize(new File(operate_files[i]))>get_Available_Space(new File(parameter))){
						mCallBack.noSpace(operate_files[i]);
						break;
					}
					ArrayList<String> commandLine = new ArrayList<String>();   
					commandLine.add( COMMANDPREFIX);  
					commandLine.add( "mv");   
				    commandLine.add( "-f");
				    commandLine.add( operate_files[i]); 
				    commandLine.add( parameter);
					if (SUCCESS != do_exec(commandLine.toArray(new String[commandLine.size()]))) {
						do_exec(new String[]{COMMANDPREFIX,CMD_SYNC});
						return i;
					}
				}
				preCommand = -1;
			}else{
				operate_files=null;
			}
			do_exec(new String[]{COMMANDPREFIX,CMD_SYNC});
			
			break;
		}
		
		return SUCCESS;
	}

	private int do_exec(String [] cmd) {
		 //Log.e(TAG, cmd);
		try {
			
			Process p = Runtime.getRuntime().exec(cmd);
			try {
				StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
				// kick off stderr
				errorGobbler.start();
				StreamGobbler outGobbler = new StreamGobbler(p.getInputStream(), "STDOUT");
				// kick off stdout
				outGobbler.start();
				p.waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
		return SUCCESS;
	}

	public class StreamGobbler extends Thread {
		InputStream is;
		String type;
		OutputStream os;

		StreamGobbler(InputStream is, String type) {
			this(is, type, null);
		}

		StreamGobbler(InputStream is, String type, OutputStream redirect) {
			this.is = is;
			this.type = type;
			this.os = redirect;
		}

		public void run() {
			InputStreamReader isr = null;
			BufferedReader br = null;
			PrintWriter pw = null;
			try {
				if (os != null)
					pw = new PrintWriter(os);

				isr = new InputStreamReader(is);
				br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null) {
					if (pw != null)
						pw.println(line);
					System.out.println(type + ">" + line);
				}

				if (pw != null)
					pw.flush();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} finally {
				try {
					if (pw != null)
						pw.close();
					if (br != null)
						br.close();
					if (isr != null)
						isr.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private void cleanCommand(){
		preCommand = -1;
		postCommand = -1;
		operate_files=null;
	}
	public static long getFileSize(File file) {
		long dirSize = 0;
		if (file == null) {
			return 0;
		}
		if (!file.isDirectory()) {
			return file.length();
		} else {
			File[] files = file.listFiles();
			for (File f : files) {
				if (f.isFile()) {
					dirSize += f.length();
				} else if (file.isDirectory()) {
					dirSize += f.length();
					dirSize += getFileSize(f); // 濡傛灉閬囧埌鐩綍鍒欓�杩囬�褰掕皟鐢ㄧ户缁粺璁�
				}
			}
		}
		return dirSize;
	}
	public static long get_Available_Space(final File file) {
		// File path = Environment.getExternalStorageDirectory();
		if (file != null&&file.exists()) {
			String path = file.getPath();
			StatFs stat = new StatFs(path);
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			return availableBlocks * blockSize;
		} else if(file != null){
			String path = file.getPath();
			path=path.substring(0, path.lastIndexOf("/"));
			if(!new File(path).exists()){
				return 0;
			}
			StatFs stat = new StatFs(path);
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			return availableBlocks * blockSize;
		}else{
			return 0;
			
		}
	}
	public int _exec(String parameter) {
		Log.e(TAG, "preCommand = " + preCommand + "postCommand = " + postCommand + " parameter = " + parameter + " preCommand="
				+ preCommand);
		mExec = new JavaImplCommand();
		if (parameter != null && !parameter.endsWith("/"))
			parameter += "/";
		int cmd = (postCommand != -1) ? postCommand : preCommand;
		switch (cmd) {
		case DELETE:
			for (int i = 0; i < operate_files.length; i++) {
				//String path = "\""+operate_files[i]+"\"";
				 
				Log.e(TAG, operate_files[i]);
				if (SUCCESS != mExec.delete(new File(operate_files[i]), mCallBack)) {
					do_exec(new String[]{COMMANDPREFIX,CMD_SYNC});
					return i;
				}

			}
			do_exec(new String[]{COMMANDPREFIX,CMD_SYNC});
			preCommand = -1;
			break;
		case COPY:
			preCommand = COPY;
			break;
		case PASTE:
			if (preCommand == COPY) {
				for (int i = 0; i < operate_files.length; i++) {
					
					File source = new File(operate_files[i]);
					File target = new File(parameter);
					Log.e(TAG, "source.getPath() = "+source.getAbsolutePath()+" target.getPath()="+target.getPath());
					if(target.getPath().startsWith(source.getAbsolutePath())){
						continue;
					}
					if(getFileSize(source)>get_Available_Space(target)){
						if(mCallBack!=null)
						mCallBack.noSpace(operate_files[i]);
						break;
					}
				
					if (SUCCESS != mExec.copy(source, target, mCallBack)) {
						do_exec(new String[]{COMMANDPREFIX,CMD_SYNC});
						return i;
					}
				}
			} else if (preCommand == MV) {
				for (int i = 0; i < operate_files.length; i++) {
					File source = new File(operate_files[i]);
					File target = new File(parameter);
//					if(source.getPath().startsWith(target.getPath())){
//						continue;
//					}
					if(getFileSize(source)>get_Available_Space(target)){
						mCallBack.noSpace(operate_files[i]);
						break;
					}
					
					if (SUCCESS != mExec.mv(source, target, mCallBack)) {
						do_exec(new String[]{COMMANDPREFIX,CMD_SYNC});
						return i;
					}
				}
				preCommand = -1;
			}else{
				operate_files=null;
			}
			do_exec(new String[]{COMMANDPREFIX,CMD_SYNC});
			
			break;
		case MV:
			preCommand = MV;
			break;
		}
		
		return SUCCESS;
	}
	
	public int stop(){
		return mExec.stop();
	}

	public void setNewFileName(String mNewFileName) {
		this.mNewFileName = mNewFileName;
	}

	public String getNewFileName() {
		return mNewFileName;
	}
}
