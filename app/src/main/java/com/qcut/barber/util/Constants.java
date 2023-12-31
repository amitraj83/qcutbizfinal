package com.qcut.barber.util;

public interface Constants {

    String ANY = "Any";
    String STOPPED = "STOPPED";
    String STOP_QUEUE = "Log Off";
    String ON_BREAK = "On Break";
    String BREAK = "Break";
    String NONE = "NONE";


    public interface Customer {
        String NAME = "name";
        String PLACE_IN_QUEUE = "placeInQueue";
        String SKIP_COUNT = "skipcount";
        String TIME_TO_WAIT = "timeToWait";
        String STATUS = "status";
        String TIME_ADDED = "timeAdded";
        String TIME_FIRST_ADDED_IN_QUEUE = "timeFirstAddedInQueue";
        String CUSTOMER_ID = "customerId";
        String IS_ANY_BARBER = "anyBarber";
        String TIME_SERVICE_STARTED = "timeServiceStarted";

    }
}
