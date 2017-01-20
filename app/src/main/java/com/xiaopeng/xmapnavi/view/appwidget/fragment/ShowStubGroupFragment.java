package com.xiaopeng.xmapnavi.view.appwidget.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.model.NaviLatLng;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.bean.PowerPoint;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.callback.XpStubGroupListener;
import com.xiaopeng.xmapnavi.view.appwidget.activity.BaseFuncActivityInteface;
import com.xiaopeng.xmapnavi.view.appwidget.adapter.PowerPointAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linzx on 2016/12/28.
 */

public class ShowStubGroupFragment extends Fragment implements View.OnClickListener,AdapterView.OnItemClickListener{
    private static final String TAG = "ShowStubGroupFragment";
    private ILocationProvider mLocationProvider;
    private View rootView;
    private BaseFuncActivityInteface mActivity;
    private ListView mListView;
    private PowerPointAdapter mAdapater;
    private List<PowerPoint> mPowerPoints;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationProvider = LocationProvider.getInstence(getActivity());
        mActivity = (BaseFuncActivityInteface) getActivity();

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_show_power,container,false);
        initView();

        return rootView;
    }

    private View findViewById(int id){
        return rootView.findViewById(id);
    }

    private void initView(){
        LatLng latLng = null;
        if (mLocationProvider!=null) {

            latLng = new LatLng(mLocationProvider.getAmapLocation().getLatitude(),mLocationProvider.getAmapLocation().getLongitude());
        }
        mListView       = (ListView) findViewById(R.id.lv_show_collect);
        mAdapater       = new PowerPointAdapter(getActivity(),R.layout.fragment_show_power);
        mAdapater       .setPoi(latLng);
        mListView       .setAdapter(mAdapater);
        mListView       .setOnItemClickListener(this);
        
        findViewById(R.id.btn_return).setOnClickListener(this);
        findViewById(R.id.rl_out_side).setOnClickListener(this);
    }




    @Override
    public void onStart() {
        super.onStart();
        if (mActivity!=null){
            mActivity.showDialogwithOther();
        }
        if (mLocationProvider!=null){
            mLocationProvider.addStubGroupListener(listener);
            mLocationProvider.getStubGroups();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mLocationProvider!=null){
            mLocationProvider.removeStubGroupListener(listener);
        }
        if (mActivity!=null){
            mActivity.dismissDeleyDialog();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_return:

            case R.id.rl_out_side:
                if (mActivity!=null){
                    mActivity.exitFragment();
                }
                break;



        }
    }

    XpStubGroupListener listener = new XpStubGroupListener() {
        @Override
        public void OnStubData(List<PowerPoint> powerPoints) {
            if (mActivity!=null){
                mActivity.dismissDeleyDialog();
            }
            mAdapater.setData(powerPoints);
            mPowerPoints = powerPoints;

        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mPowerPoints!=null && position < mPowerPoints.size()){
            PowerPoint powerPoint  = mPowerPoints.get(position);
            List<NaviLatLng> naviLatLngs = new ArrayList<>();
            naviLatLngs.add(new NaviLatLng(powerPoint.getLat(),powerPoint.getLon()));

            if(mLocationProvider.tryCalueRunWay(naviLatLngs)){
                mActivity.showDialogwithOther();
            }

        }
    }
}
