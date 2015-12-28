package com.example.networkbroard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Networbroastrecever extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
	/*	State wifiState = null;
		State mobileState = null;
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
				.getState();
		if (wifiState != null && mobileState != null
				&& State.CONNECTED != wifiState
				&& State.CONNECTED == mobileState) {
			// 手机网络连接成功
			Toast.makeText(context, "有网络", Toast.LENGTH_SHORT).show();
			// StartService(context);
			Log.i("this is action", intent.getAction().toString());
			Intent service = new Intent(context, UpdateService.class);
			context.startService(service);
		} else if (wifiState != null && mobileState != null
				&& State.CONNECTED != wifiState
				&& State.CONNECTED != mobileState) {
			// 手机没有任何的网络
			Toast.makeText(context, "手机没有任何网络", Toast.LENGTH_SHORT).show();
		} else if (wifiState != null && State.CONNECTED == wifiState) {
			// 无线网络连接成功
			Toast.makeText(context, "当前为wifi网络", Toast.LENGTH_SHORT).show();
			Intent service = new Intent(context, UpdateService.class);

			context.startService(service);

		}
*/
		
/*		networkutil util=new networkutil(context);
		util.Initnetworkstate();*/



	}

	/*public void StartService(Context context) {

		boolean serviceRunning = false;

		String serviceName = "com.Jctech.ALLServices.UpdateService";

		ActivityManager am = (ActivityManager) context
				.getSystemService(Service.ACTIVITY_SERVICE);

		List<ActivityManager.RunningServiceInfo> l = am.getRunningServices(50);

		Iterator<ActivityManager.RunningServiceInfo> i = l.iterator();

		while (i.hasNext()) {

			ActivityManager.RunningServiceInfo runningServiceInfo = (ActivityManager.RunningServiceInfo) i
					.next();

			Log.i("superContacts",
					"检查到的服务:" + runningServiceInfo.service.getClassName());

			if (runningServiceInfo.service.getClassName().equals(serviceName)) {

				serviceRunning = true;

			}

		}

		if (!serviceRunning) {

			Intent service = new Intent(context, UpdateService.class);

			context.startService(service);

		}

	}
	*/

}
