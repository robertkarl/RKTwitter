package com.codepath.apps.RKTwitterClient.fragments;

import com.codepath.apps.RKTwitterClient.TwitterClient;

public class MentionsFragment extends TweetsListFragment {
    public String getTitle() {
        return "Mentions";
    }

    public void clearAndPopulate() {
        client.getMentionsTimeline(makeUnpackingRefreshingJsonHandler());
    }

    protected String getEndpoint() {
        return TwitterClient.MENTIONS_ENDPOINT;
    }


}
