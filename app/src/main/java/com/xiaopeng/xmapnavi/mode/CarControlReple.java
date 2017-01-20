package com.xiaopeng.xmapnavi.mode;

import android.content.Context;

import com.xiaopeng.lib.scu.AbsCarControlCallback;
import com.xiaopeng.lib.scu.ScuMailboxes;
import com.xiaopeng.lib.scu.msg.CAN336;
import com.xiaopeng.xmapnavi.XpApplication;
import com.xiaopeng.xmapnavi.presenter.ICarControlReple;
import com.xiaopeng.xmapnavi.presenter.callback.XpCarMsgListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linzx on 2017/1/4.
 */

public class CarControlReple implements ICarControlReple {
    Context mContext;
    private List<XpCarMsgListener> xpCarMsgListeners = new ArrayList<>();
    ScuMailboxes mScuMailboxes;
    private int nowPowerTral = 0;
    private static int[] msgs = new int[]{
            0x336
    };
    @Override
    public void init(Context context) {
        mContext = context;
        mScuMailboxes = XpApplication.sApplication.getScuMailboxes();
        mScuMailboxes.getCarControlBox(mCarControlCallback,msgs);
    }

    @Override
    public void addXpCarMsgListener(XpCarMsgListener listener) {
        xpCarMsgListeners.add(listener);
    }

    @Override
    public void removeXpCarMsgListener(XpCarMsgListener listener) {
        xpCarMsgListeners.remove(listener);
    }

    @Override
    public int getCarTralLenght() {
        return nowPowerTral;
    }

    private AbsCarControlCallback mCarControlCallback = new AbsCarControlCallback() {
        @Override
        protected void onReportMsg(String data) {
            super.onReportMsg(data);

        }

        @Override
        protected void onMsg336(CAN336 msg) {
            super.onMsg336(msg);
            nowPowerTral = msg.VCU_dstBat_Dsp ;
            for (XpCarMsgListener xpCarMsgListener : xpCarMsgListeners ){
                try {
                    xpCarMsgListener.carTrayLenght(nowPowerTral);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    };



}
