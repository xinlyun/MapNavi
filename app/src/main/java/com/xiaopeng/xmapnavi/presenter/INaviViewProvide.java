package com.xiaopeng.xmapnavi.presenter;

import android.view.View;

import com.amap.api.navi.model.AMapNaviPath;

import java.util.HashMap;
import java.util.List;

/**
 * Created by linzx on 2016/10/15.
 */
public interface INaviViewProvide {
    HashMap<Integer,View> createViewByPath(HashMap<Integer, AMapNaviPath> pathDates);
}
