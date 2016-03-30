package com.example.morgoth.got_list_view;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {

    String[] episodes;
    String[] ids;
    String[][] details;

    ListView mListView;
    Button playButton;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.list);

        playButton = (Button) findViewById(R.id.retry);
        playButton.setVisibility(View.INVISIBLE);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(loadInitialList() == true){
                    playButton.setVisibility(View.INVISIBLE);
                }
            }
        });

        loadInitialList();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Get the value of the string where the click occured
                String itemValue = (String) mListView.getItemAtPosition(position);

                // Only do the async call if the data wasn't already fetched.
                if(details[position][0] == null) {
                    if(isNetworkAvailable(parent.getContext()) == true) {
                        DetailsAsyncTask task = new DetailsAsyncTask(position, itemValue);
                        task.execute(new String[]{"http://www.omdbapi.com/?i=" + ids[position] + "&plot=short&r=json"});
                    }else{
                        Context context = getApplicationContext();
                        CharSequence text = "No internet connection available";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                }
                else{
                    // start the episode details activity
                    Intent intent = new Intent(getApplicationContext(), EpisodeActivity.class);

                    intent.putExtra("title", itemValue);
                    intent.putExtra("details", details[position]);

                    startActivity(intent);
                }

            }
        });
    }

    public boolean loadInitialList(){
        if(isNetworkAvailable(this)) {
            FetchTitlesAsync task = new FetchTitlesAsync(this);
            task.execute(new String[]{"http://www.omdbapi.com/?t=Game%20of%20Thrones&Season=1"});
            return true;
        }
        else{

            playButton.setVisibility(View.VISIBLE);
            Context context = getApplicationContext();
            CharSequence text = "No internet connection available";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return id == R.id.action_settings || super.onOptionsItemSelected(item);

    }


    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private class FetchTitlesAsync extends AsyncTask<String, Void, String> {


        // need to pass in a reference to the mainActivity in order to set the adapter on the listView
        private AppCompatActivity activity;

        public FetchTitlesAsync(AppCompatActivity activity){
            this.activity = activity;
        }

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            for (String url : urls) {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                try {
                    HttpResponse execute = client.execute(httpGet);
                    InputStream content = execute.getEntity().getContent();

                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return response;
        }


        // Important method. parses all json and stores info in the global string arrays
        @Override
        protected void onPostExecute(String result) {

            JSONObject mainObject = null;
            try {
                mainObject = new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONArray episodeObject = null;
            try {
                episodeObject = mainObject != null ? mainObject.getJSONArray("Episodes") : null;
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // the array of arrays to hold all details for the episodes
            details = new String[episodeObject != null ? episodeObject.length() : 0][6];
            episodes = new String[episodeObject != null ? episodeObject.length() : 0];
            ids = new String[episodeObject != null ? episodeObject.length() : 0];
            for(int i = 0; i < (episodeObject != null ? episodeObject.length() : 0); i++){
                try {
                    episodes[i] = episodeObject.getJSONObject(i).getString("Title");
                    ids[i] = episodeObject.getJSONObject(i).getString("imdbID");
                    
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            ArrayAdapter<String> episodeAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, episodes);
            mListView.setAdapter(episodeAdapter);
        }

    }


    // Second Async class to be called from the on click method. fetches details for particular episodes
    private class DetailsAsyncTask extends AsyncTask<String, Void, String> {


        // need to pass in a reference to the mainActivity in order to set the adapter on the listView
        private int id;
        private String title;

        public DetailsAsyncTask(int id, String title){
            this.id = id;
            this.title = title;
        }

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            for (String url : urls) {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                try {
                    HttpResponse execute = client.execute(httpGet);
                    InputStream content = execute.getEntity().getContent();

                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return response;
        }


        // Important method. parses all json.
        @Override
        protected void onPostExecute(String result) {

            JSONObject mainObject = null;
            try {
                mainObject = new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                details[id][0] = mainObject != null ? mainObject.getString("Year") : null;
                details[id][1] = mainObject != null ? mainObject.getString("Rated") : null;
                details[id][2] = mainObject != null ? mainObject.getString("Released") : null;
                details[id][3] = mainObject != null ? mainObject.getString("Season") : null;
                details[id][4] = mainObject != null ? mainObject.getString("Episode") : null;
                details[id][5] = mainObject != null ? mainObject.getString("Runtime") : null;

            } catch (JSONException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(getApplicationContext(), EpisodeActivity.class);

            intent.putExtra("title", title);
            intent.putExtra("details", details[id]);

            startActivity(intent);

        }

    }


}

