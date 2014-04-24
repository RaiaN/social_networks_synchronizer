package com.example.SocialNetworksSynchronizer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.LinkedList;
import java.util.Queue;

public class Phonebook {
    private Context context = null;

    Phonebook(Context context) {
        this.context = context;
    }

    //Получение контактов из самого Андроида через внутренную БД устройства
    private Cursor getContacts() {
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String[] projection = new String[] { ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME };
        String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = 1";
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
                + " COLLATE LOCALIZED ASC";

        return context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
    }

    //Возвращает список имём контактов, имеющихся на устройстве
    public Queue<String> getContactNames() {
        Cursor cursor = getContacts();

        Queue<String> contactNames = new LinkedList<String>();
        while(cursor.moveToNext()) {

            String displayName = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
            contactNames.add(displayName);
        }
        return contactNames;
    }
}
