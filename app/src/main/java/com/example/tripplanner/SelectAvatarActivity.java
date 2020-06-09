package com.example.tripplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class SelectAvatarActivity extends AppCompatActivity {
    public String bundleKey = "avatar";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_avatar);

        setTitle("Select Avatar");

        ImageView iv_avatar1 = findViewById(R.id.iv_avatar1);
        iv_avatar1.setTag("avatar1");

        iv_avatar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createIntent(bundleKey, "avatar1");
            }
        });

        ImageView iv_avatar2 = findViewById(R.id.iv_avatar2);
        iv_avatar2.setTag("avatar2");

        iv_avatar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createIntent(bundleKey, "avatar2");

            }
        });

        ImageView iv_avatar3 = findViewById(R.id.iv_avatar3);
        iv_avatar3.setTag("avatar3");

        iv_avatar3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createIntent(bundleKey, "avatar3");
            }
        });

        ImageView iv_avatar4 = findViewById(R.id.iv_avatar4);
        iv_avatar4.setTag("avatar4");

        iv_avatar4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createIntent(bundleKey, "avatar4");
            }
        });

        ImageView iv_avatar5 = findViewById(R.id.iv_avatar5);
        iv_avatar5.setTag("avatar5");

        iv_avatar5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createIntent(bundleKey, "avatar5");
            }
        });

        ImageView iv_avatar6 = findViewById(R.id.iv_avatar6);
        iv_avatar6.setTag("avatar6");

        iv_avatar6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createIntent(bundleKey, "avatar6");
            }
        });
    }

    public void createIntent(String key, String avatarTag) {
        Intent intentBackToSignup = new Intent(SelectAvatarActivity.this, SignUpActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(key, avatarTag);
        setResult(SelectAvatarActivity.RESULT_OK, intentBackToSignup);
        intentBackToSignup.putExtra(SignUpActivity.SIGNUP_KEY, bundle);
        finish();
    }
}
