package sns;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.SocialNetworksSynchronizer.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListItemArrayAdapter extends ArrayAdapter<Contact> implements Filterable {

    private final Context context;
    private ArrayList<Contact> contactsInfo;
    public static ArrayList<Contact> filteredContactsInfo = new ArrayList<Contact>();

    private class ViewHolder {
        public TextView text;
        public ImageView image;
    }

    private final LruCache<String, Bitmap> bitmapCache;

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            bitmapCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return bitmapCache.get(key);
    }

    public ListItemArrayAdapter(Context context,  ArrayList<Contact> contactsInfo) {
        super(context, R.layout.list_item, contactsInfo);

        this.context = context;
        this.contactsInfo = contactsInfo;
        filteredContactsInfo = contactsInfo;
        this.bitmapCache = MainActivity.bitmapCache;

        Collections.sort(contactsInfo, new Comparator<Contact>() {
            @Override
            public int compare(Contact c1, Contact c2) {
                return c1.getName().compareTo(c2.getName());
            }
        });
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

        if( position < filteredContactsInfo.size() ) {
            holder.text.setText(filteredContactsInfo.get(position).getName());

            String photoUrl = filteredContactsInfo.get(position).getPhotoUrl();
            Bitmap image = getBitmapFromMemCache(photoUrl);
            if( image != null ) {
                holder.image.setImageBitmap(image);
            } else {
                if (filteredContactsInfo.get(position).getImage() != null) {
                    image = BitmapFactory.decodeByteArray(filteredContactsInfo.get(position).getImage(), 0,
                                                          filteredContactsInfo.get(position).getImage().length);
                    if (image != null) {
                        addBitmapToMemoryCache(photoUrl, image);
                        holder.image.setImageBitmap(image);
                    }
                }
            }
        } else {
            holder.text.setText("");
            holder.image.setImageDrawable(null);
        }

        return view;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<Contact> filteredContacts = new ArrayList<Contact>();

                // perform your search here using the searchConstraint String.

                constraint = constraint.toString().toLowerCase();
                for( Contact contact: contactsInfo ) {
                    String name = contact.getName();
                    if( name.toLowerCase().startsWith(constraint.toString()) ) {
                        filteredContacts.add(contact);
                    }
                }

                results.count = filteredContacts.size();
                results.values = filteredContacts;

                return results;
            }


            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredContactsInfo = (ArrayList<Contact>)filterResults.values;

                Collections.sort(filteredContactsInfo, new Comparator<Contact>() {
                    @Override
                    public int compare(Contact c1, Contact c2) {
                        return c1.getName().compareTo(c2.getName());
                    }
                });

                notifyDataSetChanged();
            }
        };

        return filter;
    }
}
