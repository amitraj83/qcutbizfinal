package com.qcut.barber.adaptors;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.qcut.barber.views.fragments.ContactsFragment;
import com.qcut.barber.views.fragments.HoursFragment;
import com.qcut.barber.views.fragments.ServicesFragment;

public class SectionPagerAdapter extends FragmentPagerAdapter {


    public SectionPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ServicesFragment();
            case 1:
                return new HoursFragment();
            default:
                return new ContactsFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Services";
            case 1:
                return "Hours";
            default:
                return "Contacts";
        }
    }
}
