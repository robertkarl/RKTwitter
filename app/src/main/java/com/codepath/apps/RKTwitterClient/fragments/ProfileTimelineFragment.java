package com.codepath.apps.RKTwitterClient.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.apps.RKTwitterClient.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;

public class ProfileTimelineFragment extends TweetsListFragment {

    public User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        user = User.getCurrentUserFromLocalDBSynchronous();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void fetchTheUsersTweets() {
        client.getProfileTimeline(user.uid, makeUnpackingRefreshingJsonHandler());
    }

    public void clearAndPopulate() {
        if (user == null) {
            toggleLoadingVisibility(true);
            // Fetch the user
            // When that's done, perform the fetch
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


    public void onTriggerInfiniteScroll() {
        client.fetchOlderTweets("mentions_timelineTODO",new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONArray jsonArray) {
                unpackTweetsFromJSON(jsonArray);
                listener.onConnectionRegained();
            }

            @Override
            public void onFailure(Throwable throwable, String s) {
                listener.onConnectionLost();
            }
        }, lastTweetID - 1);
    }


}
