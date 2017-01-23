package com.xiaopeng.xmapnavi.view.appwidget.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.view.appwidget.activity.BaseFuncActivityInteface;
import com.xiaopeng.xmapnavi.view.appwidget.activity.MainActivity;
import com.xiaopeng.xmapnavi.view.appwidget.adapter.FragmentViewPagerAdapter;
import com.xiaopeng.xmapnavi.view.appwidget.fragment.second.SettingFirstFragment;
import com.xiaopeng.xmapnavi.view.appwidget.fragment.second.SettingSecondFragment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by linzx on 2016/11/30.
 */

public class SettingFragment extends Fragment implements View.OnClickListener{
    private View rootView;
    private static final String TAG = "SettingFragment";
    private static final int FIRST_FRAME = 0,SECOND_FRAME = 1;
    private ImageView mImgFirst,mImgSecond;
    private TextView mTvFirst,mTvSecond;
    private ViewPager mViewPager;
    private List<Fragment> mFragments;
    private BaseFuncActivityInteface mActivityInteface;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragments = new ArrayList<>();
        mFragments.add(new SettingFirstFragment());
        mFragments.add(new SettingSecondFragment());
        mActivityInteface = (MainActivity)getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_setting,container,false);
        initView();
        return rootView;
    }

    private View findViewById(int id){
        return rootView.findViewById(id);
    }

    private void initView(){
        mImgFirst       = (ImageView) findViewById(R.id.iv_first_view);
        mImgSecond      = (ImageView) findViewById(R.id.iv_second_view);
        mTvFirst        = (TextView) findViewById(R.id.tx_first_view);
        mTvSecond       = (TextView) findViewById(R.id.tx_second_view);
        mViewPager      = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(new MyFragmentAdapter(getChildFragmentManager()));
        findViewById(R.id.btn_return).setOnClickListener(this);
        findViewById(R.id.office_map).setOnClickListener(this);

        findViewById(R.id.second_framelayout).setOnClickListener(this);
        findViewById(R.id.rl_out_side).setOnClickListener(this);
        mTvFirst        .setOnClickListener(this);
        mTvSecond       .setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_return:
                mActivityInteface.exitFragment();
                break;

            case R.id.tx_first_view:
                mViewPager.setCurrentItem(0,false);
                break;

            case R.id.tx_second_view:
                mViewPager.setCurrentItem(1,false);
                break;

            case R.id.office_map:
                if(LocationProvider.getInstence(getActivity()).isNetworkAvailable()) {
                    mActivityInteface.startFragment(new OfflineMapFragment());
                }else {
                    Toast.makeText(getActivity(),"暂无网络连接，请稍后再试",Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.rl_out_side:
                mActivityInteface.exitFragment();
                break;

        }
    }


    class MyFragmentAdapter extends FragmentViewPagerAdapter implements ViewPager.OnPageChangeListener {
        HashMap<Integer, Fragment> fragmentHashMap = new HashMap();

        public MyFragmentAdapter(FragmentManager fm) {
//            super(fm);
            super(fm, mViewPager, mFragments);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public void onPageScrolled(int i, float v, int i2) {
            super.onPageScrolled(i, v, i2);
        }

        @Override
        public void onPageScrollStateChanged(int i) {
            super.onPageScrollStateChanged(i);
        }

        @Override
        public void onPageSelected(int i) {
            super.onPageSelected(i);
            switch (i){
                case FIRST_FRAME:
                    mTvFirst.setTextColor(getResources().getColor(R.color.text_blue));
                    mTvSecond.setTextColor(getResources().getColor(R.color.first_text_color));
                    mImgFirst.setBackgroundColor(getResources().getColor(R.color.text_blue));
                    mImgSecond.setBackgroundColor(getResources().getColor(R.color.lucensy));
                    break;

                case SECOND_FRAME:
                    mTvSecond.setTextColor(getResources().getColor(R.color.text_blue));
                    mTvFirst.setTextColor(getResources().getColor(R.color.first_text_color));
                    mImgSecond.setBackgroundColor(getResources().getColor(R.color.text_blue));
                    mImgFirst.setBackgroundColor(getResources().getColor(R.color.lucensy));
                    break;

            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
