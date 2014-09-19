package com.uniclau.network;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import org.apache.http.impl.client.DefaultHttpClient;


import android.os.Handler;
import android.util.Log;


public class URLNetRequester extends Thread {
	final static public int ERR_CANCELLED=2001;
	final static public int ERR_PARSE=2002;

	public interface AnswerHandler {
		public abstract void OnAnswer(Object CallbackParam, byte[] Res);
	};
	public static void NewRequest(Object CallbackParam, String aUrl, String aCancelString, AnswerHandler aAnswerHandler) {
		URLNetRequester requester = new URLNetRequester(CallbackParam,aUrl,aCancelString,aAnswerHandler);
		requester.addToPool();
		requester.start();
	}
	
	
	public static void CnacelRequest(String CancelString) {
		Log.d("URLNetRequester", "Individual Cancel");
		Iterator<URLNetRequester> iter=requests.iterator();
		while (iter.hasNext()) {
			URLNetRequester R=iter.next();
			if (R.cancelString == CancelString) {
				R.response(false);
			}
		}
	}
	
	public static void CancelAll() {
		Log.d("URLNetRequester", "Cancel All");
		Iterator<URLNetRequester> iter=requests.iterator();
		while (iter.hasNext()) {
			URLNetRequester R=iter.next();
			R.response(false);
		}		
	}
	
	
//////////////
	static private List<URLNetRequester> requests = new ArrayList<URLNetRequester>();
	
	private String url;
	private String cancelString;
	private AnswerHandler answerHandler;
	private Boolean responseSended;
    private Handler mHandler = new Handler();
    private byte [] Res;
    private Object callbackParam;
    
    private synchronized void addToPool() {
    	requests.add(this);
    }
    
    private synchronized void removeFromPool() {
    	requests.remove(this);
    }
	
	private synchronized void response(boolean isok) {
		if (!responseSended) {
			responseSended=true;	
			if (isok) {
		        mHandler.post(new Runnable() {
	                public void run() {
	                	answerHandler.OnAnswer(callbackParam,Res);
	                }
	            });
			} else {
		        mHandler.post(new Runnable() {
		                public void run() {
		                	answerHandler.OnAnswer(callbackParam,null);
		                }
		            });
			}
		}
	}
	
	private synchronized Boolean isResponseSended() {
		return responseSended;
	}
	
	private URLNetRequester(Object aCallbackParam,String aUrl, String aCancelString, AnswerHandler aAnswerHandler) {
		if ((aUrl==null)||(aUrl.equals(""))) {
			Log.i("ERROR","ERROR");
		}
		url=aUrl;
		callbackParam=aCallbackParam;
		cancelString=aCancelString;
		answerHandler=aAnswerHandler;
		responseSended=false;
	}
	
	public void run() {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);
		try {
	
			HttpResponse response = httpclient.execute(httpget);		
			
		    byte[] buf = new byte[4 * 1024]; // 4K buffer
		    int bytesRead;    
		    ByteArrayOutputStream r = new ByteArrayOutputStream();	
				
			HttpEntity entity = response.getEntity();
		    if (entity != null) {
		        InputStream instream = entity.getContent();
			    while ((bytesRead = instream.read(buf)) != -1) {
		            if (isResponseSended())
		            	httpget.abort();
		            // Process the file
		     	    r.write(buf, 0, bytesRead);
		        }
			    Res=r.toByteArray();
			    response(true);
		     } else {
				Log.d("URLNetRequester", "Null entity");
				response(false);
		     }
		     removeFromPool();
		} catch (Exception e) {
			Log.d("URLNetRequester", e.getMessage());
			response(false);
			removeFromPool();
			return;
		}
	}
}
