package com.qcut.biz.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.qcut.biz.R;
import com.qcut.biz.presenters.SignInPresenter;
import com.qcut.biz.views.SignInView;

public class SignInActivity extends AppCompatActivity implements SignInView {

    private Button signIn;
    private TextView emailTextBox;
    private TextView passwordTextbox;
    private SignInPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferences sp = getSharedPreferences("login", MODE_PRIVATE);
        presenter = new SignInPresenter(this, sp, this);
        emailTextBox = findViewById(R.id.sign_in_email);
        passwordTextbox = findViewById(R.id.sign_in_password);

        passwordTextbox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    presenter.onSignInClick();
                    return true;
                }
                return false;
            }
        });

        signIn = findViewById(R.id.sign_in_screen);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onSignInClick();
            }
        });

        LayoutInflater factory = LayoutInflater.from(SignInActivity.this);
        final View forgotPasswordView = factory.inflate(R.layout.forgot_password, null);
        final AlertDialog forgotPasswordDialog = new AlertDialog.Builder(SignInActivity.this).create();
        forgotPasswordDialog.setView(forgotPasswordView);

        TextView forgotPasswordBtn = findViewById(R.id.forgot_btn);
        forgotPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPasswordDialog.show();
                forgotPasswordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                forgotPasswordDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 750);
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
    public void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public String getEmail() {
        return emailTextBox.getText().toString();
    }

    @Override
    public String getPassword() {
        return passwordTextbox.getText().toString();
    }

    @Override
    public void startActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
