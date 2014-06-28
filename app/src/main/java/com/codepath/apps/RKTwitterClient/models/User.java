package com.codepath.apps.RKTwitterClient.models;

import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.codepath.apps.RKTwitterClient.TwitterApplication;
import com.codepath.apps.RKTwitterClient.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;


@Table(name = "User")
public class User extends Model implements Serializable {
    static String PROFILE_IMAGE_KEY = "profile_image_url";

    @Column
    public boolean isAuthenticatedUser;

    @Column(name="name")
    private String name;

    @Column(name="remote_id")
    public long uid;

    @Column(name="screen_name")
    private String screenName;

    @Column(name="profile_image_url")
    private String profileImageURL;

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
            user.screenName = object.getString("screen_name");
            if (object.has(PROFILE_IMAGE_KEY)) {
                user.profileImageURL = object.getString(PROFILE_IMAGE_KEY);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return user;
    }

    public static User getCurrentUserFromLocalDBSynchronous() {
        User user = new Select().from(User.class).where("isAuthenticatedUser = ?", true).executeSingle();
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
                    user.save();
                    if (userLoadedCallback != null) {
                        userLoadedCallback.onUserLoaded(user);
                    }
                }

                @Override
                public void onFailure(Throwable throwable, String s) {
                    Log.e("DBG", throwable.toString());
                    if (userLoadedCallback != null) {
                        userLoadedCallback.onUserLoaded(null);
                    }
                }
            });
    }

}
