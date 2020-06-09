package com.example.tripplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {
    private FirebaseFirestore mDatabase;
    ArrayList<User> userArrayList = new ArrayList<>();

    EditText email, password;
    Button signIn, signUp;
    public FirebaseAuth mAuth;

    private  static final int BC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setTitle("Login");

        email = findViewById(R.id.emailSignIn);
        password = findViewById(R.id.passwordSignIn);
        signIn = findViewById(R.id.buttonSignIn);
        signUp = findViewById(R.id.buttonSignUpSIgnIn);

        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseFirestore.getInstance();

        mDatabase.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        User user = new User(document.getData());
                        userArrayList.add(user);
                    }
                }
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });


        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailInput = email.getText().toString();
                String passwordInput = password.getText().toString();

                mAuth.signInWithEmailAndPassword(emailInput, passwordInput).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            Gson gsonObject = new Gson();

                            User user = new User();

                            user.userId = mAuth.getCurrentUser().getUid();

                            for(int i = 0; i < userArrayList.size(); i++)
                            {
                                if(user.userId.equals(userArrayList.get(i).userId))
                                {
                                    user.firstName = userArrayList.get(i).firstName;
                                    user.lastName = userArrayList.get(i).lastName;
                                    user.email = userArrayList.get(i).email;
                                    user.gender = userArrayList.get(i).gender;
                                    user.url = userArrayList.get(i).url;

                                }
                            }

                            SharedPreferences prefs = getSharedPreferences("info", MODE_PRIVATE);
                            prefs.edit().putString("user", gsonObject.toJson(user)).commit();

                            boolean inDB = false;

                            for(int i = 0; i < userArrayList.size(); i++)
                            {
                                if(user.userId.equals(userArrayList.get(i).userId))
                                {
                                    inDB = true;
                                    user.firstName = userArrayList.get(i).firstName;
                                    user.lastName = userArrayList.get(i).lastName;
                                    user.email = userArrayList.get(i).email;
                                    user.gender = userArrayList.get(i).gender;
                                    user.url = userArrayList.get(i).url;
                                }
                            }


                            Toast.makeText(LoginActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();


                            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else
                        {
                            Toast.makeText(LoginActivity.this, "Login failure.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginActivity.this, "Login failure.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();

        isConnected();

        User user;
        Gson gson = new Gson();

        SharedPreferences prefs = getSharedPreferences("info", MODE_PRIVATE);

        user = gson.fromJson(prefs.getString("user", null), User.class);

        if(user != null) {
            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected()) {
            Log.d("connected", "not connected");
            Toast.makeText(this, "Not connected to the Internet", Toast.LENGTH_LONG).show();

            return false;

        }
        Toast.makeText(this, "Connected to the Internet", Toast.LENGTH_LONG).show();

        return true;
    }
}
