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

    public TimelinePagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
        fragments = new TweetsListFragment[2];
    }

    @Override
    public Fragment getItem(int i) {
        return fragments[i];
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (fragments[0] == null) {
            fragments[0] = new HomeTimelineFragment();
            fragments[1] = new MentionsFragment();
            fragments[0].listener = fragments[1].listener = listener;
        }
        return fragments[position].getTitle();
    }
}
