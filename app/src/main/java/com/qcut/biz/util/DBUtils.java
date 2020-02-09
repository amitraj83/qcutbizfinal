package com.qcut.biz.util;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.qcut.biz.models.Barber;
import com.qcut.biz.models.BarberQueue;
import com.qcut.biz.models.BarberQueueStatus;
import com.qcut.biz.models.Customer;
import com.qcut.biz.models.ShopDetails;
import com.qcut.biz.tasks.FetchBarberStatusTask;
import com.qcut.biz.tasks.FetchBarbersQueuesTask;
import com.qcut.biz.tasks.FetchBarbersTask;
import com.qcut.biz.tasks.FindBarberQueueTask;
import com.qcut.biz.tasks.FindBarberTask;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DBUtils {


    public static DatabaseReference getDbRefShopStatus(FirebaseDatabase database, String userid) {
        return database.getReference().child(ShopDetails.SHOP_DETAILS).child(userid).child(ShopDetails.STATUS);
    }

    public static DatabaseReference getDbRefBarbers(FirebaseDatabase database, String userid) {
        return database.getReference().child(Barber.BARBERS).child(userid);
    }

    public static DatabaseReference getDbRefAllBarberQueues(FirebaseDatabase database, String userid) {
        return database.getReference().child(BarberQueue.BARBER_WAITING_QUEUES).child(buildShopIdForToday(userid));
    }

    public static DatabaseReference getDbRefBarberQueue(FirebaseDatabase database, String userid, String barberKey) {
        return database.getReference().child(BarberQueue.BARBER_WAITING_QUEUES)
                .child(buildShopIdForToday(userid)).child(barberKey);
    }

    public static DatabaseReference getDbRefBarberQueueStatus(FirebaseDatabase database, String userid, String barberKey) {
        return database.getReference().child(BarberQueueStatus.BARBER_QUEUES_STATUSES)
                .child(buildShopIdForToday(userid)).child(barberKey);
    }

    public static DatabaseReference getDbRefBarberQueueStatuses(FirebaseDatabase database, String userid) {
        return database.getReference().child(BarberQueueStatus.BARBER_QUEUES_STATUSES)
                .child(buildShopIdForToday(userid));
    }

    public static void getBarbersQueues(FirebaseDatabase database, String userid, OnSuccessListener<List<BarberQueue>> onSuccessListener) {
        Tasks.<Void>forResult(null).continueWithTask(new FetchBarbersTask(database, userid))
                .continueWithTask(new FetchBarbersQueuesTask(database, userid)).addOnSuccessListener(onSuccessListener);
    }

    public static void getBarberQueue(FirebaseDatabase database, String userid, String barberKey, OnSuccessListener<BarberQueue> onSuccessListener) {
        Tasks.<Void>forResult(null).continueWithTask(new FetchBarbersTask(database, userid))
                .continueWithTask(new FetchBarbersQueuesTask(database, userid))
                .continueWithTask(new FindBarberQueueTask(database, userid, barberKey))
                .addOnSuccessListener(onSuccessListener);
    }

    public static void getBarbers(FirebaseDatabase database, String userid, OnSuccessListener<Map<String, Barber>> onSuccessListener) {
        Tasks.<Void>forResult(null).continueWithTask(new FetchBarbersTask(database, userid))
                .addOnSuccessListener(onSuccessListener);
    }

    public static void getBarber(FirebaseDatabase database, String userid, String barberKey, OnSuccessListener<Barber> onSuccessListener) {
        Tasks.<Void>forResult(null).continueWithTask(new FetchBarbersTask(database, userid))
                .continueWithTask(new FindBarberTask(barberKey))
                .addOnSuccessListener(onSuccessListener);
    }

    public static void getBarberQueueStatus(FirebaseDatabase database, String userid, String barberKey, OnSuccessListener<BarberQueueStatus> onSuccessListener) {
        Tasks.<Void>forResult(null).continueWithTask(new FetchBarberStatusTask(database, userid, barberKey))
                .addOnSuccessListener(onSuccessListener);
    }

    public static DatabaseReference getDbRefShopDetails(FirebaseDatabase database) {
        return database.getReference().child(ShopDetails.SHOP_DETAILS);
    }

    public static Uri getValueDownloadUri(final StorageReference storageReference, final String imagePath) {
        return DbSyncUtils.loadUriSynchronous(storageReference, imagePath);
    }

    public static String buildShopIdForToday(String userid) {
        return String.format("%s_%s", userid, TimeUtil.getTodayDDMMYYYY());
    }

    public static String extractBarberIdFromQueueId(String queueId) {
        return queueId.substring(0, queueId.length() - 8);
    }

    public static Task<Void> pushCustomerToDB(Context mContext, DataSnapshot dataSnapshot, String selectedKey, String name, String customerId, boolean isAny) {
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
                if (status.equalsIgnoreCase(Status.PROGRESS.name())) {
                    timeServiceStarted = next.child("timeServiceStarted").getValue();
                }
            }
        }

        DataSnapshot avgTimeToCutData = dataSnapshot.child("avgTimeToCut");

        if (!avgTimeToCutData.exists()) {
            Toast.makeText(mContext, "Failed - Avg. Cut time not set. Set this in Shop Details. ", Toast.LENGTH_SHORT).show();
            return null;
        }

        String avgTimeToCut = avgTimeToCutData.exists() ? avgTimeToCutData.getValue().toString() : null;
        long timePerCut = 0;
        if (StringUtils.isNotBlank(avgTimeToCut)) {
            timePerCut = Long.valueOf(avgTimeToCut);
        }
        long timeToWait = timePerCut;
        //timeServiceStarted
        if (count == 0 && timeServiceStarted != null) {
            long timePreviousServiceStarted = Long.valueOf(timeServiceStarted.toString());
            long minutesPassedSinceStarted = ((new Date().getTime() - timePreviousServiceStarted) / 1000) / 60;
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

    public static BarberQueue findKeyObjectFromChildren(List<BarberQueue> queues, String searchKey) {
        Iterator<BarberQueue> queuesIterator = queues.iterator();
        while (queuesIterator.hasNext()) {
            BarberQueue queue = queuesIterator.next();
            if (queue.getBarberKey().equals(searchKey)) {
                return queue;
            }
        }
        return null;
    }
}
