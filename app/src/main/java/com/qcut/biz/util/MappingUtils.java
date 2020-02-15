package com.qcut.biz.util;

import com.google.firebase.database.DataSnapshot;
import com.qcut.biz.models.Barber;
import com.qcut.biz.models.BarberQueue;
import com.qcut.biz.models.Customer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MappingUtils {

    public static Customer mapToCustomer(DataSnapshot custSnapshot) {
        Customer customer = custSnapshot.getValue(Customer.class);
        customer.setKey(custSnapshot.getKey());
        return customer;
    }

    public static BarberQueue mapToBarberQueue(final DataSnapshot queueSnapshot) {
        Iterator<DataSnapshot> custSnapIterator = queueSnapshot.getChildren().iterator();
        List<Customer> customers = new ArrayList<>();

        while (custSnapIterator.hasNext()) {
            customers.add(MappingUtils.mapToCustomer(custSnapIterator.next()));
        }
        String barberKey = queueSnapshot.getKey();
        return BarberQueue.builder().barberKey(barberKey).customers(customers).build();
    }

    public static Barber mapToBarber(DataSnapshot snapshot) {
        final Barber barber = snapshot.getValue(Barber.class);
        barber.setKey(snapshot.getKey());
        return barber;
    }
}
