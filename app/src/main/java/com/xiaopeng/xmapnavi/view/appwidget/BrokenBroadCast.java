package com.xiaopeng.xmapnavi.view.appwidget;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import com.xiaopeng.lib.utils.utils.LogUtils;

import com.xiaopeng.xmapnavi.view.appwidget.services.LocationProService;

/**
 * Created by xinlyun on 16-5-25.
 */
public class BrokenBroadCast extends BroadcastReceiver {

    private static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    //远程绑定广播
    public static final String REMOTE_CONTROL_BAND = "com.xiaopeng.action.REMOTE_CONTROL_BAND";
    private String musicName = "1122", artist = "234";

    @Override
    public void onReceive(Context context, Intent intent) {

        LogUtils.d("SearchMusic", "Receive" + ACTION);
        if (intent.getAction().equals(ACTION)) {
//            Intent intent1=new Intent();
//            intent1.setAction(NaviProService.class.getName());
//            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startService(intent1);
            Intent i1 = new Intent(Intent.ACTION_RUN);
            i1.setClass(context, LocationProService.class);
            context.startService(i1);
            Intent intent1 = new Intent(context,LocationProService.class);
            context.startService(intent1);
        }else if (intent.getAction().equals(REMOTE_CONTROL_BAND)){

        }
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
}
