package com.qcut.biz.listeners;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.qcut.biz.eventbus.EventBus;
import com.qcut.biz.events.BarberStatusChangeEvent;
import com.qcut.biz.models.Barber;
import com.qcut.biz.models.BarberQueue;
import com.qcut.biz.models.BarberStatus;
import com.qcut.biz.util.DBUtils;
import com.qcut.biz.util.LogUtils;
import com.qcut.biz.views.WaitingView;

public class BarberStatusChangeListener implements ChildEventListener {

    public void onDataChange(final DataSnapshot dataSnapshot) {
        if (!dataSnapshot.exists()) {
            return;
        }
        EventBus.instance().fireEvent(new BarberStatusChangeEvent(dataSnapshot.getValue(Barber.class)));

    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        LogUtils.info("BarberStatusChangeListener:onChildAdded");
        onDataChange(dataSnapshot);
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        LogUtils.info("BarberStatusChangeListener:onChildChanged ");
        onDataChange(dataSnapshot);
    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
}