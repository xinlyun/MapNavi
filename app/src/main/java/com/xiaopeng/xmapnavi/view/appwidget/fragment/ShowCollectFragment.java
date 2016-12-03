package com.xiaopeng.xmapnavi.view.appwidget.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.xiaopeng.lib.bughunter.BugHunter;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.bean.CollectItem;
import com.xiaopeng.xmapnavi.presenter.callback.XpCollectListener;
import com.xiaopeng.xmapnavi.presenter.collect.IShowCollectPresenter;
import com.xiaopeng.xmapnavi.presenter.collect.ShowCollectPresenter;
import com.xiaopeng.xmapnavi.view.appwidget.activity.BaseFuncActivityInteface;
import com.xiaopeng.xmapnavi.view.appwidget.adapter.CollectShowAdapter;

import java.util.List;

/**
 * Created by linzx on 2016/11/8.
 */
public class ShowCollectFragment extends Fragment implements View.OnClickListener
        ,XpCollectListener ,AdapterView.OnItemClickListener{
    private static final String TAG = "ShowCollectFragment";
    private View rootView;
    private ListView mListView;
    private IShowCollectPresenter mPresenter;
    private List<CollectItem> mCollectItems;
    private CollectShowAdapter mAdapter;
    private ProgressDialog mProgDialog;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        BugHunter.countTimeStart(BugHunter.TIME_TYPE_START,TAG,BugHunter.SWITCH_TYPE_START_COOL);
        super.onCreate(savedInstanceState);
        mPresenter = new ShowCollectPresenter(getActivity());
        mPresenter.init();
        mPresenter.setOnCollectListener(this);
        mAdapter = new CollectShowAdapter(getActivity(),R.layout.layout_fix_collect);


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_show_collect,container,false);
        initView();
//        rootView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
        mProgDialog = new ProgressDialog(ShowCollectFragment.this.getActivity());
        mProgDialog.setTitle("正在搜索数据");
        mProgDialog.setMessage("正在搜索相关信息....");
        mProgDialog.setCancelable(true);
        //----init listener ---//
        mProgDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });
//            }
//        },500);
        return rootView;
    }



    private View findViewById(int id ){
        return rootView.findViewById(id);
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.getCollect();
    }

    @Override
    public void onResume() {
        super.onResume();
        BugHunter.countTimeEnd(getActivity().getApplication(),BugHunter.TIME_TYPE_START,TAG,BugHunter.SWITCH_TYPE_START_COOL);
    }

    private void initView(){
        TextView txTitle = (TextView) findViewById(R.id.title_title);
        txTitle.setText(R.string.collect_poi);
        txTitle.setOnClickListener(this);
        findViewById(R.id.title_return).setOnClickListener(this);
        mListView = (ListView) findViewById(R.id.lv_show_collect);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.title_return:
                //down
            case R.id.title_title:
                ((BaseFuncActivityInteface)getActivity()).exitFragment();
                break;

        }
    }

    @Override
    public void onCollectCallBack(List<CollectItem> collectItems) {
        mCollectItems = collectItems;
        mAdapter.setData(collectItems);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (mCollectItems==null || mCollectItems.size()<=i)return;
        CollectItem collectItem = mCollectItems.get(i);
        mProgDialog.show();
        rootView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgDialog.dismiss();
            }
        },6 * 1000);
        mPresenter.startNavi(collectItem);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.release();
    }
}
