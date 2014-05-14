package com.example.SocialNetworksSynchronizer;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListItemArrayAdapter extends ArrayAdapter<Contact> {
    private final Context context;
    private final ArrayList<Contact> contactsInfo;

    public ListItemArrayAdapter(Context context,  ArrayList<Contact> contactsInfo) {
        super(context, R.layout.list_item, contactsInfo);

        this.context = context;
        this.contactsInfo = contactsInfo;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.list_item, parent, false);

        //получаем TextView и ImageView, соответствующие имени контакта и аватару
        TextView pbName = (TextView)itemView.findViewById(R.id.contact_name);
        ImageView avatar = (ImageView)itemView.findViewById(R.id.ava);

        pbName.setText(contactsInfo.get(position).getName());
        avatar.setImageBitmap((Bitmap)Serializer.deserializeObject(contactsInfo.get(position).getPhotoUrl().getBytes()));


        return itemView;
    }
}
