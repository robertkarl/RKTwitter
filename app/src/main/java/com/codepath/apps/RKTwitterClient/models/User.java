package com.codepath.apps.RKTwitterClient.models;

import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.codepath.apps.RKTwitterClient.TwitterApplication;
import com.codepath.apps.RKTwitterClient.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;


@Table(name = "User")
public class User extends Model implements Serializable {
    static String PROFILE_IMAGE_KEY = "profile_image_url";
    static String PROFILE_BANNER_KEY = "profile_banner_url";
    static String FOLLOWERS_KEY = "followers_count";
    static String FOLLOWING_KEY = "friends_count";
    static String STATUSES_COUNT = "statuses_count";

    public static User currentlyAuthenticatedUser;

    public static String currentUsername;

    public boolean isAuthenticatedUser;

    @Column
    private String name;

    @Column(name="remote_id")
    public long uid;

    @Column
    private String screenName;

    @Column(name="profile_image_url")
    private String profileImageURL;

    @Column(name="profile_banner_url")
    public String profileBannerUrl;

    @Column
    public int tweetCount;

    @Column
    public int followerCount;

    @Column
    public int followingCount;

    public String getName() {
        return name;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getProfileImageURL() {
        return profileImageURL;
    }

    public static User fromJSON(JSONObject object) {
        User user = new User();
        try {
            user.name = object.getString("name");
            user.uid = object.getLong("id");
            if (currentlyAuthenticatedUser != null && user.uid == currentlyAuthenticatedUser.uid) {
                user.isAuthenticatedUser = true;
            }
            user.screenName = object.getString("screen_name");
            if (object.has(FOLLOWERS_KEY)) {
                user.followerCount = object.getInt("followers_count");
            }
            if (object.has(FOLLOWING_KEY)) {
                user.followingCount = object.getInt(FOLLOWING_KEY);
            }
            if (object.has(STATUSES_COUNT)) {
                user.tweetCount = object.getInt(STATUSES_COUNT);
            }
            if (object.has(PROFILE_BANNER_KEY)) {
                user.profileBannerUrl = object.getString(PROFILE_BANNER_KEY);
            }
            if (object.has(PROFILE_IMAGE_KEY)) {
                user.profileImageURL = object.getString(PROFILE_IMAGE_KEY).replace("_normal", "");
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return user;
    }

    public interface UserLoadedCallback {
        void onUserLoaded(User user);
    }

    public static void fetchCurrentUser(final UserLoadedCallback userLoadedCallback) {
        TwitterClient client = TwitterApplication.getRestClient();
        client.getUser(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    User user = User.fromJSON(jsonObject);
                    user.isAuthenticatedUser = true;
                    currentlyAuthenticatedUser = user;
                    Log.d("dbg", String.format("Completed fetching user %s", user.getScreenName()));
                    if (userLoadedCallback != null) {
                        userLoadedCallback.onUserLoaded(user);
                    }
                }

            @Override
            public void onFailure(Throwable throwable, JSONObject jsonObject) {
                onFailureReported(throwable);
                super.onFailure(throwable, jsonObject);
            }

            @Override
            public void onFailure(Throwable throwable, JSONArray jsonArray) {
                onFailureReported(throwable);
                super.onFailure(throwable, jsonArray);
            }

            void onFailureReported(Throwable t) {
                Log.d("DBG", "Failed to load the current user");
                Log.e("DBG", t.toString());
                if (userLoadedCallback != null) {
                    userLoadedCallback.onUserLoaded(null);
                }
            }

            @Override
                public void onFailure(Throwable throwable, String s) {
                onFailureReported(throwable);
                }
            });
    }

}
