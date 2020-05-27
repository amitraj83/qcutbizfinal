package com.qcut.barber.listeners;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.qcut.barber.eventbus.EventBus;
import com.qcut.barber.events.BarbersChangeEvent;
import com.qcut.barber.models.Barber;
import com.qcut.barber.util.LogUtils;
import com.qcut.barber.util.MappingUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BarbersChangeListener implements ValueEventListener {

    @Override
    public void onDataChange(@NonNull final DataSnapshot barbersSnapshot) {
        if (!barbersSnapshot.exists()) {
            return;
        }
        LogUtils.info("BarbersChangeListener..");
        final Iterator<DataSnapshot> iterator = barbersSnapshot.getChildren().iterator();
        List<Barber> barberList = new ArrayList<>();
        while (iterator.hasNext()) {
            barberList.add(MappingUtils.mapToBarber(iterator.next()));
        }
        EventBus.instance().fireEvent(new BarbersChangeEvent(barberList));
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        LogUtils.error("BarbersChangeListener:databaseError {0}", databaseError);
    }
}