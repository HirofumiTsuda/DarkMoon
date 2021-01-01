package com.dacho.darkmoon.data;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.Vector;

/**
 * Created by hirofumi on 15/05/25.
 */
public class ListPagerAdapter extends PagerAdapter {
    private  static int PAGE_COUNT = 4;
    private Context context;
    private Vector<View> pages;
    public static int destroy;
    public static int instantiate;

    public  ListPagerAdapter(Context context,Vector<View> pages){
        this.context = context;
        this.pages = pages;
    }

    public void add(View item,int location){
        pages.add(location,item);
    }

    public void add(View item){
        pages.add(item);
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        //ページを生成するもの
        //中にはListViewをいれる
        //ListViewあらかじめつくっておく
        instantiate = position;
        Log.d("pager inst",""+position);
        View page = pages.get(position);
        container.addView(page);
        return page;
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        destroy = position;
        Log.d("pager destroy",""+position);
        container.removeView((View)object);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String str;
        switch(position){
            case 0:
                str = "Tweet";
                break;
            case 1:
                str = "Home";
                break;
            case 2:
                str = "Reply";
                break;
            case 3:
                str = "DM";
                break;
            case 4:
                str = "list";
                break;
            default:
                str = "What happen";
        }
        return str;
    }


}
