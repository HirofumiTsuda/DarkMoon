package com.dacho.darkmoon.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dacho.darkmoon.R;
import com.dacho.darkmoon.activitys.MainActivity;
import com.dacho.darkmoon.cache.BitmapCache;
import com.dacho.darkmoon.async.BitmapLoading;

import java.util.ArrayList;

import twitter4j.URLEntity;

/**
 * Created by hirofumi on 15/05/09.
 */
public class TimelineAdapter extends ArrayAdapter<TimelineData>{

    private LayoutInflater layoutInflater;
    private static BitmapCache bitmapCache;

    private static class ViewHolder {
        TextView userName;
        TextView tweetText;
        TextView client;
        ImageView userIcon;
        ImageView retweetUserIcon;
        ImageView retweetMark;
        ImageView star;
        TextView createTime;
        TextView retweetText;
        ImageView lock;
        LinearLayout layout;

        public ViewHolder(View view){
            this.userName = (TextView)view.findViewById(R.id.userName);
            this.tweetText = (TextView)view.findViewById(R.id.tweetText);
            this.client = (TextView)view.findViewById(R.id.client);
            this.userIcon = (ImageView)view.findViewById(R.id.userIcon);
            this.retweetUserIcon = (ImageView)view.findViewById(R.id.retweetUserIcon);
            this.retweetMark = (ImageView)view.findViewById(R.id.retweetMark);
            this.star = (ImageView)view.findViewById(R.id.star);
            this.createTime = (TextView)view.findViewById(R.id.time);
            this.retweetText = (TextView)view.findViewById(R.id.retweetText);
            this.lock = (ImageView)view.findViewById(R.id.lock);
            this.layout = (LinearLayout)view.findViewById(R.id.status_layout);

        }
    }


    public TimelineAdapter(Context context, int resource) {
        super(context, resource);
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //nullならつくっておく
        if(bitmapCache == null) {
            bitmapCache = new BitmapCache();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        //まずViewがなかった場合
        if(convertView == null){
            //新しいviewを生成 convertViewにいれる
            convertView = layoutInflater.inflate(R.layout.status,null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            //nullでなかったらholderをとりだす
            viewHolder = (ViewHolder)convertView.getTag();
        }
        //表示するデータをとりだす
        TimelineData item = (TimelineData)getItem(position);
        //データの中身をいれていく
        viewHolder.userName.setText("@"+item.getUserID());
        viewHolder.tweetText.setText(item.getTweetText());
        viewHolder.client.setText(item.getClient());
        viewHolder.createTime.setText(item.getCreatedTime());
        //ふぁぼされてるなら星を表示　でないなら非表示
        if(item.getIsFavorite()){
            viewHolder.star.setVisibility(View.VISIBLE);
        }else{
            viewHolder.star.setVisibility(View.INVISIBLE);
        }
        //鍵なら鍵マーク
        if(item.getIsLock()){
            viewHolder.lock.setVisibility(View.VISIBLE);
        }else{
            viewHolder.lock.setVisibility(View.INVISIBLE);
        }
        //アイコンはキャッシュ、または取得しにいく
        setImage(viewHolder.userIcon,item.getBiggerProfileURLHttps());
        //リツイートされたやつならアイコンと説明を描画
        if(item.getIsRetweet()){
            viewHolder.retweetMark.setVisibility(View.VISIBLE);
            viewHolder.retweetText.setVisibility(View.VISIBLE);
            viewHolder.retweetUserIcon.setVisibility(View.VISIBLE);
            viewHolder.retweetText.setText("Retweeted By @" + item.getRetweetUserID() +
                    " (" + item.getRetweetCount() + " users retweet)");
            setImage(viewHolder.retweetUserIcon,item.getRetweetProfileURLHttps());
        }else {
            //リツイートされてないなら非表示に
            viewHolder.retweetMark.setVisibility(View.GONE);
            viewHolder.retweetUserIcon.setVisibility(View.GONE);
            viewHolder.retweetText.setVisibility(View.GONE);
        }


        return  convertView;
    }
    void setImage(ImageView imageView,String url){
        Bitmap bitmap = bitmapCache.getBitmap(url);
        //まず仮の画像を貼っておく
        //imageView.setImageResource(R.mipmap.darkmoon);
        if(bitmap==null) {
            new BitmapLoading(url, imageView, bitmapCache).execute();
        }else{
            imageView.setImageBitmap(bitmap);
        }
    }

}
