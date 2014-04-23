package com.example.SocialNetworksSynchronizer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;



public class ContactArrayAdapter extends ArrayAdapter<SyncContact> {
    private final Context context;
    private final ArrayList<SyncContact> contactsInfo;

    public ContactArrayAdapter(Context context, ArrayList<SyncContact> contactsInfo) {
        super(context, R.layout.phonebook_contact, contactsInfo);

        this.context = context;
        this.contactsInfo = contactsInfo;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.phonebook_contact, parent, false);
        TextView pbName = (TextView)itemView.findViewById(R.id.pb_contact_name);
        TextView vkName = (TextView)itemView.findViewById(R.id.vk_contact_name);
        TextView fbName = (TextView)itemView.findViewById(R.id.fb_contact_name);

        pbName.setText(contactsInfo.get(position).getPhonebookName());
        vkName.setText(contactsInfo.get(position).getVkName());
        fbName.setText(contactsInfo.get(position).getFbName());

        return itemView;
    }
}
