package com.qcut.barber.tasks;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.qcut.barber.models.BarberQueue;
import com.qcut.barber.models.Customer;

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