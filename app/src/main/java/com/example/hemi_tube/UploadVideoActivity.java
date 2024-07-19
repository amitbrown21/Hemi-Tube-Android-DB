package com.example.hemi_tube;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.entities.Video;
import com.example.hemi_tube.viewmodel.VideoViewModel;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadVideoActivity extends AppCompatActivity {

    private static final int PICK_VIDEO_REQUEST = 1;
    public static final int PICK_THUMBNAIL_REQUEST = 2;
    private static final String TAG = "UploadVideoActivity";

    private EditText title;
    private EditText description;
    private Button selectVideoButton;
    private Button selectThumbnailButton;
    private Button uploadButton;

    private Uri videoUri;
    private Uri thumbnailUri;

    private User currentUser;
    private VideoViewModel videoViewModel;

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

        videoViewModel = new ViewModelProvider(this).get(VideoViewModel.class);

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
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
        startActivityForResult(intent, PICK_VIDEO_REQUEST);
    }

    private void selectThumbnail() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_THUMBNAIL_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri selectedUri = data.getData();
            if (selectedUri != null) {
                try {
                    int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getContentResolver().takePersistableUriPermission(selectedUri, takeFlags);

                    if (requestCode == PICK_VIDEO_REQUEST) {
                        videoUri = selectedUri;
                    } else if (requestCode == PICK_THUMBNAIL_REQUEST) {
                        thumbnailUri = selectedUri;
                    }
                } catch (SecurityException e) {
                    Log.e(TAG, "Failed to take persistable URI permission", e);
                    Toast.makeText(this, "Failed to access the selected file", Toast.LENGTH_SHORT).show();
                }
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
            // Prepare the file parts
            File videoFile = new File(FileUtil.getPathFromUri(this, videoUri));
            RequestBody videoRequestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(videoUri)), videoFile);
            MultipartBody.Part videoBody = MultipartBody.Part.createFormData("video", videoFile.getName(), videoRequestFile);

            File thumbnailFile = new File(FileUtil.getPathFromUri(this, thumbnailUri));
            RequestBody thumbnailRequestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(thumbnailUri)), thumbnailFile);
            MultipartBody.Part thumbnailBody = MultipartBody.Part.createFormData("thumbnail", thumbnailFile.getName(), thumbnailRequestFile);

            // Prepare other parts
            RequestBody titlePart = RequestBody.create(MultipartBody.FORM, videoTitle);
            RequestBody descriptionPart = RequestBody.create(MultipartBody.FORM, videoDescription);
            RequestBody userIdPart = RequestBody.create(MultipartBody.FORM, currentUser.getId());

            videoViewModel.uploadVideo(userIdPart, titlePart, descriptionPart, videoBody, thumbnailBody).enqueue(new Callback<Video>() {
                @Override
                public void onResponse(Call<Video> call, Response<Video> response) {
                    if (response.isSuccessful()) {
                        Video result = response.body();
                        Log.d(TAG, "Video uploaded successfully: " + result.getId());
                        Toast.makeText(UploadVideoActivity.this, "Video uploaded successfully", Toast.LENGTH_SHORT).show();
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("uploaded_video_id", result.getId());
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        Log.e(TAG, "Failed to upload video: " + response.message());
                        Toast.makeText(UploadVideoActivity.this, "Failed to upload video: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Video> call, Throwable t) {
                    Log.e(TAG, "Failed to upload video: " + t.getMessage(), t);
                    Toast.makeText(UploadVideoActivity.this, "Failed to upload video: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
