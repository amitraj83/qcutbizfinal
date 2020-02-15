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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FindCustomerTask implements Continuation<BarberQueue, Task<Customer>> {

    private String customerKey;

    public FindCustomerTask(String customerKey) {
        this.customerKey = customerKey;
    }

    @Override
    public Task<Customer> then(@NonNull Task<BarberQueue> task) throws Exception {
        final TaskCompletionSource<Customer> tcs = new TaskCompletionSource<>();
        final BarberQueue barberQueue = task.getResult();
        boolean found = false;
        for (Customer c : barberQueue.getCustomers()) {
            if (c.getKey().equalsIgnoreCase(customerKey)) {
                found = true;
                tcs.setResult(c);
            }
        }
        if (!found) {
            tcs.setResult(null);
        }
        return tcs.getTask();
    }
}