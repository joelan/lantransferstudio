package com.test.service;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.test.threads.ListenFileThread;
import com.test.threads.ListenThread;

public class LocalService extends Service {

	private static final String TAG = "LocalService";
	static  ListenThread thread=null;
    static ListenFileThread filethread=null;
	private IBinder binder=new LocalService.LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {

		return binder;
	}
	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}



	@Override
	public void onDestroy() {
		super.onDestroy();

		if(thread!=null)
		{
			Log.i(TAG, "thread is not null");
			thread.closethread();
			thread=null;
		}

		closefilethread();
	}

	/**
	 * 关闭进程
	 */
	public void  closethread()
	{
		if(thread!=null)
	{
		Log.i(TAG, "thread is not null");
		thread.closethread();
		thread=null;
	}

	}

	/**
	 * 关闭进程
	 */
	public void  closefilethread()
	{
		if(filethread!=null)
		{
			Log.i(TAG, "thread is not null");
			filethread.closethread();
			filethread=null;
		}

	}



	public void startWaitDataThread(Handler handler)
	{
		Log.i(TAG, "startWaitDataThread");
		if(thread!=null)
		{
			Log.i(TAG, "thread is not null");
			thread.closethread();
			thread=null;
		}
		thread=	new ListenThread(12345,handler);
		thread.start();

	}
	//定义内容类继承Binder
	public class LocalBinder extends Binder{
		//返回本地服务
		public LocalService getService(){
			return LocalService.this;
		}
	}


	public void startReceiveFile(Handler handler,String path)
	{
		if(filethread!=null)
		{
			filethread.closethread();
			filethread=null;
		}

		filethread=	new ListenFileThread(handler,path);
		filethread.start();
	}

}
