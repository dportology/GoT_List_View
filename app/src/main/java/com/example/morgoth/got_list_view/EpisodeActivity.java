package com.example.morgoth.got_list_view;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;


public class EpisodeActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode);

        TextView tv6 = (TextView) findViewById(R.id.textView6);
        TextView tv7 = (TextView) findViewById(R.id.textView7);
        TextView tv8 = (TextView) findViewById(R.id.textView8);
        TextView tv9 = (TextView) findViewById(R.id.textView9);
        TextView tv10 = (TextView) findViewById(R.id.textView10);
        TextView tv11 = (TextView) findViewById(R.id.textView11);

        Intent myIntent = getIntent();
        String title = myIntent.getStringExtra("title");
        String[] details = myIntent.getStringArrayExtra("details");

        android.support.v7.app.ActionBar ab = this.getSupportActionBar();
        ab.setTitle(title);

        tv6.setText(details[0]);
        tv7.setText(details[1]);
        tv8.setText(details[2]);
        tv9.setText(details[3]);
        tv10.setText(details[4]);
        tv11.setText(details[5]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_episode, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}