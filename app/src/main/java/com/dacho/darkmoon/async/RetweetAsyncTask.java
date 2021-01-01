package com.dacho.darkmoon.async;

import android.os.AsyncTask;

import com.dacho.darkmoon.data.TimelineData;

import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by admin on 2015/06/09.
 */
public class RetweetAsyncTask extends AsyncTask<String,Void,String> {

    private Twitter twitter;
    private TimelineData timelineData;


    public RetweetAsyncTask(Twitter twitter, TimelineData timelineData){
        this.twitter = twitter;
        this.timelineData = timelineData;
    }

    @Override
    protected String doInBackground(String... strings) {
        try{
            if(!timelineData.getIsRetweeted()) {
                //まだされていない
                twitter.retweetStatus(timelineData.getTweetID());
            }else{
                //もうしたなら解除
                twitter.destroyStatus(timelineData.getRetweetID());
            }
        }catch (TwitterException te){
            te.printStackTrace();
        }
        return null;
    }
}
