package com.sxau.weather.test;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class TestService extends Service {
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.d("test", "Service+onCreate");
		Intent intent=new Intent();
		intent.putExtra("key", "这是广播的内容");
		intent.setAction("123");
	    sendBroadcast(intent);     
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.d("test", "Service+onStartCommand");
		return super.onStartCommand(intent, flags, startId);
		
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		Log.d("test", "Service+onBind");
		return null;
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.d("test", "Service+onDestroy");
		super.onDestroy();
	}

}
