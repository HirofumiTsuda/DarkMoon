package com.dacho.darkmoon.async;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.util.Vector;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by admin on 2015/05/28.
 */
public class TweetAsyncTask extends AsyncTask<String,Void,String>{

    Twitter twitter;
    String str;
    Vector<String> filePathVector;
    long replyID;
    boolean twitterStreamFlag = true;


    public TweetAsyncTask(Twitter twitter,String str,Vector<String> filePathVector,long replyID){
        this.twitter = twitter;
        this.str = str;
        this.filePathVector = filePathVector;
        this.replyID = replyID;
    }

    @Override
    protected String doInBackground(String... strings) {
        StatusUpdate statusUpdate = new StatusUpdate(str);
        try {
            if(!filePathVector.isEmpty()){
                long[] array = new long[filePathVector.size()];
                for(int i=0;i<filePathVector.size();i++){
                    array[i] = twitter.uploadMedia(new File(filePathVector.get(i))).getMediaId();
                }
                filePathVector.clear();
                statusUpdate.setMediaIds(array);
            }
            Log.d("id",""+replyID);
            if(replyID != -1)
                statusUpdate.inReplyToStatusId(replyID);
            twitter.updateStatus(statusUpdate);
        }catch (TwitterException te){
            te.printStackTrace();
        }
        return null;
    }
}
