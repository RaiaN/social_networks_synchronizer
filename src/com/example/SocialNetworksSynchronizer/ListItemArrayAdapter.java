package com.example.SocialNetworksSynchronizer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import java.util.ArrayList;

public class ListItemArrayAdapter extends ArrayAdapter<Contact> {

    private final Context context;
    private final ArrayList<Contact> contactsInfo;

    private class ViewHolder {
        public TextView text;
        public ImageView image;
    }

    private final LruCache<String, Bitmap> bitmapCache;
    private final ImageLoader loader;

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            bitmapCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return bitmapCache.get(key);
    }

    public ListItemArrayAdapter(Context context,  ArrayList<Contact> contactsInfo, LruCache<String,
                                Bitmap> bitmapCache, ImageLoader loader) {
        super(context, R.layout.list_item, contactsInfo);

        this.context = context;
        this.contactsInfo = contactsInfo;
        this.bitmapCache = bitmapCache;
        this.loader = loader;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = convertView;
        final ViewHolder holder;
        if( convertView == null ) {
            view = inflater.inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.text = (TextView) view.findViewById(R.id.contact_name);
            holder.image = (ImageView) view.findViewById(R.id.ava);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.text.setText(contactsInfo.get(position).getName());

        String photoUrl = contactsInfo.get(position).getPhotoUrl();
        Bitmap image = getBitmapFromMemCache(photoUrl);
        if( image != null ) {
            holder.image.setImageBitmap(image);
        } else {
            if (contactsInfo.get(position).getImage() != null) {
                image = BitmapFactory.decodeByteArray(contactsInfo.get(position).getImage(), 0,
                                                      contactsInfo.get(position).getImage().length);
                if (image != null) {
                    addBitmapToMemoryCache(photoUrl, image);
                    holder.image.setImageBitmap(image);
                }
            } else {
                loader.displayImage(photoUrl, holder.image);
            }
        }

        return view;
    }
}
