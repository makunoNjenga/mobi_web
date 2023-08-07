package com.peammobility;

import static com.peammobility.classes.Env.RETRIES;
import static com.peammobility.classes.Env.UPDATE_APP_TOKEN_URL;
import static com.peammobility.classes.Env.VOLLEY_TIME_OUT;
import static com.peammobility.classes.Env.getURL;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.peammobility.auth.LoginActivity;
import com.peammobility.auth.ProfileActivity;
import com.peammobility.maps.DirectionsJSONParser;
import com.peammobility.maps.DirectionsParser;
import com.peammobility.maps.TripRoute;
import com.peammobility.resources.CustomResources;
import com.peammobility.resources.LoadingDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {
    private static final int FINE_PERMISSION_CODE = 1;
    private static final String PICK_UP = "Pick Up";
    private static final String DESTINATION = "Destination";
    private static final String PEAM_4 = "Peam 4";
    private static final String PEAM_2 = "Peam 2";
    private static final String GOOGLE_MAPS_URL = "https://maps.googleapis.com/maps/api/directions/";
    private static final String GOOGLE_API_KEY = "AIzaSyABLWkA85cwC3Jsm8KGZxa_FzGXtDeqeHs";
    private static final int COST_PER_KM_PEAM_2_SHORT = 50;
    private static final int COST_PER_KM_PEAM_2_LONG = 45;
    private static final int COST_PER_KM_PEAM_4_SHORT = 55;
    private static final int COST_PER_KM_PEAM_4_LONG = 45;
    private static final Double MIN_COST = 150.0;
    private static final Double LONG_DISTANCE_MIN = 51.0;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    LoadingDialog loadingDialog = new LoadingDialog(this);
    SharedPreferences sharedPreferences;
    SupportMapFragment mapFragment;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    String token, phoneNumber, userID;
    RequestQueue requestQueue;
    private GoogleMap mMap;
    ArrayList<LatLng> tripPoints;
    TripRoute tripRoute;
    boolean fetched = true;
    LinearLayout defaultLayout, tripDetailsLayout;
    GridLayout offerLayout;
    TextView tripDistanceText, tripDurationText, peam2Text, peam4Text;
    int tripTotalCostPeam4 = 0;
    int tripTotalCostPeam2 = 0;
    CustomResources customResources = new CustomResources();
    Button clearBTN;

    @SuppressLint({"MissingInflatedId", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        defaultLayout = findViewById(R.id.m_main_layout);
        tripDetailsLayout = findViewById(R.id.m_trip_details);

        tripDistanceText = findViewById(R.id.m_trip_distance);
        tripDurationText = findViewById(R.id.m_trip_time);
        peam2Text = findViewById(R.id.m_peam_2);
        peam4Text = findViewById(R.id.m_peam_4);
        clearBTN = findViewById(R.id.m_clear_button);
        offerLayout = findViewById(R.id.m_offer_layout);

        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        phoneNumber = sharedPreferences.getString("phone_number", "null");
        userID = sharedPreferences.getString("userIO", "null");
        tripPoints = new ArrayList<>();

        //permission request
        locationPermissionRequest();

        //map context
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        //set toolbar
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_open_drawer, R.string.navigation_close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //edit toolbar
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.menu_light);

        //actionate menu items
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);

        //click events
        onClick();
    }

    /**
     * click events
     */
    private void onClick() {
        //clear all and move to current location
        clearBTN.setOnClickListener(v -> {
            tripPoints.clear();
            mMap.clear();
            moveToCurrentLocation();
        });
    }


    /**
     *
     */
    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest();
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            currentLocation = location;

            mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
            assert mapFragment != null;
            mapFragment.getMapAsync(MainActivity.this);

            //move to current location
//            moveToCurrentLocation();
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressLint("ShowToast")
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_book_a_trip) {
            Toast.makeText(this, "Book a trip selected", Toast.LENGTH_LONG).show();
        } else if (item.getItemId() == R.id.nav_my_trips) {
            Toast.makeText(this, "My trips selected", Toast.LENGTH_LONG).show();
        } else if (item.getItemId() == R.id.nav_offers) {
            Toast.makeText(this, "Offers selected", Toast.LENGTH_LONG).show();
        } else if (item.getItemId() == R.id.nav_help) {
            Toast.makeText(this, "Help selected", Toast.LENGTH_LONG).show();
        } else if (item.getItemId() == R.id.nav_about) {
            Toast.makeText(this, "About selected", Toast.LENGTH_LONG).show();
        } else if (item.getItemId() == R.id.nav_profile) {
            loadingDialog.startLoadingDialog();
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (item.getItemId() == R.id.nav_logout) {
            logout();
        }
        return true;
    }

    /**
     * logout functions
     */
    private void logout() {
        loadingDialog.startLoadingDialog();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("authenticated", false);
        editor.apply();

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest();
            return;
        }
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (currentLocation != null) {
            moveToCurrentLocation();
        }

        mMap.setOnMapLongClickListener(latLng -> {
            MarkerOptions markerOptions = new MarkerOptions();
            showLayouts("default");

            //reset markers
            if (tripPoints.size() == 2) {
                tripPoints.clear();
                mMap.clear();
            }

            if (tripPoints.size() == 0) {
                tripPoints.add(latLng);
                markerOptions.position(latLng);

                //add first marker
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                markerOptions.title(PICK_UP);
            } else {
                //add the points
                tripPoints.add(latLng);
                markerOptions.position(latLng);

                // add second marker
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                markerOptions.title(DESTINATION);
            }

            mMap.addMarker(markerOptions);
            //TODO Request direction

            if (tripPoints.size() == 2) {
                fetched = false;
//                    create url to get request from pick up to destination
                String url = getRequestURL(tripPoints.get(0), tripPoints.get(1));
                TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                taskRequestDirections.execute(url);
            }
        });
    }

    /**
     * move focus on current location
     */
    private void moveToCurrentLocation() {
        showLayouts("default");
        LatLng myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        markerOptions.title(PICK_UP);
        markerOptions.position(myLocation);

        tripPoints.clear();
        mMap.clear();

        tripPoints.add(myLocation);
        mMap.addMarker(markerOptions);
    }

    private String getRequestURL(LatLng pickup, LatLng destination) {
        String str_origin = "origin=" + pickup.latitude + "," + pickup.longitude;
        String str_destination = "destination=" + destination.latitude + "," + destination.longitude;
//        enable sensor
        String sensor = "sensor=false";
        //mode to find direction
        String mode = "mode=driving";
        String key = "key=" + GOOGLE_API_KEY;
        String params = str_origin + "&" + str_destination + "&" + sensor + "&" + mode + "&" + key;
        //output format
        String output = "json";
        return GOOGLE_MAPS_URL + output + "?" + params;
    }

    private String requestDirection(String requestUrl) throws IOException {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(requestUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            //get response
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            assert httpURLConnection != null;
            httpURLConnection.disconnect();
        }
        return responseString;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
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
                                getLastLocation();
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                // Only approximate location access granted.
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
            locationPermissionRequest();
            Toast.makeText(this, "Location permission is denied.", Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Token is generated from the background
     */
    private void checkAppTokenThread() {
        Runnable tokenRunnable = this::getToken;
        Thread tokenThread = new Thread(tokenRunnable);
        tokenThread.start();
    }


    //generate app token
    public void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("TAG", "onComplete: Failed to get the token. ");
                Log.e("TAG", Objects.requireNonNull(task.getException()).getMessage());
            } else {
                token = task.getResult();
                //update to server
                updateTokenToWebServer();
            }
        });
    }

    /**
     * update token to server
     */
    private void updateTokenToWebServer() {
        StringRequest request = new StringRequest(Request.Method.POST, getURL(UPDATE_APP_TOKEN_URL), response -> {
            //add to shared resources
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("app_token", token);
            editor.apply();
        }, error -> {
        }) {
            @NonNull
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> param = new HashMap<>();
                param.put("phone_number", phoneNumber);
                param.put("client_id", userID);
                param.put("token", token);
                return param;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(VOLLEY_TIME_OUT, RETRIES, 1.0f));
        requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(request);
    }

    /**
     *
     */
    public class TaskRequestDirections extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";
            try {
                responseString = requestDirection(strings[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (!fetched) {
                //parse our json here
                TaskParser taskParser = new TaskParser();
                taskParser.execute(s);
            }
        }
    }

    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                routes = directionsParser.parse(jsonObject);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            //get route lists and display to the map
            ArrayList points = null;
            PolylineOptions polylineOptions = null;
            if (!fetched) {
                for (List<HashMap<String, String>> path : lists) {
                    points = new ArrayList();
                    polylineOptions = new PolylineOptions();

                    for (HashMap<String, String> point : path) {
                        Double lat = Double.parseDouble(Objects.requireNonNull(point.get("lat")));
                        Double lon = Double.parseDouble(Objects.requireNonNull(point.get("lon")));
                        points.add(new LatLng(lat, lon));

                        //get distances
                        String distance = point.get("distance");
                        String duration = point.get("duration");
                        String actualDistance = point.get("actual_distance");
                        if (!fetched) {
                            showTripDetails(distance, duration, actualDistance);
                        }

                        fetched = true;
                    }

                    polylineOptions.addAll(points);
                    polylineOptions.width(15);
                    polylineOptions.color(Color.BLUE);
                    polylineOptions.geodesic(true);
                }

                if (polylineOptions != null) {
                    mMap.addPolyline(polylineOptions);
                } else {
                    Toast.makeText(getApplicationContext(), "Direction not found", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * Trip details
     *
     * @param distance
     * @param duration
     * @param actualDistance
     */
    private void showTripDetails(String distance, String duration, String actualDistance) {
        showLayouts("trip_details");
        tripTotalCostPeam4 = getTripTotalCost(Double.parseDouble(actualDistance), PEAM_4);
        tripTotalCostPeam2 = getTripTotalCost(Double.parseDouble(actualDistance), PEAM_2);

        //set values
        tripDistanceText.setText(distance);
        tripDurationText.setText(duration);

        String totalPeam4Text = "Ksh " + customResources.numberFormat(tripTotalCostPeam4);
        String totalPeam2Text = "Ksh " + customResources.numberFormat(tripTotalCostPeam2);

        peam2Text.setText(totalPeam2Text);
        peam4Text.setText(totalPeam4Text);
    }

    /**
     * /**
     * show layouts
     *
     * @param layout
     */
    public void showLayouts(String layout) {
        if (layout.equals("default")) {
            defaultLayout.setVisibility(View.VISIBLE);
            offerLayout.setVisibility(View.VISIBLE);
            tripDetailsLayout.setVisibility(View.GONE);
        }
        if (layout.equals("trip_details")) {
            defaultLayout.setVisibility(View.GONE);
            offerLayout.setVisibility(View.GONE);
            tripDetailsLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * calculate trip details
     *
     * @param actualDistance
     */
    private int getTripTotalCost(Double actualDistance, String type) {
        double costPerkM = 0;
        boolean shortDistance = (actualDistance / 1000) < LONG_DISTANCE_MIN;
        if (shortDistance) {
            switch (type) {
                case PEAM_4:
                    costPerkM = COST_PER_KM_PEAM_4_SHORT;
                    break;
                case PEAM_2:
                    costPerkM = COST_PER_KM_PEAM_2_SHORT;
                    break;
            }
        } else {
            switch (type) {
                case PEAM_4:
                    costPerkM = COST_PER_KM_PEAM_4_LONG;
                    break;
                case PEAM_2:
                    costPerkM = COST_PER_KM_PEAM_2_LONG;
                    break;
            }
        }

        Double cost = (actualDistance / 1000 * costPerkM);

        //check minimum
        if (cost < MIN_COST) {
            cost = MIN_COST;
        }
        return cost.intValue();
    }

}