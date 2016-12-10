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
import com.amap.api.navi.model.AMapTrafficStatus;
import com.xiaopeng.xmapnavi.R;

import java.util.List;

public class MTrafficBarView2 extends ImageView {
    private int left;
    private int right;
    private int progressBarHeight;
    private Paint paint;
    private Bitmap displayingBitmap;
    private Bitmap tmcBarBitmapPortrait;
    private Bitmap tmcBarBitmapLandscape;
    private List<AMapTrafficStatus> mTmcSections;
    private int tmcBarTopMargin = 30;
    private Bitmap rawBitmap;
    private int totalDis = 0;
    private RectF colorRectF;
    private int drawTmcBarBgX;
    private int drawTmcBarBgY;
    private int tmcBarBgWidth;
    private int tmcBarBgHeight = 0;
    private int unknownTrafficColor;
    private int smoothTrafficColor;
    private int slowTrafficColor;
    private int jamTrafficColor;
    private int veryJamTrafficColor;

    public MTrafficBarView2(Context var1, AttributeSet var2, int var3) {
        super(var1, var2, var3);
        this.initResource();
    }

    public MTrafficBarView2(Context var1, AttributeSet var2) {
        super(var1, var2);
        this.initResource();
    }

    public MTrafficBarView2(Context var1) {
        super(var1);
        this.initResource();
    }

    public Bitmap getDisplayingBitmap() {
        return this.displayingBitmap;
    }

    private void initResource() {

        this.rawBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_light_show);
        this.tmcBarBitmapPortrait = this.rawBitmap;
        this.left = this.tmcBarBitmapPortrait.getWidth() * 28 / 100;
        this.right = this.tmcBarBitmapPortrait.getWidth() * 72 / 100;

        this.progressBarHeight = (int)((double)this.tmcBarBitmapPortrait.getHeight() * 0.893D);

        this.tmcBarBgWidth = this.tmcBarBitmapPortrait.getWidth();

        this.tmcBarBgHeight = this.tmcBarBitmapPortrait.getHeight();
        this.paint = new Paint();
        if(VERSION.SDK_INT >= 11) {
            this.tmcBarTopMargin = Math.abs(this.progressBarHeight - this.tmcBarBitmapPortrait.getHeight()) / 4 - (int)((double)this.progressBarHeight * 0.010D);
        } else {
            this.tmcBarTopMargin = Math.abs(this.progressBarHeight - this.tmcBarBitmapPortrait.getHeight()) / 4 - 3;
        }

        this.setTmcBarHeightWhenLandscape(0.6666666666666666D);
        this.displayingBitmap = this.tmcBarBitmapPortrait;
        this.colorRectF = new RectF();
        this.unknownTrafficColor = Color.parseColor("#4f9bfe");
        this.smoothTrafficColor = Color.parseColor("#0bd64e");
        this.slowTrafficColor = Color.parseColor("#ffcc00");
        this.jamTrafficColor = Color.argb(255, 244,77,55);
        this.veryJamTrafficColor = Color.argb(255, 179, 17, 15);
    }

    public void onConfigurationChanged(boolean var1) {
        if(var1) {
            this.displayingBitmap = this.tmcBarBitmapLandscape;
        } else {
            this.displayingBitmap = this.tmcBarBitmapPortrait;
        }

        this.setProgressBarSize(var1);
    }

    private void setProgressBarSize(boolean var1) {
        this.progressBarHeight = (int)((double)this.displayingBitmap.getHeight() * 0.8D);
        this.tmcBarBgWidth = this.displayingBitmap.getWidth();
        this.tmcBarBgHeight = this.displayingBitmap.getHeight();
        if(!var1) {
            if(VERSION.SDK_INT >= 11) {
                this.tmcBarTopMargin = Math.abs(this.progressBarHeight - this.displayingBitmap.getHeight()) / 4 - (int)((double)this.progressBarHeight * 0.017D);
            } else {
                this.tmcBarTopMargin = Math.abs(this.progressBarHeight - this.displayingBitmap.getHeight()) / 4 - 4;
            }
        } else {
            this.tmcBarTopMargin = Math.abs(this.progressBarHeight - this.displayingBitmap.getHeight()) / 4 - (int)((double)this.progressBarHeight * 0.017D);
        }

    }

    public void update(List<AMapTrafficStatus> var1, int var2) {
        this.mTmcSections = var1;
        this.totalDis = var2;
        Bitmap var3 = this.produceFinalBitmap();
        if(var3 != null) {
            this.setImageBitmap(this.produceFinalBitmap());
        }

    }

    public int getUnknownTrafficColor() {
        return this.unknownTrafficColor;
    }

    public void setUnknownTrafficColor(int var1) {
        this.unknownTrafficColor = var1;
    }

    public int getSmoothTrafficColor() {
        return this.smoothTrafficColor;
    }

    public void setSmoothTrafficColor(int var1) {
        this.smoothTrafficColor = var1;
    }

    public int getSlowTrafficColor() {
        return this.slowTrafficColor;
    }

    public void setSlowTrafficColor(int var1) {
        this.slowTrafficColor = var1;
    }

    public int getJamTrafficColor() {
        return this.jamTrafficColor;
    }

    public void setJamTrafficColor(int var1) {
        this.jamTrafficColor = var1;
    }

    public int getVeryJamTrafficColor() {
        return this.veryJamTrafficColor;
    }

    public void setVeryJamTrafficColor(int var1) {
        this.veryJamTrafficColor = var1;
    }

    Bitmap produceFinalBitmap() {
        if(this.mTmcSections == null) {
            return null;
        } else {
            Bitmap var1 = Bitmap.createBitmap(this.displayingBitmap.getWidth(), this.displayingBitmap.getHeight(), Config.ARGB_8888);
            Canvas var2 = new Canvas(var1);
            this.paint.setStyle(Style.FILL);
            float var3 = (float)this.totalDis;

            for(int var4 = 0; var4 < this.mTmcSections.size(); ++var4) {
                AMapTrafficStatus var5 = (AMapTrafficStatus)this.mTmcSections.get(var4);
                switch(var5.getStatus()) {
                    case 0:
                        this.paint.setColor(this.unknownTrafficColor);
                        break;
                    case 1:
                        this.paint.setColor(this.smoothTrafficColor);
                        break;
                    case 2:
                        this.paint.setColor(this.slowTrafficColor);
                        break;
                    case 3:
                        this.paint.setColor(this.jamTrafficColor);
                        break;
                    case 4:
                        this.paint.setColor(this.veryJamTrafficColor);
                        break;
                    default:
                        this.paint.setColor(this.unknownTrafficColor);
                }

                if(var3 - (float)var5.getLength() > 0.0F) {
                    this.colorRectF.set((float)this.left, (float)this.progressBarHeight * (var3 - (float)var5.getLength()) / (float)this.totalDis + (float)this.tmcBarTopMargin, (float)this.right, (float)this.progressBarHeight * var3 / (float)this.totalDis + (float)this.tmcBarTopMargin);
                } else {
                    this.colorRectF.set((float)this.left, (float)this.tmcBarTopMargin, (float)this.right, (float)this.progressBarHeight * var3 / (float)this.totalDis + (float)this.tmcBarTopMargin);
                }

                if(var4 == this.mTmcSections.size() - 1) {
                    this.colorRectF.set((float)this.left, (float)this.tmcBarTopMargin, (float)this.right, (float)this.progressBarHeight * var3 / (float)this.totalDis + (float)this.tmcBarTopMargin);
                }

                var2.drawRect(this.colorRectF, this.paint);
                var3 -= (float)var5.getLength();
            }

            this.paint.setColor(-65536);
            var2.drawBitmap(this.displayingBitmap, 0.0F, 0.0F, (Paint)null);
            return var1;
        }
    }

    public void setTmcBarPosition(int var1, int var2, int var3, int var4, boolean var5) {
        this.setTmcBarHeightWhenLandscape(0.6666666666666666D * (double)var2 / (double)var3);
        this.setTmcBarHeightWhenPortrait(1.0D * (double)var2 / (double)var3);
        var4 = var4 * var2 / var3;
        this.onConfigurationChanged(var5);
        if(var5) {
            this.drawTmcBarBgX = Math.abs(var1 - (int)(1.3D * (double)this.tmcBarBgWidth));
            this.drawTmcBarBgY = (var2 - this.tmcBarBgHeight / 2) * 6 / 10;
        } else {
            this.drawTmcBarBgX = Math.abs(var1 - (int)(1.3D * (double)this.tmcBarBgWidth));
            this.drawTmcBarBgY = (int)((double)var2 - 1.5D * (double)var4 - (double)this.tmcBarBgHeight);
        }

    }

    public void setTmcBarHeightWhenLandscape(double var1) {
        if(var1 > 1.0D) {
            var1 = 1.0D;
        } else if(var1 < 0.1D) {
            var1 = 0.1D;
        }

        this.tmcBarBitmapLandscape = Bitmap.createScaledBitmap(this.rawBitmap, this.rawBitmap.getWidth(), (int)((double)this.rawBitmap.getHeight() * var1), true);
    }

    public void setTmcBarHeightWhenPortrait(double var1) {
        if(var1 > 1.0D) {
            var1 = 1.0D;
        } else if(var1 < 0.1D) {
            var1 = 0.1D;
        }

        this.tmcBarBitmapPortrait = Bitmap.createScaledBitmap(this.rawBitmap, this.rawBitmap.getWidth(), (int)((double)this.rawBitmap.getHeight() * var1), true);
        this.displayingBitmap = this.tmcBarBitmapPortrait;
        this.setProgressBarSize(false);
    }

    public int getTmcBarBgPosX() {
        return this.drawTmcBarBgX;
    }

    public int getTmcBarBgPosY() {
        return this.drawTmcBarBgY;
    }

    public int getTmcBarBgWidth() {
        return this.tmcBarBgWidth;
    }

    public int getTmcBarBgHeight() {
        return this.tmcBarBgHeight;
    }

    public void recycleResource() {
        if(this.displayingBitmap != null) {
            this.displayingBitmap.recycle();
            this.displayingBitmap = null;
        }

        if(this.tmcBarBitmapPortrait != null) {
            this.tmcBarBitmapPortrait.recycle();
            this.tmcBarBitmapPortrait = null;
        }

        if(this.tmcBarBitmapLandscape != null) {
            this.tmcBarBitmapLandscape.recycle();
            this.tmcBarBitmapLandscape = null;
        }

    }
}
