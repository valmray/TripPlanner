package com.example.tripplanner;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ChatRoomAdapter extends ArrayAdapter<Message> {

    public String TAG = "demo";
    public FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    public StorageReference storageReference = firebaseStorage.getReference();
    public FirebaseAuth mAuth;
    public String userEmail;
    public String userId;

    public ChatRoomAdapter(@NonNull Context context, int resource, @NonNull List<Message> messages) {
        super(context, resource, messages);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Message message = getItem(position);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        userEmail = user.getEmail();
        userId = user.getUid();

        if (convertView == null) {
            if(userEmail.equals(message.email)){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.chat_item, parent, false);
            }else{
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.chat_receive_item, parent, false);
            }

        }else{
            if(userEmail.equals(message.email)){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.chat_item, parent, false);
            }else{
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.chat_receive_item, parent, false);
            }

        }
        Log.d(TAG, "message.message " + message.message );

        if(message.message != null && !message.message.equals("") ){
            TextView tv_message = convertView.findViewById(R.id.tv_message);
            if(userEmail.equals(message.email)){
                tv_message.setBackgroundColor(Color.GRAY);
            }else{
                tv_message.setBackgroundColor(Color.WHITE);
            }
            tv_message.setText(message.message + ": " + message.email + "   Date: " + message.date);
        }

        ImageView iv_coverPhoto_trips = convertView.findViewById(R.id.iv_message_photo);
        Log.d(TAG, "message.imageUrl " + message.imageUrl );

        if(message.imageUrl != null){
            TextView tv_message = convertView.findViewById(R.id.tv_message);
            tv_message.setText(message.email + "   Date: " + message.date);

            iv_coverPhoto_trips.setVisibility(View.VISIBLE);
            Picasso.get().load(message.imageUrl).into(iv_coverPhoto_trips);
        }else{
            //Keeps getting null and crashes app
            //iv_coverPhoto_trips.setVisibility(View.GONE);
        }

        return convertView;
    }
}

