package com.codepath.apps.RKTwitterClient;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.codepath.apps.RKTwitterClient.fragments.ProfileTimelineFragment;
import com.codepath.apps.RKTwitterClient.fragments.TweetsListFragment;
import com.codepath.apps.RKTwitterClient.models.Tweet;

public class ProfileActivity extends StatusTrackingActivity implements TweetsListFragment.TweetsListListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.vgProfileTweetsList, ProfileTimelineFragment.newInstance(3), ProfileTimelineFragment.FRAGMENT_NAME);
        ft.commit();
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
