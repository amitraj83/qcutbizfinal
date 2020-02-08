package com.qcut.biz.presenters.activities;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qcut.biz.R;
import com.qcut.biz.activity.StartActivity;
import com.qcut.biz.views.activities.MainView;
import com.qcut.biz.models.ShopStatus;
import com.qcut.biz.util.DBUtils;
import com.qcut.biz.util.LogUtils;

import static android.content.Context.MODE_PRIVATE;

public class MainPresenter {

    private final String userid;
    private MainView view;
    private SharedPreferences preferences;
    private Context context;
    private FirebaseDatabase database;

    public MainPresenter(MainView mainView, Context context) {
        this.view = mainView;
        this.context = context;
        this.preferences = context.getSharedPreferences("login", MODE_PRIVATE);
        FirebaseApp.initializeApp(context);
        database = FirebaseDatabase.getInstance();
        userid = preferences.getString("userid", null);
        setStatusListner();
    }

    public void onStatusClick() {
        final String OFFLINE = context.getString(R.string.status_offline);
        if (userid == null) {
            view.showMessage("Problem. Please logout and Login again. ");
            LogUtils.error("UserId is null: {0}", userid);
            return;
        }
        if (view.getCurrentStatus().equalsIgnoreCase(OFFLINE)) {
            //go online
            updateStatus(context.getString(R.string.status_online));
            view.setShopStatusOnline();

        } else {
            //go offline
            updateStatus(OFFLINE);
            view.setShopStatusOffline();
        }
    }

    public void setStatusListner() {
        DatabaseReference shopStatusRef = DBUtils.getDbRefShopStatus(database, userid);
        shopStatusRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LogUtils.info("Status changed to: ");
                if (dataSnapshot.exists()) {
                    String status = dataSnapshot.getValue(String.class);
                    if (ShopStatus.valueOf(status) == ShopStatus.ONLINE) {
                        view.setShopStatusOnline();
                    } else {
                        view.setShopStatusOffline();
                    }
                    LogUtils.info("Status changed to: {0}", status);
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
        }
//        else if (id == R.id.nav_list) {
//            view.navigateToId(R.id.nav_waiting_list);
//        }
        else if (id == R.id.shop_address) {
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
        }
        view.closeDrawer();
    }
}
