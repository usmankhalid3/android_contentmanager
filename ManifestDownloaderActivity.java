package com.server.content;

import android.app.Activity;
import android.os.Bundle;

import com.redriverstorm.how.manifest.AssetsManager;
import com.redriverstorm.how.manifest.listener.ProgressListener;

public class ManifestDownloaderActivity extends Activity implements ProgressListener {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String contentServerUrl = "http://192.168.21.17/i";
        AssetsManager.getInstance().init(contentServerUrl, this, this);
        AssetsManager.getInstance().prepareGameAssets();
    }

	@Override
	public void onInitializing() {
		System.out.println("Initializing manifest...");
	}

	@Override
	public void onDownloadingManifest() {
		System.out.println("Downloading manifest...");
		
	}

	@Override
	public void onStart(int totalDownloads) {
		System.out.println("Starting downloads...Total: " + totalDownloads);
	}

	@Override
	public void onProgress(int completedDownloads, int totalDownloads) {
		System.out.println("Completed downloads: " + completedDownloads + " / " + totalDownloads);
	}

	@Override
	public void onComplete(boolean newContent) {
		System.out.println("All done. Got new content: " + newContent);
	}

	@Override
	public void onManifestDownloadFailed(int error) {
		//TODO raise an alarm
	}

	@Override
	public void onContentDownloadFailed(int error) {
		// TODO all hell broke loose
		
	}    
}
