package com.uniclau.simplefile;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.uniclau.network.URLNetRequester;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Base64;

import android.util.Log;


public class SimpleFilePlugin extends CordovaPlugin {

       private final String TAG="SimpleFilePlugin";
	
	   @Override
	    public void onPause(boolean multitasking) {
	        Log.d(TAG, "onPause");
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

		private String getRootPath(Context ctx, String type) {
			if ("external".equals(type)) {
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					return ctx.getExternalFilesDir(null).getAbsolutePath();
	            } else {
	            	String packageName = ctx.getPackageName();
	            	return "/data/data/" + packageName;
	            }
			} else {
				return ctx.getFilesDir().getAbsolutePath();				
			}
		}
	    
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		try {
			final Context ctx= this.cordova.getActivity();
			if ("read".equals(action)) {
				String root = args.getString(0);
				String fileName = args.getString(1);
				byte[] buff;
				if ("bundle".equals(root)) {
					AssetManager assets = ctx.getAssets();
					InputStream is = assets.open(fileName);
					ByteArrayOutputStream outstream = new ByteArrayOutputStream();
					int bytesRead;
					byte[] buf = new byte[4 * 1024]; // 4K buffer
					while ((bytesRead = is.read(buf)) != -1) {
						outstream.write(buf,0,bytesRead);
					}
					buff = outstream.toByteArray(); 
					
				} else {
					String rootPath = getRootPath(ctx,root);
				
				    File f= new File(rootPath + "/" + fileName);
					if (!f.exists()) {
					    Log.d(TAG, "The file does not exist:" + fileName);
					    callbackContext.error("File does not exist");
					    return false;
					}
					FileInputStream is = new FileInputStream(rootPath + "/" +fileName);
					buff = new byte[(int)f.length()];
					is.read(buff);
					is.close();
				}
				String data64 = Base64.encodeToString(buff,  Base64.DEFAULT | Base64.NO_WRAP);
				
				callbackContext.success(data64);
			    return true; 		
			}
			if ("write".equals(action)) {			
				String root = args.getString(0);
				if ("bundle".equals(root)) {
				    callbackContext.error("Bundle is readonly");
				    return false;
				}
				String rootPath = getRootPath(ctx,root);
				String fileName = args.getString(1);
				String data64 = args.getString(2);
				byte []data = Base64.decode(data64, Base64.DEFAULT);
				  
				  
				File f= new File(rootPath + "/" +fileName);
				if (f.exists()) {
					f.delete();
				}
				
				File dir = f.getParentFile();
				dir.mkdirs();
								
				FileOutputStream fstream;
				fstream = new FileOutputStream(rootPath + "/" +fileName);
				fstream.write(data);
				fstream.flush();
				fstream.close();
				
			         
				callbackContext.success();
			    return true; 		
			}
			if ("remove".equals(action)) {
				String root = args.getString(0);
				if ("bundle".equals(root)) {
				    callbackContext.error("Bundle is readonly");
				    return false;
				}
				String rootPath = getRootPath(ctx,root);
				String fileName = args.getString(1);
				File f= new File(rootPath + "/" + fileName);
				if (f.exists()) {
					if (f.isDirectory())
						DeleteRecursive(f);
					else
						f.delete();
				}
				callbackContext.success();
			    return true;	
			}
			if ("download".equals(action)) {
				String root = args.getString(0);
				if ("bundle".equals(root)) {
				    callbackContext.error("Bundle is readonly");
				    return false;
				}

				final String rootPath = getRootPath(ctx,root);
				String url = args.getString(1);
				final String fileName = args.getString(2);
				
				final CallbackContext cb = callbackContext;
				
				URLNetRequester.NewRequest("", url, url, new URLNetRequester.AnswerHandler() {
					
					@Override
					public void OnAnswer(Object CallbackParam, byte[] Res) {
						if (Res == null) {
							cb.error("Network Error");
						}
						
						try {
							File f= new File(rootPath + "/" +fileName);
							if (f.exists()) {
								f.delete();
							}

							File dir = f.getParentFile();
							dir.mkdirs();							
							
							FileOutputStream fstream;
							fstream = new FileOutputStream(rootPath + "/" +fileName);
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
			if ("getUrl".equals(action)) {
				String root = args.getString(0);
				String fileName = args.getString(1);
				String res;
				if ("bundle".equals(root)) {
					res = "file:///android_asset/" + fileName;
				} else {
					String rootPath = getRootPath(ctx,root);				
					res = "file://" + rootPath + "/" + fileName;
				}
				callbackContext.success(res);
			    return true;
				
			}
			if ("createFolder".equals(action)) {
				String root = args.getString(0);
				if ("bundle".equals(root)) {
				    callbackContext.error("Bundle is readonly");
				    return false;
				}
				String rootPath = getRootPath(ctx,root);				
				String dirName = args.getString(1);
				File dir = new File(rootPath + "/" + dirName);
				dir.mkdirs();
				callbackContext.success();
			    return true; 		
			}
			if ("list".equals(action)) {
				String root = args.getString(0);
				String dirName = args.getString(1);
				if ("bundle".equals(root)) {
					if (".".equals(dirName)) dirName="";
					String [] files =ctx.getAssets().list(dirName);
					
					JSONArray res = new JSONArray();
					
					int i;
					for (i=0; i<files.length; i++) {
						JSONObject fileObject = new JSONObject();
						fileObject.put("name", files[i]);
						fileObject.put("isFolder", true);
						try {
							String [] subFolders = ctx.getAssets().list("".equals(dirName) ? files[i] : dirName + "/" +files[i]);
							if (subFolders.length == 0) {
								fileObject.put("isFolder", false);							
							}
						} catch(Exception e) {
							fileObject.put("isFolder", false);							
						}
						res.put(fileObject);
					}
					callbackContext.success(res);
				    return true;
					
					
				} else {

					String rootPath = getRootPath(ctx,root);				
					File dir;
					if ("".equals(dirName) || ".".equals(dirName)) {
						dir = new File(rootPath);					
					} else {
						dir = new File(rootPath + "/" + dirName);
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
			}
			return false;		
		} catch(Exception e) {
		    System.err.println("Exception: " + e.getMessage());
		    callbackContext.error(e.getMessage());
		    return false;
		} 
	}
}
