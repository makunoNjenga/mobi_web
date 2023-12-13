package com.peammobility.trips;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.gson.Gson;
import com.peammobility.R;
import com.peammobility.auth.LoginActivity;
import com.peammobility.auth.RegisterActivity;
import com.peammobility.classes.Trip;
import com.peammobility.resources.CustomResources;
import com.peammobility.resources.LoadingDialog;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class TripViewActivity extends AppCompatActivity {
    LoadingDialog loadingDialog = new LoadingDialog(this);
    TextView getReceiptBTN, tripDate, from, to, tripType, tripPayment, totalTripPayment, title;
    Trip trip;
    CustomResources resources = new CustomResources();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_view);
        getReceiptBTN = findViewById(R.id.tv_get_receipt);
        tripDate = findViewById(R.id.tv_date);
        from = findViewById(R.id.tv_from);
        to = findViewById(R.id.tv_to);
        tripType = findViewById(R.id.tv_trip_type);
        tripPayment = findViewById(R.id.tv_trip_payment);
        totalTripPayment = findViewById(R.id.tv_total_trip_payment);
        title = findViewById(R.id.tv_title);

        Gson gson = new Gson();
        trip = gson.fromJson(getIntent().getStringExtra("trip"), Trip.class);

        //updates
        tripDate.setText(trip.getDate());
        from.setText(trip.getOriginName());
        to.setText(trip.getDestinationName());
        tripType.setText(trip.getCabType());

        String price = "Ksh " + resources.numberFormat(trip.getPrice());
        tripPayment.setText(price);
        totalTripPayment.setText(price);

        String titleText = "Ride with " + trip.getCustomerName();
        title.setText(titleText);

        onClick();
    }

    private void onClick() {
        findViewById(R.id.tv_back_icon).setOnClickListener(v -> {
            loadingDialog.startLoadingDialog();
            startActivity(new Intent(this, MyTripsActivity.class));
            finish();
        });


        getReceiptBTN.setOnClickListener(v -> new SweetAlertDialog(TripViewActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                .setContentText("Trip receipt has been sent to your email.")
                .setConfirmText("Ok")
                .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation)
                .show());
    }
}