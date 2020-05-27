package com.qcut.barber.events;

import com.qcut.barber.eventbus.Event;
import com.qcut.barber.eventbus.EventHandler;
import com.qcut.barber.models.Barber;

import java.util.List;

public class BarbersChangeEvent implements Event<BarbersChangeEvent.BarbersChangeEventHandler> {

    public static final Type<BarbersChangeEventHandler> TYPE = new Type<>();
    private List<Barber> barbers;


    public BarbersChangeEvent(List<Barber> barbers) {
        this.barbers = barbers;
    }

    @Override
    public void dispatch(BarbersChangeEventHandler handler) {
        handler.onBarbersChange(this);
    }

    @Override
    public Type<BarbersChangeEventHandler> getAssociatedType() {
        return TYPE;
    }

    public List<Barber> getBarbers() {
        return barbers;
    }

    public interface BarbersChangeEventHandler extends EventHandler {
        void onBarbersChange(BarbersChangeEvent event);
    }
}
