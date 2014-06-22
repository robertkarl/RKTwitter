package com.codepath.apps.RKTwitterClient.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * TODO: Add support for linking to hashtags and users
 */
@Table(name = "twitter_url")
public class TwitterURL extends Model implements Serializable {

    @Column(name = "url")
    public String url;

    @Column
    int startIndex;

    @Column
    int endIndex;

    @Column(name = "display_url")
    public String displayURL;

    public static TwitterURL fromJSON(JSONObject object) throws JSONException {
        TwitterURL url = new TwitterURL();
        url.url = object.getString("url");
        JSONArray indices = object.getJSONArray("indices");
        url.startIndex = indices.getInt(0);
        url.endIndex = indices.getInt(1);
        url.displayURL = object.getString("display_url");
        return url;
    }
}
