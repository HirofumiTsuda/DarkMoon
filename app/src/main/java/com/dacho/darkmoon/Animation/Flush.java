package com.dacho.darkmoon.animation;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by hirofumi on 15/05/09.
 */
public class Flush {
    private android.os.Handler handler;
    private ScheduledExecutorService mScheduledExecutorService;
    private TextView textView;
    public Flush(android.os.Handler handler,TextView textView){
        this.handler = handler;
        this.textView = textView;
    }

    public void stopFlush(){
        handler.removeCallbacksAndMessages(null);
    }

    public void startFlush(){
        mScheduledExecutorService = Executors.newScheduledThreadPool(2);
        mScheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.setVisibility(View.VISIBLE);

                        // HONEYCOMBより前のAndroid SDKがProperty Animation非対応のため
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            animateAlpha();

                        }
                    }
                });

            }

                private void animateAlpha(){

                    // 実行するAnimatorのリスト
                    List<Animator> animatorList = new ArrayList<Animator>();

                    // alpha値を0から1へ1000ミリ秒かけて変化させる。
                    ObjectAnimator animeFadeIn = ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f);
                    animeFadeIn.setDuration(2000);

                    // alpha値を1から0へ600ミリ秒かけて変化させる。
                    ObjectAnimator animeFadeOut = ObjectAnimator.ofFloat(textView, "alpha", 1f, 0f);
                    animeFadeOut.setDuration(1200);

                    // 実行対象Animatorリストに追加。
                    animatorList.add(animeFadeIn);
                    animatorList.add(animeFadeOut);

                    final AnimatorSet animatorSet = new AnimatorSet();

                    // リストの順番に実行
                    animatorSet.playSequentially(animatorList);

                    animatorSet.start();
                }
            }, 1500, 3300, TimeUnit.MILLISECONDS);


    }
}
