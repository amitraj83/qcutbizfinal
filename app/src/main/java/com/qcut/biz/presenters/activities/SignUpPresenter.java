package com.qcut.biz.presenters.activities;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.qcut.biz.models.ShopDetails;
import com.qcut.biz.models.ShopStatus;
import com.qcut.biz.util.DBUtils;
import com.qcut.biz.util.LogUtils;
import com.qcut.biz.views.SignUpView;
import com.qcut.biz.views.activities.MainActivity;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.TimeZone;

import static android.content.Context.MODE_PRIVATE;

public class SignUpPresenter {

    private SignUpView view;
    private SharedPreferences preferences;
    private Context context;
    private FirebaseDatabase database;

    public SignUpPresenter(SignUpView view, Context context) {
        this.view = view;
        this.context = context;
        this.preferences = context.getSharedPreferences("login", MODE_PRIVATE);
        FirebaseApp.initializeApp(context);
        database = FirebaseDatabase.getInstance();
    }

    public void onSignUpClick() {
        final String email = view.getEmail();
        if (StringUtils.isBlank(email)) {
            view.showMessage("Email cannot be empty");
            return;
        }
        if (StringUtils.isBlank(view.getPassword())) {
            view.showMessage("Password cannot be empty");
            return;
        } else if (view.getPassword().length() < 8) {
            view.showMessage("Password must be at least 8 character long");
        }
        DBUtils.getShopsDetails(database, new OnSuccessListener<List<ShopDetails>>() {
            @Override
            public void onSuccess(List<ShopDetails> shopsDetails) {
                boolean emailExists = false;
                for (ShopDetails sDetails : shopsDetails) {
                    if (sDetails.getEmail().equalsIgnoreCase(email)) {
                        emailExists = true;
                        LogUtils.info("User already exists with ShopDetails: {0}", sDetails);
                        break;
                    }
                }
                if (!emailExists) {
                    //this shopid does not exists, create new one
                    final DatabaseReference dbRef = DBUtils.getDbRefShopsDetails(database);
                    String key = dbRef.push().getKey();
                    ShopDetails shopDetails = ShopDetails.builder().key(key).email(email).password(view.getPassword())
                            .name(view.getShopContactName()).shopName(view.getShopName())
                            .timezone(TimeZone.getDefault().getID())
                            .status(ShopStatus.OFFLINE.name()).build();
                    dbRef.child(key).setValue(shopDetails);
                    LogUtils.info("New ShopDetails are saved: {0}", shopDetails);
                    preferences.edit().putBoolean("isLoggedIn", true).apply();
                    preferences.edit().putString("userid", key).apply();
                    view.startActivity(MainActivity.class);
                } else {
                    view.showMessage("User Already exists.");
                }
            }
        });
    }
}
