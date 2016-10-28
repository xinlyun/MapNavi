package com.xiaopeng.xmapnavi.view.appwidget.selfview;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;

//import com.gc.materialdesign.views.CheckBox;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;

/**
 * Created by linzx on 2016/10/27.
 */
public class NaviChanDialog  implements CheckBox.OnCheckedChangeListener,View.OnClickListener{
    Context context;
    Dialog mDialog;

    private CheckBox mCbxCongestion,mCbxAvoidHightSpeed,mCbxHightSpeed,mCbxCost;
    private ILocationProvider mLocationPro;
    private OnChioceNaviStyleListner mListner;
    public NaviChanDialog(Context context){
        this.context = context;
        mLocationPro = LocationProvider.getInstence(context);
        mDialog = new Dialog(context,R.style.navi_dialog);
        mDialog . requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_change_navi_style,null);
        mCbxAvoidHightSpeed = (CheckBox) view.findViewById(R.id.cbx_avoidhightspeed);
        mCbxCongestion = (CheckBox) view.findViewById(R.id.cbx_congestion);
        mCbxCost = (CheckBox) view.findViewById(R.id.cbx_cost);
        mCbxHightSpeed = (CheckBox) view.findViewById(R.id.cbx_hightspeed);

        mCbxHightSpeed.setOnCheckedChangeListener(this);
        mCbxAvoidHightSpeed.setOnCheckedChangeListener(this);
        mCbxCost.setOnCheckedChangeListener(this);
        mCbxCongestion.setOnCheckedChangeListener(this);
        view.findViewById(R.id.btn_exit).setOnClickListener(this);
        view.findViewById(R.id.btn_enter).setOnClickListener(this);
        mDialog.setContentView(view);

        Window dialogWindow = mDialog.getWindow();

        WindowManager.LayoutParams lp = dialogWindow.getAttributes();

        lp.height  = 680;
        lp.width = 430;
        lp.x = -45;
        lp.y = 0;
        dialogWindow.setAttributes(lp);

    }


    public void show(){
        mDialog.show();
    }

    public void dismiss(){
        mDialog.dismiss();
    }




    public void setOnChioceNaviStyleListner(OnChioceNaviStyleListner listner){
        mListner = listner;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.btn_exit:
                mDialog.dismiss();
                break;

            case R.id.btn_enter:
                if (mListner!=null){
                    mListner.onChioceNaviStyle(mCbxCongestion.isChecked(),mCbxAvoidHightSpeed.isChecked(),
                            mCbxCost.isChecked(),mCbxHightSpeed.isChecked());
                }
                mDialog.dismiss();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            switch (compoundButton.getId()) {

                case R.id.cbx_avoidhightspeed:
                    mCbxHightSpeed.setChecked(false);
                    break;

                case R.id.cbx_hightspeed:

                    mCbxAvoidHightSpeed.setChecked(false);
                    mCbxCost.setChecked(false);

                    break;

                case R.id.cbx_cost:
                    mCbxHightSpeed.setChecked(false);
                    break;

                case R.id.cbx_congestion:

                    break;

                default:
                    break;
            }
        }
    }

    public interface OnChioceNaviStyleListner{
        void onChioceNaviStyle(boolean congestion,boolean avHighSpeed,boolean avCost,boolean highSpeed);
    }

}
