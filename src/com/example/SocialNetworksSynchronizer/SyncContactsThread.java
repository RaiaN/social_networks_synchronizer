package com.example.SocialNetworksSynchronizer;

import java.util.ArrayList;
import java.util.Queue;

public class SyncContactsThread extends Thread {
    private ArrayList<Contact> vkFriends = null;
    private ArrayList<Contact> fbFriends = null;
    private ArrayList<SyncContact> syncContacts = null;
    private Phonebook phonebook = null;

    private VkRequestThread vkThread = null;
    private FbRequestThread fbThread = null;

    SyncContactsThread(ArrayList<Contact> vkFriends, ArrayList<Contact> fbFriends,
                     ArrayList<SyncContact> syncContacts, Phonebook phonebook) {
        this.vkFriends = vkFriends;
        this.fbFriends = fbFriends;
        this.syncContacts = syncContacts;
        this.phonebook = phonebook;
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

    @Override
    public synchronized void start() {
        super.start();
        vkThread = new VkRequestThread(vkFriends, Requests.vkFriendsRequest(Requests.LOCALE_RUS));
        fbThread = new FbRequestThread(fbFriends, Requests.fbFriendsRequest());

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    vkThread.start();
                    fbThread.start();
                    vkThread.join();
                    fbThread.join();
                    fillSyncContactsList();
                } catch (InterruptedException e) {

                }
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {

        }
    }
}
