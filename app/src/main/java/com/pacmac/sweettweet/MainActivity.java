package com.pacmac.sweettweet;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.AppSession;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.SearchService;

import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {


    private static final String TWITTER_KEY = "usF6mtjg0uKtFO5MorMC1ZeWP";
    private static final String TWITTER_SECRET = "tT3gXb52jZigzUKyVTFMtfbk0k6zGXwZZLSasd8mrWGm7eB6LH";
    private static final String TWEET_SWEET_STORE = "pacmac_tweet_app";
    private static final String TWEET_SEARCH = "pacmac_tweet_search";
    private static final String TWEET_DEFAULT = "tribalscale";

    private TwitterApiClient twitterApiClient = null;
    private RecyclerViewAdapter adapter = null;
    private List<Tweet> tweetList = new ArrayList<>();
    private String searchParam = null;
    private SwipeRefreshLayout refreshLayout = null;
    private boolean isInitialized = false;

    private ProgressDialog pDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);

        searchParam = getSearchParam();
        pDialog = new ProgressDialog(this);
        pDialog = ProgressDialog.show(this,null,null);
        pDialog.setContentView(R.layout.progress_dialog);
        pDialog.setCancelable(false);
        pDialog.show();

        final TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        getTwitterApiClient();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new RecyclerViewAdapter(getApplicationContext(), tweetList);
        recyclerView.setAdapter(adapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSearchDialog();
            }
        });

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        refreshLayout.setColorSchemeColors(Color.DKGRAY, Color.GREEN, Color.BLUE, Color.CYAN);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                if (isInitialized)
                    searchOnTwitter();
                else {
                    ConnectivityManager conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = conn.getActiveNetworkInfo();
                    boolean isConnected = networkInfo != null ? networkInfo.isConnected() : false;
                    if (isConnected) {
                        getTwitterApiClient();
                    } else {
                        refreshLayout.setRefreshing(false);
                        showError();
                    }
                }
            }
        });

    }

    private void getTwitterApiClient() {
        TwitterCore.getInstance().logInGuest(new Callback<AppSession>() {
            @Override
            public void success(Result<AppSession> appSessionResult) {
                AppSession session = appSessionResult.data;
                twitterApiClient = TwitterCore.getInstance().getApiClient(session);
                isInitialized = true;
                searchOnTwitter();
            }

            @Override
            public void failure(TwitterException exception) {
                showError();
                exception.printStackTrace();
            }
        });

    }

    private void searchOnTwitter() {
        SearchService searchService = twitterApiClient.getSearchService();
        searchService.tweets(searchParam, null, null, "us", null, 50, null, null, null, null, new Callback<Search>() {
            @Override
            public void success(Result<Search> result) {
                adapter.updateTweetList(result.data.tweets);
                adapter.notifyDataSetChanged();
                refreshLayout.setRefreshing(false);
                pDialog.hide();
            }

            @Override
            public void failure(TwitterException e) {
                e.printStackTrace();
                showError();
            }
        });
    }


    private void showError() {
        pDialog.hide();
        refreshLayout.setRefreshing(false);
        Toast.makeText(getApplicationContext(), "Check your connectivity", Toast.LENGTH_SHORT).show();
    }

    //dialog for searching tweets
    private void showSearchDialog() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.new_search_dialog);
        dialog.setCancelable(false);

        Button yesButton = (Button) dialog.findViewById(R.id.searchBtn);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText searchET = (EditText) dialog.findViewById(R.id.searchText);
                String result = searchET.getText().toString();
                if (result.length() > 2) {
                    searchParam = result;
                    saveSearchParam();
                    dialog.dismiss();
                    if (isInitialized) {
                        refreshLayout.setRefreshing(true);
                        searchOnTwitter();

                    } else {
                        refreshLayout.setRefreshing(true);
                        getTwitterApiClient();
                    }

                } else {
                    Snackbar.make(view, "Enter word with at least 3 characters", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
        Button noButton = (Button) dialog.findViewById(R.id.cancelBtn);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        ConnectivityManager conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();
        boolean isConnected = networkInfo != null ? networkInfo.isConnected() : false;
        if (isConnected) {
            dialog.show();
        } else {
            showError();
        }
    }


    // shared preferences setting
    private void saveSearchParam() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences(TWEET_SWEET_STORE, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TWEET_SEARCH, searchParam);
        editor.commit();
    }

    private String getSearchParam() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences(TWEET_SWEET_STORE, MODE_PRIVATE);
        return preferences.getString(TWEET_SEARCH, TWEET_DEFAULT);
    }

}
