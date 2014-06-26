package com.codepath.apps.RKTwitterClient.fragments;

import android.app.Fragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.codepath.apps.RKTwitterClient.R;
import com.codepath.apps.RKTwitterClient.TweetArrayAdapter;
import com.codepath.apps.RKTwitterClient.TwitterApplication;
import com.codepath.apps.RKTwitterClient.TwitterClient;
import com.codepath.apps.RKTwitterClient.models.Tweet;
import com.codepath.apps.RKTwitterClient.util.Connectivity;
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
    TweetsListListener listener;
    private PullToRefreshLayout pullToRefreshLayout;

    private boolean mConnecting = false;

    public TweetsListFragment() {

    }

    public void addAll(List<Tweet> tweets) {
        tweetsAdapter.addAll(tweets);
    }

    public void clearTweets() {
        tweetsAdapter.clear();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_tweets_list, container, false);
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

        listener.onClearAndPopulate();

        pullToRefreshLayout = (PullToRefreshLayout)v.findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(getActivity()).allChildrenArePullable()
                .listener(new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View view) {
                        listener.onClearAndPopulate();
                    }
                })
                .setup(pullToRefreshLayout);

        checkBackForAConnection(0);

        return v;
    }


    void setActionBarTwitterColor() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Util.setActionBarDrawable(getActivity().getActionBar(), new ColorDrawable(getResources().getColor(R.color.twitterBlue)));
            }
        });
    }


    private void completeRefreshIfNeeded(boolean refreshSucceeded) {
        if (pullToRefreshLayout.isRefreshing()) {
            pullToRefreshLayout.setRefreshComplete();
            String message = refreshSucceeded ? "Refresh completed!" : "Please reconnect and try again";
            Toast.makeText(getActivity(), message , Toast.LENGTH_SHORT).show();
        }
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
                if (Connectivity.isOnline(getActivity())) {
                    setNoNetworkBannerVisibility(View.GONE);
                    if (delay != 0) {
                        Toast.makeText(getActivity(), "Welcome back", Toast.LENGTH_SHORT).show();
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

    void showSavedTweets() {
        getProgressBar().setVisibility(View.GONE);
        tweetsAdapter.clear();
        List<Tweet> storedTweets = new Select().from(Tweet.class).execute();
        tweetsAdapter.addAll(storedTweets);
        tweetsAdapter.notifyDataSetChanged();

        setNoNetworkBannerVisibility(View.VISIBLE);
    }


    public interface TweetsListListener {
        void onTweetClicked(Tweet tweet);
        void onTriggerInfiniteScroll();
        void onClearAndPopulate();
    }



}
