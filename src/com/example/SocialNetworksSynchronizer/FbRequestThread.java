package com.example.SocialNetworksSynchronizer;

import android.os.AsyncTask;
import android.text.TextUtils;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphLocation;
import com.facebook.model.GraphMultiResult;
import com.facebook.model.GraphUser;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FbRequestThread extends Thread {
    Request request = null;
    private ArrayList<Contact> fbFriends = null;

    FbRequestThread(ArrayList<Contact> fbFriends, Request request) {
        this.fbFriends = fbFriends;
        this.request = request;
    }

    private void makeFbFriendsList(Response response) {
        fbFriends.clear();
        List<GraphUser> friends = response.getGraphObjectAs(GraphMultiResult.class).
                getData().castToListOf(GraphUser.class);

        for (GraphUser gu : friends) {
            String fullName = gu.getName();

            String birthday = gu.getBirthday();
            if( birthday == null ) {
                birthday = "";
            }

            GraphLocation gl = gu.getLocation();
            String address = "";
            if (gl != null) {
                address = gu.getLocation().getProperty("name").toString();
            }
            String url = "";
            try {
                url = gu.getInnerJSONObject().getJSONObject("picture").getJSONObject("data").getString("url");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            HashMap<String,String> contactInfo = new HashMap<String, String>();
            contactInfo.put(Contact.PHOTO_URL, url);
            contactInfo.put(Contact.NAME, fullName);
            contactInfo.put(Contact.BIRTHDAY, birthday);
            contactInfo.put(Contact.MOBILE_PHONE, "");
            contactInfo.put(Contact.HOME_PHONE, "");
            contactInfo.put(Contact.ADDRESS, address);
            contactInfo.put(Contact.SKYPE, "");
            contactInfo.put(Contact.TWITTER, "");
            contactInfo.put(Contact.INSTAGRAM, "");
            contactInfo.put(Contact.EDUCATION, "");

            Contact contact = new Contact(contactInfo);
            fbFriends.add(contact);
        }
    }

    private void loadImages() {
        List <String> urls = new ArrayList<String>();
        for(Contact contact: fbFriends) {
            urls.add(contact.getPhotoUrl());
        }

        List <byte[]> avatars = new ArrayList<byte[]>();
        AsyncTask at = new DownloadImageTask(avatars).execute(TextUtils.join(",", urls));
        while( !((DownloadImageTask)at).finished ) {}

        for( int i = 0; i < avatars.size(); ++i ) {
            byte []bytesImage = avatars.get(i);
            fbFriends.get(i).setImage(bytesImage);
        }
    }

    @Override
    public synchronized void start() {
        super.start();
        Response response = request.executeAndWait();
        makeFbFriendsList(response);

        loadImages();
    }
}
