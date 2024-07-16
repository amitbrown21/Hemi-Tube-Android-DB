package com.example.hemi_tube;

import android.graphics.Outline;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hemi_tube.dao.UserDao;
import com.example.hemi_tube.dao.VideoDao;
import com.example.hemi_tube.database.AppDatabase;
import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.entities.Video;

import java.util.List;

public class ChannelActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView usernameText;
    private TextView subscribersText;
    private RecyclerView videosRecyclerView;
    private VideoRecyclerViewAdapter videoAdapter;

    private AppDatabase database;
    private UserDao userDao;
    private VideoDao videoDao;

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

        database = AppDatabase.getInstance(this);
        userDao = database.userDao();
        videoDao = database.videoDao();

        int userId = getIntent().getIntExtra("userId", -1);
        if (userId == -1) {
            finish();
            return;
        }

        loadChannelData(userId);
    }

    private void loadChannelData(int userId) {
        new Thread(() -> {
            channelUser = userDao.getUserById(userId);
            List<Video> userVideos = videoDao.getVideosForUser(userId);

            runOnUiThread(() -> {
                if (channelUser != null) {
                    updateUI(channelUser, userVideos);
                } else {
                    finish();
                }
            });
        }).start();
    }

    private void updateUI(User user, List<Video> videos) {
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

        videoAdapter = new VideoRecyclerViewAdapter(this, videos, null, null, videos);
        videosRecyclerView.setAdapter(videoAdapter);
        videosRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}