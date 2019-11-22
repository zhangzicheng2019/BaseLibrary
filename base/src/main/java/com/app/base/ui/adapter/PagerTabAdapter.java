package com.app.base.ui.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class PagerTabAdapter extends FragmentStatePagerAdapter {

    private FragmentManager fragmentManager;

    //添加的Fragment的集合
    private final List<Fragment> mFragments = new ArrayList<>();

    //每个Fragment对应的title的集合
    private final List<String> mFragmentsTitles = new ArrayList<>();

    public PagerTabAdapter(FragmentManager fm) {
        super(fm);
        fragmentManager = fm;
    }

    public void addFragment(Fragment fragment, String fragmentTitle) {
        // 添加Fragment
        mFragments.add(fragment);
        //Fragment的标题，即TabLayout中对应Tab的标题
        mFragmentsTitles.add(fragmentTitle);
    }

    @Override
    public Fragment getItem(int position) {
        //得到对应position的Fragment
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        //返回Fragment的数量
        return mFragments.size();
    }


    @Override
    public CharSequence getPageTitle(int position) {
        //得到对应position的Fragment的title
        return mFragmentsTitles.get(position);
    }
}

