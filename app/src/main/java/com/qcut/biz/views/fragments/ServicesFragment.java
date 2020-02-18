package com.qcut.biz.views.fragments;

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
import com.qcut.biz.models.ServicePriceModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class ServicesFragment extends Fragment {

    ListView mListView;
    String[] mNames, mPrices;
    private FirebaseDatabase database = null;
    private String userid;
    private SharedPreferences sp;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_services, container, false);
        database = FirebaseDatabase.getInstance();
        sp = getContext().getSharedPreferences("login", MODE_PRIVATE);
        userid = sp.getString("userid", null);
        //Initialize ListView
        mListView = root.findViewById(R.id.add_service_list_services);

        showList();

        return root;
    }

    private void showList() {

        final DatabaseReference servicesRef = database.getReference().child("barbershops").child(userid)
                .child("services");
        servicesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                List<ServicePriceModel> spList = new ArrayList<>();
                while (iterator.hasNext()) {
                    DataSnapshot next = iterator.next();
                    String sName = next.getKey();
                    String pName = "n/a";
                    if(next.getValue() != null) {
                        pName = String.valueOf(next.getValue());
                    }
                    spList.add(new ServicePriceModel(sName, pName));
                }
                CustomAdapter customAdapter = new CustomAdapter(spList);
                mListView.setAdapter(customAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    class CustomAdapter extends BaseAdapter {

        List<ServicePriceModel> spList;

        public CustomAdapter(List<ServicePriceModel> spList){
            this.spList = spList;
        }

        @Override
        public int getCount() {
            return spList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            @SuppressLint("ViewHolder")
            View listView_layout = getLayoutInflater().inflate(R.layout.services_list_item, null);

            TextView mServiceName = listView_layout.findViewById(R.id.service_name);
            TextView mServicePrice = listView_layout.findViewById(R.id.service_price);

            mServiceName.setText(spList.get(i).getName());
            mServicePrice.setText(spList.get(i).getPrice());

            return listView_layout;
        }
    }

}
