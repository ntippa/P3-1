package com.ntippa.android.myApp3;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Nalini on 3/2/2016.
 */
public  class DetailsActivity extends AppCompatActivity {

    private static final String TAG = DetailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_detail_activity);

        if (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) {
            // If the screen is now in landscape mode, we can show the
            // dialog in-line with the list so we don't need this activity.
            finish();
            return;
        }

        if (savedInstanceState == null) {
            // During initial setup, plug in the details fragment.
            BookDetail details = new BookDetail();
            details.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, details)//todo:addToBackStack??
                .addToBackStack((String) getTitle())
                .commit();
            //getFragmentManager().beginTransaction().add(android.R.id.content,details).commit();
        }
    }
}
