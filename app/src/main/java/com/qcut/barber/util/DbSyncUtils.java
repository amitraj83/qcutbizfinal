package com.qcut.barber.util;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

public class DbSyncUtils {

    public static <T> T loadSynchronous(DatabaseReference databaseReference, Class<T> clazz) {
        return loadSynchronous(databaseReference).getValue(clazz);
    }

    public static DataSnapshot loadSynchronous(Query databaseReference) {
        final DataSnapshotWrapper snapshotWrapper = new DataSnapshotWrapper();
        final CountDownLatch latch = new CountDownLatch(1);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LogUtils.info("Location loaded");
                snapshotWrapper.snapshot = dataSnapshot;
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError dbError) {
                LogUtils.error("Error loading location:{0}", dbError.getMessage());
                latch.countDown();
            }
        });
        try {
            LogUtils.info("Prelatch: {0}", databaseReference);
            latch.await();
            LogUtils.info("Returning from latch");
            return snapshotWrapper.snapshot;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static DataSnapshot loadSynchronous(DatabaseReference databaseReference) {
        final DataSnapshotWrapper snapshotWrapper = new DataSnapshotWrapper();
//        final CountDownLatch latch = new CountDownLatch(1);
        final Semaphore s=new Semaphore(1);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LogUtils.info("Location loaded");
                snapshotWrapper.snapshot = dataSnapshot;
                s.release();
//                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError dbError) {
                LogUtils.error("Error loading location:{0}", dbError.getMessage());
                s.release();
//                latch.countDown();
            }
        });
//        try {

            LogUtils.info("Prelatch: {0}", databaseReference);
        try {
            s.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//            latch.await();
            LogUtils.info("Returning from latch");
            return snapshotWrapper.snapshot;
//        } catch (InterruptedException e) {
//            LogUtils.error("Error loading location: ", e);
//            return null;
//        }
    }


    public static Uri loadUriSynchronous(StorageReference storageReference, final String imagePath) {
        final CountDownLatch latch = new CountDownLatch(1);
        final DownloadUrlWrapper downloadUrlWrapper = new DownloadUrlWrapper();
        StorageReference child = storageReference.child(imagePath);
        child.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    downloadUrlWrapper.downloadUri = task.getResult();
                } else {
                    LogUtils.error("Problem while getting image url at path: {0}. {1}",
                            imagePath, task.getException().getMessage());
                }
                latch.countDown();
            }

        });
        try {
            LogUtils.info("Prelatch: {0}", storageReference);
            latch.await();
            LogUtils.info("Returning from latch");
            return downloadUrlWrapper.downloadUri;
        } catch (InterruptedException e) {
            LogUtils.error("Problem while getting image url at path: {0}.", e, imagePath);
            return null;
        }
    }

    private static class DataSnapshotWrapper {
        private DataSnapshot snapshot;
    }

    private static class DownloadUrlWrapper {
        private Uri downloadUri;
    }
}