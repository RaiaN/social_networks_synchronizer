package com.example.SocialNetworksSynchronizer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DownloadImageTask extends AsyncTask<String, Void, Void> {
    private boolean finished = false;
    private List<byte[]> avatars = new ArrayList<byte[]>();

    DownloadImageTask(List<byte[]> avatars) {
        this.avatars = avatars;
    }

    Void getBitmapFromUrl(String urls) throws java.io.IOException {
        try {
            for(String src: urls.split(",")) {
                URL url = new URL(src);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                avatars.add(Serializer.serializeObject(myBitmap));
            }
            finished = true;
        } catch (IOException e) {
            e.printStackTrace();
            avatars.add(new byte[]{});
        }
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
