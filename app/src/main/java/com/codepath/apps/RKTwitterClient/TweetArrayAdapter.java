package com.codepath.apps.RKTwitterClient;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.RKTwitterClient.models.Tweet;
import com.codepath.apps.RKTwitterClient.models.User;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class TweetArrayAdapter extends ArrayAdapter<Tweet> {

    public TweetArrayAdapter(Context context, List<Tweet> tweets) {
        super(context, 0, tweets);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Tweet tweet = getItem(position);
        View v;
        if (convertView == null) {
            LayoutInflater inflator = LayoutInflater.from(getContext());
            v = inflator.inflate(R.layout.tweet_item, parent, false);
        }
        else {
            v = convertView;
        }

        Typeface robotoMedium = Typeface.createFromAsset(getContext().getAssets(), "Roboto/Roboto-Medium.ttf");
        TextView userName = (TextView)v.findViewById(R.id.tvUserName);
        userName.setTypeface(robotoMedium);

        setupProfileImage(v, tweet);

        setupTextviewContents(v, R.id.tvUserName, tweet.getUser().getName());
        setupTextviewContents(v, R.id.tvUserScreenName, String.format("@%s", tweet.getUser().getScreenName(), "@"));
        setupTextviewContents(v, R.id.tvBody, tweet.getBody());
        setupTextviewContents(v, R.id.tvRelativeTimestamp, tweet.getRelativeDate());

        setupRetweetBanner(v, tweet);

        return v;
    }

    void setupTextviewContents(View parentView, int textViewID, String textValue) {
        TextView tvUserName = (TextView) parentView.findViewById(textViewID);
        tvUserName.setText(textValue);
    }

    void setupRetweetBanner(View v, Tweet tweet) {
        ViewGroup retweetBanner = (ViewGroup)v.findViewById(R.id.llRetweetContainer);
        if (tweet.retweeted_status != null) {
            retweetBanner.setVisibility(View.VISIBLE);
            TextView tvRetweeter = (TextView)v.findViewById(R.id.tvRetweeterLabel);
            User originalTweeter = tweet.retweeted_status.getUser();
            tvRetweeter.setText(String.format("%s retweeted", originalTweeter .getScreenName()));
        }
        else {
            retweetBanner.setVisibility(View.GONE);
        }

    }

    void setupProfileImage(View v, Tweet tweet) {
        ImageView ivProfileImage = (ImageView)v.findViewById(R.id.ivProfileImage);
        ivProfileImage.setImageResource(getContext().getResources().getColor(android.R.color.transparent));
        ImageLoader imageLoader = ImageLoader.getInstance();
        User user = tweet.getUser();
        imageLoader.displayImage(user.getProfileImageURL(), ivProfileImage);
    }
}
