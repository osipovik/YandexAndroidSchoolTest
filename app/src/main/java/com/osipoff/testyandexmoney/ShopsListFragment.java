package com.osipoff.testyandexmoney;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

/**
 * Created by OsIpOff on 16.08.2015.
 */
public class ShopsListFragment extends ListFragment {

    private static final String TAG = ShopsListFragment.class.getSimpleName();

    OnListItemClickListener listItemClickListener;

    public interface OnListItemClickListener {
        void onListItemClick(YaService service);
    }

    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "--- On attach ---");
        super.onAttach(activity);
        try {
            listItemClickListener = (OnListItemClickListener) activity;
        } catch (ClassCastException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        Log.d(TAG, "--- On activity created ---");
        super.onActivityCreated(savedInstanceState);
        setEmptyText("empty data");
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        listItemClickListener.onListItemClick((YaService) l.getItemAtPosition(position));
    }
}
