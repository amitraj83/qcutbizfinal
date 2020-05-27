package com.qcut.barber.tasks;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qcut.barber.models.ServiceAvailable;
import com.qcut.barber.util.DBUtils;
import com.qcut.barber.util.LogUtils;
import com.qcut.barber.util.MappingUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FetchShopServicesTask implements Continuation<Void, Task<List<ServiceAvailable>>> {

    private FirebaseDatabase database;
    private String userid;

    public FetchShopServicesTask(FirebaseDatabase database, String userid) {
        this.database = database;
        this.userid = userid;
    }

    @Override
    public Task<List<ServiceAvailable>> then(@NonNull Task<Void> task) throws Exception {
        final TaskCompletionSource<List<ServiceAvailable>> tcs = new TaskCompletionSource<>();

        DBUtils.getDbRefShopsServices(database, userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LogUtils.info("ServicesAvailable loaded");
                Iterator<DataSnapshot> childrenIterator = dataSnapshot.getChildren().iterator();
                List<ServiceAvailable> servicesAvailable = new ArrayList<>();
                while (childrenIterator.hasNext()) {
                    final DataSnapshot next = childrenIterator.next();
                    servicesAvailable.add(MappingUtils.mapToServiceAvailable(next));
                }
                tcs.setResult(servicesAvailable);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError dbError) {
                LogUtils.error("Error loading ServicesAvailable:{0}", dbError.getMessage());
            }
        });
        return tcs.getTask();
    }
}