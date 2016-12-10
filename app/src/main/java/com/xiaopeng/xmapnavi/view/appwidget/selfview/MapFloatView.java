package com.xiaopeng.xmapnavi.view.appwidget.selfview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
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
                    canvas.drawRect(mPoint0.x-84,mPoint0.y-134,mPoint0.x+84,mPoint0.y-66,mPaint);
                    canvas.drawRect(mPoint0.x-84,mPoint0.y-134,mPoint0.x+84,mPoint0.y-66,mPaint2);
                    if (msg0!=null) {
                        canvas.drawText(msg0,mPoint0.x,mPoint0.y - 100+10,mTxPaint);
                    }
                    break;

                case LINE_TYPE_BOTTOM:
                    rectF = new RectF();
                    rectF.set(mPoint0.x-40,mPoint0.y+85,mPoint0.x+40,mPoint0.y+1155);
                    canvas.drawRect(mPoint0.x-84,mPoint0.y+66,mPoint0.x+84,mPoint0.y+134,mPaint);
                    canvas.drawRect(mPoint0.x-84,mPoint0.y+66,mPoint0.x+84,mPoint0.y+134,mPaint2);
                    if (msg0!=null) {
                        canvas.drawText(msg0,mPoint0.x,mPoint0.y + 100+10,mTxPaint);
                    }
                    break;

                case LINE_TYPE_RIGHT:
                    rectF = new RectF();
                    rectF.set(mPoint0.x+60,mPoint0.y-15,mPoint0.x+140,mPoint0.y+15);
                    canvas.drawRect(mPoint0.x+16,mPoint0.y-34,mPoint0.x+184,mPoint0.y+34,mPaint);
                    canvas.drawRect(mPoint0.x+16,mPoint0.y-34,mPoint0.x+184,mPoint0.y+34,mPaint2);
                    if (msg0!=null) {
                        canvas.drawText(msg0,mPoint0.x + 100,mPoint0.y+10,mTxPaint);
                    }
                    break;

                case LINE_TYPE_LEFT:
                    rectF = new RectF();
                    rectF.set(mPoint0.x-140,mPoint0.y-15,mPoint0.x-60,mPoint0.y+15);
                    canvas.drawRect(mPoint0.x-184,mPoint0.y-34,mPoint0.x-16,mPoint0.y+34,mPaint);
                    canvas.drawRect(mPoint0.x-184,mPoint0.y-34,mPoint0.x-16,mPoint0.y+34,mPaint2);
                    if (msg0!=null) {
                        canvas.drawText(msg0,mPoint0.x - 100,mPoint0.y+10,mTxPaint);
                    }
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
                    canvas.drawRect(mPoint1.x-84,mPoint1.y-134,mPoint1.x+84,mPoint1.y-66,mPaint);
                    canvas.drawRect(mPoint1.x-84,mPoint1.y-134,mPoint1.x+84,mPoint1.y-66,mPaint2);
                    if (msg1!=null) {
                        canvas.drawText(msg1,mPoint1.x,mPoint1.y - 100 + 10,mTxPaint);
                    }
                    break;

                case LINE_TYPE_BOTTOM:
                    rectF = new RectF();
                    rectF.set(mPoint1.x-40,mPoint1.y+85,mPoint1.x+40,mPoint1.y+115);
                    canvas.drawRect(mPoint1.x-84,mPoint1.y+66,mPoint1.x+84,mPoint1.y+134,mPaint);
                    canvas.drawRect(mPoint1.x-84,mPoint1.y+66,mPoint1.x+84,mPoint1.y+134,mPaint2);
                    if (msg1!=null) {
                        canvas.drawText(msg1,mPoint1.x,mPoint1.y + 100 + 10,mTxPaint);
                    }
                    break;

                case LINE_TYPE_RIGHT:
                    rectF = new RectF();
                    rectF.set(mPoint1.x+60,mPoint1.y-15,mPoint1.x+140,mPoint1.y+15);
                    canvas.drawRect(mPoint1.x+16,mPoint1.y-34,mPoint1.x+184,mPoint1.y+34,mPaint);
                    canvas.drawRect(mPoint1.x+16,mPoint1.y-34,mPoint1.x+184,mPoint1.y+34,mPaint2);
                    if (msg1!=null) {
                        canvas.drawText(msg1,mPoint1.x + 100,mPoint1.y+10,mTxPaint);
                    }
                    break;

                case LINE_TYPE_LEFT:
                    rectF = new RectF();
                    rectF.set(mPoint1.x-140,mPoint1.y-15,mPoint1.x-60,mPoint1.y+15);
                    canvas.drawRect(mPoint1.x-184,mPoint1.y-34,mPoint1.x-16,mPoint1.y+34,mPaint);
                    canvas.drawRect(mPoint1.x-184,mPoint1.y-34,mPoint1.x-16,mPoint1.y+34,mPaint2);
                    if (msg1!=null) {
                        canvas.drawText(msg1,mPoint1.x - 100,mPoint1.y+10,mTxPaint);
                    }
                    break;

                default:
                    break;
            }
        }
    }
}
