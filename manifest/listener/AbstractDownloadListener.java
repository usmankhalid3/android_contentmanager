package com.redriverstorm.how.manifest.listener;

import com.redriverstorm.how.manifest.DownloadScheduler;
import com.redriverstorm.how.manifest.common.ManifestEntry;

public class AbstractDownloadListener {

	protected ManifestEntry entry;
	protected DownloadScheduler scheduler;
	
	public AbstractDownloadListener(DownloadScheduler scheduler, ManifestEntry entry) {
		this.scheduler = scheduler;
		this.entry = entry;
	}
	
}
