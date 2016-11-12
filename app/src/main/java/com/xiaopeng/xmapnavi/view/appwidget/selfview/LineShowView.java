package com.xiaopeng.xmapnavi.view.appwidget.selfview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import com.xiaopeng.lib.utils.utils.LogUtils;
import com.xiaopeng.xmapnavi.R;

/**
 * Created by linzx on 2016/11/10.
 */

public class LineShowView extends View{
    private static final String TAG =  "LineShowView";
    private Paint mPaint;
    private Context mContext;
    private int width = 0,height = 0;
    private Point mCent;
    private Point point;
    public LineShowView(Context context) {
        super(context);
        init(context);
    }

    public LineShowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    private void init(Context context){
        mContext = context;
        mPaint = new Paint();
        mPaint.setColor(context.getResources().getColor(R.color.red));
        mPaint.setStrokeWidth(2);
        mCent = new Point();
    }

    public void setPoint(Point point){
        this.point = point;
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getWidth()!=0){
            width = getWidth();
            height  = getHeight();
            LogUtils.d(TAG,"onAttachedToWindow   width:"+width);
            mCent.set(width/2,height/2);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (width==0){
            width = getWidth();
            height = getHeight();
            mCent.set(width/2,height/2);
        }
        if (point!=null && mCent!=null) {
            canvas.drawLine(point.x, point.y, mCent.x, mCent.y, mPaint);
        }
    }
}
