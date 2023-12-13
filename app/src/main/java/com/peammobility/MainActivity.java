package com.peammobility;

import static android.widget.Toast.LENGTH_LONG;
import static com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY;
import static com.peammobility.classes.Env.CREATE_TRIP_URL;
import static com.peammobility.classes.Env.RETRIES;
import static com.peammobility.classes.Env.UPDATE_APP_TOKEN_URL;
import static com.peammobility.classes.Env.VOLLEY_TIME_OUT;
import static com.peammobility.classes.Env.getURL;
import static com.peammobility.firebase.FirebaseMessages.CHANNEL_ID;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import com.peammobility.auth.LoginActivity;
import com.peammobility.auth.ProfileActivity;
import com.peammobility.classes.LatLong;
import com.peammobility.classes.Trip;
import com.peammobility.maps.DirectionsParser;
import com.peammobility.maps.Place;
import com.peammobility.maps.SelectPlaceInterface;
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
import com.peammobility.trips.MyTripsActivity;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.json.JSONArray;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, SelectPlaceInterface {
    public static final int FINE_PERMISSION_CODE = 1;
    public static final String TAG = "PEAM DEBUG";
    private static final String PICK_UP = "Pick Up";
    private static final String DESTINATION = "Destination";
    private static final String PEAM_4 = "Peam 4";
    private static final String PEAM_2 = "Peam 2";
    public static final String GOOGLE_MAPS_URL = "https://maps.googleapis.com/maps/api/directions/";
    public static final String GOOGLE_API_KEY = "AIzaSyABLWkA85cwC3Jsm8KGZxa_FzGXtDeqeHs";
    private int COST_PER_KM_PEAM_2_SHORT = 50;
    public static float zoom = 15.0f;
    private int COST_PER_KM_PEAM_2_LONG = 45;
    private int COST_PER_KM_PEAM_4_SHORT = 55;
    private int COST_PER_KM_PEAM_4_LONG = 45;
    private Double MIN_COST = 150.0;
    private Double LONG_DISTANCE_MIN = 51.0;
    private static final String FIREBASE_REFERENCE_TRIPS = "trips";
    private static final String REQUEST = "Requested a Trip";
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    LoadingDialog loadingDialog = new LoadingDialog(this);
    SharedPreferences sharedPreferences;
    SupportMapFragment mapFragment;
    Location currentLocation;
    ArrayList<Trip> historyList = new ArrayList<>();
    FusedLocationProviderClient fusedLocationProviderClient;
    String activeTripID, token, phoneNumber, userID, first_name, appToken;
    RequestQueue requestQueue;
    private GoogleMap mMap;
    ArrayList<LatLng> tripPoints;
    TripRoute tripRoute;
    boolean fetched = true;
    boolean fetchPlaceID = true;
    LinearLayout defaultLayout, tripDetailsLayout, activeTripLayout, placesLayout, dataLayout, history2Layout, history1Layout;
    TextView history1Title, history2Title, activeTitleText, manuUsernameText, pickupText, activeDriverText, activeDriverPhoneNumberText, activePickupText, activeTotalCostText, activeStatusText, activeDurationText, activeDistanceText, activeDestinationText, tripDestinationText, tripDistanceText, tripDurationText, peam2Text, peam2Title, peam2Capacity, peam2CapacityCount, peam4Text, peam4Title, peam4Capacity, peam4CapacityCount;
    int tripTotalCostPeam4 = 0;
    int tripTotalCostPeam2 = 0;
    CustomResources customResources = new CustomResources();
    Button clearBTN, confirmBTN;
    List<Place> places;
    TextInputEditText mapSearch, startLocation;
    boolean getNewPlaces = true;
    int count = 1;
    int capacity = 0;
    private String previousSearch = null;
    String destinationPlaceID, customerName, currentLocationName, destinationName;
    boolean keyDown = true;
    private Integer PLACE_REQUEST_CODE = 100;
    private Integer START_LOCATION_REQUEST_CODE = 200;
    GridLayout selectPeam2, selectPeam4;
    ImageView peam4Icon, peam4CarIcon, peam2Icon, peam2CarIcon;
    LatLng history2LatLng = new LatLng(-1.2860088, 36.8257063);
    LatLng history1LatLng = new LatLng(-1.2107673, 36.79463680000001);
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    Trip trip = new Trip();
    Boolean hasActiveTrip = false;
    public LocationManager locationManager;
    public LocationListener locationListener;
    public Criteria criteria;
    LatLng originLatLng = null;
    public String pickUpMessage = "Pickup: My location";

    @SuppressLint({"MissingInflatedId", "ResourceAsColor", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create notification
        createNotificationChannel();
        //permission request
        locationPermissionRequest();
        //forcefully get application location
        forceRequestLocation();

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        defaultLayout = findViewById(R.id.m_main_layout);
        tripDetailsLayout = findViewById(R.id.m_trip_details);
        activeTripLayout = findViewById(R.id.m_active_details);
        placesLayout = findViewById(R.id.m_places_layout);
        places = new ArrayList<>();

        activeTitleText = findViewById(R.id.active_trip_title);
        tripDistanceText = findViewById(R.id.m_trip_distance);
        tripDurationText = findViewById(R.id.m_trip_time);
        peam2Text = findViewById(R.id.m_peam_2);
        tripDestinationText = findViewById(R.id.m_trip_destination);
        pickupText = findViewById(R.id.m_trip_pickup);

        peam4Text = findViewById(R.id.m_peam_4);
        peam4Title = findViewById(R.id.p_4_title);
        peam4Capacity = findViewById(R.id.p_4_capacity);
        peam4CapacityCount = findViewById(R.id.p_4_capacity_count);
        peam4Icon = findViewById(R.id.peam_4_people);
        peam4CarIcon = findViewById(R.id.peam_4_icon);

        peam2Text = findViewById(R.id.m_peam_2);
        peam2Title = findViewById(R.id.p_2_title);
        peam2Capacity = findViewById(R.id.p_2_capacity);
        peam2CapacityCount = findViewById(R.id.p_2_count_capacity);
        peam2Icon = findViewById(R.id.peam_2_people);
        peam2CarIcon = findViewById(R.id.peam_2_icon);


        clearBTN = findViewById(R.id.m_clear_button);
        confirmBTN = findViewById(R.id.m_confirm_trip);
        mapSearch = findViewById(R.id.map_search);
        startLocation = findViewById(R.id.start_location);
        dataLayout = findViewById(R.id.data_layout);
        selectPeam2 = findViewById(R.id.select_peam_2);
        selectPeam4 = findViewById(R.id.select_peam_4);

        activeDestinationText = findViewById(R.id.m_active_trip_destination);
        activeDistanceText = findViewById(R.id.m_trip_active_distance);
        activeDurationText = findViewById(R.id.m_active_trip_time);
        activeStatusText = findViewById(R.id.m_active_status);
        activeTotalCostText = findViewById(R.id.m_trip_active_total_cost);
        activePickupText = findViewById(R.id.m_active_pick_up);
        activeDriverText = findViewById(R.id.m_active_trip_driver);
        activeDriverPhoneNumberText = findViewById(R.id.m_trip_active_driver_phone);

        history1Layout = findViewById(R.id.m_two_rivers);
        history1Title = findViewById(R.id.am_history_1_title);
//        history1Text = findViewById(R.id.am_history_1_text);

        history2Layout = findViewById(R.id.m_kencom);
        history2Title = findViewById(R.id.am_history_2_title);
//        history2Text = findViewById(R.id.am_history_2_text);

        firebaseDatabase = FirebaseDatabase.getInstance();


        trip = new Trip();

        //bing forward
        dataLayout.bringToFront();
        Places.initialize(getApplicationContext(), GOOGLE_API_KEY);

        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        phoneNumber = sharedPreferences.getString("phone_number", "null");
        customerName = sharedPreferences.getString("name", "null");
        userID = sharedPreferences.getString("userID", "null");
        first_name = sharedPreferences.getString("first_name", "null");
        appToken = sharedPreferences.getString("app_token", "null");
        activeTripID = sharedPreferences.getString("active_trip_key", "null");

        updateTripLocally(false); //todo remove this

        COST_PER_KM_PEAM_2_SHORT = sharedPreferences.getInt("COST_PER_KM_PEAM_2_SHORT", COST_PER_KM_PEAM_2_SHORT);
        COST_PER_KM_PEAM_2_LONG = sharedPreferences.getInt("COST_PER_KM_PEAM_2_LONG", COST_PER_KM_PEAM_2_LONG);
        COST_PER_KM_PEAM_4_SHORT = sharedPreferences.getInt("COST_PER_KM_PEAM_4_SHORT", COST_PER_KM_PEAM_4_SHORT);
        COST_PER_KM_PEAM_4_LONG = sharedPreferences.getInt("COST_PER_KM_PEAM_4_LONG", COST_PER_KM_PEAM_4_LONG);
        MIN_COST = Double.parseDouble(sharedPreferences.getString("MIN_COST", MIN_COST.toString()));
        LONG_DISTANCE_MIN = Double.parseDouble(sharedPreferences.getString("LONG_DISTANCE_MIN", LONG_DISTANCE_MIN.toString()));
        getSettings();

        tripPoints = new ArrayList<>();

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
//        toolbar.setNavigationIcon(R.drawable.menu_ic_dark);
//        toolbar.setTitle("Welcome " + first_name);

        //actionate menu items
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        View menuLayout = navigationView.getHeaderView(0);
        manuUsernameText = menuLayout.findViewById(R.id.menu_user_name);
        manuUsernameText.setText(customerName);

        //click events
        onClick();

        //to stay here at the bottom
        showActiveTrip();

        //update app token
        checkAppTokenThread();

        getLast2Histories();
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

    @SuppressLint({"ResourceAsColor", "UseCompatLoadingForDrawables"})
    private void selectCabs(int i) {
        if (i == 2) {
            selectPeam2.setBackgroundResource(R.color.primary);
            selectPeam4.setBackgroundResource(R.color.white);

            peam4Text.setTextColor(getResources().getColor(R.color.black));
            peam4Title.setTextColor(getResources().getColor(R.color.black));
            peam4Capacity.setTextColor(getResources().getColor(R.color.black));
            peam4CapacityCount.setTextColor(getResources().getColor(R.color.black));
            peam4Icon.setImageDrawable(getResources().getDrawable(R.drawable.people));
            peam4CarIcon.setImageDrawable(getResources().getDrawable(R.drawable.car_black));


            peam2Text.setTextColor(getResources().getColor(R.color.white));
            peam2Title.setTextColor(getResources().getColor(R.color.white));
            peam2Capacity.setTextColor(getResources().getColor(R.color.white));
            peam2CapacityCount.setTextColor(getResources().getColor(R.color.white));
            peam2Icon.setImageDrawable(getResources().getDrawable(R.drawable.people_white));
            peam2CarIcon.setImageDrawable(getResources().getDrawable(R.drawable.car_white));
        }
        if (i == 4) {
            selectPeam2.setBackgroundResource(R.color.white);
            selectPeam4.setBackgroundResource(R.color.primary);

            peam4Text.setTextColor(getResources().getColor(R.color.white));
            peam4Title.setTextColor(getResources().getColor(R.color.white));
            peam4Capacity.setTextColor(getResources().getColor(R.color.white));
            peam4CapacityCount.setTextColor(getResources().getColor(R.color.white));
            peam4Icon.setImageDrawable(getResources().getDrawable(R.drawable.people_white));
            peam4CarIcon.setImageDrawable(getResources().getDrawable(R.drawable.car_white));

            peam2Text.setTextColor(getResources().getColor(R.color.black));
            peam2Title.setTextColor(getResources().getColor(R.color.black));
            peam2Capacity.setTextColor(getResources().getColor(R.color.black));
            peam2CapacityCount.setTextColor(getResources().getColor(R.color.black));
            peam2Icon.setImageDrawable(getResources().getDrawable(R.drawable.people));
            peam2CarIcon.setImageDrawable(getResources().getDrawable(R.drawable.car_black));
        }
    }

    /**
     * click events
     */
    @SuppressLint("SetTextI18n")
    private void onClick() {
        //clear all and move to current location
        clearBTN.setOnClickListener(v -> {
            startLocation.setText(pickUpMessage);
            showLayouts("default");
            originLatLng = null;
            getLastLocation();
            moveToCurrentLocation();
        });

        history2Layout.setOnClickListener(v -> {
            showLayouts("trip_details");
            LatLng origin = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            tripPoints.clear();
            tripPoints.add(origin);
            tripPoints.add(history2LatLng);

            // add second marker
            addMarkerOptions(tripPoints);

            //set name
            pickupText.setText(currentLocationName);
            String defaultDestinationName = "Kencom House";
            tripDestinationText.setText(defaultDestinationName);

            updateTripDetailsFromDefaults(new LatLong(history2LatLng.latitude, history2LatLng.longitude), defaultDestinationName);

            //prep trip
            if (historyList.size() >= 1) {
                updateTripDetails(historyList.get(1));
            }

            zoom = 11.05f;
            mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
            drawTripDirection();
        });

        history1Layout.setOnClickListener(v -> {
            LatLng origin = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            tripPoints.clear();
            tripPoints.add(origin);
            tripPoints.add(history1LatLng);

            //set name
            pickupText.setText(currentLocationName);
            String defaultDestinationName = "Two rivers shopping mall";
            tripDestinationText.setText(defaultDestinationName);


            updateTripDetailsFromDefaults(new LatLong(history2LatLng.latitude, history2LatLng.longitude), defaultDestinationName);

            //prep trip
            if (historyList.size() >= 1) {
                updateTripDetails(historyList.get(0));
            }

            // add second marker
            addMarkerOptions(tripPoints);
            zoom = 11.05f;
            mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
            drawTripDirection();
        });

        mapSearch.setFocusable(false);
        mapSearch.setOnClickListener(view -> {
            originLatLng = null;
            trip = new Trip();
            List<com.google.android.libraries.places.api.model.Place.Field> fieldList = Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ADDRESS, com.google.android.libraries.places.api.model.Place.Field.LAT_LNG, com.google.android.libraries.places.api.model.Place.Field.NAME);

            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList)
                    .build(MainActivity.this);
            startActivityForResult(intent, PLACE_REQUEST_CODE);
        });


        startLocation.setFocusable(false);
        startLocation.setOnClickListener(view -> {
            originLatLng = null;
            List<com.google.android.libraries.places.api.model.Place.Field> fieldList = Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ADDRESS, com.google.android.libraries.places.api.model.Place.Field.LAT_LNG, com.google.android.libraries.places.api.model.Place.Field.NAME);

            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList)
                    .build(MainActivity.this);
            startActivityForResult(intent, START_LOCATION_REQUEST_CODE);
        });

        //create tip to firebase
        confirmBTN.setOnClickListener(v -> {
            loadingDialog.startLoadingDialog();
            createTrip(trip);
            updateTripLocally(true);
            showLayouts("active_details");
            showActiveTrip();
            getTrip();
        });

        activeStatusText.setOnClickListener(v -> {
            if (trip.isCompleted()) {
                loadingDialog.startLoadingDialog();

                startLocation.setText(pickUpMessage);
                showLayouts("default");
                updateTripLocally(false);
                originLatLng = null;
                getLastLocation();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("active_trip_key", "null");
                editor.apply();

                //relaunch the activity
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            } else {
                Log.e(TAG, "Trip not completed = " + trip.getStatus());
            }
        });
    }


    /**
     *
     */
    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    private void showActiveTrip() {
        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);

        String totalCost = "Ksh ";
        String pickUp = sharedPreferences.getString("pick_up", "null");
        String destination = sharedPreferences.getString("destination", "null");
        String distance = sharedPreferences.getString("distance", "null");
        String tripTime = sharedPreferences.getString("trip_time", "null");
        String status = sharedPreferences.getString("status", "null");
        String driver = sharedPreferences.getString("driver", "null");
        String driverPhoneNumber = sharedPreferences.getString("driver_phone_number", "null");
        totalCost += customResources.numberFormat(sharedPreferences.getString("total_cost", "0"));
        boolean tripAvailable = sharedPreferences.getBoolean("trip_available", false);


        if (destination.equals("null")) {
            return;
        }

        showLayouts("active_details");
        destinationName = destination;
        hasActiveTrip = true;
        activePickupText.setText(pickUp);
        activeDestinationText.setText(destination);
        activeDistanceText.setText(distance);
        activeDurationText.setText(tripTime);
        activeStatusText.setText(status);
        activeTotalCostText.setText(totalCost);

        if (!driver.equals("null")) {
            findViewById(R.id.m_active_driver_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.m_active_driver_phone_layout).setVisibility(View.VISIBLE);
            activeDriverText.setText(driver);
            activeDriverPhoneNumberText.setText(driverPhoneNumber);

            activeDriverPhoneNumberText.setOnClickListener(v -> {
                loadingDialog.startLoadingDialog();
                String url = "tel://" + driverPhoneNumber;
                startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
            });
        }

        activeTitleText.setText(status);
        if (status.equalsIgnoreCase("onboard")) {
            activeTitleText.setText("Enroute to Destination");
            activeStatusText.setText("Enroute to Destination");
        }

        if (status.equalsIgnoreCase("requested a trip")) {
            activeStatusText.setText("Finding Driver");
        }

        if (status.equalsIgnoreCase("Trip Cancelled")) {
            activeStatusText.setText("Homepage");
            activeStatusText.setBackground(getResources().getDrawable(R.drawable.btn_light));

            activeTitleText.setText("Trip Cancelled");
        }

        if (status.equalsIgnoreCase("complete")) {
            activeStatusText.setText("Homepage");
            activeStatusText.setBackground(getResources().getDrawable(R.drawable.btn_light));

            activeTitleText.setText("Trip Summary");
        }
    }

    /**
     * update trip locally
     *
     * @param status
     */
    private void updateTripLocally(boolean status) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (status) {
            String distance = trip.getDistance() + " Kms";
            destinationName = trip.getDestinationName();
            editor.putString("pick_up", trip.getOriginName());
            editor.putString("destination", trip.getDestinationName());
            editor.putString("distance", distance);
            editor.putString("trip_time", trip.getDuration());
            editor.putString("total_cost", Integer.toString(trip.getPrice()));
            editor.putString("status", trip.getStatus());
            editor.putBoolean("trip_available", true);
            editor.putString("driver", trip.getDriverName());
            editor.putString("driver_phone_number", trip.getDriverPhoneNumber());
        } else {
            editor.putString("destination", null);
            editor.putString("distance", null);
            editor.putString("trip_time", null);
            editor.putString("total_cost", null);
            editor.putString("status", null);
            editor.putBoolean("trip_available", false);
            editor.putString("driver", null);
            editor.putString("driver_phone_number", null);
        }

        editor.apply();

        showActiveTrip();
    }

    //-----------------------------------onActivityResult-------------------------------//
    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LatLng origin;
        if (requestCode == PLACE_REQUEST_CODE && resultCode == RESULT_OK) {
//            loadingDialog.startLoadingDialog();
            //Success
            com.google.android.libraries.places.api.model.Place place = Autocomplete.getPlaceFromIntent(data);
            //Get
            destinationName = place.getName();
            String placeAddress = place.getAddress();
            LatLng destinationLatLng = place.getLatLng();

            //Set
            mapSearch.setText(destinationName);
            pickupText.setText(currentLocationName);
            tripDestinationText.setText(destinationName);
            origin = originLatLng != null ? originLatLng : new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            //add location here
            tripPoints.add(destinationLatLng);
            // add second marker
            addMarkerOptions(tripPoints);
            zoom = 11.05f;
            mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
            drawTripDirection();

            //update trip
            trip.setDestination(new LatLong(destinationLatLng.latitude, destinationLatLng.longitude));
            trip.setDestinationName(destinationName);
            trip.setOrigin(new LatLong(origin.latitude, origin.longitude));
            trip.setOriginName(currentLocationName);
            trip.setStatus(REQUEST);
            trip.setCompleted(false);
            trip.setUserID(Integer.parseInt(userID));
            trip.setPhoneNumber(phoneNumber);
            trip.setCustomerName(customerName);
            trip.setDate(currentDate());

        } else if (requestCode == START_LOCATION_REQUEST_CODE && resultCode == RESULT_OK) {
            com.google.android.libraries.places.api.model.Place place = Autocomplete.getPlaceFromIntent(data);

            //add location here
            tripPoints.clear();
            tripPoints.add(place.getLatLng());

            //Get current location
            currentLocationName = place.getName();
            originLatLng = place.getLatLng();
            startLocation.setText("Pickup: " + currentLocationName);
            moveToCurrentLocation();
        } else if (requestCode == AutocompleteActivity.RESULT_ERROR) {
            //Error
            Status status = Autocomplete.getStatusFromIntent(data);
//            Toast.makeText(getApplicationContext(), "" + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        } else {
//            Toast.makeText(getApplicationContext(), "An error occurred!!", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String currentDate() {
        Date myDate = new Date();
        String date = new SimpleDateFormat("dd MMM, yyyy").format(myDate);
        return date;
    }

    /**
     * @param tripPoints
     */
    private void addMarkerOptions(ArrayList<LatLng> tripPoints) {
        MarkerOptions markerOptions = new MarkerOptions();
        //add first marker
        markerOptions.position(tripPoints.get(0));
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        markerOptions.title(currentLocationName);

        // add second marker
        markerOptions.position(tripPoints.get(1));
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        markerOptions.title(destinationName);
        mMap.addMarker(markerOptions);
    }

    /**
     * @param historyTrip
     */
    private void updateTripDetails(Trip historyTrip) {
        //update trip
        trip = new Trip();

        trip.setDestination(historyTrip.getDestination());
        trip.setDestinationName(historyTrip.getDestinationName());
        trip.setOrigin(new LatLong(currentLocation.getLatitude(), currentLocation.getLongitude()));
        trip.setOriginName(currentLocationName);
        trip.setStatus(REQUEST);
        trip.setUserID(Integer.parseInt(userID));
        trip.setPhoneNumber(historyTrip.getPhoneNumber());
        trip.setCustomerName(historyTrip.getCustomerName());
        trip.setDistance(historyTrip.getDistance());
        trip.setDuration(historyTrip.getDuration());


        //set name
        pickupText.setText(currentLocationName);
        tripDestinationText.setText(trip.getDestinationName());

        String distanceInKMs = historyTrip.getDistance() + " Kms";
        showTripDetails(distanceInKMs, historyTrip.getDuration(), Double.toString(historyTrip.getDistance() * 1000));
    }

    /**
     * @param destinationLatLng
     * @param destinationName
     */
    private void updateTripDetailsFromDefaults(LatLong destinationLatLng, String destinationName) {
        //update trip
        trip = new Trip();

        trip.setDestination(destinationLatLng);
        trip.setDestinationName(destinationName);
        trip.setOrigin(new LatLong(currentLocation.getLatitude(), currentLocation.getLongitude()));
        trip.setOriginName(currentLocationName);
        trip.setStatus(REQUEST);
        trip.setUserID(Integer.parseInt(userID));
        trip.setPhoneNumber(phoneNumber);
        trip.setCustomerName(customerName);
    }

    /**
     *
     */
    private void getLastLocation() {
        originLatLng = null;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest();
            return;
        }

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            Location lastLocation = location;
            if (location == null) {
                forceRequestLocation();
                getLastLocation();
                return;
            }
            currentLocation = location;
            currentLocationName = getCompleteAddressString(location.getLatitude(), location.getLongitude());

            mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
            assert mapFragment != null;
            mapFragment.getMapAsync(MainActivity.this);
        });
    }

    /**
     * @param LATITUDE
     * @param LONGITUDE
     * @return
     */
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder();
                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    if (i >= 0) {
                        strReturnedAddress.append(returnedAddress.getAddressLine(i));
                        strReturnedAddress.append(",");
                    }
                }

                strAdd = strReturnedAddress.toString();

                //split and remove first index
                String[] split = strAdd.split(",");
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < (split.length - 1); i++) {
                    sb.append(split[i]);
                    if (i != split.length - 2) {
                        sb.append(",");
                    }
                }

                strAdd = strAdd.replace("Kenya", "");
                strAdd = sb.toString().replace(".", "").trim();


            } else {
                Log.w(TAG, "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "Cannot get Address!");
        }
        return strAdd;
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
            Toast.makeText(this, "Book a trip selected", LENGTH_LONG).show();
        } else if (item.getItemId() == R.id.nav_my_trips) {
            loadingDialog.startLoadingDialog();
            startActivity(new Intent(this, MyTripsActivity.class));
        } else if (item.getItemId() == R.id.nav_offers) {
            Toast.makeText(this, "Offers selected", LENGTH_LONG).show();
        } else if (item.getItemId() == R.id.nav_help) {
            Toast.makeText(this, "Help selected", LENGTH_LONG).show();
        } else if (item.getItemId() == R.id.nav_about) {
            Toast.makeText(this, "About selected", LENGTH_LONG).show();
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


    @SuppressLint("NewApi")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        //default get trip
        getTrip();

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
    }

    /**
     *
     */
    private void drawTripDirection() {
        fetched = false;
        if (tripPoints.size() == 0) {
            return;
        }
        String url = getRequestURL(tripPoints.get(0), tripPoints.get(1));
        TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
        taskRequestDirections.execute(url);
    }

    /**
     * move focus on current location
     */
    @SuppressLint("SetTextI18n")
    private void moveToCurrentLocation() {
        LatLng myLocation = originLatLng != null ? originLatLng : new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        markerOptions.title(currentLocationName);
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

//                                getLastLocation();
//                                getLocation();
//                                getCurrentLocation();
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
//                getLastLocation();
//                getLocation();
//                getCurrentLocation();
            }
            locationPermissionRequest();
            Toast.makeText(this, "Location permission is denied.", LENGTH_LONG).show();
        }
    }


    /**
     * Token is generated from the background
     */
    private void checkAppTokenThread() {
        //update only if the token is not available
        if (appToken.equalsIgnoreCase("null")) {
            Runnable tokenRunnable = this::getToken;
            Thread tokenThread = new Thread(tokenRunnable);
            tokenThread.start();
        }
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
//
//            Log.e(TAG, response.toString());
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

    @Override
    public void selectedPlace(Place selectedPlace) {
//        loadingDialog.startLoadingDialog();
//        Toast.makeText(MainActivity.this, "You clicked " + selectedPlace.getName(), Toast.LENGTH_LONG).show();
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
                        if (!fetched && !hasActiveTrip) {
                            showTripDetails(distance, duration, actualDistance);
                        }

                        fetched = true;
                    }

                    polylineOptions.addAll(points);
                    polylineOptions.width(6);
                    polylineOptions.color(Color.BLUE);
                    polylineOptions.geodesic(true);
                }

                if (polylineOptions != null) {
                    mMap.addPolyline(polylineOptions);
                } else {
                    Toast.makeText(getApplicationContext(), "Direction not found", LENGTH_LONG).show();
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
        tripTotalCostPeam4 = getTripTotalCost(Double.parseDouble(actualDistance), PEAM_4);
        tripTotalCostPeam2 = getTripTotalCost(Double.parseDouble(actualDistance), PEAM_2);

        //set values
        tripDistanceText.setText(distance);
        tripDurationText.setText(duration);

        String totalPeam4Text = "Ksh " + customResources.numberFormat(tripTotalCostPeam4);
        String totalPeam2Text = "Ksh " + customResources.numberFormat(tripTotalCostPeam2);

        peam2Text.setText(totalPeam2Text);
        peam4Text.setText(totalPeam4Text);

        placesLayout.setVisibility(View.GONE);
        mapSearch.setText("");
        loadingDialog.dismissDialog();

        //update trip
        trip.setDistance(Double.parseDouble(actualDistance) / 1000);
        trip.setDuration(duration);
        trip.setOnboard(false);

        //selects
        selectCabToBoard(PEAM_4, tripTotalCostPeam4, 4);
        findViewById(R.id.select_peam_4).setOnClickListener(v -> selectCabToBoard(PEAM_4, tripTotalCostPeam4, 4));
        findViewById(R.id.select_peam_2).setOnClickListener(v -> selectCabToBoard(PEAM_2, tripTotalCostPeam2, 2));

        showLayouts("trip_details");
    }

    /**
     * /**
     * show layouts
     *
     * @param layout
     */
    public void showLayouts(String layout) {
        if (layout.equals("default")) {
            zoom = 15.0f;
            defaultLayout.setVisibility(View.VISIBLE);
            tripDetailsLayout.setVisibility(View.GONE);
            activeTripLayout.setVisibility(View.GONE);
            moveToCurrentLocation();
        }
        if (layout.equals("trip_details")) {
            zoom = 11.0f;
            tripDetailsLayout.setVisibility(View.VISIBLE);
            defaultLayout.setVisibility(View.GONE);
            activeTripLayout.setVisibility(View.GONE);
        }
        if (layout.equals("active_details")) {
            zoom = 11.0f;
            activeTripLayout.setVisibility(View.VISIBLE);
            tripDetailsLayout.setVisibility(View.GONE);
            defaultLayout.setVisibility(View.GONE);
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
                    capacity = 4;
                    costPerkM = COST_PER_KM_PEAM_4_SHORT;
                    break;
                case PEAM_2:
                    capacity = 2;
                    costPerkM = COST_PER_KM_PEAM_2_SHORT;
                    break;
            }
        } else {
            switch (type) {
                case PEAM_4:
                    capacity = 4;
                    costPerkM = COST_PER_KM_PEAM_4_LONG;
                    break;
                case PEAM_2:
                    capacity = 2;
                    costPerkM = COST_PER_KM_PEAM_2_LONG;
                    break;
            }
        }

        double cost = (actualDistance / 1000 * costPerkM);

        //check minimum
        if (cost < MIN_COST) {
            cost = MIN_COST;
        }
        return (int) cost;
    }

    private void selectCabToBoard(String type, int cost, int cabCapacity) {
        trip.setCabType(type);
        trip.setPrice((int) cost);
        trip.setCapacity(cabCapacity);

        selectCabs(cabCapacity);
    }


    /**
     * add layouts
     *
     * @param places
     */
    private void updatePlacesLayout(List<Place> places) {
        //remove all existing views
        placesLayout.setVisibility(View.VISIBLE);
        placesLayout.removeAllViews();
        int suggestions = 1;

        for (Place place : places) {
            LayoutInflater inflater = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            assert inflater != null;
            View layoutToAdd = inflater.inflate(R.layout.places_list, placesLayout, false);

            TextView name = layoutToAdd.findViewById(R.id.p_name);
            TextView city = layoutToAdd.findViewById(R.id.p_city);

            name.setText(place.getName());
            city.setText(customResources.getCityCountry(place.getName()));

            name.setId(count);
            count++;
            city.setId(count);
            count++;
            layoutToAdd.setId(count);

            layoutToAdd.setOnClickListener(view -> {
//                loadingDialog.startLoadingDialog();
                CustomResources.hideKeyboard(this);

                fetchPlaceID = true;
                CalculateLatLon calculateLatLon = new CalculateLatLon();

                destinationPlaceID = place.getPlaceID();
                calculateLatLon.onPostExecute(place.getPlaceID());
            });

            count++;

            //access interface
//            this.selectedPlace(place);

            placesLayout.addView(layoutToAdd);

            getNewPlaces = true;
            suggestions++;
            if (suggestions >= 4) {
                break;
            }
        }
    }

    /**
     *
     */
    public class CalculateLatLon extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            fetchPlaceID = false;
            String url = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + strings[0] + "&key=" + GOOGLE_API_KEY + "";

            StringBuilder stringBuilder = new StringBuilder();
            HttpPost httppost = null;
            HttpClient client = null;
            HttpResponse response = null;
            HttpEntity entity = null;
            InputStream stream = null;
            getNewPlaces = true;

            try {
                httppost = new HttpPost(url);
                client = new DefaultHttpClient();
                stringBuilder = new StringBuilder();


                response = client.execute((HttpUriRequest) httppost);
                entity = response.getEntity();
                stream = entity.getContent();
                int b;
                while ((b = stream.read()) != -1) {
                    stringBuilder.append((char) b);
                }
            } catch (ClientProtocolException ignored) {
            } catch (IOException e) {
                Log.e(TAG, "getLocationInfo IOException " + e.getMessage());
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }


            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(stringBuilder.toString());

                JSONObject result = (JSONObject) new JSONObject(jsonObject.getString("result"));
                JSONObject geometry = (JSONObject) new JSONObject(result.getString("geometry"));
                JSONObject location = (JSONObject) new JSONObject(geometry.getString("location"));

                LatLng origin = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                LatLng destination = new LatLng(Double.parseDouble(location.getString("lat")), Double.parseDouble(location.getString("lng")));

                //add location here
                tripPoints.clear();
                tripPoints.add(origin);
                tripPoints.add(destination);

                drawTripDirection();

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return "null";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (fetchPlaceID) {
                CalculateLatLon calculateLatLon = new CalculateLatLon();
                calculateLatLon.execute(s);
            }
        }
    }

    public void createTrip(Trip trip) {
        databaseReference = firebaseDatabase.getReference();
        DatabaseReference updateTrip = databaseReference.child("trips");
        String key = updateTrip.push().getKey();
        trip.setTripKey(key);
        updateTrip.child(key).setValue(trip);

        //add editor
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("active_trip_key", key);
        editor.apply();

        createTripWeb(key);
    }

    /**
     *
     */
    public void getTrip() {
        activeTripID = sharedPreferences.getString("active_trip_key", "null");
        databaseReference = firebaseDatabase.getReference("trips");
        DatabaseReference updateTrip = databaseReference.child("");
        Query updatedTrip;

        if (!activeTripID.equals("null")) {
            updatedTrip = updateTrip
                    .orderByChild("tripKey")
                    .equalTo(activeTripID)
                    .limitToLast(1);
        } else {
            updatedTrip = updateTrip
                    .orderByChild("userID")
                    .equalTo(Integer.parseInt(userID))
                    .limitToLast(1);
        }

        updatedTrip.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean hasTrip = false;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    trip = dataSnapshot.getValue(Trip.class);

                    if (!activeTripID.equals("null")) {
                        updateTripLocally(true);
                        return;
                    }

                    if (!trip.isCompleted() || activeTripID.equals(dataSnapshot.getKey())) {
                        hasTrip = true;
                        updateTripLocally(true);

                        tripPoints.clear();
                        tripPoints.add(trip.getOrigin().getLatLng());
                        tripPoints.add(trip.getDestination().getLatLng());
                        // add second marker
                        addMarkerOptions(tripPoints);

                        zoom = 11.05f;
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));

                        fetched = true;//disable showing trip details
                        drawTripDirection();

                        //Active trip here
                        loadingDialog.dismissDialog();

                        //add editor
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("active_trip_key", dataSnapshot.getKey());
                        editor.apply();
                    }
                }

//                        if (!hasTrip) {
//                            showLayouts("default");
//                            updateTripLocally(false);
//                        }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Reading data error = " + error.getMessage());
            }
        });
    }

    /**
     *
     */
    public void getSettings() {
        databaseReference = firebaseDatabase.getReference("settings");
        DatabaseReference updateTrip = databaseReference;
        updateTrip.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    return;
                }

                COST_PER_KM_PEAM_2_SHORT = Integer.parseInt(snapshot.child("peam2Short").getValue().toString());
                COST_PER_KM_PEAM_2_LONG = Integer.parseInt(snapshot.child("peam2Long").getValue().toString());
                COST_PER_KM_PEAM_4_SHORT = Integer.parseInt(snapshot.child("peam4Short").getValue().toString());
                COST_PER_KM_PEAM_4_LONG = Integer.parseInt(snapshot.child("peam4Long").getValue().toString());
                MIN_COST = Double.parseDouble(snapshot.child("minimumCost").getValue().toString());
                LONG_DISTANCE_MIN = Double.parseDouble(snapshot.child("longDistance").getValue().toString());

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("COST_PER_KM_PEAM_2_SHORT", COST_PER_KM_PEAM_2_SHORT);
                editor.putInt("COST_PER_KM_PEAM_2_LONG", COST_PER_KM_PEAM_2_LONG);
                editor.putInt("COST_PER_KM_PEAM_4_SHORT", COST_PER_KM_PEAM_4_SHORT);
                editor.putInt("COST_PER_KM_PEAM_4_LONG", COST_PER_KM_PEAM_4_LONG);
                editor.putString("MIN_COST", MIN_COST.toString());
                editor.putString("LONG_DISTANCE_MIN", LONG_DISTANCE_MIN.toString());
                editor.apply();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Reading data error = " + error.getMessage());
            }

        });
    }

    /**
     * update token to server
     */
    private void createTripWeb(String tripKey) {
        String url = getURL(CREATE_TRIP_URL);
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            loadingDialog.dismissDialog();
        }, error -> {
            Log.e(TAG, "Error => " + error.toString());
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> param = new HashMap<>();
                param.put("user_id", userID);
                param.put("originName", currentLocationName);
                param.put("destinationName", destinationName);
                param.put("price", Integer.toString(trip.getPrice()));
                param.put("capacity", Integer.toString(trip.getCapacity()));
                param.put("duration", trip.getDuration());
                param.put("status", trip.getStatus());
                param.put("cabType", trip.getCabType());
                param.put("distance", Double.toString(trip.getDistance()));
                param.put("origin", trip.getOrigin().getLatLngString());
                param.put("destination", trip.getDestination().getLatLngString());
                param.put("tripKey", tripKey);
                return param;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(VOLLEY_TIME_OUT, RETRIES, 1.0f));
        requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(request);
    }


    /**
     * Create notification channel to receive messages
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "notifications";
            String description = "Receive firebase notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    /**
     *
     */
    private void getLast2Histories() {
        historyList = new ArrayList<>();
        ArrayList<Trip> trips = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("trips");
        Query updatedTrip;

        updatedTrip = databaseReference
                .orderByChild("userID")
                .equalTo(Integer.parseInt(userID));

        updatedTrip.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    //Arraylist
                    trips.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Trip trip = dataSnapshot.getValue(Trip.class);

                        if (!trip.isCompleted()) {
                            continue;
                        }
                        trips.add(trip);
                    }

                    Collections.reverse(trips);

                    int count = 0;
                    String previousName = "";
                    for (Trip trip : trips) {
                        count++;

                        if (count == 1) {
                            historyList.add(trip);
                            history1LatLng = trip.getDestination().getLatLng();
                            history1Title.setText(trip.getDestinationName());
//                            history1Text.setText(trip.getDuration() + " Away");
                            previousName = trip.getDestinationName();
                        }

                        if (count == 2) {
                            //prevent same desitnations in quick action table
                            if (previousName.equals(trip.getDestinationName())) {
                                count = 1;
                                continue;
                            }

                            historyList.add(trip);
                            history2LatLng = trip.getDestination().getLatLng();
                            history2Title.setText(trip.getDestinationName());
//                            history2Text.setText(trip.getDuration() + " Away");
                        }

                        if (count >= 2) {
                            break;
                        }
                    }

                    //update ui
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}