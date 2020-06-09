package com.example.tripplanner;

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

import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;

public class CreateTripPlanActivity extends AppCompatActivity{
    Button findCity, addPlaceAll, addPlacePlan, resetPlan, cancel, finalize, addPlanDescription, btn_done;
    EditText tripNameEdit;
    TextView chosenCity;
    Spinner allPlacesSpinner, userPlacesSpinner;
    ListView tripPlanList;
    ImageButton deleteUserPlace;
    LinearLayout cityPlacesLayout, tripPlanLayout;
    FirebaseFirestore databaseReference;
    User user;
    EditText addPlanDescriptionEdit;

    ArrayList<Place> allPlacesList = new ArrayList<>();
    ArrayList<Place> userPlacesList = new ArrayList<>();
    ArrayList<PlanPlace> planPlaceArrayList = new ArrayList<>();

    ArrayList<String> allPlacesListS = new ArrayList<>();
    ArrayList<String> userPlacesListS = new ArrayList<>();
    ArrayList<String> planArrayListS = new ArrayList<>();
    String description = "";
    int pos;
    int pos2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trip_plan);

        allPlacesList = (ArrayList<Place>) getIntent().getSerializableExtra("Places");

        for(int i = 0; i < allPlacesList.size(); i++)
        {
            allPlacesListS.add(allPlacesList.get(i).name);
        }

        SharedPreferences prefs = getSharedPreferences("info", MODE_PRIVATE);

        Gson gson = new Gson();
        user = gson.fromJson(prefs.getString("user", null), User.class);


        databaseReference = FirebaseFirestore.getInstance();

        findCity = findViewById(R.id.findCityAdd);
        addPlaceAll = findViewById(R.id.addPlaceFromAllButtion);
        addPlacePlan = findViewById(R.id.addPlaceToPlanButton);
        resetPlan = findViewById(R.id.resetPlanButton);
        chosenCity = findViewById(R.id.cityTextAdd);
        allPlacesSpinner = findViewById(R.id.allPlacesSpinner);
        userPlacesSpinner = findViewById(R.id.userPlacesSpinner);
        tripPlanList = findViewById(R.id.listViewAdd);
        addPlanDescription = findViewById(R.id.addPlanDescriptionButton);
        cityPlacesLayout  = findViewById(R.id.cityPlacesLayout);
        tripPlanLayout  = findViewById(R.id.tripPlanLayout);
        deleteUserPlace  = findViewById(R.id.deleteUserPlaceButton);
        btn_done  = findViewById(R.id.btn_done);

        cityPlacesLayout.setVisibility(View.INVISIBLE);

        addPlaceAll.setEnabled(false);

        tripPlanLayout.setVisibility(View.INVISIBLE);

        addPlacePlan.setEnabled(false);

        addPlanDescription.setEnabled(false);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(CreateTripPlanActivity.this, android.R.layout.simple_list_item_1,
                android.R.id.text1, allPlacesListS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        allPlacesSpinner.setAdapter(adapter);

        cityPlacesLayout.setVisibility(View.VISIBLE);
        addPlaceAll.setEnabled(true);

        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPlan();
            }
        });

        addPlanDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RelativeLayout relativeLayout = new RelativeLayout(CreateTripPlanActivity.this);
                relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));


                addPlanDescriptionEdit = new EditText(CreateTripPlanActivity.this);

                addPlanDescriptionEdit.setHint("Description of the trip plan");
                addPlanDescriptionEdit.setPadding(16, 16, 16, 16);
                addPlanDescriptionEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                addPlanDescriptionEdit.setSingleLine(false);
                addPlanDescriptionEdit.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                addPlanDescriptionEdit.setId(Integer.parseInt("5"));

                relativeLayout.addView(addPlanDescriptionEdit);

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(CreateTripPlanActivity.this);
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

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(CreateTripPlanActivity.this, android.R.layout.simple_list_item_1,
                        android.R.id.text1, planArrayListS);
                tripPlanList.setAdapter(adapter);
            }
        });

        deleteUserPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(CreateTripPlanActivity.this);
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

                                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(CreateTripPlanActivity.this, android.R.layout.simple_list_item_1,
                                                android.R.id.text1, planArrayListS);
                                        tripPlanList.setAdapter(adapter);

                                        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(CreateTripPlanActivity.this, android.R.layout.simple_list_item_1,
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
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(CreateTripPlanActivity.this);
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


                                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(CreateTripPlanActivity.this, android.R.layout.simple_list_item_1,
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

                    ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(CreateTripPlanActivity.this, android.R.layout.simple_list_item_1,
                            android.R.id.text1, userPlacesListS);
                    adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    userPlacesSpinner.setAdapter(adapter2);


                }
                else
                {
                    if(valid == true)
                    {
                        Toast.makeText(CreateTripPlanActivity.this, "You've already added this place", Toast.LENGTH_LONG).show();
                    }

                    if(userPlacesList.size() == 15)
                    {
                        Toast.makeText(CreateTripPlanActivity.this, "You can only add up to 15 places", Toast.LENGTH_LONG).show();
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

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(CreateTripPlanActivity.this, android.R.layout.simple_list_item_1,
                            android.R.id.text1, planArrayListS);
                    tripPlanList.setAdapter(adapter);


                }
                else
                {
                    Toast.makeText(CreateTripPlanActivity.this, "You've already added this place to the trip plan list", Toast.LENGTH_LONG).show();

                    valid = false;
                }

            }
        });
    }

    protected void sendPlan(){

        Intent intent = new Intent();
        intent.putExtra(AddTripActivity.VALUE_KEY_PLAN, planPlaceArrayList);
        intent.putExtra(AddTripActivity.VALUE_KEY_PLACES, userPlacesList);
        intent.putExtra(AddTripActivity.VALUE_KEY_PLAN_DESCRIPTION, description);
        setResult(RESULT_OK, intent);

        finish();
    }


}
