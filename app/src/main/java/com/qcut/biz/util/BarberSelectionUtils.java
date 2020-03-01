package com.qcut.biz.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.qcut.biz.models.BarberQueue;
import com.qcut.biz.models.Customer;
import com.qcut.biz.models.CustomerStatus;
import com.qcut.biz.views.WaitingListView;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class BarberSelectionUtils {

    public static void assignBarber(final FirebaseDatabase database, final String userid, final Customer.CustomerBuilder customerBuilder, final WaitingListView view) {
        DBUtils.getBarbersQueues(database, userid, new OnSuccessListener<List<BarberQueue>>() {
            @Override
            public void onSuccess(List<BarberQueue> barberQueues) {
                final Customer tempCust = customerBuilder.build();
                if (tempCust.isAnyBarber()) {
                    final List<BarberSorted> barberSortedList = sortedListOfBarbers(barberQueues);
                    if (barberSortedList.size() > 0) {
                        final String key = barberSortedList.get(0).getKey();
                        saveCustomerToBarberQueue(barberQueues, key, customerBuilder, database, userid);
                    }
                } else {
                    final String preferredBarberKey = tempCust.getPreferredBarberKey();
                    saveCustomerToBarberQueue(barberQueues, preferredBarberKey, customerBuilder, database, userid);
                }
                LogUtils.info("Customer added to queue");
                view.showMessage(" added to queue");
                view.startDoorBell();
            }

            private void saveCustomerToBarberQueue(List<BarberQueue> barberQueues, String barberKey, Customer.CustomerBuilder customerBuilder, FirebaseDatabase database, String userid) {
                final BarberQueue queue = DBUtils.findBarberQueueByKey(barberQueues, barberKey);
                final Pair<Integer, Long> waitingInfo = calculateNewCustomerWaitingInfo(queue);
                final long now = new Date().getTime();
                Customer newCustomer = customerBuilder.actualBarberId(barberKey).arrivalTime(now)
                        .expectedWaitingTime(waitingInfo.getRight()).placeInQueue(waitingInfo.getLeft())
                        .timeAdded(now).status(CustomerStatus.QUEUE.name()).build();
                DBUtils.saveCustomer(database, userid, newCustomer, barberKey);
            }
        });
    }

    public static void reAllocateCustomers(final FirebaseDatabase database, final String userId) {
        final long avgServiceTime = 15;// Long.valueOf(dataSnapshot.getValue().toString()) * 60 * 1000;
        DBUtils.getBarbersQueues(database, userId, new OnSuccessListener<List<BarberQueue>>() {
            @Override
            public void onSuccess(final List<BarberQueue> barberQueues) {
                DBUtils.getDbRefBarberQueues(database, userId).runTransaction(new Transaction.Handler() {
                    //TODO if single transaction can have multiple dbref commit
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        List<BarberSorted> barberSortedList = sortedListOfBarbersForReAllocation(barberQueues, avgServiceTime);
                        Set<Customer> allCustomers = removeAndGetAllCustomerToBeAddedLater(database, userId, barberQueues);
                        assignCustomersToBarbers(database, userId, barberSortedList, allCustomers, avgServiceTime);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                        if (databaseError != null) {
                            LogUtils.error("databaseError: {}", databaseError.getMessage());
                        }
                        TimerService.updateWaitingTimes(database, userId);
                    }
                });
            }
        });

    }

    private static Pair<Integer, Long> calculateNewCustomerWaitingInfo(BarberQueue queue) {
        long waitingTime = 0;
        if (queue == null) {
            return Pair.of(0, waitingTime);
        }
        long avgServiceTime = 15;//TODO consider avServiceTime from db
        int lastCustPlaceInQueue = 0;
        for (Customer customer : queue.getCustomers()) {
            if (customer.isInQueue()) {
                waitingTime = waitingTime + avgServiceTime;
                lastCustPlaceInQueue++;
            } else if (customer.isInProgress()) {
                waitingTime = waitingTime + calculateRemainingMinsToComplete(customer, avgServiceTime);
            }
        }
        return Pair.of(lastCustPlaceInQueue + 1, waitingTime);
    }

    private static long calculateRemainingMinsToComplete(Customer customer, long avgServiceTime) {
        long minutesPassedSinceStarted = ((new Date().getTime() - customer.getServiceStartTime()) / 1000) / 60;
        long minsRemaining = avgServiceTime - minutesPassedSinceStarted;

        if (minsRemaining < 0) {
            //it may take more than avgServiceTime
            final long absDiffInMins = Math.abs(minsRemaining);
            if (absDiffInMins < 5) {
                minsRemaining = 5 - absDiffInMins;
            } else if (absDiffInMins < 10) {
                minsRemaining = 10 - absDiffInMins;
            } else if (absDiffInMins < 15) {
                minsRemaining = 15 - absDiffInMins;
            } else if (absDiffInMins < 20) {
                minsRemaining = 20 - absDiffInMins;
            } else if (absDiffInMins < 25) {
                minsRemaining = 25 - absDiffInMins;
            } else if (absDiffInMins < 30) {
                minsRemaining = 30 - absDiffInMins;
            }
        }
        return Math.max(0, minsRemaining);
    }


    private static List<BarberSorted> sortedListOfBarbersForReAllocation(List<BarberQueue> queues, long avgServiceTime) {
        List<BarberSorted> barberSortedList = new ArrayList<>();
        for (BarberQueue queue : queues) {
            long remainingMinsToComplete = -1;
            for (Customer customer : queue.getCustomers()) {
                if (customer.isInProgress()) {
                    remainingMinsToComplete = calculateRemainingMinsToComplete(customer, avgServiceTime);
                }
            }
            barberSortedList.add(BarberSorted.builder().key(queue.getBarberKey()).timeToGetAvailable(remainingMinsToComplete).build());
        }
        Collections.sort(barberSortedList, new BarberComparator());
        LogUtils.info("sortedListOfBarbersForReAllocation: {0}", barberSortedList);
        return barberSortedList;
    }


    private static void assignCustomersToBarbers(final FirebaseDatabase database, final String userid,
                                                 List<BarberSorted> barberSortedList, Set<Customer> allCustomers, long avgServiceTime) {
        int placeInQueue = 1;
        for (Customer customer : allCustomers) {
            String barberKey;
            if (customer.isAnyBarber()) {
                barberKey = barberSortedList.get(0).getKey();
            } else {
                barberKey = customer.getPreferredBarberKey();
            }
            customer.setTimeAdded(placeInQueue++);
            DBUtils.saveCustomer(database, userid, customer, barberKey);

            for (BarberSorted barberSorted : barberSortedList) {
                if (barberSorted.getKey().equalsIgnoreCase(barberKey)) {
                    long timeToGetAvailable = barberSorted.getTimeToGetAvailable();
                    barberSorted.setTimeToGetAvailable(timeToGetAvailable + avgServiceTime);
                }
            }
            Collections.sort(barberSortedList, new BarberComparator());
        }
    }

    private static Set<Customer> removeAndGetAllCustomerToBeAddedLater(FirebaseDatabase database, String userid, @NonNull List<BarberQueue> queues) {
        Set<Customer> allCustomers = new TreeSet<>(new CustComparatorBasedOnArrivalTime());
        for (BarberQueue queue : queues) {
            for (Customer customer : queue.getCustomers()) {
                if (customer.isInQueue()) {
                    allCustomers.add(customer);
                    DBUtils.getDbRefCustomer(database, userid, queue.getBarberKey(), customer.getKey()).removeValue();
                    //clear key
                    customer.setKey(null);
                }
            }
        }
        return allCustomers;
    }

    /**
     * @param queues
     * @return
     */
    private static List<BarberSorted> sortedListOfBarbers(@NonNull List<BarberQueue> queues) {
        List<BarberSorted> barberSortedList = new ArrayList<>();
        for (BarberQueue queue : queues) {
            long waitingTimeOfLastCustomer = -1;
            final int totalCustomers = queue.getCustomers().size();
            if (totalCustomers > 0) {
                waitingTimeOfLastCustomer = queue.getCustomers().get(totalCustomers - 1).getExpectedWaitingTime();
            }
            barberSortedList.add(BarberSorted.builder().key(queue.getBarberKey())
                    .timeToGetAvailable(waitingTimeOfLastCustomer).build());
        }

        Collections.sort(barberSortedList, new BarberComparator());
        LogUtils.info("barberSortedList: {0}", barberSortedList);
        return barberSortedList;
    }

    private static class CustComparatorBasedOnArrivalTime implements Comparator<Customer> {
        @Override
        public int compare(Customer o1, Customer o2) {
            //sort based on arrival time ascending order
            if (o1.getArrivalTime() > o2.getArrivalTime()) {
                return 1;
            } else if (o1.getArrivalTime() == o2.getArrivalTime()) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    @Getter
    @Setter
    @Builder
    private static class BarberSorted {
        private String key;
        private long timeToGetAvailable;
    }

    private static class BarberComparator implements Comparator<BarberSorted> {
        @Override
        public int compare(BarberSorted o1, BarberSorted o2) {
            //sort based on ascending waiting time
            if (o1.getTimeToGetAvailable() > o2.getTimeToGetAvailable()) {
                return 1;
            } else if (o1.getTimeToGetAvailable() == o2.getTimeToGetAvailable()) {
                return 0;
            } else {
                return -1;
            }
        }
    }
}