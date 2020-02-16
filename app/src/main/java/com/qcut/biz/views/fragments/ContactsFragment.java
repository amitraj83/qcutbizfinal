package com.qcut.biz.views.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qcut.biz.R;

import static android.content.Context.MODE_PRIVATE;


public class ContactsFragment extends Fragment {

    private FirebaseDatabase database = null;
    private String userid;
    private SharedPreferences sp;
    private TextView phoneTV;
    private TextView emailTV;
    private TextView gmapTV;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        database = FirebaseDatabase.getInstance();
        sp = getContext().getSharedPreferences("login", MODE_PRIVATE);
        userid = sp.getString("userid", null);
        View root = inflater.inflate(R.layout.fragment_contacts, container, false);

        phoneTV = root.findViewById(R.id.profile_contact_phone);
        emailTV = root.findViewById(R.id.profile_contact_email);
        gmapTV = root.findViewById(R.id.profile_contact_gmap);

        populateData();

        return root;
    }

    private void populateData() {
        DatabaseReference shopDetailsRef = database.getReference().child("barbershops").child(userid);
        shopDetailsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    DataSnapshot email = dataSnapshot.child("email");
                    if(email != null && email.getValue() != null) {
                        emailTV.setText(email.getValue().toString());
                    }
                    DataSnapshot phone = dataSnapshot.child("phone");
                    if(phone != null && phone.getValue() != null) {
                        phoneTV.setText(phone.getValue().toString());
                    }
                    DataSnapshot gmaplink = dataSnapshot.child("gmaplink");
                    if(gmaplink != null && gmaplink.getValue() != null) {
                        gmapTV.setText(gmaplink.getValue().toString());
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        }

}
