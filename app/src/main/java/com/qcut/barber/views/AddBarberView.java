package com.qcut.barber.views;

import android.net.Uri;
import android.widget.ImageView;

import com.qcut.barber.models.Barber;

import java.util.List;

public interface AddBarberView {

    void chooseImage();

    String getEnteredBarberName();

    void showMessage(String msg);

    void showBarbersList(List<Barber> barberList);

    void showProgressDialog(String title);

    void setProgressDialogMsg(String msg);

    void hideProgressDialog();

    void resetFileUploadBox();

    void setPhotoUrl(ImageView photo, Uri result);
}
