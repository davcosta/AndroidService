package com.jessicathornsby.datalayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;

public class TriggerMain extends WearableActivity {
    private View textView;
    private View talkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView =  findViewById(R.id.text);
        talkButton =  findViewById(R.id.talkClick);


        setContentView(R.layout.activity_main);

        // Start the BackgroundService to receive and handle Myo events.
        startService(new Intent(this, MainActivity.class));

        // Close this activity since BackgroundService will run in the background.
        finish();
    }
}
