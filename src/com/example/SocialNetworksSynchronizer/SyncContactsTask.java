package com.example.SocialNetworksSynchronizer;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.facebook.Request;
import com.facebook.Response;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.*;

public class SyncContactsTask extends AsyncTask<Void, Void, Void> {
    private ArrayList<Contact> vkFriends = null;
    private ArrayList<Contact> fbFriends = null;
    private ArrayList<SyncContact> syncContacts = null;
    private Phonebook phonebook = null;
    private DatabaseHandler handler = null;
    private AsyncTaskListener listener = null;

    ParseResponseTask vkTask = null;
    ParseResponseTask fbTask = null;

    SyncContactsTask(ArrayList<Contact> vkFriends, ArrayList<Contact> fbFriends,
                     ArrayList<SyncContact> syncContacts, Phonebook phonebook,
                     DatabaseHandler handler, AsyncTaskListener listener) {
        this.vkFriends = vkFriends;
        this.fbFriends = fbFriends;
        this.syncContacts = syncContacts;
        this.phonebook = phonebook;
        this.handler = handler;
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        final VKRequest vkRequest = Requests.vkFriendsRequest(Requests.LOCALE_RUS);

        vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                vkTask = new ParseVkResponseTask(vkFriends, listener, response);
                vkTask.setShowResults(false);
                vkTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        while( vkTask == null ) {}

        try {
            vkTask.get();
            Request fbRequest = Requests.fbFriendsRequest();
            Response response = fbRequest.executeAndWait();
            fbTask = new ParseFbResponseTask(fbFriends, listener, response);
            fbTask.setShowResults(false);
            Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    fbTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
            fbTask.get();
        } catch( Exception e ) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    protected void onPreExecute() {
        listener.onTaskBegin("Синхронизация...", MainActivity.ACTNS_BTNS);
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        fillSyncContactsList();

        SQLiteDatabase db = handler.getWritableDatabase();
        handler.dropTable(db);

        int id = 0;
        for(SyncContact sc: syncContacts) {
            ContentValues values = new ContentValues();
            values.put(DatabaseHandler.ID_COLUMN, id);
            values.put(DatabaseHandler.CONTACT, Serializer.serializeObject(sc));

            try {
                long resRowInd = db.insert(DatabaseHandler.TABLE_NAME, null, values);
                Log.e("INSERT QUERY ID", String.valueOf(resRowInd));
                id += 1;
            } catch( NullPointerException e) {
                e.printStackTrace();
                return;
            }
        }
        db.close();

        listener.onTaskCompleted("Синхронизация успешно завершена.", MainActivity.ACTNS_BTNS);
    }

    private int findSimilarContact(String currentName, ArrayList<String> names) {
        //В цикле пройтись по всем друзьям ВК и выяснить, есть ли контакт с таким же именем(phonebookName)
        for (int vkInd = 0; vkInd < names.size(); ++vkInd) {
            String[] components = names.get(vkInd).split(" ");
            int containsCount = 0;
            for (int compInd = 0; compInd < components.length; ++compInd) {
                if (currentName.contains(components[compInd])) {
                    ++containsCount;
                }
            }
            //Если схожий контакт найден, то добавить его в item
            if (containsCount == components.length) {
                return vkInd;
            }
        }
        return -1;
    }

    private void fillSyncContactsList() {
        ArrayList<String> vkNames = MainActivity.getContactNames(vkFriends);
        ArrayList<String> fbNames = MainActivity.getContactNames(fbFriends);
        Queue<String> phonebookNames = phonebook.getContactNames();

        while (!phonebookNames.isEmpty()) {
            //Создаём новый SyncContact. SyncContact.java для подробностей
            SyncContact item = new SyncContact();

            //получить текущее имя контакта с телефона
            String currentName = phonebookNames.remove();
            //добавить его в item
            item.setPhonebookName(currentName);

            int pos = findSimilarContact(currentName, vkNames);
            if( pos != -1 ) {
                item.setVkContact(vkFriends.get(pos));
            } else {
                item.setVkContact(null);
            }

            pos = findSimilarContact(currentName, fbNames);
            if( pos != -1 ) {
                item.setFbContact(fbFriends.get(pos));
            } else {
                item.setFbContact(null);
            }

            //добавить item в синхронизированный список контактов
            syncContacts.add(item);
        }
    }
}
