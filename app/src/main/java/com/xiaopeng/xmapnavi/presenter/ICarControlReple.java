package com.xiaopeng.xmapnavi.presenter;

import android.content.Context;

import com.xiaopeng.xmapnavi.presenter.callback.XpCarMsgListener;

/**
 * Created by linzx on 2017/1/4.
 */

public interface ICarControlReple {
    void init(Context context);

    void addXpCarMsgListener(XpCarMsgListener listener);

    void removeXpCarMsgListener(XpCarMsgListener listener);

    int getCarTralLenght();

}
