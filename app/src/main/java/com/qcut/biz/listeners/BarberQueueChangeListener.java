package com.qcut.biz.listeners;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.qcut.biz.eventbus.ChangeType;
import com.qcut.biz.eventbus.EventBus;
import com.qcut.biz.events.BarberQueueChangeEvent;
import com.qcut.biz.util.LogUtils;
import com.qcut.biz.util.MappingUtils;

public class BarberQueueChangeListener implements ChildEventListener {

    @Override
    public void onChildAdded(@NonNull final DataSnapshot queueSnapshot, @Nullable String previouseKey) {
        fireBarberQueueChangeEvent(queueSnapshot, ChangeType.CHILD_ADDED);
    }

    public void fireBarberQueueChangeEvent(@NonNull DataSnapshot queueSnapshot, ChangeType changeType) {
        if (!queueSnapshot.exists()) {
            return;
        }
        LogUtils.info("BarberQueueChangeListener: {0}", changeType);
        final BarberQueueChangeEvent queueChangeEvent = new BarberQueueChangeEvent
                (MappingUtils.mapToBarberQueue(queueSnapshot), changeType);
        EventBus.instance().fireEvent(queueChangeEvent);
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        fireBarberQueueChangeEvent(dataSnapshot, ChangeType.CHILD_UPDATED);
    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
        fireBarberQueueChangeEvent(dataSnapshot, ChangeType.CHILD_REMOVED);
    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
}