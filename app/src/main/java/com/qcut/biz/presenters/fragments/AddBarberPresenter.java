package com.qcut.biz.presenters.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.qcut.biz.models.Barber;
import com.qcut.biz.models.ShopDetails;
import com.qcut.biz.util.DBUtils;
import com.qcut.biz.util.LogUtils;
import com.qcut.biz.views.AddBarberView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;

public class AddBarberPresenter {

    private String userid;
    private AddBarberView view;
    private SharedPreferences preferences;
    private Context context;
    private FirebaseDatabase database;
    private StorageReference storageReference;
    private ShopDetails shopDetails;

    public AddBarberPresenter(AddBarberView view, Context context) {
        this.view = view;
        this.context = context;
        this.preferences = context.getSharedPreferences("login", MODE_PRIVATE);
        FirebaseApp.initializeApp(context);
        database = FirebaseDatabase.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        userid = preferences.getString("userid", null);
        DBUtils.getShopDetails(database, userid, new OnSuccessListener<ShopDetails>() {
            @Override
            public void onSuccess(ShopDetails shopDetails) {
                AddBarberPresenter.this.shopDetails = shopDetails;
            }
        });
    }


    public void onUploadImageClick() {
        view.chooseImage();
    }

    public void onUploadButtonClick(Uri filePath) {
        String newBarberName = view.getEnteredBarberName();
        if (StringUtils.isBlank(newBarberName)) {
            view.showMessage("Invalid name.");
        } else {
            uploadImage(filePath);
        }
    }

    private void uploadImage(Uri filePath) {
        if (filePath != null) {
            view.showProgressDialog("Uploading...");
            final String path = "images/" + userid + "/" + UUID.randomUUID().toString();

            final String name = view.getEnteredBarberName();
            StorageReference ref = storageReference.child(path);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            view.hideProgressDialog();
                            final DatabaseReference barbersRef = DBUtils.getDbRefBarbers(database, userid);
                            if (barbersRef != null) {
                                //create barber
                                String key = barbersRef.push().getKey();
                                barbersRef.child(key).setValue(Barber.builder().key(key).name(name)
                                        .imagePath(taskSnapshot.getMetadata().getPath())
                                        //copy avgTimeToCut from shopDetails
                                        .avgTimeToCut(getAvgTimeToCut()).build());
                                LogUtils.info("Image uploaded successfully.");
                            }
                            view.showMessage("Uploaded");
                            view.resetFileUploadBox();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            view.hideProgressDialog();
                            view.showMessage("Failed " + e.getMessage());
                            LogUtils.error("Error while uploading image: {0}", e, e.getMessage());
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            view.setProgressDialogMsg("Uploaded " + (int) progress + "%");
                            LogUtils.info("Image uploading in progress.");
                        }
                    });
        }
    }

    public long getAvgTimeToCut() {
        if (shopDetails == null || shopDetails.getAvgTimeToCut() == 0) {
            return 15;
        }
        return shopDetails.getAvgTimeToCut();
    }

    public void populateBarbers() {
        DBUtils.getDbRefBarbers(database, userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                List<Barber> barberList = new ArrayList<>();
                while (iterator.hasNext()) {
                    DataSnapshot next = iterator.next();
                    Barber barber = next.getValue(Barber.class);
                    barber.setKey(next.getKey());
                    barberList.add(barber);
                }
                view.showBarbersList(barberList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                LogUtils.error("Error while getting barbers: {0}", databaseError.getMessage());
            }
        });
    }

    public void getDownloadUrlAndSetInView(final ImageView photo, final String imagePath) {
        StorageReference child = storageReference.child(imagePath);
        child.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    view.setPhotoUrl(photo, task.getResult());
                } else {
                    String message = task.getException().getMessage();
                    view.showMessage(message);
                    LogUtils.error("Problem while getting image url at path: {0}. {1}", imagePath, message);
                }
            }
        });
    }
}
