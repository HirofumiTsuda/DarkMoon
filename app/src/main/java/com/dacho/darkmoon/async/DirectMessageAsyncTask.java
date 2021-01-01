package com.dacho.darkmoon.async;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.TextView;

import com.dacho.darkmoon.data.TimelineAdapter;
import com.dacho.darkmoon.data.TimelineData;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by admin on 15/12/11.
 */
public class DirectMessageAsyncTask extends AsyncTask<String,Void,Boolean>{

    long lastID;
    Twitter twitter;
    TimelineAdapter dmAdapter;
    Paging paging;
    SwipeRefreshLayout swipeRefreshLayout;
    Handler handler;
    TextView textView;
    String str;
    boolean isNewTimeline = true;
    static boolean nowLoadingFlag = false;


    public DirectMessageAsyncTask (Paging paging,Twitter twitter,TimelineAdapter dmAdapter,
                                 Handler handler,
                                 TextView textView,String str){
        this.paging = paging;
        this.twitter = twitter;
        this.handler = handler;
        this.dmAdapter = dmAdapter;
        this.textView = textView;
        this.str = str;
        this.isNewTimeline = false;
    }

    public DirectMessageAsyncTask (long lastID,Twitter twitter,TimelineAdapter dmAdapter){
        this.lastID = lastID;
        this.twitter = twitter;
        this.dmAdapter = dmAdapter;
        paging = new Paging();
        paging.maxId(lastID);
    }

    public DirectMessageAsyncTask(Paging paging,Twitter twitter,
                                 TimelineAdapter dmAdapter,
                                 boolean isNewTimeline,SwipeRefreshLayout swipeRefreshLayout,
                                 Handler handler){
        this.lastID = lastID;
        this.twitter = twitter;
        this.dmAdapter = dmAdapter;
        this.paging = paging;
        this.isNewTimeline = isNewTimeline;
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.handler = handler;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        ResponseList<twitter4j.DirectMessage> tl,tl_r;
        if(nowLoadingFlag){
            cancel(true);
        }
        nowLoadingFlag = true;
        boolean changeFlag = false;
        try{

            Log.d("new Direct Message","user timeline");
            //SentとRecieve両方もってくる
            tl_r = twitter.getDirectMessages(paging);
            tl = twitter.getSentDirectMessages(paging);
            for(DirectMessage item : tl_r){
                tl.add(item);
            }
            //時間でソート
            Collections.sort(tl, new DmComparator());
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
                            dmAdapter.insert(newItem, 0);
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
                for (twitter4j.DirectMessage status : tl) {
                    changeFlag = true;
                    Log.d("new tweet", "add old timeline:");
                    final TimelineData newItem = new TimelineData(status);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            dmAdapter.add(newItem);
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
                        dmAdapter.notifyDataSetChanged();
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

    public class DmComparator implements Comparator<DirectMessage> {

        //比較メソッド（データクラスを比較して-1, 0, 1を返すように記述する）
        public int compare(DirectMessage a, DirectMessage b) {
            Date no1 = a.getCreatedAt();
            Date no2 = b.getCreatedAt();

            //こうすると社員番号の昇順でソートされる
            if (no1.before(no2)) {
                return 1;

            } else if (no1.equals(no2)) {
                return 0;

            } else {
                return -1;

            }
        }

    }

}
