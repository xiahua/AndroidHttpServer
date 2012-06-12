package com.nevin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class DownloadFileAsync extends AsyncTask<String, String, String> {
	private final static String TAG = "DownloadFileAsync";
	File mSavedDir = Environment.getExternalStorageDirectory();
	private String mDownloadUrl = null;
	private String mFileName = null;

	private OnDownloadStartedListener mOnDownloadStartedListener = null;
	private OnProgressUpdateListener mOnProgressUpdateListener = null;
	private OnDownloadFinishedListener mOnDownloadFinishedListener = null;

	public interface OnDownloadStartedListener{
		void onDownloadStarted();
	}
	public interface OnProgressUpdateListener{
		void onProgressUpdate(int progress);
	}
	public interface OnDownloadFinishedListener{
		void onDownloadFinished(String fileName, String downloadUrl);
	}

	public void setOnDownloadStartedListener(OnDownloadStartedListener l){
		mOnDownloadStartedListener = l;
	}

	public void setOnProgressUpdateListener(OnProgressUpdateListener l){
		mOnProgressUpdateListener = l;

	}

	public void setOnDownloadFinishedListener(OnDownloadFinishedListener l){
		mOnDownloadFinishedListener = l;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if(mOnDownloadStartedListener != null){
			mOnDownloadStartedListener.onDownloadStarted();
		}
	}

	@Override
	protected String doInBackground(String... params) {
		try {
			//connecting to url
			
			String mFileName = params[0];
			String mDownloadUrl = params[1];
			
			Log.d(TAG,"do in Background ...."+mDownloadUrl);
			URL u = new URL(mDownloadUrl);
			HttpURLConnection c = (HttpURLConnection) u.openConnection();
			c.setRequestMethod("GET");
			c.setDoOutput(true);
			c.connect();

			//lenghtOfFile is used for calculating download progress
			int lenghtOfFile = c.getContentLength();

			//this is where the file will be seen after the download
			String filePath = checkFilePath(mSavedDir+"/qvod_shared",mFileName);
			FileOutputStream f = new FileOutputStream(new File(filePath));
			//file input is from the url
			InputStream in = c.getInputStream();

			//here's the download code
			byte[] buffer = new byte[1024];
			int len1 = 0;
			long total = 0;

			while ((len1 = in.read(buffer)) > 0) {
				total += len1; //total = total + len1
				publishProgress("" + (int)((total*100)/lenghtOfFile));
				f.write(buffer, 0, len1);
			}
			f.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(String... progress) {
		if(mOnProgressUpdateListener != null){
			mOnProgressUpdateListener.onProgressUpdate(Integer.parseInt(progress[0]));
		}
	}

	@Override
	protected void onPostExecute(String unused) {
		//dismiss the dialog after the file was downloaded
		if(mOnDownloadFinishedListener != null){
			mOnDownloadFinishedListener.onDownloadFinished(mFileName, mDownloadUrl);
		}
	}

	public String checkFilePath(String fileDir, String fileName){
		File file = new File(fileDir);
		if( !file.exists() ){
			file.mkdirs();
		}
		file = new File(fileDir + "/" +fileName);
		if(file.exists() ){
			fileName = fileName+"(1)";
		}
		return fileDir+"/"+fileName;
	}
	
}

