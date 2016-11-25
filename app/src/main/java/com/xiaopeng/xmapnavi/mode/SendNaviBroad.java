package com.xiaopeng.xmapnavi.mode;

import android.content.Context;
import android.content.Intent;

import com.amap.api.navi.model.NaviInfo;

/**
 * Created by linzx on 2016/11/22.
 */

public class SendNaviBroad implements ISendNaviBroad,GuideInfoExtraKey {
    private Context mContext;

    @Override
    public void initBroad(Context context) {
        mContext = context;

    }

    @Override
    public void sendNaviMsg(NaviInfo naviInfo) {
        Intent intent = new Intent();
        intent.setAction(ACTION_NAVI);
        intent.putExtra("KEY_TYPE",10001);
        intent.putExtra(ROUTE_REMAIN_DIS,naviInfo.getPathRetainDistance());
        intent.putExtra(ROUTE_REMAIN_TIME,naviInfo.getPathRetainTime());
        intent.putExtra(CUR_ROAD_NAME,naviInfo.getCurrentRoadName());
        intent.putExtra(NEXT_ROAD_NAME,naviInfo.getNextRoadName());
        int iconType = naviInfo.getIconType();
        intent.putExtra(ICON,iconType);
        intent.putExtra(SEG_REMAIN_DIS,naviInfo.getCurStepRetainDistance());
        intent.putExtra(CAMERA_TYPE,naviInfo.getCameraType());
        intent.putExtra(CAMERA_SPEED,naviInfo.getLimitSpeed());
        intent.putExtra(CAMERA_DIST,naviInfo.getCameraDistance());
        intent.putExtra(LIMITED_SPEED,naviInfo.getLimitSpeed());
        intent.putExtra(ROUND_ALL_NUM,0);
        intent.putExtra(ROUND_ABOUT_NUM,0);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void stopNavi() {
        Intent intent = new Intent();
        intent.setAction(ACTION_NAVI);
        intent.putExtra("KEY_TYPE",10019);
        intent.putExtra("EXTRA_STATE",9);
        mContext.sendBroadcast(intent);
    }


}
