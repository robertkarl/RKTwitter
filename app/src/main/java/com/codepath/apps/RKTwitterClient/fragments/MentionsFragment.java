package com.codepath.apps.RKTwitterClient.fragments;

import android.util.Log;

import com.activeandroid.query.Select;
import com.codepath.apps.RKTwitterClient.TwitterClient;
import com.codepath.apps.RKTwitterClient.models.Tweet;
import com.codepath.apps.RKTwitterClient.models.User;

import java.util.List;

public class MentionsFragment extends TweetsListFragment {
    public String getTitle() {
        return "Mentions";
    }

    public void clearAndPopulate() {
        client.getMentionsTimeline(makeUnpackingRefreshingJsonHandler());
    }

    protected String getEndpoint() {
        return TwitterClient.MENTIONS_ENDPOINT;
    }

    @Override
    protected List<Tweet> getStoredTweets() {
        String username  = User.currentUsername;
        if (username == null) {
            return null;
        }
        String whereClause = String.format("body LIKE '%%%s%%'", username);
        List <Tweet> tweets = new Select()
                .from(Tweet.class)
                .where(whereClause)
                .orderBy("remote_id DESC")
                .execute();
        Log.d("DBG", String.format("%s loading %d stored tweets", getTitle(), tweets.size()));
        return tweets;
    }
}
