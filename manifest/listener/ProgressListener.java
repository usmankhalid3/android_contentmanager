package com.redriverstorm.how.manifest.listener;

public interface ProgressListener {

	public void onInitializing();
	public void onDownloadingManifest();
	public void onStart(int totalDownloads);
	public void onProgress(int completedDownloads, int totalDownloads);
	public void onComplete(boolean newContent);
	
	public void onManifestDownloadFailed(int error);
	public void onContentDownloadFailed(int error);
	
}
