package com.example.magicalscript.facefactor;

/**
 * Created by Abdellah on 28/09/2015.
 */

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class VideoWithSurfaceVw extends Activity{

    // Adapted from http://sandyandroidtutorials.blogspot.co.uk/2013/05/android-video-capture-tutorial.html


    private VideoRecordEvent videoRecordEvent;
    private Camera myCamera;
    private CameraPreview myCameraSurfaceView;
    private MediaRecorder mediaRecorder;

    Button myButton;
    SurfaceHolder surfaceHolder;
    boolean recording;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        recording = false;

        setContentView(R.layout.activity_video_with_surface_vw);
try{
    //Get Camera for preview

    myCamera = getCameraInstance();
    myCamera.release();
    myCamera.unlock();
    if(myCamera == null){
        Toast.makeText(VideoWithSurfaceVw.this,
                "Fail to get Camera",
                Toast.LENGTH_LONG).show();
        finish();
    }


    myCameraSurfaceView = new CameraPreview(this, myCamera);
    FrameLayout myCameraPreview = (FrameLayout)findViewById(R.id.videoview);
    myCameraPreview.addView(myCameraSurfaceView);

}catch(Exception e){
    e.printStackTrace();
    myCamera.unlock();
}


        myButton = (Button)findViewById(R.id.mybutton);
        myButton.setOnClickListener(myButtonOnClickListener);
        ///////////////////////////////////////////*******************************************************

        ///////////////////////////////////////////*******************************************************
    }
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.height / size.width;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;

            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }

    Button.OnClickListener myButtonOnClickListener
            = new Button.OnClickListener(){

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub

            try{
                if(recording){
                    // stop recording and release camera
                    mediaRecorder.stop();  // stop the recording
                    releaseMediaRecorder(); // release the MediaRecorder object

                    //Exit after saved
                    //finish();
                    myButton.setText("REC");
                    recording = false;

                    videoRecordEvent.VideoRecorded();
                }else{

                    //Release Camera before MediaRecorder start
                    releaseCamera();

                    if(!prepareMediaRecorder()){
                        Toast.makeText(VideoWithSurfaceVw.this,
                                "Fail in prepareMediaRecorder()!\n - Ended -",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }

                    mediaRecorder.start();
                    recording = true;
                    myButton.setText("STOP");
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }};

    private Camera getCameraInstance(){
        // TODO Auto-generated method stub
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
            //FrameLayout.LayoutParams trparams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            //((FrameLayout)(findViewById(R.id.surface_camera))).setLayoutParams(trparams);

        }
        catch (Exception e){
            e.printStackTrace();
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private String getFileName_CustomFormat() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }


    private boolean prepareMediaRecorder(){
        myCamera = getCameraInstance();
        mediaRecorder = new MediaRecorder();

        myCamera.unlock();
        mediaRecorder.setCamera(myCamera);

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));


        String VideoFileName="/sdcard/" + getFileName_CustomFormat() + ".mp4";
        MainActivity._property.put("VideoFileName",VideoFileName);

        mediaRecorder.setOutputFile(VideoFileName);
        //mediaRecorder.setOutputFile("/sdcard/myvideo1.mp4");
        mediaRecorder.setMaxDuration(60000); // Set max duration 60 sec.
        mediaRecorder.setMaxFileSize(50000000); // Set max file size 50M

        mediaRecorder.setPreviewDisplay(myCameraSurfaceView.getHolder().getSurface());

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            releaseMediaRecorder();
            return false;
        }
        return true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if y
        // ou are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = new MediaRecorder();
            myCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (myCamera != null){
            myCamera.release();        // release the camera for other applications
            myCamera = null;
        }
    }


}
interface VideoRecordEvent{
    public void VideoRecorded();
}