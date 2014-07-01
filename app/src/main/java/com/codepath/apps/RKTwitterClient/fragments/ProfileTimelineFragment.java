package com.codepath.apps.RKTwitterClient.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.codepath.apps.RKTwitterClient.R;
import com.codepath.apps.RKTwitterClient.models.User;
import com.codepath.apps.RKTwitterClient.util.Util;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;

public class ProfileTimelineFragment extends TweetsListFragment {

    public static String FRAGMENT_NAME = "profile_fragment";

    public User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey("user")) {
            user = (User) getArguments().getSerializable("user");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void fetchTheUsersTweets() {
        Log.v("dbg", "Profile view initiating fetch");
        client.getProfileTimeline(user.uid, makeUnpackingRefreshingJsonHandler());
    }

    public void clearAndPopulate() {
        Log.v("dbg", String.format("%s clearAndPopulate: %h", getTitle(), user));
        if (user == null) {
            toggleLoadingVisibility(true);
            // Fetch the user
            // When that's done, perform the data fetch
            User.fetchCurrentUser(new User.UserLoadedCallback() {
                @Override
                public void onUserLoaded(User user) {
                    ProfileTimelineFragment.this.user = user;
                    fetchTheUsersTweets();
                }
            });

        }
        else {
            fetchTheUsersTweets();
        }
    }

    public static ProfileTimelineFragment newInstance(int numberOfTweetsToLoad, User userToDisplay) {
        ProfileTimelineFragment frag = new ProfileTimelineFragment();
        Bundle args = new Bundle();
        if (numberOfTweetsToLoad > 0) {
            args.putInt(TweetsListFragment.TWEET_COUNT_KEY, numberOfTweetsToLoad);
        }
        args.putSerializable("user", userToDisplay);
        frag.setArguments(args);
        return frag;
    }

    JsonHttpResponseHandler makeUnpackingRefreshingJsonHandler() {
        return new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONArray jsonArray) {
                Log.v("dbg", String.format("%s onSuccess called", getTitle()));
                clearTweets();
                unpackTweetsFromJSON(jsonArray);
                Util.setListViewHeightBasedOnChildren((ListView) getView().findViewById(R.id.lvTweetsFragmentList));
                completeRefreshIfNeeded(true);
                setActionBarTwitterColor();
                getListener().onConnectionRegained();
            }

            @Override
            public void onFailure(Throwable throwable, JSONArray jsonArray) {
                super.onFailure(throwable, jsonArray);
                getListener().onConnectionLost();
                completeRefreshIfNeeded(false);
                Log.e("DBG", String.format("Timeline populate failed %s %s", throwable.toString(), jsonArray.toString()));
            }
        };
    }


    public String getTitle() {
        return "Profile";
    }

    public void onTriggerInfiniteScroll() {
        client.fetchOlderTweets("mentions_timelineTODO",new JsonHttpResponseHandler() {

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


}
