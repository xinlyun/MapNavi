package com.xiaopeng.xmapnavi.view.appwidget.fragment.second;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.bean.WherePoi;
import com.xiaopeng.xmapnavi.mode.DateHelper;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.callback.XpWhereListener;
import com.xiaopeng.xmapnavi.view.appwidget.activity.BaseFuncActivityInteface;
import com.xiaopeng.xmapnavi.view.appwidget.activity.MainActivity;
import com.xiaopeng.xmapnavi.view.appwidget.activity.SearchCollectActivity;
import com.xiaopeng.xmapnavi.view.appwidget.fragment.SearchCollectFragment;
import com.xiaopeng.xmapnavi.view.appwidget.fragment.ShowCollectFragment;

import java.util.List;

/**
 * Created by linzx on 2016/11/30.
 */

public class SettingSecondFragment extends Fragment implements View.OnClickListener {
    private View rootView;
    private TextView mTxBackHome,mTxCompany;
//    private ILocationProvider mLocationPro;
    private DateHelper mDateHelper;
    private static final int REQUEST_FIND_HOME = 1;
    private static final int REQUEST_FIND_COMPLETE = 0;
    private BaseFuncActivityInteface mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (BaseFuncActivityInteface) getActivity();
//        mLocationPro = LocationProvider.getInstence(getActivity());
        mDateHelper = new DateHelper();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_second_setting,container,false);
        initView();
        return rootView;
    }

    private View findViewById(int id){
        return rootView.findViewById(id);
    }
    private void initView(){
        mTxBackHome     = (TextView) findViewById(R.id.tx_back_home);
        mTxCompany      = (TextView) findViewById(R.id.tx_company);

        findViewById(R.id.first_ll).setOnClickListener(this);
        findViewById(R.id.second_ll).setOnClickListener(this);
        findViewById(R.id.third_ll).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.first_ll:
//                getActivity().startActivityForResult(new Intent(getActivity(), SearchCollectActivity.class), REQUEST_FIND_HOME);
                SearchCollectFragment searchCollectFragment = new SearchCollectFragment();
                searchCollectFragment.setRequestCode(REQUEST_FIND_HOME);
                mActivity.startFragment(searchCollectFragment);
                break;

            case R.id.second_ll:
//                getActivity().startActivityForResult(new Intent(getActivity(), SearchCollectActivity.class), REQUEST_FIND_COMPLETE);
                SearchCollectFragment searchCollectFragment2 = new SearchCollectFragment();
                searchCollectFragment2.setRequestCode(REQUEST_FIND_COMPLETE);
                mActivity.startFragment(searchCollectFragment2);
                break;

            case R.id.third_ll:
                ((MainActivity)getActivity()).showCollectDialog();
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mDateHelper.setOnWhereListener(xpWhereListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        mDateHelper.setOnWhereListener(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        mTxCompany.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDateHelper.getWhereItems();
            }
        },500);


    }

    private XpWhereListener xpWhereListener = new XpWhereListener() {
        @Override
        public void onWhereCallBack(List<WherePoi> wherePois) {
            for (WherePoi poi:wherePois){
                if (poi.type == REQUEST_FIND_HOME){
                    mTxBackHome.setText(poi.pName);
                }else {
                    mTxCompany.setText(poi.pName);
                }
            }
        }
    };
}
