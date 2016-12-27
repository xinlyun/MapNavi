package com.xiaopeng.xmapnavi.view.appwidget.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.xiaopeng.lib.scu.AbsNcmControlCallback;
import com.xiaopeng.lib.scu.NcmControlBox;
import com.xiaopeng.lib.scu.ScuMailboxes;
import com.xiaopeng.lib.utils.utils.LogUtils;
import com.xiaopeng.xmapnavi.XpApplication;
import com.xiaopeng.xmapnavi.mode.LocationForICU;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.mode.SoloNaviInfo;
import com.xiaopeng.xmapnavi.presenter.ILocationForICU;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.IRunBroadInfo;

/**
 * Created by linzx on 2016/10/12.
 */
public class LocationProService extends Service {
    ILocationProvider mLocationProvider;
    private static final String TAG  = "LocationProService";
    private ScuMailboxes mScuMailboxes = null;
    private NcmControlBox mNcmControlBox;
    String ACTION_NAVI = "AUTONAVI_STANDARD_BROADCAST_SEND";
    String ACTION_MUSIC = "com.music.broadcast.ACTION_MUSIC";
    String ACTION_RADIO = "com.radio.broadcast";
    String ACTION_BT = "com.xiaopeng.btphone.changeinfo";
    //    String ACTION_BT = "com.bluetooth.broadcast";
    String ACTION_WEATHER = "com.weather.broadcast";
    public static final String ACTION_NAVI2 = "com.xiaopeng.navi.broadcast";
    private final static String ACTION0_DOWN_COMPLETE = "com.xiaopeng.searchmusic.IMGDOWN";

    public IRunBroadInfo naviInfo;
    public ILocationForICU mLocationICU;
    private IBinder localBind ;
    private boolean isConnect =true;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        LocationProvider.init(this);
        deleyToInit.sendEmptyMessageDelayed(0,1000);
        try {
            mScuMailboxes = XpApplication.sApplication.getScuMailboxes();
            mNcmControlBox = mScuMailboxes.getNcmControlBox(mNcmControlCallback);
            naviInfo = new SoloNaviInfo(this, mNcmControlBox);
        }catch (Exception e){
            e.printStackTrace();
        }

//        localBind = new LocalBinder();
    }
    Handler deleyToInit = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            mLocationProvider  = LocationProvider.getInstence(LocationProService.this);
            try {
                initBroadCast();
                mLocationICU = new LocationForICU(LocationProService.this, mNcmControlBox);
                mLocationICU.beginToLocation(mLocationProvider);
                mLocationICU.setConnectState(isConnect);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };


    private AbsNcmControlCallback mNcmControlCallback = new AbsNcmControlCallback()
    {
        protected void OnNavResult(int rpcNum, int result) {
            try {
                naviInfo.OnResult(rpcNum, result);
                LogUtils.d(TAG, "OnNavResult   rpcNum=" + rpcNum + ", result=" + result);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        protected void OnMusicResult(int rpcNum, int result) {
            LogUtils.d(TAG, "OnMusicResult   rpcNum=" + rpcNum + ", result="+ result);}
        protected void OnRadioResult(int rpcNum, int result) {
            LogUtils.d(TAG, "OnRadioResult   rpcNum=" + rpcNum + ", result="+ result);}
        protected void OnBluetoothResult(int rpcNum, int result) {

            LogUtils.d(TAG, "OnBluetoothResult   rpcNum=" + rpcNum + ", result="+ result);}
        protected void OnWeaterResult(int rpcNum, int result) {
            LogUtils.d(TAG, "OnWeaterResult   rpcNum=" + rpcNum + ", result="+ result);}
        protected void OnAccountResult(int rpcNum, int result) {
            LogUtils.d(TAG, "OnAccountResult  rpcNum=" + rpcNum + ", result="+ result);}



        @Override
        protected void OnConnectStatus(boolean isConnect) {
            super.OnConnectStatus(isConnect);
            LocationProService.this.isConnect = isConnect;
            if (mLocationICU!=null){
                mLocationICU.setConnectState(isConnect);
            }
        }
    };

    private void initBroadCast(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_NAVI);
        intentFilter.addAction(ACTION_NAVI2);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
//            LogUtils.d("NaviProService","onReceive:Action="+action);
            if (action.equals(ACTION_NAVI2)) {
                try {
                    mLocationICU.readIntentInfo(intent);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if(!isConnect)return;
            try {
                if (action.equals(ACTION_NAVI)) {
                    naviInfo.readIntentInfo(intent);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

}
