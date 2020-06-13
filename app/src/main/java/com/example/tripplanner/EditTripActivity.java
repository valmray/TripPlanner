package com.example.tripplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditTripActivity extends AppCompatActivity implements MyInterface{
    Trip selectedTrip;
    Button changeLoc, save, addRemove, editPlan, cancel;
    EditText title, descriptionEdit, date;
    TextView cityText;
    ImageView tripImage;
    public int EDIT_REQ_CODE = 5;

    public ArrayList<User> users = new ArrayList<User>();
    public String TAG = "demo";
    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    protected CharSequence[] _users;

    public FirebaseAuth mAuth;
    public String userId;
    public int REQ_CODE = 5;

    public FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    public StorageReference storageReference = firebaseStorage.getReference();
    public String coverPhotoUrl;
    public int selectedCoverPhoto;
    public Bitmap bMap;
    public boolean isCoverPhotoUpload = false;

    static final int REQ_CODE_SELECT = 1234;
    static final int REQ_CODE_PLAN = 3456;

    static final String VALUE_KEY = "value";
    static final String VALUE_KEY_PLAN = "plan";
    static final String VALUE_KEY_PLACES = "places";
    static final String VALUE_KEY_PLAN_DESCRIPTION = "description";
    String description = "";

    LatLng cityLoc;
    Place chosenPlace;
    boolean changePhoto = false;

    ArrayList<Place> allPlacesList = new ArrayList<>();
    ArrayList<Place> userPlacesList = new ArrayList<>();
    ArrayList<PlanPlace> planPlaceArrayList = new ArrayList<>();
    ArrayList<User> newParticipants = new ArrayList<>();
    ArrayList<User> origParticipants = new ArrayList<>();

    ArrayList<String> allPlacesListS = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_trip);

        selectedTrip = (Trip)getIntent().getSerializableExtra("Trip");

        SharedPreferences prefs = getSharedPreferences("info", MODE_PRIVATE);

        Gson gson = new Gson();
        final User user = gson.fromJson(prefs.getString("user", null), User.class);
        final String userId = user.userId;

        getListOfFriends(userId);

        getCurrentPlan();

        changeLoc = findViewById(R.id.btn_changeLocation);
        save = findViewById(R.id.edit_saveChanges);
        addRemove = findViewById(R.id.btn_editParticipants);
        editPlan = findViewById(R.id.btn_editTripPlan);
        cancel = findViewById(R.id.edit_btn_cancel);

        title = findViewById(R.id.edit_tripTitle);
        descriptionEdit = findViewById(R.id.edit_tripDescription);
        date = findViewById(R.id.edit_tripDate);

        cityText = findViewById(R.id.edit_cityText);

        tripImage = findViewById(R.id.edit_tripImage);

        title.setText(selectedTrip.title);
        cityText.setText(selectedTrip.city);
        descriptionEdit.setText(selectedTrip.description);
        date.setText(selectedTrip.date);

        Log.d("Current Participants", selectedTrip.people.toString());

        Picasso.get().load(selectedTrip.url).into(tripImage);


        String link = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + selectedTrip.latitude + "," + selectedTrip.longitude + "&radius=1609.34&" +
                "type=restaurant&key=" + BuildConfig.GMAPS_CONSUMER_SECRET;

        new GetPlacesAsync(EditTripActivity.this, db, EditTripActivity.this).execute(link);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditTripActivity.this, ViewMyTripActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("selectedTrip", selectedTrip);
                intent.putExtra("bundleData", bundle);
                startActivity(intent);
                finish();
            }
        });

        tripImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentToCoverPhoto = new Intent(EditTripActivity.this, CoverPhotoLibrary.class);
                startActivityForResult(intentToCoverPhoto, EDIT_REQ_CODE);
            }
        });

        addRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean[] checkedUsers = new boolean[users.size()];
                int count = users.size();

                for (int i = 0; i < count; i++) {
                    boolean participant = false;

                    for (int j = 0; j < newParticipants.size(); j++) {
                        if(newParticipants.get(j).userId.equals(users.get(i).userId))
                        {
                            participant = true;
                        }
                    }

                    checkedUsers[i] = participant;
                }

                DialogInterface.OnMultiChoiceClickListener receiversDialogListener = new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            Log.d(TAG, "Added User " + users.get(which));
                            newParticipants.add(users.get(which));
                        } else {
                            Log.d(TAG, "Removed User " + users.get(which));
                            for (int j = 0; j < newParticipants.size(); j++) {
                                if(newParticipants.get(j).userId.equals(users.get(which).userId))
                                {
                                    newParticipants.remove(j);
                                }
                            }
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(EditTripActivity.this);
                builder
                        .setTitle("Add/Remove Participants")
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

        changeLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditTripActivity.this, MapsActivity.class);
                intent.putExtra("Request", "pick");
                startActivityForResult(intent, REQ_CODE_SELECT);
            }
        });

        editPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createPlanIntent = new Intent(EditTripActivity.this, EditTripPlanActivity.class);

                if(chosenPlace != null)
                {
                    if(selectedTrip.latitude == chosenPlace.latitude && selectedTrip.longitude == chosenPlace.longitude)
                    {
                        createPlanIntent.putExtra("Trip", selectedTrip);
                    }
                }
                else
                {
                    createPlanIntent.putExtra("Trip", selectedTrip);
                }

                createPlanIntent.putExtra("Places", allPlacesList);
                startActivityForResult(createPlanIntent, REQ_CODE_PLAN);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title_text = title.getText().toString();
                String date_text = date.getText().toString();
                String description_text = descriptionEdit.getText().toString();

                String photoUrl = selectedTrip.url;

                String city_text = cityText.getText().toString();
                double lat = selectedTrip.latitude;
                double lon = selectedTrip.longitude;

                if(chosenPlace != null)
                {
                    city_text = chosenPlace.name;
                    lat = chosenPlace.latitude;
                    lon = chosenPlace.longitude;
                }

                if(changePhoto == true)
                {
                    photoUrl = coverPhotoUrl;
                }

                newParticipants.add(user);
                origParticipants.add(user);

                Trip trip = new Trip(
                        selectedTrip.user_id,
                        selectedTrip.trip_id,
                        title_text,
                        description_text,
                        newParticipants,
                        date_text,
                        city_text,
                        lat,
                        lon,
                        photoUrl,
                        new ArrayList<Message>());

                addRemoveParticipants(trip);

                setPlaces(trip);

                setNewPlan(trip);


                //The people array has a Map object and a User object which causes an error
               /* Intent intent = new Intent(EditTripActivity.this, ViewMyTripActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("selectedTrip", trip);
                intent.putExtra("bundleData", bundle);
                startActivity(intent);*/

                Intent intent = new Intent(EditTripActivity.this, MapsActivity.class);
                intent.putExtra("Request", "show");
                intent.putExtra("Trip", trip);
                startActivity(intent);
                finish();

            }
        });

    }

    public void setNewPlan(Trip trip){
        TripPlan tripPlan = new TripPlan();
        tripPlan.planDescription = description;

        HashMap<String, PlanPlace> plan = new HashMap<>();

        for(int j = 0; j < planPlaceArrayList.size(); j++)
        {
            plan.put(String.valueOf(planPlaceArrayList.get(j).position), planPlaceArrayList.get(j));
        }

        tripPlan.visitOrder = plan;


        db.collection("users").document(trip.user_id).collection("trips")
                .document(trip.trip_id).collection("plan").document("tripPlan").set(tripPlan);

        for(int j = 0; j < newParticipants.size(); j++) {
            db.collection("users").document(newParticipants.get(j).userId).collection("trips")
                    .document(trip.trip_id).collection("plan").document("tripPlan").set(tripPlan);
        }


        db.collection("trips")
                .document(trip.trip_id).collection("plan").document("tripPlan").set(tripPlan);

        for(int j = 0; j < userPlacesList.size(); j++)
        {
            db.collection("trips")
                    .document(trip.trip_id).collection("plan").document("tripPlan").collection("visitOrder")
                    .document(String.valueOf(planPlaceArrayList.get(j).position))
                    .set(planPlaceArrayList.get(j));

            db.collection("users").document(trip.user_id).collection("trips")
                    .document(trip.trip_id).collection("plan").document("tripPlan").collection("visitOrder")
                    .document(String.valueOf(planPlaceArrayList.get(j).position))
                    .set(planPlaceArrayList.get(j));

            for(int i = 0; i < newParticipants.size(); i++) {
                db.collection("users").document(newParticipants.get(i).userId).collection("trips")
                        .document(trip.trip_id).collection("plan").document("tripPlan").collection("visitOrder")
                        .document(String.valueOf(planPlaceArrayList.get(j).position))
                        .set(planPlaceArrayList.get(j));
            }

        }
    }

    public void setPlaces(Trip trip){
        for(int i = 0; i < userPlacesList.size(); i++)
        {
            db.collection("users").document(trip.user_id).collection("trips").document(trip.trip_id)
                    .collection("places").document(userPlacesList.get(i).name).set(userPlacesList.get(i));

            for(int j = 0; j < newParticipants.size(); j++) {
                db.collection("users").document(newParticipants.get(j).userId).collection("trips").document(trip.trip_id)
                        .collection("places").document(userPlacesList.get(i).name).set(userPlacesList.get(i));
            }

            db.collection("trips").document(trip.trip_id)
                    .collection("places").document(userPlacesList.get(i).name).set(userPlacesList.get(i));
        }
    }

    public void addRemoveParticipants(Trip trip)
    {
        ArrayList<String> removeParticipants = new ArrayList<>();

        db.collection("trips").document(trip.trip_id).set(trip);

        db.collection("users").document(trip.user_id).collection("trips").document(trip.trip_id).set(trip);

        for(int i = 0; i < newParticipants.size(); i++)
        {
            for(int j = 0; j < newParticipants.size(); j++)
            {
                db.collection("users").document(newParticipants.get(i).userId).collection("trips").document(trip.trip_id).collection("people").document(newParticipants.get(j).userId).set(newParticipants.get(j));
            }

            db.collection("users").document(newParticipants.get(i).userId).collection("trips").document(trip.trip_id).set(trip);
            db.collection("users").document(trip.user_id).collection("trips").document(trip.trip_id).collection("people").document(newParticipants.get(i).userId).set(newParticipants.get(i));
            db.collection("trips").document(trip.trip_id).collection("people").document(newParticipants.get(i).userId).set(newParticipants.get(i));

        }


        for(int j = 0; j < origParticipants.size(); j++)
        {
            boolean inNew = false;

            for(int i = 0; i < newParticipants.size(); i++)
            {
                if(origParticipants.get(j).userId.equals(newParticipants.get(i).userId))
                {
                    inNew = true;
                }
            }

            if(inNew == false)
            {
                removeParticipants.add(origParticipants.get(j).userId);
            }
        }


        for(int i = 0; i < removeParticipants.size(); i++)
        {
            Log.d("Remove ID", removeParticipants.get(i));

            db.collection("users").document(trip.user_id).collection("trips").document(trip.trip_id).collection("people").document(removeParticipants.get(i)).delete();

            for(int j = 0; j < origParticipants.size(); j++)
            {
                db.collection("users").document(origParticipants.get(j).userId).collection("trips").document(trip.trip_id).collection("people").document(removeParticipants.get(i)).delete();
            }

            db.collection("users").document(removeParticipants.get(i)).collection("trips").document(trip.trip_id).delete();
            db.collection("trips").document(trip.trip_id).collection("people").document(removeParticipants.get(i)).delete();
        }
    }

    public void getCurrentPlan()
    {

        db.collection("users").document(selectedTrip.user_id).collection("trips").document(selectedTrip.trip_id).collection("plan").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    TripPlan tripPlan = new TripPlan(document.getData());
                    description = tripPlan.planDescription;
                    int position = 1;

                    for(int i = 0; i < tripPlan.visitOrder.size(); i++)
                    {
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

                        planPlaceArrayList.add(planPlace);
                        userPlacesList.add(place1);
                        position++;
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
                    tripImage.setImageResource(R.drawable.alaska);
                } else if (data.getExtras().getSerializable("coverPhoto").equals("borabora")) {
                    selectedCoverPhoto = R.drawable.borabora;
                    tripImage.setImageResource(R.drawable.borabora);
                } else if (data.getExtras().getSerializable("coverPhoto").equals("cappadocia")) {
                    selectedCoverPhoto = R.drawable.cappadocia;
                    tripImage.setImageResource(R.drawable.cappadocia);
                } else if (data.getExtras().getSerializable("coverPhoto").equals("cavin")) {
                    selectedCoverPhoto = R.drawable.cavin;
                    tripImage.setImageResource(R.drawable.cavin);
                } else if (data.getExtras().getSerializable("coverPhoto").equals("colombia")) {
                    selectedCoverPhoto = R.drawable.colombia;
                    tripImage.setImageResource(R.drawable.colombia);
                } else if (data.getExtras().getSerializable("coverPhoto").equals("grandCanyon")) {
                    selectedCoverPhoto = R.drawable.grandcanyonofthecoloradoar;
                    tripImage.setImageResource(R.drawable.grandcanyonofthecoloradoar);
                } else if (data.getExtras().getSerializable("coverPhoto").equals("snowboard")) {
                    selectedCoverPhoto = R.drawable.snowboard;
                    tripImage.setImageResource(R.drawable.snowboard);
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
                cityLoc = new LatLng(city.latitude, city.longitude);
                cityText.setText(city.name);

                Log.d("AddTripActivity Result", "Add Trip Activity Result");

                //String[] typesCompare = {"restaurant", "airport", "amusement_park", "aquarium", "car_rental", "city_hall", "museum", "police", "parking"};
                String link = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + city.latitude + "," + city.longitude + "&radius=1609.34&" +
                        "type=restaurant&key=" + BuildConfig.GMAPS_CONSUMER_SECRET;


                new GetPlacesAsync(EditTripActivity.this, db, EditTripActivity.this).execute(link);

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

    public void getListOfFriends(final String userId) {
        db.collection("users").document(userId).collection("friends").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        User user = new User(document.getData());
                        users.add(user);
                        Log.d(TAG, document.getId() + " => " + document.getData());
                    }

                   for(int i = 0; i < selectedTrip.people.size(); i++)
                    {
                        User cUser = new User();

                        cUser.userId = (String)((Map)selectedTrip.people.get(i)).get("userId");
                        cUser.firstName = (String)((Map)selectedTrip.people.get(i)).get("firstName");
                        cUser.lastName = (String)((Map)selectedTrip.people.get(i)).get("lastName");
                        cUser.email = (String)((Map)selectedTrip.people.get(i)).get("email");
                        cUser.gender = (String)((Map)selectedTrip.people.get(i)).get("gender");
                        cUser.url = (String)((Map)selectedTrip.people.get(i)).get("url");
                        cUser.storagePath = (String)((Map)selectedTrip.people.get(i)).get("storagePath");

                        if(!cUser.userId.equals(userId))
                        {
                            origParticipants.add(cUser);
                            newParticipants.add(cUser);
                        }

                    }

                    for (int i = 0; i < origParticipants.size(); i++) {
                        boolean add = true;

                        for (int j = 0; j < users.size(); j++) {
                            if(origParticipants.get(i).userId.equals(users.get(j).userId)){
                                add = false;
                            }
                        }

                        if(add == true)
                        {
                            users.add(origParticipants.get(i));
                        }
                    }

                    _users = new CharSequence[users.size()];
                    for (int i = 0; i < users.size(); i++) {
                        _users[i] = users.get(i).firstName + " " + users.get(i).lastName;
                    }
                }
            }
        });
    }

    //UPLOAD IMAGE TO CLOUD
    private void uploadImage( Bitmap photoBitmap) {

        final StorageReference avatarRepo = storageReference.child("coverPhotos/" + selectedTrip.trip_id +".png");

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
                    changePhoto = true;
                    isCoverPhotoUpload = true;
                }else{
                    Toast.makeText(EditTripActivity.this, "Please upload a cover photo!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void addAllPlaces(ArrayList<Place> places) {
        allPlacesList.clear();
        allPlacesListS.clear();

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
