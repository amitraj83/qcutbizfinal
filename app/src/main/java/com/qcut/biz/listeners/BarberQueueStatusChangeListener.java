package com.qcut.biz.listeners;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.qcut.biz.models.Barber;
import com.qcut.biz.models.BarberQueue;
import com.qcut.biz.models.BarberStatus;
import com.qcut.biz.util.DBUtils;
import com.qcut.biz.util.LogUtils;
import com.qcut.biz.views.WaitingView;

public class BarberQueueStatusChangeListener implements ChildEventListener {

    private FirebaseDatabase database;
    private String userid;
    private WaitingView view;

    public BarberQueueStatusChangeListener(FirebaseDatabase database, String userid, WaitingView view) {
        this.database = database;
        this.userid = userid;
        this.view = view;
    }

    public void onDataChange(final DataSnapshot dataSnapshot) {
        if (!dataSnapshot.exists()) {
            return;
        }
        LogUtils.info("dataSnapshot: {0}", dataSnapshot.getValue());

        final Barber barber = dataSnapshot.getValue(Barber.class);
        if (BarberStatus.OPEN.name().equalsIgnoreCase(barber.getQueueStatus())) {
            LogUtils.info("queueStatus: {0}", barber.getQueueStatus());
            if (!view.isTabExists(barber.getKey())) {
                view.addBarberQueueTab(barber);
                final DatabaseReference queueRef = DBUtils.getDbRefBarberQueue(database, userid, barber.getKey());
                queueRef.push().setValue(BarberQueue.builder().build());
            }
        }
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        LogUtils.info("BarberQueueStatusChangeListener:onChildAdded {0}", dataSnapshot);
        onDataChange(dataSnapshot);
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        LogUtils.info("BarberQueueStatusChangeListener:onChildChanged {0}", dataSnapshot);
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