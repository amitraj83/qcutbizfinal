package com.qcut.biz.models;

import java.util.Comparator;

public class CustomerComparator implements Comparator<Customer> {
        @Override
        public int compare(Customer o1, Customer o2) {
            if(o1.getTimeAdded() > o2.getTimeAdded()) {
                return 1;
            } else if (o1.getTimeAdded() == o2.getTimeAdded()) {
                return 0;
            } else {
                return -1;
            }
        }
    }