package com.codepath.apps.RKTwitterClient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.codepath.apps.RKTwitterClient.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;

import java.util.ArrayList;

public class TimelineActivity extends Activity {
    private TwitterClient client;

    private ArrayList<Tweet> tweets;
    private TweetArrayAdapter aTweets;
    private ListView lvTweets;

    private long lastTweetID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        setTitle("Home");

        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#4099FF")));

        client = TwitterApplication.getRestClient();
        populateTimeline();

        lvTweets = (ListView)findViewById(R.id.lvTweets);
        lvTweets.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                loadAdditionalTweets(page, totalItemsCount);
            }
        });
        tweets = new ArrayList<Tweet>();
        aTweets = new TweetArrayAdapter(this, tweets);
        lvTweets.setAdapter(aTweets);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timeline, menu);
        return true;
    }

    public boolean onComposeClicked(MenuItem item) {
        Intent i = new Intent(this, ComposeActivity.class);
        startActivity(i);
        return true;
    }

    private void loadAdditionalTweets(int page, int totalItemsCount) {
        client.getHomeTimeLineTweetsNoOlderThan(new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONArray jsonArray) {
                Log.d("DBG", jsonArray.toString());
                ArrayList<Tweet> receivedTweets = Tweet.fromJSONArray(jsonArray);
                lastTweetID = getOldestTweetId(receivedTweets);
                aTweets.addAll(receivedTweets);
            }

            @Override
            public void onFailure(Throwable throwable, String s) {
                super.onFailure(throwable, s);
                Log.d("DBG", s);
            }
        }, lastTweetID);
    }

    public void populateTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONArray jsonArray) {
                Log.d("DBG", jsonArray.toString());
                ArrayList<Tweet> receivedTweets = Tweet.fromJSONArray(jsonArray);
                lastTweetID = getOldestTweetId(receivedTweets);
                aTweets.addAll(receivedTweets);
                getProgressBar().setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Throwable throwable, String s) {
                super.onFailure(throwable, s);
                Log.d("DBG", s);
            }
        });
    }

    ProgressBar getProgressBar() {
        return (ProgressBar)findViewById(R.id.progressBar);
    }


    long getOldestTweetId(ArrayList<Tweet> tweets) {
        long oldest = Tweet.OLDEST_TWEET;
        for (Tweet tweet: tweets) {
            if (tweet.getID() < oldest) {
                oldest = tweet.getID();
            }
        }
        return oldest;
    }

}
