package com.qcut.barber.presenters.fragments;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.qcut.barber.adaptors.CustomerViewAdapter;
import com.qcut.barber.models.BarberQueue;
import com.qcut.barber.models.Customer;
import com.qcut.barber.util.DBUtils;
import com.qcut.barber.util.MappingUtils;
import com.qcut.barber.views.CustomerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import static android.content.Context.MODE_PRIVATE;

public class CustomerViewPresenter {

    private String userid;
    private CustomerView view;
    private SharedPreferences preferences;
    private Context context;
    private FirebaseDatabase database;
    private StorageReference storageReference;

    public CustomerViewPresenter(CustomerView view, Context context) {
        this.view = view;
        this.context = context;
        this.preferences = context.getSharedPreferences("login", MODE_PRIVATE);
        FirebaseApp.initializeApp(context);
        database = FirebaseDatabase.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        userid = preferences.getString("userid", null);
    }

    public void addBarberQueueChangeListener() {
        DBUtils.getDbRefBarberQueues(database, userid).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ArrayList<Customer> dataSet = new ArrayList<>();
                    Iterator<DataSnapshot> barberIterator = dataSnapshot.getChildren().iterator();
                    while (barberIterator.hasNext()) {
                        final BarberQueue barberQueue = MappingUtils.mapToBarberQueue(barberIterator.next());
                        if (barberQueue.getCustomers().size() > 0) {
                            for (Customer customer : barberQueue.getCustomers()) {
                                if (customer.isInProgress() || customer.isInQueue()) {
                                    dataSet.add(customer);
                                }
                            }
                        }
                    }
                    Collections.sort(dataSet, new CustomerArrivalTimeComparator());
                    CustomerViewAdapter adapter = new CustomerViewAdapter(context, database, userid, storageReference, dataSet);
                    view.setCustomerViewAdaptor(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private static class CustomerArrivalTimeComparator implements Comparator<Customer> {
        @Override
        public int compare(Customer o1, Customer o2) {
            if (o1.getExpectedWaitingTime() > o2.getExpectedWaitingTime()) {
                return 1;
            } else if (o1.getExpectedWaitingTime() == o2.getExpectedWaitingTime()) {
                if (o1.isInProgress()) {
                    return -1;
                } else if (o2.isInProgress()) {
                    return 1;
                }
                return 0;
            } else {
                return -1;
            }
        }
    }
}
