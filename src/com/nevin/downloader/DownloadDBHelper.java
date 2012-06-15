package com.nevin.downloader;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DownloadDBHelper extends SQLiteOpenHelper{
    public DownloadDBHelper(Context context) {
        super(context, "download.db", null, 1);
    }
    
    /**
     * 在download.db数据库下创建一个download_info表存储下载信息
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table download_info(" +
        		"_id integer PRIMARY KEY AUTOINCREMENT, " +
        		"url char, "+
        		"file_size integer, " +
                "compelete_size integer, " +
        		"file_path char, " +
                "file_md5 char" +
                ")");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}




