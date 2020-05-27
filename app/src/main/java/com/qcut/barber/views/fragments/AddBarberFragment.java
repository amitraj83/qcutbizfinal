package com.qcut.barber.views.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.qcut.barber.R;
import com.qcut.barber.models.Barber;
import com.qcut.barber.presenters.fragments.AddBarberPresenter;
import com.qcut.barber.views.AddBarberView;

import java.io.IOException;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class AddBarberFragment extends Fragment implements AddBarberView {

    private final int PICK_IMAGE_REQUEST = 71;
    private Uri filePath;
    private ImageView barberUploadImageView;
    private Button barberUploadButton;
    private EditText newBarberName;
    private ListView listBarbers;
    private AddBarberPresenter presenter;
    private ProgressDialog progressDialog;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add_barber, container, false);
        if (presenter == null) {
            presenter = new AddBarberPresenter(this, getContext());
            barberUploadImageView = root.findViewById(R.id.barber_photo);
            barberUploadButton = root.findViewById(R.id.UploadBtn);
            newBarberName = root.findViewById(R.id.new_barber_name);
            listBarbers = root.findViewById(R.id.list_barbers);

            barberUploadImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    presenter.onUploadImageClick();
                }
            });

            barberUploadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    presenter.onUploadButtonClick(filePath);
                }
            });
        }
        presenter.populateBarbers();
        return root;
    }

    @Override
    public void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public String getEnteredBarberName() {
        return newBarberName.getText().toString();
    }

    @Override
    public void showMessage(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showBarbersList(List<Barber> barberList) {
        CustomAdapter customAdapter = new CustomAdapter(barberList);
        listBarbers.setAdapter(customAdapter);
    }

    @Override
    public void showProgressDialog(String title) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
        }
        progressDialog.setTitle(title);
        progressDialog.show();
    }

    @Override
    public void setProgressDialogMsg(String msg) {
        progressDialog.setMessage(msg);
    }

    @Override
    public void hideProgressDialog() {
        progressDialog.dismiss();
    }

    @Override
    public void resetFileUploadBox() {
        newBarberName.getText().clear();
        barberUploadImageView.setImageResource(R.drawable.ic_add_barber_photo_black_24dp);
    }

    @Override
    public void setPhotoUrl(ImageView photo, Uri result) {
        Glide.with(getContext()).load(result).into(photo);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), filePath);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false);
                barberUploadImageView.setImageBitmap(scaledBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    class CustomAdapter extends BaseAdapter {

        private List<Barber> barbers;

        public CustomAdapter(List<Barber> barbers) {
            this.barbers = barbers;
        }

        @Override
        public int getCount() {
            return barbers.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            @SuppressLint("ViewHolder")
            View listView_layout = getLayoutInflater().inflate(R.layout.shop_barber, null);
            final ImageView photo = listView_layout.findViewById(R.id.barber_photo_lv);
            TextView name = listView_layout.findViewById(R.id.barber_name_lv);
            name.setText(barbers.get(i).getName());
            presenter.getDownloadUrlAndSetInView(photo, barbers.get(i).getImagePath());
            return listView_layout;
        }
    }
}