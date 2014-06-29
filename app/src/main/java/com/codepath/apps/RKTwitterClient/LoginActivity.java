package com.codepath.apps.RKTwitterClient;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.codepath.apps.RKTwitterClient.util.Util;
import com.codepath.oauth.OAuthLoginActivity;

public class LoginActivity extends OAuthLoginActivity<TwitterClient> {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
        Util.setActionBarDrawable(getActionBar(), new ColorDrawable(getResources().getColor(R.color.twitterBlue)));
	}

    @Override
    public void onLoginSuccess() {
        Log.v("DBG", "LoginActivity: Login succeeded.");
    	Intent i = new Intent(this, TimelineActivity.class);
    	startActivity(i);
    }
    
    @Override
    public void onLoginFailure(Exception e) {
        e.printStackTrace();
    }

    // Click handler method for the button used to start OAuth flow
    // Uses the client to initiate OAuth authorization
    // This should be tied to a button used to timeline
    public void onLoginClicked(View v) {
        Log.d("DBG", "Attemping to connect to client.");
        getClient().connect();
    }

}
