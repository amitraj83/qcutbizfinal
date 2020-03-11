package com.qcut.biz.views.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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

import com.qcut.biz.R;
import com.qcut.biz.models.ServiceAvailable;
import com.qcut.biz.presenters.fragments.ShopAddServicesPresenter;
import com.qcut.biz.views.ShopAddServicesView;

import java.util.List;

public class ShopAddServicesFragment extends Fragment implements ShopAddServicesView {

    private ListView servicesListView;
    private ShopAddServicesPresenter presenter;
    private AlertDialog addServiceDialog;
    private EditText serviceName;
    private EditText servicePrice;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_shop_add_services, container, false);
        if (presenter == null) {
            presenter = new ShopAddServicesPresenter(this, getContext());
            final LayoutInflater factory = LayoutInflater.from(getContext());
            final View addServiceView = factory.inflate(R.layout.add_service_dialog, null);
            addServiceDialog = new AlertDialog.Builder(getContext()).create();
            addServiceDialog.setView(addServiceView);

            servicesListView = root.findViewById(R.id.add_service_list_services);

            Button addService = root.findViewById(R.id.add_service);
            addService.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addServiceDialog.show();
                    addServiceDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    addServiceDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 750);
                }
            });
            addServiceDialog.show();
            serviceName = addServiceDialog.findViewById(R.id.add_service_name);
            servicePrice = addServiceDialog.findViewById(R.id.add_service_price);

            Button yesButton = addServiceDialog.findViewById(R.id.add_service_yes_button);
            Button noButton = addServiceDialog.findViewById(R.id.add_service_cancel_button);
            yesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    presenter.onAddServiceYesClick();
                }
            });

            noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideDialog();
                }
            });
            addServiceDialog.hide();
        }
        presenter.initializeData();
        return root;
    }

    @Override
    public void hideDialog() {
        addServiceDialog.dismiss();
    }

    @Override
    public void showMessage(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public String getServiceName() {
        return serviceName.getText().toString().trim();
    }

    @Override
    public String getServicePrice() {
        return servicePrice.getText().toString().trim();
    }

    @Override
    public void setServiceAvailable(List<ServiceAvailable> serviceAvailables) {
        ShopAddServicesFragment.CustomAdapter customAdapter = new ShopAddServicesFragment.CustomAdapter(serviceAvailables);
        servicesListView.setAdapter(customAdapter);
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