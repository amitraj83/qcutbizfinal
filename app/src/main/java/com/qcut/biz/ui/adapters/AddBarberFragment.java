package com.qcut.biz.ui.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.qcut.biz.R;
import com.qcut.biz.models.Barber;
import com.qcut.biz.models.ServicePriceModel;
import com.qcut.biz.ui.go_online.GoOnlineModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class AddBarberFragment extends Fragment {

    private final int PICK_IMAGE_REQUEST = 71;
    private Uri filePath;

    private FirebaseDatabase database = null;
    private String userid;
    private SharedPreferences sp;

    ImageView barberUploadImageView;
    Button barberUploadButton;
    EditText newBarberName;
    ListView listBarbers;

    FirebaseStorage storage;
    StorageReference storageReference;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add_barber, container, false);
        database = FirebaseDatabase.getInstance();
        sp = getContext().getSharedPreferences("login", MODE_PRIVATE);
        userid = sp.getString("userid", null);

        barberUploadImageView = root.findViewById(R.id.barber_photo);
        barberUploadButton = root.findViewById(R.id.UploadBtn);
        newBarberName = root.findViewById(R.id.new_barber_name);
        listBarbers = root.findViewById(R.id.list_barbers);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        barberUploadImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        barberUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newBarberName == null ||
                        newBarberName.getText() == null ||
                        newBarberName.getText().toString().length() == 0) {
                    Toast.makeText(getContext(), "Invalid name.", Toast.LENGTH_SHORT).show();
                } else {
                    uploadImage();
                }
            }
        });

        showList();

        return root;
    }

    private void showList() {
        final DatabaseReference barbersRef = database.getReference().child("barbershops").child(userid)
                .child("barbers");
        barbersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                List<Barber> barberList = new ArrayList<>();
                while (iterator.hasNext()) {
                    DataSnapshot next = iterator.next();
                    DataSnapshot name = next.child("name");
                    DataSnapshot imagePath = next.child("imagePath");

                    barberList.add(new Barber(name.getValue().toString(), imagePath.getValue().toString()));
                }
                CustomAdapter customAdapter = new CustomAdapter(barberList);
                listBarbers.setAdapter(customAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void uploadImage() {
        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            final String path = "images/"+userid+"/"+ UUID.randomUUID().toString();

            final String name = newBarberName.getText().toString();

            StorageReference ref = storageReference.child(path);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();


                            final DatabaseReference barbersRef = database.getReference().child("barbershops").child(userid)
                                    .child("barbers");
                            if(barbersRef != null) {
                                String key = barbersRef.push().getKey();
//                                Uri downloadUrl =
//                                        taskSnapshot.getStorage().getDownloadUrl().getResult();
                                barbersRef.child(key).setValue(new Barber(name, taskSnapshot.getMetadata().getPath()));
                            }


                            Toast.makeText(getContext(), "Uploaded", Toast.LENGTH_SHORT).show();

                            newBarberName.getText().clear();
                            barberUploadImageView.setImageResource(R.drawable.ic_add_barber_photo_black_24dp);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null ){
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), filePath);
                barberUploadImageView.setImageBitmap(bitmap);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }


    class CustomAdapter extends BaseAdapter {

        List<Barber> spList;

        public CustomAdapter(List<Barber> spList){
            this.spList = spList;
        }

        @Override
        public int getCount() {
            return spList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            @SuppressLint("ViewHolder")
            View listView_layout = getLayoutInflater().inflate(R.layout.shop_barber, null);

            final ImageView photo = listView_layout.findViewById(R.id.barber_photo_lv);
            TextView name = listView_layout.findViewById(R.id.barber_name_lv);
            name.setText(spList.get(i).getName());
            StorageReference child = storageReference.child(spList.get(i).getImagePath());
            child.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()) {
                        Glide.with(getContext())
                                .load(task.getResult())
                                .into(photo);

                    } else {
                        Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });



            return listView_layout;
        }
    }


    /*
    private FirebaseDatabase database = null;
    private String userid;
    private SharedPreferences sp;

    ImageView barberUploadImageView;
    Button barberUploadButton;
    public static final String KEY_User_Document1 = "doc1";
    private String Document_img1="";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add_barber, container, false);
        database = FirebaseDatabase.getInstance();
        sp = getContext().getSharedPreferences("login", MODE_PRIVATE);
        userid = sp.getString("userid", null);

        barberUploadImageView = root.findViewById(R.id.barber_photo);
        barberUploadButton = root.findViewById(R.id.UploadBtn);

        barberUploadImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        barberUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        return root;
    }

    private void uploadImage() {
        if (Document_img1.equals("") || Document_img1.equals(null)) {
            ContextThemeWrapper ctw = new ContextThemeWrapper( getContext(), R.style.Theme_AppCompat_Dialog);
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctw);
            alertDialogBuilder.setTitle("Id Prof Can't Empty ");
            alertDialogBuilder.setMessage("Id Prof Can't empty please select any one document");
            alertDialogBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });
            alertDialogBuilder.show();
            return;
        }
        else{
            SendDetail();
        }
    }

    private void selectImage() {
        final CharSequence[] options = {"Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Choose from Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 2) {
                Uri selectedImage = data.getData();
                String[] filePath = { MediaStore.Images.Media.DATA };
                Cursor c = getContext().getContentResolver().query(selectedImage,filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                thumbnail = getResizedBitmap(thumbnail, 400);
                //Log.w("path of image from gallery......******************.........", picturePath+"");
                barberUploadImageView.setImageBitmap(thumbnail);
                BitMapToString(thumbnail);
            }
        }
    }

    public String BitMapToString(Bitmap userImage1) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        userImage1.compress(Bitmap.CompressFormat.PNG, 60, baos);
        byte[] b = baos.toByteArray();
        Document_img1 = Base64.encodeToString(b, Base64.DEFAULT);
        return Document_img1;
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void SendDetail() {

    }


     */
}