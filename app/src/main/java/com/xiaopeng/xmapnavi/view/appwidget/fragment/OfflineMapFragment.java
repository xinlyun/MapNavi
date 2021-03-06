package com.xiaopeng.xmapnavi.view.appwidget.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMapException;
import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapManager;
import com.amap.api.maps.offlinemap.OfflineMapProvince;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.xiaopeng.amaplib.amap.offlinemap.OfflineDownloadedAdapter;
import com.xiaopeng.amaplib.amap.offlinemap.OfflineListAdapter;
import com.xiaopeng.amaplib.amap.offlinemap.OfflinePagerAdapter;
import com.xiaopeng.amaplib.amap.offlinemap.ToastUtil;
import com.xiaopeng.lib.bughunter.BugHunter;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.view.appwidget.activity.BaseFuncActivityInteface;
import com.xiaopeng.xmapnavi.view.appwidget.adapter.MofflineListAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by linzx on 2016/11/9.
 */
public class OfflineMapFragment extends Fragment implements
        OfflineMapManager.OfflineMapDownloadListener, View.OnClickListener, ViewPager.OnPageChangeListener {
    private static final String TAG = "OfflineMapFragment";
    private OfflineMapManager amapManager = null;// 离线地图下载控制器
    private List<OfflineMapProvince> provinceList = new ArrayList<OfflineMapProvince>();// 保存一级目录的省直辖市
    private View rootView;
    // private HashMap<Object, List<OfflineMapCity>> cityMap = new
    // HashMap<Object, List<OfflineMapCity>>();// 保存二级目录的市

//    private TextView mDownloadText;
//    private TextView mDownloadedText;
    private ImageView mBackImage;

    // view pager 两个list以及他们的adapter
    private ViewPager mContentViewPage;
    private ExpandableListView mAllOfflineMapList;
    private ListView mDownLoadedList;

    private MofflineListAdapter adapter;
    private OfflineDownloadedAdapter mDownloadedAdapter;
    private PagerAdapter mPageAdapter;


    // 刚进入该页面时初始化弹出的dialog
    private ProgressDialog initDialog;

    /**
     * 更新所有列表
     */
    private final static int UPDATE_LIST = 0;
    /**
     * 显示toast log
     */
    private final static int SHOW_MSG = 1;

    private final static int DISMISS_INIT_DIALOG = 2;
    private final static int SHOW_INIT_DIALOG = 3;

    private boolean isLoading = false;

    private TextView mTvNowCity,mTvCityBig,mTvState;
    private String cityName;
    private OfflineMapCity mCity;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_LIST:
                    if (isLoading) {
                        if (mContentViewPage.getCurrentItem() == 0) {
                            ((BaseExpandableListAdapter) adapter)
                                    .notifyDataSetChanged();
                        } else {
                            mDownloadedAdapter.notifyDataChange();
                        }
                        handler.sendEmptyMessageDelayed(UPDATE_LIST,1000);
                    }
                    break;
                case SHOW_MSG:
//				Toast.makeText(OfflineMapActivity.this, (String) msg.obj,
//						Toast.LENGTH_SHORT).show()
                    ToastUtil.showShortToast(getActivity(), (String)msg.obj);
                    break;

                case DISMISS_INIT_DIALOG:
                    initDialog.dismiss();
                    handler.sendEmptyMessage(UPDATE_LIST);
                    break;
                case SHOW_INIT_DIALOG:
                    if (initDialog != null) {
                        initDialog.show();
                    }

                    break;

                default:
                    break;
            }
        }

    };

    public void onCreate(Bundle savedInstanceState) {
        BugHunter.countTimeStart(BugHunter.TIME_TYPE_START,TAG,BugHunter.SWITCH_TYPE_START_COOL);
        super.onCreate(savedInstanceState);
		/*
		 * 设置离线地图存储目录，在下载离线地图或初始化地图设置; 使用过程中可自行设置, 若自行设置了离线地图存储的路径，
		 * 则需要在离线地图下载和使用地图页面都进行路径设置
		 */
        // Demo中为了其他界面可以使用下载的离线地图，使用默认位置存储，屏蔽了自定义设置
        // MapsInitialihenger.sdcardDir =OffLineMapUtils.getSdCacheDir(this);
//        setContentView(R.layout.offline_map_layout);
        amapManager = LocationProvider.getInstence(getActivity()).getOfflineMapManager();
        LocationProvider.getInstence(getActivity()).setOfflineMapListner(this);

//		initDialog();
        initNowPosi();

    }

    private void initNowPosi(){
        AMapLocation location = LocationProvider.getInstence(getActivity()).getAmapLocation();
        cityName = location.getCity();
        mCity = amapManager.getItemByCityName(cityName);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_offline_map,container,false);
        init();
        return rootView;
    }

    private View findViewById(int id){
        return rootView.findViewById(id);
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onResume() {
        super.onResume();
        BugHunter.countTimeEnd(getActivity().getApplication(),BugHunter.TIME_TYPE_START,TAG,BugHunter.SWITCH_TYPE_START_COOL);
        isLoading = true;
        handler.sendEmptyMessageDelayed(UPDATE_LIST,1000);
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onPause() {
        super.onPause();
        isLoading = false;
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * 初始化如果已下载的城市多的话，会比较耗时
     */
    private void initDialog() {

        initDialog = new ProgressDialog(getActivity());
        initDialog.setMessage("正在获取离线城市列表");
        initDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        initDialog.setCancelable(false);
        initDialog.show();

        handler.sendEmptyMessage(SHOW_INIT_DIALOG);

        new Thread(new Runnable() {

            @Override
            public void run() {
                Looper.prepare();

                final Handler handler1 = new Handler();
                handler1.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do Work
                        init();
                        handler.sendEmptyMessage(DISMISS_INIT_DIALOG);
                        handler.removeCallbacks(this);
                        Looper.myLooper().quit();
                    }
                }, 10);
                Looper.loop();
            }
        }).start();
    }

    /**
     * 初始化UI布局文件
     */
    private void init() {


        initAllCityList();
        initDownloadedList();

//        mDownloadText = (TextView) findViewById(R.id.download_list_text);
//        mDownloadedText = (TextView) findViewById(R.id.downloaded_list_text);
//
//        mDownloadText.setOnClickListener(this);
//        mDownloadedText.setOnClickListener(this);
        mBackImage = (ImageView) findViewById(R.id.back_image_view);
        mBackImage.setOnClickListener(this);

        // view pager 用到了所有城市list和已下载城市list所有放在最后初始化
        mContentViewPage = (ViewPager) findViewById(R.id.content_viewpage);

        mPageAdapter = new OfflinePagerAdapter(mContentViewPage,
                mAllOfflineMapList, mDownLoadedList);

        mContentViewPage.setAdapter(mPageAdapter);
        mContentViewPage.setCurrentItem(0);
        mContentViewPage.setOnPageChangeListener(this);

        mTvNowCity      = (TextView) findViewById(R.id.tv_city_name);
        mTvCityBig      = (TextView) findViewById(R.id.tv_big_num);
        mTvState        = (TextView) findViewById(R.id.tv_now_state);

        mTvNowCity.setText(cityName);
        if (mCity!=null) {
            double size = ((int) (mCity.getSize() / 1024.0 / 1024.0 * 100)) / 100.0;
            mTvCityBig.setText(String.valueOf(size) + "MB");
            updateCity();
            mTvState.setOnClickListener(this);
        }
    }

    Handler refreshHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            updateCity();
        }
    };


    private void updateCity(){
        switch (mCity.getState()){
            case OfflineMapStatus.CHECKUPDATES:
            case OfflineMapStatus.NEW_VERSION:
            case OfflineMapStatus.STOP:
            case OfflineMapStatus.PAUSE:
            case OfflineMapStatus.START_DOWNLOAD_FAILD:
                mTvState.setText("下载");
                mTvState.setTextColor(getResources().getColor(R.color.text_blue));
                break;
            case OfflineMapStatus.LOADING:
            case OfflineMapStatus.EXCEPTION_NETWORK_LOADING:
                mTvState.setText("下载中...");
                mTvState.setTextColor(getResources().getColor(R.color.text_blue));
                refreshHandler.sendEmptyMessageDelayed(0,3000);
                break;

            case OfflineMapStatus.UNZIP:
                mTvState.setText("解压中...");
                mTvState.setTextColor(getResources().getColor(R.color.text_blue));
                refreshHandler.sendEmptyMessageDelayed(0,3000);
                break;

            default:
                mTvState.setText("已下载");
                mTvState.setTextColor(getResources().getColor(R.color.a3a3a3));
                break;

        }
    }

    /**
     * 初始化所有城市列表
     */
    public void initAllCityList() {
        // 扩展列表
        View provinceContainer = LayoutInflater.from(getActivity())
                .inflate(R.layout.offline_province_listview, null);
        mAllOfflineMapList = (ExpandableListView) provinceContainer
                .findViewById(R.id.province_download_list);



        initProvinceListAndCityMap();

        // adapter = new OfflineListAdapter(provinceList, cityMap, amapManager,
        // OfflineMapActivity.this);
        adapter = new MofflineListAdapter(provinceList, amapManager,
               getActivity());
        // 为列表绑定数据源
        mAllOfflineMapList.setAdapter(adapter);
        // adapter实现了扩展列表的展开与合并监听
        mAllOfflineMapList.setOnGroupCollapseListener(adapter);
        mAllOfflineMapList.setOnGroupExpandListener(adapter);
        mAllOfflineMapList.setGroupIndicator(null);
    }

    /**
     * sdk内部存放形式为<br>
     * 省份 - 各自子城市<br>
     * 北京-北京<br>
     * ...<br>
     * 澳门-澳门<br>
     * 概要图-概要图<br>
     * <br>
     * 修改一下存放结构:<br>
     * 概要图-概要图<br>
     * 直辖市-四个直辖市<br>
     * 港澳-澳门香港<br>
     * 省份-各自子城市<br>
     */
    private void initProvinceListAndCityMap() {

        List<OfflineMapProvince> lists = amapManager
                .getOfflineMapProvinceList();

        provinceList.add(null);
        provinceList.add(null);
        provinceList.add(null);
        // 添加3个null 以防后面添加出现 index out of bounds

        ArrayList<OfflineMapCity> cityList = new ArrayList<OfflineMapCity>();// 以市格式保存直辖市、港澳、全国概要图
        ArrayList<OfflineMapCity> gangaoList = new ArrayList<OfflineMapCity>();// 保存港澳城市
        ArrayList<OfflineMapCity> gaiyaotuList = new ArrayList<OfflineMapCity>();// 保存概要图

        for (int i = 0; i < lists.size(); i++) {
            OfflineMapProvince province = lists.get(i);
            if (province.getCityList().size() != 1) {
                // 普通省份
                provinceList.add(i + 3, province);
                // cityMap.put(i + 3, cities);
            } else {
                String name = province.getProvinceName();
                if (name.contains("香港")) {
                    gangaoList.addAll(province.getCityList());
                } else if (name.contains("澳门")) {
                    gangaoList.addAll(province.getCityList());
                } else if (name.contains("全国概要图")) {
                    gaiyaotuList.addAll(province.getCityList());
                } else {
                    // 直辖市
                    cityList.addAll(province.getCityList());
                }
            }
        }

        // 添加，概要图，直辖市，港口
        OfflineMapProvince gaiyaotu = new OfflineMapProvince();
        gaiyaotu.setProvinceName("概要图");
        gaiyaotu.setCityList(gaiyaotuList);
        provinceList.set(0, gaiyaotu);// 使用set替换掉刚开始的null

        OfflineMapProvince zhixiashi = new OfflineMapProvince();
        zhixiashi.setProvinceName("直辖市");
        zhixiashi.setCityList(cityList);
        provinceList.set(1, zhixiashi);

        OfflineMapProvince gaogao = new OfflineMapProvince();
        gaogao.setProvinceName("港澳");
        gaogao.setCityList(gangaoList);
        provinceList.set(2, gaogao);

        // cityMap.put(0, gaiyaotuList);// 在HashMap中第0位置添加全国概要图
        // cityMap.put(1, cityList);// 在HashMap中第1位置添加直辖市
        // cityMap.put(2, gangaoList);// 在HashMap中第2位置添加港澳

    }

    /**
     * 初始化已下载列表
     */
    public void initDownloadedList() {
        mDownLoadedList = (ListView) LayoutInflater.from(
                getActivity()).inflate(
                R.layout.offline_downloaded_list, null);
        android.widget.AbsListView.LayoutParams params = new android.widget.AbsListView.LayoutParams(
                android.widget.AbsListView.LayoutParams.MATCH_PARENT,
                android.widget.AbsListView.LayoutParams.WRAP_CONTENT);
        mDownLoadedList.setLayoutParams(params);
        mDownloadedAdapter = new OfflineDownloadedAdapter(getActivity(), amapManager);
        mDownLoadedList.setAdapter(mDownloadedAdapter);
    }

    /**
     * 暂停所有下载和等待
     */
    private void stopAll() {
        if (amapManager != null) {
            amapManager.stop();
        }
    }

    /**
     * 继续下载所有暂停中
     */
    private void startAllInPause() {
        if (amapManager == null) {
            return;
        }
        for (OfflineMapCity mapCity : amapManager.getDownloadingCityList()) {
            if (mapCity.getState() == OfflineMapStatus.PAUSE) {
                try {
                    amapManager.downloadByCityName(mapCity.getCity());
                } catch (AMapException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 取消所有<br>
     * 即：删除下载列表中除了已完成的所有<br>
     * 会在OfflineMapDownloadListener.onRemove接口中回调是否取消（删除）成功
     */
    private void cancelAll() {
        if (amapManager == null) {
            return;
        }
        for (OfflineMapCity mapCity : amapManager.getDownloadingCityList()) {
            if (mapCity.getState() == OfflineMapStatus.PAUSE) {
                amapManager.remove(mapCity.getCity());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        amapManager = null;
        refreshHandler.removeMessages(0);
        LocationProvider.getInstence(getActivity()).setOfflineMapListner(null);
        if(initDialog != null) {
            initDialog.dismiss();
            initDialog.cancel();
        }
    }

    private void logList() {
        ArrayList<OfflineMapCity> list = amapManager.getDownloadingCityList();

        for (OfflineMapCity offlineMapCity : list) {
            Log.i("amap-city-loading: ", offlineMapCity.getCity() + ","
                    + offlineMapCity.getState());
        }

        ArrayList<OfflineMapCity> list1 = amapManager
                .getDownloadOfflineMapCityList();

        for (OfflineMapCity offlineMapCity : list1) {
            Log.i("amap-city-loaded: ", offlineMapCity.getCity() + ","
                    + offlineMapCity.getState());
        }
    }

    /**
     * 离线地图下载回调方法
     */
    @Override
    public void onDownload(int status, int completeCode, String downName) {

        switch (status) {
            case OfflineMapStatus.SUCCESS:
                // changeOfflineMapTitle(OfflineMapStatus.SUCCESS, downName);
                if (downName.equals(cityName)){
                    mTvState.setText("已下载");
                    mTvState.setTextColor(getResources().getColor(R.color.a3a3a3));
                }
                break;
            case OfflineMapStatus.LOADING:
                Log.d("amap-download", "download: " + completeCode + "%" + ","
                        + downName);
                if (downName.equals(cityName)){
                    mTvState.setText("下载中...");
                    mTvState.setTextColor(getResources().getColor(R.color.a3a3a3));
                }
                // changeOfflineMapTitle(OfflineMapStatus.LOADING, downName);
                break;
            case OfflineMapStatus.UNZIP:
                Log.d("amap-unzip", "unzip: " + completeCode + "%" + "," + downName);
                if (downName.equals(cityName)){
                    mTvState.setText("解压中...");
                    mTvState.setTextColor(getResources().getColor(R.color.a3a3a3));
                }
                // changeOfflineMapTitle(OfflineMapStatus.UNZIP);
                // changeOfflineMapTitle(OfflineMapStatus.UNZIP, downName);
                break;
            case OfflineMapStatus.WAITING:
                Log.d("amap-waiting", "WAITING: " + completeCode + "%" + ","
                        + downName);
                if (downName.equals(cityName)){
                    mTvState.setText("等待中...");
                    mTvState.setTextColor(getResources().getColor(R.color.a3a3a3));
                }
                break;
            case OfflineMapStatus.PAUSE:
                Log.d("amap-pause", "pause: " + completeCode + "%" + "," + downName);
                if (downName.equals(cityName)){
                    mTvState.setText("暂停");
                    mTvState.setTextColor(getResources().getColor(R.color.text_blue));
                }
                break;
            case OfflineMapStatus.STOP:
                if (downName.equals(cityName)){
                    mTvState.setText("停止");
                    mTvState.setTextColor(getResources().getColor(R.color.text_blue));
                }
                break;
            case OfflineMapStatus.ERROR:
                Log.e("amap-download", "download: " + " ERROR " + downName);
                if (downName.equals(cityName)){
                    mTvState.setText("停止");
                    mTvState.setTextColor(getResources().getColor(R.color.text_blue));
                }
                break;
            case OfflineMapStatus.EXCEPTION_AMAP:
                Log.e("amap-download", "download: " + " EXCEPTION_AMAP " + downName);

                break;
            case OfflineMapStatus.EXCEPTION_NETWORK_LOADING:
                Log.e("amap-download", "download: " + " EXCEPTION_NETWORK_LOADING "
                        + downName);
                Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT)
                        .show();
                amapManager.pause();
                break;
            case OfflineMapStatus.EXCEPTION_SDCARD:
                Log.e("amap-download", "download: " + " EXCEPTION_SDCARD "
                        + downName);
                break;
            default:
                break;
        }

//         changeOfflineMapTitle(status, downName);
        handler.sendEmptyMessage(UPDATE_LIST);

    }

    @Override
    public void onCheckUpdate(boolean hasNew, String name) {
        // TODO Auto-generated method stub
        Log.i("amap-demo", "onCheckUpdate " + name + " : " + hasNew);
        Message message = new Message();
        message.what = SHOW_MSG;
        message.obj = "CheckUpdate " + name + " : " + hasNew;
        handler.sendMessage(message);
    }

    @Override
    public void onRemove(boolean success, String name, String describe) {
        // TODO Auto-generated method stub
        Log.i("amap-demo", "onRemove " + name + " : " + success + " , "
                + describe);
        handler.sendEmptyMessage(UPDATE_LIST);

        Message message = new Message();
        message.what = SHOW_MSG;
        message.obj = "onRemove " + name + " : " + success + " , " + describe;
        handler.sendMessage(message);

    }

    @Override
    public void onClick(View v) {


        if (v.equals(mBackImage)) {
            // 返回
//            finish();
            ((BaseFuncActivityInteface)getActivity()).exitFragment();
        }
        switch (v.getId()){
            case R.id.tv_now_state:
                try {

                    if (mCity.getState()!=OfflineMapStatus.LOADING) {
                        mTvState.setTextColor(getResources().getColor(R.color.text_blue));
                        mTvState.setText("下载中...");
                    }
                    amapManager.downloadByCityName(cityName);
                    refreshHandler.sendEmptyMessageDelayed(0,3000);
                } catch (AMapException e) {
                    e.printStackTrace();
                }
                break;
        }

    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int arg0) {






    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
