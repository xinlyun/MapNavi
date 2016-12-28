package com.xiaopeng.xmapnavi.mode;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.xiaopeng.lib.utils.utils.LogUtils;
import com.xiaopeng.lib.utils.utils.XPAppSharedPreferenceHelper;
import com.xiaopeng.xmapnavi.bean.StubAc;
import com.xiaopeng.xmapnavi.presenter.IStubGroupProvider;

import java.io.IOException;
import java.security.cert.CertificateFactory;
import java.util.List;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by linzx on 2016/12/28.
 */

public class StubGroupProvider implements IStubGroupProvider {
    static final String TAG = "StubGroupProvider";
    private String token ;
    private int numSave = 0;
    private Context mContext;
    private static final int GET_TOKEN = 0 ,RETURE_STUB = 1;
    private OnStubData mListener;
    private String baseUrl = "http://112.124.102.62:8180/v1/charge/stubGroup";
    @Override
    public void init(Context context) {
        mContext = context;
        token = XPAppSharedPreferenceHelper.getInstance(context).getToken();
        if (token == null){
            deleyHandler.sendEmptyMessageDelayed(GET_TOKEN,10 * 1000);
        }else {
            LogUtils.d("StubGroupProvider","\ntoken0:  "+token);
        }
    }

    @Override
    public void getStubGroupByPoi(double lat, double lon) {
        new GetStubMsgFroPoi(lat,lon).start();
    }

    @Override
    public void getStubGroupByCity(String city) {
        new GetStubMsgFroCity(city).start();
    }

    Handler deleyHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GET_TOKEN:
                    if (numSave < 12) {
                        token = XPAppSharedPreferenceHelper.getInstance(mContext).getToken();
                        if (token == null) {
                            deleyHandler.sendEmptyMessageDelayed(0, 10 * 1000);
                            numSave++;
                        }else {
                            LogUtils.d("StubGroupProvider","\ntoken2:  "+token);
                        }
                    }
                    break;
                case RETURE_STUB:
                    if (mListener != null){
                        mListener.stubProvide((List<StubAc>) msg.obj);
                    }
                    break;
            }
        }
    };






    @Override
    public void setOnStubDataListener(OnStubData onStubDataListener) {
        mListener = onStubDataListener;
    }

    class GetStubMsgFroPoi extends Thread{
        double mLat,mLon;
        GetStubMsgFroPoi(double lat,double lon){
            mLat = lat;
            mLon = lon;
        }

        @Override
        public void run() {
            super.run();
            LogUtils.d("StubGroupProvider","\ntoken3:  "+StubGroupProvider.this.token);
            OkHttpClient okHttpClient = new OkHttpClient();

            Headers.Builder headerBuilder = new Headers.Builder();
            headerBuilder.set("Authorization", "Basic " + StubGroupProvider.this.token);
            headerBuilder.add("Client", "carcontrol.xmart.com/v1.0.0");
            headerBuilder.set("Accept", "application/json");
//            headerBuilder.set("Content-Type", "application/json");
            Headers headers = headerBuilder.build();
            String url = baseUrl + "?"+"lng="+mLon+"&lat="+mLat;
            Request request = new Request.Builder().url(url)
                                    .headers(headers)
                                    .get()
                                    .build();
            try {
                Response response = okHttpClient.newCall(request).execute();
                LogUtils.d("StubGroupProvider", "code:" + response.code() + ":\n" + response.body().string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class GetStubMsgFroCity extends Thread{
        String mCity;
        GetStubMsgFroCity(String city){

            LogUtils.d(TAG,"cityCode:"+city);
            mCity = city;
        }

        @Override
        public void run() {
            super.run();
            OkHttpClient okHttpClient = new OkHttpClient();
            LogUtils.d("StubGroupProvider","\ntoken:  "+StubGroupProvider.this.token);
            Headers.Builder headerBuilder = new Headers.Builder();
            headerBuilder.set("Authorization", "Basic " + StubGroupProvider.this.token);
            headerBuilder.add("Client", "carcontrol.xmart.com/v1.0.0");
            headerBuilder.set("Accept", "application/json");
//            headerBuilder.set("Content-Type", "application/json");
            Headers headers = headerBuilder.build();
            String url = baseUrl + "?"+"city="+mCity;
            Request request = new Request.Builder().url(url)
                    .headers(headers)
                    .get()
                    .build();
            try {
                Response response = okHttpClient.newCall(request).execute();
                LogUtils.d("StubGroupProvider", "code:" + response.code() + ":\n" + response.body().string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
