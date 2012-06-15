package com.nevin.downloader;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import com.nevin.downloader.DownlaodStateListener.*;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class DownloadFileAsync extends AsyncTask<String, String, String> {
	private final static String TAG = "Downloader";
	private String SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
	private String MYDOWNLOAD = "MyDownload";
	private String mDownloadUrl = null;
	private String mFileName = null;
	private String mFilePath;
	private boolean mDownloading = true;
	private DownloadDB mDownLoadDB;
	private int mCompleteSize =0;
	
	private OnDownloadStartedListener mOnDownloadStartedListener = null;
	private OnProgressUpdateListener mOnProgressUpdateListener = null;
	private OnDownloadFinishedListener mOnDownloadFinishedListener = null;

	public DownloadFileAsync(Context context,String fileName, String downloadUrl){
		mFileName = fileName;
		File file = new File(SDCARD+"/"+MYDOWNLOAD);
		if( !file.exists() ){
			file.mkdirs();
		}
		mFilePath = SDCARD+"/"+MYDOWNLOAD+"/"+mFileName;
		mDownloadUrl = downloadUrl;
		mDownLoadDB = new DownloadDB(context);
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
			mOnDownloadStartedListener.onDownloadStarted(mFileName, mDownloadUrl,0);
		}
	}
		
	public void stopDownload(){
		mDownloading = false;
		mDownLoadDB.updataInfo(mDownloadUrl, mCompleteSize, getFileMd5String(new File(mFilePath)));
		mDownLoadDB.closeDb();
	}

	@Override
	protected String doInBackground(String... params) {
		
		DownloadInfo info = getDownloadInfo(mDownloadUrl);
		if(info==null){
			Log.e(TAG,"error, downloadInfo is null !");
			return null;
		}
	    int totalSize = info.getFileSize();
	    mCompleteSize = info.getCompeleteSize();
	    mFilePath = info.getFilePath();
	    publishProgress(mFileName, mDownloadUrl, "" + (int)((mCompleteSize*100)/totalSize));
		HttpURLConnection c = null;
        RandomAccessFile randomAccessFile = null;
        InputStream is = null;
		try {
			//connecting to url
			Log.e(TAG,"do in Background ...."+mDownloadUrl);
			URL u = new URL(mDownloadUrl);
			c = (HttpURLConnection) u.openConnection();
			c.setRequestMethod("GET");
			c.setRequestProperty("Range", "bytes="+info.getCompeleteSize() + "-" + info.getFileSize());
			c.setDoOutput(true);
			c.connect();

	        randomAccessFile = new RandomAccessFile(mFilePath, "rwd");
            randomAccessFile.seek(info.getCompeleteSize());
            // 将要下载的文件写到保存在保存路径下的文件中
            is = c.getInputStream();
            byte[] buffer = new byte[4098];
            int length = -1;
            
            while ((length = is.read(buffer)) != -1 && mDownloading) {
                randomAccessFile.write(buffer, 0, length);
                mCompleteSize += length;
                publishProgress(mFileName, mDownloadUrl, ""+(int)((mCompleteSize*100)/totalSize));
                // 更新数据库中的下载信息
                if(mCompleteSize>=totalSize){
                	delete(mDownloadUrl);
                }
            }

		} catch (Exception e) {
			e.printStackTrace();
		}finally {
            try {
            	if(is!=null){
            		is.close();
            	}
                if(randomAccessFile != null){
                	randomAccessFile.close();
                }if(c != null){
                	c.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
		return null;
	}

	@Override
	protected void onProgressUpdate(String... progress) {
		if(mOnProgressUpdateListener != null){
			mOnProgressUpdateListener.onProgressUpdate(progress[0],progress[1],Integer.parseInt(progress[2]));
		}
	}

	@Override
	protected void onPostExecute(String unused) {
		//dismiss the dialog after the file was downloaded
		if(mOnDownloadFinishedListener != null){
			mOnDownloadFinishedListener.onDownloadFinished(mFileName, mDownloadUrl);
		}
	}

	//删除数据库中urlstr对应的下载器信息
	public void delete(String url) {
		mDownLoadDB.delete(url);
	}
		
	private String checkFileName(String fileDir, String fileName){
		File file = new File(fileDir+"/"+fileName);
		String newFileName = fileName;
		if(file.exists()){
			for(int i=1; i<1024; i++){
				newFileName = String.format("%s(%d)",fileName,i);
				file = new File(fileDir+"/"+newFileName);
				if(!file.exists()){
					return newFileName;
				}
			}
		}
		return newFileName;
	}
	
	private DownloadInfo getDownloadInfo(String url){
		DownloadInfo info=null;
		if(isFirstDownlaod(url)){
			info = firstDownloadInit();
		}else{
			Log.e(TAG,"has download before...");
			info = mDownLoadDB.getDownloadInfo(url);
			if(info!=null){
				Log.e(TAG,"compeleted: "+info.getCompeleteSize());
			}
		}
		return info;
	}
    /**
     * 判断是否是第一次 下载
     */
    private boolean isFirstDownlaod(String downloadUrl) {
        return mDownLoadDB.hasInfo(downloadUrl);
    }
    
    /**
     * 初始化
     */
    private DownloadInfo firstDownloadInit() {
    	Log.e(TAG, "firstDownloadInit.....");
    	DownloadInfo downloadInfo=null;
        try {
            URL url = new URL(mDownloadUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("GET");
            int fileSize = connection.getContentLength();
            mFileName = checkFileName(SDCARD+"/"+MYDOWNLOAD,mFileName);
            mFilePath = SDCARD+"/"+MYDOWNLOAD+"/"+mFileName;
            File file = new File(mFilePath);
            if (!file.exists()) {
                file.createNewFile();
            }else{
            	
            }
            // 本地访问文件
            RandomAccessFile accessFile = new RandomAccessFile(file, "rwd");
            accessFile.setLength(fileSize);
            accessFile.close();
            downloadInfo = new DownloadInfo(mDownloadUrl,fileSize,0,mFilePath,getFileMd5String(file));
            mDownLoadDB.saveDwonloadInfo(downloadInfo);
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return downloadInfo;
    }
    
    private String getFileMd5String(File file ){
    	return "";
    	/*
    	try{
        	MessageDigest messageDigest = MessageDigest.getInstance("MD5"); 
        	FileInputStream in = new FileInputStream(file); 
        	FileChannel ch = in.getChannel(); 
        	MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, file.length()); 
        	messageDigest.update(byteBuffer); 	
        	return new String(messageDigest.digest());
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	return "";
    	*/
    }
}

