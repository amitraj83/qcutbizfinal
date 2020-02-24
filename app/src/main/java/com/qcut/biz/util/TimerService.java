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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.qcut.biz.models.Barber;
import com.qcut.biz.models.BarberQueue;
import com.qcut.biz.models.Customer;
import com.qcut.biz.models.CustomerComparator;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    public static void updateWaitingTimes(final FirebaseDatabase database, final String userid) {
        DBUtils.getBarbersQueues(database, userid, new OnSuccessListener<List<BarberQueue>>() {
            @Override
            public void onSuccess(List<BarberQueue> barberQueues) {
                for (BarberQueue barberQueue : barberQueues) {
                    //Check if barber is not on break
                    final Barber barber = barberQueue.getBarber();
                    if (barber.isOnBreak()) {
                        continue;
                    }
                    List<Customer> customers = new ArrayList<>();
                    boolean isSomeOneInProgress = false;
                    for (Customer customer : barberQueue.getCustomers()) {
                        if (StringUtils.isNotBlank(customer.getStatus())) {
                            if (customer.isInQueue()) {
                                customers.add(customer);
                            } else if (customer.isInProgress()) {
                                isSomeOneInProgress = true;
                            }
                        }
                    }
                    Collections.sort(customers, new CustomerComparator());
                    int avgTimeToCut = 15;
                    // TODO use avgTimeToCut field in db
                    long prevCustomerTime = 0;
                    for (int i = 0; i < customers.size(); i++) {
                        final DatabaseReference dbRefWaitingTime = DBUtils.getDbRefCustomerExpectedWaitingTime(database, userid,
                                barber.getKey(), customers.get(i).getKey());
                        if (i == 0) {
                            //first customer int the queue
                            if (isSomeOneInProgress) {
                                long timeToWait = customers.get(i).getExpectedWaitingTime();
                                if (timeToWait > 0) {
                                    timeToWait = Math.min(avgTimeToCut, timeToWait);
                                    long newTimeToWait = timeToWait - 1;
                                    dbRefWaitingTime.setValue(newTimeToWait);
                                    prevCustomerTime = newTimeToWait;
                                }
                            } else {
                                //customer is waiting to be served, there is none on chair
                                dbRefWaitingTime.setValue(0);
                                prevCustomerTime = 0;
                            }
                        } else {
                            dbRefWaitingTime.setValue(prevCustomerTime + avgTimeToCut);
                            prevCustomerTime = prevCustomerTime + avgTimeToCut;
                        }
                    }
                }
            }
        });
    }
}