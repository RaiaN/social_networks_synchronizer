package com.example.SocialNetworksSynchronizer;

import com.facebook.Response;
import com.facebook.model.GraphLocation;
import com.facebook.model.GraphMultiResult;
import com.facebook.model.GraphUser;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParseFbResponseTask extends ParseResponseTask {
    private String FB_START_CAPTION = "Получение списка друзей FB...";
    private String FB_END_CAPTION   = "Друзья FB";

    private Response response = null;

    ParseFbResponseTask(ArrayList<Contact> friends, AsyncTaskListener listener, Response response ) {
        super(friends, listener);
        this.response = response;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        makeFbFriendsList();
        return super.doInBackground(voids);
    }

    @Override
    protected void onPreExecute() {
        maxValue = response.getGraphObjectAs(GraphMultiResult.class).
                getData().castToListOf(GraphUser.class).size();
        caption = FB_START_CAPTION;
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void v) {
        caption = FB_END_CAPTION;
        super.onPostExecute(v);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

    }

    private Contact getFbContact(GraphUser gu) {
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
        contact.setImage(loadImage(url));

        return contact;
    }

    private void makeFbFriendsList() {
        friends.clear();
        List<GraphUser> users = response.getGraphObjectAs(GraphMultiResult.class).
                getData().castToListOf(GraphUser.class);

        for (GraphUser gu : users) {
            Contact contact = getFbContact(gu);
            friends.add(contact);
            publishProgress(friends.size());
        }
    }
}
