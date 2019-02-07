package com.sxau.weather.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Application;
import android.os.Environment;

public class MyApplication extends Application {
	public static final String  SQL_PATH=Environment.getExternalStorageDirectory().getAbsolutePath()+"/weather"+"/city.db";
	public static boolean dbExist; 
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		dbExist=openDB();
	}
public boolean openDB(){
		
		File fileDB=new File(SQL_PATH);
		
		String dirPath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/weather";
		File fileDir=new File(dirPath);
		if (!fileDir.exists()) {
			fileDir.mkdirs();
		}
		
		if (!fileDB.exists()) {
			try {
				InputStream is=getAssets().open("city.db");
				FileOutputStream fos=new FileOutputStream(fileDB);
				int len=-1;
				byte[] buffer=new byte[1024];
				while ((len=is.read(buffer))!=-1) {
					fos.write(buffer, 0, len);
					fos.flush();
					
				}
				fos.close();
				is.close();
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		else {
			return true;
		}
	}
}
