package com.codepath.apps.RKTwitterClient;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.codepath.apps.RKTwitterClient.fragments.HomeTimelineFragment;
import com.codepath.apps.RKTwitterClient.fragments.MentionsFragment;
import com.codepath.apps.RKTwitterClient.fragments.TweetsListFragment;

public class TimelinePagerAdapter extends FragmentPagerAdapter {

    TweetsListFragment fragments[];

    /**
     * Passed to fragments.
     */
    public TweetsListFragment.TweetsListListener listener;

    public TimelinePagerAdapter(FragmentManager fragmentManager, TweetsListFragment.TweetsListListener theListener) {
        super(fragmentManager);
        listener = theListener;
        fragments = new TweetsListFragment[2];
    }

    @Override
    public Fragment getItem(int i) {
        createFragmentsIfNeeded();
        return fragments[i];
    }

    private void createFragmentsIfNeeded() {
        if (fragments[0] == null) {
            fragments[0] = new HomeTimelineFragment();
            fragments[1] = new MentionsFragment();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        createFragmentsIfNeeded();
        return fragments[position].getTitle();
    }
}
