package com.peammobility;

import static android.widget.Toast.LENGTH_LONG;
import static com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY;
import static com.peammobility.MainActivity.FINE_PERMISSION_CODE;
import static com.peammobility.classes.Env.TERMS_URL;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.peammobility.auth.LoginActivity;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {
    boolean authenticated;
    SharedPreferences sharedPreferences;
    AlertDialog.Builder dialogBuilder;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launch_screen);

        // Hide status bar
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);

        if (ActivityCompat.checkSelfPermission(SplashScreenActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(SplashScreenActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationDisclosure();
        } else {
            startCountdown();
        }

    }

    public void startCountdown() {
        new CountDownTimer((3 * 1000), 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                authenticated = sharedPreferences.getBoolean("authenticated", false);

                if (authenticated) {
                    startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                } else {
                    startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                }
                finish();
            }
        }.start();
    }

    /**
     * Confirm pin and complete transaction
     */
    private void locationDisclosure() {
        dialogBuilder = new AlertDialog.Builder(this);
        final View pinView = getLayoutInflater().inflate(R.layout.location_disclosure_layout, null);
        dialogBuilder.setView(pinView);
        dialogBuilder.setCancelable(false);
        dialog = dialogBuilder.create();
        dialog.show();
        String message = "To ensure better service delivery, we use your location to calculate pricing to your destination and guide drivers to your pickup point. Your location is collected in the background solely for accurate pickup coordination by our drivers. This app requires location data for precise billing, directing drivers, and pinpointing your trip destination, even when the app is not in use. <br><br>" +
                "Please tap <b>Grant</b> to proceed. Rest assured, we strictly adhere to our Service Agreement and <a href='" + TERMS_URL + "' style='color:#0000FF'>Privacy Policy</a> to provide services and safeguard your privacy.";
        String title = "Location Permission";

        //
        Button confirmBTN, cancelBTN;
        TextView messageText, titleText;
        messageText = pinView.findViewById(R.id.bc_description);
        messageText.setText(Html.fromHtml(String.format(message)));
        titleText = pinView.findViewById(R.id.ld_titleText);
        titleText.setText(title);

        cancelBTN = pinView.findViewById(R.id.bc_personal_account);
        cancelBTN.setText("Exit");
        cancelBTN.setOnClickListener(v -> {
            finish();
        });

        confirmBTN = pinView.findViewById(R.id.bc_confirm_btn);
        confirmBTN.setText("Grant");
        confirmBTN.setOnClickListener(v -> {
            dialog.dismiss();
            authenticated = sharedPreferences.getBoolean("authenticated", false);

            if (authenticated) {
                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
            }
        });
    }

}