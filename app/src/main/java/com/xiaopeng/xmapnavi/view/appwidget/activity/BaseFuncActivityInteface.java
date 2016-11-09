package com.xiaopeng.xmapnavi.view.appwidget.activity;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Created by linzx on 2016/11/9.
 */
public interface BaseFuncActivityInteface {
    void exitFragment();
    void shouldFinish();
    void startAcitivity(Class<?> cls, Bundle bundle);
    void startFragment(Fragment fragment);
    void startFragment(Class<?> cls);
}
