package com.peammobility.trips;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.peammobility.R;
import com.peammobility.resources.LoadingDialog;

public class TripViewActivity extends AppCompatActivity {
    LoadingDialog loadingDialog = new LoadingDialog(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_view);

        onClick();
    }

    private void onClick() {
        findViewById(R.id.tv_back_icon).setOnClickListener(v -> {
            loadingDialog.startLoadingDialog();
            startActivity(new Intent(this, MyTripsActivity.class));
            finish();
        });
    }
}