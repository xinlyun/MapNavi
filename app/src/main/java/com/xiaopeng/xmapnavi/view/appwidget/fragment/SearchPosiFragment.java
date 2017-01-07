package com.xiaopeng.xmapnavi.view.appwidget.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;

import com.gc.materialdesign.widgets.ProgressDialog;
import com.xiaopeng.lib.bughunter.BugHunter;
import com.xiaopeng.lib.utils.utils.LogUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.bean.HisItem;
import com.xiaopeng.xmapnavi.mode.DateHelper;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.IHistoryDateHelper;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.callback.OnClickRightItem;
import com.xiaopeng.xmapnavi.presenter.callback.XpHisDateListner;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviCalueListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpSearchListner;
import com.xiaopeng.xmapnavi.view.appwidget.activity.BaseFuncActivityInteface;
import com.xiaopeng.xmapnavi.view.appwidget.activity.MainActivity;
import com.xiaopeng.xmapnavi.view.appwidget.adapter.HistItemAdapater;
import com.xiaopeng.xmapnavi.view.appwidget.adapter.TipItemAdapter;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.DelSlideListView;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.OnDeleteListioner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linzx on 2016/10/17.
 */
public class SearchPosiFragment extends Fragment implements XpSearchListner
        ,View.OnClickListener,TextWatcher
        ,Inputtips.InputtipsListener,XpHisDateListner
        ,XpNaviCalueListener ,OnClickRightItem
        ,OnDeleteListioner{
    private static final String TAG = "SearchPosiFragment";
    private ILocationProvider mLocationProvider;
    private View rootView;
    private EditText mEtvSearch;
    private String mCity;
    private DelSlideListView mLvShowMsg;
    private IHistoryDateHelper mDateHelper;
    private ProgressDialog mProgDialog;
    private List<HisItem> mHisItems;
    private List<Tip> mTips;
    private TipItemAdapter mTipAdapter;
    private HistItemAdapater mHistAdapter;
    private AMapLocation mLocation;
    private float poiLat,poiLon;
    private boolean isHistoryList = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        BugHunter.countTimeStart(BugHunter.TIME_TYPE_START,TAG,BugHunter.SWITCH_TYPE_START_COOL);
        super.onCreate(savedInstanceState);
        mLocationProvider = LocationProvider.getInstence(getActivity());
        mLocation = mLocationProvider.getAmapLocation();
        mLocationProvider    .stopNavi();
        mCity = mLocation.getCity();
        mDateHelper = new DateHelper();
        mDateHelper.setHisDateListner(this);
        mTipAdapter = new TipItemAdapter(getActivity(),R.layout.layout_tip_list_item);
        mHistAdapter = new HistItemAdapater(getActivity(),R.layout.layout_fix_list_item,mLocation);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_search_posi,container,false);
        initView();

        return rootView;
    }
    private View findViewById(int id){
        return rootView.findViewById(id);
    }

    private void initView(){
//        ((TextView)findViewById(R.id.title_title)).setText(R.string.search);
        mLvShowMsg = (DelSlideListView) findViewById(R.id.lv_show_tip_his);
        mLvShowMsg.setDeleteListioner(this);
        findViewById(R.id.btn_return).setOnClickListener(this);
//        findViewById(R.id.title_title).setOnClickListener(this);
        findViewById(R.id.pre_beginnavi).setOnClickListener(this);
        findViewById(R.id.btn_go_collect).setOnClickListener(this);
        findViewById(R.id.btn_search).setOnClickListener(this);
        findViewById(R.id.inside_ll).setOnClickListener(this);
        findViewById(R.id.outside_ll).setOnClickListener(this);
        findViewById(R.id.btn_start_power).setOnClickListener(this);
        mEtvSearch = (EditText) findViewById(R.id.prepare_edittext);
        mEtvSearch.addTextChangedListener(this);

        mProgDialog = new ProgressDialog(this.getActivity(),"正在搜索数据");
//        mProgDialog.setTitle("正在搜索数据");
//        mProgDialog.setMessage("正在搜索相关信息....");
        mProgDialog.setCancelable(false);
        mProgDialog.getWindow().setDimAmount(0.7f);

        //----init listener ---//
        mProgDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });
    }
    /**
     * 方法必须重写
     */
    @Override
    public void onResume() {
        super.onResume();

        mLocationProvider.addSearchListner(this);
        mEtvSearch.requestFocus();
        BugHunter.countTimeEnd(getActivity().getApplication(),BugHunter.TIME_TYPE_START,TAG,BugHunter.SWITCH_TYPE_START_COOL);
        rootView.postDelayed(new Runnable() {
            @Override
            public void run() {
                showInputT();
            }
        },800);
        rootView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDateHelper.getHisItem(12);
            }
        },400);

    }

    /**
     * 方法必须重写
     */
    @Override
    public void onPause() {
        super.onPause();
        mLocationProvider.removeSearchListner(this);
        showHide();
    }

    @Override
    public void onStart() {
        super.onStart();
        mLocationProvider.addNaviCalueListner(this);


    }

    @Override
    public void onStop() {
        super.onStop();
        mLocationProvider.removeNaviCalueListner(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_return:
                ((BaseFuncActivityInteface)getActivity()).exitFragment();
//                getFragmentManager().popBackStack();
                break;

            case R.id.pre_beginnavi:
                readyToSearch(mEtvSearch.getText().toString());
                break;

            case R.id.btn_go_collect:
                showHide();
                ((MainActivity)getActivity()).showCollectDialog();
                break;

            case R.id.btn_start_power:
                ((BaseFuncActivityInteface)getActivity()).startFragment(new ShowStubGroupFragment());
                break;

            case R.id.btn_search:
                showHide();
                if (mEtvSearch.getText().length()<1){
                    Toast.makeText(getActivity(),"请先输入文字",Toast.LENGTH_SHORT).show();
                }else {
                    String msg = mEtvSearch.getText().toString();
                    readyToSearch(msg);
                }
                break;

            case R.id.outside_ll:
                ((BaseFuncActivityInteface)getActivity()).exitFragment();
                break;

            default:
                break;
        }
    }

    private void readyToSearch(String str){
        if (str.length() < 1){
            Toast.makeText(getActivity(),R.string.please_sure_text,Toast.LENGTH_SHORT).show();
        }else {
            mProgDialog.show();
            mLocationProvider.trySearchPosi(str);

        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (charSequence.length()>=2){
            String str = charSequence.toString();
            InputtipsQuery inputQuery = new InputtipsQuery(str, mCity);
            inputQuery.setCityLimit(true);
            Inputtips inputTips = new Inputtips(getActivity(), inputQuery);
            inputTips.setInputtipsListener(this);
            inputTips.requestInputtipsAsyn();
        } else {
            mDateHelper.getHisItem(12);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onGetInputtips(List<Tip> list, int i) {
        if (mEtvSearch.getText().toString().length()>1) {
            mTips = list;
            mTipAdapter.setDate(mTips);
            mLvShowMsg.setAdapter(mTipAdapter);
            mLvShowMsg.setOnItemClickListener(mItemTipClickListner);
            isHistoryList = false;
        }
    }

    @Override
    public void onHistoryDate(List<HisItem> hisItems) {

        mHisItems = hisItems;
        mHistAdapter.setDate(hisItems);
        mHistAdapter.setOnClickRightItem(this);
        mLvShowMsg.setAdapter(mHistAdapter);
        mLvShowMsg.setOnItemClickListener(mItemHisClickListner);
        isHistoryList = true;
    }

    @Override
    public void searchSucceful() {
        mEtvSearch.postDelayed(new Runnable() {
            @Override
            public void run() {
                try{
                    mProgDialog.dismiss();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },2000);
        mEtvSearch.setText("");
        BaseFuncActivityInteface activity = (BaseFuncActivityInteface) getActivity();
        tryToShowPosi();
//        activity.exitFragment();
    }
    private void tryToShowPosi(){
        LogUtils.d(TAG,"searchSucceful");
        if (mLocationProvider.getPoiResult() != null && mLocationProvider.getPoiResult().getPois().size()>=1){

//            Intent intent = new Intent(getActivity(),ssShowPosiActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//
//            startActivity(intent);
            ShowPosiFragment fragment = new ShowPosiFragment();
            BaseFuncActivityInteface activityInteface = (BaseFuncActivityInteface) getActivity();
            fragment.setMapView(activityInteface.getMapView());
            activityInteface.startFragment(fragment);
        } else if ( mLocationProvider.getPoiResult() == null  || mLocationProvider.getPoiResult().getPois().size() < 1){
            Toast.makeText(getActivity(),"查无结果，请重试",Toast.LENGTH_LONG).show();
        }
    }


    private AdapterView.OnItemClickListener mItemHisClickListner = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            SearchPosiFragment.this.showHide();
            if (i < mHisItems.size()) {
                HisItem item = mHisItems.get(i);
                mDateHelper.updateHisItem(item);
                switch (item.type) {
                    case DateHelper.TYPE_NAME:
                        String name = item.msg;
                        mEtvSearch.setText(name);
                        readyToSearch(name);
                        break;

                    case DateHelper.TYPE_POSI:
//                        String name1 = item.posiName;
//                        mEtvSearch.setText(name1);
//                        readyToSearch(name1);
//                        SearchPosiFragment.this.onClickRightItem(i);
                        HisItem hisItem = mHisItems.get(i);
//                        hisItem.delete();
//                        mDateHelper.getHisItem(12);
                        if (hisItem.type == DateHelper.TYPE_POSI){
                            List<NaviLatLng> startPoi = new ArrayList<>();
                            startPoi.add(new NaviLatLng(mLocationProvider.getAmapLocation().getLatitude(),mLocationProvider.getAmapLocation().getLongitude()));
                            List<NaviLatLng> wayPoi = new ArrayList<>();
                            List<NaviLatLng> endPoi = new ArrayList<>();
                            endPoi.add(new NaviLatLng(hisItem.posiLat,hisItem.posiLon));
                            poiLat = hisItem.posiLat;
                            poiLon = hisItem.posiLon;
//                            ((MainActivity)getActivity()).setPosi(poiLat,poiLon);
                            mLocationProvider.calueRunWay(startPoi,wayPoi,endPoi);
                            mProgDialog.show();
                        }
                        break;

                    case DateHelper.TYPE_WAY:
                        String name2 = item.posiName;
                        mEtvSearch.setText(name2);
                        readyToSearch(name2);
                        break;

                    default:
                        break;

                }
            }else {
                mDateHelper.clearDate();
            }
        }
    };

    private AdapterView.OnItemClickListener mItemTipClickListner = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            SearchPosiFragment.this.showHide();
            Tip tip = mTips.get(i);
            String name = tip.getName();
            mDateHelper.savePosiStr(name);
            mEtvSearch.setText(name);
            readyToSearch(name);

        }
    };

    @Override
    public void onCalculateMultipleRoutesSuccess(int[] ints) {
        mEtvSearch.postDelayed(new Runnable() {
            @Override
            public void run() {
                try{
                    mProgDialog.dismiss();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },2000);
        mEtvSearch.setText("");
//        BaseFuncActivityInteface activity = (BaseFuncActivityInteface) getActivity();
//        activity.haveCalueNaviSucceful(ints,poiLat,poiLon);
//        activity.exitFragment();
    }

    @Override
    public void onCalculateRouteSuccess() {
        mEtvSearch.postDelayed(new Runnable() {
            @Override
            public void run() {
                try{
                    mProgDialog.dismiss();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },2000);
    }

    @Override
    public void onCalculateRouteFailure() {
        mEtvSearch.postDelayed(new Runnable() {
            @Override
            public void run() {
                try{
                    mProgDialog.dismiss();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },1000);
    }

    @Override
    public void onClickRightItem(int posi) {
        LogUtils.d(TAG,"onClickRightItem  POSI:"+posi);
        HisItem hisItem = mHisItems.get(posi);
        hisItem.delete();
        mDateHelper.getHisItem(12);
//        if (hisItem.type == DateHelper.TYPE_POSI){
//            List<NaviLatLng> startPoi = new ArrayList<>();
//            startPoi.add(new NaviLatLng(mLocationProvider.getAmapLocation().getLatitude(),mLocationProvider.getAmapLocation().getLongitude()));
//            List<NaviLatLng> wayPoi = new ArrayList<>();
//            List<NaviLatLng> endPoi = new ArrayList<>();
//            endPoi.add(new NaviLatLng(hisItem.posiLat,hisItem.posiLon));
//            poiLat = hisItem.posiLat;
//            poiLon = hisItem.posiLon;
//            ((MainActivity)getActivity()).setPosi(poiLat,poiLon);
//            mLocationProvider.calueRunWay(startPoi,wayPoi,endPoi);
//            mProgDialog.show();
//
//        }
    }

    @Override
    public boolean isCandelete(int position) {
        if (!isHistoryList)return false;
        if (mHisItems!=null) {
            if (position == mHisItems.size()) {
                return false;
            } else {
                return true;
            }
        }return false;
    }

    @Override
    public void onDelete(int ID) {

    }

    @Override
    public void onBack() {

    }


    private void showInputT(){
        try {
            InputMethodManager imm = (InputMethodManager) getActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mEtvSearch, InputMethodManager.RESULT_SHOWN);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                    InputMethodManager.HIDE_IMPLICIT_ONLY);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void showHide(){
        try {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            //隐藏软键盘
            imm.hideSoftInputFromWindow(mEtvSearch.getWindowToken(), 0);
            //显示软键盘
//        imm.showSoftInputFromInputMethod(mEtSearch.getWindowToken(), 0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
