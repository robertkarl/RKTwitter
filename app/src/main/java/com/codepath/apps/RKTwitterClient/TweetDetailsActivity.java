package com.codepath.apps.RKTwitterClient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.codepath.apps.RKTwitterClient.models.Tweet;

import static com.codepath.apps.RKTwitterClient.Util.setupTextviewContents;

public class TweetDetailsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_details);

        Intent i = getIntent();
        Tweet tweet = (Tweet)i.getSerializableExtra("tweet");


        View v = findViewById(R.id.rlDetailsRoot);
        setupTextviewContents(v, R.id.tvUserName, tweet.getUser().getName());
        setupTextviewContents(v, R.id.tvUserScreenName, String.format("@%s", tweet.getUser().getScreenName(), "@"));
        setupTextviewContents(v, R.id.tvBody, tweet.getBody());
        setupTextviewContents(v, R.id.tvRelativeTimestamp, tweet.getRelativeDate());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
