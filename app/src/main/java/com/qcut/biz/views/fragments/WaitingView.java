package com.qcut.biz.views.fragments;

import android.net.Uri;
import android.widget.ImageView;

import com.qcut.biz.models.Barber;
import com.qcut.biz.util.Status;

import java.util.List;

public interface WaitingView {

    void showMessage(String msg);

    void setButtonToBarberOnBreak();

    void resetBarberBreakButton();

    void hideDialog();

    void updateButtonToStopped();

    void updateButtonToStopQ();

    String getSelectedTabId();

    void setPhotoUrl(ImageView photo, Uri result);

    void showDialog(String dialogTitle, String dialogText, String confirmText, Status newStatus, String imagePath);

    boolean isTabExists(String key);

    void addBarberQueueTab(Barber barber);

    void showBarberSelectionDialog(List<Barber> remainingBarbers);

    void hideBarberSelectDialog();

    String getStopButtonText();

    String getTakeBreakButtonText();

    String getSelectedBarberKey();
}
