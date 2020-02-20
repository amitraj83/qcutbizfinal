package com.qcut.biz.presenters.fragments;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qcut.biz.adaptors.BarberSelectionArrayAdapter;
import com.qcut.biz.adaptors.WaitingListRecyclerViewAdapter;
import com.qcut.biz.listeners.BarbersChangeListener;
import com.qcut.biz.listeners.WaitingListClickListener;
import com.qcut.biz.models.Barber;
import com.qcut.biz.models.BarberQueue;
import com.qcut.biz.models.BarberStatus;
import com.qcut.biz.models.Customer;
import com.qcut.biz.models.CustomerComparator;
import com.qcut.biz.models.ShopDetails;
import com.qcut.biz.util.Constants;
import com.qcut.biz.util.DBUtils;
import com.qcut.biz.util.LogUtils;
import com.qcut.biz.util.MappingUtils;
import com.qcut.biz.util.TimerService;
import com.qcut.biz.views.WaitingListView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;

public class WaitingListPresenter {

    private String userid;
    private WaitingListView view;
    private SharedPreferences preferences;
    private String barberKey;
    private Context context;
    private FirebaseDatabase database;
    private ValueEventListener barbersChangeListener;
    private DatabaseReference dbRefBarbers;
    private DatabaseReference dbRefBarberQueue;
    private ValueEventListener barberQueueChangeListener;
    int lc = 0;

    public WaitingListPresenter(WaitingListView view, Context context, String barberKey) {
        this.view = view;
        this.context = context;
        this.preferences = context.getSharedPreferences("login", MODE_PRIVATE);
        this.barberKey = barberKey;
        FirebaseApp.initializeApp(context);
        database = FirebaseDatabase.getInstance();
        userid = preferences.getString("userid", null);
        dbRefBarbers = DBUtils.getDbRefBarbers(database, userid);
        barbersChangeListener = new BarbersChangeListener(this);
        dbRefBarberQueue = DBUtils.getDbRefBarberQueue(database, userid, barberKey);
        barberQueueChangeListener = new BarberQueuesListener(view);
    }

    public void onAddCustomerClick() {
        LogUtils.info("onAddCustomerClick");
        DBUtils.getShopDetails(database, userid, new OnSuccessListener<ShopDetails>() {
            @Override
            public void onSuccess(ShopDetails shopDetails) {
                if (shopDetails.isOnline()) {
                    view.showAddCustomerDialog();
                } else {
                    view.showMessage("Cannot add customer. First get online.");
                }
            }
        });
    }

    public void onCustomerAddYesClick() {
        LogUtils.info("onCustomerAddYesClick");
        view.hideAddCustomerDialog();
        view.setYesButtonEnable(false);
        final String selectedBarberKey = view.getSelectedBarberKey();
        final String customerName = view.getEnteredCustomerName();
        if (StringUtils.isNotBlank(customerName)) {
            DBUtils.getBarbers(database, userid, new OnSuccessListener<Map<String, Barber>>() {
                @Override
                public void onSuccess(Map<String, Barber> barbersMap) {
                    Customer.CustomerBuilder customerBuilder = Customer.builder();
                    String customerId = UUID.randomUUID().toString();
                    customerBuilder.anyBarber(selectedBarberKey.equalsIgnoreCase(Constants.ANY))
                            .name(customerName).customerId(customerId);
                    if (selectedBarberKey.equalsIgnoreCase(Constants.ANY)) {
                        for (Barber barber : barbersMap.values()) {
                            if (BarberStatus.OPEN.name().equalsIgnoreCase(barber.getQueueStatus())) {
                                final DatabaseReference queueRef = DBUtils.getDbRefBarberQueue(database, userid, barber.getKey());
                                pushCustomerToDB(customerBuilder, queueRef, barber);
                            }
                        }
                    } else {
                        Barber bq = null;
                        for (Barber barber : barbersMap.values()) {
                            if (barber.getKey().equalsIgnoreCase(selectedBarberKey)) {
                                bq = barber;
                            }
                        }
                        final DatabaseReference queueRef = DBUtils.getDbRefBarberQueue(database, userid, selectedBarberKey);
                        customerBuilder.preferredBarberKey(selectedBarberKey).actualBarberId(selectedBarberKey);
                        pushCustomerToDB(customerBuilder, queueRef, bq);
                    }
                }
            });

        } else {
            view.showMessage("Cannot add customer. No name provided");
        }
    }

    private void pushCustomerToDB(final Customer.CustomerBuilder customerBuilder, final DatabaseReference queueRef, final Barber barber) {
        LogUtils.info("WaitingListPresenter: pushCustomerToDB");
        DBUtils.getBarberQueue(database, userid, barber.getKey(), new OnSuccessListener<BarberQueue>() {
            @Override
            public void onSuccess(BarberQueue barberQueue) {
                Task<Void> voidTask = DBUtils.pushCustomerToDB(customerBuilder, queueRef, barberQueue);
                voidTask.addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        LogUtils.info("Customer added to queue");
                        TimerService.updateWaitingTimes(database, userid);
                        view.showMessage(" added to queue");
                        view.startDoorBell();

                    }
                });
                voidTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        LogUtils.error("Customer added tsk failed:", e);
                    }
                });
            }
        });

    }


    public void setBarberList(List<Barber> barberList) {
        view.setBarberList(new BarberSelectionArrayAdapter(context, barberList));
    }

    public String getBarberKey() {
        return barberKey;
    }


    public void updateBarberStatus(boolean onBreak) {
        view.updateBarberStatus(onBreak);
    }

    public WaitingListRecyclerViewAdapter createWaitingListViewAdaptor() {
        WaitingListClickListener clickListener = new WaitingListClickListener(context, barberKey, database, userid);
        return new WaitingListRecyclerViewAdapter(new ArrayList<Customer>(), context, clickListener);
    }

    public FirebaseDatabase getDatabase() {
        return database;
    }

    public String getUserId() {
        return userid;
    }

    public void showMessage(String msg) {
        view.showMessage(msg);
    }

    public void addBarbersChangeListener() {
        LogUtils.info("{1} addBarbersChangeListener: {0}", barberKey);
        dbRefBarbers.addValueEventListener(barbersChangeListener);
    }

    public void addQueueOnChangeListener() {
        LogUtils.info("addQueueOnChangeListener: {0}", barberKey);
        dbRefBarberQueue.addValueEventListener(barberQueueChangeListener);
    }

    public void removeQueueOnChangeListener() {
        LogUtils.info("removeQueueOnChangeListener: {0}", barberKey);
        dbRefBarberQueue.removeEventListener(barberQueueChangeListener);
    }

    public void removeBarbersChangeListener() {
        LogUtils.info("{1} removeBarbersChangeListener: {0}", barberKey);
        dbRefBarbers.removeEventListener(barbersChangeListener);
    }

    public static class BarberQueuesListener implements ValueEventListener {
        private WaitingListView view;

        public BarberQueuesListener(WaitingListView view) {
            this.view = view;
        }

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            final List<Customer> models = new ArrayList<>();
            final BarberQueue barberQueue = MappingUtils.mapToBarberQueue(dataSnapshot);
            boolean isSomeOneInQueue = false;
            for (Customer customer : barberQueue.getCustomers()) {
                if (!customer.isDone() && !customer.isRemoved()) {
                    if (customer.isInQueue()) {
                        isSomeOneInQueue = true;
                    }
                    models.add(customer);
                }
            }
            if (isSomeOneInQueue && models.size() > 0) {
                Collections.sort(models, new CustomerComparator());
                for (Customer model : models) {
                    if (model.isInQueue()) {
                        //just set name of first queued customer
                        view.updateNextCustomerView(model.getName(), model.getKey());
                        break;
                    }
                }
            } else {
                view.updateNextCustomerView("No customer.", "NONE");
            }
            LogUtils.info("Refreshing queue view");
            view.updateAndRefreshQueue(models);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    }
}
