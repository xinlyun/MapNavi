package com.xiaopeng.xmapnavi.presenter;

import android.content.Context;

import com.xiaopeng.xmapnavi.presenter.callback.XpLicProListener;

/**
 * Created by linzx on 2017/1/4.
 */

public interface IWeixinLicenceProvider {

    void init(Context context);
    void setLicProListener(XpLicProListener licProListener);
    void getLic();
}
