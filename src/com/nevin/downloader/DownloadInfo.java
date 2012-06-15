package com.nevin.downloader;

/**
 *创建一个下载信息的实体类
 */
public class DownloadInfo {
	private String url;//下载器网络标识
    private int fileSize; //文件大小
    private int compeleteSize;//完成度
    private String filePath; //文件存储路劲
    private String fileMd5; //文件MD5值
    
    
    public DownloadInfo(String url,int fileSize,int compeleteSize,	String filePath, String fileMd5) {
    	this.url=url;
    	this.fileSize = fileSize;
        this.compeleteSize = compeleteSize;
        this.filePath = filePath;
        this.fileMd5 = fileMd5;
    }
    
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public int getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }
    
    public int getCompeleteSize() {
        return compeleteSize;
    }
    
    public void setCompeleteSize(int compeleteSize) {
        this.compeleteSize = compeleteSize;
    }
    
    public String getFilePath(){
    	return this.filePath;
    }
    public void setFilePath(String filePath){
    	this.filePath = filePath;
    }
    
    public String getFileMd5(){
    	return this.fileMd5;
    }
    public void setFileMd5(String fileMd5){
    	this.fileMd5 = fileMd5;
    }

    @Override
    public String toString() {
        return "DownloadInfo [url: "+url 
        		+ "fileSize=" + fileSize
                + ", compeleteSize=" + compeleteSize +"]";
    }
}
