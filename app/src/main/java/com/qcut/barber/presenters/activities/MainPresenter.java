package com.qcut.barber.presenters.activities;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qcut.barber.R;
import com.qcut.barber.eventbus.EventBus;
import com.qcut.barber.events.BarbersChangeEvent;
import com.qcut.barber.events.RelocationRequestEvent;
import com.qcut.barber.listeners.BarberQueueChangeListener;
import com.qcut.barber.listeners.BarberStatusChangeListener;
import com.qcut.barber.listeners.BarbersChangeListener;
import com.qcut.barber.listeners.IResult;
import com.qcut.barber.models.Barber;
import com.qcut.barber.models.ShopStatus;
import com.qcut.barber.util.BarberSelectionUtils;
import com.qcut.barber.util.CloudFunctionsUtils;
import com.qcut.barber.util.DBUtils;
import com.qcut.barber.util.LogUtils;
import com.qcut.barber.views.MainView;
import com.qcut.barber.views.activities.StartActivity;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class MainPresenter implements BarbersChangeEvent.BarbersChangeEventHandler,
        RelocationRequestEvent.RelocationRequestEventHandler {

    private final String userid;
    private MainView view;
    private SharedPreferences preferences;
    private Context context;
    private FirebaseDatabase database;
    private Map<String, Barber> barberMap = new HashMap<>();

    public MainPresenter(MainView mainView, Context context) {
        this.view = mainView;
        this.context = context;
        this.preferences = context.getSharedPreferences("login", MODE_PRIVATE);
        FirebaseApp.initializeApp(context);
        database = FirebaseDatabase.getInstance();
        userid = preferences.getString("userid", null);
        setStatusListner();
        DBUtils.getDbRefBarbers(database, userid).addValueEventListener(new BarbersChangeListener());
        LogUtils.info("addBarbersChangeListener: MainPresenter");
        DBUtils.getDbRefBarberQueues(database, userid).addValueEventListener(new BarberQueueChangeListener());
        DBUtils.getDbRefBarbers(database, userid).addChildEventListener(new BarberStatusChangeListener());
        EventBus.instance().registerHandler(RelocationRequestEvent.TYPE, this);
        EventBus.instance().registerHandler(BarbersChangeEvent.TYPE, this);
    }

//    public void onStatusClick() {
//        final String OFFLINE = context.getString(R.string.status_offline);
//        if (userid == null) {
//            view.showMessage("Problem. Please logout and Login again. ");
//            LogUtils.error("UserId is null: {0}", userid);
//            return;
//        }
//        if (view.getCurrentStatus().equalsIgnoreCase(OFFLINE)) {
//            //go online
//            updateStatus(context.getString(R.string.status_online));
//            view.setShopStatusOnline();
//
//        } else {
//            //go offline
//            updateStatus(OFFLINE);
//            view.setShopStatusOffline();
//        }
//    }

    public void setStatusListner() {
        DatabaseReference shopStatusRef = DBUtils.getDbRefShopStatus(database, userid);
        shopStatusRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String status = dataSnapshot.getValue(String.class);
                    if (ShopStatus.valueOf(status) == ShopStatus.ONLINE) {
                        view.setShopStatusOnline();
                    } else {
                        view.setShopStatusOffline();
                    }
                    LogUtils.info("BarberStatus changed to: {0}", status);
                } else {
                    view.setShopStatusOffline();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                LogUtils.error("Error while changing shop status: {0}", databaseError.getMessage());
            }
        });
    }

    private void updateStatus(String status) {
        DatabaseReference shopStatusRef = DBUtils.getDbRefShopStatus(database, userid);
        shopStatusRef.setValue(status);
    }

    public void onNavigationItemSelected(int id) {
        if (id == R.id.nav_profile) {
            view.navigateToId(R.id.nav_profile);
        } else if (id == R.id.addBarber) {
            view.navigateToId(R.id.nav_add_barber);
        } else if (id == R.id.temp) {
            view.navigateToId(R.id.temp_fragment);
        } else if (id == R.id.shop_address) {
            view.navigateToId(R.id.nav_go_shop_details);
        } else if (id == R.id.opening_hours) {
            view.navigateToId(R.id.nav_go_shop_opening_hours);
        } else if (id == R.id.add_services) {
            view.navigateToId(R.id.nav_go_shop_add_services);
        } else if (id == R.id.nav_log_out) {
            preferences.edit().putBoolean("isLoggedIn", false).apply();
            preferences.edit().putString("userid", null).apply();
            updateStatus(context.getString(R.string.status_offline));
            view.startActivity(StartActivity.class);
        } else if (id == R.id.customer_view) {
            view.navigateToId(R.id.frag_customer_view);
        }
        view.closeDrawer();
    }

    @Override
    public void onBarbersChange(BarbersChangeEvent event) {
        barberMap.clear();
        boolean online = false;
        for (Barber barber : event.getBarbers()) {
            if (!barber.isStopped()) {
                online = true;
            }
            barberMap.put(barber.getKey(), barber);
        }
        if (online) {
            updateStatus(context.getString(R.string.status_online));
        } else {
            updateStatus(context.getString(R.string.status_offline));
        }
    }

    @Override
    public void onRelocationRequested(RelocationRequestEvent event) {
        BarberSelectionUtils.reAllocateCustomers(database, userid, barberMap);
    }
}
