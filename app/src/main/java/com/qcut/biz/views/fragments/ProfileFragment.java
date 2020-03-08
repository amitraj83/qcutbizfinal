package com.qcut.biz.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.qcut.biz.R;
import com.qcut.biz.adaptors.SectionPagerAdapter;
import com.qcut.biz.models.ProfileViewModel;
import com.qcut.biz.presenters.fragments.ProfilePresenter;
import com.qcut.biz.views.ProfileView;

public class ProfileFragment extends Fragment implements ProfileView {

    private ProfilePresenter presenter;
    private ProfileViewModel profileViewModel;
    private TextView profileNameTV;
    private TextView profileAddress1TV;
    private TextView profileAddress2TV;
    private TextView cityTV;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        if (presenter == null) {
            presenter = new ProfilePresenter(this, getContext());
            TabLayout tabLayout = root.findViewById(R.id.tablayout);
            ViewPager viewPager = root.findViewById(R.id.viewPager);
            viewPager.setAdapter(new SectionPagerAdapter(getChildFragmentManager(), 3));
            tabLayout.setupWithViewPager(viewPager);

            profileNameTV = root.findViewById(R.id.profile_shop_name);
            profileAddress1TV = root.findViewById(R.id.profile_address_1);
            profileAddress2TV = root.findViewById(R.id.profile_address_2);
            cityTV = root.findViewById(R.id.profile_city);
        }
        presenter.initializeData();
        return root;
    }

    @Override
    public void setShopName(String shopName) {
        profileNameTV.setText(shopName);
    }

    @Override
    public void setAddressLine1(String addressLine1) {
        profileAddress1TV.setText(addressLine1);
    }

    @Override
    public void setAddressLine2(String addressLine2) {
        profileAddress2TV.setText(addressLine2);
    }

    @Override
    public void setCity(String city) {
        cityTV.setText(city);
    }
}

