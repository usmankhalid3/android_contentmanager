package com.redriverstorm.how.manifest.downloader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.os.AsyncTask;
import android.util.Log;

import com.redriverstorm.how.manifest.F.Callback;

public abstract class AbstractFileDownloader<X, Y> extends AsyncTask<X, Void, Y> {
	
    protected int downloadFile(String url, Callback<String> consumeCallback) throws Throwable {

    	// initilize the default HTTP client object
    	HttpParams httpParams = new BasicHttpParams();
    	
    	HttpConnectionParams.setConnectionTimeout(httpParams, 6000);
    	HttpConnectionParams.setSoTimeout(httpParams, 6000);
    	
        final DefaultHttpClient client = new DefaultHttpClient(httpParams);
        
        //forming a HttpGet request 
        final HttpGet getRequest = new HttpGet(url);
        
        try {

            HttpResponse response = client.execute(getRequest);

            //check 200 OK for success
            final int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK) {
                return statusCode;
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    // getting contents from the stream 
                    inputStream = entity.getContent();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line = null;
                    
    				while ((line = reader.readLine()) != null) {
    					consumeCallback.invoke(line);
    				}

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
            Log.e("File Downloading", "Something went wrong while retrieving from " + url + ", trace: " + e.toString());
            throw new RuntimeException(e);
        } 

        return HttpStatus.SC_OK;
    }

}
