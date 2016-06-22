package com.osipoff.testyandexmoney;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;


public class MainActivity extends AppCompatActivity implements ShopsListFragment.OnListItemClickListener{
    private ArrayList<YaService> shopsList;
    private DBHelper dbHelper;
    ListFragment listFragment;
    private static final String TAG = ShopsListFragment.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createShopsList(null);
    }

    private void createShopsList(YaService service){
        dbHelper = new DBHelper(getBaseContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String where = "depth_level = 1";

        boolean rootList = true;

        if(service != null){
            int parentId = service.getId();
            where = "parent_id = " + parentId;
            rootList = false;
        }

        Cursor c = db.query("yaShops", null, where, null, null, null, null);

        if(c.moveToFirst()){
            shopsList = new ArrayList<>();

            int idColIndex = c.getColumnIndex("id");
            int titleColIndex = c.getColumnIndex("title");

            do{
                int id = c.getInt(idColIndex);
                String title = c.getString(titleColIndex);

                Log.d(TAG, title);
                YaService yaService = new YaService(id, title);

                shopsList.add(yaService);
            }while(c.moveToNext());

            Log.d(TAG, "--- Local data show ---");

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            listFragment = new ShopsListFragment();
            listFragment.setListAdapter(new ServiceAdapter(getBaseContext(), shopsList));

            fragmentTransaction.replace(R.id.fragmentContainer, listFragment);

            if(!rootList){
                fragmentTransaction.addToBackStack(null);
            }

            fragmentTransaction.commit();

            c.close();
            db.close();
            dbHelper.close();
        }else{
            if(rootList){
                new GetDataTask().execute();
            }else{
                Toast.makeText(getBaseContext(), service.getTitle(), Toast.LENGTH_SHORT).show();

            }
        }
    }

    @Override
    public void onListItemClick(YaService service) {
        Log.d(TAG, service.getTitle());

        createShopsList(service);
    }

    //вынести работы с базой в сюда
    private class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context) {
            super(context, "yaShopsDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "--- onCreate database ---");
            //Создаем таблицу с полями
            db.execSQL("CREATE TABLE yaShops (" +
                    "id integer primary key autoincrement," +
                    "title text," +
                    "parent_id integer," +
                    "depth_level integer " +
                    ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    private class GetDataTask extends AsyncTask<Void, Void, ArrayList<YaService>> {
        private SQLiteDatabase db;

        @Override
        protected ArrayList<YaService> doInBackground(Void... params) {
            shopsList = new ArrayList<>();

            URL url = null;

            try {
                url = new URL("https://money.yandex.ru/api/categories-list");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                Scanner sc = new Scanner(in);

                StringBuilder sb = new StringBuilder();

                while(sc.hasNextLine()){
                    sb.append(sc.nextLine());
                }

                //Подключаемся к БД
                db = dbHelper.getWritableDatabase();
                db.delete("yaShops", null, null);

                shopsList.addAll(parseData(sb.toString(), 0, 1));

                db.close();
                dbHelper.close();
                urlConnection.disconnect();
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
            }

            return shopsList;
        }

        protected void onPostExecute(ArrayList<YaService> shopsList){
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            listFragment = new ShopsListFragment();
            listFragment.setListAdapter(new ServiceAdapter(getBaseContext(), shopsList));

            fragmentTransaction.replace(R.id.fragmentContainer, listFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

        private List<YaService> parseData(String json, long parentId, int depthLevel) {
            List<YaService> shopsList = new LinkedList<>();

            try {
                JSONArray serviceArray = new JSONArray(json);
                int id;

                for(int i=0; i<serviceArray.length();i++){
                    JSONObject serviceObj = serviceArray.getJSONObject(i);

                    String title = serviceObj.getString("title");

                    if(serviceObj.has("id")){
                        id = serviceObj.getInt("id");
                    }else{
                        id = i;
                    }

                    YaService service = new YaService(id, title);
                    shopsList.add(service);

                    //Создаем объекст для данных
                    ContentValues cv = new ContentValues();
                    String subs;

                    cv.put("id", id);
                    cv.put("title", title);
                    cv.put("parent_id", parentId);
                    cv.put("depth_level", depthLevel);

                    long rowId = db.insert("yaShops", null, cv);
                    Log.d(TAG, "row inserted, ID = " + rowId);

                    if(serviceObj.has("subs")){
                        subs = serviceObj.getString("subs");
                        parseData(subs, id, depthLevel+1);
                    }
                }
            }catch (JSONException e) {
                Log.e(TAG, "Error parsing JSON", e);
            }

            return shopsList;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_update){
            new GetDataTask().execute();
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }
}
