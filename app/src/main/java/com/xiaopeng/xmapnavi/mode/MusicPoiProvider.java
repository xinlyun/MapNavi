package com.xiaopeng.xmapnavi.mode;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.amap.api.location.AMapLocation;
import com.xiaopeng.lib.utils.utils.LogUtils;
import com.xiaopeng.xmapnavi.presenter.IMusicPoiProvider;

/**
 * Created by linzx on 2016/12/28.
 */

public class MusicPoiProvider implements IMusicPoiProvider {
    private int time = 0;
    private Context mContext;
    @Override
    public void init(Context context) {
        mContext = context;
    }

    @Override
    public void sendProvide(AMapLocation aMapLocation) {
        if (time<8 && aMapLocation != null && mContext!=null){
            LogUtils.d("MusicPoiProvider","sendProvide");
            time++;
            Intent intent = new Intent();
            intent.setAction(ACTION_BROADCAST_LOCATION_PROVINCE);
            Bundle bundle = new Bundle();
            bundle.putString(LOCATION_PROVINCE_KEY,aMapLocation.getProvince());
            bundle.putString(LOCATION_CITY_KEY,aMapLocation.getCity());
            intent.putExtras(bundle);
            mContext.sendBroadcast(intent);
        }
    }
}
