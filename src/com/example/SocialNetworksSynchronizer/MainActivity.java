package com.example.SocialNetworksSynchronizer;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import com.facebook.*;
import com.facebook.model.GraphLocation;
import com.facebook.model.GraphMultiResult;
import com.facebook.model.GraphUser;
import com.vk.sdk.*;
import com.vk.sdk.api.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.*;

public class MainActivity extends FragmentActivity {
    private final String vkAppId = "4313814";   //Вконтакте, номер приложения
    private final String fbAppId = "1416447705289612";
    private VKAccessToken accessToken = null;

    public final String[] permissions = new String[] { //Разрешения для ВК: друзья и не использовать HTTPS
            VKScope.FRIENDS,
            VKScope.NOHTTPS
    };
    public static final String[] extras = new String[] { //названия параметров контакта пользователя для передачи в другую Activity
            "full_name",
            "phones",
            "address",
            "email"
    };
    private final String localeRus = "ru"; //Задать локаль для запросов в ВК, то есть все ответы вернутся кириллицей
    private final String localeEng = "en";

    private ArrayList<Contact> vkFriends = new ArrayList<Contact>(); //Список друзей ВК, который всегда обновляется
                                                                     //в ходе синхронизации, или при нажатии на кнопку Vk Friends

    private ArrayList<Contact> fbFriends = new ArrayList<Contact>();
    private ArrayList<SyncContact> syncContacts = new ArrayList<SyncContact>();   //SyncContact.java, синхронизированный список друзей

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);  //устанавливаем Layout для текущей активити (файл main_layout.xml)

        //Задаём обработчики события нажатия на кнопку для каждой кнопки
        final Button syncButton = (Button)findViewById(R.id.sync_button);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sync();
            }
        });

        final Button vkButton = (Button)findViewById(R.id.vk_friends_button);
        vkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performVkRequestAndShowResults();
            }
        });

        final Button fbButton = (Button)findViewById(R.id.fb_friends_button);
        fbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performFbRequestAndShowResults();
            }
        });

        final Button phonebookButton = (Button)findViewById(R.id.phonebook_button);
        phonebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makePhonebook();
            }
        });

        final Button logoutButton = (Button)findViewById(R.id.logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VKSdk.logout();
                finish();
            }
        });

        //вызываем функцию для авторизации пользователя в ВК и FB
        init();
    }

    //Следующие 3 метода нужны для корректной работы с VkSDK и FacebookSDK
    @Override
    protected void onResume() {
        super.onResume();
        VKUIHelper.onResume(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VKUIHelper.onDestroy(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        VKUIHelper.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    //Переопределение стандартного метода openActiveSession FacebookSDK. Сделано с целью получения дополнительных разрешений
    //для данного приложения, чтобы получить дополнительную информацию для пользователя
    private Session openActiveSession(Activity activity, boolean allowLoginUI, List<String> permissions, Session.StatusCallback callback) {
        Session.OpenRequest openRequest = new Session.OpenRequest(activity).setPermissions(permissions).setCallback(callback);
        Session session = new Session.Builder(activity).build();
        if (SessionState.CREATED_TOKEN_LOADED.equals(session.getState()) || allowLoginUI) {
            Session.setActiveSession(session);
            session.openForRead(openRequest);
            return session;
        }
        return null;
    }

    private void init() {
        //Авторизация в Вконтакте
        VKUIHelper.onCreate(this);
        VKSdk.initialize(sdkListener, vkAppId);
        VKSdk.authorize(permissions);

        //авторизация в Facebook
        Session session = openActiveSession(this, true, Arrays.asList("friends_hometown, friends_location"),
            new Session.StatusCallback() {
                @Override
                public void call(Session session, SessionState state, Exception exception) {
                    if (session.isOpened()) {}
                }
            });
    }

    //стандартный Listener(обрабочик событий) для VkSDK
    private final VKSdkListener sdkListener = new VKSdkListener() {
        @Override
        public void onCaptchaError(VKError captchaError) {
            new VKCaptchaDialog(captchaError).show();
        }

        @Override
        public void onTokenExpired(VKAccessToken expiredToken) {
            VKSdk.authorize(permissions);
        }

        @Override
        public void onAccessDenied(VKError authorizationError) {
            //((ListView)findViewById(R.id.lv)).(authorizationError.apiError.errorMessage);
        }

        @Override
        public void onReceiveNewToken(VKAccessToken newToken) {
            accessToken = newToken;
            //((ListView)findViewById(R.id.lv)).setText("Successful authorization");
        }

        @Override
        public void onAcceptUserToken(VKAccessToken token) {

        }
    };


    //функция формирования запроса для получения информации о друзьях в ВК
    //возвращает сформированный запрос
    private VKRequest getFriendsRequest(String locale){
        VKRequest request = VKApi.friends().get(VKParameters.from(VKApiConst.USER_IDS, "",
                                                                    "order", "name",
                                                                    VKApiConst.FIELDS, "city,country,contacts"));
        request.secure = false;
        request.setPreferredLang(locale);
        return request;
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

        return getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
    }

    //Возвращает список имём контактов, имеющихся на устройстве
    private Queue<String> getPhoneContacts() {
        Cursor cursor = getContacts();

        Queue<String> contactNames = new LinkedList<String>();
        while(cursor.moveToNext()) {

            String displayName = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
            contactNames.add(displayName);
        }
        return contactNames;
    }

    //Синхронизация контактов(пока только с сетью ВК)
    private void sync() {
        this.setTitle("Синхронизация...");
        syncContacts.clear();


        VKRequest request = getFriendsRequest(localeRus); //Получаем запрос для ВК, см. выше функцию getFriendsRequest
        request.executeWithListener(new VKRequest.VKRequestListener() { //отправляем запрос на сервер ВК
            @Override
            public void onComplete(VKResponse response) { //При удачном завершении запроса
                super.onComplete(response);

                try {
                    ArrayList<String> vkFriendsList = makeVkFriendsList(response); //Получить список друзей ВК, т.е. заполнить список vkFriends
                    Queue<String> phonebookNames = getPhoneContacts(); //Получить список друзей с устройства

                    //Следующий цикл находит схожие контакты в ВК и на устройстве, путём сравнивания имён пользователей
                    while (!phonebookNames.isEmpty()) {
                        //Создаём новый SyncContact. SyncContact.java для подробностей
                        SyncContact item = new SyncContact();

                        //получить текущее имя контакта с телефона
                        String phonebookName = phonebookNames.remove();
                        //добавить его в item
                        item.setPhonebookName(phonebookName);

                        //В цикле пройтись по всем друзьям ВК и выяснить, есть ли контакт с таким же именем(phonebookName)
                        for (int vkInd = 0; vkInd < vkFriendsList.size(); ++vkInd) {
                            String[] components = vkFriendsList.get(vkInd).split(" ");
                            int containsCount = 0;
                            for (int compInd = 0; compInd < components.length; ++compInd) {
                                if (phonebookName.contains(components[compInd])) {
                                    ++containsCount;
                                }
                            }

                            //Если схожий контакт найден, то добавить его в item
                            if (containsCount == components.length) {
                                item.setVkContact(vkFriends.get(vkInd));
                                break;
                            }
                        }

                        //если схожий контакт не найден, то установить схожий контакт в ВК или FB как null, т.е. не нашли нужного пользователя
                        if (item.getVkName().length() == 0) {
                            item.setVkContact(null);
                        }
                        if (item.getFbName().length() == 0) {
                            item.setFbContact(null);
                        }

                        //добавить item в синхронизированный список контактов
                        syncContacts.add(item);
                    }
                    MainActivity.this.setTitle("Синхронизация успешно завершена!");
                } catch (JSONException e) {

                }
            }
        });
    }

    //Проверить номер-телефона
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

    //Заполнить/обновить список друзей ВК
    private ArrayList<String> makeVkFriendsList(VKResponse response) throws JSONException {
        //Получить результат ответа сервера в виде JSONArray
        JSONArray res = response.json.getJSONObject("response").getJSONArray("items");
        ArrayList<String> friendsList = new ArrayList<String>();

        //Для каждого элемента массива res(который по сути представляет собой список друзей, но в неудобном для нас виде)
        //получить структуру Contact, т.е. контакт ВК
        for( int i = 0; i < res.length(); ++i ) {
            //Получить имя и фамилию
            String fullName = res.getJSONObject(i).getString("first_name") + ' ' +
                              res.getJSONObject(i).getString("last_name");
            //добавить в friendsList
            friendsList.add(fullName);

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

            //Формируем новый Contact используя fullName, mobilePhone, homePhone и address
            //Подробно в файле Contact.java
            Contact contact = new Contact(new String[]{fullName, mobilePhone, homePhone, address});
            vkFriends.add(contact);
        }
        return friendsList;
    }

    //Отобразить список друзей ВК
    private void showFriends(final ArrayList <String> friendsList, final ArrayList<Contact> friends) {
        this.setTitle("Друзья ВК"); /*TODO: add string with name of social network*/
        //Список, в котоый будем выводить имена друзей
        ListView lv = ((ListView)findViewById(R.id.lv));

        //задаём данные для данного списка, т.е. чем список будет заполняться
        ArrayAdapter<String> friendsAdapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.li, friendsList);
        lv.setAdapter(friendsAdapter);

        //если пользователь кликает на какой-либо элемент списка, то предоставить подробную информацию о данном пользователе
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Получаем Contact соответствующий выбранному пользователю ВК
                Contact selectedContact = friends.get(i);

                //Формируем параметры для другой Activity(для отображения подробной информации о пользователе ВК
                Intent intent = new Intent(MainActivity.this, FriendInfoActivity.class);
                intent.putExtra(extras[0], selectedContact.getName());
                intent.putExtra(extras[1], selectedContact.getMobilePhone() + '\n' + selectedContact.getHomePhone());
                intent.putExtra(extras[2], selectedContact.getAddress());
                intent.putExtra(extras[3], selectedContact.getEmail());

                //открыть ещё одну Activity, FriendInfoActivity
                startActivity(intent);
            }
        });
    }

    //Сделать запрос на сервер ВК для получения списка друзей и отобразить результаты в ListView
    private void performVkRequestAndShowResults() {
        VKRequest request = getFriendsRequest(localeRus);

        //Делаем запрос
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                //В результате запроса получаем из ответа список друзей в удобном виде и выводим имена друзей в ListView
                try {
                    ArrayList<String> friendsList = makeVkFriendsList(response);
                    showFriends(friendsList, vkFriends);
                } catch (JSONException e) {
                    ((ListView) findViewById(R.id.lv)).setAdapter(
                            new ArrayAdapter<String>(MainActivity.this,
                            R.layout.list_item,
                            R.id.li,
                            new String[]{e.toString()}));
                }
            }
            //Обработка ошибки в случае неудачного результата
            @Override
            public void onError(VKError error) {
                if (error.apiError != null)
                    ((ListView) findViewById(R.id.lv)).setAdapter(
                            new ArrayAdapter<String>(MainActivity.this,
                            R.layout.list_item,
                            R.id.li,
                            new String[]{error.errorMessage}));
                else {
                    ((ListView) findViewById(R.id.lv)).setAdapter(
                            new ArrayAdapter<String>(MainActivity.this,
                            R.layout.list_item,
                            R.id.li,
                            new String[]{String.format("Error %d: %s", error.errorCode, error.errorMessage)}));
                }
            }
        });
    }

    private ArrayList<String> makeFbFriendsList(Response response) {
        List<GraphUser> friends = response.getGraphObjectAs(GraphMultiResult.class).
                getData().castToListOf(GraphUser.class);

        ArrayList<String> friendsList = new ArrayList<String>();
        for (GraphUser gu : friends) {
            String fullName = gu.getName();
            friendsList.add(fullName);

            GraphLocation gl = gu.getLocation();
            String address = "";
            if (gl != null) {
                address = gu.getLocation().getProperty("name").toString();
            }
            String email = "E-mail не указан";
            if( gu.getProperty("email") != null ) {
                email = gu.getProperty("email").toString();
            }

            Contact contact = new Contact(new String[]{ fullName, address, email });
            contact.setMobilePhone("Facebook не предоставляет данную информацию");
            fbFriends.add(contact);
        }
        return friendsList;
    }

    //Тоже самое для FB, в разработке
    private void performFbRequestAndShowResults() {
        Request request = Request.newGraphPathRequest(Session.getActiveSession(), "me/friends", null);
        Set<String> fields = new HashSet<String>();
        String[] requiredFields = new String[] { "name, location, email" };
        fields.addAll(Arrays.asList(requiredFields));

        Bundle parameters = request.getParameters();
        parameters.putString("fields", TextUtils.join(",", fields));
        request.setParameters(parameters);

        request.setCallback(new Request.Callback() {
            @Override
            public void onCompleted(Response response) {
                ArrayList<String> friendsList = makeFbFriendsList(response);
                showFriends(friendsList, fbFriends);
            }
        });
        request.executeAsync();
    }

    //Отобразить синхронизированный список контактов(пока только с ВК)
    private void makePhonebook() {
        this.setTitle("Телефонная книга");
        //Отображение контактов происходит в ListView
        ListView lv = ((ListView)findViewById(R.id.lv));
        lv.setClickable(false);
        //Проверяем, заполнен ли синхронизированный список контактов(была ли произведена синхронизация)
        if( syncContacts.isEmpty() ) { //если нет, то
            this.setTitle("Телефонная книга(без синхронизации)");

            //просто выводим весь список контактов с устройства
            //заполняем синхронизированный список контактов только именами контактов с устройства
            Queue<String> phoneContacts = getPhoneContacts();
            while( !phoneContacts.isEmpty() ) {
                String pbName = phoneContacts.remove();
                SyncContact item = new SyncContact(pbName, null, null);
                syncContacts.add(item);
            }
        }
        //Создаём адаптер для отображения данных в ListView
        ContactArrayAdapter adapter = new ContactArrayAdapter(this, syncContacts);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(null);
    }
}