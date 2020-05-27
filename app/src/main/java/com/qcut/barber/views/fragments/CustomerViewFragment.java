package com.qcut.barber.views.fragments;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qcut.barber.R;
import com.qcut.barber.adaptors.CustomerViewAdapter;
import com.qcut.barber.presenters.fragments.CustomerViewPresenter;
import com.qcut.barber.views.CustomerView;

public class CustomerViewFragment extends Fragment implements CustomerView {


    private CustomerViewPresenter presenter;
    private RecyclerView customerViewLV;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (presenter == null) {
            presenter = new CustomerViewPresenter(this, context);
        }
        presenter.addBarberQueueChangeListener();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.customer_view, container, false);
        final Context context = getContext();
        presenter = new CustomerViewPresenter(this, context);
        customerViewLV = root.findViewById(R.id.global_queue);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context.getApplicationContext());
        customerViewLV.setLayoutManager(mLayoutManager);
        customerViewLV.setItemAnimator(new DefaultItemAnimator());
        return root;
    }

    @Override
    public void setCustomerViewAdaptor(CustomerViewAdapter adapter) {
        customerViewLV.setAdapter(adapter);
    }

    @Override
    public void showMessage(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void startDoorBell() {
        MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), R.raw.door_bell);
        mediaPlayer.start();
    }
}