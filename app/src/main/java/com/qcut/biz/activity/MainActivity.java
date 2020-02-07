package com.qcut.biz.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qcut.biz.R;
import com.qcut.biz.models.ShopDetails;
import com.qcut.biz.models.ShopStatus;
import com.qcut.biz.util.DBUtils;
import com.qcut.biz.util.LogUtils;
import com.qcut.biz.util.TimeUtil;
import com.qcut.biz.util.TimerService;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private ImageView blinkingDot;
    private SharedPreferences sp;
    private String userid;
    private FirebaseDatabase database = null;
    private TextView statusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FirebaseApp.initializeApp(this);
        database = FirebaseDatabase.getInstance();
        sp = getSharedPreferences("login", MODE_PRIVATE);
        userid = sp.getString("userid", null);

        startService(new Intent(getBaseContext(), TimerService.class));

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        blinkingDot = findViewById(R.id.blinking_dot);

        final Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(1000);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        animation.cancel();
        animation.reset();
        blinkingDot.startAnimation(animation);
        final String OFFLINE = MainActivity.this.getString(R.string.status_offline);
        final String ONLINE = MainActivity.this.getString(R.string.status_online);
        statusView = findViewById(R.id.status_change);
        DatabaseReference shopStatusRef = DBUtils.getDbRefShopStatus(database, userid);
        updateStatus(OFFLINE);

        shopStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LogUtils.info("Status changed to: ");
                if (dataSnapshot.exists()) {
                    String status = dataSnapshot.getValue(String.class);
                    if (ShopStatus.valueOf(status) == ShopStatus.ONLINE) {
                        statusView.setText(ONLINE);
                        animation.start();
                    } else {
                        statusView.setText(OFFLINE);
                        animation.cancel();
                        animation.reset();
                    }
                    LogUtils.info("Status changed to: {0}", status);
                } else {
                    statusView.setText(OFFLINE);
                    animation.cancel();
                    animation.reset();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                LogUtils.error("Error while changing shop status: {0}", databaseError.getMessage());
            }
        });

        statusView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userid == null) {
                    Toast.makeText(MainActivity.this, "Problem. Please logout and Login again. ", Toast.LENGTH_SHORT).show();
                    LogUtils.error("UserId is null: {0}", userid);
                    return;
                }

                final TextView statusView = (TextView) v;
                final String currentStatus = String.valueOf(statusView.getText());

                if (currentStatus.equalsIgnoreCase(OFFLINE)) {
                    //go online
                    updateStatus(ONLINE);
                    blinkingDot.startAnimation(animation);
                    statusView.setText(ONLINE);

                } else {
                    //go offline
                    updateStatus(OFFLINE);
                    statusView.setText(OFFLINE);
                    animation.cancel();
                    animation.reset();
                }
            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener((NavigationView.OnNavigationItemSelectedListener) this);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            navController.navigate(R.id.nav_profile);

        } else if (id == R.id.addBarber) {
            navController.navigate(R.id.nav_add_barber);
        } else if (id == R.id.temp) {
            navController.navigate(R.id.temp_fragment);
        }
//        else if (id == R.id.nav_list) {
//            navController.navigate(R.id.nav_waiting_list);
//        }
        else if (id == R.id.shop_address) {
            navController.navigate(R.id.nav_go_shop_details);
        } else if (id == R.id.opening_hours) {
            navController.navigate(R.id.nav_go_shop_opening_hours);
        } else if (id == R.id.add_services) {
            navController.navigate(R.id.nav_go_shop_add_services);
        } else if (id == R.id.nav_log_out) {

            sp.edit().putBoolean("isLoggedIn", false).apply();
            sp.edit().putString("userid", null).apply();

            updateStatus(MainActivity.this.getString(R.string.status_offline));
            Intent intent = new Intent(MainActivity.this, StartActivity.class);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void updateStatus(String status) {
        DatabaseReference shopStatusRef = DBUtils.getDbRefShopStatus(database, userid);
        shopStatusRef.setValue(status);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
