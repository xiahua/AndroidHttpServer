package com.nevin.downloader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DownloadDB {
	private final static String TAG = "DownloadDB";
	private DownloadDBHelper dbHelper;

	public DownloadDB(Context context) {
		dbHelper = new DownloadDBHelper(context);
	}

	/**
	 * 查看数据库中是否有数据
	 */
	public boolean hasInfo(String urlstr) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		/*
		String whereClause = "url=?";
		Cursor c = database.query("download_info", null, whereClause ,new String[]{urlstr},null, null, null);
		boolean ret = c.moveToFirst();
		c.close();
		
		return !ret;
		*/
		
		Log.e(TAG,"urlstr: "+urlstr);
		String sql = "select count(*)  from download_info where url=?";
		Cursor cursor = database.rawQuery(sql, new String[] { urlstr });
		cursor.moveToFirst();
		int count = cursor.getInt(0);
		cursor.close();
		Log.e(TAG,"count: "+count);
		return count==0;
		
	}

	/**
	 * 保存 下载的具体信息
	 */
	public void saveDwonloadInfo(DownloadInfo info) {
		Log.e(TAG,"saveDwonloadInfo");
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Log.e(TAG,"urlstr: "+info.getUrl());
		
		String sql = "insert into download_info(url,file_size,compelete_size,file_path,file_md5) values (?,?,?,?,?)";
		Object[] bindArgs = {info.getUrl(), info.getFileSize(),info.getCompeleteSize(),info.getFilePath(),info.getFileMd5()};
		database.execSQL(sql, bindArgs);
		
		/*
		Log.e(TAG,"urlstr: "+info.getUrl());
		ContentValues cv = new ContentValues();
		cv.put("url", info.getUrl());
		cv.put("file_size", info.getFileSize());
		cv.put("compelete_size", info.getCompeleteSize());
		cv.put("file_path", info.getFilePath());
		cv.put("file_md5", info.getFileMd5());
		database.insert("download_info", null, cv);
		*/
	}

	/**
	 * 得到下载具体信息
	 */
	public DownloadInfo getDownloadInfo(String urlstr) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String sql = "select url,file_size,compelete_size,file_path,file_md5 from download_info where url=?";
		Cursor cursor = database.rawQuery(sql, new String[] { urlstr });
		DownloadInfo info = null;
		if(cursor.moveToFirst()){
			info = new DownloadInfo(cursor.getString(0),
					cursor.getInt(1), cursor.getInt(2), 
					cursor.getString(3),cursor.getString(4));
		}
		cursor.close();
		return info;
	}

	/**
	 * 更新数据库中的下载信息
	 */
	public void updataInfo(String urlstr,  int compeleteSize, String fileMd5) {
		Log.e(TAG,"updateInfo... compeleteSize: "+compeleteSize);
		/*
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String sql = "update download_info set compelete_size=?, file_md5=? where url=? ";
		Object[] bindArgs = { compeleteSize, fileMd5, urlstr };
		database.execSQL(sql, bindArgs);
		*/
		
		SQLiteDatabase	database = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("compelete_size", compeleteSize);
		values.put("file_md5", fileMd5);
		String whereCaluse = "url=?";
        String[] whereArgs = {urlstr};
        database.update("download_info", values, whereCaluse, whereArgs);
        database.close();
	}
	
	/**
	 * 关闭数据库
	 */
	public void closeDb() {
		dbHelper.close();
	}

	/**
	 * 下载完成后删除数据库中的数据
	 */
	public void delete(String url) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		database.delete("download_info", "url=?", new String[] { url });
		database.close();
	}
}
