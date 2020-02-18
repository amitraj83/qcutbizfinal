package com.qcut.biz.presenters.activities;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.qcut.biz.models.ShopDetails;
import com.qcut.biz.util.DBUtils;
import com.qcut.biz.util.LogUtils;
import com.qcut.biz.views.SignInView;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class SignInPresenter {

    private SignInView view;
    private SharedPreferences preferences;
    private Context context;
    private FirebaseDatabase database;

    public SignInPresenter(SignInView view, Context context) {
        this.view = view;
        this.context = context;
        this.preferences = context.getSharedPreferences("login", MODE_PRIVATE);
        FirebaseApp.initializeApp(context);
        database = FirebaseDatabase.getInstance();
    }

    public void onSignInClick() {
        final String email = view.getEmail();
        DBUtils.getShopsDetails(database, new OnSuccessListener<List<ShopDetails>>() {
            @Override
            public void onSuccess(List<ShopDetails> shopsDetails) {
                ShopDetails shopDetails = null;
                for (ShopDetails details : shopsDetails) {
                    if (details.getEmail().equalsIgnoreCase(email)) {
                        shopDetails = details;
                        break;
                    }
                }
                if (shopDetails != null) {
                    view.showMessage("Login Successful.");
                    LogUtils.info("Login successful with email: {0}", shopDetails.getEmail());
                    preferences.edit().putBoolean("isLoggedIn", true).apply();
                    preferences.edit().putString("userid", shopDetails.getKey()).apply();
                    view.startActivity();
                } else {
                    view.showMessage("Login unsuccessful.");
                    LogUtils.error("Login unsuccessful with email: {0}", email);
                }
            }
        });
    }
}
