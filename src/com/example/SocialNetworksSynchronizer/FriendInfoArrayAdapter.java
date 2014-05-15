package com.example.SocialNetworksSynchronizer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;


public class FriendInfoArrayAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final List<String> values;

    private class ViewHolder {
        TextView text;
    }

    FriendInfoArrayAdapter(Context context, List<String> values) {
        super(context, R.layout.friend_info_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = convertView;
        final ViewHolder holder;
        if( view == null ) {
            view = inflater.inflate(R.layout.friend_info_item, parent, false);
            holder = new ViewHolder();
            holder.text = (TextView)view.findViewById(R.id.friend_info_field);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.text.setText(values.get(position));

        return view;
    }
}
