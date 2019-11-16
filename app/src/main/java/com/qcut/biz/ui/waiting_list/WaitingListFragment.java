package com.qcut.biz.ui.waiting_list;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.qcut.biz.R;

public class WaitingListFragment extends Fragment {

    private WaitingListModel waitingListModel;
    ListView mListView;
    String[] mClients, mStatuses;
    Button startService, skipCostumer;
    FloatingActionButton addCustomer;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        waitingListModel =
                ViewModelProviders.of(this).get(WaitingListModel.class);
        View root = inflater.inflate(R.layout.fragment_waiting_list, container, false);

        LayoutInflater factory = LayoutInflater.from(getContext());

        final View startServiceView = factory.inflate(R.layout.start_service_dialog, null);
        final AlertDialog startServiceDialog = new AlertDialog.Builder(getContext()).create();
        startServiceDialog.setView(startServiceView);

        startService = root.findViewById(R.id.start_service);
        startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startServiceDialog.show();
                startServiceDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                startServiceDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 750);
            }
        });

        final View skipCustomerView = factory.inflate(R.layout.skip_customer_dialog, null);
        final AlertDialog skipCustomerDialog = new AlertDialog.Builder(getContext()).create();
        skipCustomerDialog.setView(skipCustomerView);

        skipCostumer = root.findViewById(R.id.skip_customer);
        skipCostumer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipCustomerDialog.show();
                skipCustomerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                skipCustomerDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 750);
            }
        });

        final View addCustomerView = factory.inflate(R.layout.add_customer_dialog, null);
        final AlertDialog addCustomerDialog = new AlertDialog.Builder(getContext()).create();
        addCustomerDialog.setView(addCustomerView);

        addCustomer = root.findViewById(R.id.add_customer_fab);
        addCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCustomerDialog.show();
                addCustomerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                addCustomerDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 750);
            }
        });

        mClients = new String[] {
                "Sandra Adams",
                "Nate Miller",
                "John",
                "Mark Brandson",
                "Mile Mansow",
                "Elena Nomer",
                "Mark",
                "Jane",
                "Frenk",
                "Devid",
        };

        mStatuses = new String[] {
                "IN CHAIR",
                "IN CHAIR",
                "12 MIN",
                "IN CHAIR",
                "24 MIN",
                "3 MIN",
                "IN CHAIR",
                "50 MIN",
                "IN CHAIR",
                "33 MIN",
                "IN CHAIR",
        };

        //Initialize ListView
        mListView = root.findViewById(R.id.waitingListView);
        WaitingListFragment.CustomAdapter customAdapter = new WaitingListFragment.CustomAdapter();
        mListView.setAdapter(customAdapter);

        final View serviceDoneView = factory.inflate(R.layout.service_done_dialog, null);
        final AlertDialog serviceDoneDialog = new AlertDialog.Builder(getContext()).create();
        serviceDoneDialog.setView(serviceDoneView);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                serviceDoneDialog.show();
                serviceDoneDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                serviceDoneDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 750);
            }
        });

        return root;
    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mClients.length;
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

            @SuppressLint("ViewHolder") View listView_layout = getLayoutInflater().inflate(R.layout.waiting_list_item, null);

            TextView mClientName = listView_layout.findViewById(R.id.client_name);
            TextView mStatus = listView_layout.findViewById(R.id.status);

            mClientName.setText(mClients[i]);
            mStatus.setText(mStatuses[i]);

            return listView_layout;
        }
    }

}