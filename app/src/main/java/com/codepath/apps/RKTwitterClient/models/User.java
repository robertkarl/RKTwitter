package com.codepath.apps.RKTwitterClient.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;


@Table(name = "User")
public class User extends Model implements Serializable {

    @Column(name="name")
    private String name;
    @Column(name="remote_id")
    private long uid;
    @Column(name="screen_name")
    private String screenName;
    @Column(name="profile_image_url")
    private String profileImageURL;

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
