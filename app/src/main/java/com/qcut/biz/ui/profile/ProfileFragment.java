package com.qcut.biz.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.qcut.biz.R;
import com.qcut.biz.ui.adapters.SectionPagerAdapter;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        TabLayout tabLayout = (TabLayout) root.findViewById(R.id.tablayout);
        ViewPager viewPager = (ViewPager) root.findViewById(R.id.viewPager);


        viewPager.setAdapter(new SectionPagerAdapter(getChildFragmentManager(), 3));
        tabLayout.setupWithViewPager(viewPager);

        return root;
    }
}

