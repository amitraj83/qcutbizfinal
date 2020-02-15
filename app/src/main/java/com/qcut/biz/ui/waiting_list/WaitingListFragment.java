package com.qcut.biz.ui.waiting_list;

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
import com.qcut.biz.models.Customer;
import com.qcut.biz.presenters.fragments.WaitingListPresenter;
import com.qcut.biz.util.LogUtils;
import com.qcut.biz.util.ViewUtils;
import com.qcut.biz.views.fragments.WaitingListView;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class WaitingListFragment extends Fragment implements WaitingListView {

    //    private WaitingListModel waitingListModel;
//    private Button startService, finishService;
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
    private WaitingListClickListener waitingListClickListener;
    private AlertDialog startServiceDialog;
    private View root;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public WaitingListFragment(String tag) {
        this.tag = tag;
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        presenter = new WaitingListPresenter(this, mContext, tag);
//        waitingListModel =
//                ViewModelProviders.of(this).get(WaitingListModel.class);
        root = inflater.inflate(R.layout.fragment_waiting_list, container, false);
        this.rootView = root;
//        root.setVisibility(30);
//        root.setBackgroundColor(Color.RED);

        sp = mContext.getSharedPreferences("login", MODE_PRIVATE);
        nextCustomerTV = root.findViewById(R.id.next_customer);
        factory = LayoutInflater.from(mContext);

//        cardViewStartSkipService(root, factory);

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
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelperCallback( presenter, adapter));
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

        presenter.addQueueOnChangeListener();
        presenter.setBarbersChangeListener();
        return root;
    }

//    private void cardViewStartSkipService(View root, final LayoutInflater factory) {
//        startService = root.findViewById(R.id.start_service);
//        DBUtils.getBarber(database, userid, tag, new OnSuccessListener<Barber>() {
//            @Override
//            public void onSuccess(Barber barber) {
//                if (barber != null && Status.OPEN.name().equalsIgnoreCase(barber.getQueueStatus())) {
//                    showStartServiceDialog(factory);
//                } else {
//                    showMessage("Cannot start services. May be barber is on break or his queue is stopped.");
//                }
//            }
//        });
//
//        finishService = root.findViewById(R.id.skip_customer);
//        finishService.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //showSkipServiceDialog(factory);
//            }
//        });
//
//    }
//
//    private void showStartServiceDialog(LayoutInflater factory) {
//        String queuedCustomerId = String.valueOf(nextCustomerTV.getTag());
//        if (queuedCustomerId != null &&
//                queuedCustomerId.trim().equalsIgnoreCase("none") ||
//                queuedCustomerId.trim().equalsIgnoreCase("")) {
//            Toast toast = Toast.makeText(mContext,
//                    "No Customer in the queue", Toast.LENGTH_SHORT);
//            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
//            toast.show();
//        } else {
//            final View startServiceView = factory.inflate(R.layout.start_service_dialog, null);
//            TextView startServiceCustName = startServiceView.findViewById(R.id.start_service_cust_name);
//            startServiceCustName.setText(nextCustomerTV.getText());
//            startServiceDialog = new AlertDialog.Builder(mContext).create();
//            startServiceDialog.setView(startServiceView);
//
//            startServiceDialog.show();
//            startServiceDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
//                    ViewUtils.getDisplayHeight(getActivity().getWindowManager()) / 4, getResources().getDisplayMetrics());
//            int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
//                    ViewUtils.getDisplayWidth(getActivity().getWindowManager()) / 2, getResources().getDisplayMetrics());
//
//            startServiceDialog.getWindow().setLayout(width, height);
//
//            final Button yesButton = startServiceDialog.findViewById(R.id.yes_start_service);
//            final Button noButton = startServiceDialog.findViewById(R.id.no_start_service);
//
//            yesButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    String queuedCustomerId = String.valueOf(nextCustomerTV.getTag());
//                    setCustomerInProgress(queuedCustomerId);
//                    startServiceDialog.dismiss();
//                }
//            });
//            noButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    startServiceDialog.dismiss();
//                }
//            });
//
//        }
//    }
//
//    private void setCustomerInProgress(final String queuedCustomerId) {
//        DBUtils.getBarberQueue(database, userid, tag, new OnSuccessListener<BarberQueue>() {
//            @Override
//            public void onSuccess(BarberQueue barberQueue) {
//                boolean isSomeoneInProgress = false;
//                Customer customerToBeUpdated = null;
//                for (Customer customer : barberQueue.getCustomers()) {
//                    if (Status.PROGRESS.name().equalsIgnoreCase(customer.getStatus())) {
//                        isSomeoneInProgress = true;
//                        break;
//                    }
//                    if (customer.getKey().equalsIgnoreCase(queuedCustomerId)) {
//                        customerToBeUpdated = customer;
//                    }
//                }
//                if (!isSomeoneInProgress) {
//                    customerToBeUpdated.setPlaceInQueue(-1);
//                    customerToBeUpdated.setServiceStartTime(new Date().getTime());
//                    customerToBeUpdated.setTimeToWait(0);
//                    customerToBeUpdated.setStatus(Status.PROGRESS.name());
//                    customerToBeUpdated.setTimeAdded(-1);
//                    DBUtils.getDbRefCustomer(database, userid, tag, queuedCustomerId)
//                            .setValue(queuedCustomerId, customerToBeUpdated);
//                } else {
//                    showMessage("Cannot start services. A customer is already in progress.");
//                }
//            }
//        });
//    }

    @Override
    public void showAddCustomerDialog() {
        if (addCustomerView == null) {

        }

        addCustomerDialog.show();
        addCustomerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                ViewUtils.getDisplayHeight(getActivity().getWindowManager()) / 3, getResources().getDisplayMetrics());
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                ViewUtils.getDisplayWidth(getActivity().getWindowManager()) / 2, getResources().getDisplayMetrics());

        addCustomerDialog.getWindow().setLayout(width, height);
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