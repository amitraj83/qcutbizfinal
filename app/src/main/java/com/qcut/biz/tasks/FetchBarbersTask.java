package com.qcut.biz.tasks;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qcut.biz.models.Barber;
import com.qcut.biz.util.DBUtils;
import com.qcut.biz.util.LogUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FetchBarbersTask implements Continuation<Void, Task<Map<String, Barber>>> {

    private FirebaseDatabase database;
    private String userid;

    public FetchBarbersTask(FirebaseDatabase database, String userid) {
        this.database = database;
        this.userid = userid;
    }

    @Override
    public Task<Map<String, Barber>> then(@NonNull Task<Void> task) throws Exception {
        final TaskCompletionSource<Map<String, Barber>> tcs = new TaskCompletionSource();

        DBUtils.getDbRefBarbers(database, userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LogUtils.info("Barbers loaded");
                Iterator<DataSnapshot> childrenIterator = dataSnapshot.getChildren().iterator();
                Map<String, Barber> barbersMap = new HashMap<>();
                while (childrenIterator.hasNext()) {
                    final DataSnapshot next = childrenIterator.next();
                    Barber barber = next.getValue(Barber.class);
                    barber.setKey(next.getKey());
                    barbersMap.put(barber.getKey(), barber);
                }
                LogUtils.info("FetchBarbersTask: {0}",barbersMap);
                tcs.setResult(barbersMap);
            }

            @Override
            public void onCancelled(DatabaseError dbError) {
                LogUtils.error("Error loading location:{0}", dbError.getMessage());
            }
        });
        return tcs.getTask();
    }
}