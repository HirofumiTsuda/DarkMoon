package com.dacho.darkmoon.activitys;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dacho.darkmoon.async.BitmapLoading;
import com.dacho.darkmoon.async.DirectMessageAsyncTask;
import com.dacho.darkmoon.async.FavoriteAsyncTask;
import com.dacho.darkmoon.async.ListAsyncTask;
import com.dacho.darkmoon.async.MoreTimelineAsyncTask;
import com.dacho.darkmoon.async.RetweetAsyncTask;
import com.dacho.darkmoon.async.TweetAsyncTask;
import com.dacho.darkmoon.async.TwitterStreamShutDownAsyncTask;
import com.dacho.darkmoon.cache.BitmapCache;
import com.dacho.darkmoon.data.ListPagerAdapter;
import com.dacho.darkmoon.data.TimelineAdapter;
import com.dacho.darkmoon.data.TimelineData;
import com.dacho.darkmoon.dialogs.DialogData;
import com.dacho.darkmoon.dialogs.DialogListAdapter;
import com.dacho.darkmoon.dialogs.ListDialog;
import com.dacho.darkmoon.layoutFragment.Preference;
import com.dacho.darkmoon.listeners.EditTextListener;
import com.dacho.darkmoon.async.LoadAsyncTask;
import com.dacho.darkmoon.async.OauthAccessToken;
import com.dacho.darkmoon.dialogs.OauthDialogFragment;
import com.dacho.darkmoon.R;
import com.dacho.darkmoon.values.ReplyIDManager;
import com.dacho.darkmoon.values.TwitterKey;

import java.sql.Time;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.MediaEntity;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserStreamAdapter;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;


public class MainActivity extends ActionBarActivity implements TwitterKey,SwipeRefreshLayout.OnRefreshListener {

    private Uri intentUri;
    private AccessToken accessToken;
    private static Twitter twitter;
    private ViewPager viewPager;
    //ListViewを格納するベクトル
    Vector<ListView> listVector;
    private ListView timeLineList;
    private ListView replyList;
    private ListView dmList;
    private ListView twList;
    private View tweet;
    //TweetのeditText
    private EditText tweetText;

    private Activity mainActivity;

    private TwitterStream twitterStream;
    public static boolean twitterStreamFlag = true;

    public static TimelineAdapter timelineAdapter,replyAdapter,dmAdapter,twListAdapter;
    private ListPagerAdapter listPagerAdapter;
    private Handler handler;

    public static String userName="";
    public static long userID = -1;
    private Vector<View> viewVector;
    private Vector<String> filePathVector;
    private Vector<TimelineAdapter> tlAdapterVector;
    private double width,height,touchX,touchY;

    private NotificationManager mManager;
    //Swipe
    private SwipeRefreshLayout timelineSwipeRefreshLayout;
    private SwipeRefreshLayout replySwipeRefreshLayout;
    private SwipeRefreshLayout dmSwipeRefreshLayout;
    private SwipeRefreshLayout twListSwipeRefreshLayout;
    //リプライ先のid
    private long toReplyStatusId = -1;
    //ActionBar
    private android.support.v7.app.ActionBar actionBar;

    private static final int REQUEST_PICK_CONTENT = 666;
    private static final int REQUEST_KITKAT_PICK_CONTENT = 777;

    private Uri mPictureUri;

    private BitmapCache bitmapCache;

    //touch event
    int left,center,right,left_long,center_long,right_long;
    long list_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendNotification();
        mainActivity = this;
        //widtとheighの取得
        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        // ディスプレイのインスタンス生成
        Display disp = wm.getDefaultDisplay();
        Point size = new Point();
        disp.getSize(size);
        width = size.x;
        height = size.y;
        //preferences
        loadPreferences();
        Log.d("swipe","swipe set");
        //Fragmentを切り替えるために準備をする
        final FragmentManager fragmentManager = getFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //値をとりだす準備
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //AccessTokenをつくる
        //user1タグにいまはある
        String tokenTag[] = preferences.getString("user1","no&no").split("&");
        accessToken = new AccessToken(tokenTag[0],tokenTag[1]);
        Log.d("token",accessToken.getToken());
        Log.d("token secret",accessToken.getTokenSecret());
        Log.d("equal?",String.valueOf(accessToken.getToken().equals("no")));

        //Intentがあるかどうか
        Log.d("call","起動");
        if((intentUri = getIntent().getData()) != null){
            getAccessToken(intentUri);
            //tokenを保存
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("user1",accessToken.getToken()+"&"+accessToken.getTokenSecret());
            editor.commit();
        }

        //tokenの有無を調べる　なかったらoauth認証させる
        //よびだし
        if(accessToken.getToken().equals("no") || accessToken.getTokenSecret().equals("no")) {
            Log.d("call","need to OAuth");
            OauthDialogFragment oauthDialogFragment = new OauthDialogFragment();
            oauthDialogFragment.setActivity(this);
            oauthDialogFragment.show(fragmentManager, "Oauth");
            return;
        }

        //Tokenがある状態なので Twitterクラスを作成　なんでもできる
        Log.d("flag","let's get twitter");
        twitter = getTwitter(accessToken);
        handler = new Handler();
        //actionBar.setLogo(R.drawable.darkmoon);
        //userstream
        twitterStream = getTwitterStream(twitter);
        Log.d("call","get twitter");
        //これを受け渡ししていく
        //cache
        bitmapCache = new BitmapCache();
        //リストとその中身の設定
        setListContent();
        //listner
        setTweetListener(tweet);
        setTwitterStreamListener(twitterStream);
        //データをとってくる
        new LoadAsyncTask(twitter,fragmentTransaction,fragmentManager,this,new Handler(),
                timelineAdapter,replyAdapter,twitterStream,twitterStreamFlag).execute();

        new DirectMessageAsyncTask(new Paging(),twitter,dmAdapter,handler,null,"タッチして取得").execute();
        new ListAsyncTask(new Paging(),twitter,twListAdapter,handler,null,"タッチして取得",list_id).execute();
        return;

    }

    @Override
    protected void onDestroy() {
        Log.d("call", "onDestroy");
        //notificationをけしておく
        mManager.cancel(0);
        if(twitterStream != null) {
            twitterStream.shutdown();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("on resume","load preferences");
        loadPreferences();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, Pref.class);
            startActivity(intent);
            return true;
        }

        if(id == R.id.action_stream){
            if(twitterStreamFlag){
                new TwitterStreamShutDownAsyncTask(twitterStream).execute();
                twitterStreamFlag = false;
            }else{
                twitterStream.user();
                twitterStreamFlag = true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem streamMenu = menu.findItem(R.id.action_stream);
        if(twitterStreamFlag) {
            streamMenu.setTitle("Stream Off");
        }else{
            streamMenu.setTitle("Stream On");
        }
        return true;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        //tweeの位置で戻るボタンが押されたらHomeに移動
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK && viewPager.getCurrentItem() == 0) {
            viewPager.setCurrentItem(ReplyIDManager.before_page);
            ReplyIDManager.before_page = 1;
            return true;
        }
        return super.dispatchKeyEvent(event);
    }


    void getAccessToken(Uri intentUri) {
        //受け取ったらtokenの処理をかく
        Log.d("uri", intentUri.toString());
        String oauth_token,verifier;
        if(intentUri != null && intentUri.toString().startsWith(CALLBACK)){
            verifier = intentUri.getQueryParameter("oauth_verifier");
            oauth_token = intentUri.getQueryParameter("oauth_token");
            Log.d("verifier",verifier);
            Log.d("oauth_token",oauth_token);
            OauthAccessToken oauthAccessToken = new OauthAccessToken(OauthDialogFragment.getREQUEST_TOKEN(),OauthDialogFragment.getREQUEST_TOKEN_SECRET(),verifier,oauth_token);
            oauthAccessToken.execute();
            try {
                String accessTokenStr = oauthAccessToken.get();
                //分解する
                accessToken = new AccessToken(accessTokenStr.split("&")[0].split("=")[1],accessTokenStr.split("&")[1].split("=")[1]);

            }catch (Exception e){                Log.d("call","token set");
                e.printStackTrace();
            }
        }

    }

    private Twitter getTwitter(AccessToken mAccessToken){
            Twitter mTwitter = new TwitterFactory().getInstance();
            mTwitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
            mTwitter.setOAuthAccessToken(accessToken);
            return mTwitter;
    }

    private TwitterStream getTwitterStream(Twitter mTwitter){
        try {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            AccessToken mAccessToken = mTwitter.getOAuthAccessToken();
            builder.setOAuthConsumerKey(CONSUMER_KEY);
            builder.setOAuthConsumerSecret(CONSUMER_SECRET);
            builder.setOAuthAccessToken(mAccessToken.getToken());
            builder.setOAuthAccessTokenSecret(mAccessToken.getTokenSecret());
            Configuration conf = builder.build();
            return new TwitterStreamFactory(conf).getInstance();
        }catch (TwitterException te){
            te.printStackTrace();
        }
        return null;
    }

    void setTweetListener(View v){
        tweetText = (EditText)v.findViewById(R.id.edittweet);
        TextView count = (TextView)v.findViewById(R.id.tweetcount);
        //カウントのリスナ
        tweetText.addTextChangedListener(new EditTextListener(count));
        //ボタンのリスナ
        ImageView camera = (ImageView)v.findViewById(R.id.camera);
        ImageView location = (ImageView)v.findViewById(R.id.location);
        ImageView send = (ImageView)v.findViewById(R.id.tweetsend);


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = tweetText.getText().toString();
                final IBinder iBinder = view.getWindowToken();
                //140文字以下なら送信
                if (str.length() <= 140) {
                    new TweetAsyncTask(twitter, str, filePathVector, ReplyIDManager.id).execute();
                    tweetText.setText("");
                    viewPager.setCurrentItem(1);
                }
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT < 19) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQUEST_PICK_CONTENT);
                } else {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQUEST_KITKAT_PICK_CONTENT);
                }

            }
        });
    }

    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
    if (resultCode != RESULT_OK) return;

    if (requestCode == REQUEST_PICK_CONTENT) {
        String[] columns = {MediaStore.MediaColumns.DATA};
        Cursor cursor = getContentResolver().query(data.getData(), columns, null, null, null);

        if (cursor.moveToFirst()) {
            filePathVector.add(cursor.getString(0));
            // fileから写真を読み込む
        }
    } else if (requestCode == REQUEST_KITKAT_PICK_CONTENT) {
        Uri uri = data.getData();
        if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
            //ギャラリーからの場合
            String id = DocumentsContract.getDocumentId(data.getData());
            String selection = "_id=?";
            String[] selectionArgs = new String[]{id.split(":")[1]};

            Cursor cursor = getContentResolver().query(MediaStore.Images.Media.
                    EXTERNAL_CONTENT_URI, new String[]{MediaStore.MediaColumns.DATA}, selection, selectionArgs, null);

            if (cursor.moveToFirst()) {
                filePathVector.add(cursor.getString(0));
                // fileから写真を読み込む
            }

        } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
            // ダウンロードからの場合
            String id = DocumentsContract.getDocumentId(data.getData());
            Uri docUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

            Cursor cursor = getContentResolver().query(docUri, new String[]{MediaStore.MediaColumns.DATA}, null, null, null);

            if (cursor.moveToFirst()) {
                filePathVector.add(cursor.getString(0));
                // fileから写真を読み込む
            }

        }
    }


    }

    void loadPreferences(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        left = sharedPreferences.getInt("left",0);
        center = sharedPreferences.getInt("center",1);
        right = sharedPreferences.getInt("right",2);
        left_long = sharedPreferences.getInt("left_long",3);
        center_long = sharedPreferences.getInt("center_long",4);
        right_long = sharedPreferences.getInt("right_long",5);
        Log.d("list id",sharedPreferences.getString("list_preference","-1"));
        list_id = Long.parseLong(sharedPreferences.getString("list_preference","-1"));;

    }

    void setViewPagerListener(){
       //Listener
       viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
           @Override
           public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

           }

           @Override
           public void onPageSelected(int position) {
               //0以外ならreplyIdを-1にする
               if (position > 0) {
                   ReplyIDManager.before_page = position;
                   //キーボードを消す
                   InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                   imm.hideSoftInputFromWindow(viewPager.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                   ReplyIDManager.id = -1;
                   filePathVector.clear();
                   tweetText.setText("");
               }
           }

           @Override
           public void onPageScrollStateChanged(int state) {

           }
       });

   }

    void setListViewListener(ListView listView){

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("call", "item click");
                int num = (int)(3.0*touchX/width);
                TimelineData timelineData = (TimelineData) ((ListView) adapterView).getItemAtPosition(i);
                switch (num){
                    case 0:
                        touchEvent(left,timelineData);
                        break;
                    case 1:
                        touchEvent(center,timelineData);
                        break;
                    case 2:
                        touchEvent(right,timelineData);
                        break;
                }

            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                int num = (int)(3.0*touchX/width);
                TimelineData timelineData = (TimelineData) ((ListView) adapterView).getItemAtPosition(i);
                switch (num){
                    case 0:
                        touchEvent(left_long,timelineData);
                        break;
                    case 1:
                        touchEvent(center_long,timelineData);
                        break;
                    case 2:
                        touchEvent(right_long,timelineData);
                        break;
                }
                return true;
            }
        });

        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                touchX = motionEvent.getX();
                touchY = motionEvent.getY();
                Log.d("call", "touch X:" + touchX + ",Y:" + touchY);
                return false;
            }
        });
    }

    void touchEvent(int id,TimelineData timelineData){
        switch (id){
            case 0:
                new FavoriteAsyncTask(twitter,timelineData,twitterStreamFlag).execute();
                break;
            case 1:
                setDialogListView(timelineData);
                break;
            case 2:
                ReplyIDManager.id = timelineData.getTweetID();
                ReplyIDManager.before_page = viewPager.getCurrentItem();
                viewPager.setCurrentItem(0);
                tweetText.setText("@"+timelineData.getUserID() + " ");
                tweetText.setSelection(timelineData.getUserID().length() + 2);
                break;
            case 3:
                new RetweetAsyncTask(twitter,timelineData).execute();
                break;
            case 4:
                ReplyIDManager.before_page = viewPager.getCurrentItem();
                viewPager.setCurrentItem(0);
                tweetText.setText(timelineData.getTweetText() + " ");
                tweetText.setSelection(timelineData.getTweetText().length() + 1);
                break;
            case 5:
                ReplyIDManager.before_page = viewPager.getCurrentItem();
                tweetText.setText(" RT @" + timelineData.getUserID() + ": " + timelineData.getTweetText());
                viewPager.setCurrentItem(0);
                tweetText.setSelection(0);
                break;
            default:


        }
    }

    void setTwitterStreamListener(TwitterStream mTwitterStream){
        mTwitterStream.addListener(new UserStreamAdapter() {
            @Override
            public void onStatus(Status status) {
                //新しいtweetを受信
                final TimelineData item = new TimelineData(status);
                Log.d("call", "on status");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //あらかじめListVieの位置を知っておく
                        int position = timeLineList.getFirstVisiblePosition();
                        int y = timeLineList.getChildAt(0).getTop();
                        timelineAdapter.insert(item, 0);
                        timelineAdapter.notifyDataSetChanged();
                        //再描画されたのでリストビューの位置をかえる
                        if (position != 0 || y != 0)
                            timeLineList.setSelectionFromTop(position + 1, y);

                    }
                });
                //リプライかどうか判断する
                Pattern p = Pattern.compile("(?i)" + "@" + userName, Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(status.getText());
                //マッチしてたらリプライ
                if (m.find()) {
                    final TimelineData reply = new TimelineData(status);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            replyAdapter.insert(reply, 0);
                            replyAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }

            @Override
            public void onFavorite(final User source, User target, final Status favoritedStatus) {
                Log.d("on favorite", "target:" + target.getScreenName());
                Log.d("on favorite", "username:" + userName);
                if (!target.getScreenName().equals(userName))
                    return;
                //自分がふぁぼられたら
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("on favorite", "toast");
                        View toastView = getLayoutInflater().inflate(R.layout.fav_toast, null);
                        Toast toast = new Toast(mainActivity);
                        //画像とテキストをゲット
                        ImageView toastIcon = (ImageView) toastView.findViewById(R.id.toast_user_icon);
                        TextView toastId = (TextView) toastView.findViewById(R.id.toast_user_id);
                        TextView toastText = (TextView) toastView.findViewById(R.id.toast_tweet_text);
                        toastId.setText("favorite from @" + source.getScreenName());
                        toastText.setText(favoritedStatus.getText());
                        String userUrl = source.getProfileImageURL();
                        Bitmap bitmap = bitmapCache.getBitmap(userUrl);
                        if (bitmap != null) {
                            toastIcon.setImageBitmap(bitmap);
                        } else {
                            //まず仮の画像を貼っておく
                            toastIcon.setImageResource(R.mipmap.darkmoon);
                            new BitmapLoading(userUrl, toastIcon, bitmapCache).execute();
                        }
                        toast.setView(toastView);
                        toast.show();
                    }
                });

            }

            @Override
            public void onUnfavorite(User source, User target, Status unfavoritedStatus) {

            }
        });

    }

    void setSwipeRefleshLayoutListener(SwipeRefreshLayout swipeRefreshLayout){
        swipeRefreshLayout.setColorSchemeResources(R.color.darkred,
                R.color.semidarkred, R.color.semibrightred,
                R.color.brightred);
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        Log.d("call", "on reflesh");
        Log.d("twitter stream flag", twitterStreamFlag + "");
        if(!twitterStreamFlag){
            Log.d("want to get new tweet","async");
            Paging timelinePaging = new Paging();
            Paging replyPaging = new Paging();
            Paging dmPaging = new Paging();
            //timelineとreplyとDM 両方にやる
            TimelineData timelineData0 = timelineAdapter.getItem(0);
            TimelineData replyData0 = replyAdapter.getItem(0);
            TimelineData dmData0 = dmAdapter.getItem(0);
            if(timelineData0.getRetweetID() == -1) {
                timelinePaging.setSinceId(timelineData0.getTweetID());
            }else{
                timelinePaging.setSinceId(timelineData0.getRetweetID());
            }
            if(replyData0.getRetweetID() == -1) {
                replyPaging.setSinceId(replyData0.getTweetID());
            }else{
                replyPaging.setSinceId(replyData0.getRetweetID());
            }
            if(dmData0.getRetweetID() == -1) {
                dmPaging.setSinceId(dmData0.getTweetID());
            }else{
                dmPaging.setSinceId(dmData0.getRetweetID());
            }
            //AsyncTask にまかせる
            new MoreTimelineAsyncTask(timelinePaging,twitter,timelineAdapter,false,true,timelineSwipeRefreshLayout,handler).execute();
            new MoreTimelineAsyncTask(replyPaging,twitter,replyAdapter,true,true,replySwipeRefreshLayout,handler).execute();
            new DirectMessageAsyncTask(dmPaging,twitter,dmAdapter,true,dmSwipeRefreshLayout,handler);
        }else {
            Log.d("stream is on","no neccesity");
            timelineSwipeRefreshLayout.setRefreshing(false);
            replySwipeRefreshLayout.setRefreshing(false);
            dmSwipeRefreshLayout.setRefreshing(false);
        }

    }


    private void sendNotification() {
        mManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContentIntent(pi);
        builder.setSmallIcon(R.drawable.darkmoon);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.darkmoon));
        builder.setContentTitle("起動しました");
        builder.setContentText("タップしてアプリに移動");
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(false);
        mManager.notify(0, builder.build());
    }

    private void setDialogListView(TimelineData item){
        FragmentManager manager = getFragmentManager();
        ListDialog listDialog =  ListDialog.newInstance(item, twitterStreamFlag);
        listDialog.setViewPager(viewPager);
        listDialog.setTweetText(tweetText);
        listDialog.setActivity(this);
        listDialog.setTwitter(twitter);
        listDialog.setTimelineData(item);
        Log.d("call", "let's show dialog");
        listDialog.show(manager, "list_dialog");

    }

    public static void updateAdapter(long tweetID,boolean favflag,boolean isReply) {
        for (int i = 0; i < timelineAdapter.getCount(); i++) {
            TimelineData item = timelineAdapter.getItem(i);
            if (item.getTweetID() == tweetID)
                item.setIsFavorite(favflag);
        }

        timelineAdapter.notifyDataSetChanged();
        Log.d("is reply?",""+isReply);

        if(!isReply)
            return;

        for (int i = 0; i < replyAdapter.getCount(); i++) {
            TimelineData item = replyAdapter.getItem(i);
            if (item.getTweetID() == tweetID)
                item.setIsFavorite(favflag);

            replyAdapter.notifyDataSetChanged();
        }
    }

    void setFooterLister(View footer, final TimelineAdapter tlAdapter, final boolean isMentions){
        final TextView textView = (TextView)footer.findViewById(R.id.footer_text);
        final Paging paging = new Paging();
        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setText("取得中");
                long maxId = tlAdapter.getItem(tlAdapter.getCount()-1).getTweetID();
                Paging paging = new Paging();
                paging.setMaxId(maxId);
                new MoreTimelineAsyncTask(paging,twitter,tlAdapter,handler,isMentions,textView,"タッチして取得").execute();
            }
        });
    }

    void setDMFooterLister(View footer, final TimelineAdapter tlAdapter){
        final TextView textView = (TextView)footer.findViewById(R.id.footer_text);
        final Paging paging = new Paging();
        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setText("取得中");
                long maxId = tlAdapter.getItem(tlAdapter.getCount()-1).getTweetID();
                Paging paging = new Paging();
                paging.setMaxId(maxId);
                new DirectMessageAsyncTask(paging,twitter,tlAdapter,handler,textView,"タッチして取得").execute();
            }
        });
    }

    void setListContent(){
        //swipeRefleshLayouをつくる
        View timelineView = this.getLayoutInflater().inflate(R.layout.timeline_list,null);
        timelineSwipeRefreshLayout = (SwipeRefreshLayout)timelineView.findViewById(R.id.refresh);
        View replyView = this.getLayoutInflater().inflate(R.layout.timeline_list,null);
        replySwipeRefreshLayout = (SwipeRefreshLayout)replyView.findViewById(R.id.refresh);
        View dmView = this.getLayoutInflater().inflate(R.layout.timeline_list,null);
        dmSwipeRefreshLayout = (SwipeRefreshLayout)dmView.findViewById(R.id.refresh);
        View twListView = this.getLayoutInflater().inflate(R.layout.timeline_list,null);
        twListSwipeRefreshLayout = (SwipeRefreshLayout)twListView.findViewById(R.id.refresh);
        //swipeRefleshLayoutのリスナとか
        setSwipeRefleshLayoutListener(timelineSwipeRefreshLayout);
        setSwipeRefleshLayoutListener(replySwipeRefreshLayout);
        setSwipeRefleshLayoutListener(dmSwipeRefreshLayout);
        setSwipeRefleshLayoutListener(twListSwipeRefreshLayout);

        //Listとviewpager,adapterの作成
        //Listviewの作成
        timeLineList = (ListView)timelineView.findViewById(R.id.timeline);
        replyList = (ListView)replyView.findViewById(R.id.timeline);
        dmList = (ListView)dmView.findViewById(R.id.timeline);
        twList = (ListView)twListView.findViewById(R.id.timeline);
        //tweetViewの作成
        tweet = this.getLayoutInflater().inflate(R.layout.tweet,null);
        filePathVector = new Vector<String>();
        viewVector = new Vector<View>();
        viewVector.add(tweet);
        viewVector.add(timelineView);
        viewVector.add(replyView);
        viewVector.add(dmView);
        viewVector.add(twListView);
        //Tweet画面のLister
        //tlAdapterの作成
        timelineAdapter = new TimelineAdapter(this,R.layout.status);
        replyAdapter = new TimelineAdapter(this,R.layout.status);
        dmAdapter = new TimelineAdapter(this,R.layout.status);
        twListAdapter = new TimelineAdapter(this,R.layout.status);
        //footerをいれておく
        View timelineFooter = this.getLayoutInflater().inflate(R.layout.list_footer,null);
        View replyFooter = this.getLayoutInflater().inflate(R.layout.list_footer,null);
        View dmFooter = this.getLayoutInflater().inflate(R.layout.list_footer,null);
        View listFooter = this.getLayoutInflater().inflate(R.layout.list_footer,null);
        setFooterLister(timelineFooter,timelineAdapter,false);
        setFooterLister(replyFooter,replyAdapter,true);
        setDMFooterLister(dmFooter,dmAdapter);
        timeLineList.addFooterView(timelineFooter);
        replyList.addFooterView(replyFooter);
        dmList.addFooterView(dmFooter);
        //setしておく
        timeLineList.setAdapter(timelineAdapter);
        replyList.setAdapter(replyAdapter);
        dmList.setAdapter(dmAdapter);
        twList.setAdapter(twListAdapter);
        setListViewListener(timeLineList);
        setListViewListener(replyList);
        setListViewListener(twList);
        //pagerAdapterをつくる
        //まずViewPager
        viewPager = (ViewPager)findViewById(R.id.pager);
        listPagerAdapter = new ListPagerAdapter(this,viewVector);
        viewPager.setAdapter(listPagerAdapter);
        viewPager.setCurrentItem(1);
        setViewPagerListener();
        //strip
        PagerTabStrip pagerTabStrip = (PagerTabStrip)findViewById(R.id.strip);
        pagerTabStrip.setTextColor(0xffffffff);
        pagerTabStrip.setDrawFullUnderline(true);
        pagerTabStrip.setTabIndicatorColor(0xfff00000);
    }

    public static Twitter getTwitter(){
        return twitter;
    }


}
