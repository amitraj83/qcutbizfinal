package com.qcut.biz.views.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qcut.biz.R;
import com.qcut.biz.models.GoOnlineModel;
import com.qcut.biz.models.ServicePriceModel;
import com.qcut.biz.util.ViewUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class ShopAddServicesFragment extends Fragment {

    private GoOnlineModel goOnlineModel;
    Button addService;

    private FirebaseDatabase database = null;
    private String userid;
    private SharedPreferences sp;
    private ListView servicesListView;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        goOnlineModel =
                ViewModelProviders.of(this).get(GoOnlineModel.class);
        View root = inflater.inflate(R.layout.fragment_shop_add_services, container, false);

        database = FirebaseDatabase.getInstance();
        sp = getContext().getSharedPreferences("login", MODE_PRIVATE);
        userid = sp.getString("userid", null);


        final LayoutInflater factory = LayoutInflater.from(getContext());
        final View addServiceView = factory.inflate(R.layout.add_service_dialog, null);
        final AlertDialog addServiceDialog = new AlertDialog.Builder(getContext()).create();
        addServiceDialog.setView(addServiceView);

        servicesListView = root.findViewById(R.id.add_service_list_services);
        showList();

        addService = root.findViewById(R.id.add_service);
        addService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addServiceDialog.show();
                addServiceDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                addServiceDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 750);
                enableButtonListers(addServiceView, addServiceDialog);

            }
        });


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
                servicesListView.setAdapter(customAdapter);
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

    private void enableButtonListers(View addServiceView, final AlertDialog addServiceDialog ) {
        final EditText serviceName = addServiceDialog.findViewById(R.id.add_service_name);
        final EditText servicePrice = addServiceDialog.findViewById(R.id.add_service_price);

        Button yesButton = (Button) addServiceDialog.findViewById(R.id.add_service_yes_button);
        Button noButton = (Button) addServiceDialog.findViewById(R.id.add_service_cancel_button);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference servicesRef = database.getReference().child("barbershops").child(userid)
                        .child("services");
                Map<String, Object> map = new HashMap<>();
                if(serviceName != null && serviceName.getText() != null
                    && servicePrice != null && servicePrice.getText() != null
                && servicesRef != null) {
                    if(ViewUtils.notEmpty(serviceName) && ViewUtils.notEmpty(servicePrice)) {
                        String serName = serviceName.getText().toString();
                        String priName = servicePrice.getText().toString();
                        map.put(serName, priName);
                        servicesRef.updateChildren(map);
                        addServiceDialog.dismiss();
                        Toast.makeText(getContext(), "Service Added Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed - Invalid service name or price", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addServiceDialog.dismiss();
            }
        });

    }

}