package com.xiaopeng.xmapnavi;

import android.app.Application;
import android.graphics.Typeface;

import com.activeandroid.ActiveAndroid;
import com.aispeech.aios.sdk.AIOSForCarSDK;
import com.amap.api.navi.AMapNavi;
import com.tencent.bugly.crashreport.CrashReport;
import com.xiaopeng.lib.bughunter.BugHunter;
import com.xiaopeng.xmapnavi.bean.LocationSaver;

import java.lang.reflect.Field;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by linzx on 2016/10/12.
 */
public class XpApplication extends Application {
    public static Typeface typeFace;
    @Override
    public void onCreate() {
        super.onCreate();
        AMapNavi.setApiKey(this,"518079e13164d2910ff81c078e073bcd");
        ActiveAndroid.initialize(this);
        BugHunter.init(this);
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
            CrashReport.initCrashReport(XpApplication.this, "ae6efa93c1", true);
//            LeakCanary.install(this);
        } catch (Exception e) {

        }

    }

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
}
