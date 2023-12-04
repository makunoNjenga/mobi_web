package com.peammobility.trips;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.peammobility.MainActivity;
import com.peammobility.R;
import com.peammobility.classes.Trip;
import com.peammobility.resources.LoadingDialog;

import java.util.ArrayList;
import java.util.Collections;

public class MyTripsActivity extends AppCompatActivity implements TripInterface {

    public static final String TAG = "PEAM DEBUG";
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    RecyclerView tripRecyclerView;
    String userID;
    Boolean collapsed = false;
    SharedPreferences sharedPreferences;
    ArrayList<Trip> myTrips = new ArrayList<>();
    LoadingDialog loadingDialog = new LoadingDialog(this);

    MyTripAdapter myTripAdapter;
    LinearLayout noTripsLayout;
    TextView noDataTitleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_trips);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        tripRecyclerView = findViewById(R.id.mt_recycler_view);
        noTripsLayout = findViewById(R.id.mt_available_no_trips);
        noDataTitleText = findViewById(R.id.mt_data_title);

        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        userID = sharedPreferences.getString("userID", "null");

        loadMyTrips();

        onClick();
    }


    /**
     *
     */
    @SuppressLint("SetTextI18n")
    private void loadMyTrips() {
        databaseReference = FirebaseDatabase.getInstance().getReference("trips");
        Query updatedTrip = databaseReference
                .orderByChild("userID")
                .equalTo(Integer.parseInt(userID));


        tripRecyclerView.setHasFixedSize(true);
        tripRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        myTrips = new ArrayList<>();
        myTripAdapter = new MyTripAdapter(this, myTrips, this);
        tripRecyclerView.setAdapter(myTripAdapter);

        updatedTrip.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean hasData = false;
                //Arraylist
                myTrips.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Trip trip = dataSnapshot.getValue(Trip.class);
                    myTrips.add(trip);
                    hasData = true;
                }

                Collections.reverse(myTrips);
                myTripAdapter.notifyDataSetChanged();

                if (!hasData) {
                    noDataTitleText.setText("No data found");
                } else {
                    noTripsLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void onClick() {
        findViewById(R.id.mt_back_icon).setOnClickListener(v -> {
            loadingDialog.startLoadingDialog();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }


    @Override
    public void onTripClick(int position, TableLayout collapsibleLayout) {
        //collapse the layout
        collapsed = !collapsed;
//        collapsibleLayout.setVisibility(collapsed ? View.VISIBLE : View.GONE);
        startActivity(new Intent(this, TripViewActivity.class));
    }
}