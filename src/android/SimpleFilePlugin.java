package com.uniclau.simplefile;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.util.Base64;

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
			Context ctx= this.cordova.getActivity();
			if ("getFile".equals(action)) {
				String fileName = args.getString(0);
				
			    File f= ctx.getFileStreamPath(fileName);
				if (!f.exists()) {
				    Log.d(TAG, "File does not exist:" + fileName);
				    callbackContext.error("File does not exist");
				    return false;
				}
				FileInputStream is = ctx.openFileInput(fileName); //$NON-NLS-1$
				byte buff[] = new byte[(int)f.length()];
				is.read(buff);
				String data64 = Base64.encodeToString(buff,  Base64.DEFAULT);
				is.close();
				
				callbackContext.success(data64);
			    return true; 		
			}
			if ("setFile".equals(action)) {			
				String fileName = args.getString(0);
				String data64 = args.getString(1);
				byte []data = Base64.decode(data64, Base64.DEFAULT);
				  
				  
				File f= ctx.getFileStreamPath(fileName);
				if (f.exists()) {
					f.delete();
				}
				
				FileOutputStream fstream;
				fstream = ctx.openFileOutput(fileName, Context.MODE_PRIVATE);
				fstream.write(data);
				fstream.flush();
				fstream.close();
			         
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
				
				String fileName = args.getString(0);
				String res = "file://" + ctx.getFilesDir() + "/" + fileName;
				
				callbackContext.success(res);
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
