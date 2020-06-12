package com.example.tripplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class ViewMyTripActivity extends AppCompatActivity {
    public TextView tv_title_view_myTrip;
    public TextView tv_description_view_myTrip;
    public TextView tv_date_view_myTrip;
    public TextView tv_location_view_myTrip;
    public ImageView iv_deleteTrip;
    public ImageView iv_chatTrip;
    public ImageView iv_coverPhoto_view_myTrip, view_map, edit_trip;
    public Button leave_trip;
    public Bundle extrasFromMyTrips;
    // Access a Cloud Firestore instance from your Activity
    public FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_my_trip);

        setTitle("My Trip");

        tv_title_view_myTrip = findViewById(R.id.tv_title_view_myTrip);
        tv_description_view_myTrip = findViewById(R.id.tv_description_view_myTrip);
        iv_deleteTrip = findViewById(R.id.iv_delete_myTrip);
        iv_chatTrip = findViewById(R.id.iv_chat_myTrip);
        iv_coverPhoto_view_myTrip = findViewById(R.id.imageView_view_myTrip);
        tv_date_view_myTrip = findViewById(R.id.tv_date_view_myTrip);
        tv_location_view_myTrip = findViewById(R.id.tv_location_view_myTrip);
        view_map = findViewById(R.id.btn_viewOnMap);
        leave_trip = findViewById(R.id.btn_LeaveTrip);
        edit_trip = findViewById(R.id.btn_editTrip);

        extrasFromMyTrips = getIntent().getExtras().getBundle("bundleData");

        final Trip selectedTrip = (Trip) extrasFromMyTrips.getSerializable("selectedTrip");


        tv_title_view_myTrip.setText(selectedTrip.title);
        tv_description_view_myTrip.setText(selectedTrip.description);
        tv_date_view_myTrip.setText(selectedTrip.date);
        tv_location_view_myTrip.setText(selectedTrip.city);

        Picasso.get().load(selectedTrip.url).into(iv_coverPhoto_view_myTrip);

        SharedPreferences prefs = getSharedPreferences("info", MODE_PRIVATE);
        Gson gson = new Gson();
        final User user = gson.fromJson(prefs.getString("user", null), User.class);
        final String userId = user.userId;

        iv_deleteTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                db.collection("trips").document(selectedTrip.trip_id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        setResult(ViewMyTripActivity.RESULT_OK);
                        finish();
                    }
                });
            }
        });

        Log.d("Trip User ID", selectedTrip.user_id);
        if(userId.equals(selectedTrip.user_id))
        {
            Log.d("Trip User ID", selectedTrip.user_id);

            leave_trip.setVisibility(View.GONE);
        }
        else
        {
            iv_deleteTrip.setVisibility(View.GONE);
            edit_trip.setVisibility(View.GONE);
        }

        edit_trip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editTripIntent = new Intent(ViewMyTripActivity.this, EditTripActivity.class);
                editTripIntent.putExtra("Trip", selectedTrip);
                startActivity(editTripIntent);
                finish();
            }
        });

        leave_trip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("users").document(userId).collection("trips").document(selectedTrip.trip_id).delete();

                for(int i = 0; i < selectedTrip.people.size(); i++)
                {
                    String id = (String)((Map)selectedTrip.people.get(i)).get("userId");
                    if(id.equals(userId))
                    {
                        selectedTrip.people.remove(i);
                        for(int k = 0; k < selectedTrip.people.size(); k++)
                        {
                            String id2 = (String)((Map)selectedTrip.people.get(k)).get("userId");
                            db.collection("users").document(id2).collection("trips").document(selectedTrip.trip_id).collection("people").document(userId).delete();
                        }
                    }
                }

                db.collection("trips").document(selectedTrip.trip_id).collection("people").document(userId).delete();

                db.collection("trips").document(selectedTrip.trip_id).set(selectedTrip);

                for(int j = 0; j < selectedTrip.people.size(); j++) {
                    String userId = (String)((Map)selectedTrip.people.get(j)).get("userId");

                    Log.d("User IDs", "ID: " + userId);

                    db.collection("users").document(userId).collection("trips").document(selectedTrip.trip_id).set(selectedTrip);
                }
            }
        });

        iv_chatTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentToChat = new Intent(ViewMyTripActivity.this, ChatRoomActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("selectedTrip", selectedTrip);
                intentToChat.putExtra("bundleData", bundle);
                startActivity(intentToChat);
            }
        });

        view_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewMyTripActivity.this, MapsActivity.class);
                intent.putExtra("Request", "show");
                intent.putExtra("Trip", selectedTrip);
                startActivity(intent);
            }
        });
    }
}
