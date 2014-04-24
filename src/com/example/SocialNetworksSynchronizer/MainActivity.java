package com.example.SocialNetworksSynchronizer;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.*;
import com.facebook.*;
import com.vk.sdk.*;
import com.vk.sdk.api.*;

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

    Phonebook phonebook = new Phonebook(this);

    VkRequestThread vkThread = null;
    FbRequestThread fbThread = null;
    SyncContactsThread scThread = null;

    private ArrayList<Contact> vkFriends = new ArrayList<Contact>();
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

    //Синхронизация контактов(пока только с сетью ВК)
    private void sync() {
        ((Button)findViewById(R.id.vk_friends_button)).setEnabled(false);
        ((Button)findViewById(R.id.fb_friends_button)).setEnabled(false);
        ((Button)findViewById(R.id.phonebook_button)).setEnabled(false);

        this.setTitle("Синхронизация...");
        syncContacts.clear();

        scThread = new SyncContactsThread(vkFriends, fbFriends, syncContacts, phonebook);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    scThread.start();
                    scThread.join();
                } catch (InterruptedException e) {

                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.setTitle("Синхронизация успешно завершена!");
                        ((Button)findViewById(R.id.vk_friends_button)).setEnabled(true);
                        ((Button)findViewById(R.id.fb_friends_button)).setEnabled(true);
                        ((Button)MainActivity.this.findViewById(R.id.phonebook_button)).setEnabled(true);
                    }
                });
            }
        }).start();
    }

    public static ArrayList<String> getContactNames(ArrayList<Contact> friends) {
        ArrayList<String> friendNames = new ArrayList<String>();
        for(Contact c: friends) {
            friendNames.add(c.getName());
        }
        return friendNames;
    }

    //Отобразить список друзей ВК
    public void showFriends(final ArrayList <String> friendsList, final ArrayList<Contact> friends) {
        this.setTitle("Друзья"); /*TODO: add string with name of social network*/
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
    //Всё происходит через класс VkRequestTask
    private void performVkRequestAndShowResults() {
        this.setTitle("Получение списка друзей...");
        VKRequest request = Requests.vkFriendsRequest(Requests.LOCALE_RUS);

        vkThread = new VkRequestThread(vkFriends, request);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    vkThread.start();
                    vkThread.join();
                } catch (InterruptedException e) {

                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showFriends(getContactNames(vkFriends), vkFriends);
                    }
                });
            }
        }).start();
    }

    //Тоже самое для FB, в разработке
    private void performFbRequestAndShowResults() {
        this.setTitle("Получение списка друзей...");
        Request request = Requests.fbFriendsRequest();
        fbThread = new FbRequestThread(fbFriends, request);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    fbThread.start();
                    fbThread.join();
                } catch (InterruptedException e) {

                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showFriends(getContactNames(fbFriends), fbFriends);
                    }
                });
            }
        }).start();
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
            Queue<String> phoneContacts = phonebook.getContactNames();
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