package com.qcut.barber.presenters.fragments;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.qcut.barber.models.ShopDetails;
import com.qcut.barber.util.DBUtils;
import com.qcut.barber.views.ProfileView;

import static android.content.Context.MODE_PRIVATE;

public class ProfilePresenter {

    private String userid;
    private ProfileView view;
    private SharedPreferences preferences;
    private Context context;
    private FirebaseDatabase database;
    private StorageReference storageReference;

    public ProfilePresenter(ProfileView view, Context context) {
        this.view = view;
        this.context = context;
        this.preferences = context.getSharedPreferences("login", MODE_PRIVATE);
        FirebaseApp.initializeApp(context);
        database = FirebaseDatabase.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        userid = preferences.getString("userid", null);
    }

    public void initializeData() {
        DBUtils.getShopDetails(database, userid, new OnSuccessListener<ShopDetails>() {
            @Override
            public void onSuccess(ShopDetails shopDetails) {
                view.setShopName(shopDetails.getShopName());
                view.setAddressLine1(shopDetails.getAddressLine1());
                view.setAddressLine2(shopDetails.getAddressLine2());
                view.setCity(shopDetails.getCity());
            }
        });
    }
}
