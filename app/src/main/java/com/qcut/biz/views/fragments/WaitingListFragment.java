package com.qcut.biz.views.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.qcut.biz.R;
import com.qcut.biz.adaptors.BarberSelectionArrayAdapter;
import com.qcut.biz.adaptors.WaitingListRecyclerViewAdapter;
import com.qcut.biz.listeners.ItemTouchHelperCallback;
import com.qcut.biz.models.Customer;
import com.qcut.biz.presenters.fragments.WaitingListPresenter;
import com.qcut.biz.util.LogUtils;
import com.qcut.biz.util.ViewUtils;
import com.qcut.biz.views.WaitingListView;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class WaitingListFragment extends Fragment implements WaitingListView {

    private FloatingActionButton addCustomer;
    private SharedPreferences sp;
    private WaitingListRecyclerViewAdapter adapter = null;
    private RecyclerView dynamicListView = null;
    private TextView nextCustomerTV;
    private String tag;
    private Context mContext;
    private View rootView;
    private WaitingListPresenter presenter;
    private View addCustomerView;
    private AlertDialog addCustomerDialog;
    private Button yesButton;
    private Spinner ddSpinner;
    private Button noButton;
    private EditText customerNameInput;
    private LayoutInflater factory;
    private String selectedBarberKey;
    private View root;

    public WaitingListFragment(String tag) {
        this.tag = tag;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (presenter == null) {
            presenter = new WaitingListPresenter(this, mContext, tag);
        }
        presenter.addQueueOnChangeListener();
        presenter.addBarbersChangeListener();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        presenter.removeQueueOnChangeListener();
        presenter.removeBarbersChangeListener();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_waiting_list, container, false);
        this.rootView = root;
        sp = mContext.getSharedPreferences("login", MODE_PRIVATE);
        nextCustomerTV = root.findViewById(R.id.next_customer);
        factory = LayoutInflater.from(mContext);


//TODO remove textview
//        TextView viewById = root.findViewById(R.id.textView);
//        viewById.setText(tag);

        addCustomer = root.findViewById(R.id.add_customer_fab);

        addCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onAddCustomerClick();
            }
        });

        dynamicListView = root.findViewById(R.id.today_queue);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        dynamicListView.setLayoutManager(mLayoutManager);
        dynamicListView.setItemAnimator(new DefaultItemAnimator());
        adapter = presenter.createWaitingListViewAdaptor();
        dynamicListView.setAdapter(adapter);
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelperCallback(presenter, adapter));
        helper.attachToRecyclerView(dynamicListView);

        addCustomerView = factory.inflate(R.layout.add_customer_dialog, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(addCustomerView);
        addCustomerDialog = builder.create();
        yesButton = addCustomerView.findViewById(R.id.add_customer_dialog_yes);
        noButton = addCustomerView.findViewById(R.id.add_customer_dialog_no);
        customerNameInput = addCustomerView.findViewById(R.id.new_customer_name);
        ddSpinner = addCustomerView.findViewById(R.id.spinner_barber_selection);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCustomerDialog.dismiss();
            }
        });
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onCustomerAddYesClick();
            }
        });
        ddSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedBarberKey = ddSpinner.getAdapter().getDropDownView(i, null, null).getTag().toString();
                LogUtils.info("onBarberSelected: {0}", selectedBarberKey);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return root;
    }

    @Override
    public void showAddCustomerDialog() {
        addCustomerDialog.show();
        addCustomerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
//                ViewUtils.getDisplayHeight(getActivity().getWindowManager()) / 3, getResources().getDisplayMetrics());
//        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
//                ViewUtils.getDisplayWidth(getActivity().getWindowManager()) / 2, getResources().getDisplayMetrics());
//
//        addCustomerDialog.getWindow().setLayout(width, height);
    }

    public String getEnteredCustomerName() {
        return customerNameInput.getText().toString().trim();
    }

    @Override
    public void hideAddCustomerDialog() {
        addCustomerDialog.hide();
    }

    @Override
    public void startDoorBell() {
        MediaPlayer mediaPlayer = MediaPlayer.create(mContext, R.raw.door_bell);
        mediaPlayer.start();
    }

    @Override
    public void setBarberList(BarberSelectionArrayAdapter barberSelectionAdapter) {
        ddSpinner.setAdapter(barberSelectionAdapter);
    }

    @Override
    public void updateNextCustomerView(String customerName, String customerKey) {
        nextCustomerTV.setText(customerName);
        nextCustomerTV.setTag(customerKey);
    }

    @Override
    public void updateAndRefreshQueue(List<Customer> customers) {
        adapter.setDataSet(customers);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void updateBarberStatus(boolean onBreak) {
        if (onBreak) {
            root.findViewById(R.id.barber_on_break_message).setVisibility(View.VISIBLE);
            root.findViewById(R.id.next_customer_card).setVisibility(View.INVISIBLE);
            root.findViewById(R.id.cardView).setBackgroundColor(Color.YELLOW);
        } else {
            root.findViewById(R.id.cardView).setBackgroundColor(Color.WHITE);
            root.findViewById(R.id.next_customer_card).setVisibility(View.VISIBLE);
            root.findViewById(R.id.barber_on_break_message).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public String getSelectedBarberKey() {
        return selectedBarberKey;
    }

    @Override
    public void setYesButtonEnable(boolean enabled) {
        yesButton.setEnabled(enabled);
        yesButton.playSoundEffect(0);
    }

    @Override
    public void showMessage(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }
}