package com.example.SocialNetworksSynchronizer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


//Данный класс служить для отображения синхронизированного списка друзей, т.к. этот список намного сложнее по своей
//структуре, нежели просто список строк
//Поэтому возникает необходимость использовать данный класс для корректного отображения информации

//Является потомком класса ArrayAdapter (который используется по умолчанию для отображения элементов ListView)
public class ContactArrayAdapter extends ArrayAdapter<SyncContact> {
    private final Context context;
    private final ArrayList<SyncContact> contactsInfo;

    //Конструктор, куда передаём текущий контекст и синхронизированный список друзей
    public ContactArrayAdapter(Context context, ArrayList<SyncContact> contactsInfo) {
        super(context, R.layout.phonebook_contact, contactsInfo);

        this.context = context;
        this.contactsInfo = contactsInfo;
    }

    //Данный метод изменяет поведение оригинального метода отображения элементов ListView с целью
    //отображения синхронизированного списка друзей,
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.phonebook_contact, parent, false);

        //получаем 3 TextView, соответствующие имени контакта в устройстве, в ВК и в FB
        TextView pbName = (TextView)itemView.findViewById(R.id.pb_contact_name);
        TextView vkName = (TextView)itemView.findViewById(R.id.vk_contact_name);
        TextView fbName = (TextView)itemView.findViewById(R.id.fb_contact_name);

        //задаём значение для трёх TextView
        pbName.setText(contactsInfo.get(position).getPhonebookName());
        vkName.setText(contactsInfo.get(position).getVkName());
        fbName.setText(contactsInfo.get(position).getFbName());

        return itemView;
    }
}
