package com.qcut.biz.ui.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.qcut.biz.R;
import com.qcut.biz.models.Barber;
import com.qcut.biz.ui.waiting_list.BarberSelectionArrayAdapter;
import com.qcut.biz.util.RandomString;
import com.qcut.biz.util.Status;
import com.qcut.biz.util.TimeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class WaitingFragment extends Fragment {

    private FirebaseDatabase database = null;
    private SharedPreferences sp;
    private String userid;
    FirebaseStorage storage;
    StorageReference storageReference;
    private Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.waiting_queue, container, false);
        final TabLayout tabLayout = (TabLayout) root.findViewById(R.id.tab_layout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        sp = mContext.getSharedPreferences("login", MODE_PRIVATE);
        userid = sp.getString("userid", null);
        FirebaseApp.initializeApp(mContext);
        database = FirebaseDatabase.getInstance();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        final Button addBarber = root.findViewById(R.id.addTab);
        Button tabIndexTest = root.findViewById(R.id.tab_index_test);

        tabIndexTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(mContext, "Button pressed."+tabLayout.getSelectedTabPosition()+" -- "+tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getTag(), Toast.LENGTH_SHORT).show();
            }
        });

        final DatabaseReference barbersRef = database.getReference().child("barbershops").child(userid);


                ////////////////////
        barbersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        Iterator<DataSnapshot> iterator = dataSnapshot.child("barbers").getChildren().iterator();
                        final List<Barber> barberList = new ArrayList<>();
                        while (iterator.hasNext()) {
                            final DataSnapshot next = iterator.next();
                            final DataSnapshot name = next.child("name");
                            final DataSnapshot imagePath = next.child("imagePath");
                            boolean bq = dataSnapshot.child("queues").child(TimeUtil.getTodayDDMMYYYY()).child(next.getKey()).exists();
                            if(!bq) {
                                barberList.add(new Barber(next.getKey().toString(), name.getValue().toString(), imagePath.getValue().toString()));
                            }
                            else {
                                boolean tabExists =false;
                                for (int i = 0; i < tabLayout.getTabCount(); i++){
                                    if(tabLayout.getTabAt(i).getTag().toString().equalsIgnoreCase(next.getKey().toString())) {
                                        tabExists = true;
                                    }
                                }
                                if(!tabExists){
                                    addTab(name.getValue().toString(), tabLayout, root, next.getKey(), imagePath.getValue().toString(), dataSnapshot);
                                }
                            }
                        }


                        addBarber.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                final LayoutInflater factory = LayoutInflater.from(mContext);
                                final View selectBarberView = factory.inflate(R.layout.select_barber, null);
                                final AlertDialog selectBarberDialog = new AlertDialog.Builder(mContext).create();
                                selectBarberDialog.setView(selectBarberView);


                                selectBarberDialog.show();
                                selectBarberDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                                selectBarberDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 750);

                                final Button yesButton = (Button) selectBarberDialog.findViewById(R.id.yes_add_barber_queue);
                                final Button noButton = (Button) selectBarberDialog.findViewById(R.id.no_add_barber_queue);




                                //----------------------------
                                final Spinner ddSpinner = selectBarberDialog.findViewById(R.id.spinner_select_barber_to_start_queue);


                        BarberSelectionArrayAdapter customAdapter = new BarberSelectionArrayAdapter(mContext, barberList);
                        ddSpinner.setAdapter(customAdapter);




                        ddSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(final AdapterView<?> adapterView, final View view, final int i, long l) {

                                final String selectedKey = ddSpinner.getAdapter().getDropDownView(i, null, null).getTag().toString();

                                yesButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        String name = dataSnapshot.child("barbers").child(selectedKey).child("name").getValue().toString();
                                        String imagePath = dataSnapshot.child("barbers").child(selectedKey).child("imagePath").getValue().toString();

                                        addTab(name, tabLayout, root, selectedKey, imagePath, dataSnapshot);

                                        selectBarberDialog.dismiss();

                                    }
                                });
                                noButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        selectBarberDialog.dismiss();
                                    }
                                });

                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });


                        //----------------------------------
                            }
                        });


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


////////////////////////////////



        return root;
    }

    private void addTab(String name, TabLayout tabLayout, View root, String selectedKey, String imagePath, @NonNull DataSnapshot dataSnapshot) {
        final TabLayout.Tab tab = tabLayout.newTab();
        tab.setTag(selectedKey);
        tabLayout.addTab(tab.setText("Loading...").setIcon(R.drawable.photo_barber));

        final ViewPager viewPager = (ViewPager) root.findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter(getActivity().getSupportFragmentManager(), tabLayout.getTabCount(), tabLayout);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        tab.setText(name);
        createBarberQueue(selectedKey);


//        StorageReference child = storageReference.child();
        StorageReference child = storageReference.child(imagePath);
        child.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()) {
                    Glide.with(mContext)
                            .load(task.getResult())
                            .into(new SimpleTarget<Drawable>() {
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    tab.setIcon(resource);
                                }
                            });
                } else {
                    Toast.makeText(mContext, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createBarberQueue(String selectedKey) {
        DatabaseReference queue = database.getReference().child("barbershops").child(userid)
                .child("queues").child(TimeUtil.getTodayDDMMYYYY());

        Map<String, Object> map = new HashMap<>();
        map.put("status", Status.OPEN);
        Task<Void> voidTask = queue.child(selectedKey).setValue(map);
        voidTask.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });




    }
}
