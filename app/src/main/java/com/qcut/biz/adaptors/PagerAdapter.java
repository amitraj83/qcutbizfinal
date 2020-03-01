package com.qcut.biz.adaptors;


import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.google.android.material.tabs.TabLayout;
import com.qcut.biz.views.fragments.WaitingListFragment;

import java.util.HashMap;
import java.util.Map;

public class PagerAdapter extends FragmentStatePagerAdapter {
    private TabLayout tabLayout;
    private FragmentManager fm;
    private Map<Integer, WaitingListFragment> map = new HashMap<>();

    public PagerAdapter(FragmentManager fm, TabLayout tabLayout) {
        super(fm);
        this.tabLayout = tabLayout;
        this.fm = fm;
    }

    @Override
    public Fragment getItem(int position) {
        //TODO check if thats correct,
        WaitingListFragment waitingListFragment = new WaitingListFragment(tabLayout.getTabAt(position).getTag().toString());
        map.put(position, waitingListFragment);
        return waitingListFragment;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.destroyItem(container, position, object);
        map.get(position).destroy();
    }

    @Override
    public int getCount() {
        return tabLayout.getTabCount();
    }
}