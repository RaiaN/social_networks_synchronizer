package com.example.SocialNetworksSynchronizer;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphLocation;
import com.facebook.model.GraphMultiResult;
import com.facebook.model.GraphUser;

import java.util.ArrayList;
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

            GraphLocation gl = gu.getLocation();
            String address = "";
            if (gl != null) {
                address = gu.getLocation().getProperty("name").toString();
            }
            String email = "E-mail не указан";
            if( gu.getProperty("email") != null ) {
                email = gu.getProperty("email").toString();
            }

            Contact contact = new Contact(new String[]{ fullName, address, email });
            contact.setMobilePhone("Facebook не предоставляет данную информацию");
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
