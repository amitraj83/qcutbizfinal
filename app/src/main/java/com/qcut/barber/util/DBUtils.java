package com.qcut.barber.util;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.functions.FirebaseFunctions;
import com.qcut.barber.models.Barber;
import com.qcut.barber.models.BarberQueue;
import com.qcut.barber.models.ConfigParams;
import com.qcut.barber.models.Customer;
import com.qcut.barber.models.ServiceAvailable;
import com.qcut.barber.models.ShopDetails;
import com.qcut.barber.tasks.FetchBarbersQueuesTask;
import com.qcut.barber.tasks.FetchBarbersTask;
import com.qcut.barber.tasks.FetchShopDetailsTask;
import com.qcut.barber.tasks.FetchShopServicesTask;
import com.qcut.barber.tasks.FetchShopsDetailsTask;
import com.qcut.barber.tasks.FindBarberQueueTask;
import com.qcut.barber.tasks.FindBarberTask;
import com.qcut.barber.tasks.FindCustomerTask;

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

    public static DatabaseReference getDbRefConfigParams(FirebaseDatabase database, String userid) {
        return database.getReference().child(ConfigParams.CONFIG_PARAMETERS).child(buildShopIdForToday(userid));
    }

    public static DatabaseReference getDbRefShopsServices(FirebaseDatabase database, String userid) {
        return database.getReference().child(ServiceAvailable.SERVICES_AVAILABLE).child(userid);
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


    public static void getShopServices(FirebaseDatabase database, String userid, OnSuccessListener<List<ServiceAvailable>> onSuccessListener) {
        Tasks.<Void>forResult(null).continueWithTask(new FetchShopServicesTask(database, userid))
                .addOnSuccessListener(onSuccessListener);
    }

    public static String buildShopIdForToday(String userid) {
        return String.format("%s_%s", userid, TimeUtil.getTodayDDMMYYYY());
    }

    public static String extractBarberIdFromQueueId(String queueId) {
        return queueId.substring(0, queueId.length() - 8);
    }

    public static void saveCustomer(Customer customer, MutableData queueMutableData) {
        queueMutableData.child(customer.getKey()).setValue(customer);
    }

    public static void saveCustomerWaitingTime(MutableData customerMutableData, long waitingTime) {
        customerMutableData.child(Customer.EXPECTED_WAITING_TIME).setValue(waitingTime);
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
