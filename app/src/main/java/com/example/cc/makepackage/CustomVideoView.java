package com.example.cc.makepackage;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class CustomVideoView extends VideoView {
    public CustomVideoView(Context context){
        super(context);
    }
    public CustomVideoView(Context context, AttributeSet attrs){
        super(context,attrs);
    }
    public CustomVideoView(Context context,AttributeSet attrs,int defStyleAttr){
        super(context, attrs, defStyleAttr);
    }
    protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
        int width=getDefaultSize(0,widthMeasureSpec);
        int height=getDefaultSize(0,heightMeasureSpec);
        //设置画面大小
        setMeasuredDimension(width,height);
    }
}
