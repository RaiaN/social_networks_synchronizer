package com.example.SocialNetworksSynchronizer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

//Activity для отображения подробной информации о контакте ВК
public class FriendInfoActivity extends Activity {
    private ArrayList<TextView> tvs = new ArrayList<TextView>();

    @Override
    protected void onCreate(Bundle savedInstanceState) { //Выводим имя контакта, указанные телефоны и адрес в 3 TextView
        super.onCreate(savedInstanceState);              //См. также friend_info_layout
        setContentView(R.layout.friend_info_layout);

        tvs.add((TextView)findViewById(R.id.friend_name));    //добавляем 3 TextView в один список для удобства
        tvs.add((TextView)findViewById(R.id.friend_contacts));
        tvs.add((TextView)findViewById(R.id.friend_address));
        tvs.add((TextView)findViewById(R.id.friend_email));

        final Button backButton = (Button)findViewById(R.id.back_button);  //кнопка Назад
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        fill();

    }

    private void fill() {                               //Заполнить 3 TextView используя приведенную выше информацию
        Intent intent = getIntent();

        for( int extraInd = 0; extraInd < MainActivity.extras.length; ++extraInd ) {
            String param = intent.getStringExtra(MainActivity.extras[extraInd]);
            tvs.get(extraInd).append(param);
        }
    }
}
