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
		if (fileOrDirectory.isDirectory()) {
			for (File child : fileOrDirectory.listFiles()) {
				DeleteRecursive(child);
			}
		}

		fileOrDirectory.delete();
	}

	private String getRootPath(Context ctx, String type) {
		if ("external".equals(type)) {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				return Environment.getExternalStorageDirectory().getAbsolutePath();
			} else {
				String packageName = ctx.getPackageName();
				return "/data/data/" + packageName;
			}
		} else if ("internal".equals(type)) {
			return ctx.getFilesDir().getAbsolutePath();				
		} else if ("user".equals(type)) {
			return ctx.getFilesDir().getAbsolutePath();		
		} else if ("cache".equals(type)) {
			return ctx.getCacheDir().getAbsolutePath();				
		} else if ("tmp".equals(type)) {
			return ctx.getCacheDir().getAbsolutePath();				
		} else {
			return "";
		}
	}
	
	private byte[] readFile(Context ctx, String root, String fileName) throws Exception {
		Log.d(TAG, "start Read: " + root + "/" + fileName );
		byte[] buff;
		if ("bundle".equals(root)) {
			AssetManager assets = ctx.getAssets();
			InputStream is = assets.open(fileName);
			ByteArrayOutputStream outstream = new ByteArrayOutputStream();
			int bytesRead;
			byte[] buf = new byte[4 * 1024]; // 4K buffer
			
			while ((bytesRead = is.read(buf)) != -1) {
				outstream.write(buf, 0, bytesRead);
			}
			buff = outstream.toByteArray();
		}
		else {
			String rootPath = getRootPath(ctx, root);

			File f = new File(rootPath + "/" + fileName);
			if (!f.exists()) {
				Log.d(TAG, "The file does not exist: " + fileName);
				throw new Exception("The file does not exist: " + fileName);
			}
			FileInputStream is = new FileInputStream(rootPath + "/" +fileName);
			buff = new byte[(int)f.length()];
			is.read(buff);
			is.close();
		}
		Log.d(TAG, "end Read: " + root + "/" + fileName );
		return buff;
	}

	private boolean readFile(final Context ctx, final JSONArray params, final CallbackContext callbackContext) throws Exception {

		cordova.getActivity().runOnUiThread(new Runnable() {
			public void run() {
				try {
					String root = params.getString(0);
					String fileName = params.getString(1);
					byte [] buff = readFile(ctx, root, fileName);
					String data64 = Base64.encodeToString(buff,  Base64.DEFAULT | Base64.NO_WRAP);
		            
					callbackContext.success(data64);
				}
				catch(Exception e) {
					callbackContext.error(e.getMessage());
					return;
				}
			}
		});
		return true; 	
	}
	
	private void writeFile(Context ctx, String root, String fileName, byte [] data) throws Exception {
		Log.d(TAG, "start write: " + root + "/" + fileName );
		if ("bundle".equals(root)) {
			throw new Exception("The bundle file system is read only");
		}
		
		String rootPath = getRootPath(ctx, root);

		File f= new File(rootPath + "/" + fileName);
		if (f.exists()) {
			f.delete();
		}

		File dir = f.getParentFile();
		dir.mkdirs();

		FileOutputStream fstream;
		fstream = new FileOutputStream(rootPath + "/" + fileName);
		fstream.write(data);
		fstream.flush();
		fstream.close();
		Log.d(TAG, "end write: " + root + "/" + fileName );
	}

	private boolean writeFile(final Context ctx, final JSONArray params, final CallbackContext callbackContext) throws Exception {
		cordova.getActivity().runOnUiThread(new Runnable() {
			public void run() {
				try {
					String root = params.getString(0);
					String fileName = params.getString(1);
					String data64 = params.getString(2);
					byte [] data = Base64.decode(data64, Base64.DEFAULT);

					writeFile(ctx, root, fileName, data);
		            
					callbackContext.success();
				}
				catch(Exception e) {
					callbackContext.error(e.getMessage());
				}
			}
		});
		return true; 	
	}
	

	private boolean remove(final Context ctx, final JSONArray args, final CallbackContext callbackContext) throws Exception {
		cordova.getActivity().runOnUiThread(new Runnable() {
			public void run() {
				try {		
					String root = args.getString(0);
					if ("bundle".equals(root)) {
						throw new Exception("The bundle file system is read only");
					}
					String rootPath = getRootPath(ctx, root);
					String fileName = args.getString(1);
					File f= new File(rootPath + "/" + fileName);
					if (f.exists()) {
						DeleteRecursive(f);
					}
					callbackContext.success();
				}
				catch(Exception e) {
					callbackContext.error(e.getMessage());
				}
			}
		});
		return true; 	
	}

	private boolean download(final Context ctx, JSONArray params, final CallbackContext callbackContext) throws Exception {
		
		final JSONArray args = params;
		final String root = args.getString(0);

		if ("bundle".equals(root)) {
			callbackContext.error("The bundle file system is read only");
			return false;
		}
		
		String url;
		try {
			url = args.getString(1);
		}
		catch(Exception e) { return false;}

		URLNetRequester.NewRequest("", url, url, new URLNetRequester.AnswerHandler() {			
			@Override
			public void OnAnswer(Object CallbackParam, byte[] Res) {

				if (Res == null) {
					

					callbackContext.error("Network Error");
					return;
				}
				try {
					String rootPath = getRootPath(ctx, root);
					String fileName = args.getString(2);
					File f= new File(rootPath + "/" + fileName);
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
        
					callbackContext.success();
				} catch(Exception e) {
					callbackContext.error(e.getMessage());
				}
			}
		});
		return true; 	
	}

	private boolean getUrl(final Context ctx, JSONArray args, final CallbackContext callbackContext) throws Exception {
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
	
	private boolean createFolder(final Context ctx, final JSONArray params, final CallbackContext callbackContext) throws Exception {
		cordova.getActivity().runOnUiThread(new Runnable() {
			public void run() {
				try {
					String root = params.getString(0);
					if ("bundle".equals(root)) {
						throw new Exception("The bundle file system is read only");
					}
					String rootPath = getRootPath(ctx,root);				
					String dirName = params.getString(1);
					File dir = new File(rootPath + "/" + dirName);
					dir.mkdirs();
					callbackContext.success();
				} catch(Exception e) {
					Log.d(TAG, e.getMessage());
					callbackContext.error(e.getMessage());
				}
			};
		});
		return true;
	}

	
	private JSONArray list(Context ctx, String root, String dirName) throws Exception {
		Log.d(TAG, "start list: " + root + "/" + dirName );
		JSONArray res = new JSONArray();
		if ("bundle".equals(root)) {
			if (".".equals(dirName)) dirName = "";
			Log.d(TAG, "list  - 1");
			String [] files = ctx.getAssets().list(dirName);
			Log.d(TAG, "list  - 2");
			if (files.length == 0) {
				boolean isDirectory = true;
				try {
					// This function will raise an exception if it is a directory.
					Log.d(TAG, "list  - 3");
					ctx.getAssets().open(dirName);
					Log.d(TAG, "list  - 4");
					isDirectory = false;
				} catch (Exception e) {}
				if (!isDirectory) {
					Log.d(TAG, "List error: Not a directory: " + dirName);					
					throw new Exception(dirName + " is not a directory");
				}
			}
			
			int i;
			for (i=0; i<files.length; i++) {
				JSONObject fileObject = new JSONObject();
				fileObject.put("name", files[i]);
				fileObject.put("isFolder", true);
				try {
					Log.d(TAG, "list  - 2.1");
					String [] subFolders = ctx.getAssets().list("".equals(dirName) ? files[i] : dirName + "/" +files[i]);
					Log.d(TAG, "list  - 2.2");
					if (subFolders.length == 0) {
						fileObject.put("isFolder", false);							
					}
				} catch(Exception e) {
					fileObject.put("isFolder", false);							
				}
				res.put(fileObject);
			}
			
		} else {
			String rootPath = getRootPath(ctx,root);				
			File dir;
			if ("".equals(dirName) || ".".equals(dirName)) {
				dir = new File(rootPath);					
			} else {
				dir = new File(rootPath + "/" + dirName);
			}

					
			Log.d(TAG, "list  - 5");
			if (!dir.exists()) {
				Log.d(TAG, "The folder does not exist: " + dirName);
				throw new Error("The folder does not exist: " + dirName);
			}
			
			Log.d(TAG, "list  - 6");
			if (!dir.isDirectory()) {
				Log.d(TAG, dirName + " is not a directory");
				throw new Error(dirName + " is not a directory");
			}

		
			Log.d(TAG, "list  - 7");
			File []childs =dir.listFiles();
			int i;
			for (i=0; i<childs.length; i++) {
				JSONObject fileObject = new JSONObject();
				fileObject.put("name", childs[i].getName());
				fileObject.put("isFolder", childs[i].isDirectory());
				res.put(fileObject);
			}
		}
		Log.d(TAG, "end list: " + root + "/" + dirName );
		return res;
		
		
	}
	

	private boolean list(final Context ctx, final JSONArray params, final CallbackContext callbackContext) throws Exception {
		Log.d(TAG, "start list (main thread): " );
		cordova.getActivity().runOnUiThread(new Runnable() {
			public void run() {
				try {

					String root = params.getString(0);
					String dirName = params.getString(1);
					JSONArray res = list(ctx, root, dirName);
			            
					callbackContext.success(res);
					Log.d(TAG, "end list (main thread)");
				}
				catch(Exception e) {
					callbackContext.error(e.getMessage());
					Log.d(TAG, e.getMessage());
				}
			}
		});
		return true;
	}
	
	private void copy(Context ctx, String rootFrom, String fileFrom,String rootTo,String fileTo) throws Exception {
		Boolean isDir=false;
		JSONArray l = null;
		try {
			l=list(ctx, rootFrom, fileFrom);
			isDir = true;
		} catch (Exception E){};
		if (!isDir) {
			byte [] data = readFile(ctx, rootFrom, fileFrom);
			writeFile(ctx, rootTo,fileTo, data);
			return;
		}
		int i;
		for (i=0; i<l.length(); i++) {
			String childName = l.getJSONObject(i).getString("name");
			isDir = l.getJSONObject(i).getBoolean("isFolder");
			
			String newFrom = fileFrom;
			if (! "".equals(newFrom)) {
				newFrom +=  "/";
			}
			newFrom += childName;
			
			String newTo = fileTo;
			if (! "".equals(newTo)) {
				newTo +=  "/";
			}
			newTo += childName;
			
			if (isDir) {
				copy(ctx, rootFrom, newFrom, rootTo, newTo);
			} else {
				byte [] data = readFile(ctx, rootFrom, newFrom);
				writeFile(ctx, rootTo,newTo, data);
			}
		}
	}
	
	private boolean copy(final Context ctx, final JSONArray params, final CallbackContext callbackContext) throws Exception {
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				try {

					String rootFrom = params.getString(0);
					String fileFrom = params.getString(1);
					String rootTo = params.getString(2);
					String fileTo = params.getString(3);
					copy(ctx, rootFrom, fileFrom, rootTo, fileTo);
			            
					callbackContext.success();
				}
				catch(Exception e) {
					Log.d(TAG, "ERROR copy: " + e.getMessage());
					callbackContext.error(e.getMessage());
				}
			}
		});
		return true;
	}
	
	
	
	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
		try {
			final Context ctx= this.cordova.getActivity();
			if ("read".equals(action)) {
				return readFile(ctx, args, callbackContext);		
			}
			else if ("write".equals(action)) {				
				return writeFile(ctx, args, callbackContext);		
			}
			else if ("remove".equals(action)) {
				return remove(ctx, args, callbackContext);		
			}
			else if ("download".equals(action)) {	
				return download(ctx, args, callbackContext);		
			}
			else if ("getUrl".equals(action)) {
				return getUrl(ctx, args, callbackContext);		
			}
			else if ("createFolder".equals(action)) {
				return createFolder(ctx, args, callbackContext);		
			}
			else if ("list".equals(action)) {
				return list(ctx, args, callbackContext);
			}
			else if ("copy".equals(action)) {
					return copy(ctx, args, callbackContext);
			}
			return false;		
		} catch(Exception e) {
			System.err.println("Exception: " + e.getMessage());
			callbackContext.error(e.getMessage());
			return false;
		} 
	}
}
