package com.peammobility.auth;

import static android.widget.Toast.LENGTH_LONG;
import static com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY;
import static com.peammobility.MainActivity.FINE_PERMISSION_CODE;
import static com.peammobility.MainActivity.TAG;
import static com.peammobility.classes.Env.APP_VERSION;
import static com.peammobility.classes.Env.AUTH_TOKEN_URL;
import static com.peammobility.classes.Env.ENVIRONMENT;
import static com.peammobility.classes.Env.PLAYSTORE_APP_URL;
import static com.peammobility.classes.Env.RETRIES;
import static com.peammobility.classes.Env.TERMS_URL;
import static com.peammobility.classes.Env.VOLLEY_TIME_OUT;
import static com.peammobility.classes.Env.getURL;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.peammobility.MainActivity;
import com.peammobility.R;
import com.peammobility.resources.CustomResources;
import com.peammobility.resources.LoadingDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class LoginActivity extends AppCompatActivity {
    Button loginBTN;
    TextView terms, register;
    CustomResources customResources = new CustomResources(this);
    LoadingDialog loadingDialog = new LoadingDialog(this);
    TextInputEditText phoneNumber;
    RequestQueue requestQueue;
    AwesomeValidation awesomeValidation;
    AlertDialog.Builder dialogBuilder;
    AlertDialog dialog;
    String message, name;
    SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        customResources.setStatusBar();
        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);

        name = sharedPreferences.getString("name", "null");

        loginBTN = findViewById(R.id.loginBTN);
        phoneNumber = findViewById(R.id.login_phone);
        terms = findViewById(R.id.l_terms);
        register = findViewById(R.id.l_register_btn);

        terms.setText(Html.fromHtml(String.format(getString(R.string.terms_and_conditions_statement))));

        locationPermissionRequest();

        onClick();
    }

    private void onClick() {
        loginBTN.setOnClickListener(v -> {
            if (validateInputs()) {
                loadingDialog.startLoadingDialog();
                loginUsingFirebase();
            }
        });

        register.setOnClickListener(v -> {
            loadingDialog.startLoadingDialog();
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    /**
     * Validate form inputs
     */
    private boolean validateInputs() {
        //start validation
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        //validate Phone Number
        awesomeValidation.addValidation(this, R.id.login_phone, "[0-9]{9}$", R.string.invalid_phone_number);
        return awesomeValidation.validate();
    }

    /**
     *
     */
    public void loginUsingFirebase() {
        StringRequest request = new StringRequest(Request.Method.POST, getURL(AUTH_TOKEN_URL), response -> {

            JSONObject jsonObject = null;
            Double remote_version = null;
            Double local_version = null;
            String authToken = null;
            boolean success = false;

            try {
                jsonObject = new JSONObject(response);
                success = jsonObject.getBoolean("success");

                if (success) {
                    //login user
                    authToken = jsonObject.getString("auth_token");
                    remote_version = Double.parseDouble(jsonObject.getString("app_version"));
                    local_version = Double.parseDouble(APP_VERSION);

                    //** save user data as in login activity
                    customResources.saveUserDetails(response, LoginActivity.this);

                    loginUser(authToken);
                } else {
                    String type = customResources.extractData(response, "type");
                    String code = customResources.extractData(response, "code");

                    //app update required
                    if (type.equals("app_update")) {
                        appVersioning();
                    } else {
                        message = customResources.extractMessage(response);

                        if (code.equals("1000")) {
                            Intent intent = new Intent(this, RegisterActivity.class);
                            intent.putExtra("phone_number", phoneNumber.getText().toString());
                            startActivity(intent);
                        } else {
                            new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setContentText(message)
                                    .setConfirmText("Register")
                                    .setConfirmClickListener(sDialog -> {
                                        loadingDialog.startLoadingDialog();
                                        sDialog.dismissWithAnimation();
                                        startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                                    })
                                    .show();
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {

        }) {
            @NonNull
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> param = new HashMap<>();
                param.put("phone_number", phoneNumber.getText().toString());
                param.put("user_type", "customer");
                param.put("app_version", APP_VERSION);
                return param;
            }
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> headerMap = new HashMap<String, String>();
//                headerMap.put("Content-Type", "application/json");
//                String encodedCredentials = Base64.encodeToString(APP_BEARER_KEY.getBytes(), Base64.DEFAULT);
//                Log.e("TAG",APP_BEARER_KEY);
//                headerMap.put("Authorization", "Bearer " + APP_BEARER_KEY);
//                return headerMap;
//            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(VOLLEY_TIME_OUT, RETRIES, 1.0f));
        requestQueue = Volley.newRequestQueue(LoginActivity.this);
        requestQueue.add(request);
    }


    /**
     * Confirm pin and complete transaction
     */
    private void appVersioning() {
        dialogBuilder = new AlertDialog.Builder(this);
        final View pinView = getLayoutInflater().inflate(R.layout.confirmation_layout, null);
        dialogBuilder.setView(pinView);
        dialogBuilder.setCancelable(false);
        dialog = dialogBuilder.create();
        dialog.show();
        message = "Dear " + name + ", you're using an outdated version of the app to access empower services. " +
                "Kindly install the latest app to access more features. Thank you";

        //
        Button confirmBTN, updateBTN;
        TextView messageText, titleText;
        messageText = pinView.findViewById(R.id.bc_description);
        messageText.setText(message);

        titleText = pinView.findViewById(R.id.bc_titleText);
        titleText.setText(R.string.update_required);

        updateBTN = pinView.findViewById(R.id.bc_personal_account);
        updateBTN.setText(R.string.update_app);
        updateBTN.setOnClickListener(v -> {
            dialog.dismiss();
            loadingDialog.startLoadingDialog();
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse(PLAYSTORE_APP_URL)));
            finish();
        });

        confirmBTN = pinView.findViewById(R.id.bc_confirm_btn);
        confirmBTN.setVisibility(View.GONE);
    }


    /**
     * Call firebase to login user
     *
     * @param authToken
     */
    public void loginUser(String authToken) {
        mAuth.signInWithCustomToken(authToken)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("auth_reset", "no");
                        editor.putBoolean("authenticated", true);
                        editor.apply();

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Log.e("AuthError", "signInWithCustomToken:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                    Log.e("AuthComplete", "signInWithCustomToken:success");
                })
                .addOnFailureListener(this, e -> Log.e("AuthFailed", "Auth Failed Miserably || ENV == " + ENVIRONMENT));
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

                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                // Only approximate location access granted.
                                forceRequestLocation();
                            } else {
                            }
                        }
                );

        //request permission
        if (ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //disclosure
            locationDisclosure(locationPermissionRequest);
        } else {
            forceRequestLocation();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                forceRequestLocation();
            }
            locationPermissionRequest();
            Toast.makeText(this, "Location permission is denied.", LENGTH_LONG).show();
        }
    }


    /**
     * Confirm pin and complete transaction
     */
    private void locationDisclosure(ActivityResultLauncher<String[]> locationPermissionRequest) {
        dialogBuilder = new AlertDialog.Builder(this);
        final View pinView = getLayoutInflater().inflate(R.layout.location_disclosure_layout, null);
        dialogBuilder.setView(pinView);
        dialogBuilder.setCancelable(false);
        dialog = dialogBuilder.create();
        dialog.show();
        message = "To ensure that we offer better services, we will take your location to calculate pricing to your destination and direct drivers to your pickup point. " +
                "Kindly click <b>Grant</b> to proceed.<br><br>We will strictly follow the Service Agreement and <a href='" + TERMS_URL + "' style='color:blue'> Privacy Policy</a>" +
                " to provide service and protect your privacy.";
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
            locationPermissionRequest.launch(new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            });
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