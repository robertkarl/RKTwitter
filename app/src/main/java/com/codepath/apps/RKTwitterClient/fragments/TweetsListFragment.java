package com.codepath.apps.RKTwitterClient.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import android.view.ViewAnimationUtils;

import com.activeandroid.query.Select;
import com.codepath.apps.RKTwitterClient.R;
import com.codepath.apps.RKTwitterClient.TweetArrayAdapter;
import com.codepath.apps.RKTwitterClient.TwitterApplication;
import com.codepath.apps.RKTwitterClient.TwitterClient;
import com.codepath.apps.RKTwitterClient.models.Tweet;
import com.codepath.apps.RKTwitterClient.util.EndlessScrollListener;
import com.codepath.apps.RKTwitterClient.util.Util;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class TweetsListFragment extends Fragment {

    private ArrayList<Tweet> tweets;
    private TweetArrayAdapter tweetsAdapter;
    private ListView lvTweets;
    public TweetsListListener listener;
    PullToRefreshLayout pullToRefreshLayout;
    TwitterClient client;

    long lastTweetID = -1;

    public void addAll(List<Tweet> tweets) {
        tweetsAdapter.addAll(tweets);
        tweetsAdapter.notifyDataSetChanged();
    }
    public void clearTweets() {
        tweetsAdapter.clear();
    }

    public String getTitle() {
        return "Tweets list";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_tweets_list, container, false);
        client = TwitterApplication.getRestClient();

        pullToRefreshLayout = (PullToRefreshLayout)v.findViewById(R.id.pullToRefresh);

        ActionBarPullToRefresh.from(getActivity()).allChildrenArePullable()
                .listener(new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View view) {
                        clearAndPopulate();
                    }
                })
                .setup(pullToRefreshLayout);
        setupListView(v);

        clearAndPopulate();


        return v;
    }

    @Override
    public void onResume() {
        Log.v("DBG", ((Object) this).getClass().getSimpleName() +  "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.v("DBG", ((Object) this).getClass().getSimpleName() +  "onPause");
        super.onPause();
    }

    private void setupListView(View v) {
        lvTweets = (ListView)v.findViewById(R.id.lvTweetsFragmentList);
        lvTweets.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                listener.onTriggerInfiniteScroll();
            }
        });
        lvTweets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.onTweetClicked(tweetsAdapter.getItem(position));
            }
        });
        tweets = new ArrayList<Tweet>();
        tweetsAdapter = new TweetArrayAdapter(getActivity(), tweets);
        lvTweets.setAdapter(tweetsAdapter);
    }


    void setActionBarTwitterColor() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Util.setActionBarDrawable(getActivity().getActionBar(), new ColorDrawable(getResources().getColor(R.color.twitterBlue)));
            }
        });
    }

    public interface TweetsListListener {
        void onTweetClicked(Tweet tweet);
        void onTriggerInfiniteScroll();
        void onConnectionLost();
        void onConnectionRegained();
    }


    void unpackTweetsFromJSON(JSONArray jsonArray) {
        ArrayList<Tweet> receivedTweets = Tweet.fromJSONArray(jsonArray);
        lastTweetID = getOldestTweetId(receivedTweets);
        addAll(receivedTweets);
    }

    long getOldestTweetId(ArrayList<Tweet> tweets) {
        long oldest = Tweet.OLDEST_TWEET;
        for (Tweet tweet: tweets) {
            if (tweet.ID < oldest) {
                oldest = tweet.ID;
            }
        }
        return oldest;
    }

    public void clearAndPopulate() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONArray jsonArray) {
                Log.d("DBG", jsonArray.toString());
                clearTweets();
                unpackTweetsFromJSON(jsonArray);
                toggleLoadingVisibility(false);
                completeRefreshIfNeeded(true);
                setActionBarTwitterColor();
                listener.onConnectionRegained();
            }

            @Override
            public void onFailure(Throwable throwable, String s) {
                listener.onConnectionLost();
                completeRefreshIfNeeded(false);
                Log.e("DBG", String.format("Timeline populate failed %s %s", throwable.toString(), s));
            }
        });
    }


    private void toggleLoadingVisibility(boolean showLoading) {
        final View loadingIndicator = getProgressBar();
        final View listHolder = getView().findViewById(R.id.lvTweetsFragmentList);

        final int cx = loadingIndicator.getRight() / 2;
        final int cy = loadingIndicator.getBottom() / 2;
        float radius = Math.max(loadingIndicator.getWidth(), loadingIndicator.getHeight()) * 1.2f;

        if (showLoading) {

            ValueAnimator reveal = ViewAnimationUtils.createCircularReveal(loadingIndicator, cx, cy, 0, radius);

            reveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(final Animator animation) {
                }

                @Override
                public void onAnimationEnd(final Animator animation) {
                    Log.v("DBG", "onAnimationEnd");
                }
            });

            reveal.setDuration(5000);
            reveal.start();
        } else {
            ValueAnimator reveal = ViewAnimationUtils.createCircularReveal(loadingIndicator, cx, cy, radius, 0);

            reveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(final Animator animation) {
                    loadingIndicator.setVisibility(View.INVISIBLE);
                }
            });

            reveal.setDuration(1000);
            reveal.start();
        }
    }



    ProgressBar getProgressBar() {
        return (ProgressBar)getView().findViewById(R.id.progressBar);
    }


    protected void completeRefreshIfNeeded(boolean refreshSucceeded) {
        if (pullToRefreshLayout.isRefreshing()) {
            pullToRefreshLayout.setRefreshComplete();
            String message = refreshSucceeded ? "Refresh completed!" : "Please reconnect and try again";
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }


    void showSavedTweets() {
        getProgressBar().setVisibility(View.GONE);
        clearTweets();
        List<Tweet> storedTweets = new Select().from(Tweet.class).execute();
        addAll(storedTweets);
    }



}
