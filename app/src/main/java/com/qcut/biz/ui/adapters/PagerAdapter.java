package com.qcut.biz.ui.adapters;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.google.android.material.tabs.TabLayout;
import com.qcut.biz.ui.waiting_list.WaitingListFragment;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    TabLayout tabLayout;

    public PagerAdapter(FragmentManager fm, int NumOfTabs, TabLayout tabLayout) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        this.tabLayout = tabLayout;
    }

    @Override
    public Fragment getItem(int position) {
        WaitingListFragment tab1 = new WaitingListFragment(tabLayout.getTabAt(position).getTag().toString());
        return tab1;
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}