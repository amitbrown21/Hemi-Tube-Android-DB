package com.example.hemi_tube;

import android.graphics.Outline;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.entities.Video;
import com.example.hemi_tube.viewmodel.UserViewModel;
import com.example.hemi_tube.viewmodel.VideoViewModel;

import java.util.List;

public class ChannelActivity extends AppCompatActivity {
    private ImageView profileImage;
    private TextView usernameText;
    private TextView subscribersText;
    private RecyclerView videosRecyclerView;
    private VideoRecyclerViewAdapter videoAdapter;

    private UserViewModel userViewModel;
    private VideoViewModel videoViewModel;

    private User channelUser;
    private User currentUser; // Add this line
    private boolean dataLoaded = false;
    private String currentProfilePicturePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        initializeViews();
        setupViewModels();

        String userId = getIntent().getStringExtra("currentUserId");
        if (userId == null || userId.isEmpty()) {
            finish();
            return;
        }

        Log.d("Shon in channel", "user id recived: "+userId);

        userViewModel.getUserById(userId).observe(this, user -> {
            if (user != null) {
                currentUser = user; // Set the currentUser
                Log.d("Shon in channel", "user received: " + currentUser);
                loadChannelData(userId); // Load channel data after setting currentUser
            } else {
                Log.d("Shon in channel", "Failed to retrieve current user");
            }
        });

        //Log.d("Shon in channel", "user recived: "+currentUser);



        loadChannelData(userId);
    }

    private void initializeViews() {
        profileImage = findViewById(R.id.profileImage);
        profileImage.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });
        profileImage.setClipToOutline(true);
        usernameText = findViewById(R.id.usernameText);
        subscribersText = findViewById(R.id.subscribersText);
        videosRecyclerView = findViewById(R.id.videosRecyclerView);
    }

    private void setupViewModels() {
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        videoViewModel = new ViewModelProvider(this).get(VideoViewModel.class);
    }

    private void loadChannelData(String userId) {
        if (dataLoaded) return;

        userViewModel.getUserById(userId).observe(this, user -> {
            if (user != null && !dataLoaded) {
                channelUser = user;
                currentProfilePicturePath = user.getProfilePicture();
                updateUserUI(user);
                loadUserVideos(userId);
                dataLoaded = true;
            } else if (user != null && !currentProfilePicturePath.equals(user.getProfilePicture())) {
                currentProfilePicturePath = user.getProfilePicture();
                updateProfilePicture(user.getProfilePicture());
            }
        });
    }

    private void loadUserVideos(String userId) {
        videoViewModel.getVideosForUser(userId).observe(this, videos -> {
            if (videos != null) {
                updateVideosUI(videos);
            }
        });
    }

    private void updateUserUI(User user) {
        usernameText.setText(user.getUsername());
        subscribersText.setText(String.format("%s subscribers", user.getSubscribers()));
        updateProfilePicture(user.getProfilePicture());
    }

    private void updateProfilePicture(String profilePicturePath) {
        if (profilePicturePath != null && !profilePicturePath.isEmpty()) {
            String imageUrl = "http://10.0.2.2:3000/" + profilePicturePath.replace("\\", "/");
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .circleCrop()
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.profile);
        }
    }

    private void updateVideosUI(List<Video> videos) {
        if (videoAdapter == null) {
            videoAdapter = new VideoRecyclerViewAdapter(this, videos, userViewModel, videoViewModel, null);
            videosRecyclerView.setAdapter(videoAdapter);
            videosRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        } else {
            videoAdapter.updateList(videos);
        }
    }
}
