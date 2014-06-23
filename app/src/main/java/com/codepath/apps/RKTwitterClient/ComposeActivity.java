package com.codepath.apps.RKTwitterClient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.RKTwitterClient.models.Tweet;
import com.codepath.apps.RKTwitterClient.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

import static com.codepath.apps.RKTwitterClient.util.Util.setupTextviewContents;

public class ComposeActivity extends Activity {
    User mUser;
    ImageView ivProfile;
    TextView mFreeCharacters;
    EditText mComposeEditText;
    public static String TWEET_EXTRA_KEY = "tweet_prefix";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_compose);
        initIvars();
        setupChrome();
        setInitialEditTextState();
        setupListeners();
        updateFreeCharactersLabel();

        super.onCreate(savedInstanceState);
    }

    private void setupListeners() {
        mComposeEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                updateFreeCharactersLabel();
                return false;
            }
        });

        TwitterApplication.getRestClient().getUser(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                mUser = User.fromJSON(jsonObject);
                ImageLoader.getInstance().displayImage(mUser.getProfileImageURL(), ivProfile);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        View v = getRoot();
                        setupTextviewContents(v, R.id.tvUserName, mUser.getName());
                        setupTextviewContents(v, R.id.tvUserScreenName, String.format("@%s", mUser.getScreenName(), "@"));
                    }
                });
            }
        });
    }

    private void setupChrome() {
        getActionBar().hide();
        getProgressBar().setVisibility(View.GONE);
    }

    private void setInitialEditTextState() {
        Intent i = getIntent();
        if (i.hasExtra(TWEET_EXTRA_KEY)) {
            Tweet theTweet = (Tweet)i.getSerializableExtra(TWEET_EXTRA_KEY);
            mComposeEditText.setText(String.format("@%s ", theTweet.getUser().getScreenName()));
            mComposeEditText.setSelection(mComposeEditText.length());
        }
        else {
            mComposeEditText.setText("");
        }
        updateFreeCharactersLabel();
    }

    private void initIvars() {
        ivProfile = (ImageView)findViewById(R.id.ivProfileImage);
        mFreeCharacters = (TextView)findViewById(R.id.tvCharacterCount);
        mComposeEditText = (EditText)findViewById(R.id.etTweetCompose);
    }

    void updateFreeCharactersLabel() {
        mFreeCharacters.setText(String.format("%d", 140 - mComposeEditText.getText().length()));
    }

    View getRoot() {
        View v = findViewById(R.id.vgComposeRoot);
        return v;
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
        EditText et = (EditText)findViewById(R.id.etTweetCompose);
        String tweetText = et.getText().toString();
        TwitterClient client = TwitterApplication.getRestClient();
        getProgressBar().setVisibility(View.VISIBLE);

        client.updateStatus(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject object) {
                getProgressBar().setVisibility(View.GONE);
                Toast.makeText(ComposeActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent();
                i.putExtra("mostCurrentID", Tweet.fromJSON(object));
                ComposeActivity.this.setResult(RESULT_OK, i);
                finish();
            }

            @Override
            public void onFailure(Throwable throwable, String s) {
                getProgressBar().setVisibility(View.GONE);
                Log.e("DBG", throwable.toString());
                Log.e("DBG", s);
                Toast.makeText(ComposeActivity.this, "Tweet failed!", Toast.LENGTH_SHORT).show();
                super.onFailure(throwable, s);
            }
        }, tweetText);

    }
    ProgressBar getProgressBar() {
        return (ProgressBar)findViewById(R.id.progressBarCompose);
    }
}
