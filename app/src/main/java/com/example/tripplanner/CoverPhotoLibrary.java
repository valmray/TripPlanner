package com.example.tripplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class CoverPhotoLibrary extends AppCompatActivity {
    public ImageView iv_alaska;
    public ImageView iv_borabora;
    public ImageView iv_cappadocia;
    public ImageView iv_cavin;
    public ImageView iv_colombia;
    public ImageView iv_grandCanyon;
    public ImageView iv_snowboard;
    public Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cover_photo_library);

        setTitle("Cover Photo Library");

        intent = getIntent();


        iv_alaska = findViewById(R.id.iv_alaska);
        iv_alaska.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("coverPhoto","alaska");
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        iv_borabora = findViewById(R.id.iv_borabora);
        iv_borabora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("coverPhoto","borabora");
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        iv_cappadocia = findViewById(R.id.iv_cappadocia);
        iv_cappadocia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("coverPhoto","cappadocia");
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        iv_cavin = findViewById(R.id.iv_calvin);
        iv_cavin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("coverPhoto","cavin");
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        iv_colombia = findViewById(R.id.iv_colombia);
        iv_colombia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("coverPhoto","colombia");
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        iv_grandCanyon = findViewById(R.id.iv_grandCanyon);
        iv_grandCanyon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("coverPhoto","grandCanyon");
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        iv_snowboard = findViewById(R.id.iv_snowBoard);
        iv_snowboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("coverPhoto","snowboard");
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
