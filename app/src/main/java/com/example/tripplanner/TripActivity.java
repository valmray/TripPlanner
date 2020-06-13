package com.example.tripplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TripActivity extends AppCompatActivity {
    // Access a Cloud Firestore instance from your Activity
    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    public ImageView iv_coverPhoto_singleTrip, btn_viewMap;
    public Bundle extrasFromViewTrips;
    public TextView tv_title_singleTrip;
    public TextView tv_description_singleTrip;
    public TextView tv_location_singleTrip;
    public TextView tv_date_singleTrip;
    public Button btn_joinTrip, back;
    public ArrayList<User> friends = new ArrayList<User>();
    public FirebaseAuth mAuth;
    public String userId;
    public User newUser = new User();
    public String TAG = "demo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);

        setTitle("Join Trip");

        extrasFromViewTrips = getIntent().getExtras().getBundle("bundleData");

        final Trip selectedTrip = (Trip) extrasFromViewTrips.getSerializable("selectedTrip");


        tv_title_singleTrip = findViewById(R.id.tv_title_singleTrip);
        tv_description_singleTrip = findViewById(R.id.tv_description_join);
        tv_location_singleTrip = findViewById(R.id.tv_location_join);
        tv_date_singleTrip = findViewById(R.id.tv_date_join);
        btn_joinTrip = findViewById(R.id.btn_join);
        iv_coverPhoto_singleTrip = findViewById(R.id.iv_coverPhoto_join);
        btn_viewMap = findViewById(R.id.btn_ViewOnMap);
        back = findViewById(R.id.viewTrip_btn_back);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        userId = user.getUid();

        db.collection("trips").document(selectedTrip.trip_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        tv_title_singleTrip.setText(selectedTrip.title);
                        tv_description_singleTrip.setText(selectedTrip.description);
                        tv_location_singleTrip.setText(selectedTrip.city);
                        tv_date_singleTrip.setText(selectedTrip.date);
                        Picasso.get().load(selectedTrip.url).into(iv_coverPhoto_singleTrip);

                    }
                });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TripActivity.this, OtherTripsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btn_viewMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TripActivity.this, MapsActivity.class);
                intent.putExtra("Request", "show");
                intent.putExtra("Trip", selectedTrip);
                intent.putExtra("OtherUser", selectedTrip.user_id);
                startActivity(intent);
            }
        });

        btn_joinTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                db.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot != null) {
                                newUser = documentSnapshot.toObject(User.class);

                                HashMap<String, String> userMap = new HashMap<>();
                                userMap.put("firstName", newUser.firstName);
                                userMap.put("lastName", newUser.lastName);
                                userMap.put("gender", newUser.gender);
                                userMap.put("url", newUser.url);
                                userMap.put("storagePath", newUser.storagePath);
                                userMap.put("email", newUser.email);
                                userMap.put("userId", newUser.userId);

                                ArrayList<HashMap<String, String>> people = new ArrayList<>();
                                for(int i = 0; i < selectedTrip.people.size(); i++)
                                {
                                    people.add((HashMap<String, String>) ((Map)selectedTrip.people.get(i)));
                                }

                                people.add(userMap);
                                selectedTrip.people.add(newUser);
                                Log.d("Updated People", selectedTrip.people.toString());
                                Log.d("Updated People Hash", people.toString());

                                db.collection("trips").document(selectedTrip.trip_id).collection("people").document(newUser.userId).set(newUser);
                                db.collection("trips").document(selectedTrip.trip_id).set(selectedTrip);

                                for(int j = 0; j < people.size(); j++) {
                                    Log.d("User", ((Map)people.get(j)).toString());
                                    //User userMap = ((User)selectedTrip.people.get(j));
                                    String userId = (String)((Map)people.get(j)).get("userId");

                                    Log.d("User IDs", "ID: " + userId);

                                    db.collection("users").document(userId).collection("trips").document(selectedTrip.trip_id).collection("people").document(newUser.userId).set(newUser);
                                    db.collection("users").document(userId).collection("trips").document(selectedTrip.trip_id).set(selectedTrip);
                                }

                                Log.d(TAG, "Selected User: " + newUser.toString());
                            }

                            finish();
                        }

                    }
                });


            }
        });
    }
}
