package com.example.hemi_tube;

import android.graphics.Outline;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        profileImage = findViewById(R.id.profileImage);

        // Make the ImageView circular
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

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        videoViewModel = new ViewModelProvider(this).get(VideoViewModel.class);

        String userId = getIntent().getStringExtra("userId");
        if (userId == null || userId.isEmpty()) {
            finish();
            return;
        }

        loadChannelData(userId);
    }

    private void loadChannelData(String userId) {
        userViewModel.getUserById(userId).observe(this, user -> {
            if (user != null) {
                channelUser = user;
                updateUserUI(user);
                loadUserVideos(userId);
            } else {
                finish();
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
        subscribersText.setText(String.format("%d subscribers", user.getSubscribers()));

        String profilePicturePath = user.getProfilePicture();
        if (profilePicturePath != null && profilePicturePath.startsWith("content://")) {
            profileImage.setImageURI(Uri.parse(profilePicturePath));
        } else if (profilePicturePath != null && profilePicturePath.contains("/")) {
            int resourceId = getResources().getIdentifier(profilePicturePath.substring(profilePicturePath.lastIndexOf("/") + 1, profilePicturePath.lastIndexOf(".")), "drawable", getPackageName());
            profileImage.setImageResource(resourceId != 0 ? resourceId : R.drawable.profile);
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