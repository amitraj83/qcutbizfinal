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
import com.qcut.biz.models.BarberQueue;
import com.qcut.biz.models.Customer;
import com.qcut.biz.util.DBUtils;
import com.qcut.biz.util.LogUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FindBarberQueueTask implements Continuation<List<BarberQueue>, Task<BarberQueue>> {

    private FirebaseDatabase database;
    private String userid;
    private String barberKey;

    public FindBarberQueueTask(FirebaseDatabase database, String userid, String barberKey) {
        this.database = database;
        this.userid = userid;
        this.barberKey = barberKey;
    }

    @Override
    public Task<BarberQueue> then(@NonNull final Task<List<BarberQueue>> barbersQueues) throws Exception {
        final TaskCompletionSource<BarberQueue> tcs = new TaskCompletionSource();
        for (BarberQueue bq : barbersQueues.getResult()) {
            if (bq.getBarberKey().equalsIgnoreCase(barberKey)) {
                tcs.setResult(bq);
            }
        }
        return tcs.getTask();
    }
}