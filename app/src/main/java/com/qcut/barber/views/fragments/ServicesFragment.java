package com.qcut.barber.views.fragments;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.qcut.barber.R;
import com.qcut.barber.models.ServiceAvailable;
import com.qcut.barber.util.DBUtils;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class ServicesFragment extends Fragment {

    ListView mListView;
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
        DBUtils.getShopServices(database, userid, new OnSuccessListener<List<ServiceAvailable>>() {
            @Override
            public void onSuccess(List<ServiceAvailable> serviceAvailables) {
                CustomAdapter customAdapter = new CustomAdapter(serviceAvailables);
                mListView.setAdapter(customAdapter);
            }
        });
    }

    class CustomAdapter extends BaseAdapter {

        List<ServiceAvailable> spList;

        public CustomAdapter(List<ServiceAvailable> spList) {
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

            mServiceName.setText(spList.get(i).getServiceName());
            mServicePrice.setText(spList.get(i).getServicePrice());

            return listView_layout;
        }
    }

}
