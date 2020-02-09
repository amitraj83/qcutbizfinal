package com.qcut.biz.tasks;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qcut.biz.models.BarberQueueStatus;
import com.qcut.biz.util.DBUtils;
import com.qcut.biz.util.LogUtils;

public class FetchBarberStatusTask implements Continuation<Void, Task<BarberQueueStatus>> {

    private FirebaseDatabase database;
    private String userid;
    private String barberKey;

    public FetchBarberStatusTask(FirebaseDatabase database, String userid, String barberKey) {
        this.database = database;
        this.userid = userid;
        this.barberKey = barberKey;
    }

    @Override
    public Task<BarberQueueStatus> then(@NonNull Task<Void> task) throws Exception {
        final TaskCompletionSource<BarberQueueStatus> tcs = new TaskCompletionSource();

        DBUtils.getDbRefBarberQueueStatus(database, userid, barberKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LogUtils.info("Location loaded");
                tcs.setResult(dataSnapshot.getValue(BarberQueueStatus.class));
            }

            @Override
            public void onCancelled(DatabaseError dbError) {
                LogUtils.error("Error loading location:{0}", dbError.getMessage());
            }
        });
        return tcs.getTask();
    }
}