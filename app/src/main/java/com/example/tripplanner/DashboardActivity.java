package com.example.tripplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

public class DashboardActivity extends AppCompatActivity {
    public Button btn_listOfUsers;
    public Button btn_addTrip;
    public Button btn_logout;
    public Button btn_viewTrips;
    public Button btn_editProfile;
    public Button btn_myTrips;
    public Bundle extrasFromMain;
    public String TAG = "demo";
    public FirebaseAuth mAuth;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        setTitle("Home Page");

        mAuth = FirebaseAuth.getInstance();

        SharedPreferences prefs = getSharedPreferences("info", MODE_PRIVATE);

        Gson gson = new Gson();
        user = gson.fromJson(prefs.getString("user", null), User.class);
        //extrasFromMain = getIntent().getExtras().getBundle("bundleData");

        final String userId = user.userId;

        btn_listOfUsers = findViewById(R.id.btn_ListOfUsers);
        btn_listOfUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentToUsers = new Intent(DashboardActivity.this, UsersActivity.class);
                startActivity(intentToUsers);
            }
        });

        btn_addTrip = findViewById(R.id.btn_addTrip);
        btn_addTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentToAddTrip = new Intent(DashboardActivity.this, AddTripActivity.class);
                startActivity(intentToAddTrip);
            }
        });

        btn_viewTrips = findViewById(R.id.btn_trips);
        btn_viewTrips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentToViewTrip = new Intent(DashboardActivity.this, OtherTripsActivity.class);
                startActivity(intentToViewTrip);
            }
        });

        btn_editProfile = findViewById(R.id.btn_editProfile);
        btn_editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentToEditProfile = new Intent(DashboardActivity.this, EditProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("userId", userId);
                intentToEditProfile.putExtra("bundleData", bundle);
                startActivity(intentToEditProfile);
            }
        });

        btn_myTrips = findViewById(R.id.btn_myTrips);
        btn_myTrips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentToViewTrip = new Intent(DashboardActivity.this, MyTripsActivity.class);
                startActivity(intentToViewTrip);
            }
        });

        btn_logout = findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = getSharedPreferences("info", MODE_PRIVATE);
                prefs.edit().putString("user", null).commit();


                mAuth.signOut();

                Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }
}
