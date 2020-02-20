package com.qcut.biz.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qcut.biz.R;
import com.qcut.biz.adaptors.CustomerViewAdapter;
import com.qcut.biz.presenters.fragments.CustomerViewPresenter;
import com.qcut.biz.views.CustomerView;

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
}