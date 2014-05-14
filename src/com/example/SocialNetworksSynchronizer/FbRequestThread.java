package com.example.SocialNetworksSynchronizer;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphLocation;
import com.facebook.model.GraphMultiResult;
import com.facebook.model.GraphUser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
            contactInfo.put("photoUrl", url);
            contactInfo.put("name", fullName);
            contactInfo.put("birthday", birthday);
            contactInfo.put("mobilePhone", "");
            contactInfo.put("homePhone", "");
            contactInfo.put("address", address);
            contactInfo.put("skype", "");
            contactInfo.put("twitter", "");
            contactInfo.put("instagram", "");
            contactInfo.put("university", "");
            contactInfo.put("faculty", "");

            Contact contact = new Contact(contactInfo);
            fbFriends.add(contact);
        }
    }

    @Override
    public synchronized void start() {
        super.start();
        Response response = request.executeAndWait();
        makeFbFriendsList(response);
    }
}
