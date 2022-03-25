package com.example.olhovirtual.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.olhovirtual.R;

public class MainActivity extends AppCompatActivity {

    public Button testeCamera;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testeCamera = findViewById(R.id.btnCamera);

        testeCamera.setOnClickListener(v -> {
            testeCamera();
        });

    }

    public void testeCamera(){
        Intent clickPhoto = new Intent(this, CameraActivity.class);
        startActivity(clickPhoto);
    }

}