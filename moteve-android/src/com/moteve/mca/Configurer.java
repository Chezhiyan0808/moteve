package com.moteve.mca;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

public class Configurer extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.config_basic);
	final Context ctx = this;
	
	Button connectionSettingsButton = (Button) findViewById(R.id.configConnection);
	Button okButton = (Button) findViewById(R.id.saveConfig);
	Spinner videoPermissionsSpinner = (Spinner) findViewById(R.id.videoPermissionsSpinner);
	
	connectionSettingsButton.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		Intent intent = new Intent(ctx, ConnectionSettings.class);
		startActivity(intent);
	    }
	});
	
	okButton.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		// TODO save the permissions
		finish();
	    }
	});
    }
    
    

}
