package com.qcut.biz.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.qcut.biz.R;
import com.qcut.biz.presenters.activities.SignUpPresenter;
import com.qcut.biz.views.activities.SignUpView;

public class SignUpActivity extends AppCompatActivity implements SignUpView {

    private Button signUp;
    private EditText shopContactName;
    private EditText shopNameET;
    private EditText shopEmailET;
    private EditText shopPassET;
    private SignUpPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        shopContactName = findViewById(R.id.user_name);
        shopNameET = findViewById(R.id.shop_name);
        shopEmailET = findViewById(R.id.sign_up_email);
        shopPassET = findViewById(R.id.sign_up_password);
        presenter = new SignUpPresenter(this, this);
        signUp = findViewById(R.id.sign_up_screen);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onSignUpClick();

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void startActivity(Class clazz) {
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
        finish();
    }

    @Override
    public String getShopContactName() {
        return shopContactName.getText().toString().trim();
    }

    @Override
    public String getShopName() {
        return shopNameET.getText().toString().trim();
    }

    @Override
    public void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public String getEmail() {
        return shopEmailET.getText().toString().trim();
    }

    @Override
    public String getPassword() {
        return shopPassET.getText().toString().trim();
    }
}
