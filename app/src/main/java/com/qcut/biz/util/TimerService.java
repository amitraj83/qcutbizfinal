package com.qcut.biz.util;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qcut.biz.models.Customer;
import com.qcut.biz.models.CustomerComparator;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class TimerService extends Service {
    private static Timer timer = new Timer();
    private Context ctx;

    private FirebaseDatabase database = null;
    private String userid;
    private SharedPreferences sp;
//    int avgTimeToCut = 15;


    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        ctx = this;
        database = FirebaseDatabase.getInstance();
        sp = ctx.getSharedPreferences("login", MODE_PRIVATE);
        userid = sp.getString("userid", null);

        startService();
    }

    private void startService() {
        timer.scheduleAtFixedRate(new mainTask(), 0, 60 * 1000);
    }

    private class mainTask extends TimerTask {
        public void run() {
            toastHandler.sendEmptyMessage(0);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Stopped ...", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("HandlerLeak")
    private final Handler toastHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            updateWaitingTimes(database, userid);

        }
    };

    public static void updateWaitingTimes(FirebaseDatabase database, String userid) {
        final DatabaseReference dbRef = database.getReference().child("barbershops").child(userid);
//                .child("queues").child(TimeUtil.getTodayDDMMYYYY());
        if (dbRef != null) {
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshotUserId) {
                    final DataSnapshot dataSnapshot = dataSnapshotUserId.child("queues").child(TimeUtil.getTodayDDMMYYYY());
                    if (dataSnapshot.exists()) {
                        Iterator<DataSnapshot> snapshotIterator = dataSnapshot.getChildren().iterator();
                        while (snapshotIterator.hasNext()) {

                            DataSnapshot aBarberQueue = snapshotIterator.next();

                            //Check if barber is not on break
                            String aBarberQueueKey = aBarberQueue.getKey();
                            if (!aBarberQueueKey.equalsIgnoreCase("online")
                                    && !aBarberQueue.child("status").getValue().toString().equalsIgnoreCase(Status.BREAK.name())) {

                                List<Customer> customers = new ArrayList<Customer>();
                                boolean isSomeOneInProgress = false;
                                if (!aBarberQueueKey.equalsIgnoreCase("online")) {
                                    Iterator<DataSnapshot> childIterator = aBarberQueue.getChildren().iterator();
                                    while (childIterator.hasNext()) {
                                        DataSnapshot aCustomer = childIterator.next();
                                        if (!aCustomer.getKey().equalsIgnoreCase("status") && aCustomer.child("status").exists()) {
//                                                && aCustomer.child("status").getValue().toString().equalsIgnoreCase(Status.QUEUE.name())) {
                                            if( aCustomer.child("status").getValue().toString().equalsIgnoreCase(Status.QUEUE.name())) {
                                                String aCustomerKey = aCustomer.getKey();
                                                customers.add(new Customer(aCustomerKey,
                                                        Long.valueOf(aCustomer.child("timeAdded").getValue().toString()),
                                                        Integer.valueOf(aCustomer.child("timeToWait").getValue().toString()))
                                                );
                                            } else if (aCustomer.child("status").getValue().toString().equalsIgnoreCase(Status.PROGRESS.name())) {
                                                isSomeOneInProgress = true;
                                            }
                                        }
                                    }
                                }
                                Collections.sort(customers, new CustomerComparator());
                                int avgTimeToCut = 15;

                                DataSnapshot avgTimeToCutData = dataSnapshotUserId.child("avgTimeToCut");
                                String avgTimeToCutStr = avgTimeToCutData.exists() ? avgTimeToCutData.getValue().toString() : null;
                                if (StringUtils.isNotBlank(avgTimeToCutStr)) {
                                    avgTimeToCut = Integer.valueOf(avgTimeToCutStr);
                                }

                                int prevCustomerTime = 0;
                                for (int i = 0; i < customers.size(); i++) {
                                    if (i == 0) {
                                        if(isSomeOneInProgress) {
                                            int timeToWait = customers.get(i).getTimeToWait();
                                            if (timeToWait > 0) {
                                                if (timeToWait > avgTimeToCut) {
                                                    timeToWait = avgTimeToCut;
                                                }
                                                int newTimeToWait = timeToWait - 1;
                                                aBarberQueue.getRef().child(customers.get(i).getKey()).child("timeToWait").setValue(newTimeToWait);
                                                prevCustomerTime = newTimeToWait;
                                            }
                                        } else {
                                            aBarberQueue.getRef().child(customers.get(i).getKey()).child("timeToWait").setValue(0);
                                            prevCustomerTime = 0;
                                        }
                                    } else {
                                        aBarberQueue.getRef().child(customers.get(i).getKey())
                                                .child("timeToWait").setValue(prevCustomerTime + avgTimeToCut);
                                        prevCustomerTime = prevCustomerTime + avgTimeToCut;
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
    }


}