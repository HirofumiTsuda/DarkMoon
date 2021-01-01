package com.dacho.darkmoon.async;

import android.os.AsyncTask;

import twitter4j.TwitterStream;

/**
 * Created by admin on 2015/07/18.
 */
public class TwitterStreamShutDownAsyncTask extends AsyncTask<Void,Void,Void>{

    TwitterStream twitterStream;

    public TwitterStreamShutDownAsyncTask(TwitterStream twitterStream){
        this.twitterStream = twitterStream;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        twitterStream.shutdown();
        return null;
    }
}
