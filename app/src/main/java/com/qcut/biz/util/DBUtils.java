package com.qcut.biz.util;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.qcut.biz.models.Barber;
import com.qcut.biz.models.BarberQueue;
import com.qcut.biz.models.Customer;
import com.qcut.biz.models.ShopDetails;
import com.qcut.biz.tasks.FetchBarbersQueuesTask;
import com.qcut.biz.tasks.FetchBarbersTask;
import com.qcut.biz.tasks.FetchShopDetailsTask;
import com.qcut.biz.tasks.FetchShopsDetailsTask;
import com.qcut.biz.tasks.FindBarberQueueTask;
import com.qcut.biz.tasks.FindBarberTask;
import com.qcut.biz.tasks.FindCustomerTask;

import java.util.List;
import java.util.Map;

public class DBUtils {


    public static DatabaseReference getDbRefShopStatus(FirebaseDatabase database, String userid) {
        return database.getReference().child(ShopDetails.SHOP_DETAILS).child(userid).child(ShopDetails.STATUS);
    }

    public static DatabaseReference getDbRefShopDetails(FirebaseDatabase database, String userid) {
        return database.getReference().child(ShopDetails.SHOP_DETAILS).child(userid);
    }

    public static DatabaseReference getDbRefBarbers(FirebaseDatabase database, String userid) {
        return database.getReference().child(Barber.BARBERS).child(userid);
    }

    public static DatabaseReference getDbRefBarber(FirebaseDatabase database, String userid, String barberKey) {
        return database.getReference().child(Barber.BARBERS).child(userid).child(barberKey);
    }

    public static DatabaseReference getDbRefBarberQueues(FirebaseDatabase database, String userid) {
        return database.getReference().child(BarberQueue.BARBER_WAITING_QUEUES).child(buildShopIdForToday(userid));
    }

    public static DatabaseReference getDbRefBarberQueue(FirebaseDatabase database, String userid, String barberKey) {
        return database.getReference().child(BarberQueue.BARBER_WAITING_QUEUES)
                .child(buildShopIdForToday(userid)).child(barberKey);
    }

    public static DatabaseReference getDbRefCustomer(FirebaseDatabase database, String userid, String barberKey, String customerKey) {
        return database.getReference().child(BarberQueue.BARBER_WAITING_QUEUES)
                .child(buildShopIdForToday(userid)).child(barberKey).child(customerKey);
    }

    public static DatabaseReference getDbRefCustomerExpectedWaitingTime(FirebaseDatabase database, String userid, String barberKey, String customerKey) {
        return database.getReference().child(BarberQueue.BARBER_WAITING_QUEUES)
                .child(buildShopIdForToday(userid)).child(barberKey).child(customerKey).child(Customer.EXPECTED_WAITING_TIME);
    }

    public static DatabaseReference getDbRefShopsDetails(FirebaseDatabase database) {
        return database.getReference().child(ShopDetails.SHOP_DETAILS);
    }


    public static void getShopDetails(FirebaseDatabase database, String userid, OnSuccessListener<ShopDetails> onSuccessListener) {
        Tasks.<Void>forResult(null).continueWithTask(new FetchShopDetailsTask(database, userid))
                .addOnSuccessListener(onSuccessListener);
    }

    public static void getShopsDetails(FirebaseDatabase database, OnSuccessListener<List<ShopDetails>> onSuccessListener) {
        Tasks.<Void>forResult(null).continueWithTask(new FetchShopsDetailsTask(database))
                .addOnSuccessListener(onSuccessListener);
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

    public static void getCustomer(FirebaseDatabase database, String userid, String barberKey, String customerKey, OnSuccessListener<Customer> onSuccessListener) {
        Tasks.<Void>forResult(null).continueWithTask(new FetchBarbersTask(database, userid))
                .continueWithTask(new FetchBarbersQueuesTask(database, userid))
                .continueWithTask(new FindBarberQueueTask(database, userid, barberKey))
                .continueWithTask(new FindCustomerTask(customerKey))
                .addOnSuccessListener(onSuccessListener);
    }

    public static String buildShopIdForToday(String userid) {
        return String.format("%s_%s", userid, TimeUtil.getTodayDDMMYYYY());
    }

    public static String extractBarberIdFromQueueId(String queueId) {
        return queueId.substring(0, queueId.length() - 8);
    }

    public static Task<Void> saveCustomer(FirebaseDatabase database, String userid, Customer customer, String barberKey) {
        DatabaseReference queueRef = DBUtils.getDbRefBarberQueue(database, userid, barberKey);
        String key = queueRef.push().getKey();
        customer.setKey(key);
        LogUtils.info("DbUtils: saveCustomer adding customer:{0}", customer);
        return queueRef.child(key).setValue(customer);
    }

    public static void saveCustomer(Customer customer, MutableData queueMutableData) {
        queueMutableData.child(customer.getKey()).setValue(customer);
        LogUtils.info("DbUtils: saveCustomer adding customer:{0}", customer);
    }

    public static BarberQueue findBarberQueueByKey(List<BarberQueue> queues, String searchKey) {
        for (BarberQueue queue : queues) {
            if (queue.getBarberKey().equals(searchKey)) {
                return queue;
            }
        }
        return null;
    }

    public static Task<Void> saveShopDetails(FirebaseDatabase database, String userid, ShopDetails shopDetails) {
        final DatabaseReference dbRef = getDbRefShopDetails(database, userid);
        return dbRef.setValue(shopDetails);
    }
}
