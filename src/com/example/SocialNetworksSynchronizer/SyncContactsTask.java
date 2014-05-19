package com.example.SocialNetworksSynchronizer;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import com.facebook.Request;
import com.facebook.Response;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;

public class SyncContactsTask extends AsyncTask<Void, Void, Void> {
    private final String MAPPING_CAPTION = "Нахождение соответствий...";
    private final String OK = "Ещё немного...";

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
            return null;
        }

        fillSyncContactsList();
        storeSyncContacts();

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

        listener.onTaskCompleted("Синхронизация завершена.", MainActivity.ACTNS_BTNS);
    }

    private int findSimilarContact(String currentName, ArrayList<String> names) {
        String qwe = Translit.toTranslit(currentName);
        //В цикле пройтись по всем друзьям ВК и выяснить, есть ли контакт с таким же именем(phonebookName)
        for (int vkInd = 0; vkInd < names.size(); ++vkInd) {
            String[] components = names.get(vkInd).split(" ");
            int containsCount = 0;
            for( String component: components ) {
                if( currentName.contains(component) ||
                    qwe.contains(component))
                {
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

    private ArrayList<String> getContactNames(ArrayList<Contact> friends) {
        ArrayList<String> friendNames = new ArrayList<String>();
        for(Contact c: friends) {
            friendNames.add(c.getName());
        }
        return friendNames;
    }

    private void fillSyncContactsList() {
        ArrayList<String> vkNames = getContactNames(vkFriends);
        boolean []vkNamesUsed = new boolean[vkNames.size()];
        Arrays.fill(vkNamesUsed, false);

        ArrayList<String> fbNames = getContactNames(fbFriends);
        boolean []fbNamesUsed = new boolean[fbNames.size()];
        Arrays.fill(fbNamesUsed, false);

        Queue< Pair<String, String> > phonebookNames = phonebook.getContactNames();
        syncContacts.clear();

        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.onTaskBegin(MAPPING_CAPTION, phonebook.getContactNames().size(), new int[]{});
            }
        });

        while (!phonebookNames.isEmpty()) {
            //Создаём новый SyncContact. SyncContact.java для подробностей
            SyncContact item = new SyncContact();

            //получить текущее имя контакта с телефона
            Pair <String, String> pbItem = phonebookNames.remove();
            String currentName = pbItem.first;
            String currentNumber = pbItem.second;
            //добавить его в se
            item.setPhonebookName(currentName);
            item.setPhonebookMobileNumber(currentNumber);

            int pos = findSimilarContact(currentName, vkNames);
            if( pos != -1 ) {
                item.setVkContact(vkFriends.get(pos));
                vkNamesUsed[pos] = true;
            } else {
                item.setVkContact(null);
            }

            pos = findSimilarContact(currentName, fbNames);
            if( pos != -1 ) {
                item.setFbContact(fbFriends.get(pos));
                fbNamesUsed[pos] = true;
            } else {
                item.setFbContact(null);
            }

            //добавить item в синхронизированный список контактов
            syncContacts.add(item);
            listener.onTaskProgress(syncContacts.size());
        }

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.onTaskCompleted(OK, new int[]{});
            }
        });

        for( int i = 0; i < vkNamesUsed.length; ++i ) {
            if( !vkNamesUsed[i] ) {
                SyncContact item = new SyncContact();
                item.setPhonebookName("");
                item.setVkContact(vkFriends.get(i));
                item.setFbContact(null);
                syncContacts.add(item);
            }
        }

        for( int i = 0; i < fbNamesUsed.length; ++i ) {
            if( !fbNamesUsed[i] ) {
                SyncContact item = new SyncContact();
                item.setPhonebookName("");
                item.setVkContact(null);
                item.setFbContact(fbFriends.get(i));
                syncContacts.add(item);
            }
        }
    }

    void storeSyncContacts() {
        SQLiteDatabase db = handler.getWritableDatabase();
        if( db == null ) {
            return;
        }
        handler.dropTable(db);

        int id = 0;
        for(SyncContact sc: syncContacts) {
            ContentValues values = new ContentValues();
            values.put(DatabaseHandler.ID_COLUMN, id);
            values.put(DatabaseHandler.CONTACT, Serializer.serializeObject(sc));

            long resRowInd = db.insert(DatabaseHandler.TABLE_NAME, null, values);
            Log.e("INSERT QUERY ID", String.valueOf(resRowInd));
            id += 1;
        }
        db.close();
    }
}
