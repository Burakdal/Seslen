package com.burakdal.voiceproject;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.burakdal.voiceproject.utils.ViewPagerAdapter;

import java.util.ArrayList;

public class HomeTotal extends Fragment {
    private final String TAG="HomeTotalFragment";
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;
    private Home mHomeFragment=new Home();
    private HomeFollowings mHomeFollowings=new HomeFollowings();
    private Toolbar mToolbar;


    private ArrayList<String> mFragmentTitles=new ArrayList<>();
    private ArrayList<Fragment> mFragmentList=new ArrayList<>();
    private IMainActivity mInterface;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mInterface=(IMainActivity)context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.home_total_layout,container,false);
        mToolbar=(Toolbar)view.findViewById(R.id.home_total_toolbar);
        mToolbar.setTitle("Seslen");
        mToolbar.inflateMenu(R.menu.home_total_menu);
        mToolbar.getMenu().findItem(R.id.messages).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(getActivity(),"messages",Toast.LENGTH_SHORT).show();
                mInterface.inflateMessageThreads();
                return false;
            }
        });
        mTabLayout=(TabLayout)view.findViewById(R.id.home_total_tab_layout);
        mViewPager=(ViewPager)view.findViewById(R.id.home_total_view_pager);

        initAdapter();
        return view;
    }
    private void initAdapter(){
        mFragmentTitles.add(getString(R.string.title_home_locations));
        mFragmentTitles.add(getString(R.string.title_home_followings));

        mFragmentList.add(mHomeFragment);
        mFragmentList.add(mHomeFollowings);

        mViewPagerAdapter=new ViewPagerAdapter(getFragmentManager(),mFragmentTitles,mFragmentList);
        mViewPager.setAdapter(mViewPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);


    }
}
