package com.example.SocialNetworksSynchronizer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


public class CacheImageDownloader implements com.nostra13.universalimageloader.core.download.ImageDownloader {

    @Override
    public InputStream getStream(String imageUri, Object extra) throws IOException {
        URL url = new URL(imageUri);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setConnectTimeout(1000);
        urlConnection.setDoInput(true);
        urlConnection.setReadTimeout(1000);

        return urlConnection.getInputStream();
    }
}
