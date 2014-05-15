package com.example.SocialNetworksSynchronizer;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.*;
import com.facebook.*;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.vk.sdk.*;
import com.vk.sdk.api.*;

import java.util.*;

public class MainActivity extends FragmentActivity {
    private final String vkAppId = "4313814";   //Вконтакте, номер приложения
    //private final String fbAppId = "1416447705289612";
    //private VKAccessToken accessToken = null;

    private ImageLoader loader = null;
    private LruCache<String,Bitmap> bitmapCache = null;

    public final String[] permissions = new String[] { //Разрешения для ВК: друзья и не использовать HTTPS
            VKScope.FRIENDS,
            VKScope.NOHTTPS
    };

    private final int SYNC_BTN_IND = 0; //кнопка Sync
    private final int VK_BTN_IND   = 1; //кнопка VK friends
    private final int FB_BTN_IND   = 2; //кнопка FB friends
    private final int PB_BTN_IND   = 3; //кнопка Phonebook
    private final int ST_BTN_IND   = 4; //кнопка Settings
    private final int[] ACTNS_BTNS = new int[] { SYNC_BTN_IND, VK_BTN_IND, FB_BTN_IND, ST_BTN_IND };

    private Button[] buttons = new Button[ST_BTN_IND+1];

    private Phonebook phonebook = new Phonebook(this);
    private final DatabaseHandler handler = new DatabaseHandler(this);

    private VkRequestThread vkThread = null;
    private FbRequestThread fbThread = null;
    private SyncContactsThread scThread = null;

    private ArrayList<Contact> vkFriends = new ArrayList<Contact>();
    private ArrayList<Contact> fbFriends = new ArrayList<Contact>();
    private ArrayList<SyncContact> syncContacts = new ArrayList<SyncContact>();   //SyncContact.java, синхронизированный список друзей

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);  //устанавливаем Layout для текущей активити (файл main_layout.xml)

        buttons[SYNC_BTN_IND] = (Button)findViewById(R.id.sync_button);
        buttons[VK_BTN_IND] = (Button)findViewById(R.id.vk_friends_button);
        buttons[FB_BTN_IND] = (Button)findViewById(R.id.fb_friends_button);
        buttons[PB_BTN_IND] = (Button)findViewById(R.id.phonebook_button);
        buttons[ST_BTN_IND] = (Button)findViewById(R.id.settings_button);

        //Задаём обработчики события нажатия на кнопку для каждой кнопки
        ((Button)findViewById(R.id.sync_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sync();
            }
        });
        ((Button)findViewById(R.id.vk_friends_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performVkRequestAndShowResults();
            }
        });
        ((Button)findViewById(R.id.fb_friends_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performFbRequestAndShowResults();
            }
        });
        ((Button)findViewById(R.id.phonebook_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makePhonebook();
            }
        });
        ((Button)findViewById(R.id.settings_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSettings();
            }
        });
        ((Button)findViewById(R.id.logout_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VKSdk.logout();
                finish();
            }
        });

        //вызываем функцию для авторизации пользователя в ВК и FB

        init();
        readSyncContactsFromDb();
        prepareImageLoader();
        prepareCache();
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

    private void prepareImageLoader() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                                      .cacheInMemory(true)
                                      .cacheOnDisc(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).imageDownloader(new CacheImageDownloader())
                                          .defaultDisplayImageOptions(options).build();
        ImageLoader.getInstance().init(config);
        loader = ImageLoader.getInstance();
    }

    private void prepareCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        bitmapCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

    }

    private void readSyncContactsFromDb() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                changeButtonsState(ACTNS_BTNS, false);
                SQLiteDatabase db = handler.getReadableDatabase();
                handler.onCreate(db);
                String[] projection = {
                        DatabaseHandler.ID_COLUMN,
                        DatabaseHandler.CONTACT
                };

                try {
                    Cursor cursor = db.query(DatabaseHandler.TABLE_NAME, projection, null, null, null, null, null);
                    while( cursor.moveToNext() ) {
                        byte[] b = cursor.getBlob(cursor.getColumnIndex(DatabaseHandler.CONTACT));
                        SyncContact contact = (SyncContact)Serializer.deserializeObject(b);
                        syncContacts.add(contact);
                    }
                    cursor.close();
                    db.close();

                } catch (NullPointerException e) {
                    e.printStackTrace();
                    return;
                }
                changeButtonsState(ACTNS_BTNS, true);
            }
        }).start();
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

        }
        @Override
        public void onReceiveNewToken(VKAccessToken newToken) {
            //accessToken = newToken;
        }
        @Override
        public void onAcceptUserToken(VKAccessToken token) {

        }
    };

    private void vkLogin() {
        if( (VKSdk.instance() != null) && VKSdk.isLoggedIn() ) {
            return;
        }

        //Авторизация в Вконтакте
        VKUIHelper.onCreate(MainActivity.this);
        VKSdk.initialize(sdkListener, vkAppId);
        VKSdk.authorize(permissions);
    }

    private void fbLogin() {
        if( (Session.getActiveSession() != null) &&Session.getActiveSession().isOpened() ) {
            return;
        }

        //Авторизация в Facebook
        Session.openActiveSession(this, true,new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                if (session.isOpened()) { }
            }
        });
    }

    private void init() {
        vkLogin();
        fbLogin();
    }

    private void changeButtonsState(final int[] indexes, final boolean enabled) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for( int ind: indexes ) {
                    buttons[ind].setEnabled(enabled);
                }
            }
        });

    }

    //Синхронизация контактов c VK и FB
    private void sync() {
        changeButtonsState(ACTNS_BTNS, false);

        vkLogin();
        fbLogin();

        if( !VKSdk.isLoggedIn() || !Session.getActiveSession().isOpened() ) {
            changeButtonsState(ACTNS_BTNS, true);
            return;
        }

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
                    e.printStackTrace();
                    return;
                }
                SQLiteDatabase db = handler.getWritableDatabase();
                handler.dropTable(db);

                int id = 0;
                for(SyncContact sc: syncContacts) {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHandler.ID_COLUMN, id);
                    values.put(DatabaseHandler.CONTACT, Serializer.serializeObject(sc));

                    try {
                        long resRowInd = db.insert(DatabaseHandler.TABLE_NAME, null, values);
                        Log.e("INSERT QUERY ID", String.valueOf(resRowInd));
                        id += 1;
                    } catch( NullPointerException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                db.close();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.setTitle("Синхронизация успешно завершена!");
                        changeButtonsState(ACTNS_BTNS, true);
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

    //Отобразить список друзей ВК или FB
    public void showFriends(final ArrayList<Contact> friends) {
        this.setTitle("Друзья"); /*TODO: add string with name of social network*/
        //Список, в котоый будем выводить имена друзей
        ListView lv = ((ListView)findViewById(R.id.lv));

        //задаём данные для списка, т.е. чем список будет заполняться
        ListItemArrayAdapter adapter = new ListItemArrayAdapter(this, friends, bitmapCache, loader);
        lv.setAdapter(adapter);

        //если пользователь кликает на какой-либо элемент списка, то предоставить подробную информацию о данном пользователе
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Получаем Contact соответствующий выбранному пользователю ВК
                Contact selectedContact = friends.get(i);

                //Формируем параметры для другой Activity(для отображения подробной информации о пользователе ВК
                Intent intent = new Intent(MainActivity.this, FriendInfoActivity.class);

                List <String> info = selectedContact.getAllInfo();
                for( int fieldInd = 0; fieldInd < info.size(); ++fieldInd ) {
                    intent.putExtra(Contact.FIELDS[fieldInd], info.get(fieldInd));
                }
                //открыть ещё одну Activity, FriendInfoActivity
                startActivity(intent);
            }
        });
    }

    //Сделать запрос на сервер ВК для получения списка друзей и отобразить результаты в ListView
    //Всё происходит через класс VkRequestTask
    private void performVkRequestAndShowResults() {
        changeButtonsState(new int[]{ VK_BTN_IND }, false);

        vkLogin();
        if( !VKSdk.isLoggedIn() ) {
            changeButtonsState(new int[]{ VK_BTN_IND }, true);
            return;
        }

        this.setTitle("Получение списка друзей ВК...");

        VKRequest request = Requests.vkFriendsRequest(Requests.LOCALE_RUS);
        vkThread = new VkRequestThread(vkFriends, request);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    vkThread.start();
                    vkThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showFriends(vkFriends);
                        changeButtonsState(new int[]{ VK_BTN_IND }, true);
                    }
                });
            }
        }).start();
    }

    //Тоже самое для FB, в разработке
    private void performFbRequestAndShowResults() {
        changeButtonsState(new int[]{ FB_BTN_IND }, false);

        fbLogin();
        if( !Session.getActiveSession().isOpened() ) {
            changeButtonsState(new int[]{ FB_BTN_IND }, true);
            return;
        }

        this.setTitle("Получение списка друзей FB...");

        Request request = Requests.fbFriendsRequest();
        fbThread = new FbRequestThread(fbFriends, request);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    fbThread.start();
                    fbThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showFriends(fbFriends);
                        changeButtonsState(new int[]{ FB_BTN_IND }, true);
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
        ContactArrayAdapter adapter = new ContactArrayAdapter(this, syncContacts, bitmapCache, loader);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(null);
    }

    private void showSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }
}