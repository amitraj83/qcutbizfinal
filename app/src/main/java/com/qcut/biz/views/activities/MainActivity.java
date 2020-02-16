package com.qcut.biz.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.database.FirebaseDatabase;
import com.qcut.biz.R;
import com.qcut.biz.presenters.activities.MainPresenter;
import com.qcut.biz.util.TimerService;
import com.qcut.biz.views.MainView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, MainView {

    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private ImageView blinkingDot;
    private TextView statusView;
    private String userid;
    private FirebaseDatabase database = null;
    private Animation animation;
    private MainPresenter mainPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        statusView = findViewById(R.id.status_change);

        mainPresenter = new MainPresenter(this, this);

        startService(new Intent(getBaseContext(), TimerService.class));

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        blinkingDot = findViewById(R.id.blinking_dot);

        animation = new AlphaAnimation(1, 0);
        animation.setDuration(1000);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        animation.cancel();
        animation.reset();
        blinkingDot.setAnimation(animation);

        statusView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainPresenter.onStatusClick();
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
        mainPresenter.onNavigationItemSelected(id);
        return true;
    }

    @Override
    public void closeDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    @Override
    public void setShopStatusOnline() {
        blinkingDot.startAnimation(animation);
        statusView.setText(MainActivity.this.getString(R.string.status_online));
        animation.start();
    }

    @Override
    public void setShopStatusOffline() {
        statusView.setText(MainActivity.this.getString(R.string.status_offline));
        animation.cancel();
        animation.reset();
    }

    @Override
    public void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public String getCurrentStatus() {
        return String.valueOf(statusView.getText());
    }

    public void navigateToId(int id) {
        navController.navigate(id);
    }

    @Override
    public void startActivity(Class clazz) {
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
        finish();
    }
}
