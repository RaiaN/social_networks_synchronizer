package com.example.SocialNetworksSynchronizer;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.*;
import com.facebook.*;
import com.facebook.model.GraphLocation;
import com.facebook.model.GraphUser;
import com.vk.sdk.*;
import com.vk.sdk.api.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.security.Permission;
import java.util.*;

public class MainActivity extends FragmentActivity {
    private final String vkAppId = "4313814";
    private final String fbAppId = "1416447705289612";
    private VKAccessToken accessToken = null;

    public final String[] permissions = new String[] {
            VKScope.FRIENDS,
            VKScope.NOHTTPS
    };
    public static final String[] extras = new String[] {
            "full_name",
            "phones",
            "address"
    };
    private final String localeRus = "ru";
    private final String localeEng = "en";

    private ArrayList<Contact> vkFriends = new ArrayList<Contact>();
    ArrayList<String> friendsList = new ArrayList<String>();

    private ArrayList<Contact> fbFriends = new ArrayList<Contact>();
    private ArrayList<SyncContact> syncContacts = new ArrayList<SyncContact>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);


        final Button syncButton = (Button)findViewById(R.id.sync_button);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sync();
            }
        });

        final Button vkButton = (Button)findViewById(R.id.vk_friends_button);
        vkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performVkRequestAndShowResults();
            }
        });

        final Button fbButton = (Button)findViewById(R.id.fb_friends_button);
        fbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performFbRequestAndShowResults();
            }
        });

        final Button phonebookButton = (Button)findViewById(R.id.phonebook_button);
        phonebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makePhonebook();
            }
        });

        final Button logoutButton = (Button)findViewById(R.id.logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VKSdk.logout();
                finish();
            }
        });

        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        VKUIHelper.onResume(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VKUIHelper.onDestroy(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        VKUIHelper.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    private static Session openActiveSession(Activity activity, boolean allowLoginUI, List<String> permissions, Session.StatusCallback callback) {
        Session.OpenRequest openRequest = new Session.OpenRequest(activity).setPermissions(permissions).setCallback(callback);
        Session session = new Session.Builder(activity).build();
        if (SessionState.CREATED_TOKEN_LOADED.equals(session.getState()) || allowLoginUI) {
            Session.setActiveSession(session);
            session.openForRead(openRequest);
            return session;
        }
        return null;
    }

    private void init() {
        VKUIHelper.onCreate(this);
        VKSdk.initialize(sdkListener, vkAppId);
        VKSdk.authorize(permissions);

        MainActivity.openActiveSession(this, true, Arrays.asList("friends_hometown"), new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                if (session.isOpened()) {
                }
            }
        });
    }

    private final VKSdkListener sdkListener = new VKSdkListener() {
        @Override
        public void onCaptchaError(VKError captchaError) {
            new VKCaptchaDialog(captchaError).show();
        }

        @Override
        public void onTokenExpired(VKAccessToken expiredToken) {
            VKSdk.authorize(permissions);
        }

        @Override
        public void onAccessDenied(VKError authorizationError) {
            //((ListView)findViewById(R.id.lv)).(authorizationError.apiError.errorMessage);
        }

        @Override
        public void onReceiveNewToken(VKAccessToken newToken) {
            accessToken = newToken;
            //((ListView)findViewById(R.id.lv)).setText("Successful authorization");
        }

        @Override
        public void onAcceptUserToken(VKAccessToken token) {

        }
    };

    private VKRequest getFriendsRequest(String locale){
        VKRequest request = VKApi.friends().get(VKParameters.from(VKApiConst.USER_IDS, "",
                "order", "name",
                VKApiConst.FIELDS, "city,country,contacts"));
        request.secure = false;
        request.setPreferredLang(locale);
        return request;
    }

    private Cursor getContacts() {
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String[] projection = new String[] { ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME };
        String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = 1";
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
                + " COLLATE LOCALIZED ASC";

        return getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
    }

    private Queue<String> getPhoneContacts() {
        Cursor cursor = getContacts();

        Queue<String> contactNames = new LinkedList<String>();
        while(cursor.moveToNext()) {

            String displayName = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
            contactNames.add(displayName);
        }
        return contactNames;
    }

    private void sync() {
        this.setTitle("Синхронизация...");
        syncContacts.clear();

        VKRequest request = getFriendsRequest(localeRus);
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);

                try {
                    makeVkFriendsList(response);
                    Queue<String> phonebookNames = getPhoneContacts();

                    while (!phonebookNames.isEmpty()) {
                        SyncContact item = new SyncContact();

                        String phonebookName = phonebookNames.remove();
                        item.setPhonebookName(phonebookName);

                        for (int vkInd = 0; vkInd < vkFriends.size(); ++vkInd) {
                            String[] components = vkFriends.get(vkInd).getName().split(" ");
                            int containsCount = 0;
                            for (int compInd = 0; compInd < components.length; ++compInd) {
                                if (phonebookName.contains(components[compInd])) {
                                    ++containsCount;
                                }
                            }

                            if (containsCount == components.length) {
                                item.setVkContact(vkFriends.get(vkInd));
                                break;
                            }
                        }

                        if (item.getVkName().length() == 0) {
                            item.setVkContact(null);
                        }
                        if (item.getFbName().length() == 0) {
                            item.setFbContact(null);
                        }

                        syncContacts.add(item);
                    }
                    MainActivity.this.setTitle("Синхронизация успешно завершена!");
                } catch (JSONException e) {

                }
            }
        });
    }

    private boolean correctPhoneNumber(String number) {
        String modifiedNumber = number.replaceAll("[^0-9\\+]","");

        if( modifiedNumber.length() == 0) {
            return false;
        }
        for( int i = 0; i < modifiedNumber.length(); ++i ) {
            if( !Character.isDigit(modifiedNumber.charAt(i)) ) {
                return false;
            }
        }

        return true;
    }

    private void makeVkFriendsList(VKResponse response) throws JSONException {
        JSONArray res = response.json.getJSONObject("response").getJSONArray("items");
        friendsList.clear();

        for( int i = 0; i < res.length(); ++i ) {
            String fullName = res.getJSONObject(i).getString("first_name") + ' ' +
                              res.getJSONObject(i).getString("last_name");
            friendsList.add(fullName);

            String mobilePhone = "";
            if( res.getJSONObject(i).has("mobile_phone") &&
                res.getJSONObject(i).getString("mobile_phone").length() > 0 &&
                correctPhoneNumber(res.getJSONObject(i).getString("mobile_phone")) )
            {
                mobilePhone += "Мобильный телефон:\n" + res.getJSONObject(i).getString("mobile_phone");
            } else {
                mobilePhone += "Мобильный телефон не указан";
            }

            String homePhone = "";
            if( res.getJSONObject(i).has("home_phone") &&
                res.getJSONObject(i).getString("home_phone").length() > 0 &&
                correctPhoneNumber(res.getJSONObject(i).getString("home_phone")) )
            {
                homePhone += "Домашний телефон:\n" + res.getJSONObject(i).getString("home_phone");
            } else {
                homePhone += "Домашний телефон не указан";
            }

            String address = "";
            if( res.getJSONObject(i).has("country") ) {
                address += res.getJSONObject(i).getJSONObject("country").getString("title");
                if( res.getJSONObject(i).has("city") ) {
                    address += ", " + res.getJSONObject(i).getJSONObject("city").getString("title");
                }
            } else {
                address += "Адрес не указан";
            }

            Contact contact = new Contact(new String[]{fullName, mobilePhone, homePhone, address});
            vkFriends.add(contact);
        }
    }

    private void showVkFriends() {
        this.setTitle("Друзья"); /*TODO: add string with name of social network*/
        ListView lv = ((ListView)findViewById(R.id.lv));

        ArrayAdapter<String> friendsAdapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.li, friendsList);
        lv.setAdapter(friendsAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Contact selectedContact = vkFriends.get(i);

                Intent intent = new Intent(MainActivity.this, FriendInfoActivity.class);
                intent.putExtra(extras[0], selectedContact.getName());
                intent.putExtra(extras[1], selectedContact.getMobilePhone() + '\n' + selectedContact.getHomePhone());
                intent.putExtra(extras[2], selectedContact.getAddress());

                startActivity(intent);
            }
        });
    }

    private void performVkRequestAndShowResults() {
        VKRequest request = getFriendsRequest(localeRus);

        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                try {
                    makeVkFriendsList(response);
                    showVkFriends();
                } catch (JSONException e) {
                    ((ListView) findViewById(R.id.lv)).setAdapter(
                            new ArrayAdapter<String>(MainActivity.this,
                            R.layout.list_item,
                            R.id.li,
                            new String[]{e.toString()}));
                }
            }
            @Override
            public void onError(VKError error) {
                if (error.apiError != null)
                    ((ListView) findViewById(R.id.lv)).setAdapter(
                            new ArrayAdapter<String>(MainActivity.this,
                            R.layout.list_item,
                            R.id.li,
                            new String[]{error.errorMessage}));
                else {
                    ((ListView) findViewById(R.id.lv)).setAdapter(
                            new ArrayAdapter<String>(MainActivity.this,
                            R.layout.list_item,
                            R.id.li,
                            new String[]{String.format("Error %d: %s", error.errorCode, error.errorMessage)}));
                }
            }
        });
    }

    private void performFbRequestAndShowResults() {
        Request fbRequest = Request.newMyFriendsRequest(Session.getActiveSession(), new Request.GraphUserListCallback() {
            @Override
            public void onCompleted(List<GraphUser> users, Response response) {
                ArrayList<String> friendsList = new ArrayList<String>();
                for (GraphUser gu : users) {
                    String fullName = gu.getName();
                    GraphLocation gl = gu.getLocation();
                    String address = "";
                    if (gl != null) {
                        address = gu.getLocation().getCountry();
                        if (address.length() > 0 && gu.getLocation().getCity().length() > 0) {
                            address += ", " + gu.getLocation().getCity();
                        }
                    }
                    friendsList.add(fullName);
                    friendsList.add(address);
                }
            }
        });
        fbRequest.executeAsync();
    }

    private void makePhonebook() {
        this.setTitle("Телефонная книга");
        ListView lv = ((ListView)findViewById(R.id.lv));
        lv.setClickable(false);
        if( syncContacts.isEmpty() ) {
            this.setTitle("Телефонная книга(без синхронизации)");

            Queue<String> phoneContacts = getPhoneContacts();
            while( !phoneContacts.isEmpty() ) {
                String pbName = phoneContacts.remove();
                SyncContact item = new SyncContact(pbName, null, null);
                syncContacts.add(item);
            }
        }
        ContactArrayAdapter adapter = new ContactArrayAdapter(this, syncContacts);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(null);
    }
}