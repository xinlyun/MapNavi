package com.xiaopeng.xmapnavi.view.appwidget.fragment.second;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.navi.enums.AimLessMode;
import com.amap.api.navi.enums.BroadcastMode;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;

/**
 * Created by linzx on 2016/11/30.
 */

public class SettingFirstFragment extends Fragment implements View.OnClickListener{
    private View rootView;
    //    private ImageView mIv0,mIv1,mIv2,mIv3,mIv4,mIv5,mIv6,mIv7,mIv8;
//    private TextView mTx0,mTx1,mTx2,mTx3,mTx4,mTx5,mTx6,mTx7,mTx8;
    private ILocationProvider mLocationPro;
    private int[] ivId = new int[]{
            R.id.iv_icon_0,R.id.iv_icon_1,R.id.iv_icon_2,R.id.iv_icon_3
            ,R.id.iv_icon_4,R.id.iv_icon_5,R.id.iv_icon_6,R.id.iv_icon_7,R.id.iv_icon_8
    };
    private int[] txId = new int[]{
            R.id.btn_select_0,R.id.btn_select_1,R.id.btn_select_2,R.id.btn_select_3
            ,R.id.btn_select_4,R.id.btn_select_5,R.id.btn_select_6,R.id.btn_select_7,R.id.btn_select_8
    };
    private ImageView[] imageViews = new ImageView[9];
    //    {
//            mIv0,mIv1,mIv2,mIv3,mIv4,mIv5,mIv6,mIv7,mIv8
//    };
    private TextView[] textViews = new TextView[9];
    //    {
//            mTx0,mTx1,mTx2,mTx3,mTx4,mTx5,mTx6,mTx7,mTx8
//    };
    private boolean[] booleens = new boolean[]{
            false,false,false,false
    };

    private int aimState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationPro = LocationProvider.getInstence(getActivity());
        aimState = mLocationPro.getAimState();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_first_setting,container,false);
        initView();
        return rootView;
    }

    private View findViewById(int id){
        return rootView.findViewById(id);
    }
    private void initView(){
        for (int i =0;i<ivId.length;i++){
            imageViews[i] = (ImageView) findViewById(ivId[i]);
            imageViews[i].setOnClickListener(this);
        }
        for (int i =0;i<txId.length;i++){
            textViews[i] = (TextView) findViewById(txId[i]);
            textViews[i] .setOnClickListener(this);
        }

        findViewById(R.id.btn_weixin).setOnClickListener(this);
        initFirst();
        initSecond();
        initThird();
    }
    private void initFirst(){
        for (int i=0;i<4;i++){
            booleens[i] = mLocationPro.getNaviLikeStyle(i);
            if (booleens[i]){
                imageViews[i].setImageResource(R.drawable.icon_like_02);
                textViews[i].setTextColor(getResources().getColor(R.color.text_blue));
            }else {
                imageViews[i].setImageResource(R.drawable.icon_like_01);
                textViews[i].setTextColor(getResources().getColor(R.color.first_text_color));
            }
        }
    }

    private void initSecond(){
        if (mLocationPro.getBroadCastMode()== BroadcastMode.CONCISE){
            imageViews[4].setImageResource(R.drawable.icon_like_02);
            textViews[4].setTextColor(getResources().getColor(R.color.text_blue));
            imageViews[5].setImageResource(R.drawable.icon_like_01);
            textViews[5].setTextColor(getResources().getColor(R.color.first_text_color));

        }else {
            imageViews[5].setImageResource(R.drawable.icon_like_02);
            textViews[5].setTextColor(getResources().getColor(R.color.text_blue));
            imageViews[4].setImageResource(R.drawable.icon_like_01);
            textViews[4].setTextColor(getResources().getColor(R.color.first_text_color));
        }
    }

    private void initThird(){

        switch (aimState){
            case AimLessMode.NONE_DETECTED:
                imageViews[6].setImageResource(R.drawable.icon_like_01);
                imageViews[8].setImageResource(R.drawable.icon_like_01);
                textViews[6].setTextColor(getResources().getColor(R.color.first_text_color));
                textViews[8].setTextColor(getResources().getColor(R.color.first_text_color));

                break;

            case AimLessMode.CAMERA_DETECTED:
                imageViews[6].setImageResource(R.drawable.icon_like_01);
                imageViews[8].setImageResource(R.drawable.icon_like_02);
                textViews[6].setTextColor(getResources().getColor(R.color.first_text_color));
                textViews[8].setTextColor(getResources().getColor(R.color.text_blue));
                break;

            case AimLessMode.SPECIALROAD_DETECTED:
                imageViews[6].setImageResource(R.drawable.icon_like_02);
                imageViews[8].setImageResource(R.drawable.icon_like_01);
                textViews[6].setTextColor(getResources().getColor(R.color.text_blue));
                textViews[8].setTextColor(getResources().getColor(R.color.first_text_color));
                break;

            case AimLessMode.CAMERA_AND_SPECIALROAD_DETECTED:
                imageViews[6].setImageResource(R.drawable.icon_like_02);
                imageViews[8].setImageResource(R.drawable.icon_like_02);
                textViews[6].setTextColor(getResources().getColor(R.color.text_blue));
                textViews[8].setTextColor(getResources().getColor(R.color.text_blue));
                break;
        }
    }

    private void startWeixinNavi(){
        try{
            Intent intent = new Intent();
            intent.setClassName("com.xiaopeng.naviprovide","com.xiaopeng.naviprovide.ShowCode2Activity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getActivity().startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_weixin:
                startWeixinNavi();
                break;


            case R.id.iv_icon_0:
                //fill down
            case R.id.btn_select_0:
                booleens[0] = !booleens[0];

                mLocationPro.setNaviStyle(booleens[0],booleens[3],booleens[1],booleens[2]);
                initFirst();
                break;

            case R.id.iv_icon_1:
                //fill down
            case R.id.btn_select_1:
                booleens[1] = !booleens[1];
                if(booleens[1]) {
                    booleens[2] = false;
                }
                mLocationPro.setNaviStyle(booleens[0],booleens[3],booleens[1],booleens[2]);
                initFirst();
                break;

            case R.id.iv_icon_2:
                //fill down
            case R.id.btn_select_2:
                booleens[2] = !booleens[2];
                if (booleens[2]) {
                    booleens[1] = false;
                    booleens[3] = false;
                }
                mLocationPro.setNaviStyle(booleens[0],booleens[3],booleens[1],booleens[2]);
                initFirst();
                break;

            case R.id.iv_icon_3:
                //fill down
            case R.id.btn_select_3:
                booleens[3] = !booleens[3];
                if (booleens[3]) {
                    booleens[2] = false;
                }
                mLocationPro.setNaviStyle(booleens[0],booleens[3],booleens[1],booleens[2]);
                initFirst();
                break;

            case R.id.iv_icon_4:
                //fill down
            case R.id.btn_select_4:
                mLocationPro.setBroadCastMode(BroadcastMode.CONCISE);
                initSecond();
                break;

            case R.id.iv_icon_5:
                //fill down
            case R.id.btn_select_5:
                mLocationPro.setBroadCastMode(BroadcastMode.DETAIL);
                initSecond();
                break;

            case R.id.iv_icon_6:
                //fill down
            case R.id.btn_select_6:
                clickDegree();
                break;

            case R.id.iv_icon_8:
                //fill down
            case R.id.btn_select_8:
                clickCamera();
                break;



        }
    }

    private void clickDegree(){
        switch (aimState){
            case AimLessMode.NONE_DETECTED:
                aimState = AimLessMode.SPECIALROAD_DETECTED;
                break;

            case AimLessMode.CAMERA_DETECTED:
                aimState = AimLessMode.CAMERA_AND_SPECIALROAD_DETECTED;
                break;

            case AimLessMode.SPECIALROAD_DETECTED:
                aimState = AimLessMode.NONE_DETECTED;
                break;

            case AimLessMode.CAMERA_AND_SPECIALROAD_DETECTED:
                aimState = AimLessMode.CAMERA_DETECTED;
                break;
        }
        mLocationPro.setAimState(aimState);
        initThird();
    }

    private void clickCamera(){
        switch (aimState){
            case AimLessMode.NONE_DETECTED:
                aimState = AimLessMode.CAMERA_DETECTED;

                break;

            case AimLessMode.CAMERA_DETECTED:
                aimState = AimLessMode.NONE_DETECTED;
                break;

            case AimLessMode.SPECIALROAD_DETECTED:
                aimState = AimLessMode.CAMERA_AND_SPECIALROAD_DETECTED;
                break;

            case AimLessMode.CAMERA_AND_SPECIALROAD_DETECTED:
                aimState = AimLessMode.SPECIALROAD_DETECTED;
                break;
        }
        mLocationPro.setAimState(aimState);
        initThird();
    }

}
