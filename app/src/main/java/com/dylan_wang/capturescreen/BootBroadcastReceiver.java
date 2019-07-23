package com.dylan_wang.capturescreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver {

	
	/**
	 * 
	 * 这个广播是开机自启服务目前没用：使用需在AndroidManifest配置并测
	 * 
	 * */
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(ACTION)) {
            Intent intent1 = new Intent(context, ScreenService.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(intent1);
        }
    }
}