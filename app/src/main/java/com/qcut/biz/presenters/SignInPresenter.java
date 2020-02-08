package com.qcut.biz.presenters;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.qcut.biz.models.ShopDetails;
import com.qcut.biz.util.LogUtils;
import com.qcut.biz.views.SignInView;

public class SignInPresenter {

    private SignInView view;
    private SharedPreferences preferences;
    private Context context;
    private FirebaseDatabase database;

    public SignInPresenter(SignInView view, SharedPreferences preferences, Context context) {
        this.view = view;
        this.preferences = preferences;
        this.context = context;
        FirebaseApp.initializeApp(context);
        database = FirebaseDatabase.getInstance();
    }

    public void onSignInClick() {
        Query query = database.getReference().child(ShopDetails.SHOP_DETAILS).
                orderByChild(ShopDetails.EMAIL).equalTo(view.getEmail().trim());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String emailText = view.getEmail();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot shopData : dataSnapshot.getChildren()) {
                        ShopDetails shopDetails = shopData.getValue(ShopDetails.class);
                        String email = shopDetails.getEmail().trim();
                        String password = shopDetails.getPassword().trim();
                        if (email.equalsIgnoreCase(emailText.trim()) &&
                                password.equalsIgnoreCase(view.getPassword().trim())) {
                            view.showMessage("Login Successful.");
                            LogUtils.info("Login successful with email: {0}", emailText);
                            preferences.edit().putBoolean("isLoggedIn", true).apply();
                            preferences.edit().putString("userid", shopData.getKey().toString()).apply();
                            view.startActivity();

                        } else {
                            view.showMessage("Login unsuccessful.");
                            LogUtils.error("Login unsuccessful with email: {0}", emailText);
                        }
                    }
                } else {
                    view.showMessage("Login Does not exist.");
                    LogUtils.error("Login email does not exists: {0}", emailText);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtils.error("Error while quering email in db: {0}", databaseError.getMessage());
            }
        });
    }
}
