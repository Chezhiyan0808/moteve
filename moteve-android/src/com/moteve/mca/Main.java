package com.moteve.mca;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Main extends Activity {
    
    private static Activity CONF_ACTIVITY;
    
    public static SharedPreferences getPrefs() {
	// TODO Here it's assumed the Main activity was already launched and the var is set
	return CONF_ACTIVITY.getPreferences(Context.MODE_PRIVATE);
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CONF_ACTIVITY = this;
        final Context ctx = this;
        setContentView(R.layout.main);
        
        Button settingsButton = (Button) findViewById(R.id.settings);
        Button videoCaptureButton = (Button) findViewById(R.id.captureVideo);
        
        settingsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ctx, Configurer.class);
				startActivity(intent);
			}
		});
        
        videoCaptureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ctx, VideoCapturer.class);
				startActivity(intent);
			}
		});
    }
}