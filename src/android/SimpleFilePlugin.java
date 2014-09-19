package com.uniclau.simplefile;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.uniclau.network.URLNetRequester;

import android.content.Context;
import android.util.Base64;

import android.util.Log;


public class SimpleFilePlugin extends CordovaPlugin {

       private final String TAG="SimpleFilePlugin";
	
	   @Override
	    public void onPause(boolean multitasking) {
	        //Log.d(TAG, "onPause");
	        //URLNetRequester.CancelAll();
	        super.onPause(multitasking);
	    }

	    @Override
	    public void onResume(boolean multitasking) {
	        Log.d(TAG, "onResume " );
	        super.onResume(multitasking);
	    }
	    
		private void DeleteRecursive(File fileOrDirectory) {
		    if (fileOrDirectory.isDirectory())
		        for (File child : fileOrDirectory.listFiles())
		            DeleteRecursive(child);

		    fileOrDirectory.delete();
		}
	    
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		try {
			final Context ctx= this.cordova.getActivity();
			if ("read".equals(action)) {
				String fileName = args.getString(0);
				
			    File f= new File(ctx.getFilesDir().getAbsolutePath() + "/" + fileName);
				if (!f.exists()) {
				    Log.d(TAG, "The file does not exist:" + fileName);
				    callbackContext.error("File does not exist");
				    return false;
				}
				FileInputStream is = new FileInputStream(ctx.getFilesDir().getAbsolutePath() + "/" +fileName);
				byte buff[] = new byte[(int)f.length()];
				is.read(buff);
				String data64 = Base64.encodeToString(buff,  Base64.DEFAULT | Base64.NO_WRAP);
				is.close();
				
				callbackContext.success(data64);
			    return true; 		
			}
			if ("write".equals(action)) {			
				String fileName = args.getString(0);
				String data64 = args.getString(1);
				byte []data = Base64.decode(data64, Base64.DEFAULT);
				  
				  
				File f= new File(ctx.getFilesDir().getAbsolutePath() + "/" +fileName);
				if (f.exists()) {
					f.delete();
				}
				
				File dir = f.getParentFile();
				dir.mkdirs();
								
				FileOutputStream fstream;
				fstream = new FileOutputStream(ctx.getFilesDir().getAbsolutePath() + "/" +fileName);
				fstream.write(data);
				fstream.flush();
				fstream.close();
				
			         
				callbackContext.success();
			    return true; 		
			}
			if ("remove".equals(action)) {
				String fileName = args.getString(0);
				File f= new File(ctx.getFilesDir().getAbsolutePath() + "/" + fileName);
				if (f.exists()) {
					if (dir.isDirectory())
						DeleteRecursive(f);
					else
						f.delete();
				}
				callbackContext.success();
			    return true;	
			}
			if ("download".equals(action)) {
				String url = args.getString(0);
				final String fileName = args.getString(1);
				
				final CallbackContext cb = callbackContext;
				
				URLNetRequester.NewRequest("", url, url, new URLNetRequester.AnswerHandler() {
					
					@Override
					public void OnAnswer(Object CallbackParam, byte[] Res) {
						if (Res == null) {
							cb.error("Network Error");
						}
						
						try {
							File f= new File(ctx.getFilesDir().getAbsolutePath() + "/" +fileName);
							if (f.exists()) {
								f.delete();
							}

							File dir = f.getParentFile();
							dir.mkdirs();							
							
							FileOutputStream fstream;
							fstream = new FileOutputStream(ctx.getFilesDir().getAbsolutePath() + "/" +fileName);
							fstream.write(Res);
							fstream.flush();
							fstream.close();
						         
							cb.success();
						} catch(Exception e) {
						    cb.error(e.getMessage());
						}
					}
				});
			    return true; 		
			}
			if ("getURL".equals(action)) {
				
				String fileName = args.getString(0);
				String res = "file://" + ctx.getFilesDir() + "/" + fileName;
				
				callbackContext.success(res);
			    return true; 		
			}
			if ("create".equals(action)) {
				String dirName = args.getString(0);
				File dir = new File(ctx.getFilesDir().getAbsolutePath() + "/" + dirName);
				dir.mkdirs();
				callbackContext.success();
			    return true; 		
			}
			if ("list".equals(action)) {
				String dirName = args.getString(0);
				File dir;
				if ("".equals(dirName) || ".".equals(dirName)) {
					dir = new File(ctx.getFilesDir().getAbsolutePath());					
				} else {
					dir = new File(ctx.getFilesDir().getAbsolutePath() + "/" + dirName);
				}
					
				if (!dir.exists()) {
				    Log.d(TAG, "The folder does not exist:" + dirName);
				    callbackContext.error("The file does not exist");
				    return false;	
				}
				
				if (!dir.isDirectory()) {
				    Log.d(TAG, dirName + " is not a directory");
				    callbackContext.error(dirName + " is not a directory");
				    return false;						
				}
				
				JSONArray res = new JSONArray();
				
				File []childs =dir.listFiles();
				int i;
				for (i=0; i<childs.length; i++) {
					JSONObject fileObject = new JSONObject();
					fileObject.put("name", childs[i].getName());
					fileObject.put("isFolder", childs[i].isDirectory());
					res.put(fileObject);
				}
				callbackContext.success(res);
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
