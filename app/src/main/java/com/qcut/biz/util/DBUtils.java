package com.qcut.biz.util;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DBUtils {

    public static Task<Void> pushCustomerToDB (DataSnapshot dataSnapshot, String selectedKey, String name, String customerId, boolean isAny) {
        int count = 0;
        DataSnapshot queueSnapShot = dataSnapshot.child("queues").child(TimeUtil.getTodayDDMMYYYY()).child(selectedKey);
        Iterator<DataSnapshot> iterator = queueSnapShot.getChildren().iterator();
        Object timeServiceStarted = null;
        while (iterator.hasNext()) {
            DataSnapshot next = iterator.next();
            if (next != null && !next.getKey().equalsIgnoreCase("online")) {
                DataSnapshot statusChild = next.child("status");
                String status = String.valueOf(statusChild.getValue());
                if (status.equalsIgnoreCase(Status.QUEUE.name())) {
                    count++;
                }
                if(status.equalsIgnoreCase(Status.PROGRESS.name())) {
                    timeServiceStarted = next.child("timeServiceStarted").getValue();
                }
            }
        }

        long timePerCut = Long.valueOf(dataSnapshot.child("avgTimeToCut").getValue().toString());
        Object avgTimeToCut = dataSnapshot.child("avgTimeToCut").getValue();
        if(avgTimeToCut != null) {
            timePerCut = Long.valueOf(avgTimeToCut.toString());
        }
        long timeToWait = timePerCut;
        //timeServiceStarted
        if(count == 0 && timeServiceStarted != null) {
            long timePreviousServiceStarted = Long.valueOf(timeServiceStarted.toString());
            long minutesPassedSinceStarted = ((new Date().getTime() - timePreviousServiceStarted)/1000)/60;
            timeToWait = Math.max(0, timePerCut - minutesPassedSinceStarted);
        } else {
            timeToWait = timePerCut * count;
        }

        DatabaseReference queue = queueSnapShot.getRef();
        String key = queue.push().getKey();

        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("placeInQueue", count);
        map.put("skipcount", 0);
        map.put("timeToWait", timeToWait);
        map.put("status", Status.QUEUE);
        map.put("timeAdded", new Date().getTime());
        map.put("timeFirstAddedInQueue", new Date().getTime());
        map.put("customerId", customerId);
        map.put("anyBarber", isAny);

        return queue.child(key).setValue(map);

    }
}
