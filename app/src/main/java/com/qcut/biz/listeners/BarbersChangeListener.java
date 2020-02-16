package com.qcut.biz.listeners;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.qcut.biz.models.Barber;
import com.qcut.biz.presenters.fragments.WaitingListPresenter;
import com.qcut.biz.util.Constants;
import com.qcut.biz.util.LogUtils;
import com.qcut.biz.util.MappingUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BarbersChangeListener implements ValueEventListener {

    private WaitingListPresenter presenter;

    public BarbersChangeListener(WaitingListPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onDataChange(@NonNull final DataSnapshot barbersSnapshot) {
        if (!barbersSnapshot.exists()) {
            return;
        }

        LogUtils.info("BarbersChangeListener:onDataChange {0}", barbersSnapshot);
        final Iterator<DataSnapshot> iterator = barbersSnapshot.getChildren().iterator();
        List<Barber> barberList = new ArrayList<>();
        barberList.add(Barber.builder().key(Constants.ANY).name(Constants.ANY).imagePath("").build());
        while (iterator.hasNext()) {
            final Barber barber = MappingUtils.mapToBarber(iterator.next());
            if (!barber.isStopped()) {
                barberList.add(barber);
            }

            if (barber.getKey().equalsIgnoreCase(presenter.getBarberKey())) {
                presenter.updateBarberStatus(barber.isOnBreak());
            }
        }
        presenter.setBarberList(barberList);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

}