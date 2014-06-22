package com.codepath.apps.RKTwitterClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.codepath.apps.RKTwitterClient.models.Tweet;
import com.codepath.apps.RKTwitterClient.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class TimelineActivity extends Activity {
    public static int COMPOSE_REQUEST = 1234;
    private TwitterClient client;

    private ArrayList<Tweet> tweets;
    private TweetArrayAdapter aTweets;
    private ListView lvTweets;
    private PullToRefreshLayout mPullToRefreshLayout;

    private long lastTweetID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        setTitle("Home");

        setActionBarTwitterColor();

        client = TwitterApplication.getRestClient();
        clearAndPopulateTimeline();

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

        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(this).allChildrenArePullable()
                .listener(new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View view) {
                        clearAndPopulateTimeline();
                    }
                })
                .setup(mPullToRefreshLayout);

        checkForInternetConnectivity();
    }

    void checkForInternetConnectivity() {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!Connectivity.isOnline(TimelineActivity.this)) {
                    showSavedTweets();
                }
            }
        });
    }

    void showSavedTweets() {
        getProgressBar().setVisibility(View.GONE);
        aTweets.clear();
        List<Tweet> storedTweets = new Select().from(Tweet.class).execute();
        List<User> users = new Select().from(User.class).execute();
        aTweets.addAll(storedTweets);
        aTweets.notifyDataSetChanged();

        new AlertDialog.Builder(this)
                .setTitle("Please reconnect!")
                .setPositiveButton("OK", null)
                .setMessage("Until then we've saved some old Tweets for you.").show();

        dimActionBar();

    }

    void dimActionBar() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ColorDrawable drawable = new ColorDrawable(Color.parseColor("#D52114"));
                Log.d("DBG", "setting drawable " + drawable.toString());
                getActionBar().setBackgroundDrawable(drawable);
                getActionBar().setDisplayShowTitleEnabled(false);
                getActionBar().setDisplayShowTitleEnabled(true);
            }
        });
    }

    void setActionBarTwitterColor() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ColorDrawable drawable = new ColorDrawable(Color.parseColor("#4099FF"));
                Log.d("DBG", "setting drawable " + drawable.toString());
                getActionBar().setBackgroundDrawable(drawable);
                getActionBar().setDisplayShowTitleEnabled(false);
                getActionBar().setDisplayShowTitleEnabled(true);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timeline, menu);
        return true;
    }

    public boolean onComposeClicked(MenuItem item) {
        Intent i = new Intent(this, ComposeActivity.class);
        startActivityForResult(i, COMPOSE_REQUEST);
        return true;
    }

    private void loadAdditionalTweets(int page, int totalItemsCount) {
        client.fetchOlderTweets(new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONArray jsonArray) {
                unpackTweetsFromJSON(jsonArray);
                Log.d("DBG", jsonArray.toString());
            }

            @Override
            public void onFailure(Throwable throwable, String s) {
                super.onFailure(throwable, s);
                Log.d("DBG", s);
            }
        }, lastTweetID - 1);
    }

    void unpackTweetsFromJSON(JSONArray jsonArray) {
        ArrayList<Tweet> receivedTweets = Tweet.fromJSONArray(jsonArray);
        lastTweetID = getOldestTweetId(receivedTweets);
        aTweets.addAll(receivedTweets);
    }

    public void clearAndPopulateTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONArray jsonArray) {
                Log.d("DBG", jsonArray.toString());
                aTweets.clear();
                unpackTweetsFromJSON(jsonArray);
                getProgressBar().setVisibility(View.GONE);
                completeRefreshIfNeeded(true);
                setActionBarTwitterColor();
            }

            @Override
            public void onFailure(Throwable throwable, String s) {
                completeRefreshIfNeeded(false);
            }
        });
    }

    private void completeRefreshIfNeeded(boolean refreshSucceeded) {
        if (mPullToRefreshLayout.isRefreshing()) {
            mPullToRefreshLayout.setRefreshComplete();
            String message = refreshSucceeded ? "Refresh completed!" : "Please reconnect and try again";
            Toast.makeText(this, message , Toast.LENGTH_SHORT).show();
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Tweet justAddedTweet = (Tweet)data.getSerializableExtra("mostCurrentID");
            aTweets.insert(justAddedTweet, 0);
            aTweets.notifyDataSetChanged();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
