package com.xiaopeng.xmapnavi.mode;

import android.os.Handler;
import android.os.Message;

import com.activeandroid.query.Select;
import com.amap.api.services.core.PoiItem;
import com.xiaopeng.xmapnavi.bean.HisItem;
import com.xiaopeng.xmapnavi.presenter.IHistoryDateHelper;
import com.xiaopeng.xmapnavi.presenter.callback.XpHisDateListner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linzx on 2016/10/17.
 */
public class DateHelper implements IHistoryDateHelper {
    public static final int TYPE_NAME = 0,TYPE_POSI = 1,TYPE_WAY = 2 ;
    private XpHisDateListner mHistoryListner;
    private static final long LENGTHST_TIME = 604800000; // 7 * 24 * 60 * 60 *1000 一周时间

    @Override
    public void savePosiStr(String str) {
        new SaveThread(str,null,null).start();
    }

    @Override
    public void savePoiItem(PoiItem item) {
        new SaveThread(null,item,null).start();
    }

    @Override
    public void updateHisItem(HisItem hisItem) {
        hisItem.time = System.currentTimeMillis();
        hisItem.save();
    }

    @Override
    public void saveGoAway(String path) {
        new SaveThread(null,null,path).start();
    }

    @Override
    public void getHisItem(int count) {
        new ReadHistory(count).start();
    }

    @Override
    public void setHisDateListner(XpHisDateListner listner) {
        mHistoryListner = listner;
    }

    @Override
    public void clearDate() {
        new ClearHistory().start();
    }

    class SaveThread extends Thread{
        String msg,path;
        PoiItem poiItem;
        private SaveThread(String msg,PoiItem poiItem,String path){
            this.msg  = msg;
            this.poiItem = poiItem;
            this.path = path;
        }

        @Override
        public void run() {
            super.run();
            HisItem hisItem = new HisItem();
            hisItem.time = System.currentTimeMillis();
            if (msg != null){
                hisItem.type = TYPE_NAME;
                hisItem.msg = msg;
                hisItem.save();
                return;
            }else if (poiItem != null){
                hisItem.type = TYPE_POSI;
                hisItem.posiName = poiItem.toString();
                hisItem.posiArt = poiItem.getCityName() + " " + poiItem.getSnippet();
                hisItem.posiLat = (float) poiItem.getLatLonPoint().getLatitude();
                hisItem.posiLon = (float) poiItem.getLatLonPoint().getLongitude();
                hisItem.save();
                return;
            }
            hisItem.type = TYPE_WAY;
            hisItem.posiName = path;
            hisItem.msg = "从 我的位置 --> "+path;
            hisItem.save();
        }
    }
    Handler callBackHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            List<HisItem> hisItems = (List<HisItem>) msg.obj;
            if (mHistoryListner != null){
                mHistoryListner.onHistoryDate(hisItems);
            }
        }
    };

    class ClearHistory extends Thread{

        @Override
        public void run() {
            super.run();
            List<HisItem> hisItems = getHistoryMsg();
            for (HisItem hisItem : hisItems){
                hisItem.delete();
            }
            Message msg = callBackHandler.obtainMessage();
            msg.obj = null;
            callBackHandler.sendMessage(msg);
        }
    }

    class ReadHistory extends Thread{
        int count ;
        ReadHistory(int count){
            this.count = count;
        }
        @Override
        public void run() {
            super.run();
            List<HisItem> hisItems = getHistoryMsg();
            List<HisItem> rebackItems = new ArrayList<>();
            for (int i = 0;i<hisItems.size();i++){
                HisItem hisItem = hisItems.get(i);
                if ((System.currentTimeMillis() - hisItem.time) > LENGTHST_TIME){
                    hisItem.delete();
                }else {
                    if (i < count) {
                        rebackItems.add(hisItem);
                    }
                }
            }
            Message msg = callBackHandler.obtainMessage();
            msg.obj = rebackItems;
            callBackHandler.sendMessage(msg);

        }
    }

    public static List<HisItem> getHistoryMsg(){
        return new Select()
                .from(HisItem.class)
                .orderBy("time desc")
                .execute();
    }
}
