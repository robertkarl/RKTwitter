package com.codepath.apps.RKTwitterClient.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.apps.RKTwitterClient.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;

public class ProfileTimelineFragment extends TweetsListFragment {

    public static String FRAGMENT_NAME = "profile_fragment";

    public User user;

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

    public static ProfileTimelineFragment newInstance(int numberOfTweetsToLoad) {
        ProfileTimelineFragment frag = new ProfileTimelineFragment();
        Bundle args = new Bundle();
        if (numberOfTweetsToLoad > 0) {
            args.putInt(TweetsListFragment.TWEET_COUNT_KEY, numberOfTweetsToLoad);
        }
        frag.setArguments(args);
        return frag;
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
