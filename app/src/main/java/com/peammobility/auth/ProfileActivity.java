package com.peammobility.auth;

import static com.peammobility.classes.Env.GET_PROFILE_IMAGES;
import static com.peammobility.classes.Env.UPDATE_IMAGES;
import static com.peammobility.classes.Env.getURL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DatabaseReference;
import com.peammobility.MainActivity;
import com.peammobility.R;
import com.peammobility.resources.CustomResources;
import com.peammobility.resources.LoadingDialog;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ProfileActivity extends AppCompatActivity {
    public static int PROFILE_REQUEST_CODE = 100;
    public static int ID_FRONT_REQUEST_CODE = 101;
    public static int ID_BACK_REQUEST_CODE = 102;
    ImageView idFrontImage, idBackImage;
    SharedPreferences sharedPreferences;
    TextView nameText, emailEditText, phoneNumber, joinedText;
    String name, phone_number, email, encodedImage, source, profileImage, joined;
    CustomResources cstResources;
    ShapeableImageView layoutProfileIcon;
    Bitmap captureProfileImage;
    CustomResources customResources = new CustomResources();
    RequestQueue requestQueue;
    LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        cstResources = new CustomResources();

        //changing the status bar background color
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.primary));

        //request permissions
        requestPermissions();

        layoutProfileIcon = findViewById(R.id.layout_profile_icon);

        //profile
        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);

        loadingDialog = new LoadingDialog(ProfileActivity.this);

        //add images
        threadManager();

        //user Profile
        name = sharedPreferences.getString("name", "N/A");
        phone_number = sharedPreferences.getString("phone_number", "N/A");
        email = sharedPreferences.getString("email", "N/A");
        joined = sharedPreferences.getString("createdAT", "N/A");

        nameText = findViewById(R.id.p_other_names);
        phoneNumber = findViewById(R.id.p_phone_number_text);
        emailEditText = findViewById(R.id.p_email_text);
        joinedText = findViewById(R.id.p_joined_at);

        //set respective texts
        nameText.setText(name);
        phoneNumber.setText(phone_number);
        emailEditText.setText(email);
        joinedText.setText(joined);

        // layout icons click listeners
        findViewById(R.id.layout_profile_icon).setOnClickListener(v -> openCamera(PROFILE_REQUEST_CODE));
        findViewById(R.id.layout_edit_icon).setOnClickListener(v -> openCamera(PROFILE_REQUEST_CODE));

        findViewById(R.id.layout_edit_icon).setOnClickListener(v -> openCamera(PROFILE_REQUEST_CODE));

        findViewById(R.id.ap_back_arrow).setOnClickListener(v -> {
            loadingDialog.startLoadingDialog();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    /**
     * Token is generated from the background
     */
    private void threadManager() {
        Runnable tokenRunnable = this::addImagesToView;
        Thread tokenThread = new Thread(tokenRunnable);
        tokenThread.start();
    }

    /**
     * sharedPreferences
     */
    private void addImagesToView() {
        StringRequest request = new StringRequest(Request.Method.POST, getURL(GET_PROFILE_IMAGES), response -> {
            if (customResources.extractStatus(response)) {

                profileImage = customResources.extractData(response, "profile");

                SharedPreferences.Editor editor = sharedPreferences.edit();

                //edit profile images
                editor.putString("profile_picture", profileImage);
                editor.apply();


                //load images
                Picasso.get().load(profileImage).into(layoutProfileIcon);


            } else {
                String message = customResources.extractMessage(response);

                new SweetAlertDialog(ProfileActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setContentText(message)
                        .show();
            }
        }, error -> new SweetAlertDialog(ProfileActivity.this, SweetAlertDialog.ERROR_TYPE)
                .setContentText(getResources().getString(R.string.sth_went_wrong))
                .show()) {
            @NonNull
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> param = new HashMap<>();
                param.put("phone_number", phone_number);
                return param;
            }
        };

        requestQueue = Volley.newRequestQueue(ProfileActivity.this);
        requestQueue.add(request);
    }

    /**
     * Open camera and take the picture
     */
    private void openCamera(int requestCode) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            source = "profile";
        }


        if ((requestCode == PROFILE_REQUEST_CODE || requestCode == ID_FRONT_REQUEST_CODE || requestCode == ID_BACK_REQUEST_CODE) && resultCode == RESULT_OK) {
            assert data != null;
            captureProfileImage = (Bitmap) data.getExtras().get("data");

            if (requestCode == 100) {
                layoutProfileIcon.setImageBitmap(captureProfileImage);
            }

            encodedImage = encodeBitmap(captureProfileImage);

            //update image to db
            uploadImages(source);
        }
    }

    /**
     * @param source
     */
    public void uploadImages(String source) {

        StringRequest request = new StringRequest(Request.Method.POST, getURL(UPDATE_IMAGES), response -> {
            if (customResources.extractStatus(response)) {

                String imageUrl = customResources.extractData(response);

                SharedPreferences.Editor editor = sharedPreferences.edit();

                switch (source) {
                    case "profile":
                        editor.putString("profile_picture", imageUrl);
                        break;
                    case "id_front":
                        editor.putString("id_front_picture", imageUrl);
                        break;
                    case "id_back":
                        editor.putString("id_back_picture", imageUrl);
                        break;
                }

                editor.apply();


            } else {
                String message = customResources.extractMessage(response);

                new SweetAlertDialog(ProfileActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setContentText(message)
                        .show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                new SweetAlertDialog(ProfileActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setContentText(getResources().getString(R.string.sth_went_wrong))
                        .show();

            }
        }) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> param = new HashMap<>();
                param.put("source", source);
                param.put("phone_number", phone_number);
                param.put("encoded_image", encodedImage);
                return param;
            }
        };

        requestQueue = Volley.newRequestQueue(ProfileActivity.this);
        requestQueue.add(request);
    }

    /**
     * @param bitmap
     */
    private String encodeBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        captureProfileImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] bytesOfImages = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytesOfImages, Base64.DEFAULT);
    }

    /**
     * Request all permissions for this page
     */
    private void requestPermissions() {
        //storage WRITE permissions
        if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 100);
        }
        //storage READ permissions
        if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 101);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ProfileActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}