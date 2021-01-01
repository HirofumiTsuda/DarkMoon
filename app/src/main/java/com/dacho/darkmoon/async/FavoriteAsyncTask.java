package com.dacho.darkmoon.async;

import android.os.AsyncTask;

import com.dacho.darkmoon.activitys.MainActivity;
import com.dacho.darkmoon.data.TimelineData;

import java.sql.Time;

import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by admin on 2015/06/09.
 */
public class FavoriteAsyncTask extends AsyncTask<String,Void,Boolean> {

    private Twitter twitter;
    private TimelineData timelineData;
    private boolean isStreamOn;

    public FavoriteAsyncTask(Twitter twitter,TimelineData timelineData,boolean isStreamOn){
        this.twitter = twitter;
        this.timelineData = timelineData;
        this.isStreamOn = isStreamOn;
    }

    @Override
    protected Boolean doInBackground(String... strings) {

        try{
            if(timelineData.getIsFavorite()){
                //もうお気に入りにしている
                twitter.destroyFavorite(timelineData.getTweetID());
                //ストリームがオフのとき
                if(isStreamOn){
                    //itemのフラグを変更
                    timelineData.setIsFavorite(false);
                    return false;
                }
            }else{
                //まだお気に入りにしていない
                twitter.createFavorite(timelineData.getTweetID());
                //ストリームがオフのとき
                if(isStreamOn){
                    //itemのフラグを変更
                    timelineData.setIsFavorite(true);
                    return true;
                }
            }
        }catch (TwitterException te){
            te.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        //MainActivity.updateAdapter(timelineData.getTweetID(), aBoolean, timelineData.getIsReplyToUser());
    }
}
