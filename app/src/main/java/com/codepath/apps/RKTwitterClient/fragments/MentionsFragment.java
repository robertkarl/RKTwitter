package com.codepath.apps.RKTwitterClient.fragments;

import android.util.Log;
import android.view.View;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;

public class MentionsFragment extends TweetsListFragment {
    public String getTitle() {
        return "Mentions";
    }

    public void clearAndPopulate() {
        client.getMentionsTimeline(new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONArray jsonArray) {
                Log.d("DBG", jsonArray.toString());
                clearTweets();
                unpackTweetsFromJSON(jsonArray);
                getProgressBar().setVisibility(View.GONE);
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
