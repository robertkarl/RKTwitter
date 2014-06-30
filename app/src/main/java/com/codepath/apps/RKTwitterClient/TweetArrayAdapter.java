package com.codepath.apps.RKTwitterClient;

import android.content.Context;
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
import com.codepath.apps.RKTwitterClient.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class TweetArrayAdapter extends ArrayAdapter<Tweet> {
    static int x = 0;

    public interface TweetActionsListener {
        void onTweetFavorited(Tweet tweet);
        void onTweetRetweeted(Tweet tweet);
        void onTweetClicked(Tweet tweet);
    }

    public TweetArrayAdapter(Context context, List<Tweet> tweets) {
        super(context, 0, tweets);
    }

    static class TweetViewHolder {
        TextView tvBody;
        TextView tvRelativeTimestamp;
        TextView tvUserName;
        TextView tvUserScreenName;
        TextView tvFavoriteCount;
        TextView tvRetweetCount;
        ImageView ivReply;
        ImageView ivFavorite;
        ImageView ivRetweet;
        TextView tvRetweeterLabel;
        ViewGroup llRetweetBanner;

        ImageView ivProfile;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Tweet tweet = getItem(position);
        View v;
        final TweetViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflator = LayoutInflater.from(getContext());
            v = inflator.inflate(R.layout.tweet_item, parent, false);
            holder = new TweetViewHolder();
            holder.tvRelativeTimestamp = (TextView)v.findViewById(R.id.tvRelativeTimestamp);
            holder.llRetweetBanner = (ViewGroup)v.findViewById(R.id.llRetweetContainer);
            holder.tvUserScreenName = (TextView)v.findViewById(R.id.tvUserScreenName);
            holder.tvRetweeterLabel = (TextView)v.findViewById(R.id.tvRetweeterLabel);
            holder.tvFavoriteCount = (TextView)v.findViewById(R.id.tvFavoriteCount);
            holder.tvRetweetCount = (TextView)v.findViewById(R.id.tvRetweetCount);
            holder.ivProfile = (ImageView)v.findViewById(R.id.ivProfileImage);
            holder.ivFavorite = (ImageView)v.findViewById(R.id.ivFavorite);
            holder.tvUserName = (TextView)v.findViewById(R.id.tvUserName);
            holder.ivRetweet = (ImageView)v.findViewById(R.id.ivRetweet);
            holder.ivReply = (ImageView)v.findViewById(R.id.ivReply);
            holder.tvBody = (TextView)v.findViewById(R.id.tvBody);
            v.setTag(holder);
        }
        else {
            v = convertView;
            holder = (TweetViewHolder)v.getTag();
        }

        setupProfileImage(holder.ivProfile, tweet);
        String usernameText = tweet.retweeted ?
                tweet.retweeted_status.user.getName() :
                tweet.user.getName();
        holder.tvUserName.setText(usernameText);
        holder.tvUserName.setTypeface(Util.getRobotoMedium(getContext()));
        String userScreenName = tweet.retweeted ?
                tweet.retweeted_status.user.getScreenName() :
                tweet.user.getScreenName();
        holder.tvUserScreenName.setText("@" + userScreenName);
        String tweetText;
        if (tweet.retweeted_status != null) {
            tweetText = tweet.getRetweetedText();
        }
        else {
            tweetText = tweet.body;
        }
        holder.tvBody.setText(tweetText);
        holder.tvRelativeTimestamp.setText(tweet.relativeDate);

        String favoriteText = tweet.favoriteCount == 0 ? "" : String.format("%d", tweet.favoriteCount);
        holder.tvFavoriteCount.setText(favoriteText);

        String retweetText = tweet.retweetCount == 0 ? "" : String.format("%d", tweet.retweetCount);
        holder.tvRetweetCount.setText(retweetText);

        setListItemFavoritedState(holder.ivFavorite, tweet.favorited);
        setListItemRetweeted(holder.ivRetweet, tweet.retweeted);

        holder.ivReply .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TimelineActivity) getContext()).onReplyToTweet(tweet);
            }
        });

        holder.ivFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimelineActivity activity = (TimelineActivity) getContext();
                activity.onFavoriteTweet(tweet, holder.ivFavorite);
            }
        });

        holder.ivRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimelineActivity activity = (TimelineActivity) getContext();
                activity.onRetweetClicked(tweet, holder.ivRetweet);
            }
        });

        setupTweetBody(tweet, holder.tvBody);

        setupRetweetBanner(holder.llRetweetBanner, holder.tvRetweeterLabel, tweet);

        return v;
    }

    /**
     * Show a given list item as favorited. Don't allow for unfavoriting at all yet.
     */
    public static void setListItemFavoritedState(ImageView favoritedImage, boolean favorited) {
        int starID = favorited ? R.drawable.ic_star_gold : R.drawable.ic_star;
        favoritedImage.setImageResource(starID);
        favoritedImage.setEnabled(!favorited);
    }

    /**
     * Show a given list item as having been retweeted by the current user.
     */
    public static void setListItemRetweeted(ImageView retweetImage, boolean retweeted) {
        int retweetIconID = retweeted ? R.drawable.ic_retweet_blue: R.drawable.ic_retweet;
        retweetImage.setImageResource(retweetIconID);
        retweetImage.setEnabled(!retweeted);
    }

    TweetActionsListener getListener() {
        return (TweetActionsListener)getContext();
    }

    private void setupTweetBody(final Tweet tweet, TextView body) {
        body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DBG", String.format("onClick"));
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final StatusTrackingActivity activity = (StatusTrackingActivity) getContext();
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!activity.mIsRunning) {
                                    Log.d("DBG", "Detected a click on a URL. ignoring.");
                                    return;
                                } else {
                                    getListener().onTweetClicked(tweet);
                                }
                            }
                        });
                    }
                }, 10);
            }
        });
    }

    void setupRetweetBanner(ViewGroup banner, TextView retweeterLabel, Tweet tweet) {
        if (tweet.retweeted_status != null) {
            banner.setVisibility(View.VISIBLE);
            retweeterLabel.setText(String.format("%s retweeted", tweet.user.getScreenName()));
        }
        else {
            banner.setVisibility(View.GONE);
        }

    }

    void setupProfileImage(ImageView profileImage, Tweet tweet) {
        profileImage.setImageResource(getContext().getResources().getColor(android.R.color.transparent));
        ImageLoader imageLoader = ImageLoader.getInstance();
        User user = tweet.user;
        if (tweet.retweeted) {
            imageLoader.displayImage(tweet.retweeted_status.user.getProfileImageURL(), profileImage);
        }
        else {
            imageLoader.displayImage(user.getProfileImageURL(), profileImage);
        }
    }

}
