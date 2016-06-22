package com.osipoff.testyandexmoney;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by OsIpOff on 16.08.2015.
 */
public class ServiceAdapter extends ArrayAdapter<YaService> {
    private final LayoutInflater mInflater;

    public ServiceAdapter(Context context, List<YaService> service) {
        super(context, 0, service);
        mInflater = LayoutInflater.from(context);
    }

    public View getView(int position, View convertView, ViewGroup parent){
        View view = null;

        if(convertView != null){
            view = convertView;
        }else{
            view = mInflater.inflate(R.layout.list_item_service, parent, false);
            view.setTag(new ViewHolder(view));
        }

        YaService service = getItem(position);
        String title = service.getTitle();

        ViewHolder vh = (ViewHolder) view.getTag();

        vh.title.setText(title);

        TextView titleView = (TextView) view.findViewById(R.id.title);
        titleView.setText(title);

        return view;
    }

    private class ViewHolder { // Реализуем паттерн ViewHolder
        private TextView title;

        public ViewHolder(View v) {
            title = (TextView) v.findViewById(R.id.title);
        }
    }
}
