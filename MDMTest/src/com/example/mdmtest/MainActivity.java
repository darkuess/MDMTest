package com.example.mdmtest;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int REQUEST_CODE_ENABLE_ADMIN = 1;

    public static final String ACTION_MDM_RECEIVED = "action_mdm_received";
    public static final String MDM_RECIEVED_DIABLED = "mdm_diabled";
    public static final String MDM_RECIEVED_ENABLED = "mdm_enabled";
    
	DevicePolicyManager mDPM;
	ComponentName mDeviceAdminSample;
	boolean mCameraEnable;
	Button mAdminButton;
	Button mCameraButton;
	boolean mAdminActive;
	IntentFilter mActionMDMReceiverFilter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mAdminButton = (Button) findViewById(R.id.admin_button);
		mCameraButton = (Button) findViewById(R.id.camera_button);
		
        // Prepare to work with the DPM
        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDeviceAdminSample = new ComponentName(this, MDMReceiver.class);

        mActionMDMReceiverFilter = new IntentFilter();
        mActionMDMReceiverFilter.addAction(ACTION_MDM_RECEIVED);
        registerReceiver(mActionMDMReceiver, mActionMDMReceiverFilter);        
	}


	@Override
	protected void onDestroy() {
		unregisterReceiver(mActionMDMReceiver);
		super.onDestroy();
	};
	
	
	BroadcastReceiver mActionMDMReceiver = new BroadcastReceiver() {		
		@Override
		public void onReceive(Context context, Intent intent) {

			if(intent.getBooleanExtra(MDM_RECIEVED_ENABLED,false))
			{
				Toast.makeText(context, "enable", Toast.LENGTH_SHORT).show();
				changeAdminButtonStatus(true);
			}
			else if(intent.getBooleanExtra(MDM_RECIEVED_DIABLED,false))
			{
				Toast.makeText(context, "disable", Toast.LENGTH_SHORT).show();
				changeAdminButtonStatus(false);
				changeCameraButtonStatus(false);
			}
		}
	};
	
	@Override
	protected void onResume() {
		mAdminActive = isActiveAdmin();

		mCameraEnable = mDPM.getCameraDisabled(mDeviceAdminSample);

        changeCameraButtonStatus(mCameraEnable);
        changeAdminButtonStatus(mAdminActive);
    
		super.onResume();
	}
	
    /**
     * Helper to determine if we are an active admin
     */
    private boolean isActiveAdmin() {
        return mDPM.isAdminActive(mDeviceAdminSample);
    }

    public void onAdminButtonClick(View view)
    {
    	mAdminActive = isActiveAdmin();
    	if(mAdminActive)
    	{
    		mDPM.removeActiveAdmin(mDeviceAdminSample);
    	}else{
            if(checkPlatformSigned())
            {
        		// from AOSP\packages\apps\Settings\src\com\android\settings\DeviceAdminAdd.java
        		// used permission android.permission.MANAGE_DEVICE_ADMINS
        		// with platform signing key    		
            	
        		// with compile framework_intermediates.jar                	
        		// @hidden api
            	mDPM.setActiveAdmin(mDeviceAdminSample, true);
            }else{
        		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdminSample);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        getString(R.string.add_admin_extra_app_text));
                startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);            	
            }
    	}
    }
    
	public void onCameraButtonClick(View view)
	{
		if(isActiveAdmin())
		{
			mCameraEnable = !mDPM.getCameraDisabled(mDeviceAdminSample);
			mDPM.setCameraDisabled(mDeviceAdminSample, mCameraEnable);
			
			//mCameraEnable = !mCameraEnable;			
			changeCameraButtonStatus(mCameraEnable);
		}
	}
	
	public void onCameraOpenClick(View view)
	{
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            // Ignore exception
        }
	}
	
	void changeCameraButtonStatus(boolean b)
	{
		int str_id = R.string.button_camera_enable;
		
		if(b)
			str_id = R.string.button_camera_disable;
		
		mCameraButton.setText(getResources().getString(str_id));
	}
	
	void changeAdminButtonStatus(boolean b)
	{
		int str_id = R.string.button_admin_disable;
		
		if(b)
			str_id = R.string.button_admin_enable;
		
		mAdminButton.setText(getResources().getString(str_id));
	}

	@Deprecated
	private boolean checkPlatformSigned()
	{
		int settingSigCode=0, currentSigCode=0;
		Signature[] settingSigs = null;
		try {
			settingSigs = getPackageManager().getPackageInfo("com.android.settings", PackageManager.GET_SIGNATURES).signatures;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		for (Signature sig : settingSigs)
		{
			//Log.i("MyApp", "Signature hashcode : " + sig.hashCode());
			settingSigCode = sig.hashCode();
			break;
		}
		
		Signature[] sigs = null;
		try {
			sigs = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES).signatures;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		for (Signature sig : sigs)
		{
		   //Log.i("MyApp", "Signature hashcode : " + sig.hashCode());
			currentSigCode = sig.hashCode();
			break;
		}
		
		if((0 == currentSigCode) || (0 == settingSigCode))
			return false;
		
		if(currentSigCode == settingSigCode)
			return true;
			
		return false;
	}
}
