package com.qcut.biz.views;

import android.net.Uri;
import android.widget.ImageView;

import com.qcut.biz.models.Barber;
import com.qcut.biz.models.BarberStatus;

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

    void showBarberStatusConfirmationDialog(String dialogTitle, String dialogText, String confirmText, BarberStatus newStatus, String imagePath);

    boolean isTabExists(String key);

    void addBarberQueueTab(Barber barber);

    void showBarberSelectionDialog(List<Barber> remainingBarbers);

    void hideBarberSelectDialog();

    String getStopButtonText();

    String getTakeBreakButtonText();

    String getSelectedBarberKey();
}
