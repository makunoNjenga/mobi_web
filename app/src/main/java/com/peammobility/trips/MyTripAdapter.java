package com.peammobility.trips;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.peammobility.R;
import com.peammobility.classes.Trip;
import com.peammobility.resources.CustomResources;

import java.util.ArrayList;


public class MyTripAdapter extends RecyclerView.Adapter<MyTripAdapter.TripHolder> {
    Context context;
    String previousHeader = "null";
    ArrayList<Trip> tripArrayList;
    private final TripInterface tripInterface;
    public static final String TAG = "PEAM DEBUG";
    CustomResources customResources = new CustomResources();

    public MyTripAdapter(Context context, ArrayList<Trip> tripArrayList, TripInterface tripInterface) {
        this.context = context;
        this.tripArrayList = tripArrayList;
        this.tripInterface = tripInterface;
    }

    @NonNull
    @Override
    public TripHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.my_trips_list, parent, false);
        return new TripHolder(v, tripInterface);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TripHolder holder, int position) {
        String price = "Ksh " + customResources.numberFormat(tripArrayList.get(position).getPrice());

        holder.destinationText.setText(tripArrayList.get(position).getDestinationName());
        holder.dateText.setText(tripArrayList.get(position).getDate());
        holder.priceText.setText(price);
        holder.statusText.setText(tripArrayList.get(position).getStatus());
        holder.pickupText.setText(tripArrayList.get(position).getOriginName());
        holder.destinationMoreText.setText(tripArrayList.get(position).getDestinationName());
        holder.cabTypeText.setText(tripArrayList.get(position).getCabType());
        holder.distanceText.setText(tripArrayList.get(position).getDistance() + " Kms");
        holder.tripTimeText.setText(tripArrayList.get(position).getDuration());
        holder.driverText.setText(tripArrayList.get(position).getDriverName());
        holder.driverPhoneText.setText(tripArrayList.get(position).getDriverPhoneNumber());

        holder.titleNameText.setVisibility(View.GONE);


        //split and remove first index
        String[] split = tripArrayList.get(position).getDate().split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= (split.length - 1); i++) {
            sb.append(split[i]);
        }

        String header = sb.toString().replace(",", " ").trim();

        if (!previousHeader.equals(header)) {
            holder.titleNameText.setVisibility(View.VISIBLE);
            holder.titleNameText.setText(header);
        }
        previousHeader = header;
    }


    @Override
    public int getItemCount() {
        return tripArrayList.size();
    }


    /**
     *
     */
    public class TripHolder extends RecyclerView.ViewHolder {
        TextView tripTimeText, driverPhoneText, driverText, distanceText, cabTypeText, titleNameText, destinationText, destinationMoreText, dateText, priceText, statusText, pickupText;
        TableLayout collapsibleLayout;
        LinearLayout mainLayout;

        public TripHolder(@NonNull View itemView, TripInterface tripInterface) {
            super(itemView);

            mainLayout = itemView.findViewById(R.id.mtl_main_layout);
            collapsibleLayout = itemView.findViewById(R.id.collapsible_layout);

            destinationText = itemView.findViewById(R.id.mtl_destination);
            dateText = itemView.findViewById(R.id.mtl_date);
            priceText = itemView.findViewById(R.id.mtl_price);
            statusText = itemView.findViewById(R.id.mtl_status);
            titleNameText = itemView.findViewById(R.id.mtl_title_name);
            tripTimeText = itemView.findViewById(R.id.mtl_trip_time);

            pickupText = itemView.findViewById(R.id.mtl_pickup);
            destinationMoreText = itemView.findViewById(R.id.mtl_more_destination);
            cabTypeText = itemView.findViewById(R.id.mtl_cab_type);
            distanceText = itemView.findViewById(R.id.mtl_distance);
            driverText = itemView.findViewById(R.id.mtl_driver);
            driverPhoneText = itemView.findViewById(R.id.mtl_driver_phone);

            mainLayout.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (tripInterface != null) {
                    tripInterface.onTripClick(position, collapsibleLayout);
                }
            });
        }
    }
}
