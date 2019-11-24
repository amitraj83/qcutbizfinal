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


public class HoursFragment extends Fragment {

    ListView mListView;
    String[] mDays, mHours;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_services, container, false);

        // Inflate the layout for this fragment
        mDays = new String[] {
                "Monday",
                "Tuesday",
                "Wednesday",
                "Thursday",
                "Friday",
                "Saturday",
                "Sunday",
        };

        mHours = new String[] {
                "10:00 AM - 6:00 PM",
                "10:00 AM - 6:00 PM",
                "10:00 AM - 6:00 PM",
                "10:00 AM - 6:00 PM",
                "10:00 AM - 6:00 PM",
                "10:00 AM - 6:00 PM",
                "10:00 AM - 6:00 PM",
        };

        //Initialize ListView
        mListView = root.findViewById(R.id.add_service_list_services);
        mListView.setDivider(null);
        HoursFragment.CustomAdapter customAdapter = new HoursFragment.CustomAdapter();
        mListView.setAdapter(customAdapter);

        return root;
    }

    class CustomAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mDays.length;
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

            @SuppressLint("ViewHolder") View listView_layout = getLayoutInflater().inflate(R.layout.hours_list_item, null);

            TextView mDays = listView_layout.findViewById(R.id.days);
            TextView mHours = listView_layout.findViewById(R.id.hours);

            mDays.setText(HoursFragment.this.mDays[i]);
            mHours.setText(HoursFragment.this.mHours[i]);

            return listView_layout;
        }
    }

}
