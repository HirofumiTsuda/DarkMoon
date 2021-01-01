package com.dacho.darkmoon.async;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.TextView;

import com.dacho.darkmoon.data.TimelineAdapter;
import com.dacho.darkmoon.data.TimelineData;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by admin on 2015/06/12.
 */
public class MoreTimelineAsyncTask extends AsyncTask<String,Void,Boolean> {

    long lastID;
    Twitter twitter;
    TimelineAdapter timelineAdapter;
    Paging paging;
    boolean isMentions = false;
    boolean isNewTimeline = true;
    SwipeRefreshLayout swipeRefreshLayout;
    Handler handler;
    TextView textView;
    String str;
    static boolean nowLoadingFlag = false;


    public MoreTimelineAsyncTask(Paging paging,Twitter twitter,TimelineAdapter timelineAdapter,
                                 Handler handler,boolean isMentions,
                                 TextView textView,String str){
        this.paging = paging;
        this.twitter = twitter;
        this.handler = handler;
        this.isMentions = isMentions;
        this.timelineAdapter = timelineAdapter;
        this.textView = textView;
        this.str = str;
        this.isNewTimeline = false;
    }

    public MoreTimelineAsyncTask(long lastID,Twitter twitter,TimelineAdapter timelineAdapter,boolean isMentions){
        this.lastID = lastID;
        this.twitter = twitter;
        this.timelineAdapter = timelineAdapter;
        this.isMentions = isMentions;
        paging = new Paging();
        paging.maxId(lastID);
    }

    public MoreTimelineAsyncTask(Paging paging,Twitter twitter,
                                 TimelineAdapter timelineAdapter,boolean isMentions,
                                 boolean isNewTimeline,SwipeRefreshLayout swipeRefreshLayout,
                                 Handler handler){
        this.lastID = lastID;
        this.twitter = twitter;
        this.timelineAdapter = timelineAdapter;
        this.isMentions = isMentions;
        this.paging = paging;
        this.isNewTimeline = isNewTimeline;
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.handler = handler;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        ResponseList<twitter4j.Status> tl;
        if(nowLoadingFlag){
            cancel(true);
        }
        nowLoadingFlag = true;
        boolean changeFlag = false;
        try{
            if(isMentions){
                Log.d("new tweet","mention");
                tl = twitter.getMentionsTimeline(paging);
                show(tl);
            }else {
                Log.d("new tweet","user timeline");
                tl = twitter.getHomeTimeline(paging);
                show(tl);
            }
            if(isNewTimeline) {
                //新しいものがほしいとき
                //まず自分の番号が配列のどこにあるかを知る
                int num;
                for(num=0;num<tl.size();num++){
                    if(tl.get(num).getId() == paging.getSinceId())
                        break;
                }
                Log.d("tl.size",tl.size()+"");
                Log.d("num",num+"");
                //num がTLsizeと同じならそこに存在　tlsize ならばさらに読み込む必要がある
                //0に自分自身があるなら 更新の必要がない 同じときもいまは更新する
                Log.d("since id",paging.getSinceId()+"");
                //tl.size　より小さいならとりあえずつっこんでおく　num+1 から順にいれておけばよい
                for (int i=0;i<tl.size();i++) {
                    changeFlag = true;
                    Log.d("new tweet", "insert new timeline:" + i);
                    final TimelineData newItem = new TimelineData(tl.get(i));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            timelineAdapter.insert(newItem, 0);
                        }
                    });
                }
            }else {
                //古いやつがほしいとき
                //そもそもなかったら終わり
                if(tl.size()==0)
                    return false;
                //最初の要素は消す
                tl.remove(0);
                for (twitter4j.Status status : tl) {
                    changeFlag = true;
                    Log.d("new tweet", "add old timeline:");
                    final TimelineData newItem = new TimelineData(status);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            timelineAdapter.add(newItem);
                        }
                    });
                }
            }
        }catch (TwitterException te){
            te.printStackTrace();
        }
        return changeFlag;
    }

    @Override
    protected void onCancelled() {
        //textView.setText("もう一度タップして取得");
    }

    @Override
    protected void onPostExecute(final Boolean aBoolean) {

        if(handler != null){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(textView != null)
                        textView.setText(str);
                    if(aBoolean)
                        timelineAdapter.notifyDataSetChanged();
                    if(swipeRefreshLayout != null)
                        swipeRefreshLayout.setRefreshing(false);
                }
            });
        }
        nowLoadingFlag = false;
    }

    void show(ResponseList<twitter4j.Status> list){
        for(twitter4j.Status s : list){
            Log.d("show",s+"");
        }
    }

}
