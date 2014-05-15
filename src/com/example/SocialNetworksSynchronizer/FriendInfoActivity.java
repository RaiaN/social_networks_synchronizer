package com.example.SocialNetworksSynchronizer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

//Activity для отображения подробной информации о контакте ВК
public class FriendInfoActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) { //Выводим имя контакта, указанные телефоны и адрес в 3 TextView
        super.onCreate(savedInstanceState);              //См. также friend_info_layout
        setContentView(R.layout.friend_info_layout);

        final Button backButton = (Button)findViewById(R.id.back_button);  //кнопка Назад
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        fill();
    }

    private void fill() {                               //Заполнить TextView используя приведенную выше информацию
        Intent intent = getIntent();

        List<String> values = new ArrayList<String>();
        for( int fieldInd = 0; fieldInd < Contact.FIELDS.length; ++fieldInd ) {
            if( intent.getStringExtra(Contact.FIELDS[fieldInd]).length() > 0 ) {
                String param = "";
                if( Contact.FIELDS[fieldInd].compareTo(Contact.BIRTHDAY) == 0 ) {
                    param += "День рождения:\n";
                } else if( Contact.FIELDS[fieldInd].compareTo(Contact.ADDRESS) == 0 ) {
                    param += "Адрес:\n";
                } else if( Contact.FIELDS[fieldInd].compareTo(Contact.MOBILE_PHONE) == 0 ) {
                    param += "Мобильный телефон:\n";
                } else if( Contact.FIELDS[fieldInd].compareTo(Contact.HOME_PHONE) == 0 ) {
                    param += "Домашний телефон:\n";
                } else if( Contact.FIELDS[fieldInd].compareTo(Contact.SKYPE) == 0 ) {
                    param += "Skype:\n";
                } else if( Contact.FIELDS[fieldInd].compareTo(Contact.TWITTER) == 0 ) {
                    param += "Twitter:\n";
                } else if( Contact.FIELDS[fieldInd].compareTo(Contact.INSTAGRAM) == 0 ) {
                    param += "Instagram:\n";
                } else if( Contact.FIELDS[fieldInd].compareTo(Contact.EDUCATION) == 0 ) {
                    param += "Образование:\n";
                }
                param += intent.getStringExtra(Contact.FIELDS[fieldInd]);
                values.add(param);
            }
        }

        ListView lv = (ListView)findViewById(R.id.friend_info_list);
        FriendInfoArrayAdapter adapter = new FriendInfoArrayAdapter(this, values);

        lv.setClickable(false);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(null);
    }
}
