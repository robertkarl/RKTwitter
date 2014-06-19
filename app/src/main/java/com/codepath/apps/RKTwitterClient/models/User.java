package com.codepath.apps.RKTwitterClient.models;

import org.json.JSONException;
import org.json.JSONObject;


public class User {

    public String getName() {
        return name;
    }

    public long getUid() {
        return uid;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getProfileImageURL() {
        return profileImageURL;
    }

    private String name;
    private long uid;
    private String screenName;
    private String profileImageURL;

    public static User fromJSON(JSONObject object) {
        User user = new User();
        try {
            user.name = object.getString("name");
            user.uid = object.getLong("id");
            user.screenName = object.getString("screen_name");
            user.profileImageURL = object.getString("profile_image_url");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return user;
    }
}
