package com.codepath.apps.RKTwitterClient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.codepath.apps.RKTwitterClient.models.Tweet;
import com.codepath.apps.RKTwitterClient.util.Connectivity;
import com.codepath.apps.RKTwitterClient.util.EndlessScrollListener;
import com.codepath.apps.RKTwitterClient.util.Util;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

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

    private MenuItem mRefreshItem;

    private boolean mConnecting = false;

    public boolean mIsRunning = false;

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
        lvTweets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onTweetClicked(aTweets.getItem(position));
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

        checkBackForAConnection(0);

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

    void showSavedTweets() {
        getProgressBar().setVisibility(View.GONE);
        aTweets.clear();
        List<Tweet> storedTweets = new Select().from(Tweet.class).execute();
        aTweets.addAll(storedTweets);
        aTweets.notifyDataSetChanged();

        setNoNetworkBannerVisibility(View.VISIBLE);
    }

    /**
     * Perform exponential backoff checking for internet connectivity
     * @param delay milliseconds later for initial check.
     */
    private void checkBackForAConnection(final int delay) {
        if (mConnecting && delay == 0) {
            // Don't spawn off multiple threads trying to check back
            return;
        }
        mConnecting = true;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Connectivity.isOnline(TimelineActivity.this)) {
                    setNoNetworkBannerVisibility(View.GONE);
                    if (delay != 0) {
                        Toast.makeText(TimelineActivity.this, "Welcome back", Toast.LENGTH_SHORT).show();
                        Log.d("DBG", String.format("found a connection again! delay would have been %d", delay));
                    }
                    mConnecting = false;
                }
                else {
                    Log.d("DBG", String.format("Checking server connection in %d millis", delay * 2));
                    checkBackForAConnection(delay == 0 ? 500 : delay * 2);
                    setNoNetworkBannerVisibility(View.VISIBLE);
                    showSavedTweets();
                }
            }
        }, delay);
    }

    private void setNoNetworkBannerVisibility(int visibility) {
        View v = findViewById(R.id.vgDisconnectedBanner);
        v.setVisibility(visibility);
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

    private void loadAdditionalTweets(int page, int totalItemsCount) {
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
                setNoNetworkBannerVisibility(View.GONE);
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

    public void onReplyToTweet(Tweet tweet) {
        Intent i = new Intent(this, ComposeActivity.class);
        i.putExtra(ComposeActivity.TWEET_EXTRA_KEY, tweet);
        startActivityForResult(i, COMPOSE_REQUEST);
    }

    public void onFavoriteTweet(final Tweet tweet, final View tweetContainer) {
        Log.d("DBG", "Beginning favorite operation");
        mRefreshItem.setVisible(true);
        client.performFavoriteTweet(tweet, new JsonHttpResponseHandler() {

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
                        String toastMsg = String.format("Successfully retweeted @%s's tweet", t.getUser().getScreenName());
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
}
