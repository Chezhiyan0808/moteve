package com.moteve.mca;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ConnectionSettings extends Activity {

    private static final String TAG = "Moteve_ConnectionSettings";
    private static final String MCA_DESC = "Moteve Android 20100220";
    private static final String AUTH_ERROR = "AUTH_ERROR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.config_connection);
	final Context ctx = this;

	final EditText serverUrlEdit = (EditText) findViewById(R.id.serverUrl);
	final EditText emailEdit = (EditText) findViewById(R.id.email);
	final EditText passwordEdit = (EditText) findViewById(R.id.password);
	final Button okButton = (Button) findViewById(R.id.saveConnectionConfig);

	SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
	serverUrlEdit.setText(prefs.getString("serverUrl", "http://moteve.com"));
	emailEdit.setText(prefs.getString("email", ""));
	passwordEdit.setText(prefs.getString("password", ""));

	okButton.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		// TODO save settings
		String serverUrl = serverUrlEdit.getText().toString();
		String email = emailEdit.getText().toString();
		String password = passwordEdit.getText().toString();
		saveParams(serverUrl, email, password);
		String token = registerDevice(serverUrl, email, password);
		if (token != null) {
		    getPreferences(Context.MODE_PRIVATE).edit().putString("token", token);
		    Toast.makeText(ctx, "Connection OK", Toast.LENGTH_SHORT).show();
		    finish();
		} else {
		    new AlertDialog.Builder(ctx).setMessage("Authentication failed").show();
		}
	    }

	    private void saveParams(String serverUrl, String email,
		    String password) {
		Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
		editor.putString("serverUrl", serverUrl);
		editor.putString("email", email);
		editor.putString("password", password);
		editor.commit();
	    }
	});
    }

    public String registerDevice(String serverUrl, String email,
	    String password) {
	try {
	    String regUrl = serverUrl + "/mca/register.htm";
	    String authHeader = email + "\\" + password + "\\" + MCA_DESC;
	    Log.i(TAG, "Registering device. URL=" + regUrl);
	    URL url = new URL(regUrl);
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setDoInput(true);
	    conn.setDoOutput(true);
	    conn.setUseCaches(false);
	    conn.setRequestMethod("POST");
	    conn.setRequestProperty("Cache-Control", "no-cache");
	    conn.setRequestProperty("Pragma", "no-cache");
	    // conn.setRequestProperty("Content-Type",
	    // "application/octet-stream");
	    conn.setRequestProperty("Moteve-Auth", authHeader);
	    
	    String token = ConnectionUtils.receiveResponse(conn);
	    Log.i(TAG, "Obtained token='" + token + "'");
	    conn.disconnect();

	    if (token == null || AUTH_ERROR.equals(token)) {
		Log.e(TAG, "Authentication failed");
		return null;
	    } else {
		Log.i(TAG, "Authentication successful");
		return token;
	    }
	} catch (IOException e) {
	    Log.e(TAG, "Authentication failed", e);
	    return null;
	}
    }

}
