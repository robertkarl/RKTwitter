package com.codepath.apps.RKTwitterClient;

import android.content.Context;
import android.util.Log;

import com.codepath.apps.RKTwitterClient.models.Tweet;
import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class TwitterClient extends OAuthBaseClient {
    public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class; // Change this
    public static final String REST_URL = "https://api.twitter.com/1.1"; // Change this, base API URL
    public static final String REST_CONSUMER_KEY = "FT5gJGNvj5JPSrX8W5HrF14wC";
    public static final String REST_CONSUMER_SECRET = "Cc4gudmSfWD8FynCwdt11Kf1ojjF22CMsuOU0CJNS2x9aquz4p";
    public static final String REST_CALLBACK_URL = "oauth://cpbasictweets"; // Change this (here and in manifest)
    
    public TwitterClient(Context context) {
        super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
    }

    public void getProfile(AsyncHttpResponseHandler handler) {
        fetchOlderTweets(handler, 0);
    }

    public void getHomeTimeline(AsyncHttpResponseHandler handler) {
        fetchOlderTweets(handler, 0);
    }

    /**
     * @param tweetID tweets will not be returned as new or newer than this one
     */
    public void fetchOlderTweets(AsyncHttpResponseHandler handler, long tweetID) {
        String apiUrl = getApiUrl("statuses/home_timeline.json");
        RequestParams params = new RequestParams();
        params.put("since_id", "1");
        Log.i("DBG", String.format("Requesting %s ", apiUrl));
        if (tweetID != 0) {
            params.put("max_id", Long.toString(tweetID));
        }
        client.get(apiUrl, params, handler);
    }


    /**
     * Tweeting endpoint.
     */
    public void updateStatus(AsyncHttpResponseHandler handler, String tweetContents) {
        String apiUrl = getApiUrl("statuses/update.json");
        RequestParams params = new RequestParams();
        params.put("status", tweetContents);
        client.post(apiUrl, params, handler);
    }

    public void getUser(AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("account/verify_credentials.json");
        client.get(apiUrl, handler);
    }

    public void performFavoriteTweet(Tweet tweet, AsyncHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.put("id", String.format("%d", tweet.getID()));
        String apiUrl = getApiUrl("favorites/create.json");
        client.get(apiUrl, handler);
    }

    /* 1. Define the endpoint URL with getApiUrl and pass a relative path to the endpoint
     * 	  i.e getApiUrl("statuses/home_timeline.json");
     * 2. Define the parameters to pass to the request (query or body)
     *    i.e RequestParams params = new RequestParams("foo", "bar");
     * 3. Define the request method and make a call to the client
     *    i.e client.get(apiUrl, params, handler);
     *    i.e client.post(apiUrl, params, handler);
     */
}