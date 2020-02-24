package com.qcut.biz.presenters;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qcut.biz.models.Customer;
import com.qcut.biz.models.CustomerStatus;
import com.qcut.biz.util.Constants;
import com.qcut.biz.util.DBUtils;
import com.qcut.biz.util.MappingUtils;

import java.util.Iterator;

public class CustomerOptionPresenter {

    FirebaseDatabase database;
    String userid;
    private String barberKey;

    public CustomerOptionPresenter(FirebaseDatabase database, String userid, String barberKey) {
        this.database = database;
        this.userid = userid;
        this.barberKey = barberKey;
    }

    public void onAssignToAnyClick(final String customerKey) {

        DBUtils.getCustomer(database, userid, barberKey, customerKey, new OnSuccessListener<Customer>() {
            @Override
            public void onSuccess(Customer customer) {
                customer.setAnyBarber(true);
                DBUtils.getDbRefBarberQueue(database, userid, barberKey).child(customerKey).setValue(customer);
            }
        });
    }

    public void onRemoveCustomerClick(final String customerKey) {
        DBUtils.getCustomer(database, userid, barberKey, customerKey, new OnSuccessListener<Customer>() {
            @Override
            public void onSuccess(Customer customer) {
                customer.setStatus(CustomerStatus.REMOVED.name());
                DBUtils.getDbRefBarberQueue(database, userid, barberKey).child(customerKey).setValue(customer);
            }
        });
    }
}
