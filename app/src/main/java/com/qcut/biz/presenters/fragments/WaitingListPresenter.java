package com.qcut.biz.presenters.fragments;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.qcut.biz.adaptors.BarberSelectionArrayAdapter;
import com.qcut.biz.adaptors.WaitingListRecyclerViewAdapter;
import com.qcut.biz.eventbus.EventBus;
import com.qcut.biz.events.BarberQueueChangeEvent;
import com.qcut.biz.events.BarbersChangeEvent;
import com.qcut.biz.events.QueueTabSelectedEvent;
import com.qcut.biz.listeners.WaitingListClickListener;
import com.qcut.biz.models.Barber;
import com.qcut.biz.models.BarberQueue;
import com.qcut.biz.models.Customer;
import com.qcut.biz.models.CustomerComparator;
import com.qcut.biz.models.ShopDetails;
import com.qcut.biz.util.BarberSelectionUtils;
import com.qcut.biz.util.Constants;
import com.qcut.biz.util.DBUtils;
import com.qcut.biz.util.LogUtils;
import com.qcut.biz.views.WaitingListView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;

public class WaitingListPresenter implements BarbersChangeEvent.BarbersChangeEventHandler,
        BarberQueueChangeEvent.BarberQueueChangeEventHandler, QueueTabSelectedEvent.QueueTabSelectedEventHandler {

    private String userid;
    private WaitingListView view;
    private SharedPreferences preferences;
    private String barberKey;
    private Context context;
    private FirebaseDatabase database;

    public WaitingListPresenter(WaitingListView view, Context context, String barberKey) {
        this.view = view;
        this.context = context;
        this.preferences = context.getSharedPreferences("login", MODE_PRIVATE);
        this.barberKey = barberKey;
        FirebaseApp.initializeApp(context);
        database = FirebaseDatabase.getInstance();
        userid = preferences.getString("userid", null);
        registerHandlers();
        //fetch values first time
        DBUtils.getBarbers(database, userid, new OnSuccessListener<Map<String, Barber>>() {
            @Override
            public void onSuccess(Map<String, Barber> barbersMap) {
                updateBarbersSelectionList(barbersMap.values());
            }
        });
    }

    private void registerHandlers() {
        EventBus.instance().registerHandler(BarbersChangeEvent.TYPE, this);
        EventBus.instance().registerHandler(BarberQueueChangeEvent.TYPE, this);
        EventBus.instance().registerHandler(QueueTabSelectedEvent.TYPE, this);

    }

    public void onDestroy() {
        unregisterHandlers();
    }

    private void unregisterHandlers() {
        EventBus.instance().unregisterHandler(BarbersChangeEvent.TYPE, this);
        EventBus.instance().unregisterHandler(BarberQueueChangeEvent.TYPE, this);
        EventBus.instance().unregisterHandler(QueueTabSelectedEvent.TYPE, this);
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
            final Customer.CustomerBuilder customerBuilder = Customer.builder();
            //TODO remove customerID if not used
            String customerId = UUID.randomUUID().toString();
            final boolean anyBarber = selectedBarberKey.equalsIgnoreCase(Constants.ANY);
            customerBuilder.anyBarber(anyBarber).name(customerName).customerId(customerId);
            if (!anyBarber) {
                customerBuilder.preferredBarberKey(selectedBarberKey);
            }
            BarberSelectionUtils.assignBarber(database, userid, customerBuilder, view);
        } else {
            view.showMessage("Cannot add customer. No name provided");
        }
    }

    public String getBarberKey() {
        return barberKey;
    }


    public void updateBarberStatus(boolean onBreak) {
        view.updateBarberStatus(onBreak);
    }

    public WaitingListRecyclerViewAdapter createWaitingListViewAdaptor() {
        WaitingListClickListener clickListener = new WaitingListClickListener(context, barberKey, database, userid);
        return new WaitingListRecyclerViewAdapter(new ArrayList<Customer>(), context, clickListener, database, userid, barberKey);
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

    @Override
    public void onBarbersChange(BarbersChangeEvent event) {
        updateBarbersSelectionList(event.getBarbers());
    }

    public void updateBarbersSelectionList(Collection<Barber> barbers) {
        List<Barber> barberList = new ArrayList<>();
        barberList.add(Barber.builder().key(Constants.ANY).name(Constants.ANY).imagePath("").build());

        for (Barber barber : barbers) {
            if (!barber.isStopped()) {
                barberList.add(barber);
            }

            if (barber.getKey().equalsIgnoreCase(barberKey)) {
                updateBarberStatus(barber.isOnBreak());
            }
        }
        view.setBarberList(new BarberSelectionArrayAdapter(context, barberList));
    }

    @Override
    public void onBarberQueueChange(BarberQueueChangeEvent event) {
        // customer is added/removed/updated
        final BarberQueue barberQueue = event.getChangedBarberQueue();
        if (!barberQueue.getBarberKey().equalsIgnoreCase(barberKey)) {
            return;
        }
        LogUtils.info("WaitingListPresenter onBarberQueueChange");
        populateCustomers(barberQueue);
    }

    public void populateCustomers(BarberQueue barberQueue) {
        final List<Customer> models = new ArrayList<>();
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
        view.updateAndRefreshQueue(models);
    }

    @Override
    public void onQueueTabSelected(QueueTabSelectedEvent event) {
        if (!event.getBarberQueue().getBarberKey().equalsIgnoreCase(barberKey)) {
            return;
        }
        LogUtils.info("WaitingListPresenter onQueueTabSelected");
        populateCustomers(event.getBarberQueue());
    }
}
