package com.codepath.apps.RKTwitterClient.models;

import android.text.format.DateUtils;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

@Table(name = "Tweets")
public class Tweet extends Model implements Serializable {

    public static long OLDEST_TWEET = Long.MAX_VALUE;

    @Column(name = "body")
    private String body;
    @Column(name = "created_at")
    private String createdAt;
    @Column(name = "user")
    private User user;
    @Column(name = "relative_date")
    private String relativeDate;
    @Column(name = "remote_id", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private long ID;

    @Column
    public String absoluteDate;

    @Column(name = "retweet_count")
    private int retweetCount;

    public Tweet retweeted_status;

    public long getID() {
        return ID;
    }

    public String getRelativeDate() {
        return relativeDate;
    }

    public String getBody() {
        return body;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    public Tweet() {
        super();
    }

    public int getRetweetCount() {
        return retweetCount;
    }

    @Column
    public boolean retweeted;

    @Column(name = "urls")
    public ArrayList<TwitterURL> urls;

    @Column
    public String mediaURL;

    public static Tweet fromJSON(JSONObject object) {
        Tweet tweet = new Tweet();
        try {
            tweet.body = object.getString("text");
            tweet.ID = object.getLong("id");
            tweet.retweeted = object.getBoolean("retweeted");
            tweet.retweetCount = object.getInt("retweet_count");
            tweet.createdAt = object.getString("created_at");
            tweet.user = User.fromJSON(object.getJSONObject("user"));
            tweet.relativeDate = Tweet.getRelativeTimeAgo(tweet.createdAt);
            tweet.absoluteDate = Tweet.getAbsoluteTime(tweet.createdAt);

            tweet.retweeted_status = attemptGetRetweet(object);
            tweet.urls = Tweet.attemptLoadURLs(object);

            JSONObject entities = object.getJSONObject("entities");
            if (entities.has("media")) {
                JSONObject media = entities.getJSONArray("media").getJSONObject(0); // Ignore subsequent media
                if (media.has("media_url")) {
                    tweet.mediaURL = media.getString("media_url");
                }
            }

            tweet.user.save();
            tweet.save();
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return tweet;
    }

    public static Tweet attemptGetRetweet(JSONObject tweet) throws JSONException{
        if (tweet.has("retweeted_status")) {
            Tweet retweetedStatus = Tweet.fromJSON(tweet.getJSONObject("retweeted_status"));
            int x = 0;
            x++;
            return retweetedStatus;
        }
        return null;
    }


    public static ArrayList<Tweet> fromJSONArray(JSONArray array) {
        ArrayList<Tweet> tweets = new ArrayList<Tweet>(array.length());

        for (int i = 0; i < array.length(); i++) {
            JSONObject tweetJson = null;
            try {
                tweetJson = array.getJSONObject(i);

            }
            catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            Tweet tweet = Tweet.fromJSON(tweetJson);
            if (tweet != null) {
                tweets.add(tweet);
            }
        }
        return tweets;
    }

    public static ArrayList<TwitterURL> attemptLoadURLs(JSONObject object) throws JSONException {
        ArrayList<TwitterURL> result = new ArrayList<TwitterURL>();
        if (!object.getJSONObject("entities").has("urls")) {
            return null;
        }

        JSONArray urls = object.getJSONObject("entities").getJSONArray("urls");
        for (int i = 0; i < urls.length(); i++) {
            JSONObject urlJSON = urls.getJSONObject(i);
            TwitterURL url = TwitterURL.fromJSON(urlJSON);
            result.add(url);
        }
        return result;
    }

    @Override
    public String toString() {
        return  String.format("%s - %s", getBody(), getUser());
    }

    public static String getAbsoluteTime(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String absoluteDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            absoluteDate = DateUtils.formatSameDayTime(dateMillis, System.currentTimeMillis(), DateFormat.MEDIUM, DateFormat.MEDIUM).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return absoluteDate;
    }

    public static String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        HashMap<String, String> replaceMappings = new HashMap<String, String>();
        replaceMappings.put(" hours ago", "h");
        replaceMappings.put(" hour ago", "h");
        replaceMappings.put(" minutes ago", "m");
        replaceMappings.put(" minute ago", "m");
        replaceMappings.put(" seconds ago", "s");
        replaceMappings.put(" second ago", "s");
        replaceMappings.put(" day ago", "d");
        replaceMappings.put(" days ago", "d");
        for (String suffixKey: replaceMappings.keySet()) {
            if (relativeDate.endsWith(suffixKey)) {
                relativeDate = relativeDate.replace(suffixKey, replaceMappings.get(suffixKey));
            }
        }

        return relativeDate;
    }
}
