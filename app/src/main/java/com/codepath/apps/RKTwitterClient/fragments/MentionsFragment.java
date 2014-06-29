package com.codepath.apps.RKTwitterClient.fragments;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;

public class MentionsFragment extends TweetsListFragment {
    public String getTitle() {
        return "Mentions";
    }

    public void clearAndPopulate() {
        client.getMentionsTimeline(makeUnpackingRefreshingJsonHandler());
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
