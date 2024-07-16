package com.example.hemi_tube;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hemi_tube.database.AppDatabase;
import com.example.hemi_tube.dao.VideoDao;
import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.entities.Video;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UploadVideoActivity extends AppCompatActivity {

    private static final int PICK_VIDEO_REQUEST = 1;
    public static final int PICK_THUMBNAIL_REQUEST = 2;

    private EditText title;
    private EditText description;
    private Button selectVideoButton;
    private Button selectThumbnailButton;
    private Button uploadButton;

    private Uri videoUri;
    private Uri thumbnailUri;

    private User currentUser;
    private AppDatabase database;
    private VideoDao videoDao;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload_video);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize database, DAO, and executor
        database = AppDatabase.getInstance(this);
        videoDao = database.videoDao();
        executorService = Executors.newSingleThreadExecutor();

        // Receive currentUser from the intent
        Intent intent = getIntent();
        currentUser = (User) intent.getSerializableExtra("currentUser");

        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        selectVideoButton = findViewById(R.id.selectVideoButton);
        selectThumbnailButton = findViewById(R.id.selectThumbnailButton);
        uploadButton = findViewById(R.id.uploadButton);

        selectVideoButton.setOnClickListener(v -> selectVideo());
        selectThumbnailButton.setOnClickListener(v -> selectThumbnail());
        uploadButton.setOnClickListener(v -> uploadVideo());
    }

    private void selectVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_VIDEO_REQUEST);
    }

    private void selectThumbnail() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_THUMBNAIL_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_VIDEO_REQUEST) {
                videoUri = data.getData();
                grantUriPermission(getPackageName(), videoUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else if (requestCode == PICK_THUMBNAIL_REQUEST) {
                thumbnailUri = data.getData();
                grantUriPermission(getPackageName(), thumbnailUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
    }

    private void uploadVideo() {
        String videoTitle = title.getText().toString();
        String videoDescription = description.getText().toString();

        if (videoTitle.isEmpty() || videoDescription.isEmpty() || videoUri == null || thumbnailUri == null) {
            Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show();
        } else if (currentUser == null) {
            Toast.makeText(this, "Please Sign in to upload a video", Toast.LENGTH_SHORT).show();
        } else {
            Video newVideo = new Video(
                    0, // ID will be auto-generated
                    videoUri.toString(),
                    videoTitle,
                    currentUser.getId(),
                    new Date().toString(), // Current date
                    0, // Initial views
                    0, // Initial likes
                    0, // Initial dislikes
                    thumbnailUri.toString(),
                    videoDescription,
                    "00:00", // Duration placeholder
                    new ArrayList<>() // Initialize comments list
            );

            insertVideo(newVideo);
        }
    }

    private void insertVideo(Video video) {
        executorService.execute(() -> {
            long videoId = videoDao.insert(video);
            runOnUiThread(() -> {
                if (videoId > 0) {
                    Toast.makeText(this, "Video uploaded successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "Failed to upload video", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}