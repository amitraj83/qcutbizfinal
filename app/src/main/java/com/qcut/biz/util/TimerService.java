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
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.qcut.biz.eventbus.EventBus;
import com.qcut.biz.events.RelocationRequestEvent;
import com.qcut.biz.models.Barber;
import com.qcut.biz.models.BarberQueue;
import com.qcut.biz.models.Customer;
import com.qcut.biz.models.CustomerComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
            EventBus.instance().fireEvent(new RelocationRequestEvent());
        }
    };

    public static void updateWaitingTimes(final FirebaseDatabase database, final String userid, final Map<String, Barber> barberMap) {
        final DatabaseReference dbRefBarberQueues = DBUtils.getDbRefBarberQueues(database, userid);
        dbRefBarberQueues.runTransaction(new Transaction.Handler() {

            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                List<BarberQueue> barberQueues = MappingUtils.mapToBarberQueues(mutableData);
                for (BarberQueue barberQueue : barberQueues) {
                    List<Customer> customers = new ArrayList<>();
                    Customer inProgressCustomer = null;
                    for (Customer customer : barberQueue.getCustomers()) {
                        if (customer.isInQueue()) {
                            customers.add(customer);
                        } else if (customer.isInProgress()) {
                            inProgressCustomer = customer;
                        }
                    }
                    Collections.sort(customers, new CustomerComparator());
                    long barberAvgTimeToCut = barberMap.get(barberQueue.getBarberKey()).getAvgTimeToCut();
                    long avgTimeToCut = barberAvgTimeToCut == 0 ? 15 : barberAvgTimeToCut;
                    long prevCustomerTime = 0;
                    for (int i = 0; i < customers.size(); i++) {
                        final MutableData customerMutableData = mutableData.child(barberQueue.getBarberKey()).child(customers.get(i).getKey());
                        long newTimeToWait = 0;
                        if (i == 0) {
                            //first customer in the queue
                            if (inProgressCustomer != null) {
                                long serviceStartTime = inProgressCustomer.getServiceStartTime();
                                long timeToWait = avgTimeToCut - ((System.currentTimeMillis() - serviceStartTime) / 60000);
                                newTimeToWait = Math.max(0, timeToWait);
                                //incase in progress customer is ther for more than avgTimeToCut
                            } else {
                                //customer is waiting to be served, there is none on chair
                                newTimeToWait = 0;
                            }
                            prevCustomerTime = newTimeToWait;
                            DBUtils.saveCustomerWaitingTime(customerMutableData, newTimeToWait);
                        } else {
                            newTimeToWait = prevCustomerTime + avgTimeToCut;
                            prevCustomerTime = newTimeToWait;
                            DBUtils.saveCustomerWaitingTime(customerMutableData, newTimeToWait);
                        }
                    }
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    LogUtils.error("databaseError: {0}", databaseError.getMessage());
                }
            }
        });
    }
}