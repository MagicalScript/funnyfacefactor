package com.example.magicalscript.facefactor;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;

public class VideoPlayer extends Activity{
	VideoView videoView;
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		//Create a VideoView widget in the layout file
		//use setContentView method to set content of the activity to the layout file which contains videoView
		this.setContentView(R.layout.videoplayer);
		
        videoView = (VideoView)this.findViewById(R.id.videoView);
        
        //add controls to a MediaPlayer like play, pause.
        MediaController mc = new MediaController(this);
        videoView.setMediaController(mc);
        
        //Set the path of Video or URI
        videoView.setVideoURI(Uri.parse(MainActivity._property.get("VideoFile")));
        videoView.start();
      //Set the focus
        videoView.requestFocus();
		
    }

    public void shareclk(View v){
        Toast.makeText(this, "share record video", Toast.LENGTH_SHORT).show();
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("video/*");
        Uri shareBody = Uri.fromFile(new File(MainActivity._property.get("VideoFile")));
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }
}
