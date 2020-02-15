package com.qcut.biz.listeners;

import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qcut.biz.R;
import com.qcut.biz.models.Barber;
import com.qcut.biz.models.BarberQueue;
import com.qcut.biz.models.Customer;
import com.qcut.biz.presenters.fragments.WaitingListPresenter;
import com.qcut.biz.ui.waiting_list.BarberSelectionArrayAdapter;
import com.qcut.biz.util.Constants;
import com.qcut.biz.util.DBUtils;
import com.qcut.biz.util.LogUtils;
import com.qcut.biz.util.MappingUtils;
import com.qcut.biz.util.Status;
import com.qcut.biz.util.TimeUtil;
import com.qcut.biz.util.TimerService;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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