package com.qcut.biz.ui.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
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
import com.qcut.biz.models.Barber;
import com.qcut.biz.models.BarberQueueStatus;
import com.qcut.biz.presenters.fragments.WaitingPresenter;
import com.qcut.biz.ui.waiting_list.BarberSelectionArrayAdapter;
import com.qcut.biz.util.Constants;
import com.qcut.biz.util.LogUtils;
import com.qcut.biz.util.ViewUtils;
import com.qcut.biz.views.fragments.WaitingView;

import java.util.List;

public class WaitingFragment extends Fragment implements WaitingView {

    private Context mContext;
    private Button takeBreakButton;
    private Button stopQButton;
    private Button closeQButton;
    private View addBarber;
    private WaitingPresenter presenter;
    private TabLayout tabLayout;
    private AlertDialog dialog;
    private View root;
    private AlertDialog barberSelectionDialog;
    private Spinner ddSpinner;
    private String selectedBarberKeyFromBarberDialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.waiting_queue, container, false);
        tabLayout = (TabLayout) root.findViewById(R.id.tab_layout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        presenter = new WaitingPresenter(this, getContext());
        addBarber = root.findViewById(R.id.addTab);
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
        return root;
    }

    public void showDialog(String dialogTitle, String dialogText, String confirmText,
                           final BarberQueueStatus newStatus, String photoPath) {
        if (dialog == null) {
            final LayoutInflater factory = LayoutInflater.from(mContext);
            final View takeBreakView = factory.inflate(R.layout.take_break_dialog, null);
            dialog = new AlertDialog.Builder(mContext).create();
            dialog.setView(takeBreakView);
        }
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                ViewUtils.getDisplayHeight(getActivity().getWindowManager()) / 3, getResources().getDisplayMetrics());
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                ViewUtils.getDisplayWidth(getActivity().getWindowManager()) / 2, getResources().getDisplayMetrics());

        dialog.getWindow().setLayout(width, height);

        ((TextView) dialog.findViewById(R.id.take_break_dialog_title)).setText(dialogTitle);
        ((TextView) dialog.findViewById(R.id.take_break_text)).setText(dialogText);
        ((TextView) dialog.findViewById(R.id.take_break_confirm_text)).setText(confirmText);

        final ImageView photoView = dialog.findViewById(R.id.take_break_photo);
        presenter.getDownloadUrlAndSetInView(photoView, photoPath);

        final Button yesButton = dialog.findViewById(R.id.take_break_dialog_yes);
        final Button noButton = dialog.findViewById(R.id.take_break_dialog_no);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onDialogYesButtonClick(newStatus);
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideDialog();
            }
        });
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
        if (barberSelectionDialog == null) {
            LayoutInflater factory = LayoutInflater.from(mContext);
            View selectBarberView = factory.inflate(R.layout.select_barber, null);
            barberSelectionDialog = new AlertDialog.Builder(mContext).create();
            barberSelectionDialog.setView(selectBarberView);
            barberSelectionDialog.show();
            ddSpinner = barberSelectionDialog.findViewById(R.id.spinner_select_barber_to_start_queue);
        }
        barberSelectionDialog.show();
        BarberSelectionArrayAdapter customAdapter = new BarberSelectionArrayAdapter(mContext, remainingBarbers);
        ddSpinner.setAdapter(customAdapter);

        barberSelectionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                ViewUtils.getDisplayHeight(getActivity().getWindowManager()) / 4, getResources().getDisplayMetrics());
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                ViewUtils.getDisplayWidth(getActivity().getWindowManager()) / 2, getResources().getDisplayMetrics());
        barberSelectionDialog.getWindow().setLayout(width, height);

        final Button yesButton = (Button) barberSelectionDialog.findViewById(R.id.yes_add_barber_queue);
        final Button noButton = (Button) barberSelectionDialog.findViewById(R.id.no_add_barber_queue);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onBarberSelectionClick(selectedBarberKeyFromBarberDialog);

            }
        });
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideBarberSelectDialog();
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
    public String getTakeBreakButtonText() {
        return takeBreakButton.getText().toString();
    }

    @Override
    public void addBarberQueueTab(Barber barber) {
        if (isTabExists(barber.getKey())) {
            LogUtils.error("Tab already exists for: {0}, so no new tab will be added", barber.getKey());
            return;
        }
        final TabLayout.Tab tab = tabLayout.newTab();
        View customView = LayoutInflater.from(mContext).inflate(R.layout.tab_customer_layout, null);
        tab.setCustomView(customView);
        tab.setTag(barber.getKey());
        tabLayout.addTab(tab.setText("Loading...").setIcon(R.drawable.photo_barber));

        final ViewPager viewPager = root.findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter(getActivity().getSupportFragmentManager(), tabLayout.getTabCount(), tabLayout);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        viewPager.setCurrentItem(adapter.getCount() - 1);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(final TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                ((TextView) tab.getCustomView().findViewById(R.id.tab_name)).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                presenter.onBarberQueueTabSelected(tab.getTag().toString());
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
        dialog.dismiss();
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
