package com.example.mapintegration;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class AnimationsActivity extends AppCompatActivity {

    private VideoView videoView;
    private TextView placeholderText;
    private Button btnCPR, btnFracture, btnFirstAid, btnEmergency, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animations);

        // Initialize UI components
        videoView = findViewById(R.id.videoView);
        placeholderText = findViewById(R.id.textViewPlaceholder);

        btnCPR = findViewById(R.id.buttonCPR);
        btnFracture = findViewById(R.id.buttonFracture);
        btnFirstAid = findViewById(R.id.buttonFirstAid);
        btnEmergency = findViewById(R.id.buttonEmergency);
        btnBack = findViewById(R.id.buttonBack);

        // Set click listeners for video buttons
        btnCPR.setOnClickListener(view -> playVideo("choking"));
        btnFracture.setOnClickListener(view -> playVideo("fracture"));
        btnFirstAid.setOnClickListener(view -> playVideo("heatstroke"));
        btnEmergency.setOnClickListener(view -> playVideo("faint"));

        // Set click listener for back button
        btnBack.setOnClickListener(view -> finish());

        // Set media controller for video playback controls
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        // Set on completion listener
        videoView.setOnCompletionListener(mp -> {
            // Show placeholder when video finishes
            placeholderText.setVisibility(View.VISIBLE);
        });

        // Set on prepared listener
        videoView.setOnPreparedListener(mp -> {
            // Hide placeholder when video is ready
            placeholderText.setVisibility(View.GONE);
        });
    }

    private void playVideo(String videoName) {
        try {
            // Get the resource ID for the video file
            int resourceId = getResources().getIdentifier(
                    videoName, "raw", getPackageName());

            if (resourceId == 0) {
                Toast.makeText(this, "Video not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a Uri for the video resource
            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + resourceId);

            // Set the video URI and start playing
            videoView.setVideoURI(uri);
            videoView.start();

            // Hide placeholder text
            placeholderText.setVisibility(View.GONE);

        } catch (Exception e) {
            Toast.makeText(this, "Error playing video: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}