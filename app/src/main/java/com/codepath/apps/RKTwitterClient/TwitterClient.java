package com.codepath.apps.RKTwitterClient;

import android.content.Context;
import android.util.Log;

import com.codepath.apps.RKTwitterClient.models.Tweet;
import com.codepath.apps.RKTwitterClient.models.User;
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
    public static final String REST_CONSUMER_KEY = "Wf68gqZ9VHWlcteVf9d4Gr7Dt";
    public static final String REST_CONSUMER_SECRET = "RKpr8Bp1TJ9TYFaTVUrhzJ3vNlZddcL1GGpohW12TOI5XJBjYL";
    public static final String REST_CALLBACK_URL = "oauth://cpbasictweets"; // Change this (here and in manifest)

    public static final String MENTIONS_ENDPOINT = "statuses/mentions_timeline.json";
    public static final String HOME_TIMELINE_ENDPOINT = "statuses/home_timeline.json";
    public static final String PROFILE_ENDPOINT = "statuses/user_timeline.json";

    public TwitterClient(Context context) {
        super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
    }

    public void getMentionsTimeline(AsyncHttpResponseHandler handler) {
        fetchOlderTweets(MENTIONS_ENDPOINT, handler, 0);
    }

    public void getHomeTimeline(AsyncHttpResponseHandler handler) {
        fetchOlderTweets(HOME_TIMELINE_ENDPOINT, handler, 0);
    }

    /**
     * @param tweetID tweets will not be returned as new or newer than this one
     */
    public void fetchOlderTweets(String endpoint, AsyncHttpResponseHandler handler, long tweetID) {
        String apiUrl = getApiUrl(endpoint);
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
        Log.i("DBG", String.format("Requesting %s ", apiUrl));
        RequestParams params = new RequestParams();
        params.put("status", tweetContents);
        performAndLogPost(apiUrl, params, handler);
    }

    public void getUser(AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("account/verify_credentials.json");
        Log.i("DBG", String.format("Requesting %s ", apiUrl));
        client.get(apiUrl, handler);
    }

    public void getFollowers(User user, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("followers/ids.json");
        RequestParams params = new RequestParams();
        params.put("user_id", String.format("%d", user.uid));
        client.get(apiUrl, params, handler);
    }

    public void performRetweet(Tweet tweet, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl(String.format("statuses/retweet/%d.json", tweet.ID));
        RequestParams params = new RequestParams();
        params.put("id", String.format("%d", tweet.ID));
        performAndLogPost(apiUrl, params, handler);
    }

    public void performFavorite(Tweet tweet, AsyncHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.put("id", String.format("%d", tweet.ID));
        String apiUrl = getApiUrl("favorites/create.json");
        performAndLogPost(apiUrl, params, handler);
    }

    public void getProfileTimeline(long userID, AsyncHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.put("id", String.format("%d", userID));
        String apiUrl = getApiUrl(PROFILE_ENDPOINT);
        Log.i("DBG", String.format("Requesting %s", apiUrl));
        client.get(apiUrl, params, handler);
    }

    private void performAndLogPost(String apiUrl, RequestParams params, AsyncHttpResponseHandler handler) {
        Log.i("DBG", String.format("Requesting %s ", apiUrl));
        client.post(apiUrl, params, handler);
    }
}