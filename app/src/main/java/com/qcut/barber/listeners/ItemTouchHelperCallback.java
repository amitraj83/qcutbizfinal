package com.qcut.barber.listeners;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.SuccessContinuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.qcut.barber.adaptors.WaitingListRecyclerViewAdapter;
import com.qcut.barber.models.Barber;
import com.qcut.barber.models.Customer;
import com.qcut.barber.models.CustomerComparator;
import com.qcut.barber.presenters.fragments.WaitingListPresenter;
import com.qcut.barber.util.DBUtils;
import com.qcut.barber.util.LogUtils;
import com.qcut.barber.util.TimerService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private WaitingListPresenter waitingListPresenter;
    private Customer draggedCustomer;

    public ItemTouchHelperCallback(WaitingListPresenter waitingListPresenter) {
        this.waitingListPresenter = waitingListPresenter;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
    }

    @Override
    public boolean onMove(@NonNull final RecyclerView recyclerView, @NonNull final RecyclerView.ViewHolder dragged,
                          @NonNull final RecyclerView.ViewHolder target) {
        draggedCustomer = null;
        final List<Customer> customers = getDataSet(recyclerView.getAdapter());
        for (Customer customer : customers) {
            if (customer.isInProgress()) {
                //in any customer is in progress drag is not allowed
                waitingListPresenter.showMessage("A customer in chair. Cannot drag or drop others.");
                return true;
            }
        }

        final int position_dragged = dragged.getAdapterPosition();
        Customer draggedCustomer = getCustomerFromViewHolder(dragged);
        if (draggedCustomer.isInQueue()) {
            final int position_target = target.getAdapterPosition();
            final RecyclerView.Adapter adapter = recyclerView.getAdapter();
            Collections.swap(customers, position_dragged, position_target);
            //if not moved to first then, get arrival time of one customer ahead in queue
            // note position starts with 0 and index also starts with 0
            long beforeCustomerTime = position_target == 0 ? -1 : getCustomerTime(customers, position_target - 1);
            long afterCustomerTime = position_target == customers.size() - 1 ? -1 : getCustomerTime(customers, position_target + 1);
            if (beforeCustomerTime == -1 && afterCustomerTime != -1) {
                beforeCustomerTime = afterCustomerTime - 10;
            } else if (afterCustomerTime == -1 && beforeCustomerTime != -1) {
                afterCustomerTime = beforeCustomerTime + 10;
            }
            //there is only one customer in queue no need to change anything
            boolean dragNeeded = beforeCustomerTime != -1 && afterCustomerTime != -1;

            final long arrivalTime = CustomerComparator.getCustomerTime(draggedCustomer);
            //if customer is already in between than drag not required
            dragNeeded = dragNeeded && (arrivalTime < beforeCustomerTime || arrivalTime > afterCustomerTime);
            if (dragNeeded) {
                long newDragTime = beforeCustomerTime + ((afterCustomerTime - beforeCustomerTime) / 2);
                draggedCustomer.setDragAdjustedTime(newDragTime);
                this.draggedCustomer = draggedCustomer;
                adapter.notifyItemMoved(position_dragged, position_target);
            } else {
                LogUtils.info("Customer is already in correct position so drag time change not required");
            }
        }
        return true;
    }

    public long getCustomerTime(List<Customer> customers, int index) {
        return CustomerComparator.getCustomerTime(customers.get(index));
    }

    public List<Customer> getDataSet(RecyclerView.Adapter adapter) {
        return ((WaitingListRecyclerViewAdapter) adapter).getDataSet();
    }

    public Customer getCustomerFromViewHolder(@NonNull RecyclerView.ViewHolder dragged) {
        return ((WaitingListRecyclerViewAdapter.MyViewHolder) dragged).getCustomer();
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

//    @Override
//    public float getMoveThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
//        //TODO Consider overriding it
//        return super.getMoveThreshold(viewHolder);
//    }

    @Override
    public void clearView(final RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        if (draggedCustomer == null) {
            //nothing was dragged
            return;
        }
        final FirebaseDatabase database = waitingListPresenter.getDatabase();
        final String userid = waitingListPresenter.getUserId();
        final String barberKey = waitingListPresenter.getBarberKey();
        Map<String, Object> updateProperties = new HashMap<>();
        updateProperties.put(draggedCustomer.getKey() + "/" + Customer.DRAG_ADJUSTED_TIME, draggedCustomer.getDragAdjustedTime());
        DBUtils.getDbRefBarberQueue(database, userid, barberKey).updateChildren(updateProperties).onSuccessTask(new SuccessContinuation<Void, Object>() {
            @NonNull
            @Override
            public Task<Object> then(@Nullable Void aVoid) throws Exception {
                DBUtils.getBarbers(database, userid, new OnSuccessListener<Map<String, Barber>>() {
                    @Override
                    public void onSuccess(Map<String, Barber> barbersMap) {
                        TimerService.updateWaitingTimes(database, userid, barbersMap);
                    }
                });
                return null;
            }
        });

    }
}
