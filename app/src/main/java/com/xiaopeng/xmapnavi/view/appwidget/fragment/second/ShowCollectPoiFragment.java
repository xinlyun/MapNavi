package com.xiaopeng.xmapnavi.view.appwidget.fragment.second;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.navi.model.NaviLatLng;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.bean.CollectItem;
import com.xiaopeng.xmapnavi.mode.DateHelper;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.view.appwidget.activity.BaseFuncActivityInteface;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by linzx on 2016/12/3.
 */

public class ShowCollectPoiFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = "ShowCollectPoiFragment";
    private View rootView;
    private ILocationProvider mLocation;
    private LatLng mLatlng;
    private CollectItem mCollectPoiItem;
    private BaseFuncActivityInteface mActivity;
    private MapView mMapView;
    private AMap mAmap;

    private TextView mTxName,mTxDesc,mTxDis;
    private Button mBtnExit,mBtnLove,mBtnNavi;
    private Marker mMarkerPoi;
    LatLng latLng ;
    private String poiName,poiDesc;
    private DateHelper dateHelper;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocation = LocationProvider.getInstence(getActivity());
        mLatlng = new LatLng(mLocation.getAmapLocation().getLatitude(),mLocation.getAmapLocation().getLongitude());
        mActivity = (BaseFuncActivityInteface) getActivity();
        mMapView = mActivity.getMapView();
        mAmap = mMapView.getMap();
        dateHelper = new DateHelper();
    }

    public void setCollectItem(CollectItem item){
        mCollectPoiItem = item;
        latLng = new LatLng(mCollectPoiItem.posLat,mCollectPoiItem.posLon);
        poiName = item.pName;
        poiDesc = item.pDesc;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_show_collect_poi,container,false);
        initView();
        return rootView;
    }


    private View findViewById(int id){
        return rootView.findViewById(id);
    }


    private void initView(){
        //TODO
        mTxName         = (TextView) findViewById(R.id.tv_poi_name);
        mTxDesc         = (TextView) findViewById(R.id.tv_poi_str);
        mTxDis          = (TextView) findViewById(R.id.tv_poi_dis);
        mBtnExit        = (Button) findViewById(R.id.btn_exit_show);
        mBtnLove        = (Button) findViewById(R.id.btn_collect);
        mBtnNavi        = (Button) findViewById(R.id.btn_begin_navi);

        mBtnExit        .setOnClickListener(this);
        mBtnLove        .setOnClickListener(this);
        mBtnNavi        .setOnClickListener(this);

    }

    @Override
    public void onStart() {
        super.onStart();
        initMsg();
    }

    private void initMsg(){
        mAmap.getUiSettings().setAllGesturesEnabled(false);
        mAmap.getUiSettings().setZoomGesturesEnabled(true);
        mAmap.getUiSettings().setScrollGesturesEnabled(true);
        mTxName         .setText(mCollectPoiItem.pName);
        mTxDesc         .setText(mCollectPoiItem.pDesc);

        float dis = (int) AMapUtils.calculateLineDistance(mLatlng,latLng);
        if (dis > 1000){
            dis = dis/1000;
            DecimalFormat df = new DecimalFormat("0.0");
            String result = df.format(dis);
            mTxDis.setText(result+getString(R.string.kmile));
        }else {
            mTxDis.setText(""+dis+getString(R.string.mile));
        }
        mMarkerPoi = mAmap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.pre_list_img2))
                .draggable(false));
        mMarkerPoi.setPosition(latLng);
        mMarkerPoi.setAnchor(0.5f,0.5f);
        mMarkerPoi.setVisible(true);
        mMarkerPoi.setInfoWindowEnable(true);

        CameraUpdate update = CameraUpdateFactory.newCameraPosition(new CameraPosition(
                latLng
                ,16, //新的缩放级别
                0, //俯仰角0°~45°（垂直与地图时为0）
                0  ////偏航角 0~360° (正北方为0)
        ));
        mAmap.animateCamera(update);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_exit_show:
                mActivity.exitFragment();
                break;

            case R.id.btn_collect:
                changeLoveState();
                break;

            case R.id.btn_begin_navi:
                List<NaviLatLng> endPoi = new ArrayList<>();
                endPoi.add(new NaviLatLng(latLng.latitude,latLng.longitude));
                mLocation.tryCalueRunWay(endPoi);
                mActivity.showDialogwithOther();
                break;

            default:
                break;

        }
    }

    private void changeLoveState(){
        if (mCollectPoiItem!=null){
            mCollectPoiItem.delete();
            mCollectPoiItem = null;
            mBtnLove.setBackgroundResource(R.drawable.icon_collect_2);


        }else {
            mBtnLove.setBackgroundResource(R.drawable.icon_collect_1);
            dateHelper.saveCollect(poiName,poiDesc,latLng.latitude,latLng.longitude);
            mCollectPoiItem = dateHelper.getCollectByName(poiName);
        }
    }
}
