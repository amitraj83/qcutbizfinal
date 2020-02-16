package com.qcut.biz.tasks;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.FirebaseDatabase;
import com.qcut.biz.models.BarberQueue;

import java.util.List;

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
        final TaskCompletionSource<BarberQueue> tcs = new TaskCompletionSource<>();
        boolean found = false;
        for (BarberQueue bq : barbersQueues.getResult()) {
            if (bq.getBarberKey().equalsIgnoreCase(barberKey)) {
                tcs.setResult(bq);
                found = true;
                break;
            }
        }
        if (!found) {
            tcs.setResult(null);
        }
        return tcs.getTask();
    }
}