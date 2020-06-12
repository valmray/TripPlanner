package com.example.tripplanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MyTripsActivity extends AppCompatActivity {
    public ListView lv_myTrips;
    // Access a Cloud Firestore instance from your Activity
    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    public FirebaseAuth mAuth;
    public String userId;
    public String TAG = "demo";
    public ArrayList<Trip> trips = new ArrayList<Trip>();
    public User userObj = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_trips);
        setTitle("My Trips");

        lv_myTrips = findViewById(R.id.lv_myTrips);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        userId = user.getUid();

        Log.d("Success?", "Success!");

        db.collection("users").document(userId).collection("trips").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Trip trip = new Trip(document.getData());
                        trips.add(trip);
                        Log.d("Trip Desc", trip.title);
                    }


                    final TripAdapter ad = new TripAdapter(MyTripsActivity.this,
                            android.R.layout.simple_list_item_1, trips);

                    // give adapter to ListView UI element to render
                    lv_myTrips.setAdapter(ad);

                    lv_myTrips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            Intent intentToViewTrip = new Intent(MyTripsActivity.this, ViewMyTripActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("selectedTrip", trips.get(i));
                            intentToViewTrip.putExtra("bundleData", bundle);
                            startActivityForResult(intentToViewTrip, 5);
                            finish();
                        }
                    });

                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 5) {
            if (resultCode == RESULT_OK) {
                trips.clear();
                Log.d(TAG, "onActivityResult: DELETE " );
                db.collection("trips").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.toObject(Trip.class).user_id.equals(userId)) {
                                    Trip trip = new Trip(document.getData());
                                    trips.add(trip);
                                }
                            }

                            final TripAdapter ad = new TripAdapter(MyTripsActivity.this,
                                    android.R.layout.simple_list_item_1, trips);

                            // give adapter to ListView UI element to render
                            lv_myTrips.setAdapter(ad);
                        }
                    }
                });
            }
        }
    }
}
