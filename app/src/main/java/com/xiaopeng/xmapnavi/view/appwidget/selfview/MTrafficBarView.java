//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.xiaopeng.xmapnavi.view.appwidget.selfview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.amap.api.col.ee;
import com.amap.api.navi.model.AMapTrafficStatus;
import com.amap.api.navi.view.TrafficBarView;
import com.xiaopeng.xmapnavi.R;

import java.util.List;

public class MTrafficBarView extends TrafficBarView {
    private XpTrafficBarListener mListener;
    public MTrafficBarView(Context var1, AttributeSet var2, int var3) {
        super(var1, var2, var3);
    }

    public MTrafficBarView(Context var1, AttributeSet var2) {
        super(var1, var2);
    }

    public MTrafficBarView(Context var1) {
        super(var1);
    }

    @Override
    public void update(List<AMapTrafficStatus> list, int i) {
        super.update(list, i);
        if (mListener!=null){
            mListener.trafficUpdate(list,i);
        }
    }

    public void setTrafficListener(XpTrafficBarListener listener){
        mListener = listener;
    }

    public interface XpTrafficBarListener{
        void trafficUpdate(List<AMapTrafficStatus> list, int i);
    }

}
