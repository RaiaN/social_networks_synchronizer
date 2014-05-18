package com.example.SocialNetworksSynchronizer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
public class ParseResponseTask extends AsyncTask<Void, Integer, Void> {
    protected ArrayList<Contact> friends = null;
    protected AsyncTaskListener listener = null;

    protected int maxValue = -1;
    protected int[] buttonIndexes = new int[]{};
    protected String caption = "";
    private boolean showResults = true;

    ParseResponseTask(ArrayList<Contact> friends, AsyncTaskListener listener)
    {
        this.friends = friends;
        this.listener = listener;
    }

    public void setButtonIndexes(int []buttonIndexes) {
        this.buttonIndexes = buttonIndexes;
    }
    public void setShowResults(boolean showResults ) { this.showResults = showResults; }

    byte[] loadImage(String photoUrl) {
        try {
            URL url = new URL(photoUrl);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(1000);
            urlConnection.setDoInput(true);
            urlConnection.setReadTimeout(1000);
            Bitmap bm = BitmapFactory.decodeStream(urlConnection.getInputStream());

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 90, stream);
            byte[] byteArray = stream.toByteArray();
            return  byteArray;
        } catch (IOException ioe ) {
            ioe.printStackTrace();
        }
        return null;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        return null;
    }

    @Override
    protected void onPreExecute() {
        listener.onTaskBegin(caption, maxValue, buttonIndexes);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        listener.onTaskProgress(friends.size());
    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);

        if( showResults ) {
            listener.onTaskCompleted(caption, friends, buttonIndexes);
        }
    }
}
