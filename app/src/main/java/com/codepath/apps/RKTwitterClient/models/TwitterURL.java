package com.codepath.apps.RKTwitterClient.models;

import android.util.Pair;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Table(name = "twitter_url")
public class TwitterURL extends Model {

    @Column(name = "url")
    public String url;

    @Column
    public Pair<Integer, Integer> inlineIndices;

    @Column(name = "display_url")
    public String displayURL;

    public static TwitterURL fromJSON(JSONObject object) throws JSONException {
        TwitterURL url = new TwitterURL();
        url.url = object.getString("url");
        JSONArray indices = object.getJSONArray("indices");
        url.inlineIndices = new Pair<Integer, Integer>(indices.getInt(0), indices.getInt(1));
        url.displayURL = object.getString("display_url");
        return url;
    }
}
