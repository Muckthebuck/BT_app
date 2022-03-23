package com.example.bt_app;


import android.app.Activity;
import android.os.Bundle;
import java.lang.*;

import android.content.Intent;
import android.widget.Button;

public class MainActivity extends Activity {
    //bluetooth variables
    Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = (Button) findViewById(R.id.buttonStart);

        startButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, connectingBTDevices.class);
            startActivity(intent);
        });
    }
}