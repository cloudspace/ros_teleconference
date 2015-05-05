package com.cloudspace.rosteleconference;

import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.cloudspace.rosjava_video.Constants;
import com.cloudspace.rosjava_video.VideoPublisher;
import com.cloudspace.rosjava_video.v21.CameraOps;

import org.ros.address.InetAddressFactory;
import org.ros.android.NodeMainExecutorService;
import org.ros.android.RosActivity;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.net.URI;

/**
 * Created by r2DoesInc (r2doesinc@futurehax.com) on 4/7/15.
 */
public class MainActivity extends RosActivity {

    boolean isActive = false;
    public boolean isActive() {
        return isActive;
    }
    public AudioManager audioManager;
    public SurfaceView mSurfaceView;
    public SurfaceHolder mSurfaceHolder;
    VideoPublisher vPub;

    Handler readyHandler = new Handler();

    public MainActivity(String notificationTicker, String notificationTitle) {
        super(notificationTicker, notificationTitle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mSurfaceView = (SurfaceView) findViewById(R.id.surface);
        mSurfaceHolder = mSurfaceView.getHolder();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, NodeMainExecutorService.class);
        intent.setAction(Constants.ACTION_START_NODE_RUNNER_SERVICE);
        stopService(intent);
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        NodeConfiguration config = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostName())
                .setMasterUri(getMasterUri());
        CameraOps.ErrorDisplayer displayer = new CameraOps.ErrorDisplayer() {
            @Override
            public void showErrorDialog(String errorMessage) {
                Log.d("ERROR", errorMessage);
            }

            @Override
            public String getErrorString(CameraAccessException e) {
                return e.getMessage();
            }
        };


        vPub = new VideoPublisher("tele", (CameraManager) getSystemService(Context.CAMERA_SERVICE), readyHandler, displayer, mSurfaceHolder);
        nodeMainExecutor.execute(vPub, config);
    }

    public MainActivity(String notificationTicker, String notificationTitle, URI customMasterUri) {
        super(notificationTicker, notificationTitle, customMasterUri);
    }

    public MainActivity() {
        super("Ardrobot is running.", "Ardrobot", URI.create("http://10.100.4.65:11311"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActive = true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (vPub != null) {
            vPub.stop();
        }
        finish();

    }
}
