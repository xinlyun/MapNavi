package com.xiaopeng.xmapnavi.presenter.callback;

import android.support.annotation.NonNull;

import com.aispeech.aios.common.bean.PoiBean;

/**
 * Created by linzx on 2016/12/9.
 */

public interface XpAiosMapListener {
    void onStartNavi(@NonNull String s, @NonNull PoiBean poiBean) ;

    void onStartNavi(double lat,double lon);

    void onCancelNavi(@NonNull String s);

    void onOverview(@NonNull String s);


    void onRoutePlanning(@NonNull String s, @NonNull String s1);


    void onZoom(@NonNull String s, int i) ;


    void onLocate(@NonNull String s);
}
