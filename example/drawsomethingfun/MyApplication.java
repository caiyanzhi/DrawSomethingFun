package com.example.drawsomethingfun;

import android.app.Application;
/**
 * 
 * @author yanzhi
 *
 */

public class MyApplication extends Application{
	
	private static MyApplication instance;

	public void onCreate()
	{
		super.onCreate();
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(this);
	}
	@Override
	public void onTerminate()
	{
		super.onTerminate();
	}
	public static MyApplication getInstance() {
		if (instance == null) {
			instance = new MyApplication();
		}
		return instance;
	}
}
