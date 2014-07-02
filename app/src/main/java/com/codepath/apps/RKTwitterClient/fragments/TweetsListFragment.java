package com.codepath.apps.RKTwitterClient.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
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
import com.codepath.apps.RKTwitterClient.models.User;
import com.codepath.apps.RKTwitterClient.util.Connectivity;
import com.codepath.apps.RKTwitterClient.util.EndlessScrollListener;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public abstract class TweetsListFragment extends Fragment {

    public static String TWEET_COUNT_KEY = "TWEET_COUNT";
    static int REVEAL_DURATION_MILLIS = 300;

    protected int numberOfTweetsToLoad;

    private ArrayList<Tweet> tweets;
    private TweetArrayAdapter tweetsAdapter;
    private ListView lvTweets;
    PullToRefreshLayout pullToRefreshLayout;
    TwitterClient client;
    private View rootView;

    long lastTweetID = -1;
    boolean mConnecting;

    TweetsListFragment() {
        super();
        Log.v("DBG", String.format("Constructing %s fragment", getTitle()));
    }

    JsonHttpResponseHandler makeUnpackingRefreshingJsonHandler() {
        return new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONArray jsonArray) {
                Log.v("dbg", String.format("%s onSuccess called", getTitle()));
                clearTweets();
                unpackTweetsFromJSON(jsonArray);
                completeRefreshIfNeeded(true);
                getListener().onConnectionRegained();
            }

            @Override
            public void onFailure(Throwable throwable, JSONObject jsonObject) {
                onFailureReported();
            }

            @Override
            public void onFailure(Throwable throwable, String s) {
                onFailureReported();
            }

            @Override
            public void onFailure(Throwable throwable, JSONArray jsonArray) {
                onFailureReported();
            }

            void onFailureReported() {
                getListener().onConnectionLost();
                completeRefreshIfNeeded(false);
                showSavedTweetsIfNeeded(true);
                Log.e("DBG", "Timeline populate failed %s %s");
            }
        };
    }

    void addAll(List<Tweet> tweets) {
        Log.d("DBG", String.format("%s: Adding %d tweets to adapter", getTitle(), tweets.size()));
        tweetsAdapter.addAll(tweets);
        tweetsAdapter.notifyDataSetChanged();
    }

    public void clearTweets() {
        if (tweetsAdapter.isEmpty()) {
            Log.d("DBG", String.format("Not clearing an empty adapter"));
        }
        else {
            Log.d("DBG", String.format("%s: clearing the adapter with %d items", getTitle(), tweets.size()));
            tweetsAdapter.clear();
        }
    }

    protected TweetsListListener getListener() {
        return (TweetsListListener)getActivity();
    }

    public abstract String getTitle();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            numberOfTweetsToLoad = getArguments().getInt(TWEET_COUNT_KEY, -1);
        }
        else {
            numberOfTweetsToLoad = -1;
        }
        int q = 7;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_tweets_list, container, false);
        client = TwitterApplication.getRestClient();

        pullToRefreshLayout = (PullToRefreshLayout)rootView.findViewById(R.id.pullToRefresh);

        ActionBarPullToRefresh.from(getActivity()).allChildrenArePullable()
                .listener(new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View view) {
                        toggleLoadingVisibility(true);
                        clearAndPopulate();
                    }
                })
                .setup(pullToRefreshLayout);
        setupListView(rootView);

        clearAndPopulate();


        return rootView;
    }

    @Override
    public void onResume() {
        Log.v("DBG", ((Object) this).getClass().getSimpleName() +  "onResume");
        checkBackForAConnection(0);
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
                onTriggerInfiniteScroll();
            }
        });
        lvTweets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getListener().onTweetClicked(tweetsAdapter.getItem(position));
            }
        });
        tweets = new ArrayList<Tweet>();
        tweetsAdapter = new TweetArrayAdapter(getActivity(), tweets);
        lvTweets.setAdapter(tweetsAdapter);
    }

    public interface TweetsListListener {
        void onTweetClicked(Tweet tweet);
        void onConnectionLost();
        void onConnectionRegained();
    }


    void unpackTweetsFromJSON(final JSONArray jsonArray) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                final ArrayList<Tweet> receivedTweets = Tweet.fromJSONArray(jsonArray);
                TweetsListFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<Tweet> tweetsToAdd = new ArrayList<Tweet>();
                        Log.v("DBG", String.format("Loading %d of total %d tweets", numberOfTweetsToLoad, receivedTweets.size()));
                        if (numberOfTweetsToLoad == -1) {
                            tweetsToAdd.addAll(receivedTweets);
                        }
                        else {
                            for (int i = 0; i < numberOfTweetsToLoad; i++) {
                                tweetsToAdd.add(receivedTweets.get(i));
                            }
                        }
                        lastTweetID = getOldestTweetId(tweetsToAdd);
                        addAll(tweetsToAdd);
                        toggleLoadingVisibility(false);
                    }
                });
                return null;
            }
        }.execute();
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
        client.getHomeTimeline(makeUnpackingRefreshingJsonHandler());
    }


    protected void toggleLoadingVisibility(boolean showLoading) {
        Log.d("DBG", String.format("Beginning toggle %b", showLoading));
        final View loadingIndicator = getProgressBar();

        final int cx = loadingIndicator.getRight() / 2;
        final int cy = loadingIndicator.getBottom() / 2;
        float radius = Math.max(loadingIndicator.getWidth(), loadingIndicator.getHeight()) * 1.2f;

        if (showLoading) {

            ValueAnimator reveal = ViewAnimationUtils.createCircularReveal(loadingIndicator, cx, cy, 0, radius);

            reveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(final Animator animation) {
                    loadingIndicator.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(final Animator animation) {
                }
            });

            reveal.setDuration(REVEAL_DURATION_MILLIS );
            reveal.start();
        } else {
            ValueAnimator reveal = ViewAnimationUtils.createCircularReveal(loadingIndicator, cx, cy, radius, 0);

            reveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(final Animator animation) {
                    loadingIndicator.setVisibility(View.INVISIBLE);
                }
            });


            reveal.setDuration(REVEAL_DURATION_MILLIS);
            reveal.start();
        }
    }



    ProgressBar getProgressBar() {
        return (ProgressBar)rootView.findViewById(R.id.progressBar);
    }


    protected void completeRefreshIfNeeded(boolean refreshSucceeded) {
        if (pullToRefreshLayout.isRefreshing()) {
            pullToRefreshLayout.setRefreshComplete();
            String message = refreshSucceeded ? "Refresh completed!" : "Please reconnect and try again";
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }


    public void showSavedTweetsIfNeeded(boolean forceReload) {
        if (tweetsAdapter.getCount() == 0 || forceReload) {
            clearTweets();
            List<Tweet> storedTweets = getStoredTweets();
            Log.d("DBG", String.format("showing %d saved tweets", storedTweets == null? 0 : storedTweets.size()));
            if (storedTweets != null) {
                addAll(storedTweets);
            }
            toggleLoadingVisibility(false);
        }
    }

    protected List<Tweet> getStoredTweets() {
        List <Tweet> tweets = new Select().from(Tweet.class).orderBy("remote_id DESC").execute();
        Log.d("DBG", String.format("%s loading %d stored tweets", getTitle(), tweets.size()));
        return tweets;
    }

    protected abstract String getEndpoint();

    public void onTriggerInfiniteScroll() {
        client.fetchOlderTweets(getEndpoint(), new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONArray jsonArray) {
                unpackTweetsFromJSON(jsonArray);
                getListener().onConnectionRegained();
            }

            @Override
            public void onFailure(Throwable throwable, String s) {
                getListener().onConnectionLost();
            }
        }, lastTweetID - 1);
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
                    getListener().onConnectionRegained();
                    if (delay != 0) {
                        Toast.makeText(getActivity(), "Welcome back", Toast.LENGTH_SHORT).show();
                        Log.d("DBG", String.format("found a connection again! delay would have been %d", delay));
                    }
                    mConnecting = false;
                } else {
                    Log.d("DBG", String.format("Checking server connection in %d millis", delay * 2));
                    checkBackForAConnection(delay == 0 ? 500 : delay * 2);
                    getListener().onConnectionLost();
                    showSavedTweetsIfNeeded(false);
                }
            }
        }, delay);
    }

}
