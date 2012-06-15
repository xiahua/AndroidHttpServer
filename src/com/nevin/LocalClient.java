package com.nevin;

import android.webkit.WebChromeClient;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.nevin.NanoHTTPD;
import com.nevin.downloader.DownlaodStateListener.*;
import com.nevin.downloader.*;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;
import android.webkit.WebView;
import android.widget.Button;
import android.util.Log;
import android.webkit.WebViewClient;

public class LocalClient extends Activity {
	private final static String TAG = "Local";
	
	private ProgressDialog mProgressDialog;
	public static final int DIALOG_DOWNLOAD_PROGRESS = 0;

	WebView webview;
	NanoHTTPD httpServer;
	TextView tv;

	Button butClose;

	/* To ensure we don't open a new window each click. */
	public class LocalWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			char last = '\0';
			if( url != null && url.length() > 0 ) last = url.charAt( url.length() - 1 ) ;
			if (!(last == '/')) {
				Log.d(TAG,"url: "+url);
				String decodeUrl = decodeUri(url);
				String fileName = decodeUrl.substring(decodeUrl.lastIndexOf("/")+1);
				startDownload(fileName,url);
				return(true);
			}
			else {
				view.loadUrl(url);
				return(false);
			}
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		webview = (WebView) findViewById(R.id.webview);
		webview.setVisibility(android.view.View.INVISIBLE);

		tv = (TextView) this.findViewById(R.id.textview);
		tv.setText("Ready!");
	}
	
	
	@Override
	protected void onStart(){
		super.onStart();
		try {
			httpServer = new NanoHTTPD(8080,Environment.getExternalStorageDirectory());
			webview = (WebView) findViewById(R.id.webview);
            webview.setWebViewClient(new LocalWebViewClient());
            
            webview.getSettings().setJavaScriptEnabled(true);
            webview.loadUrl("http://localhost:8080/");
            webview.setVisibility(android.view.View.VISIBLE);
		}
		catch(Exception e) {
			tv.setText(e.toString());
		}
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		httpServer.stop();
	}

	
	private void startDownload(final String fileName, final String downloadUrl){
		Log.d(TAG,"startDownload...: "+fileName);
		Log.d(TAG,"startDownload...: "+downloadUrl);
		
		final DownloadFileAsync downloader = new DownloadFileAsync(this,fileName,downloadUrl);
		downloader.setOnDownloadStartedListener(new OnDownloadStartedListener() {
			@Override
			public void onDownloadStarted(String fileName, String downloadUrl,int startProgress) {
				showDialog(DIALOG_DOWNLOAD_PROGRESS);
				mProgressDialog.setMessage("downloading "+fileName);
				mProgressDialog.setProgress(startProgress);
				mProgressDialog.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						// TODO Auto-generated method stub
						downloader.stopDownload();
					}
				});
			}
		});
		downloader.setOnProgressUpdateListener(new OnProgressUpdateListener() {
			@Override
			public void onProgressUpdate(String fileName, String downloadUrl,int progress) {
				mProgressDialog.setMessage("downloading "+fileName);
				mProgressDialog.setProgress(progress);
			}
		});
		downloader.setOnDownloadFinishedListener(new OnDownloadFinishedListener() {
			@Override
			public void onDownloadFinished(String fileName, String downloadUrl) {
				dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
			}
		});
		
		downloader.execute(fileName,downloadUrl);
	}
	
	//our progress bar settings
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS: //we set this to 0
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Downloading file...");
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setMax(100);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(true);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }
    
	private String decodeUri(String uri){
		String newUri="";
		try{
			newUri = URLDecoder.decode(uri, "utf-8");	
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}
		return newUri;
	}
}
