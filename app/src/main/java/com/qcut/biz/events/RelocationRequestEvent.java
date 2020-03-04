package com.qcut.biz.events;

import com.qcut.biz.eventbus.Event;
import com.qcut.biz.eventbus.EventHandler;

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
