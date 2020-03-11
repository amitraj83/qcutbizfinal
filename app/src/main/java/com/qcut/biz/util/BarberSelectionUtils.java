package com.qcut.biz.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.qcut.biz.eventbus.EventBus;
import com.qcut.biz.events.RelocationRequestEvent;
import com.qcut.biz.models.Barber;
import com.qcut.biz.models.BarberQueue;
import com.qcut.biz.models.Customer;
import com.qcut.biz.models.CustomerStatus;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class BarberSelectionUtils {

    public static void assignBarber(final FirebaseDatabase database, final String userid,
                                    final Customer.CustomerBuilder customerBuilder,
                                    final Map<String, Barber> barberMap) {
        final DatabaseReference dbRefBarberQueues = DBUtils.getDbRefBarberQueues(database, userid);
        dbRefBarberQueues.runTransaction(new Transaction.Handler() {

            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                //doTransaction may called again if transaction in not committed in first attempt
                final List<BarberQueue> barberQueues = MappingUtils.buildBarberQueue(mutableData, barberMap);
                final Customer tempCust = customerBuilder.build();
                if (tempCust.isAnyBarber()) {
                    //assign to any available barber
                    final List<BarberSorted> barberSortedList = sortedListOfBarbers(barberQueues);
                    if (barberSortedList.size() > 0) {
                        final String key = barberSortedList.get(0).getKey();
                        saveCustomerToBarberQueue(dbRefBarberQueues, barberQueues, key, customerBuilder, mutableData);
                    }
                } else {
                    //assign to preferred barber
                    final String preferredBarberKey = tempCust.getPreferredBarberKey();
                    saveCustomerToBarberQueue(dbRefBarberQueues, barberQueues, preferredBarberKey, customerBuilder, mutableData);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    LogUtils.error("databaseError: {0}", databaseError.getMessage());
                    return;
                }
                LogUtils.info("Customer added to queue");
                //customer is added to a queue successfully, now relocate customers in all queues
                EventBus.instance().fireEvent(new RelocationRequestEvent());
            }

            private void saveCustomerToBarberQueue(DatabaseReference dbRefBarberQueues, List<BarberQueue> barberQueues, String barberKey,
                                                   Customer.CustomerBuilder customerBuilder, MutableData queuesMutableData) {
                final BarberQueue queue = DBUtils.findBarberQueueByKey(barberQueues, barberKey);
                final Pair<Integer, Long> waitingInfo = calculateNewCustomerWaitingInfo(queue);
                final long now = new Date().getTime();
                final Customer newCustomer = customerBuilder.actualBarberId(barberKey).arrivalTime(now)
                        .expectedWaitingTime(waitingInfo.getRight()).placeInQueue(waitingInfo.getLeft())
                        .timeAdded(now).status(CustomerStatus.QUEUE.name())
                        .key(dbRefBarberQueues.child(barberKey).push().getKey()).build();
                DBUtils.saveCustomer(newCustomer, queuesMutableData.child(barberKey));
            }
        });

    }


    public static void reAllocateCustomers(final FirebaseDatabase database, final String userId,
                                           final Map<String, Barber> barberMap) {
        final DatabaseReference dbRefBarberQueues = DBUtils.getDbRefBarberQueues(database, userId);
        dbRefBarberQueues.runTransaction(new Transaction.Handler() {

            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                final List<BarberQueue> barberQueues = MappingUtils.buildBarberQueue(mutableData, barberMap);
                //this queue include available and un available barbers, because barber may have customer
                // but he is logged out, so we need to distribute those customer among other barbers
                List<BarberSorted> barberSortedList = sortedListOfBarbersForReAllocation(barberQueues);
                Set<Customer> allCustomers = removeAndGetAllCustomerToBeAddedLater(database, userId, barberQueues, mutableData);
                assignCustomersToBarbers(dbRefBarberQueues, barberSortedList, allCustomers, mutableData);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    LogUtils.error("databaseError: {0}", databaseError.getMessage());
                    return;
                }
                //customers are reallocated successfully, now update expected waiting times
                TimerService.updateWaitingTimes(database, userId, barberMap);
            }
        });
    }

    private static Pair<Integer, Long> calculateNewCustomerWaitingInfo(BarberQueue queue) {
        long waitingTime = 0;
        if (queue == null) {
            return Pair.of(0, waitingTime);
        }
        long avgServiceTime = queue.getBarber().getAvgTimeToCut() == 0 ? Barber.DEFAULT_AVG_TIME_TO_CUT
                : queue.getBarber().getAvgTimeToCut();//if avgTimeToCut not defined in barber than use default
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
        avgServiceTime = avgServiceTime == 0 ? Barber.DEFAULT_AVG_TIME_TO_CUT : avgServiceTime;//if avgTimeToCut not defined in barber than use default
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


    private static List<BarberSorted> sortedListOfBarbersForReAllocation(List<BarberQueue> queues) {
        List<BarberSorted> barberSortedList = new ArrayList<>();
        for (BarberQueue queue : queues) {
            if (queue.getBarber().isStopped()) {
                //ignore barbers that are not available
                continue;
            }
            long remainingMinsToComplete = -1;
            for (Customer customer : queue.getCustomers()) {
                if (customer.isInProgress()) {
                    remainingMinsToComplete = calculateRemainingMinsToComplete(customer, queue.getBarber().getAvgTimeToCut());
                }
            }
            long avgServiceTime = queue.getBarber().getAvgTimeToCut() == 0 ? Barber.DEFAULT_AVG_TIME_TO_CUT
                    : queue.getBarber().getAvgTimeToCut();//if avgTimeToCut not defined in barber than use default
            barberSortedList.add(BarberSorted.builder().key(queue.getBarberKey())
                    .avgServiceTime(avgServiceTime).timeToGetAvailable(remainingMinsToComplete).build());
        }
        Collections.sort(barberSortedList, new BarberComparator());
        return barberSortedList;
    }


    private static void assignCustomersToBarbers(DatabaseReference dbRefBarberQueues, List<BarberSorted> barberSortedList,
                                                 Set<Customer> allCustomers, MutableData mutableData) {
        int placeInQueue = 1;
        for (Customer customer : allCustomers) {
            String barberKey;
            if (customer.isAnyBarber()) {
                barberKey = barberSortedList.get(0).getKey();
            } else {
                barberKey = customer.getPreferredBarberKey();
            }
            customer.setTimeAdded(placeInQueue++);
//            customer.setKey(dbRefBarberQueues.child(barberKey).push().getKey());
            DBUtils.saveCustomer(customer, mutableData.child(barberKey));

            for (BarberSorted barberSorted : barberSortedList) {
                if (barberSorted.getKey().equalsIgnoreCase(barberKey)) {
                    long timeToGetAvailable = barberSorted.getTimeToGetAvailable();
                    barberSorted.setTimeToGetAvailable(timeToGetAvailable + barberSorted.getAvgServiceTime());
                }
            }
            Collections.sort(barberSortedList, new BarberComparator());
        }
    }

    private static Set<Customer> removeAndGetAllCustomerToBeAddedLater(FirebaseDatabase database, String userid, @NonNull List<BarberQueue> queues, MutableData mutableData) {
        Set<Customer> allCustomers = new TreeSet<>(new CustComparatorBasedOnArrivalTime());
        for (BarberQueue queue : queues) {
            for (Customer customer : queue.getCustomers()) {
                if (customer.isInQueue()) {
                    allCustomers.add(customer);
                    removeCustomer(mutableData, queue.getBarberKey(), customer.getKey());
                    //clear key
//                    customer.setKey(null);
                }
            }
        }
        return allCustomers;
    }

    private static void removeCustomer(MutableData mutableData, String barberKey, String customerKey) {
        mutableData.child(barberKey).child(customerKey).setValue(null);
    }

    /**
     * @param queues
     * @return
     */
    private static List<BarberSorted> sortedListOfBarbers(@NonNull List<BarberQueue> queues) {
        List<BarberSorted> barberSortedList = new ArrayList<>();
        for (BarberQueue queue : queues) {
            if (queue.getBarber().isStopped()) {
                //ignore barbers that are not available
                continue;
            }
            long waitingTimeOfLastCustomer = -1;
            final int totalCustomers = queue.getCustomers().size();
            if (totalCustomers > 0) {
                waitingTimeOfLastCustomer = queue.getCustomers().get(totalCustomers - 1).getExpectedWaitingTime();
            }
            long avgServiceTime = queue.getBarber().getAvgTimeToCut() == 0 ? Barber.DEFAULT_AVG_TIME_TO_CUT
                    : queue.getBarber().getAvgTimeToCut();//if avgTimeToCut not defined in barber than use default
            barberSortedList.add(BarberSorted.builder().key(queue.getBarberKey()).avgServiceTime(avgServiceTime)
                    .timeToGetAvailable(waitingTimeOfLastCustomer).build());
        }

        Collections.sort(barberSortedList, new BarberComparator());
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
        private long avgServiceTime;
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
