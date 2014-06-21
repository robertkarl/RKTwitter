package com.codepath.apps.RKTwitterClient;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.loopj.android.http.AsyncHttpResponseHandler;

public class ComposeActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_compose);
        getProgressBar().setVisibility(View.GONE);
        super.onCreate(savedInstanceState);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.compose, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_tweet:
                onTweet();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onTweet() {
        EditText et = (EditText)findViewById(R.id.tvTweetCompose);
        String tweetText = et.getText().toString();
        TwitterClient client = TwitterApplication.getRestClient();
        getProgressBar().setVisibility(View.VISIBLE);
        client.updateStatus(new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String s) {
                getProgressBar().setVisibility(View.GONE);
                super.onSuccess(s);
            }

            @Override
            public void onFailure(Throwable throwable, String s) {
                getProgressBar().setVisibility(View.GONE);
                Log.e("DBG", throwable.toString());
                Log.e("DBG", s);
                super.onFailure(throwable, s);
            }
        }, tweetText);

    }
    ProgressBar getProgressBar() {
        return (ProgressBar)findViewById(R.id.progressBarCompose);
    }
}
