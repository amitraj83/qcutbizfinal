package com.qcut.biz.presenters.fragments;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.qcut.biz.models.ServiceAvailable;
import com.qcut.biz.util.DBUtils;
import com.qcut.biz.views.ShopAddServicesView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class ShopAddServicesPresenter {

    private FirebaseDatabase database;
    private String userid;
    private ShopAddServicesView view;
    private SharedPreferences preferences;
    private Context context;

    public ShopAddServicesPresenter(ShopAddServicesView view, Context context) {
        this.view = view;
        this.context = context;
        this.preferences = context.getSharedPreferences("login", MODE_PRIVATE);
        FirebaseApp.initializeApp(context);
        database = FirebaseDatabase.getInstance();
        userid = preferences.getString("userid", null);
    }

    public void initializeData() {
        DBUtils.getShopServices(database, userid, new OnSuccessListener<List<ServiceAvailable>>() {
            @Override
            public void onSuccess(List<ServiceAvailable> serviceAvailables) {
                view.setServiceAvailable(serviceAvailables);
            }
        });
    }

    public void onAddServiceYesClick() {
        final DatabaseReference servicesRef = DBUtils.getDbRefShopsServices(database, userid);
        if (StringUtils.isNotBlank(view.getServiceName())
                && StringUtils.isNotBlank(view.getServicePrice())) {
            view.hideDialog();
            final DatabaseReference ref = servicesRef.push();
            final ServiceAvailable serviceAvailable = ServiceAvailable.builder().serviceName(view.getServiceName())
                    .servicePrice(view.getServicePrice()).key(ref.getKey()).build();
            servicesRef.child(serviceAvailable.getKey()).setValue(serviceAvailable).addOnSuccessListener
                    (new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            initializeData();
                            view.showMessage("Service Added Successfully");
                        }
                    });
        } else {
            view.showMessage("Failed - Invalid service name or price");
        }
    }
}
