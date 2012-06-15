package com.nevin.downloader;

public class DownlaodStateListener {
	public interface OnDownloadStartedListener{
		void onDownloadStarted(String fileName, String downloadUrl,int startProgress);
	}
	public interface OnProgressUpdateListener{
		void onProgressUpdate(String fileName, String downloadUrl,int progress);
	}
	public interface OnDownloadFinishedListener{
		void onDownloadFinished(String fileName, String downloadUrl);
	}
}
