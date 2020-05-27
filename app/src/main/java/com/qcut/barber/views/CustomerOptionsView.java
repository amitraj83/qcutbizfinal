package com.qcut.barber.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.qcut.barber.R;
import com.qcut.barber.presenters.CustomerOptionPresenter;

public class CustomerOptionsView {

    Context context;
    private CustomerOptionPresenter presenter;
    PopupWindow popupWindow;
    View removeCustomer;
    View shuffleCustomer;
    String selectedCustomerKey;

    public CustomerOptionsView (Context context, final CustomerOptionPresenter presenter) {
        this.context = context;
        presenter.setView(this);
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupWindowView = inflater.inflate(R.layout.popup_window, null);

        popupWindow = new PopupWindow(popupWindowView,
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT, true);
        removeCustomer = popupWindowView.findViewById(R.id.remove_customer_row);
        shuffleCustomer = popupWindowView.findViewById(R.id.shuffle_customer_row);
        removeCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onRemoveCustomerClick(selectedCustomerKey);
                popupWindow.dismiss();
            }
        });
        shuffleCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onAssignToAnyClick(selectedCustomerKey);
                popupWindow.dismiss();
            }
        });
    }

    public void show(RecyclerView.ViewHolder holder) {
        selectedCustomerKey = holder.itemView.getTag().toString();
        popupWindow.showAsDropDown(holder.itemView.findViewById(R.id.customer_options));
    }

    public void showMessage(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
