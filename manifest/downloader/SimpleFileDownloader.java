package com.redriverstorm.how.manifest.downloader;

import org.apache.http.HttpStatus;

import com.redriverstorm.how.manifest.F;
import com.redriverstorm.how.manifest.common.ManifestEntry;
import com.redriverstorm.how.manifest.listener.DownloadListener;

public class SimpleFileDownloader extends AbstractFileDownloader<ManifestEntry, String> {

	DownloadListener<String> listener;
	
	public SimpleFileDownloader(DownloadListener<String> listener) {
		this.listener = listener;
	}

	@Override
	protected String doInBackground(ManifestEntry... args) {
		return downloadFile(args[0].getUrl());
	}
	
	@Override
    protected void onPostExecute(String content) {
		if (content != null) {
			listener.onSuccess(content);
		}
	}
	
    private String downloadFile(final String url) {    	
    	final StringBuilder sb = new StringBuilder("");   
    	int statusCode = -1;
    	
    	try {    	
    		statusCode = downloadFile(url, new F.Callback<String>() {

				@Override
				public void invoke(String line) throws Throwable {
					sb.append(line);
					sb.append("\n");
				}
    		});
    	}
    	catch (Throwable t) {
    		throw new RuntimeException(t);
    	}
    	
    	if (statusCode != HttpStatus.SC_OK) {
    		System.err.println("File Downloading: Error " + statusCode + " while retrieving from " + url);
    		listener.onFailure(url, statusCode);
            return null;
    	}
    	else {
    		return sb.toString();
    	}
    }

}
