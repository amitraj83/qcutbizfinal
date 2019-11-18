package com.qcut.biz.ui.adapters;

import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.qcut.biz.R;
import com.qcut.biz.ui.go_online.GoOnlineModel;

public class ShopAddServicesFragment extends Fragment {

    private GoOnlineModel goOnlineModel;
    ListView mListView;
    String[] mHairCuts, mPricesHairCuts;
    Button addService;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        goOnlineModel =
                ViewModelProviders.of(this).get(GoOnlineModel.class);
        View root = inflater.inflate(R.layout.fragment_shop_add_services, container, false);
        LayoutInflater factory = LayoutInflater.from(getContext());
        final View addServiceView = factory.inflate(R.layout.add_service_dialog, null);
        final AlertDialog addServiceDialog = new AlertDialog.Builder(getContext()).create();
        addServiceDialog.setView(addServiceView);

        addService = root.findViewById(R.id.add_service);
        addService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addServiceDialog.show();
                addServiceDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                addServiceDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 750);
            }
        });

        String[] priceServiceItems = new String[] {
                "Arial", "Arial", "Arial",
        };
        Spinner priceServiceSpinner = (Spinner) addServiceView.findViewById(R.id.price_service_spinner);
        ArrayAdapter<String> priceServiceAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, priceServiceItems);
        priceServiceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        priceServiceSpinner.setAdapter(priceServiceAdapter);

        mHairCuts = new String[] {
                "Hair Cut",
                "Dry Haircut",
        };

        mPricesHairCuts = new String[] {
                "$ 14.00",
                "$ 14.00",
        };

        return root;
    }

    class CustomAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mHairCuts.length;
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
            return view;
        }
    }
}