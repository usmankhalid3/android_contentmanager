package com.redriverstorm.how.manifest.downloader;

import org.apache.http.HttpStatus;

import com.redriverstorm.how.manifest.F;
import com.redriverstorm.how.manifest.common.Manifest;
import com.redriverstorm.how.manifest.common.ManifestEntry;
import com.redriverstorm.how.manifest.listener.DownloadListener;

public class ManifestDownloader extends AbstractFileDownloader<String, Manifest> {
	
	DownloadListener<Manifest> listener;
	
	private String manifestUrl = "manifest";
	
	public ManifestDownloader(DownloadListener<Manifest> listener) {
		this.listener = listener;
	}

	@Override
	protected Manifest doInBackground(String... args) {
		return downloadManifest(args[0]);
	}
	
	@Override
    protected void onPostExecute(Manifest manifest) {
		if (manifest != null) {
			listener.onSuccess(manifest);
		}
	}
	
    private Manifest downloadManifest(final String baseUrl) {    	
    	final Manifest manifest = new Manifest();   
    	String url = baseUrl + manifestUrl;
    	int statusCode = -1;
    	
    	try {    	
    		statusCode = downloadFile(url, new F.Callback<String>() {

				@Override
				public void invoke(String line) throws Throwable {
					System.out.println("LINE: " + line);
					String[] parts = line.split(",");
					String key = parts[0];
					String checksum = parts[1];
					boolean isLocal = Boolean.parseBoolean(parts[2]);
					manifest.put(key, new ManifestEntry(baseUrl, key, checksum, isLocal));	
				}
    		});
    	}
    	catch (Throwable t) {
    		throw new RuntimeException(t);
    	}
    	
    	if (statusCode != HttpStatus.SC_OK) {
    		System.err.println("Manifest Downloading: Error " + statusCode + " while retrieving from " + url);
    		listener.onFailure(url, statusCode);
            return null;
    	}
    	else {
    		return manifest;
    	}
    }
}