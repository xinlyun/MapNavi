package com.xiaopeng.xmapnavi.presenter.callback;

import com.xiaopeng.xmapnavi.bean.CollectItem;
import com.xiaopeng.xmapnavi.bean.WherePoi;

import java.util.List;

/**
 * Created by linzx on 2016/11/18.
 */

public interface XpWhereListener {
    void onWhereCallBack(List<WherePoi> wherePois);
}
