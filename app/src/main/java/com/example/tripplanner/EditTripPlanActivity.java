package com.example.tripplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Map;

public class EditTripPlanActivity extends AppCompatActivity {
    Trip selectedTrip;
    User user;

    Button addPlaceAll, addPlacePlan, resetPlan, cancel, finalize, addPlanDescription, btn_done;
    Spinner allPlacesSpinner, userPlacesSpinner;
    ListView tripPlanList;
    ImageButton deleteUserPlace;
    LinearLayout cityPlacesLayout, tripPlanLayout;
    FirebaseFirestore databaseReference;
    EditText addPlanDescriptionEdit;

    ArrayList<Place> allPlacesList = new ArrayList<>();
    ArrayList<Place> userPlacesList = new ArrayList<>();
    ArrayList<PlanPlace> planPlaceArrayList = new ArrayList<>();

    ArrayList<String> allPlacesListS = new ArrayList<>();
    ArrayList<String> userPlacesListS = new ArrayList<>();
    ArrayList<String> planArrayListS = new ArrayList<>();

    String description = "";
    int pos, pos2;
    int position = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_trip_plan);


        selectedTrip = (Trip)getIntent().getSerializableExtra("Trip");

        SharedPreferences prefs = getSharedPreferences("info", MODE_PRIVATE);

        Gson gson = new Gson();
        user = gson.fromJson(prefs.getString("user", null), User.class);

        allPlacesSpinner = findViewById(R.id.edit_allPlacesSpinner);

        databaseReference = FirebaseFirestore.getInstance();



        addPlaceAll = findViewById(R.id.edit_addPlaceFromAllButton);
        addPlacePlan = findViewById(R.id.edit_addPlaceToPlanButton);
        resetPlan = findViewById(R.id.edit_resetPlanButton);
        userPlacesSpinner = findViewById(R.id.edit_userPlacesSpinner);
        tripPlanList = findViewById(R.id.edit_listViewAdd);
        addPlanDescription = findViewById(R.id.edit_addPlanDescriptionButton);
        cityPlacesLayout  = findViewById(R.id.edit_cityPlacesLayout);
        tripPlanLayout  = findViewById(R.id.edit_tripPlanLayout);
        deleteUserPlace  = findViewById(R.id.edit_deleteUserPlaceButton);
        btn_done  = findViewById(R.id.edit_btn_done);
        cancel = findViewById(R.id.edit_btn_cancel);

        setAllPlaces();

        //userPlacesList = (ArrayList<Place>) getIntent().getSerializableExtra("User_Places");
        //planPlaceArrayList = (ArrayList<PlanPlace>) getIntent().getSerializableExtra("Plan_Place");

        if(selectedTrip != null)
        {
            getCurrentPlan();
        }

        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPlan();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        addPlanDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RelativeLayout relativeLayout = new RelativeLayout(EditTripPlanActivity.this);
                relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));


                addPlanDescriptionEdit = new EditText(EditTripPlanActivity.this);

                addPlanDescriptionEdit.setText(description);

                addPlanDescriptionEdit.setHint("Description of the trip plan");
                addPlanDescriptionEdit.setPadding(16, 16, 16, 16);
                addPlanDescriptionEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                addPlanDescriptionEdit.setSingleLine(false);
                addPlanDescriptionEdit.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                addPlanDescriptionEdit.setId(Integer.parseInt("5"));

                relativeLayout.addView(addPlanDescriptionEdit);

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(EditTripPlanActivity.this);
                alertDialog.setTitle("Add a description for your trip plan")
                        .setView(relativeLayout)
                        .setPositiveButton("Submit",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                        description = addPlanDescriptionEdit.getText().toString();

                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                        .setCancelable(true)
                        .create()
                        .show();
            }
        });

        resetPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                planPlaceArrayList.clear();
                planArrayListS.clear();

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(EditTripPlanActivity.this, android.R.layout.simple_list_item_1,
                        android.R.id.text1, planArrayListS);
                tripPlanList.setAdapter(adapter);
            }
        });

        deleteUserPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(EditTripPlanActivity.this);
                alertDialog.setTitle("Delete " + userPlacesListS.get(pos2) + " from your places?")
                        .setPositiveButton("Delete",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        userPlacesList.remove(pos2);
                                        String placeName = userPlacesListS.get(pos2);
                                        userPlacesListS.remove(pos2);

                                        if(userPlacesList.size() == 0)
                                        {
                                            tripPlanLayout.setVisibility(View.INVISIBLE);

                                            addPlacePlan.setEnabled(false);

                                            addPlanDescription.setEnabled(false);

                                            finalize.setVisibility(View.INVISIBLE);
                                            finalize.setEnabled(false);

                                        }

                                        for(int i = 0; i < planPlaceArrayList.size(); i++)
                                        {
                                            if(planPlaceArrayList.get(i).place.name.equals(placeName))
                                            {
                                                planPlaceArrayList.remove(i);
                                            }
                                        }

                                        planArrayListS.clear();

                                        for(int i = 0; i < planPlaceArrayList.size(); i++)
                                        {
                                            int p = i + 1;
                                            planPlaceArrayList.get(i).position = String.valueOf(p);
                                            planArrayListS.add(planPlaceArrayList.get(i).position + ". " + planPlaceArrayList.get(i).place.name);
                                        }

                                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(EditTripPlanActivity.this, android.R.layout.simple_list_item_1,
                                                android.R.id.text1, planArrayListS);
                                        tripPlanList.setAdapter(adapter);

                                        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(EditTripPlanActivity.this, android.R.layout.simple_list_item_1,
                                                android.R.id.text1, userPlacesListS);
                                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        userPlacesSpinner.setAdapter(adapter2);

                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                        .setCancelable(true)
                        .create()
                        .show();
            }
        });

        tripPlanList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(EditTripPlanActivity.this);
                alertDialog.setTitle("Delete " + planPlaceArrayList.get(position).place.name + " from your trip plan?")
                        .setPositiveButton("Delete",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        planPlaceArrayList.remove(position);
                                        planArrayListS.clear();

                                        for(int i = 0; i < planPlaceArrayList.size(); i++)
                                        {
                                            int p = i + 1;
                                            planPlaceArrayList.get(i).position = String.valueOf(p);
                                            planArrayListS.add(planPlaceArrayList.get(i).position + ". " + planPlaceArrayList.get(i).place.name);
                                        }


                                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(EditTripPlanActivity.this, android.R.layout.simple_list_item_1,
                                                android.R.id.text1, planArrayListS);
                                        tripPlanList.setAdapter(adapter);

                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                        .setCancelable(true)
                        .create()
                        .show();
            }
        });

        allPlacesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        userPlacesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pos2 = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        addPlaceAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valid = false;

                for(int j = 0; j < userPlacesListS.size(); j++)
                {

                    if(userPlacesListS.get(j).equals(allPlacesListS.get(pos)))
                    {
                        valid = true;
                    }
                }

                if(valid == false && userPlacesList.size() < 15)
                {
                    userPlacesList.add(allPlacesList.get(pos));
                    userPlacesListS.add(allPlacesList.get(pos).name);

                    if(userPlacesList.size() > 0)
                    {
                        tripPlanLayout.setVisibility(View.VISIBLE);

                        addPlacePlan.setEnabled(true);

                        addPlanDescription.setEnabled(true);



                    }

                    ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(EditTripPlanActivity.this, android.R.layout.simple_list_item_1,
                            android.R.id.text1, userPlacesListS);
                    adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    userPlacesSpinner.setAdapter(adapter2);


                }
                else
                {
                    if(valid == true)
                    {
                        Toast.makeText(EditTripPlanActivity.this, "You've already added this place", Toast.LENGTH_LONG).show();
                    }

                    if(userPlacesList.size() == 15)
                    {
                        Toast.makeText(EditTripPlanActivity.this, "You can only add up to 15 places", Toast.LENGTH_LONG).show();
                    }

                    valid = false;
                }

            }
        });

        addPlacePlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean valid = false;

                for(int j = 0; j < planArrayListS.size(); j++)
                {
                    if(planPlaceArrayList.get(j).place.name.equals(userPlacesListS.get(pos2)))
                    {
                        valid = true;
                    }
                }

                if(valid == false && planPlaceArrayList.size() < 15)
                {

                    PlanPlace planPlace = new PlanPlace();
                    planPlace.place = userPlacesList.get(pos2);
                    planPlace.position = String.valueOf(planPlaceArrayList.size() + 1);
                    planPlaceArrayList.add(planPlace);
                    planArrayListS.add(planPlace.position + ". " + planPlace.place.name);

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(EditTripPlanActivity.this, android.R.layout.simple_list_item_1,
                            android.R.id.text1, planArrayListS);
                    tripPlanList.setAdapter(adapter);


                }
                else
                {
                    Toast.makeText(EditTripPlanActivity.this, "You've already added this place to the trip plan list", Toast.LENGTH_LONG).show();

                    valid = false;
                }

            }
        });

    }

    protected void sendPlan(){

        Intent intent = new Intent();
        intent.putExtra(EditTripActivity.VALUE_KEY_PLAN, planPlaceArrayList);
        intent.putExtra(EditTripActivity.VALUE_KEY_PLACES, userPlacesList);
        intent.putExtra(EditTripActivity.VALUE_KEY_PLAN_DESCRIPTION, description);
        setResult(RESULT_OK, intent);

        finish();
    }

    public void getCurrentPlan()
    {
        databaseReference.collection("users").document(user.userId).collection("trips").document(selectedTrip.trip_id).collection("plan").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    TripPlan tripPlan = new TripPlan(document.getData());
                    description = tripPlan.planDescription;

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

                setPlanList();
                setUserPlaces();
            }
        });
    }

    public void setPlanList()
    {
        for(int i = 0; i < planPlaceArrayList.size(); i++)
        {
            planArrayListS.add(planPlaceArrayList.get(i).position + ". " + planPlaceArrayList.get(i).place.name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(EditTripPlanActivity.this, android.R.layout.simple_list_item_1,
                android.R.id.text1, planArrayListS);
        tripPlanList.setAdapter(adapter);
    }

    public void setUserPlaces()
    {

        for(int i = 0; i < userPlacesList.size(); i++)
        {
            userPlacesListS.add(userPlacesList.get(i).name);
        }

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(EditTripPlanActivity.this, android.R.layout.simple_list_item_1,
                android.R.id.text1, userPlacesListS);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userPlacesSpinner.setAdapter(adapter2);
    }

    public void setAllPlaces()
    {
        allPlacesList = (ArrayList<Place>) getIntent().getSerializableExtra("Places");

        for(int i = 0; i < allPlacesList.size(); i++)
        {
            allPlacesListS.add(allPlacesList.get(i).name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(EditTripPlanActivity.this, android.R.layout.simple_list_item_1,
                android.R.id.text1, allPlacesListS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        allPlacesSpinner.setAdapter(adapter);
    }
}
