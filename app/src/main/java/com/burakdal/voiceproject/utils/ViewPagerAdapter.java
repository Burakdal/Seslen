package com.burakdal.voiceproject.utils;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.burakdal.voiceproject.SearchFragment;
import com.burakdal.voiceproject.SearchPeopleFragment;
import com.burakdal.voiceproject.SearchPlaceFragment;

import java.util.ArrayList;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    ArrayList<Fragment> mFragments=new ArrayList<>();
    ArrayList<String> mFragmentTitles=new ArrayList<>();

    public ViewPagerAdapter(FragmentManager fm,ArrayList<String> fragmentTitles,ArrayList<Fragment> fragmentList) {
        super(fm);
        mFragments=fragmentList;
        mFragmentTitles=fragmentTitles;




    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        return mFragmentTitles.get(position);
    }

    @Override
    public Fragment getItem(int i) {

        return mFragments.get(i);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}
