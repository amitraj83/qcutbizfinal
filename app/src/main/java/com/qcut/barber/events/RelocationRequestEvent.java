package com.qcut.barber.events;

import com.qcut.barber.eventbus.Event;
import com.qcut.barber.eventbus.EventHandler;

public class RelocationRequestEvent implements Event<RelocationRequestEvent.RelocationRequestEventHandler> {

    public static final Type<RelocationRequestEventHandler> TYPE = new Type<>();

    @Override
    public void dispatch(RelocationRequestEventHandler handler) {
        handler.onRelocationRequested(this);
    }

    @Override
    public Type<RelocationRequestEventHandler> getAssociatedType() {
        return TYPE;
    }

    public interface RelocationRequestEventHandler extends EventHandler {
        void onRelocationRequested(RelocationRequestEvent event);
    }
}
