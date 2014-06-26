package com.codepath.apps.RKTwitterClient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.codepath.apps.RKTwitterClient.fragments.TweetsListFragment;
import com.codepath.apps.RKTwitterClient.models.Tweet;
import com.codepath.apps.RKTwitterClient.util.Util;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

public class TimelineActivity extends Activity implements TweetsListFragment.TweetsListListener {
    public static int COMPOSE_REQUEST = 1234;
    private TwitterClient client;

    private PullToRefreshLayout mPullToRefreshLayout;

    private MenuItem mRefreshItem;

    private long lastTweetID = -1;
    private TweetsListFragment homeTweetsListFragment;


    public boolean mIsRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        setTitle("Home");

        setActionBarTwitterColor();

        client = TwitterApplication.getRestClient();

        homeTweetsListFragment = (TweetsListFragment)getFragmentManager().findFragmentById(R.id.fMainTimeline);

    }

    View getView() {
        return findViewById(R.id.rlDetailsRoot);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsRunning = true;
    }

    @Override
    protected void onPause() {
        mIsRunning = false;
        super.onPause();
    }

    public void onTweetClicked(Tweet tweet) {
        Intent i = new Intent(this, TweetDetailsActivity.class);
        i.putExtra("tweet", tweet);
        startActivity(i);
    }


    void setActionBarTwitterColor() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Util.setActionBarDrawable(getActionBar(), new ColorDrawable(getResources().getColor(R.color.twitterBlue)));
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timeline, menu);
        mRefreshItem = menu.findItem(R.id.action_loading);
        mRefreshItem.setActionView(R.layout.actionbar_progress);
        mRefreshItem.setVisible(false);
        return true;
    }

    public boolean onComposeClicked(MenuItem item) {
        Intent i = new Intent(this, ComposeActivity.class);
        startActivityForResult(i, COMPOSE_REQUEST);
        return true;
    }


    public void onReplyToTweet(Tweet tweet) {
        Intent i = new Intent(this, ComposeActivity.class);
        i.putExtra(ComposeActivity.TWEET_EXTRA_KEY, tweet);
        startActivityForResult(i, COMPOSE_REQUEST);
    }

    public void onFavoriteTweet(final Tweet tweet, final View tweetContainer) {
        Log.d("DBG", "Beginning favorite operation");
        mRefreshItem.setVisible(true);
        client.performFavorite(tweet, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONObject jsonObject) {
                Log.d("DBG", "Favorite was successful");
                final Tweet t = Tweet.fromJSON(jsonObject);
                TimelineActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String toastMsg = String.format("Successfully favorited @%s's tweet", t.getUser().getScreenName());
                        Toast.makeText(TimelineActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                        TweetArrayAdapter.setListItemFavoritedState(tweetContainer, true);
                        tweet.favorited = true;
                        tweet.save();
                        mRefreshItem.setVisible(false);
                    }
                });
            }

            @Override
            public void onFailure(Throwable throwable, String s) {
                final String failure = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("DBG", String.format("Failed to favorite. %s", failure));
                        Toast.makeText(TimelineActivity.this, "Could not favorite tweet.", Toast.LENGTH_SHORT).show();
                        mRefreshItem.setVisible(false);
                    }
                });
            }
        });
    }

    public void onRetweetClicked(final Tweet tweet, final View tweetContainerView) {
        Log.d("DBG", "Beginning retweet operation");
        mRefreshItem.setVisible(true);
        client.performRetweet(tweet, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONObject jsonObject) {
                Log.d("DBG", "Retweet was successful");
                final Tweet t = Tweet.fromJSON(jsonObject);
                TimelineActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String toastMsg = String.format("Successfully retweeted @%s's tweet", t.retweeted_status.getUser().getScreenName());
                        Toast.makeText(TimelineActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                        TweetArrayAdapter.setListItemRetweeted(tweetContainerView, true);
                        tweet.retweeted = true;
                        tweet.save();
                        mRefreshItem.setVisible(false);
                    }
                });
            }

            @Override
            public void onFailure(Throwable throwable, String s) {
                Log.e("DBG", String.format("Failed to retweet. %s", s));
                final String failure = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("DBG", String.format("Failed to retweet. %s", failure));
                        Toast.makeText(TimelineActivity.this, "Could not retweet tweet.", Toast.LENGTH_SHORT).show();
                        mRefreshItem.setVisible(false);
                    }
                });
            }
        });

    }

    private void setNoNetworkBannerVisibility(int visibility) {
        View v = findViewById(R.id.vgDisconnectedBanner);
        v.setVisibility(visibility);
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

    public void onTriggerInfiniteScroll() {
        client.fetchOlderTweets(new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONArray jsonArray) {
                unpackTweetsFromJSON(jsonArray);
                setNoNetworkBannerVisibility(View.GONE);
            }

            @Override
            public void onFailure(Throwable throwable, String s) {
                setNoNetworkBannerVisibility(View.VISIBLE);
                checkBackForAConnection(0);
            }
        }, lastTweetID - 1);
    }

    void unpackTweetsFromJSON(JSONArray jsonArray) {
        ArrayList<Tweet> receivedTweets = Tweet.fromJSONArray(jsonArray);
        lastTweetID = getOldestTweetId(receivedTweets);
        homeTweetsListFragment.addAll(receivedTweets);
    }


    public void clearAndPopulateTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONArray jsonArray) {
                Log.d("DBG", jsonArray.toString());
                homeTweetsListFragment.clearTweets();
                unpackTweetsFromJSON(jsonArray);
                getProgressBar().setVisibility(View.GONE);
                completeRefreshIfNeeded(true);
                setActionBarTwitterColor();
                setNoNetworkBannerVisibility(View.GONE);
                mConnecting = false; // Stops any retrying that's in progress
            }

            @Override
            public void onFailure(Throwable throwable, String s) {
                setNoNetworkBannerVisibility(View.VISIBLE);
                completeRefreshIfNeeded(false);
                checkBackForAConnection(0);
                Log.e("DBG", String.format("Timeline populate failed %s %s", throwable.toString(), s));
            }
        });
    }


    ProgressBar getProgressBar() {
        return (ProgressBar)findViewById(R.id.progressBar);
    }


}
