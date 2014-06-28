package com.codepath.apps.RKTwitterClient;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.Log;
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

import static com.codepath.apps.RKTwitterClient.util.Util.setupTextviewContents;

public class TweetArrayAdapter extends ArrayAdapter<Tweet> {

    public TweetArrayAdapter(Context context, List<Tweet> tweets) {
        super(context, 0, tweets);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Tweet tweet = getItem(position);
        View v;
        if (convertView == null) {
            LayoutInflater inflator = LayoutInflater.from(getContext());
            v = inflator.inflate(R.layout.tweet_item, parent, false);
        }
        else {
            v = convertView;
        }
        final View tweetContainerView = v;

        setupUsername(v);
        setupProfileImage(v, tweet);
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
        setupTextviewContents(v, R.id.tvRelativeTimestamp, tweet.relativeDate);
        String favoriteText = tweet.favoriteCount == 0 ? "" : String.format("%d", tweet.favoriteCount);
        setupTextviewContents(v, R.id.tvFavoriteCount, favoriteText);
        String retweetText = tweet.retweetCount == 0 ? "" : String.format("%d", tweet.retweetCount);
        setupTextviewContents(v, R.id.tvRetweetCount, retweetText);
        setListItemFavoritedState(v, tweet.favorited);
        setListItemRetweeted(v, tweet.retweeted);

        ImageView replyImage = (ImageView)v.findViewById(R.id.ivReply);
        replyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TimelineActivity)getContext()).onReplyToTweet(tweet);
            }
        });

        ImageView favoriteImage = (ImageView)v.findViewById(R.id.ivFavorite);
        favoriteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimelineActivity activity = (TimelineActivity)getContext();
                activity.onFavoriteTweet(tweet, tweetContainerView);
            }
        });

        ImageView retweetedImage = (ImageView)v.findViewById(R.id.ivRetweet);
        retweetedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimelineActivity activity = (TimelineActivity)getContext();
                activity.onRetweetClicked(tweet, tweetContainerView);
            }
        });

        setupTweetBody(tweet, v);

        setupRetweetBanner(v, tweet);

        return v;
    }

    /**
     * Show a given list item as favorited. Don't allow for unfavoriting at all yet.
     */
    public static void setListItemFavoritedState(View tweetContainer, boolean favorited) {
        ImageView iv = (ImageView)tweetContainer.findViewById(R.id.ivFavorite);
        int starID = favorited ? R.drawable.ic_star_gold : R.drawable.ic_star;
        iv.setImageResource(starID);
        iv.setEnabled(!favorited);
    }

    /**
     * Show a given list item as having been retweeted by the current user.
     */
    public static void setListItemRetweeted(View tweetContainer, boolean retweeted) {
        ImageView iv = (ImageView)tweetContainer.findViewById(R.id.ivRetweet);
        int retweetIconID = retweeted ? R.drawable.ic_retweet_blue: R.drawable.ic_retweet;
        iv.setImageResource(retweetIconID);
        iv.setEnabled(!retweeted);
    }

    private void setupUsername(View v) {
        Typeface robotoMedium = Typeface.createFromAsset(getContext().getAssets(), "Roboto/Roboto-Medium.ttf");
        TextView userName = (TextView)v.findViewById(R.id.tvUserName);
        userName.setTypeface(robotoMedium);
    }

    private void setupTweetBody(final Tweet tweet, View v) {
        TextView body = (TextView)v.findViewById(R.id.tvBody);
        final TimelineActivity activity = (TimelineActivity)getContext();
        body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DBG", String.format("onClick"));
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((Activity)getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!activity.mIsRunning) {
                                    Log.d("DBG", "Detected a click on a URL. ignoring.");
                                    return;
                                }
                                else {
                                    activity.onTweetClicked(tweet);
                                }
                            }
                        });
                    }
                }, 10);
            }
        });
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

    void setupProfileImage(View v, Tweet tweet) {
        ImageView ivProfileImage = (ImageView)v.findViewById(R.id.ivProfileImage);
        ivProfileImage.setImageResource(getContext().getResources().getColor(android.R.color.transparent));
        ImageLoader imageLoader = ImageLoader.getInstance();
        User user = tweet.user;
        imageLoader.displayImage(user.getProfileImageURL(), ivProfileImage);
    }
}
