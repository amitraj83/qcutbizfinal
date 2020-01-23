package com.qcut.biz.ui.adapters;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class WaitingFragment extends Fragment {

    private FirebaseDatabase database = null;
    private SharedPreferences sp;
    private String userid;
    FirebaseStorage storage;
    StorageReference storageReference;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.waiting_queue, container, false);
        final TabLayout tabLayout = (TabLayout) root.findViewById(R.id.tab_layout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        sp = getContext().getSharedPreferences("login", MODE_PRIVATE);
        userid = sp.getString("userid", null);
        FirebaseApp.initializeApp(getContext());
        database = FirebaseDatabase.getInstance();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        Button viewById = root.findViewById(R.id.addTab);
        Button tabIndexTest = root.findViewById(R.id.tab_index_test);

        tabIndexTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getContext(), "Button pressed."+tabLayout.getSelectedTabPosition()+" -- "+tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getTag(), Toast.LENGTH_SHORT).show();
            }
        });

        viewById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final LayoutInflater factory = LayoutInflater.from(getContext());
                final View selectBarberView = factory.inflate(R.layout.select_barber, null);
                final AlertDialog selectBarberDialog = new AlertDialog.Builder(getContext()).create();
                selectBarberDialog.setView(selectBarberView);


                selectBarberDialog.show();
                selectBarberDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                selectBarberDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 750);

                final Button yesButton = (Button) selectBarberDialog.findViewById(R.id.yes_add_barber_queue);
                final Button noButton = (Button) selectBarberDialog.findViewById(R.id.no_add_barber_queue);


                final Spinner ddSpinner = selectBarberDialog.findViewById(R.id.spinner_select_barber_to_start_queue);

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

                            barberList.add(new Barber(next.getKey().toString(), name.getValue().toString(), imagePath.getValue().toString()));
                        }
                        BarberSelectionArrayAdapter customAdapter = new BarberSelectionArrayAdapter(getContext(), barberList);
                        ddSpinner.setAdapter(customAdapter);




                        ddSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(final AdapterView<?> adapterView, final View view, final int i, long l) {

                                final String selectedKey = ddSpinner.getAdapter().getDropDownView(i, null, null).getTag().toString();

                                yesButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {


                                        final TabLayout.Tab tab = tabLayout.newTab();
                                        tab.setTag("xyz "+new RandomString());
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
                                        selectBarberDialog.dismiss();

                                        database.getReference().child("barbershops").child(userid)
                                                .child("barbers").child(selectedKey).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists()) {
                                                    String name = dataSnapshot.child("name").getValue().toString();
                                                    tab.setText(name);
                                                }

                                                StorageReference child = storageReference.child(dataSnapshot.child("imagePath").getValue().toString());
                                                child.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Uri> task) {
                                                        if(task.isSuccessful()) {
                                                            Glide.with(getContext())
                                                                    .load(task.getResult())
                                                                    .into(new SimpleTarget<Drawable>() {

                                                                        @Override
                                                                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                                                            tab.setIcon(resource);
                                                                        }
                                                                    });

                                                        } else {
                                                            Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });



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
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });




            }
        });



        return root;
    }
}
