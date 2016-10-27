package com.xiaopeng.xmapnavi.view.appwidget.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import com.xiaopeng.lib.utils.utils.LogUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.bean.HisItem;
import com.xiaopeng.xmapnavi.mode.DateHelper;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.IHistoryDateHelper;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.callback.XpHisDateListner;
import com.xiaopeng.xmapnavi.presenter.callback.XpSearchListner;
import com.xiaopeng.xmapnavi.view.appwidget.adapter.HistItemAdapater;
import com.xiaopeng.xmapnavi.view.appwidget.adapter.TipItemAdapter;

import java.util.List;

/**
 * Created by linzx on 2016/10/17.
 */
public class SearchPosiActivity  extends Activity implements XpSearchListner
        ,View.OnClickListener,TextWatcher
        ,Inputtips.InputtipsListener,XpHisDateListner {

    public static final String TAG = "MainActivity";
    private static final String ACTION_SEARCH = "ACTION_SEARCH";
    private static final int REQ_HAVE_RESULT = 1;
    private static final String ACTION_MSG = "ACTION_MSG";
    private ILocationProvider mLocationProvider;
    private View rootView;
    private EditText mEtvSearch;
    private String mCity;
    private ListView mLvShowMsg;
    private IHistoryDateHelper mDateHelper;
    private ProgressDialog mProgDialog;
    private List<HisItem> mHisItems;
    private List<Tip> mTips;
    private TipItemAdapter mTipAdapter;
    private HistItemAdapater mHistAdapter;
    private AMapLocation mLocation;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationProvider = LocationProvider.getInstence(this);
        mLocation = mLocationProvider.getAmapLocation();
        mCity = mLocationProvider.getAmapLocation().getCity();
        mDateHelper = new DateHelper();
        mDateHelper.setHisDateListner(this);
        mTipAdapter = new TipItemAdapter(this, R.layout.layout_tip_list_item);
        mHistAdapter = new HistItemAdapater(this,R.layout.layout_fix_list_item,mLocation);
        setContentView(R.layout.fragment_search_posi);
        initView();
    }

//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        rootView = inflater.inflate(R.layout.fragment_search_posi,container,false);
//        initView();
//
//        return rootView;
//    }
//    private View findViewById(int id){
//        return rootView.findViewById(id);
//    }

    private void initView(){
        ((TextView)findViewById(R.id.title_title)).setText(R.string.search);
        mLvShowMsg = (ListView) findViewById(R.id.lv_show_tip_his);
        findViewById(R.id.title_return).setOnClickListener(this);
        findViewById(R.id.pre_beginnavi).setOnClickListener(this);
        mEtvSearch = (EditText) findViewById(R.id.prepare_edittext);
        mEtvSearch.addTextChangedListener(this);

        mProgDialog = new ProgressDialog(this);
        mProgDialog.setTitle("正在搜索数据");
        mProgDialog.setMessage("正在搜索相关信息....");
        mProgDialog.setCancelable(true);


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
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onPause() {
        super.onPause();
        mLocationProvider.removeSearchListner(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mDateHelper.getHisItem(12);
    }




    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.title_return:

                finish();
                break;

            case R.id.pre_beginnavi:
                readyToSearch(mEtvSearch.getText().toString());
                break;

            default:
                break;
        }
    }

    private void readyToSearch(String str){
        if (str.length() < 3){
            Toast.makeText(this,R.string.please_sure_text,Toast.LENGTH_SHORT).show();
        }else {
            mProgDialog.show();
            mLocationProvider.trySearchPosi(str);
            mEtvSearch.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mProgDialog.dismiss();
                }
            },6 * 1000);
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
            Inputtips inputTips = new Inputtips(this, inputQuery);
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
        mTips = list;
        mTipAdapter.setDate(mTips);
        mLvShowMsg.setAdapter(mTipAdapter);
        mLvShowMsg.setOnItemClickListener(mItemTipClickListner);
    }

    @Override
    public void onHistoryDate(List<HisItem> hisItems) {
        mHisItems = hisItems;
        mHistAdapter.setDate(hisItems);
        mLvShowMsg.setAdapter(mHistAdapter);
        mLvShowMsg.setOnItemClickListener(mItemHisClickListner);
    }

    @Override
    public void searchSucceful() {
        mProgDialog.dismiss();
//        MainActivity activity = (MainActivity) this;
//        activity.tryToShowPosi();
//        activity.exitFragment();
        tryToShowPosi();
    }

    public void tryToShowPosi(){
        if (!handler.hasMessages(0)){
            handler.sendEmptyMessageDelayed(0,10);
        }
    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            LogUtils.d(TAG,"searchSucceful");
                if (mLocationProvider.getPoiResult() != null && mLocationProvider.getPoiResult().getPois().size()>=1){

                    Intent intent = new Intent(SearchPosiActivity.this,ShowPosiActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    intent.putExtra(ACTION_SEARCH,REQ_HAVE_RESULT);
//                    intent.putExtra(ACTION_MSG,mSearchName);
                    startActivity(intent);
                    finish();
                } else if ( mLocationProvider.getPoiResult() == null  || mLocationProvider.getPoiResult().getPois().size() < 1){
                    Toast.makeText(SearchPosiActivity.this,"查无结果，请重试",Toast.LENGTH_LONG).show();
                }
        }
    };

    private AdapterView.OnItemClickListener mItemHisClickListner = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
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
                        String name1 = item.posiName;
                        mEtvSearch.setText(name1);
                        readyToSearch(name1);
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
            Tip tip = mTips.get(i);
            String name = tip.getName();
            mDateHelper.savePosiStr(name);
            mEtvSearch.setText(name);
            readyToSearch(name);

        }
    };
}

