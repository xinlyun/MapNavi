package com.xiaopeng.xmapnavi.mode;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiaopeng.lib.utils.utils.LogUtils;
import com.xiaopeng.lib.utils.utils.XPAppSharedPreferenceHelper;
import com.xiaopeng.xmapnavi.bean.Licen;
import com.xiaopeng.xmapnavi.presenter.IWeixinLicenceProvider;
import com.xiaopeng.xmapnavi.presenter.callback.XpLicProListener;
import com.xiaopeng.xmapnavi.utils.LicenceConfig;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by linzx on 2017/1/4.
 */

public class WeixinLicenceProvider implements IWeixinLicenceProvider {
    static final String TAG = "WeixinLicenceProvider";
    private Context mContext;
    private String token ;
    private XpLicProListener mListener;
    private SharedPreferences sharedPreferences;
    private static final int GET_TOKEN = 0,RETURE_FAIL = 1,SUCCESS_GET = 2;
    private int numSave = 0;
    private boolean isGetMsgFromNet = false;

    private static String DEVICE_ID = "device_id",LIENCE = "device_licence",TICKET = "qr_ticket";
    private String baseUrl = "http://112.124.102.62:8180/v1/weixin/device/ticket";

    @Override
    public void init(Context context) {
        mContext = context;
        sharedPreferences = context.getSharedPreferences("licence",Context.MODE_PRIVATE);
        token = XPAppSharedPreferenceHelper.getInstance(context).getToken();
        if (token == null){
//            deleyHandler.sendEmptyMessageDelayed(GET_TOKEN,10 * 1000);
        }else {
            LogUtils.d(TAG,"\ntoken0:  "+token);
//            new GetLience().start();
        }
    }

    @Override
    public void setLicProListener(XpLicProListener licProListener) {
        mListener = licProListener;
    }

    @Override
    public void getLic() {
        String deviceId = sharedPreferences.getString(DEVICE_ID,"");
        if (deviceId.equals("")){
            deleyHandler.sendEmptyMessage(GET_TOKEN);


        }else {
            String lic = sharedPreferences.getString(LIENCE,null);
            String ticket = sharedPreferences.getString(TICKET,null);
            Licen licen = new Licen(deviceId,lic,ticket);
            if (mListener != null){
                mListener.getLicence(licen);
            }
        }
    }

    Handler deleyHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GET_TOKEN:
                    if (token!=null && !token.equals("")){
                        new GetLience().start();
                    }else {
                        if (numSave < 12) {
                            token = XPAppSharedPreferenceHelper.getInstance(mContext).getToken();
                            if (token == null) {
                                deleyHandler.sendEmptyMessageDelayed(0, 10 * 1000);
                                numSave++;
                            } else {
                                LogUtils.d(TAG, "\ntoken2:  " + token);
                                new GetLience().start();
                            }
                        }
                    }
                    break;

                case RETURE_FAIL:
                    numSave = 0;
                    deleyHandler.sendEmptyMessageDelayed(GET_TOKEN,5 * 1000);
                    break;

                case SUCCESS_GET:
                    Licen licen = (Licen) msg.obj;
                    if (mListener!=null){
                        mListener.getLicence(licen);
                    }
                    break;



            }
        }
    };


    class GetLience extends Thread{

        @Override
        public void run() {
            super.run();
            LogUtils.d(TAG,"\ntoken3:  "+WeixinLicenceProvider.this.token);
            OkHttpClient okHttpClient = new OkHttpClient();

            Headers.Builder headerBuilder = new Headers.Builder();
            headerBuilder.set("Authorization", "Basic " + WeixinLicenceProvider.this.token);
            headerBuilder.add("Client", "carcontrol.xmart.com/v1.0.0");
            headerBuilder.set("Accept", "application/json");
//            headerBuilder.set("Content-Type", "application/json");
            Headers headers = headerBuilder.build();
//            String url = baseUrl + "?"+"lng="+mLon+"&lat="+mLat;
            Request request = new Request.Builder().url(baseUrl)
                    .headers(headers)
                    .get()
                    .build();
            try {
                Response response = okHttpClient.newCall(request).execute();

                String body = response.body().string();
                LogUtils.d(TAG, "code:" + response.code() + ":\n" + body);


                JSONObject jsonObject = JSON.parseObject(body);
                JSONObject data = jsonObject.getJSONObject("data");
                String deviceId = data.getString(DEVICE_ID);
                String lic = data.getString(LIENCE);
                String ticket = data.getString(TICKET);
                Licen licen = new Licen(deviceId,lic,ticket);
                saveLicen(licen);
                Message message = deleyHandler.obtainMessage();
                message.what = SUCCESS_GET;
                message.obj = licen;
                deleyHandler.sendMessage(message);

            } catch (Exception e) {
                e.printStackTrace();
//                deleyHandler.sendEmptyMessage(RETURE_FAIL);
                deleyHandler.sendEmptyMessage(RETURE_FAIL);
            }
        }
    }

    private void saveLicen(Licen licen){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DEVICE_ID,licen.getDevice_id());
        editor.putString(LIENCE,licen.getLicence());
        editor.putString(TICKET,licen.getTicket());
        editor.commit();
    }
}
