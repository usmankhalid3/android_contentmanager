package com.redriverstorm.how.manifest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;

import com.redriverstorm.how.manifest.F.Callback;
import com.redriverstorm.how.manifest.common.Manifest;
import com.redriverstorm.how.manifest.common.ManifestEntry;
import com.redriverstorm.how.manifest.downloader.ImageDownloader;
import com.redriverstorm.how.manifest.downloader.ManifestDownloader;
import com.redriverstorm.how.manifest.downloader.SimpleFileDownloader;
import com.redriverstorm.how.manifest.listener.AbstractDownloadListener;
import com.redriverstorm.how.manifest.listener.DownloadListener;
import com.redriverstorm.how.manifest.listener.ProgressListener;
import com.redriverstorm.how.util.HowUtils;

public class AssetsManager {

	private Activity theApp;
    private Manifest gameManifest;
    private String manifestName = "manifest";
    private ProgressListener progressListener;
    private String contentServerUrl;
    private boolean useInternalStorage = false;
	
    private static AssetsManager instance = new AssetsManager();
    
    private AssetsManager() {
    	
    }
    
    public static AssetsManager getInstance() {
    	return instance;
    }
    
    public void init(String contentServerUrl, Activity theApp, ProgressListener progressListener) {
    	this.contentServerUrl = contentServerUrl;
    	this.theApp = theApp;
    	this.progressListener = progressListener;
    	prepareLocalAssets();
    }

    private void prepareLocalAssets() {
		progressListener.onInitializing();
    	try {
        	if (!gameManifestExists()) {
        		stdOut("Copying the game manifest");
        		copyManifestFromAssetsToStorage();
        		stdOut("Game manifest copied successfully!");
        	}
        	buildGameManifest();
        }
        catch(Exception e) {
        	stdErr("Could not load the game manifest!!!");
        	throw new RuntimeException(e);
        }
    }
    
	public void prepareGameAssets() {
    	progressListener.onDownloadingManifest();
        new ManifestDownloader(new ManifestDownloadListener()).execute(contentServerUrl);
    }
	
	public boolean isLocal(String key) {
		if (gameManifest == null) {
			System.err.println("Game Manifest was found null!!!!");
			return true;
		}
		return gameManifest.isLocal(key);
	}
	
	public String getSavedPath(String key) {
		String baseDir = theApp.getFilesDir().getAbsolutePath();
		if (!useInternalStorage) {
			baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		}
		return baseDir + getRelativeSavePath(key);
	}
	
	public InputStream getContent(String key) throws IOException {
		if (isLocal(key)) {
			return theApp.getAssets().open(key);
		}
		else {
			String savedPath = getSavedPath(key);
    		return new FileInputStream(savedPath);
		}
	}
	
	private String getRelativeSavePath(String key) {
		return "/Images/" + key;	
	}
    
    private void buildGameManifest() throws Exception {
    	gameManifest = new Manifest();
		InputStream inputStream = theApp.openFileInput(manifestName);
		InputStreamReader isr = new InputStreamReader(inputStream);
		BufferedReader bufferedReader = new BufferedReader(isr);
		//StringBuilder fileContent = new StringBuilder();
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			gameManifest.put(line);
		}
		inputStream.close();
		isr.close();
		bufferedReader.close();
    }
    
    private class ManifestDownloadListener implements DownloadListener<Manifest>, DownloadScheduler {

    	private Iterator<String> iterator;
    	private Manifest manifest;
    	private String activeDownloadKey;
    	private int startTime;
    	private int completedDownloads;
    	
		@Override
		public void onSuccess(Manifest manifest) {
			if (manifest == null || manifest.isEmpty()) {
				throw new RuntimeException("Manifest is null or empty");
			}
			stdOut("Manifest downloaded successfully! Total entries: " + manifest.size());
			this.manifest = Manifest.compare(gameManifest, manifest);
			if (this.manifest.isEmpty()) {
				progressListener.onComplete(false);
				stdOut("All assets are already up to date!");
				return;
			}
			startTime = HowUtils.getUnixTime();
			completedDownloads = 0;
			Set<String> keys = this.manifest.getKeys();
			this.iterator = keys.iterator();
			stdOut("Total assets to download: " + keys.size());
			progressListener.onStart(keys.size());
			next(false);
		}

		@Override
		public void onFailure(String url, int error) {
			stdErr("Failed with error [" + error + "] while downloading manifest from url: " + url);
		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub
		}

		@Override
		public void next(boolean lastDownloadSuccess) {
			if (lastDownloadSuccess == true && activeDownloadKey != null) {
				gameManifest.setChecksum(activeDownloadKey, this.manifest.getChecksum(activeDownloadKey));
				completedDownloads++;
			}
			if (!iterator.hasNext()) {
				updateGameManifest();
				stdOut("Total time taken for download: " + (HowUtils.getUnixTime() - startTime));
				progressListener.onProgress(completedDownloads, manifest.size());
				progressListener.onComplete(true);
				return;
			}
			String key = iterator.next();
			this.activeDownloadKey = key; 
			ManifestEntry entry = manifest.get(key);
			progressListener.onProgress(completedDownloads, manifest.size());
			if (entry.isImage()) {
				stdOut("Going to download: " + key + ", checksum:" + entry.getChecksum());
				new ImageDownloader(new ImageDownloadListener(this, entry)).execute(entry);
			}
			else if (entry.isPLIST() || entry.isJSON() || entry.isFont()) {
				stdOut("Going to download: " + key + ", checksum:" + entry.getChecksum());
				new SimpleFileDownloader(new SimpleFileDownloadListener(this, entry)).execute(entry);
			}
			else {
				stdErr("Cannot download file of type: " + entry.getType());
				next(false);
			}
		}
    }
    
    private class SimpleFileDownloadListener extends AbstractDownloadListener implements DownloadListener<String> {
    	
    	public SimpleFileDownloadListener(DownloadScheduler scheduler, ManifestEntry entry) {
    		super(scheduler, entry);
    	}
    	
		@Override
		public void onSuccess(String data) {
			if (data != null) {
				try {
					File storageDir = getStorageDirectory();
					writeContentToFile(storageDir, entry, data);
				}
				catch (Throwable t) {
					stdErr("Could not write image to disk!");
					throw new RuntimeException(t);
				}
				stdOut("Image saved successfully!");
				scheduler.next(true);
			}			
		}

		@Override
		public void onFailure(String url, int error) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub
			
		}
    	
    }
    
    private class ImageDownloadListener extends AbstractDownloadListener implements DownloadListener<Bitmap> {
    	
    	public ImageDownloadListener(DownloadScheduler scheduler, ManifestEntry entry) {
    		super(scheduler, entry);
    	}
    	
		@Override
		public void onSuccess(Bitmap data) {
			if (data != null) {
				try {
					File storageDir = getStorageDirectory();
					writeBitmapToFile(storageDir, entry, data);
				}
				catch (Throwable t) {
					stdErr("Could not write image to disk!");
					throw new RuntimeException(t);
				}
				stdOut("Image saved successfully!");
				scheduler.next(true);
			}
		}

		@Override
		public void onFailure(String url, int error) {
			stdErr("Failed with error [" + error + "] while downloading image from url: " + url);
		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub
		}
    	
    }
    
    private File getStorageDirectory() throws Throwable {
    	return getStorageDirectory(useInternalStorage);
    }
    
    private File getStorageDirectory(boolean internal) throws Throwable {
    	if (internal == true) { 
    		return theApp.getFilesDir();
    	}
    	else {
    		if (!canWriteToExternal()) {
        		throw new RuntimeException("Not permitted to write to external storage");
        	}
        	return Environment.getExternalStorageDirectory();
    	}
    }

    private void writeBitmapToFile(File baseDir, ManifestEntry entry, final Bitmap result) throws Throwable {
    	CompressFormat imageFormat = CompressFormat.PNG; 
    	if (entry.isJPEG()) {
    		imageFormat = CompressFormat.JPEG;
    	}
    	else if (entry.isPNG()) {
    		imageFormat = CompressFormat.PNG;
    	}
    	final CompressFormat fileFormat = imageFormat;
    	writeContentToFile(baseDir, entry, new Callback<FileOutputStream>(){
			@Override
			public void invoke(FileOutputStream fos) throws Throwable {
				result.compress(fileFormat, 100, fos);
			}
    	});
    }
    
    private void writeContentToFile(File baseDir, ManifestEntry entry, final String content) throws Throwable {
    	writeContentToFile(baseDir, entry, new Callback<FileOutputStream>() {
			@Override
			public void invoke(FileOutputStream fos) throws Throwable {
				fos.write(content.getBytes());
			}
    	});
    }

    private boolean canWriteToExternal() {
    	String state = Environment.getExternalStorageState();
    	if (Environment.MEDIA_MOUNTED.equals(state)) {
    		return true;
    	}
    	return false;
    }

    private void updateGameManifest() {
    	try {
    		FileOutputStream fos = theApp.openFileOutput(manifestName, Activity.MODE_PRIVATE);
    		fos.write(gameManifest.getContent().getBytes());
    		fos.flush();
    		fos.close();
    		stdOut("Game manifest updated successfully!!!");
    	}
    	catch(Exception e) {
    		stdErr("Could not save the updated game manifest!!!");
    		throw new RuntimeException(e);
    	}
    }
    
    private boolean gameManifestExists() {
    	String filePath = theApp.getFilesDir() + "/" + manifestName;
    	File file = new File(filePath);
    	return file.exists();
    }
    
    private void copyManifestFromAssetsToStorage() throws IOException {
    	  InputStream inputStream = theApp.getAssets().open(manifestName);
    	  OutputStream outputStream = theApp.openFileOutput(manifestName, Activity.MODE_PRIVATE);
    	  copyStream(inputStream, outputStream);
    	  outputStream.flush();
    	  outputStream.close();
    	  inputStream.close();
    }
    
	private void copyStream(InputStream Input, OutputStream Output) throws IOException {
	  byte[] buffer = new byte[5120];
	  int length = Input.read(buffer);
	  while (length > 0) {
	    Output.write(buffer, 0, length);
	    length = Input.read(buffer);
	  }
	}
	
    private void writeContentToFile(File baseDir, ManifestEntry entry, Callback<FileOutputStream> callback) throws Throwable {
    	String basePath = baseDir.getAbsolutePath();
    	try {
    		//TODO:: purify the path (remove test part)
    		String savePath = getRelativeSavePath(entry.getKey());
        	File downloadedImage = new File(basePath, savePath);
        	if (!downloadedImage.getParentFile().exists()) {
        		downloadedImage.getParentFile().mkdirs();
        	}
        	FileOutputStream fos = new FileOutputStream(downloadedImage);
    		callback.invoke(fos);
        	fos.flush();
        	fos.close();
    	}
    	catch(Exception e) {
    		throw new RuntimeException(e);
    	}
    }

    private void stdOut(String msg) {
    	//System.out.println(msg);
    }
    
    private void stdErr(String msg) {
    	//System.err.println(msg);
    }
}
