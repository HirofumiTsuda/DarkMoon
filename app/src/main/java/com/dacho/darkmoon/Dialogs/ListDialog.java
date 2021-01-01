    package com.dacho.darkmoon.dialogs;

    import android.app.Activity;
    import android.app.Dialog;
    import android.app.DialogFragment;
    import android.content.Context;
    import android.content.Intent;
    import android.net.Uri;
    import android.os.Bundle;
    import android.support.v4.view.ViewPager;
    import android.util.Log;
    import android.view.View;
    import android.view.Window;
    import android.widget.AdapterView;
    import android.widget.EditText;
    import android.widget.ListView;
    import android.widget.TextView;

    import com.dacho.darkmoon.R;
    import com.dacho.darkmoon.activitys.MainActivity;
    import com.dacho.darkmoon.async.FavoriteAsyncTask;
    import com.dacho.darkmoon.async.RetweetAsyncTask;
    import com.dacho.darkmoon.data.TimelineData;
    import com.dacho.darkmoon.values.ReplyIDManager;

    import twitter4j.HashtagEntity;
    import twitter4j.MediaEntity;
    import twitter4j.Twitter;
    import twitter4j.URLEntity;

    /**
     * Created by admin on 2015/06/07.
     */
    public class ListDialog extends DialogFragment{

        TimelineData item;
        Context context;
        Activity activity;
        ViewPager viewPager;
        EditText tweetText;
        Twitter twitter;
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final String user,text,retweet;
            Log.d("call","list_dialog");
            final Dialog dialog = new Dialog(activity);
            Log.d("call", "set activity");
            // タイトル非表示
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.list_dialog);
            Log.d("call","set preference");
            //Arguments
            user = getArguments().getString("user");
            text = getArguments().getString("text");
            retweet = getArguments().getString("retweet");
            final long id = getArguments().getLong("tweetID");
            final long replyID = getArguments().getLong("toReplyID");
            final boolean twitterStreamFlag = getArguments().getBoolean("flag");
            Log.d("call", "arguments");
            //ListViewをセット
            ListView list = (ListView)dialog.findViewById(R.id.dialog_list);
            Log.d("call", "set list");
            DialogListAdapter dialogListAdapter = new DialogListAdapter(this.getActivity(),R.layout.list_dialog_status);
            Log.d("call", "create adapter");
            dialogListAdapter.add(new DialogData(R.drawable.at_sign_32, "返信", "reply"));
            if(!item.getIsFavorite()) {
                dialogListAdapter.add(new DialogData(R.drawable.star_black_fivepointed_shape_32, "お気に入り", "fav"));
            }else{
                dialogListAdapter.add(new DialogData(R.drawable.star_black_fivepointed_shape_32, "お気に入りを解除", "fav"));
            }
            dialogListAdapter.add(new DialogData(R.drawable.star_black_fivepointed_shape_32, "お気に入り & RT", "fav_rt"));
            if(!item.getIsRetweeted()){
                dialogListAdapter.add(new DialogData(R.drawable.share_32, "RT", "rt"));
            }else{
                dialogListAdapter.add(new DialogData(R.drawable.share_32, "RTを解除", "rt"));
            }
            dialogListAdapter.add(new DialogData(R.drawable.share_32, "非公式RT", "unofficial_rt"));
            dialogListAdapter.add(new DialogData(R.drawable.user_silhouette_32, "@"+user, "userID",user));
            if(retweet.length() != 0) {
                dialogListAdapter.add(new DialogData(R.drawable.user_silhouette_32,"@"+retweet,"retweetID",retweet));
            }

            for(HashtagEntity m : item.getHashtagEntities()){
                dialogListAdapter.add(new DialogData(R.drawable.hashtag_symbol_32,"#"+m.getText(),"hash",m.getText()));
            }

            for(MediaEntity m :item.getMediaEntities()){
                dialogListAdapter.add(new DialogData(R.drawable.world_wide_web_32,m.getDisplayURL(),"URL",m.getExpandedURL()));
                Log.d("media",m.getExpandedURL());
            }

            for(URLEntity m :item.getUrlEntities()){
                dialogListAdapter.add(new DialogData(R.drawable.world_wide_web_32,m.getExpandedURL(),"URL",m.getExpandedURL()));
            }

            if(replyID != -1){
                        dialogListAdapter.add(new DialogData(R.drawable.comments_32,"会話を表示","talk"));
            }

            list.setAdapter(dialogListAdapter);

                //リストビューのリスナ
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                DialogData dialogData = (DialogData)((ListView)adapterView).getItemAtPosition(i);
                                String tag = dialogData.getTag();
                                Uri uri;
                                Intent intent;
                                Log.d("tag", tag);
                                switch (tag){
                                    case "reply":
                                        Log.d("reply ID in Dialog", "" + id);
                                        ReplyIDManager.id = id;
                                        ReplyIDManager.before_page = viewPager.getCurrentItem();
                                        viewPager.setCurrentItem(0);
                                        tweetText.setText("@"+user + " ");
                                        tweetText.setSelection(user.length()+2);
                                        break;
                                    case "rt":
                                        new RetweetAsyncTask(twitter,item).execute();
                                        break;
                                    case "fav":
                                        new FavoriteAsyncTask(twitter,item,twitterStreamFlag).execute();
                                        break;
                                    case "fav_rt":
                                        new RetweetAsyncTask(twitter,item).execute();
                                        new FavoriteAsyncTask(twitter,item,twitterStreamFlag).execute();
                                        break;
                                    case "unofficial_rt":
                                        tweetText.setText(" RT @" + user + ": " + item.getTweetText());
                                        viewPager.setCurrentItem(0);
                                        tweetText.setSelection(0);
                                        break;
                                    case "URL":
                                        uri = Uri.parse((String)dialogData.getObject());
                                        intent = new Intent(Intent.ACTION_VIEW,uri);
                                        startActivity(intent);
                                        break;

                        }
                        dismiss();
                    }
            });

            Log.d("call", "set adapter");

            return dialog;
        }

        public void setActivity(Activity activity){
            this.activity = activity;
        }

        public void setTwitter(Twitter twitter){
            this.twitter = twitter;
        }

        public void setViewPager(ViewPager viewPager){
            this.viewPager = viewPager;
        }

        public void setTweetText(EditText tweetText){
            this.tweetText = tweetText;
        }

        public void setTimelineData(TimelineData timelineData){
            item = timelineData;
        }

        public static ListDialog newInstance(TimelineData item,boolean twitterStreamFlag){
            ListDialog listDialog = new ListDialog();
            Bundle bundle = new Bundle();
            bundle.putString("user", item.getUserID());
            bundle.putBoolean("flag", twitterStreamFlag);
            bundle.putString("text",item.getTweetText());
            bundle.putString("retweet", item.getRetweetUserID());
            bundle.putLong("tweetID", item.getTweetID());
            bundle.putLong("toReplyID", item.getReplyID());
            listDialog.setArguments(bundle);
            return listDialog;
        }



    }
