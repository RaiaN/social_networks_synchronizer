package com.example.SocialNetworksSynchronizer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import com.facebook.Request;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VkRequestThread extends Thread {
    private ArrayList<Contact> vkFriends = null;
    private VKRequest request = null;
    private boolean finished = false;

    VkRequestThread(ArrayList<Contact> vkFriends, VKRequest request) {
        this.vkFriends = vkFriends;
        this.request = request;
    }

    //Проверить номер телефона
    private boolean correctPhoneNumber(String number) {
        //убрать все нецифровые символы и символ + из номера телефона
        String modifiedNumber = number.replaceAll("[^0-9\\+]","");

        //если в результате длина номера оказалась равной 0, то считается, что данный телефонный номер некорректный
        if( modifiedNumber.length() == 0) {
            return false;
        }
        //если номер телефона содержит какой-либо нецифровой символ, то считается, что данный телефонный номер некорректный
        for( int i = 0; i < modifiedNumber.length(); ++i ) {
            if( !Character.isDigit(modifiedNumber.charAt(i)) ) {
                return false;
            }
        }

        //иначе номер телефона корректный
        return true;
    }

    private Contact getVkContact(JSONArray res, int i) throws JSONException {
        String photoUrl = "";
        if( res.getJSONObject(i).has(Requests.VK_PHOTO) &&
                res.getJSONObject(i).getString(Requests.VK_PHOTO).length() > 0 ) {
            photoUrl = res.getJSONObject(i).getString(Requests.VK_PHOTO);
        }

        String fullName = res.getJSONObject(i).getString(Requests.VK_FIRST_NAME) + ' ' +
                res.getJSONObject(i).getString(Requests.VK_LAST_NAME);

        String birthday = "";
        if( res.getJSONObject(i).has(Requests.VK_BIRTHDATE) &&
            res.getJSONObject(i).getString(Requests.VK_BIRTHDATE).length() > 0 ) {
            birthday = "День рождения: \n" + res.getJSONObject(i).getString(Requests.VK_BIRTHDATE);
        }

        String mobilePhone = "";
        //Проверить наличие мобильного телефона
        //Если он существует, то проверить его корректность
        if( res.getJSONObject(i).has(Requests.VK_MOBILE_PHONE) &&
                res.getJSONObject(i).getString(Requests.VK_MOBILE_PHONE).length() > 0 &&
                correctPhoneNumber(res.getJSONObject(i).getString(Requests.VK_MOBILE_PHONE)) )
        {
            mobilePhone = "Мобильный телефон:\n" + res.getJSONObject(i).getString(Requests.VK_MOBILE_PHONE);
        }

        //Проверить наличие домашнего телефона
        //Если он существует, то проверить его корректность
        String homePhone = "";
        if( res.getJSONObject(i).has(Requests.VK_HOME_PHONE) &&
                res.getJSONObject(i).getString(Requests.VK_HOME_PHONE).length() > 0 &&
                correctPhoneNumber(res.getJSONObject(i).getString(Requests.VK_HOME_PHONE)) )
        {
            homePhone = "Домашний телефон:\n" + res.getJSONObject(i).getString(Requests.VK_HOME_PHONE);
        }

        //Получить адрес, если он указан
        String address = "";
        if( res.getJSONObject(i).has(Requests.VK_COUNTRY) ) {
            address += res.getJSONObject(i).getJSONObject(Requests.VK_COUNTRY).getString(Requests.VK_TITLE);
            if( res.getJSONObject(i).has(Requests.VK_CITY) ) {
                address += ", " + res.getJSONObject(i).getJSONObject(Requests.VK_CITY).getString(Requests.VK_TITLE);
            }
        } else {
            address += "Адрес не указан";
        }

        String skype = "";
        if( res.getJSONObject(i).has(Requests.VK_SKYPE) &&
                res.getJSONObject(i).getString(Requests.VK_SKYPE).length() > 0 ) {
            skype = res.getJSONObject(i).getString(Requests.VK_SKYPE);
        }
        String twitter = "";
        if( res.getJSONObject(i).has(Requests.VK_TWITTER) &&
                res.getJSONObject(i).getString(Requests.VK_TWITTER).length() > 0 ) {
            twitter = res.getJSONObject(i).getString(Requests.VK_TWITTER);
        }
        String instagram = "";
        if( res.getJSONObject(i).has(Requests.VK_INSTAGRAM) &&
                res.getJSONObject(i).getString(Requests.VK_INSTAGRAM).length() > 0 ) {
            instagram = res.getJSONObject(i).getString(Requests.VK_INSTAGRAM);
        }

        String university = "";
        if( res.getJSONObject(i).has(Requests.VK_UNIVERISTY) &&
                res.getJSONObject(i).getString(Requests.VK_UNIVERISTY).length() > 0 ) {
            university = res.getJSONObject(i).getString(Requests.VK_UNIVERISTY);
        }
        String faculty = "";
        if( res.getJSONObject(i).has(Requests.VK_FACULTY) &&
                res.getJSONObject(i).getString(Requests.VK_FACULTY).length() > 0 ) {
            faculty = res.getJSONObject(i).getString(Requests.VK_FACULTY);
        }

        HashMap <String,String> contactInfo = new HashMap<String, String>();
        contactInfo.put("photoUrl", photoUrl);
        contactInfo.put("name", fullName);
        contactInfo.put("birthday", birthday);
        contactInfo.put("mobilePhone", mobilePhone);
        contactInfo.put("homePhone", homePhone);
        contactInfo.put("address", address);
        contactInfo.put("skype", skype);
        contactInfo.put("twitter", twitter);
        contactInfo.put("instagram", instagram);
        contactInfo.put("university", university);
        contactInfo.put("faculty", faculty);

        Contact contact = new Contact(contactInfo);
        contact.setBirthday(birthday);

        return contact;
    }

    //Заполнить/обновить список друзей ВК
    public void makeVkFriendsList(VKResponse response) throws JSONException {
        vkFriends.clear();
        //Получить результат ответа сервера в виде JSONArray
        JSONArray res = response.json.getJSONObject("response").getJSONArray("items");

        //Для каждого элемента массива res(который по сути представляет собой список друзей, но в неудобном для нас виде)
        //получить структуру Contact, т.е. контакт ВК
        for( int i = 0; i < res.length(); ++i ) {
            //Формируем новый Contact используя fullName, mobilePhone, homePhone и address
            //Подробно в файле Contact.java
            Contact contact = getVkContact(res, i);
            vkFriends.add(contact);
        }

        List <String> urls = new ArrayList<String>();
        for(Contact contact: vkFriends) {
            urls.add(contact.getPhotoUrl());
        }

        List<byte[]> avatars = new ArrayList<byte[]>();
        AsyncTask at = new DownloadImageTask(avatars).execute(TextUtils.join(",", urls));
        while( at.getStatus() != AsyncTask.Status.FINISHED ) {}
    }

    @Override
    public synchronized void start() {
        super.start();
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                try {
                    makeVkFriendsList(response);
                    finished = true;
                } catch (JSONException e ) {

                }
            }
        });
        while( !finished ) {}
    }
}
