package com.xiaopeng.xmapnavi.presenter;

import android.content.Intent;

/**
 * Created by linzx on 2016/12/24.
 */

public interface ILocationForICU {

    void beginToLocation(ILocationProvider locationProvider);
    void readIntentInfo(Intent intent);
    void setConnectState(boolean isConnect);
}
