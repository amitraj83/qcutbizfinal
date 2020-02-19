package com.qcut.biz.tasks;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qcut.biz.models.ShopDetails;
import com.qcut.biz.util.DBUtils;
import com.qcut.biz.util.LogUtils;

public class FetchShopDetailsTask implements Continuation<Void, Task<ShopDetails>> {

    private FirebaseDatabase database;
    private String userid;

    public FetchShopDetailsTask(FirebaseDatabase database, String userid) {
        this.database = database;
        this.userid = userid;
    }

    @Override
    public Task<ShopDetails> then(@NonNull Task<Void> task) throws Exception {
        final TaskCompletionSource<ShopDetails> tcs = new TaskCompletionSource<>();
        DBUtils.getDbRefShopDetails(database, userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LogUtils.info("ShopDetails loaded");
                ShopDetails shopDetails = dataSnapshot.getValue(ShopDetails.class);
                shopDetails.setKey(dataSnapshot.getKey());
                LogUtils.info("FetchShopDetailsTask: {0}", shopDetails);
                tcs.setResult(shopDetails);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError dbError) {
                LogUtils.error("Error loading shopDetails:{0}", dbError.getMessage());
            }
        });
        return tcs.getTask();
    }
}