package com.codepath.apps.RKTwitterClient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.RKTwitterClient.models.Tweet;
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

        setupProfileImage(v, tweet);

        setText(v, R.id.tvUserName, tweet.getUser().getScreenName());
        setText(v, R.id.tvBody, tweet.getBody());
        setText(v, R.id.tvRelativeTimestamp, tweet.getRelativeDate());

        return v;
    }

    void setText(View parentView, int textViewID, String textValue) {
        TextView tvUserName = (TextView) parentView.findViewById(textViewID);
        tvUserName.setText(textValue);
    }

    void setupProfileImage(View v, Tweet tweet) {
        ImageView ivProfileImage = (ImageView)v.findViewById(R.id.ivProfileImage);
        ivProfileImage.setImageResource(getContext().getResources().getColor(android.R.color.transparent));
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(tweet.getUser().getProfileImageURL(), ivProfileImage);
    }
}
