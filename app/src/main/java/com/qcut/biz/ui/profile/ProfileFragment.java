package com.qcut.biz.ui.profile;

import android.content.SharedPreferences;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qcut.biz.R;
import com.qcut.biz.ui.adapters.SectionPagerAdapter;

import java.util.Arrays;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private TextView profileNameTV;
    private TextView profileAddress1TV;
    private TextView profileAddress2TV;
    private TextView cityTV;
    private FirebaseDatabase database = null;
    private SharedPreferences sp;
    private String userid;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        TabLayout tabLayout = (TabLayout) root.findViewById(R.id.tablayout);
        ViewPager viewPager = (ViewPager) root.findViewById(R.id.viewPager);
        database = FirebaseDatabase.getInstance();

        sp = getContext().getSharedPreferences("login", MODE_PRIVATE);
        userid = sp.getString("userid", null);

        viewPager.setAdapter(new SectionPagerAdapter(getChildFragmentManager(), 3));
        tabLayout.setupWithViewPager(viewPager);

        profileNameTV = root.findViewById(R.id.profile_shop_name);
        profileAddress1TV = root.findViewById(R.id.profile_address_1);
        profileAddress2TV = root.findViewById(R.id.profile_address_2);
        cityTV = root.findViewById(R.id.profile_city);

        populateData();


        return root;
    }

    private void populateData() {
        DatabaseReference shopDetailsRef = database.getReference().child("barbershops").child(userid);
        shopDetailsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {

                    DataSnapshot shopName = dataSnapshot.child("shopname");
                    if(shopName != null && shopName.getValue() !=null) {
                        profileNameTV.setText(String.valueOf(shopName.getValue()));
                    }

                    DataSnapshot addressLine1 = dataSnapshot.child("addressLine1");
                    if(addressLine1 != null && addressLine1.getValue() !=null) {
                        profileAddress1TV.setText(String.valueOf(addressLine1.getValue()));
                    }

                    DataSnapshot addressLine2 = dataSnapshot.child("addressLine2");
                    if(addressLine2 != null && addressLine2.getValue() !=null) {
                        profileAddress2TV.setText(String.valueOf(addressLine2.getValue()));
                    }
                    DataSnapshot city = dataSnapshot.child("city");
                    if(city != null && city.getValue() !=null) {
                        cityTV.setText(String.valueOf(city.getValue()));
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

