package com.qcut.biz.listeners;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.qcut.biz.models.Barber;
import com.qcut.biz.models.BarberQueue;
import com.qcut.biz.models.BarberQueueStatus;
import com.qcut.biz.util.DBUtils;
import com.qcut.biz.util.LogUtils;
import com.qcut.biz.util.Status;
import com.qcut.biz.views.fragments.WaitingView;

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

        final BarberQueueStatus queueStatus = dataSnapshot.getValue(BarberQueueStatus.class);
        if (Status.OPEN.name().equalsIgnoreCase(queueStatus.getQueueStatus())) {
            LogUtils.info("queueStatus: {0}", queueStatus);
            final String barberKey = dataSnapshot.getKey();
            DBUtils.getBarber(database, userid, barberKey, new OnSuccessListener<Barber>() {
                @Override
                public void onSuccess(Barber barber) {
                    if (!view.isTabExists(barber.getKey())) {
                        view.addBarberQueueTab(barber);
                        final DatabaseReference queueRef = DBUtils.getDbRefBarberQueue(database, userid, barberKey);
                        queueRef.push().setValue(BarberQueue.builder().build());
                    }
                }
            });
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