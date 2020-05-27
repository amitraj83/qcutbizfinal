package com.qcut.barber.events;

import com.qcut.barber.eventbus.Event;
import com.qcut.barber.eventbus.EventHandler;
import com.qcut.barber.models.BarberQueue;

import java.util.List;

public class BarberQueuesChangeEvent implements Event<BarberQueuesChangeEvent.BarberQueuesChangeEventHandler> {

    public static final Event.Type<BarberQueuesChangeEventHandler> TYPE = new Event.Type<>();
    private List<BarberQueue> barberQueues;


    public BarberQueuesChangeEvent(List<BarberQueue> barberQueues) {
        this.barberQueues = barberQueues;
    }

    @Override
    public void dispatch(BarberQueuesChangeEventHandler handler) {
        handler.onBarberQueuesChange(this);
    }

    @Override
    public Type<BarberQueuesChangeEventHandler> getAssociatedType() {
        return TYPE;
    }

    public List<BarberQueue> getBarberQueues() {
        return barberQueues;
    }

    public interface BarberQueuesChangeEventHandler extends EventHandler {
        void onBarberQueuesChange(BarberQueuesChangeEvent event);
    }
}
