package sns;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.LruCache;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.widget.*;
import com.example.SocialNetworksSynchronizer.R;
import com.facebook.*;
import com.vk.sdk.*;
import com.vk.sdk.api.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class MainActivity extends FragmentActivity implements AsyncTaskListener {
    private final String vkAppId = "4313814";   //Вконтакте, номер приложения

    public static Context context = null;
    public static LruCache<String,Bitmap> bitmapCache = null;

    public final String[] permissions = new String[] { //Разрешения для ВК: друзья и не использовать HTTPS
            VKScope.FRIENDS,
            VKScope.NOHTTPS
    };

    public  static final int SYNC_BTN_IND = 0; //кнопка Sync
    public  static final int VK_BTN_IND   = 1; //кнопка VK friends
    public  static final int FB_BTN_IND   = 2; //кнопка FB friends
    public  static final int PB_BTN_IND   = 3; //кнопка Phonebook
    public  static final int ST_BTN_IND   = 4; //кнопка Settings
    public  static final int []ACTNS_BTNS = new int[] { SYNC_BTN_IND, VK_BTN_IND, FB_BTN_IND, ST_BTN_IND };

    private Button[] buttons = new Button[ST_BTN_IND+1];

    private Phonebook phonebook = new Phonebook(this);
    private final DatabaseHandler handler = new DatabaseHandler(this);

    private ParseResponseTask vkResponseTask = null;
    private ParseResponseTask fbResponseTask = null;
    private SyncContactsTask syncContactsTask = null;

    private ArrayList<Contact> vkFriends = new ArrayList<Contact>();
    private ArrayList<Contact> fbFriends = new ArrayList<Contact>();
    private ArrayList<SyncContact> syncContacts = new ArrayList<SyncContact>();   //SyncContact.java, синхронизированный список друзей

    ListItemArrayAdapter adapter = null;
    ContactArrayAdapter pbAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(com.example.SocialNetworksSynchronizer.R.layout.main_layout);  //устанавливаем Layout для текущей активити (файл main_layout.xml)

        buttons[SYNC_BTN_IND] = (Button)findViewById(R.id.sync_button);
        buttons[VK_BTN_IND] = (Button)findViewById(R.id.vk_friends_button);
        buttons[FB_BTN_IND] = (Button)findViewById(R.id.fb_friends_button);
        buttons[PB_BTN_IND] = (Button)findViewById(R.id.phonebook_button);
        buttons[ST_BTN_IND] = (Button)findViewById(R.id.settings_button);

        //Задаём обработчики события нажатия на кнопку для каждой кнопки
        findViewById(R.id.sync_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sync();
            }
        });
        findViewById(R.id.vk_friends_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performVkRequestAndShowResults();
            }
        });
        findViewById(R.id.fb_friends_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performFbRequestAndShowResults();
            }
        });
        findViewById(R.id.phonebook_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makePhonebook();
            }
        });
        findViewById(R.id.settings_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSettings();
            }
        });
        findViewById(R.id.logout_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VKSdk.logout();
                finish();
            }
        });


        //вызываем функцию для авторизации пользователя в ВК и FB

        init();
        readSyncContactsFromDb();
        context = getApplicationContext();
        prepareCache();
        prepareApplicationHelp();
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
        if( Session.getActiveSession() != null ) {
            Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
        }
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

    private String readRawTextFile(int id) {
        InputStream inputStream = getResources().openRawResource(id);
        InputStreamReader in = new InputStreamReader(inputStream);
        BufferedReader buf = new BufferedReader(in);
        String line;
        StringBuilder text = new StringBuilder();
        try {
            while (( line = buf.readLine()) != null)
                text.append(line);
        } catch (IOException e) {
            return null;
        }
        return text.toString();
    }

    private void prepareApplicationHelp() {
        View v = findViewById (android.R.id.title);
        v.setClickable(true);

        v.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.help_dialog_layout);
                TextView tv = (TextView)dialog.findViewById(R.id.help_text_view);
                Spanned helpText = Html.fromHtml(getString(R.string.help_dialog_text));
                tv.setText(helpText);
                dialog.setTitle("Работа с приложением");
                dialog.setCancelable(true);

                dialog.show();
            }
        });
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void readSyncContactsFromDb() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                changeButtonsState(ACTNS_BTNS, false);
                SQLiteDatabase db = handler.getReadableDatabase();
                if( db == null ) {
                    return;
                }
                handler.onCreate(db);
                String[] projection = {
                    DatabaseHandler.ID_COLUMN,
                    DatabaseHandler.CONTACT
                };

                try {
                    Cursor cursor = db.query(DatabaseHandler.TABLE_NAME, projection, null, null, null, null, null);
                    if( cursor == null ) {
                        return;
                    }
                    while( cursor.moveToNext() ) {
                        byte[] b = cursor.getBlob(cursor.getColumnIndex(DatabaseHandler.CONTACT));
                        SyncContact contact = (SyncContact)Serializer.deserializeObject(b);
                        if( contact != null ) {
                            syncContacts.add(contact);
                        }
                    }
                    cursor.close();
                    db.close();

                    vkFriends.clear();
                    fbFriends.clear();
                    for( SyncContact contact: syncContacts ) {
                        if( contact.getVkContact() != null ) {
                            vkFriends.add(contact.getVkContact());
                        }
                        if( contact.getFbContact() != null ) {
                            fbFriends.add(contact.getFbContact());
                        }
                    }
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
        if( (Session.getActiveSession() != null) && Session.getActiveSession().isOpened() ) {
            return;
        }

        //Авторизация в Facebook
        Session.openActiveSession(this, true,new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {

            }
        });
    }

    private void init() {
        if( isOnline() ) {
            vkLogin();
        }
        if( isOnline() ) {
            fbLogin();
        }
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
        if( !isOnline() ) {
            setTitle("Нет подключения к интернету");
            return;
        }
        vkLogin();
        fbLogin();

        if( !VKSdk.isLoggedIn() || !Session.getActiveSession().isOpened() ) {
            return;
        }

        this.setTitle("Синхронизация...");

        ((ListView)findViewById(R.id.lv)).setAdapter(null);
        syncContacts.clear();
        syncContactsTask = new SyncContactsTask(vkFriends, fbFriends, syncContacts, phonebook, handler, this);
        syncContactsTask.execute();
    }

    //Отобразить список друзей ВК или FB
    public void showFriends(final ArrayList<Contact> friends) {
        //Список, в котоый будем выводить имена друзей
        ListView lv = ((ListView)findViewById(R.id.lv));

        //задаём данные для списка, т.е. чем список будет заполняться
        adapter = new ListItemArrayAdapter(this, friends);
        lv.setAdapter(adapter);

        //если пользователь кликает на какой-либо элемент списка, то предоставить подробную информацию о данном пользователе
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Получаем Contact соответствующий выбранному пользователю ВК
                if( i >= ListItemArrayAdapter.filteredContactsInfo.size() ) {
                    return;
                }
                Contact selectedContact = ListItemArrayAdapter.filteredContactsInfo.get(i);
                if( selectedContact == null ) {
                    return;
                }

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


        ((SearchView)findViewById(R.id.search_field)).setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String constraint) {
                if( !constraint.equals(" ")) {
                    adapter.getFilter().filter(constraint);
                }
                return false;
            }
        });
    }

    //Сделать запрос на сервер ВК для получения списка друзей и отобразить результаты в ListView
    //Всё происходит через класс VkRequestTask
    private void performVkRequestAndShowResults() {
        ((ListView)findViewById(R.id.lv)).setAdapter(null);

        if( isOnline() ) {
            vkLogin();
        } else {
            if( vkFriends.size() > 0 ) {
                showFriends(vkFriends);
            } else {
                setTitle("Нет подключения к сети интернет");
            }
            return;
        }

        if( !VKSdk.isLoggedIn() ) {
            showFriends(vkFriends);
            return;
        }

        if( isOnline() ) {
            VKRequest request = Requests.vkFriendsRequest(Requests.LOCALE_RUS);
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    vkResponseTask = new ParseVkResponseTask(vkFriends, MainActivity.this, response);
                    vkResponseTask.setButtonIndexes(ACTNS_BTNS);
                    vkResponseTask.execute();
                }
            });
        } else {
            setTitle("Нет подключения к интернету!");
        }
    }

    //Тоже самое для FB
    private void performFbRequestAndShowResults() {
        ((ListView)findViewById(R.id.lv)).setAdapter(null);

        if( isOnline() ) {
            fbLogin();
        } else {
            if( fbFriends.size() > 0 ) {
                showFriends(fbFriends);
            } else {
                setTitle("Нет подключения к сети интернет");
            }
            return;
        }
        if( !Session.getActiveSession().isOpened() ) {
            showFriends(fbFriends);
            return;
        }

        if( isOnline() ) {
            Request request = Requests.fbFriendsRequest();
            request.setCallback(new Request.Callback() {
                @Override
                public void onCompleted(Response response) {
                    fbResponseTask = new ParseFbResponseTask(fbFriends, MainActivity.this, response);
                    fbResponseTask.setButtonIndexes(ACTNS_BTNS);
                    fbResponseTask.execute();
                }
            });
            request.executeAsync();
        } else {
            setTitle("Нет подключения к интернету!");
        }
    }

    //вызов номера телефона
    private void startDialActivity(String phone){
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phone));
        startActivity(intent);
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
            Queue< Pair<String, String> > phoneContacts = phonebook.getContactNames();
            while( !phoneContacts.isEmpty() ) {
                Pair <String, String> pbContact = phoneContacts.remove();

                SyncContact item = new SyncContact(pbContact.first, null, null);
                item.setPhonebookMobileNumber(pbContact.second);
                syncContacts.add(item);
            }
        }
        //Создаём адаптер для отображения данных в ListView
        pbAdapter = new ContactArrayAdapter(this, syncContacts, bitmapCache);
        lv.setAdapter(pbAdapter);
        lv.setOnItemClickListener(null);

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if( i >= ContactArrayAdapter.filteredContactsInfo.size() ) {
                    return false;

                }
                SyncContact contact = ContactArrayAdapter.filteredContactsInfo.get(i);
                String phoneNumber = contact.getCorrectPhoneNumber();
                if( phoneNumber.length() != 0 ) {
                    startDialActivity(phoneNumber);
                }

                return false;
            }
        });

        ((SearchView)findViewById(R.id.search_field)).setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String constraint) {
                if( !constraint.equals(" ") ) {
                    pbAdapter.getFilter().filter(constraint);
                }
                return false;
            }
        });
    }

    private void showSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    @Override
    public void onTaskBegin(String caption, final int maxValue, final int []buttonIndexes ) {
        setTitle(caption);

        ((ProgressBar)findViewById(R.id.progress_bar)).setMax(maxValue);
        ((ProgressBar)findViewById(R.id.progress_bar)).setProgress(0);
        findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);

        changeButtonsState(buttonIndexes, false);
    }

    @Override
    public void onTaskProgress(int progress) {
        ((ProgressBar)findViewById(R.id.progress_bar)).setProgress(progress);
    }

    @Override
    public void onTaskCompleted(String caption, ArrayList<Contact> friends, final int []buttonIndexes) {
        setTitle(caption);
        showFriends(friends);

        findViewById(R.id.progress_bar).setVisibility(View.INVISIBLE);

        changeButtonsState(buttonIndexes, true);
    }

    @Override
    public void onTaskBegin(String caption, int []buttonIndexes) {
        changeButtonsState(buttonIndexes, false);
        setTitle(caption);
    }

    @Override
    public void onTaskCompleted(String caption, final int []buttonIndexes) {
        changeButtonsState(buttonIndexes, true);
        setTitle(caption);

        findViewById(R.id.progress_bar).setVisibility(View.INVISIBLE);
    }
}