package com.example.SocialNetworksSynchronizer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract;
import android.util.Pair;

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
                                             ContactsContract.Contacts.DISPLAY_NAME,
                                             ContactsContract.Contacts.PHOTO_FILE_ID,
                                             ContactsContract.Contacts.HAS_PHONE_NUMBER };

        String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = 1";
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
                + " COLLATE LOCALIZED ASC";

        if( uri == null ) {
            return null;
        }

        return context.getContentResolver().query(uri, projection, selection, null, sortOrder);
    }

    //Возвращает список имём контактов, имеющихся на устройстве
    public Queue <Pair<String, String>> getContactNames() {
        Queue <Pair<String, String>> contactNames = new LinkedList<Pair<String, String>>();

        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        if( uri == null ) {
            return contactNames;
        }

        Cursor cursor = getContacts();

        while(cursor.moveToNext()) {
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            String hasNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

            String mobileNumber = "";
            if( hasNumber != null && hasNumber.compareTo("0") != 0 ) {
                Cursor phones = context.getContentResolver().query(Phone.CONTENT_URI,
                        new String[]{Phone.NUMBER, Phone.TYPE}, ContactsContract.Data.CONTACT_ID + "=?",
                        new String[]{String.valueOf(contactId)}, null);

                while( phones != null && phones.moveToNext() ) {
                    mobileNumber = phones.getString(phones.getColumnIndex(Phone.NUMBER));
                    int type = phones.getInt(phones.getColumnIndex(Phone.TYPE));
                    if( type == Phone.TYPE_MOBILE ) {
                        break;
                    }
                }
            }

            String displayName = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Data.DISPLAY_NAME));

            contactNames.add(new Pair<String, String>(displayName, mobileNumber));
        }
        return contactNames;
    }
}
