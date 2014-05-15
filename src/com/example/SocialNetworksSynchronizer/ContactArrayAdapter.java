package com.example.SocialNetworksSynchronizer;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;


//Данный класс служить для отображения синхронизированного списка друзей, т.к. этот список намного сложнее по своей
//структуре, нежели просто список строк
//Поэтому возникает необходимость использовать данный класс для корректного отображения информации

//Является потомком класса ArrayAdapter (который используется по умолчанию для отображения элементов ListView)
public class ContactArrayAdapter extends ArrayAdapter<SyncContact> {
    private final Context context;
    private final ArrayList<SyncContact> contactsInfo;
    private final LruCache<String, Bitmap> bitmapCache;
    private final ImageLoader loader;

    private class ViewHolder {
        public TextView pbName;
        public TextView vkName;
        public TextView fbName;

        public ImageView pbImage;
        public ImageView vkImage;
        public ImageView fbImage;

    }

    //Конструктор, куда передаём текущий контекст и синхронизированный список друзей
    public ContactArrayAdapter(Context context, ArrayList<SyncContact> contactsInfo, LruCache<String,
                               Bitmap> bitmapCache, ImageLoader loader) {
        super(context, R.layout.phonebook_contact, contactsInfo);

        this.context = context;
        this.contactsInfo = contactsInfo;
        this.bitmapCache = bitmapCache;
        this.loader = loader;
    }

    //Данный метод изменяет поведение оригинального метода отображения элементов ListView с целью
    //отображения синхронизированного списка друзей,
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = convertView;
        final ViewHolder holder;
        if( view == null ) {
            view = inflater.inflate(R.layout.phonebook_contact, parent, false);
            holder = new ViewHolder();
            holder.pbName = (TextView)view.findViewById(R.id.pb_contact_name);
            holder.vkName = (TextView)view.findViewById(R.id.vk_contact_name);
            holder.fbName = (TextView)view.findViewById(R.id.fb_contact_name);

            holder.pbImage = (ImageView)view.findViewById(R.id.pb_icon);
            holder.vkImage = (ImageView)view.findViewById(R.id.vk_icon);
            holder.fbImage = (ImageView)view.findViewById(R.id.fb_icon);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        //задаём значение для трёх TextView
        holder.pbName.setText(contactsInfo.get(position).getPhonebookName());
        holder.vkName.setText(contactsInfo.get(position).getVkName());
        holder.fbName.setText(contactsInfo.get(position).getFbName());

        return view;
    }
}
