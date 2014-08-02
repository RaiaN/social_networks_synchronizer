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
import java.util.List;


//Данный класс служить для отображения синхронизированного списка друзей, т.к. этот список намного сложнее по своей
//структуре, нежели просто список строк
//Поэтому возникает необходимость использовать данный класс для корректного отображения информации

//Является потомком класса ArrayAdapter (который используется по умолчанию для отображения элементов ListView)
public class ContactArrayAdapter extends ArrayAdapter<SyncContact> implements Filterable{
    private final Context context;
    private final ArrayList<SyncContact> contactsInfo;
    private final LruCache<String, Bitmap> bitmapCache;
    public static ArrayList<SyncContact> filteredContactsInfo = new ArrayList<SyncContact>();

    private class ViewHolder {
        public TextView pbName;
        public TextView vkName;
        public TextView fbName;

        public ImageView pbImage;
        public ImageView vkImage;
        public ImageView fbImage;

    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            bitmapCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return bitmapCache.get(key);
    }

    //Конструктор, куда передаём текущий контекст и синхронизированный список друзей
    public ContactArrayAdapter(Context context, ArrayList<SyncContact> contactsInfo, LruCache<String,
                               Bitmap> bitmapCache) {
        super(context, R.layout.phonebook_contact, contactsInfo);

        this.context = context;
        this.contactsInfo = contactsInfo;
        filteredContactsInfo = contactsInfo;
        this.bitmapCache = bitmapCache;
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
            view.setTag(holder);
        } else {
            holder = (ViewHolder)view.getTag();
        }

        if( position < filteredContactsInfo.size() ) {
            //задаём значение для трёх TextView
            holder.pbName.setText(filteredContactsInfo.get(position).getPhonebookName());

            Contact vkContact = filteredContactsInfo.get(position).getVkContact();
            Contact fbContact = filteredContactsInfo.get(position).getFbContact();

            if( vkContact != null ) {
                holder.vkName.setText(filteredContactsInfo.get(position).getVkName());
                setImage(vkContact, holder.vkImage);
            } else {
                holder.vkName.setText("");
                holder.vkImage.setImageDrawable(context.getResources().getDrawable( R.drawable.vke_icon));
            }

            if( fbContact != null ) {
                holder.fbName.setText(filteredContactsInfo.get(position).getFbName());
                setImage(fbContact, holder.fbImage);
            } else {
                holder.fbName.setText("");
                holder.fbImage.setImageDrawable(context.getResources().getDrawable( R.drawable.fb_icon));
            }
        } else {
            holder.pbName.setText("");
            holder.pbImage.setImageDrawable(null);
            holder.vkImage.setImageDrawable(null);
            holder.fbImage.setImageDrawable(null);
        }

        return view;
    }

    private void setImage(Contact contact, ImageView iv) {
        if( contact.getImage() != null ) {
            Bitmap image = getBitmapFromMemCache(contact.getPhotoUrl());
            if( image != null ) {
                iv.setImageBitmap(image);
            } else {
                if( contact.getImage() != null) {
                    image = BitmapFactory.decodeByteArray(contact.getImage(), 0, contact.getImage().length);
                    if( image != null) {
                        addBitmapToMemoryCache(contact.getPhotoUrl(), image);
                        iv.setImageBitmap(image);
                    }
                }
            }
        }
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<SyncContact> filteredContacts = new ArrayList<SyncContact>();

                // perform your search here using the searchConstraint String.

                constraint = constraint.toString().toLowerCase();
                for( SyncContact contact: contactsInfo ) {
                    String pbName = contact.getPhonebookName();
                    String vkName = "";
                    String fbName = "";
                    if( contact.getVkContact() != null ) {
                        vkName = contact.getVkContact().getName();
                    }
                    if( contact.getFbContact() != null ) {
                        fbName = contact.getFbContact().getName();
                    }
                    if( pbName.toLowerCase().startsWith(constraint.toString()) ||
                        vkName.toLowerCase().startsWith(constraint.toString()) ||
                        fbName.toLowerCase().startsWith(constraint.toString()) )
                    {
                        filteredContacts.add(contact);
                    }
                }

                results.count = filteredContacts.size();
                results.values = filteredContacts;

                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if( filterResults.count == 0 ) {
                    notifyDataSetInvalidated();
                    return;
                }
                filteredContactsInfo = (ArrayList<SyncContact>)filterResults.values;

                notifyDataSetChanged();
            }
        };

        return filter;
    }
}
