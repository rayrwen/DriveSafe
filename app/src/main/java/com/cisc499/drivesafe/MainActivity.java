package com.cisc499.drivesafe;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    String msg = "Android: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(msg, "onCreate() MainActivity");

    }

    // React to user profile button
    public void viewUserProfile(View view) {
        Intent intent = new Intent(this, UserProfile.class);
        startActivity(intent);
    }

    // React to start driving button
    public void startDriving(View view) {
        Intent intent = new Intent(this, StartDriving.class);
        startActivity(intent);
    }

    // DEBUG React to intersection button
    public void intersection(View view) {
        Intent intent = new Intent (this, Intersection.class);
        startActivity(intent);
    }

}
