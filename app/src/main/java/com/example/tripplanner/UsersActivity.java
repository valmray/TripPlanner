package com.example.tripplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.util.ArrayList;

public class UsersActivity extends AppCompatActivity {
    public FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    public StorageReference storageReference = firebaseStorage.getReference();
    public ArrayList<User> users = new ArrayList<>();
    public ArrayList<User> friends = new ArrayList<>();
    public String TAG = "demo";
    public ListView lv_users;
    // Access a Cloud Firestore instance from your Activity
    public FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        setTitle("List of Users");

        lv_users = findViewById(R.id.lv_users);

        SharedPreferences prefs = getSharedPreferences("info", MODE_PRIVATE);

        Gson gson = new Gson();
        User user = gson.fromJson(prefs.getString("user", null), User.class);
        final String id = user.userId;

        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        User user = new User(document.getData());
                        users.add(user);
                        Log.d(TAG, document.getId() + " => " + document.getData());
                    }

                    final UsersAdapter ad = new UsersAdapter(UsersActivity.this,
                            android.R.layout.simple_list_item_1, users);

                    // give adapter to ListView UI element to render
                    lv_users.setAdapter(ad);

                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

        db.collection("users").document(id).collection("friends").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        User user = new User(document.getData());
                        friends.add(user);
                        Log.d(TAG, document.getId() + " => " + document.getData());
                    }

                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

        lv_users.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final User selectedUser = users.get(i);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(UsersActivity.this);

                boolean inFriends = false;
                for(int j = 0; j < friends.size(); j++)
                {
                    if(selectedUser.userId.equals(friends.get(j).userId))
                    {
                        inFriends = true;
                    }
                }

                if(inFriends == false && !selectedUser.userId.equals(id))
                {
                    alertDialog.setTitle("Would you like to add " + selectedUser.firstName + " " + selectedUser.lastName + " to your friends list?")
                            .setPositiveButton("Yes",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            db.collection("users").document(id).collection("friends").document(selectedUser.userId).set(selectedUser);
                                        }
                                    })
                            .setNegativeButton("No",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    })
                            .setCancelable(true)
                            .create()
                            .show();
                }
                else if(inFriends == true)
                {
                    alertDialog.setTitle("Would you like to remove " + selectedUser.firstName + " " + selectedUser.lastName + " from your friends list?")
                            .setPositiveButton("Yes",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            db.collection("users").document(id).collection("friends").document(selectedUser.userId).delete();
                                        }
                                    })
                            .setNegativeButton("No",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    })
                            .setCancelable(true)
                            .create()
                            .show();
                }


            }
        });
    }
}
