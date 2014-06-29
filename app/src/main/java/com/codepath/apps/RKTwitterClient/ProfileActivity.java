package com.codepath.apps.RKTwitterClient;

import android.os.Bundle;

import com.codepath.apps.RKTwitterClient.fragments.TweetsListFragment;
import com.codepath.apps.RKTwitterClient.models.Tweet;

public class ProfileActivity extends StatusTrackingActivity implements TweetsListFragment.TweetsListListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
    }

    @Override
    public void onConnectionRegained() {

    }

    @Override
    public void onConnectionLost() {

    }

    @Override
    public void onTweetClicked(Tweet tweet) {

    }

    @Override
    public void onTriggerInfiniteScroll() {
        /// Ignore these
    }
}
