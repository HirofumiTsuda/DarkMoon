package com.dacho.darkmoon.dialogs;

/**
 * Created by admin on 2015/06/08.
 */
public class DialogData {
    private int source;
    private String text;
    private String tag;
    private Object object;

    public DialogData(int source,String text,String tag,Object object){
        setSource(source);
        setText(text);
        setTag(tag);
        setObject(object);
    }

    public DialogData(int source,String text,String tag){
        setSource(source);
        setText(text);
        setTag(tag);
    }

    public void setSource(int source){
        this.source = source;
    }

    public void setText(String text){
        this.text = text;
    }

    public void setTag(String tag){
        this.tag = tag;
    }

    public void setObject(Object object){
        this.object = object;
    }


    public int getSource(){
        return source;
    }

    public String getText(){
        return text;
    }

    public String getTag(){
        return tag;
    }

    public Object getObject(){
        return object;
    }

}
