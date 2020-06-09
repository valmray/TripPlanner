package com.example.tripplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.firebase.firestore.EventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ArrayList<LatLng> locationList = new ArrayList<LatLng>();
    Polyline polyline1;

    User user;
    private FirebaseFirestore mDatabase;
    ArrayList<PlanPlace> planPlaceArrayList = new ArrayList<>();
    Trip trip = null;

    LocationManager locationManager;
    LocationListener locationListener;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,

    };

    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

    int PLACE_PICKER_REQUEST = 1;

    LatLng chosenPlace = null;

    String getRequest;
    int position = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        mDatabase = FirebaseFirestore.getInstance();

        getRequest = getIntent().getStringExtra("Request");

        if(getRequest.equals("show"))
        {
            trip = (Trip)getIntent().getSerializableExtra("Trip");

            //Unable to get Support Action Bar

           /* ActionBar actionBar = ((AppCompatActivity)(MapsActivity.this)).getSupportActionBar();
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayUseLogoEnabled(true);

            ViewGroup viewGroup = (ViewGroup) findViewById(android.R.id.content);

            View view = (View) LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.action_bar_view, viewGroup, false);

            TextView textView = view.findViewById(R.id.textViewMap);
            textView.setText(trip.title);



            view.findViewById(R.id.backToTripsButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MapsActivity.this, DashboardActivity.class);
                    startActivity(intent);
                    finish();
                }
            });

            actionBar.setCustomView(view);*/
        }


        //Have user be able to see the location of a specific place through trip details
        if(getRequest.equals("pick")) {
            try {
                startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        }
        else
        {
            setMap();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Create a new task")
                    .setTitle("GPS Not Enabled")
                    .setMessage("Would you like to enable the GPS settings?")
                    .setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(intent);

                                }
                            })
                    .setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    finish();
                                }
                            })
                    .setCancelable(true)
                    .create()
                    .show();
        } else {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.d("Location Details--> ", location.getLatitude() + ", " + location.getLongitude());

                    setMap();
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Activity activity = MapsActivity.this;

                int permission1 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
                int permission2 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION);

                if (permission1 != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            activity,
                            PERMISSIONS_STORAGE,
                            REQUEST_EXTERNAL_STORAGE
                    );
                }
                if (permission2 != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            activity,
                            PERMISSIONS_STORAGE,
                            REQUEST_EXTERNAL_STORAGE
                    );
                }
            }
            else
            {
                Log.d("Access", "Full Access");
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener);
            }
        }
    }

    public void setMap()
    {
        SharedPreferences prefs = getSharedPreferences("info", MODE_PRIVATE);
        Gson gson = new Gson();

        user = gson.fromJson(prefs.getString("user", null), User.class);
        trip = (Trip)getIntent().getSerializableExtra("Trip");
        String otherUser = getIntent().getStringExtra("OtherUser");
        String id;

        if(otherUser != null)
        {
            id = otherUser;
        }
        else
        {
            id = user.userId;
        }

        if(trip != null)
        {
            mDatabase.collection("users").document(id).collection("trips").document(trip.trip_id).collection("plan").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            //Log.d("In trip Details--> ", trip.trip_id);

                            TripPlan tripPlan = new TripPlan(document.getData());

                            //Log.d("PlanplaceD", tripPlan.planDescription);
                            Log.d("Planplace", tripPlan.visitOrder.toString());

                            locationList.clear();
                            planPlaceArrayList.clear();
                            position = 1;

                            for(int i = 0; i < tripPlan.visitOrder.size(); i++)
                            {
                                //Log.d("Position", "Position: " + position);
                                Map visitOrder = (Map)tripPlan.visitOrder.get(String.valueOf(position));

                                Log.d("PlanplaceM", visitOrder.toString());
                                Map place =  (Map)visitOrder.get("place");

                                Double latitudeMap = (Double)place.get("latitude");
                                Double longitudeMap = (Double)place.get("longitude");
                                String nameMap = (String)place.get("name");
                                String idMap = (String)place.get("id");
                                String restaurantMap = (String)place.get("restaurant");

                                com.example.tripplanner.Place place1 = new com.example.tripplanner.Place(nameMap, restaurantMap, idMap, latitudeMap, longitudeMap);

                                String position1 = (String)visitOrder.get("position");

                                PlanPlace planPlace = new PlanPlace(position1, place1);

                                //Log.d("PlanplacePP", place.toString());
                                //Log.d("PlanplacePos", position1.toString());

                                planPlaceArrayList.add(planPlace);
                                position++;
                            }

                            position = 1;
                        }

                        for(int j = 0; j < planPlaceArrayList.size(); j++)
                        {
                            LatLng latLng = new LatLng(planPlaceArrayList.get(j).place.latitude, planPlaceArrayList.get(j).place.longitude);
                            locationList.add(latLng);
                            Log.d("LatLng", locationList.get(j).toString());

                        }

                        for(int i = 0; i < locationList.size(); i++)
                        {
                            //Log.d("LatLng", locationList.get(i).toString());
                            mMap.addMarker(new MarkerOptions()
                                    .position(locationList.get(i))
                                    .title(planPlaceArrayList.get(i).position + ". " + planPlaceArrayList.get(i).place.name));
                        }

                        for(int i = 0; i < locationList.size(); i++)
                        {
                            int num = i+1;

                            if(num < locationList.size())
                            {
                                mMap.addPolyline(new PolylineOptions()
                                        .clickable(true)
                                        .add(locationList.get(i), locationList.get(num))
                                        .color(Color.BLUE));

                                Log.d("Points", String.valueOf(i));
                            }
                            else
                            {
                                if(locationList.size() == 1)
                                {
                                    mMap.addPolyline(new PolylineOptions()
                                            .clickable(true)
                                            .add(locationList.get(0), locationList.get(0))
                                            .color(Color.BLUE));
                                }
                                else
                                {
                                    mMap.addPolyline(new PolylineOptions()
                                            .clickable(true)
                                            .add(locationList.get(0), locationList.get(1))
                                            .color(Color.BLUE));
                                }

                            }

                        }

                        for(int i = 0; i < locationList.size(); i++) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(locationList.get(i)));
                        }

                        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                            @Override
                            public void onMapLoaded() {
                                LatLngBounds.Builder b = new LatLngBounds.Builder();
                                b.include(new LatLng(trip.latitude, trip.longitude));
                                for (LatLng a : locationList) {
                                    b.include(a);
                                }
                                LatLngBounds bounds = b.build();
                                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,10);
                                mMap.animateCamera(cu);
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MapsActivity.this, "Failed to get trip details", Toast.LENGTH_LONG).show();
                }
            });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                com.example.tripplanner.Place place1 = new com.example.tripplanner.Place();
                chosenPlace = place.getLatLng();

                place1.name = String.format("%s", place.getName());
                place1.latitude = chosenPlace.latitude;
                place1.longitude = chosenPlace.longitude;
                place1.type = "city";
                place1.id = place.getId();

                Intent intent = new Intent();
                intent.putExtra(AddTripActivity.VALUE_KEY, place1);
                setResult(RESULT_OK, intent);

                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                Log.d("test", "Location: " + place.getName() + " (" + chosenPlace + ")");

                finish();
            }
        }
    }
}
