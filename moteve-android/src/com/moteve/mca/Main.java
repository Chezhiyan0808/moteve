package com.moteve.mca;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Main extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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