package com.xiaopeng.xmapnavi.mode;

import android.content.Context;
import android.view.View;

import com.amap.api.navi.model.AMapNaviPath;
import com.xiaopeng.xmapnavi.presenter.INaviViewProvide;

import java.util.HashMap;

/**
 * Created by linzx on 2016/10/15.
 */
public class NaviViewProvide implements INaviViewProvide{
    private Context mContext;
    public NaviViewProvide(Context context){
        mContext = context;
    }

    @Override
    public HashMap<Integer, View> createViewByPath(HashMap<Integer, AMapNaviPath> pathDates) {
        //TODO
        
        return null;
    }
}
