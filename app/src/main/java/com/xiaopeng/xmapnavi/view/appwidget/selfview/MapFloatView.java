package com.xiaopeng.xmapnavi.view.appwidget.selfview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.amap.api.maps.AMap;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.model.NaviLatLng;
import com.xiaopeng.lib.utils.utils.LogUtils;
import com.xiaopeng.xmapnavi.R;

/**
 * Created by linzx on 2016/11/14.
 */

public class MapFloatView extends View{
    private static final String TAG ="MapFloatView";
    private static final int LINE_TYPE_TOP=0 ;
    private static final int LINE_TYPE_BOTTOM=1 ;
    private static final int LINE_TYPE_LEFT=2 ;
    private static final int LINE_TYPE_RIGHT=3 ;
    int width =0;
    int height = 0;

    Projection projection;
    Point mPoint0,mPoint1;
    int mStyle0,mStyle1;
    Paint mPaint,mPaint2,mTxPaint;

    private String msg0,msg1;

    private int posiby0x,posiby0y,posiby1x,posiby1y;

    private XpFloatViewTouchListener mListner;

    public void setFloatViewTouchListener(XpFloatViewTouchListener listner){
        mListner = listner;
    }

    public MapFloatView(Context context) {
        super(context);
        init();
    }

    public MapFloatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private void init(){
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.white));
        mPaint.setStrokeWidth(2);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        mPaint2 = new Paint();
        mPaint2.setColor(getResources().getColor(R.color.text_blue));
        mPaint2.setStrokeWidth(2);
        mPaint2.setStyle(Paint.Style.STROKE);
        mPaint2.setAntiAlias(true);

        mTxPaint = new Paint();
        mTxPaint.setColor(getResources().getColor(R.color.text_blue));
        mTxPaint.setStrokeWidth(2);
        mTxPaint.setTextSize(30);
        mTxPaint.setTextAlign(Paint.Align.CENTER);
        mTxPaint.setAntiAlias(true);


    }

    public void setFirstString(String msg){
        msg0 = msg;
        invalidate();
    }
    public void setSecondString(String msg){
        msg1 = msg;
        invalidate();
    }

    public void initAmap(AMap aMap){
        this.projection = aMap.getProjection();
    }
    public void setPoint(LatLng latLng,int style0,@Nullable LatLng latLng1,@Nullable int style1){
        mPoint0 = null;
        mPoint1 = null;

        mStyle0 = style0;
        mStyle1 = style1;
        mPoint0 = projection.toScreenLocation(latLng);
        if (latLng1!=null) {
            mPoint1 = projection.toScreenLocation(latLng1);
        }
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (width == 0){
            width = getWidth();
            height = getHeight();
        }
        drawLineCir(canvas);

    }

    private void drawLineCir(Canvas canvas){

        if (mPoint0!=null) {
            RectF rectF;
            switch (mStyle0) {
                case LINE_TYPE_TOP:
                    rectF = new RectF();
                    rectF.set(mPoint0.x-40,mPoint0.y-115,mPoint0.x+40,mPoint0.y-85);
                    canvas.drawRect(mPoint0.x-78,mPoint0.y-128,mPoint0.x+78,mPoint0.y-72,mPaint);
                    canvas.drawRect(mPoint0.x-78,mPoint0.y-128,mPoint0.x+78,mPoint0.y-72,mPaint2);
                    if (msg0!=null) {
                        canvas.drawText(msg0,mPoint0.x,mPoint0.y - 100+10,mTxPaint);
                    }
                    posiby0x = mPoint0.x-84;
                    posiby0y = mPoint0.y-134;

                    break;

                case LINE_TYPE_BOTTOM:
                    rectF = new RectF();
                    rectF.set(mPoint0.x-40,mPoint0.y+85,mPoint0.x+40,mPoint0.y+1155);
                    canvas.drawRect(mPoint0.x-78,mPoint0.y+72,mPoint0.x+78,mPoint0.y+128,mPaint);
                    canvas.drawRect(mPoint0.x-78,mPoint0.y+72,mPoint0.x+78,mPoint0.y+128,mPaint2);
                    if (msg0!=null) {
                        canvas.drawText(msg0,mPoint0.x,mPoint0.y + 100+10,mTxPaint);
                    }
                    posiby0x = mPoint0.x-84;
                    posiby0y = mPoint0.y+66;
                    break;

                case LINE_TYPE_RIGHT:
                    rectF = new RectF();
                    rectF.set(mPoint0.x+60,mPoint0.y-15,mPoint0.x+140,mPoint0.y+15);
                    canvas.drawRect(mPoint0.x+22,mPoint0.y-28,mPoint0.x+178,mPoint0.y+28,mPaint);
                    canvas.drawRect(mPoint0.x+22,mPoint0.y-28,mPoint0.x+178,mPoint0.y+28,mPaint2);
                    if (msg0!=null) {
                        canvas.drawText(msg0,mPoint0.x + 100,mPoint0.y+10,mTxPaint);
                    }
                    posiby0x = mPoint0.x+16;
                    posiby0y = mPoint0.y-34;
                    break;

                case LINE_TYPE_LEFT:
                    rectF = new RectF();
                    rectF.set(mPoint0.x-140,mPoint0.y-15,mPoint0.x-60,mPoint0.y+15);
                    canvas.drawRect(mPoint0.x-178,mPoint0.y-28,mPoint0.x-22,mPoint0.y+28,mPaint);
                    canvas.drawRect(mPoint0.x-178,mPoint0.y-28,mPoint0.x-22,mPoint0.y+28,mPaint2);
                    if (msg0!=null) {
                        canvas.drawText(msg0,mPoint0.x - 100,mPoint0.y+10,mTxPaint);
                    }
                    posiby0x = mPoint0.x-184;
                    posiby0y = mPoint0.y-34;
                    break;

                default:
                    break;
            }
        }

        if (mPoint1!=null) {
            RectF rectF;
            switch (mStyle1) {
                case LINE_TYPE_TOP:
                    rectF = new RectF();
                    rectF.set(mPoint1.x-40,mPoint1.y-115,mPoint1.x+40,mPoint1.y-85);
                    canvas.drawRect(mPoint1.x-78,mPoint1.y-128,mPoint1.x+78,mPoint1.y-72,mPaint);
                    canvas.drawRect(mPoint1.x-78,mPoint1.y-128,mPoint1.x+78,mPoint1.y-72,mPaint2);
                    if (msg1!=null) {
                        canvas.drawText(msg1,mPoint1.x,mPoint1.y - 100 + 10,mTxPaint);
                    }
                    posiby1x = mPoint1.x-84;
                    posiby1y = mPoint1.y-134;

                    break;

                case LINE_TYPE_BOTTOM:
                    rectF = new RectF();
                    rectF.set(mPoint1.x-40,mPoint1.y+85,mPoint1.x+40,mPoint1.y+115);
                    canvas.drawRect(mPoint1.x-78,mPoint1.y+72,mPoint1.x+78,mPoint1.y+128,mPaint);
                    canvas.drawRect(mPoint1.x-78,mPoint1.y+72,mPoint1.x+78,mPoint1.y+128,mPaint2);
                    if (msg1!=null) {
                        canvas.drawText(msg1,mPoint1.x,mPoint1.y + 100 + 10,mTxPaint);
                    }
                    posiby1x = mPoint1.x-84;
                    posiby1y = mPoint1.y+66;
                    break;

                case LINE_TYPE_RIGHT:
                    rectF = new RectF();
                    rectF.set(mPoint1.x+60,mPoint1.y-15,mPoint1.x+140,mPoint1.y+15);
                    canvas.drawRect(mPoint1.x+22,mPoint1.y-28,mPoint1.x+178,mPoint1.y+28,mPaint);
                    canvas.drawRect(mPoint1.x+22,mPoint1.y-28,mPoint1.x+178,mPoint1.y+28,mPaint2);
                    if (msg1!=null) {
                        canvas.drawText(msg1,mPoint1.x + 100,mPoint1.y+10,mTxPaint);
                    }
                    posiby1x = mPoint1.x+16;
                    posiby1y = mPoint1.y-34;
                    break;

                case LINE_TYPE_LEFT:
                    rectF = new RectF();
                    rectF.set(mPoint1.x-140,mPoint1.y-15,mPoint1.x-60,mPoint1.y+15);
                    canvas.drawRect(mPoint1.x-178,mPoint1.y-28,mPoint1.x-22,mPoint1.y+28,mPaint);
                    canvas.drawRect(mPoint1.x-178,mPoint1.y-28,mPoint1.x-22,mPoint1.y+28,mPaint2);
                    if (msg1!=null) {
                        canvas.drawText(msg1,mPoint1.x - 100,mPoint1.y+10,mTxPaint);
                    }
                    posiby1x = mPoint1.x-184;
                    posiby1y = mPoint1.y-34;
                    break;

                default:
                    break;
            }
        }
    }




    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN){

            float posix = event.getX();
            float posiy = event.getY();
            LogUtils.d(TAG,"onTouch:\nx:"+posix+"\ny:"+posiy+"\nposiby0x:"+posiby0x+"\nposiby0y:"+posiby0y+"\nposiby1x:"+posiby1x+"\nposiby1y:"+posiby1y);
            if (posix>posiby0x && posix<posiby0x + 168 && posiy>posiby0y && posiy<posiby0y+68) {
                if (mListner!=null){
                    mListner.touchOne(0);
                }
                return true;
            }

            if (posix>posiby1x && posix<posiby1x + 168 && posiy>posiby1y && posiy<posiby1y+68) {
                if (mListner!=null){
                    mListner.touchOne(1);
                }
                return true;
            }

        }
        return super.onTouchEvent(event);
    }

    public interface XpFloatViewTouchListener{
        void touchOne(int clickOne);
    }
}
