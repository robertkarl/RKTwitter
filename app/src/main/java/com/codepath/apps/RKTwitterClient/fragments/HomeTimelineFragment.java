package com.codepath.apps.RKTwitterClient.fragments;

import com.codepath.apps.RKTwitterClient.TwitterClient;

public class HomeTimelineFragment extends TweetsListFragment {
    public static String FRAGMENT_NAME = "HomeTimelineFragment";
    public String getTitle() {
        return "Home";
    }

    protected String getEndpoint() {
        return TwitterClient.HOME_TIMELINE_ENDPOINT;
    }


}
