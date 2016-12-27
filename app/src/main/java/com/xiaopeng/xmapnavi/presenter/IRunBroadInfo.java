package com.xiaopeng.xmapnavi.presenter;

import android.content.Intent;

/**
 * Created by xinlyun on 16-5-24.
 */
public interface IRunBroadInfo {
    void readIntentInfo(Intent intent);
    void OnResult(int rpcNum, int result);
}
