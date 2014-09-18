package com.uniclau.alarmplugin;

import java.text.SimpleDateFormat;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.Date;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;


public class SimpleFilePlugin extends CordovaPlugin {

       private final String TAG="SimpleFilePlugin";
	
	   @Override
	    public void onPause(boolean multitasking) {
	        Log.d(TAG, "onPause");
	        super.onPause(multitasking);
	    }

	    @Override
	    public void onResume(boolean multitasking) {
	        Log.d(TAG, "onResume " );
	        super.onResume(multitasking);
	    }
	    
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		try {
			if ("getFile".equals(action)) {
				callbackContext.success();
			    return true; 		
			}
			if ("setFile".equals(action)) {
				callbackContext.success();
			    return true; 		
			}
			if ("deleteFile".equals(action)) {
				callbackContext.success();
			    return true; 		
			}
			if ("downloadFile".equals(action)) {
				callbackContext.success();
			    return true; 		
			}
			if ("getUrlFile".equals(action)) {
				callbackContext.success();
			    return true; 		
			}
			if ("ccreateDirectory".equals(action)) {
				callbackContext.success();
			    return true; 		
			}
			if ("deleteDirectory".equals(action)) {
				callbackContext.success();
			    return true; 		
			}
			return false;		
		} catch(Exception e) {
		    System.err.println("Exception: " + e.getMessage());
		    callbackContext.error(e.getMessage());
		    return false;
		} 
	}
}
