package com.qcut.biz.events;

import com.qcut.biz.eventbus.Event;
import com.qcut.biz.eventbus.EventHandler;
import com.qcut.biz.models.BarberQueue;

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
