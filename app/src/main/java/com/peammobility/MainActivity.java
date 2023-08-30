package com.peammobility;

import static android.widget.Toast.LENGTH_LONG;
import static com.peammobility.classes.Env.RETRIES;
import static com.peammobility.classes.Env.UPDATE_APP_TOKEN_URL;
import static com.peammobility.classes.Env.VOLLEY_TIME_OUT;
import static com.peammobility.classes.Env.getURL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.nfc.Tag;
import android.os.AsyncTask;
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
import com.google.android.gms.common.api.Status;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, SelectPlaceInterface {
    private static final int FINE_PERMISSION_CODE = 1;
    private static final String TAG = "PEAM DEBUG";
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
    private static final String FIREBASE_REFERENCE_TRIPS = "trips";
    private static final String REQUEST = "request";
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
    boolean fetchPlaceID = true;
    LinearLayout defaultLayout, tripDetailsLayout, placesLayout, dataLayout, kencomLayout, twoRiversLayout;
    TextView tripDestinationText, tripDistanceText, tripDurationText, peam2Text, peam2Title, peam2Capacity, peam2CapacityCount, peam4Text, peam4Title, peam4Capacity, peam4CapacityCount;
    int tripTotalCostPeam4 = 0;
    int tripTotalCostPeam2 = 0;
    CustomResources customResources = new CustomResources();
    Button clearBTN, confirmBTN;
    List<Place> places;
    TextInputEditText mapSearch;
    boolean getNewPlaces = true;
    int count = 1;
    int capacity = 0;
    private String previousSearch = null;
    String destinationPlaceID, customerName;
    boolean keyDown = true;
    float zoom = 15.0f;
    private Integer PLACE_REQUEST_CODE = 100;
    GridLayout selectPeam2, selectPeam4;
    ImageView peam4Icon, peam2Icon;
    LatLng kencomLatLng = new LatLng(-1.2860088, 36.8257063);
    LatLng twoRiversLatLng = new LatLng(-1.2107673, 36.79463680000001);
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    Trip trip = new Trip();

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
        placesLayout = findViewById(R.id.m_places_layout);
        places = new ArrayList<>();

        tripDistanceText = findViewById(R.id.m_trip_distance);
        tripDurationText = findViewById(R.id.m_trip_time);
        peam2Text = findViewById(R.id.m_peam_2);
        tripDestinationText = findViewById(R.id.m_trip_destination);

        peam4Text = findViewById(R.id.m_peam_4);
        peam4Title = findViewById(R.id.p_4_title);
        peam4Capacity = findViewById(R.id.p_4_capacity);
        peam4CapacityCount = findViewById(R.id.p_4_capacity_count);
        peam4Icon = findViewById(R.id.peam_4_people);

        peam2Text = findViewById(R.id.m_peam_2);
        peam2Title = findViewById(R.id.p_2_title);
        peam2Capacity = findViewById(R.id.p_2_capacity);
        peam2CapacityCount = findViewById(R.id.p_2_count_capacity);
        peam2Icon = findViewById(R.id.peam_2_people);


        clearBTN = findViewById(R.id.m_clear_button);
        confirmBTN = findViewById(R.id.m_confirm_trip);
        mapSearch = findViewById(R.id.map_search);
        dataLayout = findViewById(R.id.data_layout);
        selectPeam2 = findViewById(R.id.select_peam_2);
        selectPeam4 = findViewById(R.id.select_peam_4);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        kencomLayout = findViewById(R.id.m_kencom);
        twoRiversLayout = findViewById(R.id.m_two_rivers);
        trip = new Trip();

        //bing forward
        dataLayout.bringToFront();
        Places.initialize(getApplicationContext(), GOOGLE_API_KEY);

        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        phoneNumber = sharedPreferences.getString("phone_number", "null");
        customerName = sharedPreferences.getString("name", "null");
        userID = sharedPreferences.getString("userID", "null");
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

        //select default
        selectCabs(4);
        //click events
        onClick();
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

            peam2Text.setTextColor(getResources().getColor(R.color.white));
            peam2Title.setTextColor(getResources().getColor(R.color.white));
            peam2Capacity.setTextColor(getResources().getColor(R.color.white));
            peam2CapacityCount.setTextColor(getResources().getColor(R.color.white));
            peam2Icon.setImageDrawable(getResources().getDrawable(R.drawable.people_white));
        }
        if (i == 4) {
            selectPeam2.setBackgroundResource(R.color.white);
            selectPeam4.setBackgroundResource(R.color.primary);

            peam4Text.setTextColor(getResources().getColor(R.color.white));
            peam4Title.setTextColor(getResources().getColor(R.color.white));
            peam4Capacity.setTextColor(getResources().getColor(R.color.white));
            peam4CapacityCount.setTextColor(getResources().getColor(R.color.white));
            peam4Icon.setImageDrawable(getResources().getDrawable(R.drawable.people_white));

            peam2Text.setTextColor(getResources().getColor(R.color.black));
            peam2Title.setTextColor(getResources().getColor(R.color.black));
            peam2Capacity.setTextColor(getResources().getColor(R.color.black));
            peam2CapacityCount.setTextColor(getResources().getColor(R.color.black));
            peam2Icon.setImageDrawable(getResources().getDrawable(R.drawable.people));
        }
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
        selectPeam2.setOnClickListener(v -> selectCabs(2));
        selectPeam4.setOnClickListener(v -> selectCabs(4));
        kencomLayout.setOnClickListener(v -> {
//            loadingDialog.startLoadingDialog();

            LatLng origin = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            tripPoints.clear();
            tripPoints.add(origin);
            tripPoints.add(kencomLatLng);
            // add second marker
            addMarkerOptions(tripPoints);
            zoom = 11.05f;
            mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
            drawTripDirection();
        });
        twoRiversLayout.setOnClickListener(v -> {
            LatLng origin = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            tripPoints.clear();
            tripPoints.add(origin);
            tripPoints.add(twoRiversLatLng);
            // add second marker
            addMarkerOptions(tripPoints);
            zoom = 11.05f;
            mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
            drawTripDirection();
        });

        mapSearch.setFocusable(false);
        mapSearch.setOnClickListener(view -> {
            trip = new Trip();
            List<com.google.android.libraries.places.api.model.Place.Field> fieldList = Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ADDRESS, com.google.android.libraries.places.api.model.Place.Field.LAT_LNG, com.google.android.libraries.places.api.model.Place.Field.NAME);


            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList)
                    .build(MainActivity.this);
            startActivityForResult(intent, PLACE_REQUEST_CODE);
        });

        //create tip to firebase
        confirmBTN.setOnClickListener(v -> {
            createTrip(trip);

            getTrip();
        });
    }

    //-----------------------------------onActivityResult-------------------------------//
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_REQUEST_CODE && resultCode == RESULT_OK) {
//            loadingDialog.startLoadingDialog();
            //Success
            com.google.android.libraries.places.api.model.Place place = Autocomplete.getPlaceFromIntent(data);
            //Get
            String placeName = place.getName();
            String placeAddress = place.getAddress();
            LatLng destinationLatLng = place.getLatLng();

            //Set
            mapSearch.setText(placeName);
            tripDestinationText.setText(placeName);
            LatLng origin = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            //add location here
            tripPoints.clear();
            tripPoints.add(origin);
            tripPoints.add(destinationLatLng);
            // add second marker
            addMarkerOptions(tripPoints);
            zoom = 11.05f;
            mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
            drawTripDirection();

            //update trip
            trip.setDestination(new LatLong(destinationLatLng.latitude, destinationLatLng.longitude));
            trip.setDestinationName(placeName);
            trip.setOrigin(new LatLong(origin.latitude, origin.longitude));
            trip.setOriginName("my location");
            trip.setStatus(REQUEST);
            trip.setCompleted(false);
            trip.setUserID(Integer.parseInt(userID));
            trip.setPhoneNumber(phoneNumber);
            trip.setCustomerName(customerName);

        } else if (requestCode == AutocompleteActivity.RESULT_ERROR) {
            //Error
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(getApplicationContext(), "" + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "An error occurred!!", Toast.LENGTH_SHORT).show();
        }
    }

    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext.setQueryRateLimit(3)
                .setApiKey(GOOGLE_API_KEY)
                .setConnectTimeout(1, TimeUnit.SECONDS)
                .setReadTimeout(1, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS);
    }

    private void addMarkerOptions(ArrayList<LatLng> tripPoints) {
        MarkerOptions markerOptions = new MarkerOptions();
        //add first marker
        markerOptions.position(tripPoints.get(0));
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        markerOptions.title(PICK_UP);

        // add second marker
        markerOptions.position(tripPoints.get(1));
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        markerOptions.title(DESTINATION);

        mMap.addMarker(markerOptions);
    }

    /**
     * geo locate
     */
    private void geoLocate(String searchLocation) {

        if (searchLocation.equals(previousSearch)) {
            return;
        }

        previousSearch = searchLocation;

        TaskGetLocationsByName taskGetLocationsByName = new TaskGetLocationsByName();
        taskGetLocationsByName.execute(searchLocation);
//        placesLayout.removeAllViews();

        if (places != null) {
            placesLayout.removeAllViews();
            updatePlacesLayout(places);
//                    places.clear();
        }
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
            Toast.makeText(this, "Book a trip selected", LENGTH_LONG).show();
        } else if (item.getItemId() == R.id.nav_my_trips) {
            Toast.makeText(this, "My trips selected", LENGTH_LONG).show();
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
                drawTripDirection();
            }
        });
    }

    /**
     *
     */
    private void drawTripDirection() {
        fetched = false;
        String url = getRequestURL(tripPoints.get(0), tripPoints.get(1));
        TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
        taskRequestDirections.execute(url);
    }

    /**
     *
     */
    private void drawTripDirectionSDK(LatLng origin, LatLng destinationLatLng) {
        fetched = false;
        DateTime now = new DateTime();
        try {
            DirectionsResult result = DirectionsApi.newRequest(getGeoContext())
                    .mode(TravelMode.DRIVING).origin(String.valueOf(origin))
                    .destination(String.valueOf(destinationLatLng)).departureTime(now)
                    .await();
        } catch (ApiException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * move focus on current location
     */
    private void moveToCurrentLocation() {
        showLayouts("default");
        LatLng myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
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
            Toast.makeText(this, "Location permission is denied.", LENGTH_LONG).show();
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
                        if (!fetched) {
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

        placesLayout.setVisibility(View.GONE);
        mapSearch.setText("");
        loadingDialog.dismissDialog();

        //update trip
        trip.setDistance(Double.parseDouble(actualDistance) / 1000);
        trip.setDuration(duration);
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
        }
        if (layout.equals("trip_details")) {
            zoom = 11.0f;
            defaultLayout.setVisibility(View.GONE);
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

        trip.setCabType(type);
        trip.setPrice((int) cost);
        trip.setCapacity(capacity);

        return (int) cost;
    }

    /**
     *
     */
    public class TaskGetLocationsByName extends AsyncTask<String, Void, String> {
        boolean fetched = false;

        @Override
        protected String doInBackground(String... strings) {
            fetched = false;

            if (strings.length == 0) {
                return "null";
            }

            try {
                if (getNewPlaces) {
                    places = getAddressByWeb(getLocationInfo(strings[0]));
                } else {
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            assert places != null;
            return "null";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (!fetched) {
                //parse our json here
                TaskGetLocationsByName taskGetLocationsByName = new TaskGetLocationsByName();
                taskGetLocationsByName.execute();
                fetched = true;
            }
        }

        public JSONObject getLocationInfo(String address) throws IOException {
            StringBuilder stringBuilder = new StringBuilder();
            HttpPost httppost = null;
            HttpClient client = null;
            HttpResponse response = null;
            HttpEntity entity = null;
            InputStream stream = null;
            String url = null;
            getNewPlaces = true;

            try {
                address = address.replaceAll(" ", "%20");
                url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?radius=200000&strictbounds=true&location=-1.286389%2C36.817223&input=" + address + "&types=geocode" + "&" + "key=" + GOOGLE_API_KEY;

//                httppost = new HttpPost("https://maps.google.com/maps/api/geocode/json?address=" + address + "&sensor=false" + "&" + "key=" + GOOGLE_API_KEY);
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
                    stream.close();
                }
            }


            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject = new JSONObject(stringBuilder.toString());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return jsonObject;
        }

        private List<Place> getAddressByWeb(JSONObject jsonObject) {
            List<Place> res = new ArrayList<Place>();
            try {
                JSONArray array = (JSONArray) jsonObject.get("predictions");
                for (int i = 0; i < array.length(); i++) {
                    Place place = new Place();
                    String name, placeID = "";
                    try {
                        name = array.getJSONObject(i).getString("description");
                        placeID = array.getJSONObject(i).getString("place_id");

                        place.setName(name);
                        place.setPlaceID(placeID);

                        res.add(place);
                    } catch (JSONException e) {
                        e.printStackTrace();

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();

            }

            return res;
        }
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
        DatabaseReference updateTrip = databaseReference.child("trips");
        updateTrip.push().setValue(trip);
    }

    public void getTrip() {
        DatabaseReference updateTrip = databaseReference.child("trips");
        updateTrip
                .orderByChild("userID")
                .equalTo(Integer.parseInt(userID))
                .limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                            Log.e(TAG, "Test stst tst stts tst <-------> " + dataSnapshot.toString());
                            trip = dataSnapshot.getValue(Trip.class);
                            Log.e(TAG, "Destination name <-------> " + trip.getDestinationName());

                            //Active trip here
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Reading data error = " + error.getMessage());
                    }
                });
    }
}