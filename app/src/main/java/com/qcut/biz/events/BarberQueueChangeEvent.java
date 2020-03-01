package com.qcut.biz.events;

import com.qcut.biz.eventbus.ChangeType;
import com.qcut.biz.eventbus.Event;
import com.qcut.biz.eventbus.EventHandler;
import com.qcut.biz.models.BarberQueue;

public class BarberQueueChangeEvent implements Event<BarberQueueChangeEvent.BarberQueueChangeEventHandler> {

    public static final Event.Type<BarberQueueChangeEventHandler> TYPE = new Event.Type<>();
    private BarberQueue changedBarberQueue;
    private ChangeType changeType;


    public BarberQueueChangeEvent(BarberQueue changedQueue, ChangeType changeType) {
        this.changedBarberQueue = changedQueue;
        this.changeType = changeType;
    }

    @Override
    public void dispatch(BarberQueueChangeEventHandler handler) {
        handler.onBarberQueueChange(this);
    }

    @Override
    public Type<BarberQueueChangeEventHandler> getAssociatedType() {
        return TYPE;
    }

    public BarberQueue getChangedBarberQueue() {
        return changedBarberQueue;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public interface BarberQueueChangeEventHandler extends EventHandler {
        void onBarberQueueChange(BarberQueueChangeEvent event);
    }
}
