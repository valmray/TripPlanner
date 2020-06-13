package com.example.tripplanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

public class EditProfileActivity extends AppCompatActivity {
    // Access a Cloud Firestore instance from your Activity
    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    public String TAG = "demo";
    public FirebaseAuth mAuth;
    public EditText et_firstName_edit;
    public EditText et_lastName_edit;
    public RadioGroup rg_gender_edit;
    public RadioButton rb_female;
    public RadioButton rb_male;
    public EditText et_email_edit;
    public EditText et_password_edit;
    public Bundle extrasFromDashboard;
    public ImageView iv_selectAvatar_edit;
    public Button btn_updateProfile, cancel;
    public int REQCODE = 5;
    public static final String EDIT_KEY = "avatar";
    public Bundle extrasFromSelectAvatar;
    public FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    public StorageReference storageReference = firebaseStorage.getReference();
    public String userId;
    public String gender;
    public String selectedAvatarTagName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        setTitle("Edit Profile");

        extrasFromDashboard = getIntent().getExtras().getBundle("bundleData");

        userId = (String) extrasFromDashboard.getSerializable("userId");

        et_firstName_edit = findViewById(R.id.et_firstName_edit);
        et_lastName_edit = findViewById(R.id.et_lastName_edit);
        et_email_edit = findViewById(R.id.et_newUser_email_edit);
        et_password_edit = findViewById(R.id.et_newUser_password_edit);
        rg_gender_edit = findViewById(R.id.radioGroup_edit);
        rb_female = findViewById(R.id.rb_female_edit);
        rb_male = findViewById(R.id.rb_male_edit);
        iv_selectAvatar_edit = findViewById(R.id.iv_selectAvatar_edit);
        btn_updateProfile = findViewById(R.id.btn_editProfile);
        cancel = findViewById(R.id.editProfile_btn_cancel);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        iv_selectAvatar_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentToSelectAvatar = new Intent(EditProfileActivity.this, SelectAvatarActivity.class);
                startActivityForResult(intentToSelectAvatar, REQCODE);
            }
        });
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        User user = new User(document.getData());
                        et_firstName_edit.setText(user.firstName);
                        et_lastName_edit.setText(user.lastName);
                        et_email_edit.setText(user.email);
                        Log.d(TAG, "user.getGender(): " + user.gender);
                        if(user.gender != null ) {
                            gender = user.gender;

                            switch (user.gender) {
                                case "female":
                                    rb_female.setChecked(true);
                                    break;
                                case "male":
                                    rb_male.setChecked(true);
                                    break;
                                default:
                                    rb_male.setChecked(false);
                                    rb_female.setChecked(false);
                                    break;
                            }
                        }
                        Log.d(TAG, "url: " + user.url);
                        Picasso.get().load(user.url).into(iv_selectAvatar_edit);
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        rg_gender_edit.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Log.d(TAG, "getCheckedRadioButtonId: " + radioGroup.getCheckedRadioButtonId());
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.rb_female_edit:
                        Log.d(TAG, "onCheckedChanged: " + "female");
                        gender = "female";
                        break;
                    case R.id.rb_male_edit:
                        gender = "male";
                        db.collection("users").document(userId).update("gender", "male");
                        break;
                    default:
                        Toast.makeText(EditProfileActivity.this, "Please select a gender.", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        btn_updateProfile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final boolean[] isErrorThrown = {false};

                Log.d(TAG, "current user: " + userId);
                if (!et_email_edit.getText().toString().equals("") || et_email_edit.getText() != null) {
                    final Task updateEmailTask = FirebaseAuth.getInstance().getCurrentUser().updateEmail(et_email_edit.getText().toString());
                    updateEmailTask.addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "email task: " + updateEmailTask.getResult());
                                db.collection("users").document(userId).update("email", et_email_edit.getText().toString());
                            }
                        }
                    });
                } else {
                    isErrorThrown[0] = true;
                    et_email_edit.setError("Please enter a valid email address.");
                }
                Log.d(TAG, "password: " + et_password_edit.getText().toString());
                if (!et_password_edit.getText().toString().equals("") && et_password_edit.getText() != null) {
                    Task updatePasswordTask = FirebaseAuth.getInstance().getCurrentUser().updatePassword(et_password_edit.getText().toString());
                } else {
                    isErrorThrown[0] = true;
                    et_password_edit.setError("Please enter a valid password.");
                }

                if (!et_firstName_edit.getText().toString().equals("") || et_firstName_edit.getText().toString() != null) {
                    db.collection("users").document(userId).update("firstName", et_firstName_edit.getText().toString());

                } else {
                    isErrorThrown[0] = true;
                    et_firstName_edit.setError("Please enter a first name.");
                }

                if (et_lastName_edit.getText().toString().equals("") || et_lastName_edit.getText() != null) {
                    db.collection("users").document(userId).update("lastName", et_lastName_edit.getText().toString());

                } else {
                    isErrorThrown[0] = true;
                    et_lastName_edit.setError("Please enter a last name.");
                }


                Bitmap bMap = null;
                if (selectedAvatarTagName != null && !selectedAvatarTagName.equals("")) {
                    switch (selectedAvatarTagName) {
                        case "avatar1":
                            bMap = BitmapFactory.decodeResource(getResources(), R.drawable.avatar_f_3);
                            uploadImage(bMap);
                            break;
                        case "avatar2":
                            bMap = BitmapFactory.decodeResource(getResources(), R.drawable.avatar_f_2);
                            uploadImage(bMap);
                            break;
                        case "avatar3":
                            bMap = BitmapFactory.decodeResource(getResources(), R.drawable.avatar_f_1);
                            uploadImage(bMap);
                            break;
                        case "avatar4":
                            bMap = BitmapFactory.decodeResource(getResources(), R.drawable.avatar_m_1);
                            uploadImage(bMap);
                            break;
                        case "avatar5":
                            bMap = BitmapFactory.decodeResource(getResources(), R.drawable.avatar_m_2);
                            uploadImage(bMap);
                            break;
                        case "avatar6":
                            bMap = BitmapFactory.decodeResource(getResources(), R.drawable.avatar_m_3);
                            uploadImage(bMap);
                            break;

                    }

                }

                db.collection("users").document(userId).update("gender", gender);

                if(!isErrorThrown[0]){
                    finish();
                }

            }

        });


    }

    //UPLOAD IMAGE TO CLOUD
    private void uploadImage(Bitmap photoBitmap) {

        final StorageReference avatarRepo = storageReference.child("avatars/" + userId + ".png");

        //newUser.storagePath = avatarRepo.getPath();

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

                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                return avatarRepo.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    db.collection("users").document(userId).update("url", task.getResult().toString());
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQCODE) {
            if (resultCode == RESULT_OK) {
                extrasFromSelectAvatar = data.getExtras().getBundle(EDIT_KEY);

                selectedAvatarTagName = (String) extrasFromSelectAvatar.getSerializable("avatar");
                Log.d(TAG, "onActivityResult: " + data.getExtras().getSerializable("avatar"));
                if (selectedAvatarTagName != null) {
                    if (selectedAvatarTagName.equals("avatar1")) {
                        iv_selectAvatar_edit.setImageResource(R.drawable.avatar_f_3);
                    } else if (selectedAvatarTagName.equals("avatar2")) {
                        iv_selectAvatar_edit.setImageResource(R.drawable.avatar_f_2);
                    } else if (selectedAvatarTagName.equals("avatar3")) {
                        iv_selectAvatar_edit.setImageResource(R.drawable.avatar_f_1);
                    } else if (selectedAvatarTagName.equals("avatar4")) {
                        iv_selectAvatar_edit.setImageResource(R.drawable.avatar_m_1);
                    } else if (selectedAvatarTagName.equals("avatar5")) {
                        iv_selectAvatar_edit.setImageResource(R.drawable.avatar_m_2);
                    } else if (selectedAvatarTagName.equals("avatar6")) {
                        iv_selectAvatar_edit.setImageResource(R.drawable.avatar_m_3);
                    }

                } else {
                    Toast.makeText(this, "No avatar was selected!", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }
}
