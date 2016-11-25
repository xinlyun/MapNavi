package com.xiaopeng.xmapnavi.mode;

import android.os.Handler;
import android.os.Message;

import com.activeandroid.query.Select;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.route.RouteSearch;
import com.xiaopeng.lib.utils.utils.LogUtils;
import com.xiaopeng.xmapnavi.bean.CollectItem;
import com.xiaopeng.xmapnavi.bean.HisItem;
import com.xiaopeng.xmapnavi.bean.WherePoi;
import com.xiaopeng.xmapnavi.presenter.ICollectDateHelper;
import com.xiaopeng.xmapnavi.presenter.IHistoryDateHelper;
import com.xiaopeng.xmapnavi.presenter.IWhereDateHelper;
import com.xiaopeng.xmapnavi.presenter.callback.XpCollectListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpHisDateListner;
import com.xiaopeng.xmapnavi.presenter.callback.XpWhereListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linzx on 2016/10/17.
 */
public class DateHelper implements IHistoryDateHelper ,ICollectDateHelper,IWhereDateHelper {
    private static final int HIS_BACK = 0;
    private static final int COLLECT_BACK = 1;
    private static final int WHERE_BACK = 2;

    public static final int TYPE_NAME = 0,TYPE_POSI = 1,TYPE_WAY = 2 ;
    private XpHisDateListner mHistoryListner;
    private XpCollectListener mCollectListener;
    private XpWhereListener mWhereListener;
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

    @Override
    public void setOnCollectListener(XpCollectListener listener) {
        mCollectListener = listener;
    }

    @Override
    public void getCollectItems() {
        new ReadCollectDate().start();
    }

    @Override
    public void saveCollect(String name, String desc, double poiLat, double poiLon) {
        new SaveCollectThread(name,desc,poiLat,poiLon).start();
    }

    @Override
    public void setOnWhereListener(XpWhereListener listener) {
        mWhereListener = listener;
    }

    @Override
    public void getWhereItems() {
        new ReadWhereDate().start();
    }

    @Override
    public void saveWhereIten(int type, String name, String desc, double poiLat, double poiLon) {
        new SaveWhereThread(type,name,desc,poiLat,poiLon).start();
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
    class SaveCollectThread extends  Thread{
        private String name,desc;
        private Double poiLat,poiLon;
        SaveCollectThread(String name,String desc,double poiLat,double poiLon){
            this.name =  name;
            this.desc = desc;
            this.poiLat = poiLat;
            this.poiLon = poiLon;
        }

        @Override
        public void run() {
            super.run();
            LogUtils.d("Date","SaveCollect");
            CollectItem collectItem = new CollectItem();
            collectItem.pName = name;
            collectItem.pDesc = desc;
            collectItem.posLat = poiLat;
            collectItem.posLon = poiLon;
            collectItem.time = System.currentTimeMillis();
            collectItem.save();
        }
    }

    class SaveWhereThread extends Thread{
        private String name,desc;
        private Double poiLat,poiLon;
        private int type;

        private SaveWhereThread(int type,String name,String desc,double poiLat,double poiLon){
            this.name =  name;
            this.desc = desc;
            this.poiLat = poiLat;
            this.poiLon = poiLon;
            this.type = type;
        }

        @Override
        public void run() {
            super.run();
            LogUtils.d("Date","SaveCollect");
            WherePoi whereItem = new WherePoi();
            whereItem.pName = name;
            whereItem.pDesc = desc;
            whereItem.posLat = poiLat;
            whereItem.posLon = poiLon;
            whereItem.type = type;
            whereItem.time = System.currentTimeMillis();
            whereItem.save();
        }
    }

    Handler callBackHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HIS_BACK: {
                    List<HisItem> hisItems = (List<HisItem>) msg.obj;
                    if (mHistoryListner != null) {
                        mHistoryListner.onHistoryDate(hisItems);
                    }
                }
                break;

                case COLLECT_BACK:
                    List<CollectItem> collectItems = (List<CollectItem>) msg.obj;
                    if (mCollectListener!=null){
                        mCollectListener.onCollectCallBack(collectItems);
                    }
                    break;

                case WHERE_BACK:
                    List<WherePoi> wherePois = (List<WherePoi>) msg.obj;
                    if (mWhereListener!=null){
                        mWhereListener.onWhereCallBack(wherePois);
                    }
                    break;

                default:
                    break;

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
            msg.what = HIS_BACK;
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
            msg.what = HIS_BACK;
            msg.obj = rebackItems;
            callBackHandler.sendMessage(msg);

        }
    }

    class ReadCollectDate extends Thread{
        @Override
        public void run() {
            super.run();
            List<CollectItem> list = getCollectData();
            Message msg  = callBackHandler.obtainMessage();
            msg.what = COLLECT_BACK;
            msg.obj = list;
            callBackHandler.sendMessage(msg);
        }
    }

    class ReadWhereDate extends Thread{
        @Override
        public void run() {
            super.run();
            List<WherePoi> list = getWhereMsg();
            Message msg  = callBackHandler.obtainMessage();
            msg.what = WHERE_BACK;
            msg.obj = list;
            callBackHandler.sendMessage(msg);
        }
    }


    public static List<HisItem> getHistoryMsg(){
        return new Select()
                .from(HisItem.class)
                .orderBy("time desc")
                .execute();
    }

    public static List<CollectItem> getCollectData(){
        return new Select()
                .from(CollectItem.class)
                .orderBy("time desc")
                .execute();
    }
    public static List<WherePoi> getWhereMsg(){
        return new Select()
                .from(WherePoi.class)
                .orderBy("time desc")
                .execute();
    }
}
