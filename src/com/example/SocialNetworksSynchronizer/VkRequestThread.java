package com.example.SocialNetworksSynchronizer;

import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

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
        //Получить имя и фамилию
        String fullName = res.getJSONObject(i).getString("first_name") + ' ' +
                res.getJSONObject(i).getString("last_name");
        //добавить в friendsList

        String mobilePhone = "Мобильный телефон не указан";
        //Проверить наличие мобильного телефона
        //Если он существует, то проверить его корректность
        if( res.getJSONObject(i).has("mobile_phone") &&
                res.getJSONObject(i).getString("mobile_phone").length() > 0 &&
                correctPhoneNumber(res.getJSONObject(i).getString("mobile_phone")) )
        {
            mobilePhone = "Мобильный телефон:\n" + res.getJSONObject(i).getString("mobile_phone");
        }

        //Проверить наличие домашнего телефона
        //Если он существует, то проверить его корректность
        String homePhone = "Домашний телефон не указан";
        if( res.getJSONObject(i).has("home_phone") &&
                res.getJSONObject(i).getString("home_phone").length() > 0 &&
                correctPhoneNumber(res.getJSONObject(i).getString("home_phone")) )
        {
            homePhone = "Домашний телефон:\n" + res.getJSONObject(i).getString("home_phone");
        }

        //Получить адрес, если он указан
        String address = "";
        if( res.getJSONObject(i).has("country") ) {
            address += res.getJSONObject(i).getJSONObject("country").getString("title");
            if( res.getJSONObject(i).has("city") ) {
                address += ", " + res.getJSONObject(i).getJSONObject("city").getString("title");
            }
        } else {
            address += "Адрес не указан";
        }
        return new Contact(new String[]{fullName, mobilePhone, homePhone, address});
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
