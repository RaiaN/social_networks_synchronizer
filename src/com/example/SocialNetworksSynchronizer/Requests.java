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

    //функция формирования запроса для получения информации о друзьях в ВК
    //возвращает сформированный запрос
    public static VKRequest vkFriendsRequest(String locale){
        VKRequest request = VKApi.friends().get(VKParameters.from(VKApiConst.USER_IDS, "",
                "order", "name",
                VKApiConst.FIELDS, "city,country,contacts"));
        request.secure = false;
        request.setPreferredLang(locale);
        return request;
    }

    public static Request fbFriendsRequest() {
        Request request = Request.newGraphPathRequest(Session.getActiveSession(), "me/friends", null);
        Set<String> fields = new HashSet<String>();
        String[] requiredFields = new String[] { "name, location, email" };
        fields.addAll(Arrays.asList(requiredFields));

        Bundle parameters = request.getParameters();
        parameters.putString("fields", TextUtils.join(",", fields));
        request.setParameters(parameters);

        return request;
    }
}
