package com.example.tripplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class AddTripActivity extends AppCompatActivity implements MyInterface {
    public Button btn_AddFriends, btn_createPlan, cancel;
    public ArrayList<User> users = new ArrayList<User>();
    public String TAG = "demo";
    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    public FirebaseMessaging fm;
    public ArrayList<User> selectedUsers = new ArrayList<>();
    protected CharSequence[] _users;
    public EditText et_title;
    public EditText et_description;
    public Button btn_submit;
    public EditText et_date;
    public ImageView iv_coverPhoto;
    public FirebaseAuth mAuth;
    public String userId;
    public String tripId;
    public int REQ_CODE = 5;
    public String selectedCity;
    public double latitude;
    public double longitude;
    public FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    public StorageReference storageReference = firebaseStorage.getReference();
    public String coverPhotoUrl;
    public int selectedCoverPhoto;
    public Bitmap bMap;
    public boolean isCoverPhotoUpload = false;
    public Button findCity;
    static final int REQ_CODE_SELECT = 1234;
    static final int REQ_CODE_PLAN = 3456;

    static final String VALUE_KEY = "value";
    static final String VALUE_KEY_PLAN = "plan";
    static final String VALUE_KEY_PLACES = "places";
    static final String VALUE_KEY_PLAN_DESCRIPTION = "description";
    String description = "";

    LatLng cityLoc;
    TextView chosenCity;
    Place chosenPlace;

    ArrayList<Place> allPlacesList = new ArrayList<>();
    ArrayList<Place> userPlacesList = new ArrayList<>();
    ArrayList<PlanPlace> planPlaceArrayList = new ArrayList<>();

    ArrayList<String> allPlacesListS = new ArrayList<>();
    ArrayList<String> userPlacesListS = new ArrayList<>();
    ArrayList<String> planArrayListS = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);

        setTitle("Add Trip");

        chosenCity = findViewById(R.id.cityTextAdd);
        btn_AddFriends = findViewById(R.id.btn_addFriends);
        btn_createPlan = findViewById(R.id.btn_createPlan);
        findCity = findViewById(R.id.findCityAdd);
        cancel = findViewById(R.id.addTrip_btn_cancel);

        SharedPreferences prefs = getSharedPreferences("info", MODE_PRIVATE);

        Gson gson = new Gson();
        final User user = gson.fromJson(prefs.getString("user", null), User.class);
        final String userId = user.userId;


        getListOfFriends(userId);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btn_AddFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean[] checkedUsers = new boolean[users.size()];
                int count = users.size();

                for (int i = 0; i < count; i++) {
                    checkedUsers[i] = selectedUsers.contains(_users[i]);
                }

                DialogInterface.OnMultiChoiceClickListener receiversDialogListener = new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            Log.d(TAG, "Added User " + users.get(which));
                            selectedUsers.add(users.get(which));
                        } else {
                            Log.d(TAG, "Removed User " + users.get(which));
                            selectedUsers.remove(users.get(which));
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(AddTripActivity.this);
                builder
                        .setTitle("Select Friends")
                        .setMultiChoiceItems(_users, checkedUsers, receiversDialogListener)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();

                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        et_title = findViewById(R.id.et_title);
        et_description = findViewById(R.id.et_descrption);
        btn_submit = findViewById(R.id.btn_submit);
        et_date = findViewById(R.id.et_date);
        iv_coverPhoto = findViewById(R.id.iv_coverPhoto);


        iv_coverPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentToCoverPhoto = new Intent(AddTripActivity.this, CoverPhotoLibrary.class);
                startActivityForResult(intentToCoverPhoto, REQ_CODE);
            }
        });



        tripId = UUID.randomUUID().toString();

        findCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddTripActivity.this, MapsActivity.class);
                intent.putExtra("Request", "pick");
                startActivityForResult(intent, REQ_CODE_SELECT);
            }
        });

        btn_createPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createPlanIntent = new Intent(AddTripActivity.this, CreateTripPlanActivity.class);
                createPlanIntent.putExtra("Places", allPlacesList);
                startActivityForResult(createPlanIntent, REQ_CODE_PLAN);
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dateValue = et_date.getText().toString();
                boolean isErrorThrown = false;
                boolean incorrectDate = false;
                try {
                    new SimpleDateFormat("MM/dd/yyyy").parse(dateValue);
                    incorrectDate = false;
                } catch (ParseException e) {
                    incorrectDate = true;
                }

                Log.d(TAG, "coverPhotoUrl: " + coverPhotoUrl);
                Log.d(TAG, "et_date: " + et_date.getText());
                if(et_title.getText().toString().equals("")){
                    et_title.setError("Please enter a valid title!");
                    isErrorThrown = true;
                }else if(et_description.getText().toString().equals("")) {
                    et_description.setError("Please enter a valid description!");
                    isErrorThrown = true;
                }else if(chosenCity.getText().toString().equals("Choose A Destination City")) {
                    isErrorThrown = true;
                    Toast.makeText(AddTripActivity.this, "Please select a valid city" +
                            " for you trip!", Toast.LENGTH_SHORT).show();
                }else if(et_date.getText().toString().equals("") || incorrectDate) {
                    isErrorThrown = true;
                    et_date.setError("Please enter a valid date MM/DD/YYYY");
                } else if(!isCoverPhotoUpload) {
                    Toast.makeText(AddTripActivity.this, "Please select a cover photo.",
                            Toast.LENGTH_SHORT).show();
                }
                //{

                selectedUsers.add(user);

                if(isCoverPhotoUpload && !isErrorThrown) {
                    Trip trip = new Trip(
                            userId,
                            tripId,
                            et_title.getText().toString(),
                            et_description.getText().toString(),
                            selectedUsers,
                            dateValue,
                            chosenPlace.name,
                            chosenPlace.latitude,
                            chosenPlace.longitude,
                            coverPhotoUrl,
                            new ArrayList<Message>());

                    db.collection("trips").document(tripId).set(trip);

                    for(int j = 0; j < selectedUsers.size(); j++) {
                        db.collection("users").document(selectedUsers.get(j).userId).collection("trips").document(tripId).set(trip);
                    }

                    for(int i = 0; i < userPlacesList.size(); i++)
                    {
                        for(int j = 0; j < selectedUsers.size(); j++) {
                            db.collection("users").document(selectedUsers.get(j).userId).collection("trips").document(tripId)
                                    .collection("places").document(userPlacesList.get(i).name).set(userPlacesList.get(i));
                        }

                        db.collection("trips").document(tripId)
                                .collection("places").document(userPlacesList.get(i).name).set(userPlacesList.get(i));
                    }

                    TripPlan tripPlan = new TripPlan();
                    tripPlan.planDescription = description;

                    HashMap<String, PlanPlace> plan = new HashMap<>();
                    for(int j = 0; j < planPlaceArrayList.size(); j++)
                    {
                        plan.put(String.valueOf(planPlaceArrayList.get(j).position), planPlaceArrayList.get(j));
                    }

                    tripPlan.visitOrder = plan;



                    for(int j = 0; j < selectedUsers.size(); j++) {
                        db.collection("users").document(selectedUsers.get(j).userId).collection("trips")
                                .document(tripId).collection("plan").document("tripPlan").set(tripPlan);
                    }


                    db.collection("trips")
                            .document(tripId).collection("plan").document("tripPlan").set(tripPlan);

                    for(int j = 0; j < userPlacesList.size(); j++)
                    {
                        db.collection("trips")
                                .document(tripId).collection("plan").document("tripPlan").collection("visitOrder")
                                .document(String.valueOf(planPlaceArrayList.get(j).position))
                                .set(planPlaceArrayList.get(j));

                        for(int i = 0; i < selectedUsers.size(); i++) {
                            db.collection("users").document(selectedUsers.get(i).userId).collection("trips")
                                    .document(tripId).collection("plan").document("tripPlan").collection("visitOrder")
                                    .document(String.valueOf(planPlaceArrayList.get(j).position))
                                    .set(planPlaceArrayList.get(j));
                        }

                    }

                    for(int j = 0; j < selectedUsers.size(); j++)
                    {
                        db.collection("trips")
                                .document(tripId).collection("people").document(selectedUsers.get(j).userId).set(selectedUsers.get(j));

                        for(int k = 0; k < selectedUsers.size(); k++)
                        {
                            db.collection("users").document(selectedUsers.get(j).userId).collection("trips")
                                    .document(tripId).collection("people").document(selectedUsers.get(k).userId).set(selectedUsers.get(k));
                        }

                    }


                    Intent intent = new Intent(AddTripActivity.this, MapsActivity.class);
                    intent.putExtra("Request", "show");
                    intent.putExtra("Trip", trip);
                    startActivity(intent);
                    finish();
                    //  }
                }
            }

        });

    }

    public void getListOfFriends(String userId) {
        db.collection("users").document(userId).collection("friends").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        User user = new User(document.getData());
                        users.add(user);
                        Log.d(TAG, document.getId() + " => " + document.getData());
                    }

                    _users = new CharSequence[users.size()];
                    for (int i = 0; i < users.size(); i++) {
                        _users[i] = users.get(i).firstName + " " + users.get(i).lastName;
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: " + data);

        if (requestCode == REQ_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                if (data.getExtras().getSerializable("coverPhoto").equals("alaska")) {
                    selectedCoverPhoto = R.drawable.alaska;
                    iv_coverPhoto.setImageResource(R.drawable.alaska);
                } else if (data.getExtras().getSerializable("coverPhoto").equals("borabora")) {
                    selectedCoverPhoto = R.drawable.borabora;
                    iv_coverPhoto.setImageResource(R.drawable.borabora);
                } else if (data.getExtras().getSerializable("coverPhoto").equals("cappadocia")) {
                    selectedCoverPhoto = R.drawable.cappadocia;
                    iv_coverPhoto.setImageResource(R.drawable.cappadocia);
                } else if (data.getExtras().getSerializable("coverPhoto").equals("cavin")) {
                    selectedCoverPhoto = R.drawable.cavin;
                    iv_coverPhoto.setImageResource(R.drawable.cavin);
                } else if (data.getExtras().getSerializable("coverPhoto").equals("colombia")) {
                    selectedCoverPhoto = R.drawable.colombia;
                    iv_coverPhoto.setImageResource(R.drawable.colombia);
                } else if (data.getExtras().getSerializable("coverPhoto").equals("grandCanyon")) {
                    selectedCoverPhoto = R.drawable.grandcanyonofthecoloradoar;
                    iv_coverPhoto.setImageResource(R.drawable.grandcanyonofthecoloradoar);
                } else if (data.getExtras().getSerializable("coverPhoto").equals("snowboard")) {
                    selectedCoverPhoto = R.drawable.snowboard;
                    iv_coverPhoto.setImageResource(R.drawable.snowboard);
                }

                bMap = BitmapFactory.decodeResource(getResources(), selectedCoverPhoto);
                uploadImage(bMap);
            }
        }

        if(requestCode == REQ_CODE_SELECT)
        {
            //String types = "airport|amusement_park|aquarium|car_rental|city_hall|museum|police|parking";
            if (resultCode == RESULT_OK) {
                Place city = (Place)data.getSerializableExtra(VALUE_KEY);
                chosenPlace = city;
                chosenCity.setText(city.name);
                cityLoc = new LatLng(city.latitude, city.longitude);

                Log.d("AddTripActivity Result", "Add Trip Activity Result");

                //String[] typesCompare = {"restaurant", "airport", "amusement_park", "aquarium", "car_rental", "city_hall", "museum", "police", "parking"};
                String link = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + city.latitude + "," + city.longitude + "&radius=1609.34&" +
                        "type=restaurant&key=" + BuildConfig.GMAPS_CONSUMER_SECRET;


                new GetPlacesAsync(AddTripActivity.this, db, AddTripActivity.this).execute(link);

            }

        }

        if(requestCode == REQ_CODE_PLAN)
        {
            //String types = "airport|amusement_park|aquarium|car_rental|city_hall|museum|police|parking";
            if (resultCode == RESULT_OK) {

                planPlaceArrayList = (ArrayList<PlanPlace>) data.getSerializableExtra(VALUE_KEY_PLAN);
                userPlacesList = (ArrayList<Place>) data.getSerializableExtra(VALUE_KEY_PLACES);
                description = data.getStringExtra(VALUE_KEY_PLAN_DESCRIPTION);
            }
        }

    }


    //UPLOAD IMAGE TO CLOUD
    private void uploadImage( Bitmap photoBitmap) {

        final StorageReference avatarRepo = storageReference.child("coverPhotos/" + tripId +".png");

//        Converting the Bitmap into a bytearrayOutputstream....
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photoBitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = avatarRepo.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: " + e.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "onSuccess: " + "Image Uploaded!!!");
            }
        });


        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
//                return null;
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                return avatarRepo.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "task.getResult().toString(): " + task.getResult().toString());
                    coverPhotoUrl = task.getResult().toString();
                    isCoverPhotoUpload = true;
                }else{
                    Toast.makeText(AddTripActivity.this, "Please upload a cover photo!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void addAllPlaces(ArrayList<Place> places) {
        for(int i = 0; i < places.size(); i++)
        {
            boolean valid = false;

            for(int j = 0; j < allPlacesListS.size(); j++)
            {
                if(allPlacesListS.get(j).equals(places.get(i).name))
                {
                    valid = true;
                }
            }

            if(valid == false)
            {
                allPlacesList.add(places.get(i));
                allPlacesListS.add(places.get(i).name);
            }

        }
    }

    @Override
    public void deleteTrip(Trip trip) {

    }
}
