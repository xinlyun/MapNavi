package com.xiaopeng.xmapnavi.presenter.callback;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

/**
 * Created by linzx on 2016/10/28.
 */
public interface XpSensorListener {
    void onSensorChanged(SensorEvent event);
    void onAccuracyChanged(Sensor sensor, int accuracy) ;
}
