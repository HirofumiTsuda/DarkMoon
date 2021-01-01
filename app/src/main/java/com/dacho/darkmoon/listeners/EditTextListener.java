package com.dacho.darkmoon.listeners;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by admin on 2015/05/28.
 */
public class EditTextListener implements TextWatcher{

    private static final int MAX_COUNT = 140;

    TextView textView;

    public EditTextListener(TextView textView){
        this.textView = textView;
    }


    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        //文字列を取り出す
        String str = editable.toString();
        Log.d("call","get str "+textView);
        int count = MAX_COUNT - str.length();
        //140字以上　すなわち0以下なら赤くする
        if(count < 0){
            textView.setText(""+count);
            textView.setTextColor(0xffff0000);
            //100文字を超えていたら黄色く
        }else if(count < 40){
            textView.setText(""+count);
            textView.setTextColor(0xffff5e19);
            //そうでもなかったら白く
        }else{
            textView.setText(""+count);
            textView.setTextColor(0xffffffff);
        }

    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }
}
