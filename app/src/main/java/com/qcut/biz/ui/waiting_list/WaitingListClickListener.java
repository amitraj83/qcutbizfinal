package com.qcut.biz.ui.waiting_list;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qcut.biz.R;
import com.qcut.biz.models.Customer;
import com.qcut.biz.models.CustomerComparator;
import com.qcut.biz.models.ShopQueueModel;
import com.qcut.biz.util.Status;
import com.qcut.biz.util.TimeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WaitingListClickListener implements View.OnClickListener {

    private Context mContext;
    private View itemView;
    private String tag;
    private FirebaseDatabase database = null;
    private String userid;


    public WaitingListClickListener(Context mContext, View itemView, String tag, FirebaseDatabase database, String userid) {
        this.mContext = mContext;
        this.itemView = itemView;
        this.tag = tag;
        this.database = database;
        this.userid = userid;
    }

    @Override
    public void onClick(View v) {
        changeCustomerStatus(v);
    }

    private void changeCustomerStatus( View v) {
        final LayoutInflater factory = LayoutInflater.from(mContext);
        String queueItemStatus = ((TextView)v.findViewById(R.id.cust_status)).getTag().toString();
        String queueItemName = ((TextView)v.findViewById(R.id.cust_name)).getText().toString();
//        final ShopQueueModel queueItem = (ShopQueueModel) dynamicListView.getItemAtPosition(position);
        if(queueItemStatus.equalsIgnoreCase(Status.PROGRESS.name())) {
            final View serviceDoneView = factory.inflate(R.layout.service_done_dialog, null);
            TextView custNameTV = (TextView) serviceDoneView.findViewById(R.id.service_done_customer_name);
            custNameTV.setText(queueItemName);
            final AlertDialog serviceDoneDialog = new AlertDialog.Builder(mContext).create();
            serviceDoneDialog.setView(serviceDoneView);


            serviceDoneDialog.show();
            serviceDoneDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            serviceDoneDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 750);

            addServiceDoneButtonsClickListener(serviceDoneDialog, v.getTag().toString());

        } else if (queueItemStatus.equalsIgnoreCase(Status.QUEUE.name())) {
            final View startServiceView = factory.inflate(R.layout.start_service_dialog, null);
            TextView custNameTV = (TextView) startServiceView.findViewById(R.id.start_service_cust_name);
            custNameTV.setText(queueItemName);
            final AlertDialog serviceStartDialog = new AlertDialog.Builder(mContext).create();
            serviceStartDialog.setView(startServiceView);

            serviceStartDialog.show();
            serviceStartDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            serviceStartDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 750);
            addServiceStartButtonsClickListener(serviceStartDialog, v.getTag().toString());
        }

    }



    private void addServiceDoneButtonsClickListener(final AlertDialog serviceDoneDialog, final String queueItemId) {

        Button yesButton = (Button) serviceDoneDialog.findViewById(R.id.yes_done_service);
        Button noButton = (Button) serviceDoneDialog.findViewById(R.id.no_done_service);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String queuedCustomerId = String.valueOf(queueItemId);
                if (queuedCustomerId != null) {
                    final DatabaseReference queue = database.getReference().child("barbershops").child(userid)
                            .child("queues").child(TimeUtil.getTodayDDMMYYYY()).child(tag).child(queuedCustomerId);

                    Map<String, Object> map = new HashMap<>();
                    map.put("status", Status.DONE);
                    map.put("timeToWait", 0);
                    map.put("placeInQueue", -1);
                    Task<Void> voidTask = queue.updateChildren(map);
                    voidTask.addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            DatabaseReference databaseReference = database.getReference().child("barbershops").child(userid)
                                    .child("queues").child(TimeUtil.getTodayDDMMYYYY()).child(tag);
                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot aBarberQueue) {
                                    String aBarberQueueKey = aBarberQueue.getKey();
                                    if(!aBarberQueueKey.equalsIgnoreCase("online")) {

                                        List<Customer> customers = new ArrayList<Customer>();
                                        if (!aBarberQueueKey.equalsIgnoreCase("online")) {
                                            Iterator<DataSnapshot> childIterator = aBarberQueue.getChildren().iterator();
                                            while (childIterator.hasNext()) {
                                                DataSnapshot aCustomer = childIterator.next();
                                                if (!aCustomer.getKey().equalsIgnoreCase("status")
                                                        && aCustomer.child("status").getValue().toString().equalsIgnoreCase(Status.QUEUE.name())) {
                                                    String aCustomerKey = aCustomer.getKey();
                                                    customers.add(new Customer(aCustomerKey,
                                                            Long.valueOf(aCustomer.child("timeAdded").getValue().toString()),
                                                            Integer.valueOf(aCustomer.child("timeToWait").getValue().toString()))
                                                    );
                                                }
                                            }
                                        }
                                        Collections.sort(customers, new CustomerComparator());

                                        for (int i = 0; i < customers.size(); i++) {
                                            aBarberQueue.getRef().child(customers.get(i).getKey())
                                                    .child("timeToWait").setValue(0);
                                            break;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });
                }
                serviceDoneDialog.dismiss();
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceDoneDialog.dismiss();
            }
        });
    }

    private void addServiceStartButtonsClickListener(final AlertDialog serviceStartDialog, final String queueItemId) {

        Button yesButton = (Button) serviceStartDialog.findViewById(R.id.yes_start_service);
        Button noButton = (Button) serviceStartDialog.findViewById(R.id.no_start_service);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String queuedCustomerId = String.valueOf(queueItemId);
                setCustomerInProgress(queuedCustomerId);
                serviceStartDialog.dismiss();
            }
        });
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceStartDialog.dismiss();
            }
        });
    }



    private void setCustomerInProgress(final String queuedCustomerId) {

        final DatabaseReference queryRef = database.getReference().child("barbershops").child(userid)
                .child("queues").child(TimeUtil.getTodayDDMMYYYY()).child(tag);
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                boolean isSomeoneInProgress = false;
                String customerId ="";
                while (iterator.hasNext()) {
                    DataSnapshot customer = iterator.next();

                    if(customer.getKey().equalsIgnoreCase(queuedCustomerId)) {
                        customerId = customer.child("customerId").getValue().toString();
                    }

                    if(!customer.getKey().equalsIgnoreCase("status") &&
                            customer.child("status").getValue().toString().equalsIgnoreCase(Status.PROGRESS.name())) {
                        isSomeoneInProgress = true;
                    }
                }
                if(!isSomeoneInProgress) {

                    final DatabaseReference queue = database.getReference().child("barbershops").child(userid)
                            .child("queues").child(TimeUtil.getTodayDDMMYYYY()).child(tag).child(queuedCustomerId);


                    Map<String, Object> map = new HashMap<>();
                    map.put("status", Status.PROGRESS);
                    map.put("timeToWait", 0);
                    map.put("timeServiceStarted", new Date().getTime());
                    map.put("placeInQueue", -1);
                    Task<Void> voidTask = queue.updateChildren(map);
                    final String customerIdForAnyBarber = customerId;
                    voidTask.addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            DatabaseReference dateRef = database.getReference().child("barbershops").child(userid)
                                    .child("queues").child(TimeUtil.getTodayDDMMYYYY());
                            dateRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Iterator<DataSnapshot> queueIt = dataSnapshot.getChildren().iterator();
                                    while (queueIt.hasNext()) {
                                        DataSnapshot queueSnapshot = queueIt.next();
                                        if(!queueSnapshot.getKey().equalsIgnoreCase(tag) && !queueSnapshot.getKey().equalsIgnoreCase("online")) {
                                            Iterator<DataSnapshot> customersIt = queueSnapshot.getChildren().iterator();
                                            while (customersIt.hasNext()) {
                                                DataSnapshot customerDataSnapshot = customersIt.next();
                                                if(!customerDataSnapshot.getKey().equalsIgnoreCase("status")){
                                                    if(customerDataSnapshot.child("customerId").getValue().toString()
                                                            .equalsIgnoreCase(customerIdForAnyBarber)) {
                                                        queueSnapshot.getRef().child(customerDataSnapshot.getKey()).removeValue();
                                                    }
                                                }

                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });
                } else {
                    Toast.makeText(mContext, "Cannot start services. A customer is already in progress.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}