package com.xiaopeng.xmapnavi.view.appwidget.selfview;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.navi.enums.BroadcastMode;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;

/**
 * Created by linzx on 2016/12/1.
 */

public class RouteNaviSettingDialog implements View.OnClickListener{
    Context mContext;
    Dialog mDialog;
    private ILocationProvider mLocationPro;
    private int[] ivId = new int[]{
            R.id.iv_icon_0,R.id.iv_icon_1,R.id.iv_icon_2,R.id.iv_icon_3,R.id.iv_icon_4,R.id.iv_icon_5
    };
    private int[] txId = new int[]{
            R.id.btn_select_0,R.id.btn_select_1,R.id.btn_select_2,R.id.btn_select_3,R.id.btn_select_4,R.id.btn_select_5
    };
    private ImageView[] imageViews = new ImageView[6];
    private TextView[] textViews = new TextView[6];
    private boolean[] booleen = new boolean[]{
        false,false,false,false
    };
    public RouteNaviSettingDialog(Context context){
        mContext = context;
        mLocationPro = LocationProvider.getInstence(context);
        mDialog = new Dialog(context, R.style.navi_dialog);
        mDialog . requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_navi_setting_dialog,null);
        initView(view);
        mDialog.setContentView(view);

        Window dialogWindow = mDialog.getWindow();

        WindowManager.LayoutParams lp = dialogWindow.getAttributes();

        lp.height  = 1440;
        lp.width = 1080;
        lp.x = 0;
        lp.y = 0;
    }

    public void show(){
        if (mDialog!=null)
        mDialog.show();
    }
    public void dismiss(){
        if (mDialog!=null){
            if (mDialog.isShowing()){
                mDialog.dismiss();
            }
        }
    }

    private void initView(View view){
        //TODO
        for (int i=0;i<ivId.length;i++){
            imageViews[i] = (ImageView) view.findViewById(ivId[i]);
            textViews[i] = (TextView) view.findViewById(txId[i]);
            imageViews[i] .setOnClickListener(this);
            textViews[i].setOnClickListener(this);
        }
        view.findViewById(R.id.btn_return).setOnClickListener(this);
        view.findViewById(R.id.touch_in).setOnClickListener(this);
        view.findViewById(R.id.touch_out).setOnClickListener(this);
        initFirstLine();
        initSecondLine();
    }

    private void initFirstLine(){
        for (int i=0;i<4;i++){
            booleen[i] = mLocationPro.getNaviLikeStyle(i);
            if (booleen[i]){
                imageViews[i].setImageResource(R.drawable.icon_like_02);
                textViews[i].setTextColor(mContext.getResources().getColor(R.color.text_blue));
            }else {
                imageViews[i].setImageResource(R.drawable.icon_like_01);
                textViews[i].setTextColor(mContext.getResources().getColor(R.color.first_text_color));
            }
        }
    }

    private void initSecondLine(){
        int mode = mLocationPro.getBroadCastMode();
        if (mode== BroadcastMode.CONCISE){
            imageViews[4].setImageResource(R.drawable.icon_like_02);
            textViews[4].setTextColor(mContext.getResources().getColor(R.color.text_blue));

            imageViews[5].setImageResource(R.drawable.icon_like_01);
            textViews[5].setTextColor(mContext.getResources().getColor(R.color.first_text_color));
        }else {
            imageViews[5].setImageResource(R.drawable.icon_like_02);
            textViews[5].setTextColor(mContext.getResources().getColor(R.color.text_blue));

            imageViews[4].setImageResource(R.drawable.icon_like_01);
            textViews[4].setTextColor(mContext.getResources().getColor(R.color.first_text_color));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_return:
                mDialog.dismiss();
                break;

            case R.id.iv_icon_0:
                //fill down
            case R.id.btn_select_0:
                booleen[0] = !booleen[0];
                mLocationPro.setNaviStyle(booleen[0],booleen[3],booleen[1],booleen[2]);
                initFirstLine();
                break;

            case R.id.iv_icon_1:
                //fill down
            case R.id.btn_select_1:
                booleen[1] = !booleen[1];
                if (booleen[1]){
                    booleen[2] = false;
                }
                mLocationPro.setNaviStyle(booleen[0],booleen[3],booleen[1],booleen[2]);
                initFirstLine();
                break;

            case R.id.iv_icon_2:
                //fill down
            case R.id.btn_select_2:
                booleen[2] = !booleen[2];
                if (booleen[2]){
                    booleen[1] = false;
                    booleen[3] = false;
                }
                mLocationPro.setNaviStyle(booleen[0],booleen[3],booleen[1],booleen[2]);
                initFirstLine();
                break;

            case R.id.iv_icon_3:
                //fill down
            case R.id.btn_select_3:
                booleen[3] = !booleen[3];
                if (booleen[3]){
                    booleen[2] = false;
                }
                mLocationPro.setNaviStyle(booleen[0],booleen[3],booleen[1],booleen[2]);
                initFirstLine();
                break;

            case R.id.iv_icon_4:
                //fill down
            case R.id.btn_select_4:
                mLocationPro.setBroadCastMode(BroadcastMode.CONCISE);
                initSecondLine();
                break;

            case R.id.iv_icon_5:
                //fill down
            case R.id.btn_select_5:
                mLocationPro.setBroadCastMode(BroadcastMode.DETAIL);
                initSecondLine();
                break;

            case R.id.touch_in:

                break;

            case R.id.touch_out:
                if (mDialog!=null && mDialog.isShowing()){
                    mDialog.dismiss();
                }
                break;



        }
    }
}
