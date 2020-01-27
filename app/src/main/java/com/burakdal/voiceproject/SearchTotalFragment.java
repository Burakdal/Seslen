package com.burakdal.voiceproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.burakdal.voiceproject.utils.ViewPagerAdapter;

import java.lang.ref.PhantomReference;
import java.util.ArrayList;

public class SearchTotalFragment extends Fragment {
    private final String TAG="SearchTotalFragment";




    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;
    private MainActivity mMainActivity;
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private SharedPreferences mPrefs;
    private ArrayList<String> mFragmentTitles=new ArrayList<>();
    private ArrayList<Fragment> mFragmentList=new ArrayList<>();
    private SearchFragment mSearchFragment=new SearchFragment();
    private SearchPlaceFragment mSearchPlaceFragment=new SearchPlaceFragment();
    private SearchPeopleFragment mSearchPeopleFragment=new SearchPeopleFragment();
    private SearchView mSearchView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.search_total_layout,container,false);
        mToolbar=(Toolbar)view.findViewById(R.id.search_total_toolbar);

        mToolbar.inflateMenu(R.menu.toolbar_menu);
        mSearchView=(SearchView) mToolbar.getMenu().findItem(R.id.search).getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        mTabLayout=(TabLayout)view.findViewById(R.id.tabLayout);

        mViewPager=view.findViewById(R.id.search_view_pager);
        mViewPager.setOffscreenPageLimit(2);

        initAdapter();






        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mPrefs=context.getSharedPreferences(getString(R.string.tag_searchtotal_fragment),Context.MODE_PRIVATE);

    }

    private void initAdapter(){
        mFragmentTitles.add(getString(R.string.title_search));
        mFragmentTitles.add(getString(R.string.title_search_people));
        mFragmentTitles.add(getString(R.string.title_search_places));

        mFragmentList.add(mSearchFragment);
        mFragmentList.add(mSearchPeopleFragment);
        mFragmentList.add(mSearchPlaceFragment);

        mViewPagerAdapter=new ViewPagerAdapter(getFragmentManager(),mFragmentTitles,mFragmentList);
        mViewPager.setAdapter(mViewPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);


    }
//    private void initToolbar(){
//        mToolbar.inflateMenu(R.menu.map_toolbar_menu);
//        mToolbar.setTitle("Search");
//        SearchView searchView=(SearchView) mToolbar.getMenu().findItem(R.id.search_view_map).getActionView();
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String s) {
//                Toast.makeText(getActivity(),"SEARCHED",Toast.LENGTH_SHORT).show();
//                mPrefs.edit().putString(getString(R.string.searched_word),s).apply();
//
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String s) {
//                return false;
//            }
//        });
//    }
}
