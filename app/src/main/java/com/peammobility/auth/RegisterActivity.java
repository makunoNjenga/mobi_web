package com.peammobility.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.peammobility.MainActivity;
import com.peammobility.R;
import com.peammobility.resources.CustomResources;
import com.peammobility.resources.LoadingDialog;

public class RegisterActivity extends AppCompatActivity {
    TextView terms;
    ImageView back;
    Button registerBTN;
    CustomResources customResources = new CustomResources(this);
    LoadingDialog loadingDialog = new LoadingDialog(this);

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        customResources.setStatusBar();

        terms = findViewById(R.id.r_terms);
        back = findViewById(R.id.r_back);
        registerBTN = findViewById(R.id.registerBTN);

        terms.setText(Html.fromHtml(String.format(getString(R.string.terms_and_conditions_statement))));

        onClick();
    }

    private void onClick() {
        back.setOnClickListener(v -> {
            loadingDialog.startLoadingDialog();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        registerBTN.setOnClickListener(v -> {
            loadingDialog.startLoadingDialog();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}