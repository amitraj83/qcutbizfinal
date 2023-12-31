package com.qcut.barber.events;

import com.qcut.barber.eventbus.Event;
import com.qcut.barber.eventbus.EventHandler;
import com.qcut.barber.models.BarberQueue;

public class QueueTabSelectedEvent implements Event<QueueTabSelectedEvent.QueueTabSelectedEventHandler> {

    public static final Type<QueueTabSelectedEventHandler> TYPE = new Type<>();
    private BarberQueue barberQueue;


    public QueueTabSelectedEvent(BarberQueue queue) {
        this.barberQueue = queue;
    }

    @Override
    public void dispatch(QueueTabSelectedEventHandler handler) {
        handler.onQueueTabSelected(this);
    }

    @Override
    public Type<QueueTabSelectedEventHandler> getAssociatedType() {
        return TYPE;
    }

    public BarberQueue getBarberQueue() {
        return barberQueue;
    }


    public interface QueueTabSelectedEventHandler extends EventHandler {
        void onQueueTabSelected(QueueTabSelectedEvent event);
    }
}
