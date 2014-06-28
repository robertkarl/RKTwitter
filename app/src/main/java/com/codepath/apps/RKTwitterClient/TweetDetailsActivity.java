package com.codepath.apps.RKTwitterClient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.RKTwitterClient.models.Tweet;
import com.codepath.apps.RKTwitterClient.models.User;
import com.codepath.apps.RKTwitterClient.util.Util;
import com.loopj.android.image.SmartImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import static com.codepath.apps.RKTwitterClient.util.Util.setupTextviewContents;

public class TweetDetailsActivity extends Activity {
    SmartImageView mPreviewImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_details);

        Intent i = getIntent();
        Tweet tweet = (Tweet)i.getSerializableExtra("tweet");


        View v = findViewById(R.id.rlDetailsRoot);
        setupTextviewContents(v, R.id.tvUserName, tweet.user.getName());
        setupTextviewContents(v, R.id.tvUserScreenName, String.format("@%s", tweet.user.getScreenName(), "@"));
        String tweetText;
        if (tweet.retweeted_status != null) {
            tweetText = tweet.getRetweetedText();
        }
        else {
            tweetText = tweet.body;
        }
        setupTextviewContents(v, R.id.tvBody, tweetText);
        setupTextviewContents(v, R.id.tvTimestamp, tweet.absoluteDate);

        mPreviewImage = (SmartImageView)findViewById(R.id.ivTweetImagePreview);
        mPreviewImage.setImageUrl(tweet.mediaURL);
        ImageView profileImage= (ImageView)findViewById(R.id.ivProfileImage);
        ImageLoader.getInstance().displayImage(tweet.user.getProfileImageURL(), profileImage);
        setupRetweetBanner(v, tweet);

        Util.setActionBarDrawable(getActionBar(), new ColorDrawable(getResources().getColor(R.color.twitterBlue)));
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    void setupRetweetBanner(View v, Tweet tweet) {
        ViewGroup retweetBanner = (ViewGroup)v.findViewById(R.id.llRetweetContainer);
        if (tweet.retweeted_status != null) {
            retweetBanner.setVisibility(View.VISIBLE);
            TextView tvRetweeter = (TextView)v.findViewById(R.id.tvRetweeterLabel);
            User originalTweeter = tweet.retweeted_status.user;
            tvRetweeter.setText(String.format("%s retweeted", originalTweeter .getScreenName()));
        }
        else {
            retweetBanner.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onMenuItemSelected(featureId, item);
    }
}
