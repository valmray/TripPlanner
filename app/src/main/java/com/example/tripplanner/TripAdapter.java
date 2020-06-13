package com.example.tripplanner;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

public class TripAdapter extends ArrayAdapter<Trip> {

    public String TAG = "demo";
    public FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    public StorageReference storageReference = firebaseStorage.getReference();

    public TripAdapter(@NonNull Context context, int resource, @NonNull List<Trip> trips) {
        super(context, resource, trips);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Trip trip = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.trip_item, parent, false);
        }
        Log.d("Title", trip.title);
        TextView tv_title_trips = convertView.findViewById(R.id.tv_title_trips);
        tv_title_trips.setText(trip.title);

       /* TextView tv_description_trips = convertView.findViewById(R.id.tv_description_trips);
        tv_description_trips.setText(trip.description);*/

        TextView tv_date_trips = convertView.findViewById(R.id.tv_date_trips);
        tv_date_trips.setText(trip.date);

        TextView tv_location_trip = convertView.findViewById(R.id.tv_address_trips);
        tv_location_trip.setText(trip.city);

        ImageView iv_coverPhoto_trips = convertView.findViewById(R.id.iv_coverPhoto_trips);
        Log.d(TAG, "trip url: " + trip.url);
        Picasso.get().load(trip.url).into(iv_coverPhoto_trips);

        return convertView;
    }
}