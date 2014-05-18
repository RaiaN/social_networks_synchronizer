package com.example.SocialNetworksSynchronizer;

import com.vk.sdk.api.VKResponse;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

public class ParseVkResponseTask extends ParseResponseTask {
    private String VK_START_CAPTION = "Получение списка друзей ВК...";
    private String VK_END_CAPTION   = "Друзья ВК";

    private VKResponse response = null;

    ParseVkResponseTask(ArrayList<Contact> friends, AsyncTaskListener listener, VKResponse response) {
        super(friends, listener);
        this.response = response;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            makeVkFriendsList();
        } catch( JSONException e ) {
            e.printStackTrace();
            this.cancel(true);
            return null;
        }

        return super.doInBackground(voids);
    }

    @Override
    protected void onPreExecute() {
        try {
            maxValue = response.json.getJSONObject("response").getJSONArray("items").length() + 1;
        } catch( JSONException je ) {
            je.printStackTrace();
        }
        caption = VK_START_CAPTION;
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void v) {
        caption = VK_END_CAPTION;
        super.onPostExecute(v);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
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
            birthday = res.getJSONObject(i).getString(Requests.VK_BIRTHDATE);
        }

        String mobilePhone = "";
        //Проверить наличие мобильного телефона
        //Если он существует, то проверить его корректность
        if( res.getJSONObject(i).has(Requests.VK_MOBILE_PHONE) &&
                res.getJSONObject(i).getString(Requests.VK_MOBILE_PHONE).length() > 0 &&
                correctPhoneNumber(res.getJSONObject(i).getString(Requests.VK_MOBILE_PHONE)) )
        {
            mobilePhone = res.getJSONObject(i).getString(Requests.VK_MOBILE_PHONE);
        }

        //Проверить наличие домашнего телефона
        //Если он существует, то проверить его корректность
        String homePhone = "";
        if( res.getJSONObject(i).has(Requests.VK_HOME_PHONE) &&
                res.getJSONObject(i).getString(Requests.VK_HOME_PHONE).length() > 0 &&
                correctPhoneNumber(res.getJSONObject(i).getString(Requests.VK_HOME_PHONE)) )
        {
            homePhone = res.getJSONObject(i).getString(Requests.VK_HOME_PHONE);
        }

        //Получить адрес, если он указан
        String address = "";
        if( res.getJSONObject(i).has(Requests.VK_COUNTRY) ) {
            String country = res.getJSONObject(i).getJSONObject(Requests.VK_COUNTRY).getString(Requests.VK_TITLE);
            String temp = country;
            if( temp.replaceAll(" ", "").length() > 0 ) {
                address = country;
                if( res.getJSONObject(i).has(Requests.VK_CITY) ) {
                    String city = res.getJSONObject(i).getJSONObject(Requests.VK_CITY).getString(Requests.VK_TITLE);
                    temp = city;
                    if( temp.replaceAll(" ", "").length() > 0 ) {
                        address += ", " + city;
                    }
                }
            }
        }

        String skype = "";
        if( res.getJSONObject(i).has(Requests.VK_SKYPE) &&
                res.getJSONObject(i).getString(Requests.VK_SKYPE).length() > 0 ) {
            skype += res.getJSONObject(i).getString(Requests.VK_SKYPE);
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

        String education = "";
        if( res.getJSONObject(i).has(Requests.VK_UNIVERISTY) &&
                res.getJSONObject(i).getString(Requests.VK_UNIVERISTY).length() > 0 ) {
            education = res.getJSONObject(i).getString(Requests.VK_UNIVERISTY);
            if( res.getJSONObject(i).has(Requests.VK_FACULTY) &&
                    res.getJSONObject(i).getString(Requests.VK_FACULTY).length() > 0 ) {
                education += ", " + res.getJSONObject(i).getString(Requests.VK_FACULTY);
            }
        }

        HashMap<String,String> contactInfo = new HashMap<String, String>();
        contactInfo.put(Contact.PHOTO_URL, photoUrl);
        contactInfo.put(Contact.NAME, fullName);
        contactInfo.put(Contact.BIRTHDAY, birthday);
        contactInfo.put(Contact.MOBILE_PHONE, mobilePhone);
        contactInfo.put(Contact.HOME_PHONE, homePhone);
        contactInfo.put(Contact.ADDRESS, address);
        contactInfo.put(Contact.SKYPE, skype);
        contactInfo.put(Contact.TWITTER, twitter);
        contactInfo.put(Contact.INSTAGRAM, instagram);
        contactInfo.put(Contact.EDUCATION, education);

        Contact contact = new Contact(contactInfo);
        contact.setImage(loadImage(photoUrl));

        return contact;
    }

    public void makeVkFriendsList() throws JSONException {
        friends.clear();
        //Получить результат ответа сервера в виде JSONArray
        JSONArray res = response.json.getJSONObject("response").getJSONArray("items");

        //Для каждого элемента массива res(который по сути представляет собой список друзей, но в неудобном для нас виде)
        //получить структуру Contact, т.е. контакт ВК
        for( int i = 0; i < res.length(); ++i ) {
            //Формируем новый Contact используя fullName, mobilePhone, homePhone и address
            //Подробно в файле Contact.java
            Contact contact = getVkContact(res, i);
            friends.add(contact);
            publishProgress(i+1);
        }
    }
}
