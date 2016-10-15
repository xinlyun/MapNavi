package com.xiaopeng.xmapnavi.view.appwidget.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.maps.MapView;
import com.xiaopeng.xmapnavi.view.appwidget.activity.ShowPosiActivity;

import java.util.Map;

/**
 * Created by linzx on 2016/10/15.
 */
public class ShowPosiFragment extends Fragment {

    private int[] markers = {com.xiaopeng.amaplib.R.drawable.poi_marker_1,
            com.xiaopeng.amaplib.R.drawable.poi_marker_2,
            com.xiaopeng.amaplib.R.drawable.poi_marker_3,
            com.xiaopeng.amaplib.R.drawable.poi_marker_4,
            com.xiaopeng.amaplib.R.drawable.poi_marker_5,
            com.xiaopeng.amaplib.R.drawable.poi_marker_6,
            com.xiaopeng.amaplib.R.drawable.poi_marker_7,
            com.xiaopeng.amaplib.R.drawable.poi_marker_8,
            com.xiaopeng.amaplib.R.drawable.poi_marker_9,
            com.xiaopeng.amaplib.R.drawable.poi_marker_10
    };

    private static final String TAG = "ShowPosiFragment";
    private static final String ACTION_SEARCH = "ACTION_SEARCH";
    private static final String ACTION_MSG = "ACTION_MSG";
    private static int  WINDOW_HEIGHT = 1440,
            LAYOUT_REL_HEIGHT = 600,
            TOUCH_HEIGHT = 800,
            DOWN_HEIGHT = 400,
            TITLE_HEIGHT = 100;


    private MapView mAmapView;

    public void setMapView(MapView mapView){
        mAmapView = mapView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }



}
