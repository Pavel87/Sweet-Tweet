package com.pacmac.sweettweet;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.models.Tweet;

import java.util.List;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private static final String TWEET_ID = "tweetID";

    private List<Tweet> tweetList = null;
    private Picasso picasso;
    private Context context;


    public RecyclerViewAdapter(Context context, List<Tweet> tweetList) {
        this.tweetList = tweetList;
        this.context = context;
        picasso = Picasso.with(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rc, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.tweetText.setText(tweetList.get(position).text);
        picasso.load(tweetList.get(position)
                .user.profileImageUrl)
                .placeholder(R.drawable.tw__ic_logo_blue)
                .error(R.drawable.tw__ic_tweet_photo_error_dark)
                .resize(50, 50)
                .into(holder.tweetImage);
    }

    @Override
    public int getItemCount() {
        return tweetList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView tweetText;
        public final ImageView tweetImage;


        public ViewHolder(View view) {
            super(view);
            tweetText = (TextView) view.findViewById(R.id.tweetText);
            tweetImage = (ImageView) view.findViewById(R.id.tweetImage);
            view.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            showDetailScreen(tweetList.get(getLayoutPosition()));
        }
    }

    private void showDetailScreen(Tweet tweet) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(TWEET_ID,tweet.id);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void updateTweetList(List<Tweet> tweets) {
        this.tweetList = tweets;
    }

}
