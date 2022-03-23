package com.example.bt_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class projectSelector extends AppCompatActivity {
    Button doggoButton, armButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_selector);
        Bundle bundle = getIntent().getExtras();
        String Id = bundle.getString("id");
        doggoButton = (Button) findViewById(R.id.doggo);
        doggoButton.setOnClickListener(view ->{
            Intent intent = new Intent(projectSelector.this, Doggo.class);
            intent.putExtra("id", Id);
            startActivity(intent);
        });
    }
}