package com.example.SocialNetworksSynchronizer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class DownloadImageTask extends AsyncTask<String, Void, Void> {
    public List <byte[]> avatars = null;
    public boolean finished = false;

    DownloadImageTask(List <byte[]> avatars) {
        this.avatars = avatars;
    }

    Void getBitmapFromUrl(String urls) throws java.io.IOException {
        try {
            for(String src: urls.split(",")) {
                URL url = new URL(src);
                URLConnection urlConnection = url.openConnection();
                urlConnection.setConnectTimeout(1000);
                urlConnection.setDoInput(true);
                urlConnection.setReadTimeout(1000);
                Bitmap bm = BitmapFactory.decodeStream(urlConnection.getInputStream());

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.PNG, 90, stream);
                byte[] byteArray = stream.toByteArray();

                avatars.add(byteArray);
            }
        } catch (Exception e) {
            e.printStackTrace();
            avatars.add(null);
        }
        finished = true;
        return null;
    }

    @Override
    protected Void doInBackground(String... urls) {
        try {
            getBitmapFromUrl(urls[0]);
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
