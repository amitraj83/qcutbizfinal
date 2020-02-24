package com.qcut.biz.listeners;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.qcut.biz.adaptors.WaitingListRecyclerViewAdapter;
import com.qcut.biz.models.BarberQueue;
import com.qcut.biz.models.Customer;
import com.qcut.biz.models.CustomerComparator;
import com.qcut.biz.models.CustomerStatus;
import com.qcut.biz.presenters.fragments.WaitingListPresenter;
import com.qcut.biz.util.DBUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {
    private int dragFrom = -1;
    private int dragTo = -1;
    private WaitingListRecyclerViewAdapter adapter;
    private WaitingListPresenter waitingListPresenter;

    public ItemTouchHelperCallback(WaitingListPresenter waitingListPresenter, WaitingListRecyclerViewAdapter adapter) {
        this.waitingListPresenter = waitingListPresenter;
        this.adapter = adapter;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
    }

    @Override
    public boolean onMove(@NonNull final RecyclerView recyclerView, @NonNull final RecyclerView.ViewHolder dragged,
                          @NonNull final RecyclerView.ViewHolder target) {
        FirebaseDatabase database = waitingListPresenter.getDatabase();
        String userid = waitingListPresenter.getUserId();
        String barberKey = waitingListPresenter.getBarberKey();
        DBUtils.getBarberQueue(database, userid, barberKey, new OnSuccessListener<BarberQueue>() {
            @Override
            public void onSuccess(BarberQueue barberQueue) {
                //TODO use  orderByChild(Constants.Customer.STATUS).equalTo(BarberStatus.PROGRESS.name());
                for (Customer customer : barberQueue.getCustomers()) {
                    if (CustomerStatus.PROGRESS.name().equalsIgnoreCase(customer.getStatus())) {
                        waitingListPresenter.showMessage("A customer in chair. Cannot drag or drop others.");
                        return;
                    }
                }
                final int position_dragged = dragged.getAdapterPosition();
                final String status = ((WaitingListRecyclerViewAdapter.MyViewHolder) recyclerView.findViewHolderForAdapterPosition(position_dragged))
                        .custStatus.getTag().toString();
                if (status.equalsIgnoreCase(CustomerStatus.QUEUE.name())) {
                    final int position_target = target.getAdapterPosition();
                    Collections.swap(adapter.getDataSet(), position_dragged, position_target);
                    if (dragFrom == -1) {
                        dragFrom = position_dragged;
                    }
                    dragTo = position_target;
                    adapter.notifyItemMoved(position_dragged, position_target);
                }
            }
        });
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public void clearView(final RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        final FirebaseDatabase database = waitingListPresenter.getDatabase();
        final String userid = waitingListPresenter.getUserId();
        final String barberKey = waitingListPresenter.getBarberKey();
        DBUtils.getBarberQueue(database, userid, barberKey, new OnSuccessListener<BarberQueue>() {
            @Override
            public void onSuccess(BarberQueue barberQueue) {
                List<Customer> customers = new ArrayList<>();
                for (Customer customer : barberQueue.getCustomers()) {
                    if (CustomerStatus.QUEUE.name().equalsIgnoreCase(customer.getStatus())) {
                        customers.add(customer);
                    }
                }
                Collections.sort(customers, new CustomerComparator());
                int count = 0;
                int itemCount = recyclerView.getAdapter().getItemCount();
                Map<String, Object> timeToUpdate = new HashMap<>();
                for (int i = 0; i < itemCount; i++) {
                    final String sourceTag = recyclerView.findViewHolderForAdapterPosition(i)
                            .itemView.getTag().toString();
                    final String status = ((WaitingListRecyclerViewAdapter.MyViewHolder) recyclerView
                            .findViewHolderForAdapterPosition(i)).custStatus.getTag().toString();
                    if (status.equalsIgnoreCase(CustomerStatus.QUEUE.name())) {
                        timeToUpdate.put(sourceTag + "/timeAdded", i);
                        timeToUpdate.put(sourceTag + "/expectedWaitingTime", customers.get(count++).getExpectedWaitingTime());
                    }
                }
                DBUtils.getDbRefBarberQueue(database, userid, barberKey).updateChildren(timeToUpdate);
            }
        });
    }
}
