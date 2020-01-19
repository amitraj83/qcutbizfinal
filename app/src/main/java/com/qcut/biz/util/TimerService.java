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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class TimerService extends Service
{
    private static Timer timer = new Timer();
    private Context ctx;

    private FirebaseDatabase database = null;
    private String userid;
    private SharedPreferences sp;


    public IBinder onBind(Intent arg0)
    {
          return null;
    }

    public void onCreate() 
    {
        super.onCreate();
        ctx = this;
        database = FirebaseDatabase.getInstance();
        sp = ctx.getSharedPreferences("login", MODE_PRIVATE);
        userid = sp.getString("userid", null);

        startService();
    }

    private void startService()
    {           
        timer.scheduleAtFixedRate(new mainTask(), 0, 60*1000);
    }

    private class mainTask extends TimerTask
    { 
        public void run() 
        {
            toastHandler.sendEmptyMessage(0);
        }
    }    

    public void onDestroy() 
    {
          super.onDestroy();
          Toast.makeText(this, "Service Stopped ...", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("HandlerLeak")
    private final Handler toastHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            final DatabaseReference dbRef = database.getReference().child("barbershops")
                    .child(userid).child("queues").child(TimeUtil.getTodayDDMMYYYY());
            if(dbRef != null) {
                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Iterator<DataSnapshot> snapshotIterator = dataSnapshot.getChildren().iterator();
                            while (snapshotIterator.hasNext()) {
                                DataSnapshot aCustomer = snapshotIterator.next();
                                String key = aCustomer.getKey();
                                if (!key.equalsIgnoreCase("online")) {
                                    boolean isCustomerNotDone = !aCustomer.child("status").getValue().toString().equalsIgnoreCase(Status.DONE.name());
                                    boolean isCustomerNotRemoved = !aCustomer.child("status").getValue().toString().equalsIgnoreCase(Status.REMOVED.name());

                                    if (isCustomerNotDone && isCustomerNotRemoved) {
                                        DataSnapshot timeToWait1 = aCustomer.child("timeToWait");
                                        if(timeToWait1 != null) {
                                            long value = Long.valueOf(timeToWait1.getValue() != null
                                                    ? timeToWait1.getValue().toString() : "-1");
                                            if(value > 0) {
                                                Map<String, Object> map = new HashMap<>();
                                                value--;
                                                map.put("timeToWait", value);
                                                dbRef.child(key).updateChildren(map);
                                            }
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
    };    
}