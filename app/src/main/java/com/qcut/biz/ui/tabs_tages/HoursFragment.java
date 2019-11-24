package com.qcut.biz.ui.tabs_tages;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qcut.biz.R;

import java.util.Arrays;

import static android.content.Context.MODE_PRIVATE;


public class HoursFragment extends Fragment {

    ListView mListView;
    String[] mDays, mHours;
    private FirebaseDatabase database = null;
    private String userid;
    private SharedPreferences sp;
    private TextView sundayTV;
    private TextView mondayTV;
    private TextView tuesdayTV;
    private TextView wednesdayTV;
    private TextView thursdayTV;
    private TextView fridayTV;
    private TextView saturdayTV;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_hours, container, false);
        database = FirebaseDatabase.getInstance();
        sp = getContext().getSharedPreferences("login", MODE_PRIVATE);
        userid = sp.getString("userid", null);
        sundayTV = root.findViewById(R.id.profile_sunday_hours);
        mondayTV = root.findViewById(R.id.profile_monday_hours);
        tuesdayTV = root.findViewById(R.id.profile_tuesday_hours);
        wednesdayTV = root.findViewById(R.id.profile_wednesday_hours);
        thursdayTV = root.findViewById(R.id.profile_thursday_hours);
        fridayTV = root.findViewById(R.id.profile_friday_hours);
        saturdayTV = root.findViewById(R.id.profile_saturday_hours);
        populateData();
        return root;
    }

    private void populateData() {
        DatabaseReference shopDetailsRef = database.getReference().child("barbershops").child(userid);
        shopDetailsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    DataSnapshot monOpen = dataSnapshot.child("monOpen");
                    DataSnapshot moClose = dataSnapshot.child("monClose");
                    if(monOpen != null && monOpen.getValue() != null
                        && moClose != null && moClose.getValue() != null) {
                        mondayTV.setText(monOpen.getValue().toString()+" - "+moClose.getValue().toString());
                    }
                    DataSnapshot tuOpen = dataSnapshot.child("tueOpen");
                    DataSnapshot tuClose = dataSnapshot.child("tueClose");
                    if(tuOpen != null && tuOpen.getValue() != null
                            && tuClose != null && tuClose.getValue() != null) {
                        tuesdayTV.setText(tuOpen.getValue().toString()+" - "+tuClose.getValue().toString());
                    }
                    DataSnapshot weOpen = dataSnapshot.child("wedOpen");
                    DataSnapshot weClose = dataSnapshot.child("wedClose");
                    if(weOpen != null && weOpen.getValue() != null
                        && weClose != null && weClose.getValue() != null) {
                        wednesdayTV.setText(weOpen.getValue().toString()+" - "+weClose.getValue().toString());
                    }
                    DataSnapshot thOpen = dataSnapshot.child("thuOpen");
                    DataSnapshot thClose = dataSnapshot.child("thuClose");
                    if(thOpen != null && thOpen.getValue() != null
                        && thClose != null && thClose.getValue() != null) {
                        thursdayTV.setText(thOpen.getValue().toString()+" - "+thClose.getValue().toString());
                    }
                    DataSnapshot frOpen = dataSnapshot.child("friOpen");
                    DataSnapshot frClose = dataSnapshot.child("friClose");
                    if(frOpen != null && frOpen.getValue() != null
                        && frClose != null && frClose.getValue() != null) {
                        fridayTV.setText(frOpen.getValue().toString()+" - "+frClose.getValue().toString());
                    }
                    DataSnapshot saOpen = dataSnapshot.child("satOpen");
                    DataSnapshot saClose = dataSnapshot.child("satClose");
                    if(saOpen != null && saOpen.getValue() != null
                            && saClose != null && saClose.getValue() != null) {
                        saturdayTV.setText(saOpen.getValue().toString()+" - "+saClose.getValue().toString());
                    }
                    DataSnapshot suOpen = dataSnapshot.child("sunOpen");
                    DataSnapshot suClose = dataSnapshot.child("sunClose");
                    if(suOpen != null && suOpen.getValue() != null && suClose != null && suClose.getValue() != null) {
                        saturdayTV.setText(suOpen.getValue().toString() +" - "+suClose.getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
