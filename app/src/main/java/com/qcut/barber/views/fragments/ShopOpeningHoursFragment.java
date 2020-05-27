package com.qcut.barber.views.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qcut.barber.R;
import com.qcut.barber.models.GoOnlineModel;
import com.qcut.barber.util.ViewUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class ShopOpeningHoursFragment extends Fragment {

    private GoOnlineModel goOnlineModel;
    private Spinner mondayOpen;
    private Spinner mondayClose;
    private Spinner tueOpen;
    private Spinner tueClose;
    private Spinner wedOpen;
    private Spinner wedClose;
    private Spinner thuOpen;
    private Spinner thuClose;
    private Spinner friOpen;
    private Spinner friClose;
    private Spinner satOpen;
    private Spinner satClose;
    private Spinner sunOpen;
    private Spinner sunClose;
    private Button save;

    private FirebaseDatabase database = null;
    private String userid;
    private SharedPreferences sp;
    private String[] openTimes = {"6:00 AM", "6:15 AM", "6:30 AM", "6:45 AM", "7:00 AM", "7:15 AM", "7:30 AM", "7:45 AM", "8:00 AM", "8:15 AM", "8:30 AM", "8:45 AM", "9:00 AM", "9:15 AM", "9:30 AM", "9:45 AM", "10:00 AM", "10:15 AM", "10:30 AM", "10:45 AM", "11:00 AM", "11:15 AM", "11:30 AM", "11:45 AM", "12:00 PM", "12:15 PM", "12:30 PM", "12:45 PM", "1:00 PM", "1:15 PM", "1:30 PM", "1:45 PM", "2:00 PM", "2:15 PM", "2:30 PM", "2:45 PM", "3:00 PM", "3:15 PM", "3:30 PM", "3:45 PM", "4:00 PM", "4:15 PM", "4:30 PM", "4:45 PM", "5:00 PM", "5:15 PM", "5:30 PM", "5:45 PM", "6:00 PM"};
    private String[] closeTimes = {"11:00 AM", "11:15 AM", "11:30 AM", "11:45 AM", "12:00 PM", "12:15 PM", "12:30 PM", "12:45 PM", "1:00 PM", "1:15 PM", "1:30 PM", "1:45 PM", "2:00 PM", "2:15 PM", "2:30 PM", "2:45 PM", "3:00 PM", "3:15 PM", "3:30 PM", "3:45 PM", "4:00 PM", "4:15 PM", "4:30 PM", "4:45 PM", "5:00 PM", "5:15 PM", "5:30 PM", "5:45 PM", "6:00 PM", "6:15 PM", "6:30 PM", "6:45 PM", "7:00 PM", "7:15 PM", "7:30 PM", "7:45 PM", "8:00 PM", "8:15 PM", "8:30 PM", "8:45 PM", "9:00 PM", "9:15 PM", "9:30 PM", "9:45 PM", "10:00 PM", "10:15 PM", "10:30 PM", "10:45 PM", "11:00 PM"};

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        goOnlineModel =
                ViewModelProviders.of(this).get(GoOnlineModel.class);
        View root = inflater.inflate(R.layout.fragment_shop_opening_hours, container, false);
        database = FirebaseDatabase.getInstance();
        mondayOpen = root.findViewById(R.id.monday_spinner_left);
        mondayClose = root.findViewById(R.id.monday_spinner_right);
        tueOpen = root.findViewById(R.id.tuesday_spinner_left);
        tueClose = root.findViewById(R.id.tuesday_spinner_right);
        wedOpen = root.findViewById(R.id.wednesday_spinner_left);
        wedClose = root.findViewById(R.id.wednesday_spinner_right);
        thuOpen = root.findViewById(R.id.thurday_spinner_left);
        thuClose = root.findViewById(R.id.thurday_spinner_right);
        friOpen = root.findViewById(R.id.friday_spinner_left);
        friClose = root.findViewById(R.id.friday_spinner_right);
        satOpen = root.findViewById(R.id.saturday_spinner_left);
        satClose = root.findViewById(R.id.saturday_spinner_right);
        sunOpen = root.findViewById(R.id.sunday_spinner_left);
        sunClose = root.findViewById(R.id.sunday_spinner_right);
        sp = getContext().getSharedPreferences("login", MODE_PRIVATE);
        userid = sp.getString("userid", null);

        ArrayAdapter<String> openTimesAdaper = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, openTimes);
        openTimesAdaper.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mondayOpen.setAdapter(openTimesAdaper);
        tueOpen.setAdapter(openTimesAdaper);
        wedOpen.setAdapter(openTimesAdaper);
        thuOpen.setAdapter(openTimesAdaper);
        friOpen.setAdapter(openTimesAdaper);
        satOpen.setAdapter(openTimesAdaper);
        sunOpen.setAdapter(openTimesAdaper);

        ArrayAdapter<String> closeTimesAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, closeTimes);
        openTimesAdaper.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mondayClose.setAdapter(closeTimesAdapter);
        tueClose.setAdapter(closeTimesAdapter);
        wedClose.setAdapter(closeTimesAdapter);
        thuClose.setAdapter(closeTimesAdapter);
        friClose.setAdapter(closeTimesAdapter);
        satClose.setAdapter(closeTimesAdapter);
        sunClose.setAdapter(closeTimesAdapter);

        save = (Button) root.findViewById(R.id.save_opening_hours);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveOpeningHours();
            }
        });


        populateData();


        return root;
    }

    private void saveOpeningHours() {
        DatabaseReference openingHoursRef = database.getReference().child("barbershops").child(userid);
        Map<String, Object> map = new HashMap<>();
        if(ViewUtils.notEmpty(mondayOpen)){
            map.put("monOpen", String.valueOf(mondayOpen.getSelectedItem()));
        }
        if(ViewUtils.notEmpty(tueOpen)){
            map.put("tueOpen", String.valueOf(tueOpen.getSelectedItem()));
        }
        if(ViewUtils.notEmpty(wedOpen)){
            map.put("wedOpen", String.valueOf(wedOpen.getSelectedItem()));
        }
        if(ViewUtils.notEmpty(thuOpen)){
            map.put("thuOpen", String.valueOf(thuOpen.getSelectedItem()));
        }
        if(ViewUtils.notEmpty(friOpen)){
            map.put("friOpen", String.valueOf(friOpen.getSelectedItem()));
        }
        if(ViewUtils.notEmpty(satOpen)){
            map.put("satOpen", String.valueOf(satOpen.getSelectedItem()));
        }
        if(ViewUtils.notEmpty(sunOpen)){
            map.put("sunOpen", String.valueOf(sunOpen.getSelectedItem()));
        }

        if(ViewUtils.notEmpty(mondayClose)){
            map.put("monClose", String.valueOf(mondayClose.getSelectedItem()));
        }
        if(ViewUtils.notEmpty(tueClose)){
            map.put("tueClose", String.valueOf(tueClose.getSelectedItem()));
        }
        if(ViewUtils.notEmpty(wedClose)){
            map.put("wedClose", String.valueOf(wedClose.getSelectedItem()));
        }
        if(ViewUtils.notEmpty(thuClose)){
            map.put("thuClose", String.valueOf(thuClose.getSelectedItem()));
        }
        if(ViewUtils.notEmpty(friClose)){
            map.put("friClose", String.valueOf(friClose.getSelectedItem()));
        }
        if(ViewUtils.notEmpty(satClose)){
            map.put("satClose", String.valueOf(satClose.getSelectedItem()));
        }
        if(ViewUtils.notEmpty(sunClose)){
            map.put("sunClose", String.valueOf(sunClose.getSelectedItem()));
        }

        openingHoursRef.updateChildren(map);
        Toast.makeText(getContext(),
                "Opening Hours updated successfully", Toast.LENGTH_SHORT).show();

    }

    private void populateData() {
        DatabaseReference shopDetailsRef = database.getReference().child("barbershops").child(userid);
        shopDetailsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    DataSnapshot monOpen = dataSnapshot.child("monOpen");
                    if(monOpen != null && monOpen.getValue() != null) {
                        int selectedIndex = Arrays.asList(openTimes).indexOf(monOpen.getValue().toString());
                        mondayOpen.setSelection(selectedIndex);
                    }
                    DataSnapshot tuOpen = dataSnapshot.child("tueOpen");
                    if(tuOpen != null && tuOpen.getValue() != null) {
                        int selectedIndex = Arrays.asList(openTimes).indexOf(tuOpen.getValue().toString());
                        tueOpen.setSelection(selectedIndex);
                    }
                    DataSnapshot weOpen = dataSnapshot.child("wedOpen");
                    if(weOpen != null && weOpen.getValue() != null) {
                        int selectedIndex = Arrays.asList(openTimes).indexOf(weOpen.getValue().toString());
                        wedOpen.setSelection(selectedIndex);
                    }
                    DataSnapshot thOpen = dataSnapshot.child("thuOpen");
                    if(thOpen != null && thOpen.getValue() != null) {
                        int selectedIndex = Arrays.asList(openTimes).indexOf(thOpen.getValue().toString());
                        thuOpen.setSelection(selectedIndex);
                    }
                    DataSnapshot frOpen = dataSnapshot.child("friOpen");
                    if(frOpen != null && frOpen.getValue() != null) {
                        int selectedIndex = Arrays.asList(openTimes).indexOf(frOpen.getValue().toString());
                        friOpen.setSelection(selectedIndex);
                    }
                    DataSnapshot saOpen = dataSnapshot.child("satOpen");
                    if(saOpen != null && saOpen.getValue() != null) {
                        int selectedIndex = Arrays.asList(openTimes).indexOf(saOpen.getValue().toString());
                        satOpen.setSelection(selectedIndex);
                    }
                    DataSnapshot suOpen = dataSnapshot.child("sunOpen");
                    if(suOpen != null && suOpen.getValue() != null) {
                        int selectedIndex = Arrays.asList(openTimes).indexOf(suOpen.getValue().toString());
                        sunOpen.setSelection(selectedIndex);
                    }

                    DataSnapshot suClose = dataSnapshot.child("sunClose");
                    if(suClose != null && suClose.getValue() != null) {
                        int selectedIndex = Arrays.asList(closeTimes).indexOf(suClose.getValue().toString());
                        sunClose.setSelection(selectedIndex);
                    }
                    DataSnapshot moClose = dataSnapshot.child("monClose");
                    if(moClose != null && moClose.getValue() != null) {
                        int selectedIndex = Arrays.asList(closeTimes).indexOf(moClose.getValue().toString());
                        mondayClose.setSelection(selectedIndex);
                    }
                    DataSnapshot tuClose = dataSnapshot.child("tueClose");
                    if(tuClose != null && tuClose.getValue() != null) {
                        int selectedIndex = Arrays.asList(closeTimes).indexOf(tuClose.getValue().toString());
                        tueClose.setSelection(selectedIndex);
                    }
                    DataSnapshot weClose = dataSnapshot.child("wedClose");
                    if(weClose != null && weClose.getValue() != null) {
                        int selectedIndex = Arrays.asList(closeTimes).indexOf(weClose.getValue().toString());
                        wedClose.setSelection(selectedIndex);
                    }
                    DataSnapshot thClose = dataSnapshot.child("thuClose");
                    if(thClose != null && thClose.getValue() != null) {
                        int selectedIndex = Arrays.asList(closeTimes).indexOf(thClose.getValue().toString());
                        thuClose.setSelection(selectedIndex);
                    }
                    DataSnapshot frClose = dataSnapshot.child("friClose");
                    if(frClose != null && frClose.getValue() != null) {
                        int selectedIndex = Arrays.asList(closeTimes).indexOf(frClose.getValue().toString());
                        friClose.setSelection(selectedIndex);
                    }
                    DataSnapshot saClose = dataSnapshot.child("satClose");
                    if(saClose != null && saClose.getValue() != null) {
                        int selectedIndex = Arrays.asList(closeTimes).indexOf(saClose.getValue().toString());
                        satClose.setSelection(selectedIndex);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}