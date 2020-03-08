package com.qcut.biz.listeners;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.qcut.biz.eventbus.ChangeType;
import com.qcut.biz.eventbus.EventBus;
import com.qcut.biz.events.BarberQueuesChangeEvent;
import com.qcut.biz.util.LogUtils;
import com.qcut.biz.util.MappingUtils;

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