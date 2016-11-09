package com.xiaopeng.xmapnavi.presenter.collect;

import com.xiaopeng.xmapnavi.bean.CollectItem;
import com.xiaopeng.xmapnavi.presenter.callback.XpCollectListener;

/**
 * Created by linzx on 2016/11/8.
 */
public interface IShowCollectPresenter {
    void init();
    void release();
    void getCollect();
    void setOnCollectListener(XpCollectListener mListener);
    boolean startNavi(CollectItem item);
}
