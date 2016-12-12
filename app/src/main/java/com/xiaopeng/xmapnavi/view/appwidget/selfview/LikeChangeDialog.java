package com.xiaopeng.xmapnavi.view.appwidget.selfview;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;

/**
 * Created by linzx on 2016/11/29.
 */

public class LikeChangeDialog implements View.OnClickListener{
    Context mContext;
    Dialog mDialog;
    private ILocationProvider mLocationPro;
    private ImageView mIvIcon0,mIvIcon1,mIvIcon2,mIvIcon3;
    private TextView mTxSelect0,mTxSelect1,mTxSelect2,mTxSelect3;
    private ImageView[] imageViews;
    private TextView[] textViews;
    private boolean[] styles;
    private OnSelectLikeStyle mListener;
    public LikeChangeDialog(Context context){
        mContext = context;
        mLocationPro = LocationProvider.getInstence(context);
        mDialog = new Dialog(context, R.style.navi_dialog);
        mDialog . requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_like_change_dialog,null);
        initView(view);
        mDialog.setContentView(view);

        Window dialogWindow = mDialog.getWindow();

        WindowManager.LayoutParams lp = dialogWindow.getAttributes();

        lp.height  = 1440;
        lp.width = 1080;
        lp.x = 0;
        lp.y = 0;

    }
    private void initView(View view){
        mIvIcon0        = (ImageView) view.findViewById(R.id.iv_icon_0);
        mIvIcon1        = (ImageView) view.findViewById(R.id.iv_icon_1);
        mIvIcon2        = (ImageView) view.findViewById(R.id.iv_icon_2);
        mIvIcon3        = (ImageView) view.findViewById(R.id.iv_icon_3);
        mTxSelect0      = (TextView) view.findViewById(R.id.btn_select_0);
        mTxSelect1      = (TextView) view.findViewById(R.id.btn_select_1);
        mTxSelect2      = (TextView) view.findViewById(R.id.btn_select_2);
        mTxSelect3      = (TextView) view.findViewById(R.id.btn_select_3);
        mTxSelect0      .setOnClickListener(this);
        mTxSelect1      .setOnClickListener(this);
        mTxSelect2      .setOnClickListener(this);
        mTxSelect3      .setOnClickListener(this);
        mIvIcon0        .setOnClickListener(this);
        mIvIcon1        .setOnClickListener(this);
        mIvIcon2        .setOnClickListener(this);
        mIvIcon3        .setOnClickListener(this);
        view.findViewById(R.id.btn_sure).setOnClickListener(this);
        view.findViewById(R.id.btn_return).setOnClickListener(this);
        view.findViewById(R.id.layout_outside).setOnClickListener(this);
        view.findViewById(R.id.layout_inside).setOnClickListener(this);

        imageViews = new ImageView[]{
                mIvIcon0,mIvIcon1,mIvIcon2,mIvIcon3
        };
        textViews  = new TextView[]{
                mTxSelect0,mTxSelect1,mTxSelect2,mTxSelect3
        };
        styles = new boolean[]{
                false,false,false,false
        };

    }

    private void initStyle(){
        for (int i=0;i<4;i++) {
            boolean biStyle = mLocationPro.getNaviLikeStyle(i);
            styles[i] = biStyle;
            if (biStyle){
                imageViews[i].setImageResource(R.drawable.icon_like_02);
                textViews[i].setTextColor(mContext.getResources().getColor(R.color.text_blue));
            }else {
                imageViews[i].setImageResource(R.drawable.icon_like_01);
                textViews[i].setTextColor(mContext.getResources().getColor(R.color.first_text_color));
            }
        }
    }

    public void show(){
        initStyle();
        if (mDialog!=null){
            mDialog.show();
        }
    }

    public void dismiss(){
        if (mDialog!=null){
            if (mDialog.isShowing()){
                mDialog.dismiss();
            }
        }
    }

    private void undateView(){
        for (int i=0;i<4;i++) {
            boolean biStyle = styles[i];
            if (biStyle){
                imageViews[i].setImageResource(R.drawable.icon_like_02);
                textViews[i].setTextColor(mContext.getResources().getColor(R.color.text_blue));
                textViews[i].setBackgroundResource(R.drawable.button_like_bg_2);
            }else {
                imageViews[i].setImageResource(R.drawable.icon_like_01);
                textViews[i].setTextColor(mContext.getResources().getColor(R.color.first_text_color));
                textViews[i].setBackgroundResource(R.drawable.button_like_bg_1);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_icon_0:
                //fill
            case R.id.btn_select_0:
                styles[0] = !styles[0];
                undateView();
                break;

            case R.id.iv_icon_1:
                //fill
            case R.id.btn_select_1:
                styles[1] = !styles[1];
                if (styles[1]) {
                    styles[2] = false;
                }
                undateView();
                break;

            case R.id.iv_icon_2:
                //fill
            case R.id.btn_select_2:

                styles[2] = !styles[2];
                if (styles[2]) {
                    styles[1] = false;
                    styles[3] = false;
                }
                undateView();
                break;

            case R.id.iv_icon_3:
                //fill
            case R.id.btn_select_3:

                styles[3] = !styles[3];
                if (styles[3]){
                    styles[2] = false;
                }
                undateView();
                break;

            case R.id.btn_sure:
                if (mListener!=null){
                    mListener.changeLikeStyle(styles[0],styles[1],styles[2],styles[3]);
                }
                mDialog.dismiss();
                break;

            case R.id.btn_return:
                mDialog.dismiss();
                break;

            case R.id.layout_outside:
                mDialog.dismiss();
                break;
        }
    }

    public void setOnSelectLikeStyle(OnSelectLikeStyle likeStyle){
        mListener = likeStyle;
    }

    public interface  OnSelectLikeStyle{

        void changeLikeStyle(boolean congestion, boolean avCost, boolean highSpeed, boolean avHighSpeed);
    }
}
