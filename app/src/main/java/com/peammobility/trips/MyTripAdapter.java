package com.peammobility.trips;

import android.content.Context;
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

    @Override
    public void onBindViewHolder(@NonNull TripHolder holder, int position) {
        String price = "Ksh " + customResources.numberFormat(tripArrayList.get(position).getPrice());

        holder.destinationText.setText(tripArrayList.get(position).getDestinationName());
        holder.dateText.setText(tripArrayList.get(position).getDate());
        holder.priceText.setText(price);
        holder.statusText.setText(tripArrayList.get(position).getStatus());

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
        TextView titleNameText, destinationText, dateText, priceText, statusText;
        TableLayout mainLayout;
        LinearLayout noTripsLayout;

        public TripHolder(@NonNull View itemView, TripInterface tripInterface) {
            super(itemView);

            mainLayout = itemView.findViewById(R.id.mtl_table_layout);
            destinationText = itemView.findViewById(R.id.mtl_destination);
            dateText = itemView.findViewById(R.id.mtl_date);
            priceText = itemView.findViewById(R.id.mtl_price);
            statusText = itemView.findViewById(R.id.mtl_status);
            titleNameText = itemView.findViewById(R.id.mtl_title_name);

            mainLayout.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (tripInterface != null) {
                    tripInterface.onTripClick(position);
                }
            });
        }
    }
}
