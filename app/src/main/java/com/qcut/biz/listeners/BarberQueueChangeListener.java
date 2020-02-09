package com.qcut.biz.listeners;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.qcut.biz.models.BarberQueue;
import com.qcut.biz.models.Customer;
import com.qcut.biz.util.DBUtils;
import com.qcut.biz.util.LogUtils;
import com.qcut.biz.util.Status;
import com.qcut.biz.util.TimerService;
import com.qcut.biz.views.fragments.WaitingView;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BarberQueueChangeListener implements ChildEventListener {

    private FirebaseDatabase database;
    private String userid;

    public BarberQueueChangeListener(FirebaseDatabase database, String userid) {
        this.database = database;
        this.userid = userid;
    }

    @Override
    public void onChildAdded(@NonNull final DataSnapshot queueSnapshot, @Nullable String previouseKey) {
        if (!queueSnapshot.exists()) {
            return;
        }
        LogUtils.info("BarberQueueChangeListener:onChildAdded {0}", queueSnapshot);

        DBUtils.getBarbersQueues(database, userid, new OnSuccessListener<List<BarberQueue>>() {
            @Override
            public void onSuccess(List<BarberQueue> barberQueues) {
                String barberKey = queueSnapshot.getKey();
                BarberQueue queue = DBUtils.findKeyObjectFromChildren(barberQueues, barberKey);
                if (queue != null && queue.getCustomers().size() > 0) {
                    //if this barber already has customer in queue then don't add new customers
                    return;
                }
                //A new barber checked in, add customer from existing queue
                shiftACustomerFromExistingQueues(barberKey, barberQueues);
            }
        });
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

    private void shiftACustomerFromExistingQueues(String receiverBarberKey, List<BarberQueue> queues) {
        Pair<BarberQueue, Customer> donatedCustomerWithDonorQueue = findDonatedCustomerSnapshot(receiverBarberKey, queues);
        if (donatedCustomerWithDonorQueue != null) {
            moveCustomerFromDonorQueue(donatedCustomerWithDonorQueue.getLeft(),
                    donatedCustomerWithDonorQueue.getRight(), receiverBarberKey);
        } else {
            LogUtils.error("Can't move customer to new barber queue, no eligible customer found.");
        }
    }

    private void moveCustomerFromDonorQueue(BarberQueue donerQueue,
                                            Customer donatedCustomer, String receiverBarberKey) {
        LogUtils.info("Moving customer {0} to barber {1}", donatedCustomer, receiverBarberKey);
        TimerService.updateWaitingTimes(database, userid);
    }

    private Pair<BarberQueue, Customer> findDonatedCustomerSnapshot(String receiverBarberKey, List<BarberQueue> queues) {
        Iterator<BarberQueue> queuesIterator = queues.iterator();
        //BarberA - Any1A(time - 110),Any2A(time - 210),A3A(time - 310)
        //BarberB - 1B(time - 50),2B(time - 150),3B(time - 250),Any4B(time - 350)
        //BarberC - Any1C(time - 70),Any2C(time - 170)
        //BarberD - D1(time - 5),Any2D(time - 105)
        //BarberE - Any2D (most eligible)
        Pair<BarberQueue, Customer> eligibleQueueAndCustomer = Pair.of(null, null);
        while (queuesIterator.hasNext()) {
            BarberQueue queue = queuesIterator.next();
            if (!queue.getBarberKey().equals(receiverBarberKey)) {//it should not be receiver barber
                for (Customer customer : queue.getCustomers()) {
                    Customer foundCustomer = compareAndGetMostEligibleCustomer(eligibleQueueAndCustomer.getRight(), customer);
                    if (eligibleQueueAndCustomer.getRight() != foundCustomer) {
                        eligibleQueueAndCustomer = Pair.of(queue, foundCustomer);
                    }
                }
            }
        }
        return eligibleQueueAndCustomer;
    }

    private Customer compareAndGetMostEligibleCustomer(Customer c1, Customer c2) {
        if (c1 == null && c2 == null) {
            return null;
        }
        if (c1 != null && isEligibleToCompare(c1) && c2 == null) {
            return c1;
        } else if (c2 != null && isEligibleToCompare(c2) && c1 == null) {
            return c2;
        }
        if (c1 != null && c2 != null) {
            if (isEligibleToCompare(c1) && isEligibleToCompare(c2)) {
                Customer customerBasedOnArrivalTime = null;

                //TODO this need to be discussed
                if (c1.getArrivalTime() > c2.getArrivalTime() && !c2.isAbsent()) {
                    customerBasedOnArrivalTime = c2;
                } else {
                    customerBasedOnArrivalTime = c1;
                }
                return customerBasedOnArrivalTime;
            } else if (isEligibleToCompare(c1)) {
                return c1;
            } else if (isEligibleToCompare(c2)) {
                return c2;
            }
            //both have barber preferred barber or non eligible to choose, so non is eligible
        }
        //one is null and other is non eligible to choose
        return null;
    }

    /**
     * Check if customer is eligible for comparison
     *
     * @param c
     * @return
     */
    private boolean isEligibleToCompare(Customer c) {
        return !c.getStatus().equalsIgnoreCase(Status.PROGRESS.name()) && c.isAnyBarber();
    }
}