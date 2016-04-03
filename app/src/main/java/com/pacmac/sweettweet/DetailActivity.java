package com.pacmac.sweettweet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;

import io.fabric.sdk.android.Fabric;

public class DetailActivity extends AppCompatActivity {

    private static final String TWITTER_KEY = "usF6mtjg0uKtFO5MorMC1ZeWP";
    private static final String TWITTER_SECRET = "tT3gXb52jZigzUKyVTFMtfbk0k6zGXwZZLSasd8mrWGm7eB6LH";
    private static final String TWEET_ID = "tweetID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        final LinearLayout linearLayout
                = (LinearLayout) findViewById(R.id.detail);

        long tweetId = getIntent().getLongExtra(TWEET_ID, 0l);


        final TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));



        TweetUtils.loadTweet(tweetId, new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                linearLayout.addView(new TweetView(DetailActivity.this, result.data));
            }

            @Override
            public void failure(TwitterException e) {
                e.printStackTrace();
            }
        });
    }
}