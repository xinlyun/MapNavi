package com.xiaopeng.xmapnavi.presenter;

import android.content.Context;

import com.xiaopeng.xmapnavi.bean.PowerPoint;
import com.xiaopeng.xmapnavi.bean.StubAc;

import java.util.List;

/**
 * Created by linzx on 2016/12/28.
 */

public interface IStubGroupProvider {

    void init(Context context);
    void getStubGroupByPoi(double lat,double lon);
    void getStubGroupByCity(String city);
    void setOnStubDataListener(OnStubData onStubDataListener);


    public interface OnStubData{
        void stubProvide(List<PowerPoint> stubAcs);
    }
}
