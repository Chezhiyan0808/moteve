package com.moteve.mca;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class VideoCapturer extends Activity implements SurfaceHolder.Callback {

	public static final String FILE_BASE = "/sdcard/m-video_"; // TODO: replace
	// with temp
	// files
	private static final String MEDIA_TYPE = "3GPP";
	private static final String TAG = "Moteve_VideoCapturer";
	public static final int MEDIA_DURATION = 5000;
	private SurfaceView surfaceView;
	private SurfaceHolder holder;
	private MediaRecorder recorder;
	// private VideoUploader uploader = new VideoUploader();
	private int part = 0;
	private String sequenceId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_capture);

		surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
		holder = surfaceView.getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		setSequenceId();
		recorder = new MediaRecorder();

		Button cancelButton = (Button) findViewById(R.id.stop);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		recorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
			@Override
			public void onInfo(MediaRecorder mr, int what, int extra) {
				switch (what) {
				case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
					swapFiles();
				}
			}
		});
	}

	/**
	 * Remember current part no., prepare() new recorder and in the meantime the
	 * current file should be closed. Then upload it.
	 */
	private void swapFiles() {
		int oldPart = part;
		part++;
		Log.d(TAG, "Swapping media files. old=" + buildFileName(oldPart) + ", new=" + buildFileName(part));
		prepareRecorder(MEDIA_DURATION, buildFileName(part));
		recorder.start();
		uploadFile(oldPart);
	}

	private void setSequenceId() {
		try {
			Toast.makeText(this, "Obtaining sequence ID from server", Toast.LENGTH_SHORT).show();
			this.sequenceId = VideoUploader.obtainSequenceId();
			Toast.makeText(this, "Sequence ID received", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			Log.e(TAG, "Error getting sequence ID from server: " + e.getMessage(), e);
			Toast.makeText(this, "Error getting sequence ID from server: " + e.getMessage(), Toast.LENGTH_LONG).show();
			finish();
		}
	}

	private void prepareRecorder(int maxDuration, String outputFile) {
		Log.i(TAG, "Preparing MediaRecorder; outputFile=" + outputFile);
		try {
			recorder.reset();

			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

			recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H263);
			recorder.setMaxDuration(maxDuration);

			// recorder.setVideoFrameRate(rate)
			// recorder.setVideoSize(width, height)

			recorder.setOutputFile(outputFile);
			recorder.setPreviewDisplay(holder.getSurface());
			recorder.prepare();
		} catch (Exception e) {
			Log.e(TAG, "Error preparing MediaRecorder: " + e.getMessage(), e);
			Toast.makeText(this, "Error preparing MediaRecorder: " + e.getMessage(), Toast.LENGTH_LONG).show();
			finish();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// recorder.stop(); // don't call stop - it already might be stopped
		// after maxDuration
		recorder.reset();
		File f = new File(buildFileName(part));
		long fileLength = f.length();
		Log.i(TAG, "Recorded video file size: " + f.length() + " B");
		// uploader.stopRecording();
		if (fileLength > 0) {
			uploadFile(part);
		}
		VideoUploader.closeSequence(sequenceId);
	}

	private void uploadFile(int part) {
		Log.d(TAG, "uploadFile part=" + part);
		VideoUploader uploader = new VideoUploader(buildFileName(part), sequenceId, String.valueOf(part), MEDIA_TYPE,
				this);
		(new Thread(uploader)).start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		recorder.reset();
		recorder.release();
		recorder = null;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d(TAG, "surfaceChanged called");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated called");
		prepareRecorder(MEDIA_DURATION, buildFileName(part));
		recorder.start();
		// uploader.startRecording();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed called");
	}

	private String buildFileName(int part) {
		return FILE_BASE + part + ".3gp";
	}

}
