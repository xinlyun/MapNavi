package com.xiaopeng.xmapnavi;

import android.app.Application;

import com.activeandroid.ActiveAndroid;
import com.amap.api.navi.AMapNavi;
import com.xiaopeng.lib.bughunter.BugHunter;
import com.xiaopeng.xmapnavi.bean.LocationSaver;

/**
 * Created by linzx on 2016/10/12.
 */
public class XpApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AMapNavi.setApiKey(this,"518079e13164d2910ff81c078e073bcd");
        ActiveAndroid.initialize(this);
        BugHunter.init(this);
        if (LocationSaver.getSaveLocation()==null){
            LocationSaver.saverFirst();
        }
    }
}
