package com.qcut.barber.presenters.fragments;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.qcut.barber.adaptors.BarberSelectionArrayAdapter;
import com.qcut.barber.adaptors.WaitingListRecyclerViewAdapter;
import com.qcut.barber.eventbus.EventBus;
import com.qcut.barber.events.BarberQueuesChangeEvent;
import com.qcut.barber.events.BarbersChangeEvent;
import com.qcut.barber.events.QueueTabSelectedEvent;
import com.qcut.barber.listeners.WaitingListClickListener;
import com.qcut.barber.models.Barber;
import com.qcut.barber.models.BarberQueue;
import com.qcut.barber.models.Customer;
import com.qcut.barber.models.CustomerComparator;
import com.qcut.barber.models.ShopDetails;
import com.qcut.barber.util.BarberSelectionUtils;
import com.qcut.barber.util.Constants;
import com.qcut.barber.util.DBUtils;
import com.qcut.barber.views.WaitingListView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class WaitingListPresenter implements BarbersChangeEvent.BarbersChangeEventHandler,
        BarberQueuesChangeEvent.BarberQueuesChangeEventHandler, QueueTabSelectedEvent.QueueTabSelectedEventHandler {

    private String userid;
    private WaitingListView view;
    private SharedPreferences preferences;
    private String barberKey;
    private Context context;
    private FirebaseDatabase database;
    private Map<String, Barber> barbersMap = new HashMap<>();

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
        EventBus.instance().registerHandler(BarberQueuesChangeEvent.TYPE, this);
        EventBus.instance().registerHandler(QueueTabSelectedEvent.TYPE, this);

    }

    public void onDestroy() {
        unregisterHandlers();
    }

    private void unregisterHandlers() {
        EventBus.instance().unregisterHandler(BarbersChangeEvent.TYPE, this);
        EventBus.instance().unregisterHandler(BarberQueuesChangeEvent.TYPE, this);
        EventBus.instance().unregisterHandler(QueueTabSelectedEvent.TYPE, this);
    }

    public void onAddCustomerClick() {
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
        view.hideAddCustomerDialog();
        final String selectedBarberKey = view.getSelectedBarberKey();
        final String customerName = view.getEnteredCustomerName();
        if (StringUtils.isNotBlank(customerName)) {
            final Customer.CustomerBuilder customerBuilder = Customer.builder();
            final boolean anyBarber = selectedBarberKey.equalsIgnoreCase(Constants.ANY);
            customerBuilder.anyBarber(anyBarber).name(customerName);
            if (!anyBarber) {
                customerBuilder.preferredBarberKey(selectedBarberKey);
            }
            if (barbersMap.isEmpty()) {
                DBUtils.getBarbers(database, userid, new OnSuccessListener<Map<String, Barber>>() {
                    @Override
                    public void onSuccess(Map<String, Barber> barbersMap) {
                        WaitingListPresenter.this.barbersMap = barbersMap;
                        BarberSelectionUtils.assignBarber(database, userid, customerBuilder, barbersMap);
                    }
                });
            } else {
                BarberSelectionUtils.assignBarber(database, userid, customerBuilder, barbersMap);
            }

        } else {
            view.showMessage("Cannot add customer. No name provided");
        }
        view.clearEnteredCustomerName();
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
        barbersMap.clear();
        for (Barber barber : event.getBarbers()) {
            barbersMap.put(barber.getKey(), barber);
        }
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
    public void onBarberQueuesChange(BarberQueuesChangeEvent event) {
        // customer is added/removed/updated
        final List<BarberQueue> queues = event.getBarberQueues();
        boolean queueUpdated = false;
        for (BarberQueue queue : queues) {
            if (queue.getBarberKey().equalsIgnoreCase(barberKey)) {
                queueUpdated = true;
                populateCustomers(queue);
                break;
            }
        }
        if (!queueUpdated) {
            //it mean barber may be logged out and now there is no customer in queue
            //so there is no queue instance left, so update list with empty model
            populateCustomers(BarberQueue.builder().barberKey(barberKey).build());
        }
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

    public Map<String, Barber> getBarbersMap() {
        return barbersMap;
    }

    @Override
    public void onQueueTabSelected(QueueTabSelectedEvent event) {
        if (!event.getBarberQueue().getBarberKey().equalsIgnoreCase(barberKey)) {
            return;
        }
        populateCustomers(event.getBarberQueue());
    }
}
