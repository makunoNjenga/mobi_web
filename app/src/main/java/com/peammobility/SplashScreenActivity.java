package com.peammobility;

import static android.widget.Toast.LENGTH_LONG;
import static com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY;
import static com.peammobility.MainActivity.FINE_PERMISSION_CODE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.peammobility.auth.LoginActivity;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {
    boolean authenticated;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launch_screen);
        locationPermissionRequest();

        //get location
        forceRequestLocation();

        // Hide status bar
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
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


    @SuppressLint("MissingPermission")
    private void forceRequestLocation() {
        LocationRequest locationRequest = new LocationRequest.Builder(PRIORITY_HIGH_ACCURACY, 100)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(2000)
                .setMaxUpdateDelayMillis(100)
                .build();

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
            }
        };

        LocationServices.getFusedLocationProviderClient(getApplicationContext())
                .requestLocationUpdates(locationRequest, locationCallback, null);
    }


    /**
     * location permission
     */
    private void locationPermissionRequest() {
        @SuppressLint("MissingPermission") ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                fineLocationGranted = result.getOrDefault(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION, false);
                            }
                            Boolean coarseLocationGranted = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                coarseLocationGranted = result.getOrDefault(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION, false);
                            }
                            if (fineLocationGranted != null && fineLocationGranted) {
                                // Precise location access granted.
                                forceRequestLocation();
                                startCountdown();

                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                // Only approximate location access granted.
                                forceRequestLocation();
                                startCountdown();

                            } else {
                                // No location access granted.
                            }
                        }
                );

        locationPermissionRequest.launch(new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                forceRequestLocation();
                startCountdown();
            }
            locationPermissionRequest();
            Toast.makeText(this, "Location permission is denied.", LENGTH_LONG).show();
        }
    }

}