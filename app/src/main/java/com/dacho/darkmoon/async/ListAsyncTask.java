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

import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by admin on 15/12/12.
 */
public class ListAsyncTask extends AsyncTask<String,Void,Boolean> {

    long lastID;
    Twitter twitter;
    TimelineAdapter listAdapter;
    Paging paging;
    SwipeRefreshLayout swipeRefreshLayout;
    Handler handler;
    TextView textView;
    String str;
    boolean isNewTimeline = true;
    static boolean nowLoadingFlag = false;
    long list_id;


    public ListAsyncTask (Paging paging,Twitter twitter,TimelineAdapter listAdapter,
                                   Handler handler,
                                   TextView textView,String str,long list_id){
        this.paging = paging;
        this.twitter = twitter;
        this.handler = handler;
        this.listAdapter = listAdapter;
        this.textView = textView;
        this.str = str;
        this.isNewTimeline = false;
        this.list_id = list_id;
    }

    public ListAsyncTask (long lastID,Twitter twitter,TimelineAdapter listAdapter,long list_id){
        this.lastID = lastID;
        this.twitter = twitter;
        this.listAdapter = listAdapter;
        this.list_id = list_id;
        paging = new Paging();
        paging.maxId(lastID);
    }

    public ListAsyncTask(Paging paging,Twitter twitter,
                                  TimelineAdapter listAdapter,
                                  boolean isNewTimeline,SwipeRefreshLayout swipeRefreshLayout,
                                  Handler handler,long list_id){
        this.lastID = lastID;
        this.twitter = twitter;
        this.listAdapter = listAdapter;
        this.paging = paging;
        this.isNewTimeline = isNewTimeline;
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.handler = handler;
        this.list_id = list_id;
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

            Log.d("list","user timeline");
            //SentとRecieve両方もってくる
            tl = twitter.getUserListStatuses(list_id,paging);
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
                            listAdapter.insert(newItem, 0);
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
                            listAdapter.add(newItem);
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
                        listAdapter.notifyDataSetChanged();
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
