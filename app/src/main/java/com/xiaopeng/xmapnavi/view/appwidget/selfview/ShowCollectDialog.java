package com.xiaopeng.xmapnavi.view.appwidget.selfview;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.bean.CollectItem;
import com.xiaopeng.xmapnavi.view.appwidget.adapter.CollectShowAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linzx on 2016/11/16.
 */

public class ShowCollectDialog implements AdapterView.OnItemClickListener,View.OnClickListener{
    private Activity mContext;
    private View mRootView;
    private ListView mLv;
    private List<CollectItem> mDate;
    private CollectShowAdapter mCSAdapter;
    private CollectDialogListener mListener;
    private Dialog mDialog;
    public ShowCollectDialog(Activity context){
        mContext = context;
        init();
    }
    public void setDate(List<CollectItem> date){
        mDate.clear();
        mDate.addAll(date);
        mCSAdapter.setData(mDate);
    }
    public void setCollectDialogListener(CollectDialogListener listener){
        mListener = listener;
    }
    private void init(){
        //TODO
        mDate = new ArrayList<>();
        mDialog     = new Dialog(mContext);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);  //无title
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = 600;
        params.height= 400;
        params.y = 600;
        window.setAttributes(params);
        mRootView   = mContext.getLayoutInflater().inflate(R.layout.layout_dialog_show_collect,null);
        mLv         = (ListView) findViewById(R.id.lv_show_collect);
        mCSAdapter  = new CollectShowAdapter(mContext,R.layout.layout_dialog_show_collect);
        mLv         .setAdapter(mCSAdapter);
        mLv         .setOnItemClickListener(this);
        findViewById(R.id.rl_out_side).setOnClickListener(this);
        mDialog     .setContentView(mRootView);
    }

    private View findViewById(int id){
        return mRootView.findViewById(id);
    }
    public void show(){
        if (mDialog!=null){
            mDialog.show();
        }
    }
    public void dismiss(){
        if (mDialog!=null){
            mDialog.dismiss();
        }
    }


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

            default:
                break;
        }
    }

    public interface CollectDialogListener{
        void onClickCollectItem(int position,CollectItem item);
    }
}
