package com.moteve.mca;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class Configurer extends Activity {
    
    private static final String TAG = "Moteve_Configurer";
    
    private Spinner videoPermissionsSpinner = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.config_basic);
	final Context ctx = this;
	
	Button connectionSettingsButton = (Button) findViewById(R.id.configConnection);
	connectionSettingsButton.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		Intent intent = new Intent(ctx, ConnectionSettings.class);
		startActivity(intent);
	    }
	});
	
	Button okButton = (Button) findViewById(R.id.saveConfig);
	okButton.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		String defaultGroup = (String) videoPermissionsSpinner.getSelectedItem();
		Editor editor = Main.getPrefs().edit();
		editor.putString("defaultGroup", defaultGroup);
		editor.commit();
		finish();
	    }
	});
	
	videoPermissionsSpinner = (Spinner) findViewById(R.id.videoPermissionsSpinner);
	refreshPermissionGroups();
    }

    private void refreshPermissionGroups() {
	String[] availableGroups = getGroups();
	ArrayAdapter adapter = new ArrayAdapter(this,
		android.R.layout.simple_spinner_item,
		availableGroups);
	videoPermissionsSpinner.setAdapter(adapter);
	
	// pre-select the default group
	String defaultGroup = Main.getPrefs().getString("defaultGroup",
	    "JUST_ME");
	int position = 0;
	for (; position < availableGroups.length; position++) {
	    if (defaultGroup.equals(availableGroups[position])) {
		break;
	    }
	}
	videoPermissionsSpinner.setSelection(position);
    }

    /**
     * 
     * @return names of the Groups the user has created on the server
     */
    private String[] getGroups() {
	SharedPreferences prefs = Main.getPrefs();
	String serverUrl = prefs.getString("serverUrl", null);
	String token = prefs.getString("token", null);
	if (serverUrl == null || serverUrl.length() == 0
		|| token == null || token.length() == 0) {
	    return new String[] {"Connection not configured"};
	}
	
	
	try {
	    return retrieveGroups(serverUrl, token);
	} catch (IOException e) {
	    Log.e(TAG, "Error retrieving group names", e);
	    return new String[] {"Error connecting to server"};
	}
    }
    
    private String[] retrieveGroups(String serverUrl, String token)
	    throws IOException {
	String groupsUrl = serverUrl + "/mca/listGroups.htm";
	URL url = new URL(groupsUrl);
	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	conn.setDoInput(true);
	conn.setDoOutput(true);
	conn.setUseCaches(false);
	conn.setRequestMethod("POST");
	conn.setRequestProperty("Cache-Control", "no-cache");
	conn.setRequestProperty("Pragma", "no-cache");
	conn.setRequestProperty("Moteve-Token", token);

	String groupsNames = ConnectionUtils.receiveResponse(conn);
	Log.i(TAG, "Obtained group names='" + groupsNames + "'");
	conn.disconnect();

	return groupsNames.split("\\\\"); // just one backslash: escaped for java & for regexp
    }

    @Override
    protected void onResume() {
	super.onResume();
	refreshPermissionGroups();
    }
    
    

}
