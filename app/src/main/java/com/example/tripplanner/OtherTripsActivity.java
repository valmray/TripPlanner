package com.example.tripplanner;

import androidx.annotation.NonNull;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class OtherTripsActivity extends AppCompatActivity {
    public ListView lv_trips;
    // Access a Cloud Firestore instance from your Activity
    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    public String TAG = "demo";
    public ArrayList<Trip> trips = new ArrayList<Trip>();
    public FirebaseAuth mAuth;
    public String userId;
    boolean comp = false;
    ArrayList<User> people = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_other_trips);

        setTitle("Explore Trips");


        lv_trips = findViewById(R.id.lv_trips);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        userId = user.getUid();

        db.collection("trips").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {

                            Trip trip = new Trip(document.getData());
                            people.clear();

                            boolean onTrip = false;
                            Log.d("Size", "Size: " + people.size());
                            Log.d("People",  trip.people.toString());

                            for(int i = 0; i < trip.people.size(); i++)
                            {
                                User user = new User();
                                Map userMap = ((Map)trip.people.get(i));

                                String firstName = (String) userMap.get("firstName");
                                String lastName = (String) userMap.get("lastName");
                                String gender = (String) userMap.get("gender");
                                String storagePath = (String) userMap.get("storagePath");
                                String userId = (String) userMap.get("userId");
                                String email = (String) userMap.get("email");
                                String url = (String) userMap.get("url");

                                user.firstName = firstName;
                                user.lastName = lastName;
                                user.email = email;
                                user.storagePath = storagePath;
                                user.gender = gender;
                                user.url = url;
                                user.userId = userId;

                                people.add(user);

                            }

                            //Trying to make it so that any trip the user is a part of does not show up
                            for(int i = 0; i < people.size(); i++)
                            {
                                if(people.get(i).userId.equals(userId))
                                {
                                    onTrip = true;
                                }
                            }

                            if(onTrip == false)
                            {
                                trips.add(trip);
                            }
                    }

                    final TripAdapter ad = new TripAdapter(OtherTripsActivity.this,
                            android.R.layout.simple_list_item_1, trips);

                    // give adapter to ListView UI element to render
                    lv_trips.setAdapter(ad);

                    lv_trips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            Intent intentToViewTrip = new Intent(OtherTripsActivity.this, TripActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("selectedTrip", trips.get(i));
                            intentToViewTrip.putExtra("bundleData", bundle);
                            startActivity(intentToViewTrip);
                            finish();
                        }
                    });

                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

}
