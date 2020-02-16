package com.qcut.biz.presenters.activities;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.qcut.biz.models.ShopDetails;
import com.qcut.biz.util.DBUtils;
import com.qcut.biz.util.LogUtils;
import com.qcut.biz.views.SignUpView;
import com.qcut.biz.views.activities.MainActivity;

import org.apache.commons.lang3.StringUtils;

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
        if (StringUtils.isBlank(view.getEmail())) {
            view.showMessage("Email cannot be empty");
            return;
        }
        if (StringUtils.isBlank(view.getPassword())) {
            view.showMessage("Password cannot be empty");
            return;
        } else if (view.getPassword().length() < 8) {
            view.showMessage("Password must be at least 8 character long");
        }
        DatabaseReference dbRef = DBUtils.getDbRefShopDetails(database);

        if (dbRef != null) {
            //this shopid does not exists, create new one
            String key = dbRef.push().getKey();
            ShopDetails shopDetails = ShopDetails.builder().key(key).email(view.getEmail()).password(view.getPassword())
                    .name(view.getShopContactName()).shopName(view.getShopName()).build();
            dbRef.child(key).setValue(shopDetails);
            LogUtils.info("New shop deatails are saved: {0}", shopDetails);
            preferences.edit().putBoolean("isLoggedIn", true).apply();
            preferences.edit().putString("userid", key).apply();
            view.startActivity(MainActivity.class);
        } else {
            view.showMessage("User Already exists.");
            LogUtils.error("User Already exists: {0}", view.getEmail());
        }
    }
}
