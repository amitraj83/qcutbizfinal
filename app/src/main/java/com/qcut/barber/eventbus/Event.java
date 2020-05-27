package com.qcut.barber.eventbus;

public interface Event<H extends EventHandler> {

    void dispatch(H handler);

    Event.Type<H> getAssociatedType();

    public class Type<H> {

    }
}
