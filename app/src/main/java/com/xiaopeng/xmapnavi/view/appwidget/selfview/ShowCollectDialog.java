package com.xiaopeng.xmapnavi.view.appwidget.selfview;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.xiaopeng.lib.utils.utils.LogUtils;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.bean.CollectItem;
import com.xiaopeng.xmapnavi.mode.DateHelper;
import com.xiaopeng.xmapnavi.presenter.callback.OnClickRightItem;
import com.xiaopeng.xmapnavi.presenter.callback.XpCollectListener;
import com.xiaopeng.xmapnavi.view.appwidget.adapter.CollectShowAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linzx on 2016/11/16.
 */

public class ShowCollectDialog implements AdapterView.OnItemClickListener,View.OnClickListener,XpCollectListener{
    private final static String TAG ="ShowCollectDialog";
    private Activity mContext;
    private View mRootView;
    private ListView mLv;
    private List<CollectItem> mDate;
    private CollectShowAdapter mCSAdapter;
    private CollectDialogListener mListener;
    private Dialog mDialog;
    private DateHelper mDateHelper;
    public ShowCollectDialog(Activity context){
        mContext = context;
        init();
    }
    public void setDate(List<CollectItem> date){
        mDate.clear();
        mDate.addAll(date);
        mCSAdapter.setData(mDate);
        mCSAdapter.notifyDataSetChanged();
    }
    public void setCollectDialogListener(CollectDialogListener listener){
        mListener = listener;
    }
    private void init(){
        //TODO
        mDateHelper = new DateHelper();
        mDateHelper.setOnCollectListener(this);
        mDate = new ArrayList<>();
        mDialog     = new Dialog(mContext, R.style.navi_dialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);  //无title

        mRootView   = mContext.getLayoutInflater().inflate(R.layout.layout_dialog_show_collect,null);
        mLv         = (ListView) findViewById(R.id.lv_show_collect);
        mCSAdapter  = new CollectShowAdapter(mContext,R.layout.layout_dialog_show_collect);
        mCSAdapter  .setRightLisener(listener);
        mLv         .setAdapter(mCSAdapter);
        mLv         .setOnItemClickListener(this);
        mLv         .setDividerHeight(1);
        mLv         .setDivider(mContext.getResources().getDrawable(R.drawable.gray_button_background));
        findViewById(R.id.rl_out_side).setOnClickListener(this);
        findViewById(R.id.btn_add_poi).setOnClickListener(this);
        mDialog     .setContentView(mRootView);
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = 1080;
        params.height= 1440;
        params.y = 0;
        params.x = 0;
        window.setAttributes(params);
    }

    private View findViewById(int id){
        return mRootView.findViewById(id);
    }
    public void show(){
        if (mDialog!=null){
            mDialog.show();
        }
        if (mLv!=null){
            mLv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        showHide();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            },800);
        }
    }

    private void showHide(){
        try {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            //隐藏软键盘
            imm.hideSoftInputFromWindow(mLv.getWindowToken(), 0);
            //显示软键盘
//        imm.showSoftInputFromInputMethod(mEtSearch.getWindowToken(), 0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void dismiss(){
        if (mDialog!=null){
            mDialog.dismiss();
        }
    }

    private OnClickRightItem listener = new OnClickRightItem() {
        @Override
        public void onClickRightItem(int posi) {
            LogUtils.d(TAG,"onClickRightItem:"+posi);
            CollectItem collectItem  = mDate.get(posi);
            collectItem.delete();
            if (collectItem.style == CollectItem.STYLE_STUB){
                mDateHelper.deletPowerPointById(collectItem.styleMsg);
            }
            mDateHelper.getCollectItems();
        }
    };


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position>=mDate.size())return;
        CollectItem collectItem = mDate.get(position);
        if (mListener!=null){
            mListener.onClickCollectItem(position,collectItem);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_out_side:
                dismiss();
                break;

            case R.id.btn_add_poi:
                if (mListener!=null){
                    mListener.addPoiCollect();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onCollectCallBack(List<CollectItem> collectItems) {
        this.setDate(collectItems);
    }

    public interface CollectDialogListener{
        void onClickCollectItem(int position,CollectItem item);
        void addPoiCollect();
    }
}
