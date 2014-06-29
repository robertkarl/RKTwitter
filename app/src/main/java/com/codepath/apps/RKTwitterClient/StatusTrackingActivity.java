package com.codepath.apps.RKTwitterClient;

import android.support.v4.app.FragmentActivity;

public class StatusTrackingActivity extends FragmentActivity {

    public boolean mIsRunning;

    @Override
    protected void onResume() {
        mIsRunning = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        mIsRunning = false;
        super.onPause();
    }
}
