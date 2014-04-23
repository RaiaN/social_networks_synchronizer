package com.example.SocialNetworksSynchronizer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class FriendInfoActivity extends Activity {
    private ArrayList<TextView> tvs = new ArrayList<TextView>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_info_layout);

        tvs.add((TextView)findViewById(R.id.friend_name));
        tvs.add((TextView)findViewById(R.id.friend_contacts));
        tvs.add((TextView)findViewById(R.id.friend_address));

        final Button backButton = (Button)findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        fill();

    }

    private void fill() {
        Intent intent = getIntent();

        for( int extraInd = 0; extraInd < MainActivity.extras.length; ++extraInd ) {
            String param = intent.getStringExtra(MainActivity.extras[extraInd]);
            tvs.get(extraInd).append(param);
        }
    }
}
