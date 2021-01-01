package com.dacho.darkmoon.async;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.dacho.darkmoon.activitys.MainActivity;
import com.dacho.darkmoon.animation.Flush;
import com.dacho.darkmoon.R;
import com.dacho.darkmoon.data.TimelineAdapter;
import com.dacho.darkmoon.data.TimelineData;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;

/**
 * Created by hirofumi on 15/02/27.
 */
public class LoadAsyncTask extends AsyncTask<String,Void,Void>{
    //ロードの処理をかく

    private FragmentTransaction fragmentTransaction;
    private FragmentManager fragmentManager;
    private ImageView loadMoon;
    private Activity activity;
    private Twitter twitter;
    private Handler handler;
    private TextView loadtext;
    private TimelineAdapter tlAdapter,replyAdapter;
    private TwitterStream twitterStream;
    private Flush flush;
    private boolean twitterStreamFlag = true;

    public String userName;

    public LoadAsyncTask(Twitter twitter,FragmentTransaction fragmentTransaction,
                         FragmentManager fragmentManager,Activity activity,Handler handler,
                         TimelineAdapter tlAdapter,TimelineAdapter replyAdapter,TwitterStream twitterStream,
                         boolean twitterStreamFlag){
        this.twitter = twitter;
        this.fragmentTransaction = fragmentTransaction;
        this.fragmentManager = fragmentManager;
        this.loadMoon = (ImageView)activity.findViewById(R.id.load_moon);
        this.activity = activity;
        this.handler = handler;
        this.loadtext = (TextView)activity.findViewById(R.id.loadtext);
        this.tlAdapter = tlAdapter;
        this.replyAdapter = replyAdapter;
        this.twitterStream = twitterStream;
        this.flush = new Flush(this.handler,this.loadtext);
        this.twitterStreamFlag = twitterStreamFlag;
    }

    @Override
        protected void onPreExecute() {
        //アニメーションを動かす
        Animation animation = AnimationUtils.loadAnimation(activity, R.anim.load_anim);
        loadMoon.startAnimation(animation);
        //Now Loadingも動かす
        flush.startFlush();
    }

    @Override
    protected Void doInBackground(String... params) {
        //ロード処理をする タイムラインを入手
        try{
            Log.d("call","get ScreenName");
            userName = twitter.getScreenName();
            ResponseList<twitter4j.Status> homeTL = twitter.getHomeTimeline();
            ResponseList<twitter4j.Status> replyTL = twitter.getMentionsTimeline();
            //adapterにいれる
            for(twitter4j.Status status : homeTL){
                tlAdapter.add(new TimelineData(status));
            }

            for(twitter4j.Status status : replyTL){
                replyAdapter.add(new TimelineData(status));
            }

        }catch (TwitterException te){
            onCancelled();
        }

        return null;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        MainActivity.userName = this.userName;
        //すべてがおわったので画面遷移
        flush.stopFlush();
        //フラグがオンならユーザーストリーム開始
        if(twitterStreamFlag)
            twitterStream.user();
        //まずadapterにnotification
        tlAdapter.notifyDataSetChanged();
        replyAdapter.notifyDataSetChanged();
        //アニメーション
        Animation in,out;
        in = (Animation)AnimationUtils.loadAnimation(activity,R.anim.fromabobe);
        out = (Animation)AnimationUtils.loadAnimation(activity,R.anim.tobottom);

        View mainFragment = ((Fragment)fragmentManager.findFragmentById(R.id.mainFragment)).getView();
        ViewPager pager = ((ViewPager)activity.findViewById(R.id.pager));
        Log.d("call","mainfragment");
        pager.startAnimation(in);
        mainFragment.startAnimation(out);
        mainFragment.setVisibility(View.GONE);
        pager.setVisibility(View.VISIBLE);
        /*
        ListFragment listFragment = new ListFragment();
        //置き換え
        fragmentTransaction.replace(R.id.mainFragment,listFragment);
        //アニメーション
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        //実行
        fragmentTransaction.commit();
        //*/

    }
}
