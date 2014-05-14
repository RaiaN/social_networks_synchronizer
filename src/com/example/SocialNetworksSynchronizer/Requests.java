package com.example.SocialNetworksSynchronizer;

import android.os.Bundle;
import android.text.TextUtils;
import com.facebook.Request;
import com.facebook.Session;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Requests {
    public static String LOCALE_RUS = "ru"; //Задать локаль для запросов в ВК, то есть все ответы вернутся кириллицей
    public static String LOCALE_ENG = "en";

    public static String VK_TITLE        = "title";
    public static String VK_FIRST_NAME   = "first_name";
    public static String VK_LAST_NAME    = "last_name";
    public static String VK_PHOTO        = "photo_50";
    public static String VK_BIRTHDATE    = "bdate";
    public static String VK_COUNTRY      = "country";
    public static String VK_CITY         = "city";
    public static String VK_CONTACTS     = "contacts";
    public static String VK_SERVICES     = "connections";
    public static String VK_SKYPE        = "skype";
    public static String VK_TWITTER      = "twitter";
    public static String VK_INSTAGRAM    = "instagram";
    public static String VK_EDUCATION    = "education";
    public static String VK_UNIVERISTY   = "university_name";
    public static String VK_FACULTY      = "faculty_name";
    public static String VK_MOBILE_PHONE = "mobile_phone";
    public static String VK_HOME_PHONE   = "home_phone";

    public static String []VK_ALL = new String[]{ VK_BIRTHDATE, VK_COUNTRY, VK_CITY,
                                                  VK_CONTACTS, VK_SERVICES, VK_EDUCATION };
    public static String VK_REQUEST_FIELDS = VK_PHOTO + "," + TextUtils.join(",", VK_ALL);

    public static String FB_NAME      = "name";
    public static String FB_PHOTO     = "picture";
    public static String FB_BIRTHDATE = "birthday";
    public static String FB_EDUCATION = "education";
    public static String FB_ADDRESS   = "location";
    public static String FB_WORK      = "work";

    public static String []FB_ALL = new String[]{ FB_BIRTHDATE, FB_EDUCATION, FB_ADDRESS, FB_WORK };
    public static String FB_REQUEST_FIELDS = FB_NAME + "," + FB_PHOTO + "," + TextUtils.join(",", FB_ALL);

    //функция формирования запроса для получения информации о друзьях в ВК
    //возвращает сформированный запрос
    public static VKRequest vkFriendsRequest(String locale){
        VKRequest request = VKApi.friends().get(VKParameters.from(VKApiConst.USER_IDS, "",
                "order", "name",
                VKApiConst.FIELDS, VK_REQUEST_FIELDS));
        //
        request.secure = false;
        request.setPreferredLang(locale);
        return request;
    }

    public static Request fbFriendsRequest() {
        Request request = Request.newGraphPathRequest(Session.getActiveSession(), "/v1.0/me/friends", null);
        Set<String> fields = new HashSet<String>();
        String[] requiredFields = new String[] { FB_REQUEST_FIELDS };

        fields.addAll(Arrays.asList(requiredFields));

        Bundle parameters = request.getParameters();
        parameters.putString("fields", TextUtils.join(",", fields));
        request.setParameters(parameters);

        return request;
    }
}
