package com.xiaopeng.xmapnavi.view.appwidget.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ProviderInfo;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.amap.api.services.core.LatLonPoint;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.mode.NaviViewProvide;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.INaviViewProvide;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviCalueListener;
import com.xiaopeng.xmapnavi.view.appwidget.activity.ShowPosiActivity;
import com.xiaopeng.xmapnavi.view.appwidget.adapter.NaviPathAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by linzx on 2016/10/15.
 */
public class RunNaviWayFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = "RunNaviWayFragment";
    private MapView mAmapView;
    private AMap mAMap;
    private LatLonPoint fromPoint,toPoint;
    private NaviPathAdapter adapter;
    public void setMapView(MapView mapView){
        mAmapView = mapView;
        mAMap = mAmapView.getMap();
        mAMap.clear();

    }
    public void setPosiFromTo(LatLonPoint fromPoint, LatLonPoint toPoint){
        Log.d(TAG,"setPosiFromTo:"+"from:"+fromPoint+"\nto:"+toPoint);
        this.fromPoint = fromPoint;
        this.toPoint = toPoint;
    }

    private ILocationProvider mLocaionPro;
    private View rootView;


    /**
     * 地图对象
     */
    private Marker mStartMarker;
    private Marker mEndMarker;

    /**
     * 保存当前算好的路线
     */
    private SparseArray<RouteOverLay> routeOverlays = new SparseArray<RouteOverLay>();

    /**
     * 当前用户选中的路线，在下个页面进行导航
     */
    private int routeIndex = 0;
    /**路线的权值，重合路线情况下，权值高的路线会覆盖权值低的路线**/
    private int zindex = 1;
    /**
     * 路线计算成功标志位
     */
    private boolean calculateSuccess = false;


    private GridView mGvShowNaviPaths;
    private TextView mTvShowMsg;
    private int[] ints;
    private Button mBtnStartNavi;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocaionPro = LocationProvider.getInstence(getActivity());
        mStartMarker = mAMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), com.xiaopeng.amaplib.R.drawable.start))));
        mEndMarker = mAMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), com.xiaopeng.amaplib.R.drawable.end))));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_calue_navi,container,false);
        initView();
        return rootView;
    }

    private View findViewById(int id){
        return rootView.findViewById(id);
    }

    @Override
    public void onStart() {
        super.onStart();
        readNaviMsg();
    }

    private void initView(){
        mGvShowNaviPaths    = (GridView) findViewById(R.id.gv_show_an);
        mTvShowMsg          = (TextView) findViewById(R.id.tv_show_msg);
        mBtnStartNavi       = (Button) findViewById(R.id.btn_start_navi);

        //--listener--//
        mBtnStartNavi       .setOnClickListener(this);
    }



    @Override
    public void onStop() {
        super.onStop();

    }

    public void setSucceful(int[] ints){
        this.ints = ints;
    }
    private HashMap<Integer, AMapNaviPath> paths;
    private void readNaviMsg(){
        routeOverlays.clear();
        mAmapView.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG,"ready to changeRoute \nfrom:"+fromPoint+"\n toPoint："+toPoint);
                    LatLngBounds bounds = LatLngBounds.builder().include(new LatLng(fromPoint.getLatitude(), fromPoint.getLongitude()))
                            .include(new LatLng(toPoint.getLatitude(), toPoint.getLongitude())).build();
                    CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, 100);
                    mAMap.animateCamera(update);
                    changeRoute();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },800);

        paths = mLocaionPro.getNaviPaths();

        mAmapView.postDelayed(new Runnable() {
            @Override
            public void run() {
//                HashMap<Integer, AMapNaviPath> paths = mLocaionPro.getNaviPaths();
//                INaviViewProvide naviViewProvide = new NaviViewProvide(RunNaviWayFragment.this.getActivity());
//                HashMap<Integer,View> viewHashMap = naviViewProvide.createViewByPath(paths);
//                addViewWithViews(viewHashMap);
                adapter = new NaviPathAdapter(getActivity(),R.layout.layout_gradview_item);
                adapter.setDate(paths,ints);
                mGvShowNaviPaths.setAdapter(adapter);
                mGvShowNaviPaths.setOnItemClickListener(mClickPathItemListner);
            }
        },400);


        if (paths != null && paths.size() > 1 && ints != null){
            for (int i = 0; i < ints.length; i++) {
                AMapNaviPath path = paths.get(ints[i]);
                if (path != null) {
                    drawRoutes(ints[i], path);
                }
            }
        } else {
            AMapNaviPath path = mLocaionPro.getNaviPath();
            /**
             * 单路径不需要进行路径选择，直接传入－1即可
             */
            drawRoutes(-1, path);
        }


    }



    public void changeRoute() {
        if (!calculateSuccess) {
            Toast.makeText(getActivity(), "请先算路", Toast.LENGTH_SHORT).show();
            return;
        }
        /**
         * 计算出来的路径只有一条
         */
        if (routeOverlays.size() == 1) {
//            chooseRouteSuccess = true;
            Toast.makeText(getActivity(), "导航距离:" + (mLocaionPro.getNaviPath()).getAllLength() + "m" + "\n" + "导航时间:" + (mLocaionPro.getNaviPath()).getAllTime() + "s", Toast.LENGTH_SHORT).show();
            return;
        }

        if (routeIndex >= routeOverlays.size())
            routeIndex = 0;
        int routeID = routeOverlays.keyAt(routeIndex);
        //突出选择的那条路
        for (int i = 0; i < routeOverlays.size(); i++) {
            int key = routeOverlays.keyAt(i);
            routeOverlays.get(key).setTransparency(0.4f);
        }
        routeOverlays.get(routeID).setTransparency(1f);
        /**
         * 把用户选择的那条路的权值弄高，使路线高亮显示的同时，重合路段不会变的透明
         **/
        routeOverlays.get(routeID).setZindex(zindex++);


        //必须告诉AMapNavi 你最后选择的哪条路
//        mAMapNavi.selectRouteId(routeID);
        mLocaionPro.selectRouteId(routeID);

//        Toast.makeText(getActivity(), "导航距离:" + (mLocaionPro.getNaviPaths()).get(routeID).getAllLength() + "m" + "\n" + "导航时间:" + (mLocaionPro.getNaviPaths()).get(routeID).getAllTime() + "s", Toast.LENGTH_SHORT).show();
//        routeIndex++;

//        chooseRouteSuccess = true;

        AMapNaviPath path  = paths.get(ints[routeIndex]);
        int traListhNum = getTrafficLightNum(path);
        int cost = path.getTollCost();
        String msgShow = "花费："+cost+"元   经过"+traListhNum+"个红绿灯";
        mTvShowMsg.setText(msgShow);
    }

    private int getTrafficLightNum(AMapNaviPath path){
        List<AMapNaviStep> steps =path.getSteps();
        int count = 0;
        for (AMapNaviStep step:steps){
            count = count + step.getTrafficLightNumber();
        }
        return count;
    }

    private void drawRoutes(int routeId, AMapNaviPath path) {
        calculateSuccess = true;
        mAMap.moveCamera(CameraUpdateFactory.changeTilt(0));
        RouteOverLay routeOverLay = new RouteOverLay(mAMap, path, getActivity());
        routeOverLay.setTrafficLine(true);
        routeOverLay.addToMap();
        routeOverlays.put(routeId, routeOverLay);



    }

    private AdapterView.OnItemClickListener mClickPathItemListner = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            RunNaviWayFragment.this.routeIndex = i;
            RunNaviWayFragment.this.adapter.setIndex(i);
            changeRoute();
        }
    };


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_start_navi:
                ((ShowPosiActivity)getActivity()).requestStartNavi();
                break;

            default:
                break;

        }
    }
}
