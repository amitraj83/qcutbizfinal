package com.qcut.barber.listeners;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.qcut.barber.eventbus.EventBus;
import com.qcut.barber.events.BarberQueuesChangeEvent;
import com.qcut.barber.util.LogUtils;
import com.qcut.barber.util.MappingUtils;

public class BarberQueueChangeListener implements ValueEventListener {

    @Override
    public void onDataChange(@NonNull DataSnapshot queuesSnapshot) {
        if (!queuesSnapshot.exists()) {
            return;
        }
        LogUtils.info("BarberQueueChangeListener..");
        final BarberQueuesChangeEvent queueChangeEvent = new BarberQueuesChangeEvent(MappingUtils
                .mapToBarberQueues(queuesSnapshot));
        EventBus.instance().fireEvent(queueChangeEvent);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        LogUtils.error("BarberQueueChangeListener:databaseError {0}", databaseError);
    }
}