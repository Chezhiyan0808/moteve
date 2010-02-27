package com.moteve.mca;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

/**
 * Usage: 1) obtain sequence ID from server 2) use as many times as needed a new
 * instance of this class (thread) to upload a finished video file chunks with
 * specified sequence ID
 * 
 * @author radek
 * 
 */
public class VideoUploader implements Runnable {

    private static final String TAG = "Moteve_FileUploader";
    public static final String SERVER_URLa = "http://192.168.1.14:8080/moteve/video/upload.htm";
    public static final int BUFFER_SIZE = 8192;
    public static final int SAMPLE_INTERVAL = 200; // ms
    public static final String DATA_ENCODING = "ISO8859_1";
    private String filePath;
    private String sequenceId;
    private String part;
    private String securityToken;
    private Context context;
    private static String uploadUrl;
    
    static {
	SharedPreferences prefs = Main.getPrefs();
	uploadUrl = prefs.getString("serverUrl", null) + "/mca/upload.htm";
    }

    public VideoUploader(String filePath, String sequenceId, String part,
	    Context context) {
	super();
	this.filePath = filePath;
	this.sequenceId = sequenceId;
	this.part = part;
	this.context = context;
	SharedPreferences prefs = Main.getPrefs();
	this.securityToken = prefs.getString("token", null);
    }

    // public void stopRecording() {
    // Log.d(TAG, "stopRecording()");
    // this.recording = false;
    // }

    // public void startRecording() {
    // Log.d(TAG, "startRecording()");
    // this.recording = true;
    // }

    @Override
    public void run() {
	Log.i(TAG, "VideoUploader(" + filePath + ")");
	DataOutputStream dos = null;
	HttpURLConnection conn = null;
	FileInputStream fis = null;

	try {
	    fis = new FileInputStream(filePath);
	    byte[] buffer = new byte[BUFFER_SIZE];
	    int bytesRead;
	    long totalSize = 0;

	    conn = connect();
	    dos = new DataOutputStream(conn.getOutputStream());
	    while ((bytesRead = fis.read(buffer)) != -1) {
		totalSize += bytesRead;
		dos.write(buffer, 0, bytesRead);
	    }
	    String response = ConnectionUtils.receiveResponse(conn);
	    Log.i(TAG, "File sending finished. Total size in bytes: "
		    + totalSize + ". Server response: " + response);

	} catch (IOException e) {
	    Log.e(TAG, "Error uploading media: " + e.getMessage(), e);
	    Toast.makeText(context, "Error uploading media: " + e.getMessage(),
		    Toast.LENGTH_LONG).show();
	} finally {
	    try {
		fis.close();
		dos.flush();
		dos.close();
		conn.disconnect();
		File f = new File(filePath);
		f.delete();
	    } catch (IOException e) {
		Log.e(TAG, "Error freeing resources", e);
	    }
	}

    }

    private HttpURLConnection connect() throws IOException {
	Log.d(TAG, "Connecting with sequence=" + sequenceId + ", part=" + part
		+ "...");

	// connection and headers
	URL url = new URL(uploadUrl);
	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	conn.setDoInput(true);
	conn.setDoOutput(true);
	conn.setUseCaches(false);
	conn.setRequestMethod("POST");
	conn.setRequestProperty("Cache-Control", "no-cache");
	conn.setRequestProperty("Pragma", "no-cache");
	conn.setRequestProperty("Moteve-Sequence", sequenceId);
	conn.setRequestProperty("Moteve-Token", securityToken);
	return conn;
    }

    public static String startVideo(String token, String mediaFormat,
	    String defaultGroup) throws IOException {
	Log.i(TAG, "Connecting to obtain a new sequence ID...");
	URL url = new URL(uploadUrl);
	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	conn.setDoInput(true);
	conn.setDoOutput(true);
	conn.setUseCaches(false);
	conn.setRequestMethod("POST");
	conn.setRequestProperty("Cache-Control", "no-cache");
	conn.setRequestProperty("Pragma", "no-cache");
	conn.setRequestProperty("Moteve-Sequence", "new");
	conn.setRequestProperty("Moteve-Token", token);
	conn.setRequestProperty("Moteve-DefaultGroup", defaultGroup);
	conn.setRequestProperty("Moteve-MediaFormat", mediaFormat);

	String seq = ConnectionUtils.receiveResponse(conn);
	Log.i(TAG, "Obtained sequence=" + seq);
	conn.disconnect();
	return seq;
    }

    public static void closeSequence(String sequenceId, String token) throws IOException {
	// TODO: ensure closing the sequence after all video streams are
	// finished
	Log.i(TAG, "Closing sequence " + sequenceId);
	String closure = "close_" + sequenceId;
	URL url = new URL(uploadUrl);
	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	conn.setDoInput(true);
	conn.setDoOutput(true);
	conn.setUseCaches(false);
	conn.setRequestMethod("POST");
	conn.setRequestProperty("Cache-Control", "no-cache");
	conn.setRequestProperty("Pragma", "no-cache");
	conn.setRequestProperty("Moteve-Sequence", closure);
	conn.setRequestProperty("Moteve-Token", token);

	String response = ConnectionUtils.receiveResponse(conn);
	Log.i(TAG, "Sequence " + sequenceId + " closed. Server response: "
		+ response);
    }

}
