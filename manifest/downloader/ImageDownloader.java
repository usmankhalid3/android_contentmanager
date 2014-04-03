package com.redriverstorm.how.manifest.downloader;

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.redriverstorm.how.manifest.common.ManifestEntry;
import com.redriverstorm.how.manifest.listener.DownloadListener;

public class ImageDownloader extends AsyncTask<ManifestEntry, Void, Bitmap> {
	 
	DownloadListener<Bitmap> listener;
	
	public ImageDownloader(DownloadListener<Bitmap> listener) {
		this.listener = listener;
	}
	
	@Override
    protected Bitmap doInBackground(ManifestEntry... param) {
        return downloadBitmap(param[0].getUrl());
    }
	
    @Override
    protected void onPostExecute(Bitmap result) {
    	if (result != null) {
    		listener.onSuccess(result);
    	}
    }

    private Bitmap downloadBitmap(String url) {
        // initilize the default HTTP client object
    	HttpParams httpParams = new BasicHttpParams();
    	
    	HttpConnectionParams.setConnectionTimeout(httpParams, 6000);
    	HttpConnectionParams.setSoTimeout(httpParams, 6000);
    	
        final DefaultHttpClient client = new DefaultHttpClient(httpParams);

        url = url.replaceAll(" ", "%20");
        
        //forming a HttpGet request 
        final HttpGet getRequest = new HttpGet(url);
        try {

            HttpResponse response = client.execute(getRequest);

            //check 200 OK for success
            final int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK) {
                listener.onFailure(url, statusCode);
                return null;

            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    // getting contents from the stream 
                    inputStream = entity.getContent();

                    // decoding stream data back into image Bitmap that android understands
                    final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    return bitmap;
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (Exception e) {
            // You Could provide a more explicit error message for IOException
            getRequest.abort();
            System.err.println("ImageDownloader: Something went wrong while retrieving bitmap from " + url + e.toString());
            listener.onFailure(url, -1);
        } 

        return null;
    }
    
}
