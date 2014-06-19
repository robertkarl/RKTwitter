package com.codepath.apps.RKTwitterClient.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by androiddev on 6/18/14.
 */
public class Tweet {
    private String body;
    private long uid;
    private String createdAt;
    private User user;

    public String getBody() {
        return body;
    }

    public long getUid() {
        return uid;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    public static Tweet fromJSON(JSONObject object) {
        Tweet tweet = new Tweet();
        try {
            tweet.body = object.getString("text");
            tweet.uid = object.getLong("id");
            tweet.createdAt = object.getString("created_at");
            tweet.user = User.fromJSON(object.getJSONObject("user"));
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return tweet;
    }
}
