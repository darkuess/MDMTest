package com.example.mdmtest;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

public class MDMReceiver extends DeviceAdminReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == ACTION_DEVICE_ADMIN_DISABLE_REQUESTED) {
            abortBroadcast();
        }
        super.onReceive(context, intent);
	}
	
	@Override
	public void onEnabled(Context context, Intent intent) {
		Intent i = new Intent(MainActivity.ACTION_MDM_RECEIVED);
		i.putExtra(MainActivity.MDM_RECIEVED_ENABLED, true);
		context.sendBroadcast(i);
	}
	
	@Override
	public void onDisabled(Context context, Intent intent) {
		Intent i = new Intent(MainActivity.ACTION_MDM_RECEIVED);
		i.putExtra(MainActivity.MDM_RECIEVED_DIABLED, true);
		context.sendBroadcast(i);
	}
}
