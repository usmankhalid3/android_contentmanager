package com.redriverstorm.how.manifest.listener;

public interface DownloadListener<T> {

	public void onSuccess(T data);
	public void onFailure(String url, int error);
	public void onCancel();
}
