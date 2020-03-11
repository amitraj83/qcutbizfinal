package com.qcut.biz.tasks;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.qcut.biz.models.Barber;
import com.qcut.biz.util.LogUtils;

import java.util.Map;

public class FindBarberTask implements Continuation<Map<String, Barber>, Task<Barber>> {

    private String barberKey;

    public FindBarberTask(String barberKey) {
        this.barberKey = barberKey;
    }

    @Override
    public Task<Barber> then(@NonNull Task<Map<String, Barber>> task) throws Exception {

        final TaskCompletionSource<Barber> tcs = new TaskCompletionSource<>();
        tcs.setResult(task.getResult().get(barberKey));
        return tcs.getTask();
    }
}