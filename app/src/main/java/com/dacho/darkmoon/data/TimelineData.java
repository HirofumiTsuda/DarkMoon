package com.dacho.darkmoon.data;

import com.dacho.darkmoon.activitys.MainActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.DirectMessage;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;

/**
 * Created by hirofumi on 15/05/09.
 */
public class TimelineData {
    //timelineに表示させるもの
    //なまえ ID
    private String userID;
    private String userName;
    private long userNum;
    //ツイートのID
    private long tweetID;
    //リプライ先のID
    private long replyID;
    //リツイートしたもの本体のid
    private long retweetID = -1;
    //本文
    private String tweetText;
    private String client;
    private String profileURLHttps;
    private String miniProfileURLHttps;
    private String biggerProfileURLHttps;
    //Retweetされたやつなのかどうか
    private boolean isRetweet;
    //自分がretweeできるかどうか
    private boolean isRetweeted;
    //リツイートしたやつについて
    private String retweetUserName;
    private String retweetUserID = "";
    private String retweetProfileURLHttps;
    private int retweetCount;
    //ツイートされた時間
    private String createdTime;
    private Date createdTimeDate;
    //ふぁぼされたかどうか
    private boolean isFavorite;
    //鍵がかかっているか
    private boolean isLock;
    //Media
    private ArrayList<URLEntity> urlEntities;
    private ArrayList<MediaEntity> mediaEntities;
    private boolean isReplyToUser;
    //hashtag
    private ArrayList<HashtagEntity> hashtagEntities;

    public TimelineData(twitter4j.DirectMessage status){
        //ユーザー情報
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        //送ったユーザーについて
        User user = status.getSender();
        setUserID(user.getScreenName());
        setUserName(user.getName());
        setUserNum(user.getId());
        setIsLock(user.isProtected());
        //本文
        setTweetText(status.getText());
        setClient("via Direct Message");
        //アイコンURL
        setProfileURLHttps(user.getProfileImageURLHttps());
        setMiniProfileURLHttps(user.getMiniProfileImageURLHttps());
        setBiggerProfileURLHttps(user.getBiggerProfileImageURLHttps());
        //ツイートされた時間
        setCreatedTime(sdf.format(status.getCreatedAt()));
        //status id
        setTweetID(status.getId());
        setUrlEntities(status.getURLEntities());
        setMediaEntities(status.getExtendedMediaEntities());
        setHashtagEntities(status.getHashtagEntities());
    }



    public TimelineData(twitter4j.Status status){
        //ユーザー情報
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Status item = status;
        //リツイートされたやつかどうか
        setIsRetweet(status.isRetweet());
        if(status.isRetweet()){
            //リツイートされたやつなら　もともとのユーザーのアイコン　名前をもってくる
            User user = status.getUser();
            setRetweetUserName(user.getName());
            setRetweetID(status.getId());
            setRetweetUserID(user.getScreenName());
            setRetweetProfileURLHttps(user.getProfileImageURLHttps());
            setRetweetCount(status.getRetweetCount());
            item = status.getRetweetedStatus();

        }
        //ユーザーについて
        User user = item.getUser();
        setUserID(user.getScreenName());
        setUserName(user.getName());
        setUserNum(user.getId());
        setIsLock(user.isProtected());
        //本文
        setTweetText(item.getText());
        setClient("via " + item.getSource().replaceAll("<.+?>", ""));
        //アイコンURL
        setProfileURLHttps(user.getProfileImageURLHttps());
        setMiniProfileURLHttps(user.getMiniProfileImageURLHttps());
        setBiggerProfileURLHttps(user.getBiggerProfileImageURLHttps());
        //ツイートされた時間
        setCreatedTime(sdf.format(item.getCreatedAt()));
        setCreatedTimeDate(item.getCreatedAt());
        setIsFavorite(item.isFavorited());
        //status id
        setTweetID(item.getId());
        //reply
        setReplyID(item.getInReplyToStatusId());
        //リツイートされたかどうか
        setIsRetweeted(item.isRetweeted());
        setUrlEntities(item.getURLEntities());
        setMediaEntities(item.getExtendedMediaEntities());
        setHashtagEntities(item.getHashtagEntities());
        //自分宛のリプライか
        setIsReplyToUser(tweetText);
    }

    public void setUserID(String userID){
        this.userID = userID;
    }

    public void setTweetID(long tweetID){
        this.tweetID = tweetID;
    }

    public void setReplyID(long replyID){
        this.replyID = replyID;
    }

    public void setRetweetID(long retweetID){
        this.retweetID = retweetID;
    }

    public void setUserName(String userName){
        this.userName = userName;
    }

    public void setUserNum(long userNum){
        this.userNum = userNum;
    }

    public void setClient(String client){
        this.client = client;
    }

    public void setTweetText(String tweetText){
        this.tweetText = tweetText;
    }

    public void setProfileURLHttps(String profileURLHttps){
        this.profileURLHttps = profileURLHttps;
    }

    public void setMiniProfileURLHttps(String miniProfileURLHttps){
        this.miniProfileURLHttps = miniProfileURLHttps;
    }

    public void setBiggerProfileURLHttps(String biggerProfileURLHttps){
        this.biggerProfileURLHttps = biggerProfileURLHttps;
    }

    public void setRetweetUserName(String retweetUserName){
        this.retweetUserName = retweetUserName;
    }

    public void setRetweetUserID(String retweetUserID){
        this.retweetUserID = retweetUserID;
    }

    public void setRetweetProfileURLHttps(String retweetProfileURLHttps){
        this.retweetProfileURLHttps = retweetProfileURLHttps;
    }

    public void setCreatedTime(String createdTime){
        this.createdTime = createdTime;
    }

    public void setCreatedTimeDate(Date date){this.createdTimeDate = date;}

    public void setIsFavorite(boolean isFavorite){
        this.isFavorite = isFavorite;
    }

    public void setIsRetweet(boolean isRetweet){
        this.isRetweet = isRetweet;
    }

    public void setIsRetweeted(boolean isRetweeted){
        this.isRetweeted = isRetweeted;
    }

    public void setIsLock(boolean isLock){
        this.isLock = isLock;
    }

    public void setMediaEntities(MediaEntity[] mediaEntities){
        this.mediaEntities = new ArrayList<MediaEntity>();
        for(MediaEntity m : mediaEntities) {
            this.mediaEntities.add(m);
        }
    }

    public void setUrlEntities(URLEntity[] urlEntities){
        this.urlEntities = new ArrayList<URLEntity>();
        for(URLEntity m : urlEntities){
            this.urlEntities.add(m);
        }
    }

    public void setRetweetCount(int retweetCount){
        this.retweetCount = retweetCount+1;
    }


    private void setIsReplyToUser(String text){
        Pattern p = Pattern.compile("(?i)" + MainActivity.userName, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        isReplyToUser =  m.find();
    }

    private void setHashtagEntities(HashtagEntity[] hashtagEntities){
        this.hashtagEntities = new ArrayList<HashtagEntity>();
        for(HashtagEntity m : hashtagEntities){
            this.hashtagEntities.add(m);
        }
    }

    public String getUserID(){
        return userID;
    }

    public String getUserName(){
        return userName;
    }

    public long getUserNum() {return userNum; }

    public String getTweetText(){
        return tweetText;
    }

    public String getClient() {
        return client;
    }

    public String getProfileURLHttps(){
        return profileURLHttps;
    }

    public String getMiniProfileURLHttps(){
        return miniProfileURLHttps;
    }

    public String getBiggerProfileURLHttps(){
        return biggerProfileURLHttps;
    }

    public String getRetweetUserName(){
        return retweetUserName;
    }

    public String getRetweetUserID(){
        return  retweetUserID;
    }

    public long getTweetID(){
        return tweetID;
    }

    public long getReplyID(){
        return replyID;
    }

    public long getRetweetID(){
        return retweetID;
    }

    public ArrayList<URLEntity> getUrlEntities(){
        return urlEntities;
    }

    public ArrayList<MediaEntity> getMediaEntities(){
        return mediaEntities;
    }

    public String getRetweetProfileURLHttps(){
        return retweetProfileURLHttps;
    }

    public boolean getIsFavorite(){
        return  isFavorite;
    }

    public String getCreatedTime(){
        return createdTime;
    }

    public Date getCreatedTimeDate(){
        return createdTimeDate;
    }

    public boolean getIsRetweet(){
        return isRetweet;
    }

    public boolean getIsRetweeted(){
        return isRetweeted;
    }

    public boolean getIsLock(){
        return isLock;
    }

    public int getRetweetCount(){
        return retweetCount;
    }

    public boolean getIsReplyToUser(){
        return isReplyToUser;
    }

    public ArrayList<HashtagEntity> getHashtagEntities(){
        return hashtagEntities;
    }


}
