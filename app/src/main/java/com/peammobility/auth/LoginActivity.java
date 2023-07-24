package com.peammobility.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.TextView;

import com.peammobility.R;
import com.peammobility.resources.CustomResources;
import com.peammobility.resources.LoadingDialog;

public class LoginActivity extends AppCompatActivity {
Button loginBTN;
TextView terms;
CustomResources customResources = new CustomResources(this);
LoadingDialog loadingDialog = new LoadingDialog(this);
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        customResources.setStatusBar();

        loginBTN = findViewById(R.id.loginBTN);

        terms = findViewById(R.id.l_terms);

        terms.setText(Html.fromHtml(String.format(getString(R.string.terms_and_conditions_statement))));

        onClick();
    }

    private void onClick() {
        loginBTN.setOnClickListener(v->{
            loadingDialog.startLoadingDialog();
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        try {
            loadingDialog.dismissDialog();
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            loadingDialog.dismissDialog();
        } catch (Exception ignored) {
        }
    }

}