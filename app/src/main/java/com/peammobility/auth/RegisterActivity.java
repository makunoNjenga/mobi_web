package com.peammobility.auth;

import static com.peammobility.classes.Env.REGISTER_URL;
import static com.peammobility.classes.Env.RETRIES;
import static com.peammobility.classes.Env.VOLLEY_TIME_OUT;
import static com.peammobility.classes.Env.getURL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.ImageView;
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
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.android.material.textfield.TextInputEditText;
import com.peammobility.MainActivity;
import com.peammobility.R;
import com.peammobility.resources.CustomResources;
import com.peammobility.resources.LoadingDialog;

import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class RegisterActivity extends AppCompatActivity {
    TextView terms;
    ImageView back;
    Button registerBTN;
    CustomResources customResources = new CustomResources(this);
    LoadingDialog loadingDialog = new LoadingDialog(this);
    AwesomeValidation awesomeValidation;
    TextInputEditText nameText, phoneText, emailText;
    SharedPreferences sharedPreferences;
    CustomResources resources;
    String message;
    RequestQueue requestQueue;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        customResources.setStatusBar();

        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);

        terms = findViewById(R.id.r_terms);
        back = findViewById(R.id.r_back);
        registerBTN = findViewById(R.id.registerBTN);

        terms.setText(Html.fromHtml(String.format(getString(R.string.terms_and_conditions_statement))));

        nameText = findViewById(R.id.r_name);
        phoneText = findViewById(R.id.r_phone);
        emailText = findViewById(R.id.r_email);

        resources = new CustomResources();

        onClick();
    }

    private void onClick() {
        back.setOnClickListener(v -> {
            loadingDialog.startLoadingDialog();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        registerBTN.setOnClickListener(v -> {
            if (validateInputs()) {
                registerNewAccount();
            }
        });
    }


    /**
     * Process registration sequence
     */
    public void registerNewAccount() {
        StringRequest request = new StringRequest(Request.Method.POST, getURL(REGISTER_URL), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.e("TAG", "Response Received => " + response.toString());

                Toast.makeText(RegisterActivity.this, resources.getResponseData(response, "data"), Toast.LENGTH_SHORT).show();

                if (resources.extractStatus(response)) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    editor.putString("authState", "authenticated");
                    editor.apply();

                    //** save user data as in login activity
                    resources.saveUserDetails(response, RegisterActivity.this);

                    message = "Registration has been completed successfully. Continue to login to get to your destination";
                    SweetAlertDialog alert = new SweetAlertDialog(RegisterActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                            .setContentText(message)
                            .setConfirmText("Registration Succeeded")
                            .setConfirmClickListener(sDialog -> {
                                loadingDialog.startLoadingDialog();
                                sDialog.dismissWithAnimation();

                                //redirect to login
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            });

                    alert.setCancelable(false);
                    alert.show();

                    finish();
                } else {
                    String message = resources.getResponseData(response, "data");

                    new SweetAlertDialog(RegisterActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setContentText(message)
                            .setConfirmText("Login")
                            .setConfirmClickListener(sDialog -> {
                                loadingDialog.startLoadingDialog();
                                sDialog.dismissWithAnimation();
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            })
                            .show();

                    Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(RegisterActivity.this, error.toString(), Toast.LENGTH_SHORT).show();

            }
        }) {
            @NonNull
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> param = new HashMap<>();
                param.put("name", nameText.getText().toString());
                param.put("email", emailText.getText().toString());
                param.put("phone_number", phoneText.getText().toString());
                return param;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(VOLLEY_TIME_OUT, RETRIES, 1.0f));
        requestQueue = Volley.newRequestQueue(RegisterActivity.this);
        requestQueue.add(request);
    }

    /**
     * Validate form inputs
     *
     * @return
     */
    private boolean validateInputs() {
        //start validation
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);

        //validate name
        awesomeValidation.addValidation(this, R.id.r_name, RegexTemplate.NOT_EMPTY, R.string.invalid_field);

        //validate phone number
        awesomeValidation.addValidation(this, R.id.r_phone, RegexTemplate.NOT_EMPTY, R.string.invalid_field);

        //validate email
        awesomeValidation.addValidation(this, R.id.r_email, RegexTemplate.NOT_EMPTY, R.string.invalid_field);

        return awesomeValidation.validate();
    }
}