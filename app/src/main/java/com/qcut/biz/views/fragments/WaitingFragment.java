package com.qcut.biz.views.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.qcut.biz.R;
import com.qcut.biz.adaptors.BarberSelectionArrayAdapter;
import com.qcut.biz.adaptors.PagerAdapter;
import com.qcut.biz.models.Barber;
import com.qcut.biz.models.BarberStatus;
import com.qcut.biz.presenters.fragments.WaitingPresenter;
import com.qcut.biz.util.Constants;
import com.qcut.biz.util.LogUtils;
import com.qcut.biz.views.WaitingView;

import java.util.List;

public class WaitingFragment extends Fragment implements WaitingView {

    private Context mContext;
    private Button takeBreakButton;
    private Button stopQButton;
    private WaitingPresenter presenter;
    private TabLayout tabLayout;
    private AlertDialog barberStatusChangeDialog;
    private View root;
    private AlertDialog barberSelectionDialog;
    private Spinner ddSpinner;
    private String selectedBarberKeyFromBarberDialog;
    private BarberStatus barberNewStatus;
    private ViewPager viewPager;
    private PagerAdapter adapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.waiting_queue, container, false);
        tabLayout = root.findViewById(R.id.tab_layout);
        viewPager = root.findViewById(R.id.pager);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        if (presenter == null) {
            presenter = new WaitingPresenter(this, getContext());
            View addBarber = root.findViewById(R.id.addTab);
            takeBreakButton = root.findViewById(R.id.tab_index_test);
            stopQButton = root.findViewById(R.id.stop_queue);

            stopQButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    presenter.onStopButtonClick();
                }
            });

            takeBreakButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    presenter.onTakeBreakButtonClick();
                }
            });

            presenter.initializeTab();
            addBarber.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    presenter.onAddBarberQueueTabClick();
                }
            });
            //TODO check if thats correct, do we need to create always new pager adaptor
            adapter = new PagerAdapter(getActivity().getSupportFragmentManager(), tabLayout);
            viewPager.setAdapter(adapter);
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        }
        if (barberStatusChangeDialog == null) {
            final LayoutInflater factory = LayoutInflater.from(mContext);
            final View takeBreakView = factory.inflate(R.layout.take_break_dialog, null);
            barberStatusChangeDialog = new AlertDialog.Builder(mContext).create();
            barberStatusChangeDialog.setView(takeBreakView);
            barberStatusChangeDialog.show();
            final Button yesButton = barberStatusChangeDialog.findViewById(R.id.take_break_dialog_yes);
            final Button noButton = barberStatusChangeDialog.findViewById(R.id.take_break_dialog_no);

            yesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    presenter.onBarberStatusChangeYesClick(barberNewStatus);
                }
            });

            noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideDialog();
                }
            });
            barberStatusChangeDialog.hide();
        }
        if (barberSelectionDialog == null) {
            LayoutInflater factory = LayoutInflater.from(mContext);
            View selectBarberView = factory.inflate(R.layout.select_barber, null);
            barberSelectionDialog = new AlertDialog.Builder(mContext).create();
            barberSelectionDialog.setView(selectBarberView);
            barberSelectionDialog.show();
            ddSpinner = barberSelectionDialog.findViewById(R.id.spinner_select_barber_to_start_queue);
            View yesButton = barberSelectionDialog.findViewById(R.id.yes_add_barber_queue);
            View noButton = barberSelectionDialog.findViewById(R.id.no_add_barber_queue);
            yesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    presenter.onBarberSelectionClick();

                }
            });
            ddSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(final AdapterView<?> adapterView, final View view, final int i, long l) {
                    selectedBarberKeyFromBarberDialog = ddSpinner.getAdapter().getDropDownView(i, null, null).getTag().toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
            noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideBarberSelectDialog();
                }
            });
            barberSelectionDialog.hide();
        }
        return root;
    }

    public void showBarberStatusConfirmationDialog(String dialogTitle, String dialogText, String confirmText,
                                                   final BarberStatus newStatus, String photoPath) {
        this.barberNewStatus = newStatus;
        barberStatusChangeDialog.show();
        barberStatusChangeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ((TextView) barberStatusChangeDialog.findViewById(R.id.take_break_dialog_title)).setText(dialogTitle);
        ((TextView) barberStatusChangeDialog.findViewById(R.id.take_break_text)).setText(dialogText);
        ((TextView) barberStatusChangeDialog.findViewById(R.id.take_break_confirm_text)).setText(confirmText);

        final ImageView photoView = barberStatusChangeDialog.findViewById(R.id.take_break_photo);
        presenter.getDownloadUrlAndSetInView(photoView, photoPath);

    }

    @Override
    public boolean isTabExists(String key) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            if (tabLayout.getTabAt(i).getTag().toString().equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void showBarberSelectionDialog(List<Barber> remainingBarbers) {
        barberSelectionDialog.show();
        BarberSelectionArrayAdapter customAdapter = new BarberSelectionArrayAdapter(mContext, remainingBarbers);
        ddSpinner.setAdapter(customAdapter);

        barberSelectionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    @Override
    public void hideBarberSelectDialog() {
        barberSelectionDialog.dismiss();
    }

    @Override
    public String getStopButtonText() {
        return stopQButton.getText().toString();
    }

    @Override
    public String getSelectedBarberKey() {
        return selectedBarberKeyFromBarberDialog;
    }

    @Override
    public void addBarberQueueTab(Barber barber) {
        LogUtils.info("addBarberQueueTab");

        if (isTabExists(barber.getKey())) {
            LogUtils.error("Tab already exists for: {0}, so no new tab will be added", barber.getKey());
            return;
        }
        final TabLayout.Tab tab = tabLayout.newTab();
        View customView = LayoutInflater.from(mContext).inflate(R.layout.tab_customer_layout, null);
        tab.setCustomView(customView);
        tab.setTag(barber.getKey());
        tabLayout.addTab(tab.setText("Loading...").setIcon(R.drawable.photo_barber));
        adapter.notifyDataSetChanged();
        viewPager.setCurrentItem(adapter.getCount() - 1);
        if (tabLayout.getTabCount() == 1) {
            //when only one tab onTabSelected does not called so we have to manually trigger tab initialization
            presenter.onBarberQueueTabSelected(tab.getTag().toString());
        }
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(final TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                presenter.onBarberQueueTabSelected(tab.getTag().toString());
                ((TextView) tab.getCustomView().findViewById(R.id.tab_name)).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        ((TextView) customView.findViewById(R.id.tab_name)).setText(barber.getName());
        presenter.updateBarberStatus(barber.getKey());
        presenter.getDownloadUri(barber.getImagePath(), new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri downloadUri) {
                if (downloadUri == null) {
                    showMessage("Image downloading failed..");
                } else {
                    RequestOptions myOptions = new RequestOptions().override(100, 100);
                    Glide.with(mContext).asBitmap().apply(myOptions).load(downloadUri)
                            .into((ImageView) tab.getCustomView().findViewById(R.id.tab_image));
                }
            }
        });
    }

    @Override
    public void showMessage(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setButtonToBarberOnBreak() {
        takeBreakButton.setText(Constants.ON_BREAK);
        takeBreakButton.setTextColor(Color.RED);
    }

    @Override
    public void resetBarberBreakButton() {
        takeBreakButton.setText(Constants.BREAK);
        takeBreakButton.setTextColor(getResources().getColor(R.color.backgroundItems));
    }

    @Override
    public void hideDialog() {
        barberStatusChangeDialog.dismiss();
    }

    @Override
    public void updateButtonToStopped() {
        stopQButton.setText(Constants.STOPPED);
        stopQButton.setTextColor(Color.RED);
    }

    @Override
    public void updateButtonToStopQ() {
        stopQButton.setText(Constants.STOP_QUEUE);
        stopQButton.setTextColor(getResources().getColor(R.color.backgroundItems));
    }

    @Override
    public String getSelectedTabId() {
        return tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getTag().toString();
    }

    @Override
    public void setPhotoUrl(ImageView photo, Uri result) {
        Glide.with(getContext()).load(result).into(photo);
    }
}
