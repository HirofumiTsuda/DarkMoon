package com.dacho.darkmoon.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dacho.darkmoon.R;
import com.dacho.darkmoon.cache.BitmapCache;

/**
 * Created by admin on 2015/06/08.
 */
public class DialogListAdapter extends ArrayAdapter<DialogData>{

    private LayoutInflater layoutInflater;



    private static class ViewHolder {
        TextView text;
        ImageView icon;

        public ViewHolder(View view){
            this.text = (TextView)view.findViewById(R.id.dialog_text);
            this.icon = (ImageView)view.findViewById(R.id.dialog_icon);
        }
    }

    public DialogListAdapter(Context context, int resource) {
        super(context, resource);
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        //表示するデータをとりだす
        DialogData item = (DialogData)getItem(position);
        //まずViewがなかった場合
        if(convertView == null ){
            //新しいviewを生成 convertViewにいれる
            convertView = layoutInflater.inflate(R.layout.list_dialog_status,null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            //nullでなかったらholderをとりだす
            viewHolder = (ViewHolder)convertView.getTag();
        }
        //データの中身をいれていく
        viewHolder.icon.setImageResource(item.getSource());
        viewHolder.text.setText(item.getText());

        return  convertView;
    }

}
