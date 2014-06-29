package com.codepath.apps.RKTwitterClient;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.codepath.apps.RKTwitterClient.fragments.HomeTimelineFragment;
import com.codepath.apps.RKTwitterClient.fragments.TweetsListFragment;
import com.codepath.apps.RKTwitterClient.models.Tweet;
import com.codepath.apps.RKTwitterClient.models.User;
import com.codepath.apps.RKTwitterClient.util.Connectivity;
import com.codepath.apps.RKTwitterClient.util.Util;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

public class TimelineActivity extends StatusTrackingActivity implements TweetsListFragment.TweetsListListener {
    public static int COMPOSE_REQUEST = 1234;
    private TwitterClient client;
    private boolean mConnecting = false;

    private MenuItem mRefreshItem;

    public boolean mIsRunning = false;

    TimelinePagerAdapter tweetsListPagerAdapter;
    ViewPager timelinePager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        client = TwitterApplication.getRestClient();

        setContentView(R.layout.activity_timeline);
        setTitle("Home");

        setActionBarTwitterColor();


        tweetsListPagerAdapter = new TimelinePagerAdapter(getSupportFragmentManager(), this);
        timelinePager = (ViewPager)findViewById(R.id.vpTimelineFragmentContainer);
        timelinePager.setAdapter(tweetsListPagerAdapter);
        timelinePager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                getActionBar().setSelectedNavigationItem(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        setupTabs();

        checkBackForAConnection(0);

        User.fetchCurrentUser(null);

    }

    private void setupTabs() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(true);

        ActionBar.TabListener listener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                if (tab.getTag() == HomeTimelineFragment.FRAGMENT_NAME) {
                    timelinePager.setCurrentItem(0, true);
                }
                else {
                    timelinePager.setCurrentItem(1, true);
                }
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }
        };

        /// Create tabs -- 42:53
        ActionBar.Tab homeTab = actionBar.newTab()
                .setText("Home")
                .setIcon(android.R.drawable.ic_menu_today)
                .setTag("HomeTimelineFragment")
                .setTabListener(listener);

        /// Create tabs -- 42:53
        ActionBar.Tab mentionsTab = actionBar.newTab()
                .setText("Mentions")
                .setIcon(android.R.drawable.ic_menu_add)
                .setTag("MentionsFragment")
                .setTabListener(listener);

        actionBar.addTab(homeTab);
        actionBar.addTab(mentionsTab);


    }

    View getView() {
        return findViewById(R.id.rlDetailsRoot);
    }

    @Override
    protected void onResume() {
        Log.v("DBG", ((Object) this).getClass().getSimpleName() +  "onResume");
        super.onResume();
        mIsRunning = true;
    }

    @Override
    protected void onPause() {
        Log.v("DBG", ((Object) this).getClass().getSimpleName() +  "onPause");
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

    public boolean onProfileClicked(MenuItem item) {
        Intent i = new Intent(this, ProfileActivity.class);
        startActivity(i);
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

    public void onFavoriteTweet(final Tweet tweet, final ImageView favoriteImage) {
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
                        String toastMsg = String.format("Successfully favorited @%s's tweet", t.user.getScreenName());
                        Toast.makeText(TimelineActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                        TweetArrayAdapter.setListItemFavoritedState(favoriteImage, true);
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

    public void onRetweetClicked(final Tweet tweet, final ImageView retweetImage) {
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
                        String toastMsg = String.format("Successfully retweeted @%s's tweet", t.retweeted_status.user.getScreenName());
                        Toast.makeText(TimelineActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                        TweetArrayAdapter.setListItemRetweeted(retweetImage, true);
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

    public void onConnectionLost() {
        setNoNetworkBannerVisibility(View.VISIBLE);
        checkBackForAConnection(0);
    }
    @Override
    public void onConnectionRegained() {
        setNoNetworkBannerVisibility(View.GONE);
        mConnecting = false; // Stops any retrying that's in progress
    }

    @Override
    public void onTriggerInfiniteScroll() {

    }

    private void setNoNetworkBannerVisibility(int visibility) {
        View v = findViewById(R.id.vgDisconnectedBanner);
        v.setVisibility(visibility);
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
                if (!mConnecting) {
                    return;
                }
                if (Connectivity.isOnline(TimelineActivity.this)) {
                    setNoNetworkBannerVisibility(View.GONE);
                    if (delay != 0) {
                        Toast.makeText(TimelineActivity.this, "Welcome back", Toast.LENGTH_SHORT).show();
                        Log.d("DBG", String.format("found a connection again! delay would have been %d", delay));
                    }
                    mConnecting = false;
                } else {
                    Log.d("DBG", String.format("Checking server connection in %d millis", delay * 2));
                    checkBackForAConnection(delay == 0 ? 500 : delay * 2);
                    setNoNetworkBannerVisibility(View.VISIBLE);
                    /// For each fragment:
//                        showSavedTweets();
                }
            }
        }, delay);
    }


}
