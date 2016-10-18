package com.xiaopeng.xmapnavi.view.appwidget.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;

/**
 * Created by linzx on 2016/10/12.
 */
public class LocationProService extends Service {
    ILocationProvider mLocationProvider;
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
    }
    Handler deleyToInit = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mLocationProvider  = LocationProvider.getInstence(LocationProService.this);
        }
    };
}
