package com.xiaopeng.xmapnavi;

import android.app.Application;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.aispeech.aios.sdk.AIOSForCarSDK;
import com.amap.api.navi.AMapNavi;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.wechat.Cloud;
import com.xiaopeng.lib.bughunter.BugHunter;
import com.xiaopeng.lib.scu.LibScuApplication;
import com.xiaopeng.lib.utils.utils.LogUtils;
import com.xiaopeng.lib.utils.utils.XPAppSharedPreferenceHelper;
import com.xiaopeng.xmapnavi.bean.LocationSaver;
import com.xiaopeng.xmapnavi.mode.AssetsCopyTOSDcard;
import com.xiaopeng.xmapnavi.utils.LicenceConfig;

import java.io.File;
import java.lang.reflect.Field;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by linzx on 2016/10/12.
 */
public class XpApplication extends LibScuApplication {


    static {
        System.loadLibrary("stlport_shared");
        System.loadLibrary("wxcloud");
    }

    public static Typeface typeFace;
    private static final String TAG = "XpApplication";
    public static XpApplication sApplication;

    String licence = "1A02C1DAE06DF1716A1453A6BFA7563D4D516AB38837298B646935DB045A14DD1687803A1373C75BF3A32C1D1C2724115547FBADC138F80116D6CE7AF7A44104B67B20942766B45A0CB9A66D2C10206D";

    public int vehicle_id ,litId;
    public String code = "";
    public String licShow = "";
    public String device_id = "";
    public String ticket = "";

    @Override
    public void onCreate() {
        super.onCreate();
        AMapNavi.setApiKey(this,"518079e13164d2910ff81c078e073bcd");
        try {
            ActiveAndroid.initialize(this);
        }catch (Exception e){
            e.printStackTrace();
        }
//        BugHunter.init(this);


        AIOSForCarSDK.initialize(this);
        if (LocationSaver.getSaveLocation()==null){
            LocationSaver.saverFirst();
        }

//        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
//                .setDefaultFontPath("fonts/fzlth.ttf")
//                .setFontAttrId(R.attr.fontPath)
//                .build()
//        );
//        setTypeface();
        try {

//            LeakCanary.install(this);
            initStyle();
        } catch (Exception e) {

        }
        deleyToInitMsg.sendEmptyMessageDelayed(0,500);


    }

    private Handler deleyToInitMsg = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                CrashReport.initCrashReport(XpApplication.this, "ae6efa93c1", true);
                sApplication = XpApplication.this;
                int Vehicle_id = XPAppSharedPreferenceHelper.getInstance(XpApplication.this).getVehicleId();
                vehicle_id = Vehicle_id;
                Vehicle_id = Vehicle_id%100;
                litId       = Vehicle_id;
                LicenceConfig licenceConfig = new LicenceConfig();
                if(Vehicle_id<licenceConfig.getLICENS().length&&Vehicle_id>=0){
                    Log.d(TAG, "getVehicle_id:"+Vehicle_id);
                    String lic = licenceConfig.getLICENS()[Vehicle_id].getLicence();

                    licShow = lic;
                    device_id = licenceConfig.getLICENS()[Vehicle_id].getDevice_id();
                    ticket = licenceConfig.getLICENS()[Vehicle_id].getTicket();

                    Log.d(TAG, "getLicence:"+lic);
                    if(Cloud.init(lic)){
                        code = "SDK is running!";
                        Log.d(TAG, "SDK is running!");
                        Log.d(TAG,Cloud.getDeviceId());
                        Log.d(TAG,Cloud.getVenderId());
                    }else {
                        code = "Device licence error";
                        Log.e(TAG,"Device licence error");
                    }
                }else {
                    code = "not Device";
                    Log.e(TAG,"not Device");
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    public void setTypeface(){
        //华文彩云，加载外部字体assets/front/huawen_caiyun.ttf
        typeFace = Typeface.createFromAsset(getAssets(), "fonts/fzlth.ttf");
        try
        {
            //与values/styles.xml中的<item name="android:typeface">sans</item>对应
//            Field field = Typeface.class.getDeclaredField("SERIF");
//            field.setAccessible(true);
//            field.set(null, typeFace);

//            Field field_1 = Typeface.class.getDeclaredField("DEFAULT");
//            field_1.setAccessible(true);
//            field_1.set(null, typeFace);

            //与monospace对应
//            Field field_2 = Typeface.class.getDeclaredField("MONOSPACE");
//            field_2.setAccessible(true);
//            field_2.set(null, typeFace);

            //与values/styles.xml中的<item name="android:typeface">sans</item>对应
            Field field_3 = Typeface.class.getDeclaredField("SANS_SERIF");
            field_3.setAccessible(true);
            field_3.set(null, typeFace);
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }
    private void initStyle(){
        AssetsCopyTOSDcard copyTOSDcard = new AssetsCopyTOSDcard(this);
        File file0 = new File(Environment.getExternalStorageDirectory().toString()+"/main_style_false.json");
        if (!file0.exists()){
            LogUtils.d(TAG,"file0 not exists");
            copyTOSDcard.AssetToSD("mapstyle/main_style_false.json",Environment.getExternalStorageDirectory().toString()+"/main_style_false.json");
        }else {
            LogUtils.d(TAG,"file0  exists");
        }
        AssetsCopyTOSDcard copyTOSDcard2 = new AssetsCopyTOSDcard(this);
        File file1 = new File(Environment.getExternalStorageDirectory().toString()+"/main_style_true.json");
        if (!file1.exists()){
            LogUtils.d(TAG,"file1 not exists");
            copyTOSDcard2.AssetToSD("mapstyle/main_style_true.json",Environment.getExternalStorageDirectory().toString()+"/main_style_true.json");
        }else {
            LogUtils.d(TAG,"file0  exists");
        }
    }


}
