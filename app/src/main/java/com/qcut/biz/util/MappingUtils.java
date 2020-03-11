package com.qcut.biz.util;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.MutableData;
import com.qcut.biz.models.Barber;
import com.qcut.biz.models.BarberQueue;
import com.qcut.biz.models.Customer;
import com.qcut.biz.models.ServiceAvailable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MappingUtils {

    public static Customer mapToCustomer(DataSnapshot custSnapshot) {
        Customer customer = custSnapshot.getValue(Customer.class);
        customer.setKey(custSnapshot.getKey());
        return customer;
    }

    public static Customer mapToCustomer(MutableData custSnapshot) {
        Customer customer = custSnapshot.getValue(Customer.class);
        customer.setKey(custSnapshot.getKey());
        return customer;
    }

    public static List<BarberQueue> mapToBarberQueues(final MutableData queuesSnapshot) {
        List<BarberQueue> queues = new ArrayList<>();
        final Iterator<MutableData> iterator = queuesSnapshot.getChildren().iterator();
        while (iterator.hasNext()) {
            queues.add(mapToBarberQueue(iterator.next()));
        }
        return queues;
    }

    public static List<BarberQueue> mapToBarberQueues(final DataSnapshot queuesSnapshot) {
        List<BarberQueue> queues = new ArrayList<>();
        final Iterator<DataSnapshot> iterator = queuesSnapshot.getChildren().iterator();
        while (iterator.hasNext()) {
            queues.add(mapToBarberQueue(iterator.next()));
        }
        return queues;
    }

    public static BarberQueue mapToBarberQueue(final MutableData queueSnapshot) {
        Iterator<MutableData> custSnapIterator = queueSnapshot.getChildren().iterator();
        List<Customer> customers = new ArrayList<>();

        while (custSnapIterator.hasNext()) {
            customers.add(MappingUtils.mapToCustomer(custSnapIterator.next()));
        }
        String barberKey = queueSnapshot.getKey();
        return BarberQueue.builder().barberKey(barberKey).customers(customers).build();
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

    /**
     * Build queues based on available barber and all other barber whos status may be stopped but still has customers in queue
     * note that barber may be available but there is not instance of queue until a customer gets added to the queue
     *
     * @param queuesData
     * @param barberMap
     * @return
     */
    public static List<BarberQueue> buildBarberQueue(MutableData queuesData, Map<String, Barber> barberMap) {
        Iterator<MutableData> qSnapIterator = queuesData.getChildren().iterator();
        List<Barber> availableBarbers = new ArrayList<>(barberMap.values());
        List<BarberQueue> barbersQueues = new ArrayList<>();
        while (qSnapIterator.hasNext()) {
            final MutableData queueSnapshot = qSnapIterator.next();
            final BarberQueue barberQueue = MappingUtils.mapToBarberQueue(queueSnapshot);
            final Barber barber = barberMap.get(barberQueue.getBarberKey());
            barberQueue.setBarber(barber);
            //add to queue only when barber is available
            barbersQueues.add(barberQueue);
            removeBarber(availableBarbers, barberQueue.getBarberKey());
        }
        for (Barber barber : availableBarbers) {
            //empty queue for other available barbers for which no customer exists
            if (!barber.isStopped()) {
                barbersQueues.add(BarberQueue.builder().barber(barber).barberKey(barber.getKey()).build());
            }
        }
        return barbersQueues;
    }

    private static void removeBarber(List<Barber> availableBarbers, String barberKey) {
        final Iterator<Barber> iterator = availableBarbers.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getKey().equalsIgnoreCase(barberKey)) {
                iterator.remove();
            }
        }
    }

    public static ServiceAvailable mapToServiceAvailable(DataSnapshot serviceAvailableSnapshot) {
        final ServiceAvailable serviceAvailable = serviceAvailableSnapshot.getValue(ServiceAvailable.class);
        serviceAvailable.setKey(serviceAvailableSnapshot.getKey());
        return serviceAvailable;
    }
}
