package com.qcut.biz.eventbus;

import com.google.android.gms.common.util.CollectionUtils;
import com.qcut.biz.util.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventBus {

    private static EventBus INSTANCE = new EventBus();
    private Map<Event.Type, List<EventHandler>> eventHandlerMap = new HashMap<>();

    private EventBus() {

    }

    public static EventBus instance() {
        return INSTANCE;
    }

    public void registerHandler(Event.Type type, EventHandler handler) {
        List<EventHandler> eventHandlers = eventHandlerMap.get(type);
        if (eventHandlers == null) {
            eventHandlers = new ArrayList<>();
            eventHandlerMap.put(type, eventHandlers);
        }
        eventHandlers.add(handler);
    }

    public void registerHandlerOnce(Event.Type type, EventHandler handler) {
        List<EventHandler> eventHandlers = eventHandlerMap.get(type);
        if (eventHandlers == null) {
            eventHandlers = new ArrayList<>();
            eventHandlerMap.put(type, eventHandlers);
        }
        if (eventHandlers.size() == 0) {
            eventHandlers.add(handler);
        }
    }

    public boolean unregisterHandler(Event.Type type, EventHandler handler) {
        List<EventHandler> eventHandlers = eventHandlerMap.get(type);
        if (!CollectionUtils.isEmpty(eventHandlers)) {
            return eventHandlers.remove(handler);
        }
        return false;
    }

    public <T> void fireEvent(Event event) {
        LogUtils.info("Event fired: {0}", event.getClass());
        final List<EventHandler> eventHandlers = eventHandlerMap.get(event.getAssociatedType());
        if (CollectionUtils.isEmpty(eventHandlers)) {
            //no-one subscribed to the event
            return;
        }
        for (EventHandler handler : eventHandlers) {
            try {
                event.dispatch(handler);
            } catch (Exception ex) {
                LogUtils.error("Error while dipatching event: {0}", event, ex);
            }
        }
    }
}
