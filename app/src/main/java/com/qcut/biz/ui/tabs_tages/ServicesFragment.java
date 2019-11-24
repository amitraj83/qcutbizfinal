package com.qcut.biz.ui.tabs_tages;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.qcut.biz.R;


public class ServicesFragment extends Fragment {

    ListView mListView;
    String[] mNames, mPrices;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_services, container, false);

        // Inflate the layout for this fragment
        mNames = new String[] {
                "Hair Cut",
                "Hair Cut 2",
                "Hair Cut 3",
                "Hair Cut 4",
                "Hair Cut 5",
                "Hair Cut 6",
                "Hair Cut 7",
                "Hair Cut 8",
                "Hair Cut 9",
                "Hair Cut 10",
        };

        mPrices = new String[] {
                "$ 10.00",
                "$ 11.00",
                "$ 12.00",
                "$ 13.00",
                "$ 14.00",
                "$ 15.00",
                "$ 16.00",
                "$ 17.00",
                "$ 18.00",
                "$ 19.00",
                "$ 20.00",
        };

        //Initialize ListView
        mListView = root.findViewById(R.id.add_service_list_services);
        CustomAdapter customAdapter = new CustomAdapter();
        mListView.setAdapter(customAdapter);

        return root;
    }

    class CustomAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mNames.length;
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

            @SuppressLint("ViewHolder") View listView_layout = getLayoutInflater().inflate(R.layout.services_list_item, null);

            TextView mServiceName = listView_layout.findViewById(R.id.service_name);
            TextView mServicePrice = listView_layout.findViewById(R.id.service_price);

            mServiceName.setText(mNames[i]);
            mServicePrice.setText(mPrices[i]);

            return listView_layout;
        }
    }
}
