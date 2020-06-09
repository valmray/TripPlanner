package com.example.tripplanner;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UsersAdapter extends ArrayAdapter<User> {

    public String TAG = "demo";
    public FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    public StorageReference storageReference = firebaseStorage.getReference();

    public UsersAdapter(@NonNull Context context, int resource, @NonNull List<User> users) {
        super(context, resource, users);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        User user = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_item, parent, false);
        }

        TextView tv_fullName = convertView.findViewById(R.id.user_fullName);
        tv_fullName.setText(user.firstName + " " + user.lastName);


        TextView tv_gender = convertView.findViewById(R.id.user_gender);
        tv_gender.setText(user.gender);

        TextView tv_email = convertView.findViewById(R.id.user_email);
        tv_email.setText(user.email);

        ImageView iv_avatar = convertView.findViewById(R.id.iv_user_avatar);
        Picasso.get().load(user.url).into(iv_avatar);


        return convertView;
    }
}
