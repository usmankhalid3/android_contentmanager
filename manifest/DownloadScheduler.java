package com.redriverstorm.how.manifest;

public interface DownloadScheduler {

	public void next(boolean lastDownloadSuccess);
}
