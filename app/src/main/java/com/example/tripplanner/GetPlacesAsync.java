package com.example.tripplanner;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class GetPlacesAsync extends AsyncTask<String, Integer, ArrayList<Place>> {
    Context ctx;
    Activity activity;
    FirebaseFirestore databaseRef;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
    MyInterface myInterface;

    public GetPlacesAsync(Context ctx, FirebaseFirestore databaseReference, MyInterface myInterface) {
        this.ctx = ctx;
        this.databaseRef = databaseReference;
        this.myInterface = myInterface;
    }

    @Override
    protected ArrayList<Place> doInBackground(String... strings) {
        HttpURLConnection connection = null; //Will have to establish a connection.
        ArrayList<Place> result = new ArrayList<>(); //To store the results.
        try {
            URL url = new URL(strings[0]); //To construct connection
            connection = (HttpURLConnection) url.openConnection();
            connection.connect(); //connection complete
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String json = IOUtils.toString(connection.getInputStream(), "UTF-8");
                JSONObject root = new JSONObject(json); //rest of objects obtained from root
                JSONArray places = root.getJSONArray("results");


                for (int i = 0; i < places.length(); i++) {
                    JSONObject sourceJson = places.getJSONObject(i); //getting json object at i

                    Place place = new Place();

                    place.latitude = sourceJson.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                    place.longitude = sourceJson.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                    place.name = sourceJson.getString("name");

                    String[] typesCompare = {"restaurant"};

                    JSONArray types = sourceJson.getJSONArray("types");
                    for(int j = 0; j < types.length(); j++)
                    {
                        for(int k = 0; k < typesCompare.length; k++)
                        {
                            if(typesCompare[k].equals(types.getString(j)))
                            {
                                place.type = types.getString(j);
                            }

                        }
                    }

                    String key = UUID.randomUUID().toString();
                    place.id = key;

                    Log.d("Results", place.toString());


                    result.add(place);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }


    @Override
    protected void onPostExecute(final ArrayList<Place> places) {

        myInterface.addAllPlaces(places);

    }
}
