package com.qcut.biz.tasks;

import androidx.annotation.NonNull;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qcut.biz.models.Barber;
import com.qcut.biz.models.BarberQueue;
import com.qcut.biz.util.DBUtils;
import com.qcut.biz.util.LogUtils;
import com.qcut.biz.util.MappingUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FetchBarbersQueuesTask implements Continuation<Map<String, Barber>, Task<List<BarberQueue>>> {

    private FirebaseDatabase database;
    private String userid;

    public FetchBarbersQueuesTask(FirebaseDatabase database, String userid) {
        this.database = database;
        this.userid = userid;
    }

    @Override
    public Task<List<BarberQueue>> then(@NonNull final Task<Map<String, Barber>> barbersMapTask) throws Exception {
        final TaskCompletionSource<List<BarberQueue>> tcs = new TaskCompletionSource();
        final Map<String, Barber> barberMap = barbersMapTask.getResult();

        DBUtils.getDbRefBarberQueues(database, userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LogUtils.info("Queues loaded");
                List<BarberQueue> barbersQueues = buildBarberQueue(dataSnapshot, barberMap);
                LogUtils.info("FetchBarbersQueuesTask: {0}", barbersQueues);
                tcs.setResult(barbersQueues);
            }

            @Override
            public void onCancelled(DatabaseError dbError) {
                LogUtils.error("Error loading location:{0}", dbError.getMessage());
            }
        });
        return tcs.getTask();
    }

    public List<BarberQueue> buildBarberQueue(DataSnapshot dataSnapshot, Map<String, Barber> barberMap) {
        Iterator<DataSnapshot> qSnapIterator = dataSnapshot.getChildren().iterator();
        List<String> availableBarbers = new ArrayList<>();
        for (Barber b : barberMap.values()) {
            if (b.isOpen() || b.isOnBreak()) {
                availableBarbers.add(b.getKey());
            }
        }
        List<BarberQueue> barbersQueues = new ArrayList<>();
        while (qSnapIterator.hasNext()) {
            final DataSnapshot queueSnapshot = qSnapIterator.next();
            final BarberQueue barberQueue = MappingUtils.mapToBarberQueue(queueSnapshot);
            barberQueue.setBarber(barberMap.get(barberQueue.getBarberKey()));
            barbersQueues.add(barberQueue);
            availableBarbers.remove(barberQueue.getBarberKey());
        }
        for (String barberKey : availableBarbers) {
            //empty queue for other available barbers for which no customer exists
            barbersQueues.add(BarberQueue.builder().barber(barberMap.get(barberKey)).barberKey(barberKey).build());
        }
        return barbersQueues;
    }
}