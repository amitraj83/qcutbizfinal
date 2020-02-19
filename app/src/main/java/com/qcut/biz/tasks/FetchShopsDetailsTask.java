package com.qcut.biz.tasks;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qcut.biz.models.Barber;
import com.qcut.biz.models.ShopDetails;
import com.qcut.biz.models.ShopStatus;
import com.qcut.biz.util.DBUtils;
import com.qcut.biz.util.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FetchShopsDetailsTask implements Continuation<Void, Task<List<ShopDetails>>> {

    private FirebaseDatabase database;

    public FetchShopsDetailsTask(FirebaseDatabase database) {
        this.database = database;
    }

    @Override
    public Task<List<ShopDetails>> then(@NonNull Task<Void> task) throws Exception {
        final TaskCompletionSource<List<ShopDetails>> tcs = new TaskCompletionSource<>();

        DBUtils.getDbRefShopsDetails(database).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LogUtils.info("ShopsDetails loaded");
                Iterator<DataSnapshot> childrenIterator = dataSnapshot.getChildren().iterator();
                List<ShopDetails> shopsDetails = new ArrayList<>();
                while (childrenIterator.hasNext()) {
                    final DataSnapshot next = childrenIterator.next();
                    ShopDetails shopDetails = next.getValue(ShopDetails.class);
                    shopDetails.setKey(next.getKey());
                    shopsDetails.add(shopDetails);
                }
                LogUtils.info("FetchShopsDetailsTask: {0}", shopsDetails);
                tcs.setResult(shopsDetails);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError dbError) {
                LogUtils.error("Error loading shopDetails:{0}", dbError.getMessage());
            }
        });
        return tcs.getTask();
    }
}