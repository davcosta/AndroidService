package com.jessicathornsby.datalayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
/*import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;*/
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class TriggerMain extends AppCompatActivity {

    private static final int PERMISSION_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int permissionCheck1 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionCheck1 == PackageManager.PERMISSION_GRANTED)
            Log.d("trigger", "permisison accepted");
        else {
            Log.d("trigger", "permisison denied");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST);
        }

        setContentView(R.layout.activity_trigger_main);

        // Start the BackgroundService to receive and handle Myo events.
        startService(new Intent(this, MainActivity.class));

        // Close this activity since BackgroundService will run in the background.
        finish();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, yay! Start the Bluetooth device scan.
                } else {
                    // Alert the user that this application requires the location permission to perform the scan.
                }
            }
        }
    }
}
