package com.qcut.biz.events;

import com.qcut.biz.eventbus.Event;
import com.qcut.biz.eventbus.EventHandler;
import com.qcut.biz.models.Barber;
import com.qcut.biz.models.BarberQueue;

public class BarberStatusChangeEvent implements Event<BarberStatusChangeEvent.BarberStatusChangeEventHandler> {

    public static final Type<BarberStatusChangeEventHandler> TYPE = new Type<>();
    private Barber barber;

    public BarberStatusChangeEvent(Barber barber) {
        this.barber = barber;
    }

    @Override
    public void dispatch(BarberStatusChangeEventHandler handler) {
        handler.onBarberStatusChange(this);
    }

    @Override
    public Type<BarberStatusChangeEventHandler> getAssociatedType() {
        return TYPE;
    }


    public Barber getBarber() {
        return barber;
    }

    public interface BarberStatusChangeEventHandler extends EventHandler {
        void onBarberStatusChange(BarberStatusChangeEvent event);
    }
}
