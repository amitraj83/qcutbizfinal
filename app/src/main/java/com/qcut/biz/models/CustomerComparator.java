package com.qcut.biz.models;

import java.util.Comparator;

public class CustomerComparator implements Comparator<Customer> {
    @Override
    public int compare(Customer o1, Customer o2) {
        long c1Time = getCustomerTime(o1);
        long c2Time = getCustomerTime(o2);
        if (c1Time > c2Time) {
            return 1;
        } else if (c1Time == c2Time) {
            return 0;
        } else {
            return -1;
        }
    }

    public static long getCustomerTime(Customer customer) {
        return customer.getDragAdjustedTime() == 0 ? customer.getArrivalTime() : customer.getDragAdjustedTime();
    }
}