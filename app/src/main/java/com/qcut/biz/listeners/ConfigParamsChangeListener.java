package com.qcut.biz.listeners;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.qcut.biz.models.ConfigParams;
import com.qcut.biz.util.LogUtils;

public abstract class ConfigParamsChangeListener implements ValueEventListener {

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        LogUtils.info("ConfigParams loaded");
        if (dataSnapshot.exists()) {
            onDataChange(dataSnapshot.getValue(ConfigParams.class));
        }
    }

    protected abstract void onDataChange(ConfigParams configParams);

    @Override
    public void onCancelled(DatabaseError dbError) {
        LogUtils.error("Error loading location:{0}", dbError.getMessage());
    }
}
