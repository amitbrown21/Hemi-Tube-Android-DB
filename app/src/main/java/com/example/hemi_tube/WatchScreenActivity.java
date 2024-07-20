package com.example.hemi_tube;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ViewFlipper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hemi_tube.entities.CommentObj;
import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.entities.Video;
import com.example.hemi_tube.repository.RepositoryCallback;
import com.example.hemi_tube.viewmodel.CommentViewModel;
import com.example.hemi_tube.viewmodel.UserViewModel;
import com.example.hemi_tube.viewmodel.VideoViewModel;

public class WatchScreenActivity extends AppCompatActivity {

    private static final int EDIT_VIDEO_REQUEST = 1;
    private static final int PICK_THUMBNAIL_REQUEST = 2;

    private Video currentVideo = null;
    private User currentUser = null;
    private User owner = null;
    private int isLiked = 0; // 0 = nothing, 1 = liked, -1 = disliked
    private boolean isExpanded = false; // For description expansion
    private Uri thumbnailUri;

    private VideoViewModel videoViewModel;
    private UserViewModel userViewModel;
    private CommentViewModel commentViewModel;

    private VideoRecyclerViewAdapter videoAdapter;
    private CommentRecyclerViewAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_screen);

        videoViewModel = new ViewModelProvider(this).get(VideoViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        commentViewModel = new ViewModelProvider(this).get(CommentViewModel.class);

        handleIntent(getIntent());
        setupFlipper();
        setupListenersAndData();
        setupEditButton();
    }

    private void handleIntent(Intent intent) {
        String videoId = intent.getStringExtra("videoId");
        String currentUserId = intent.getStringExtra("currentUserId");

        videoViewModel.getVideoById(videoId).observe(this, video -> {
            if (video != null) {
                currentVideo = video;
                updateUI();
                loadOwner();
                loadComments();
            }
        });

        if (currentUserId != null) {
            userViewModel.getUserById(currentUserId).observe(this, user -> {
                currentUser = user;
                updateUI();
            });
        }
    }

    private void loadOwner() {
        if (currentVideo != null) {
            userViewModel.getUserById(currentVideo.getOwner().getId()).observe(this, user -> {
                owner = user;
                updateUI();
            });
        }
    }

    private void loadComments() {
        if (currentVideo != null) {
            commentViewModel.getCommentsForVideo(currentVideo.getId()).observe(this, comments -> {
                if (commentAdapter == null) {
                    RecyclerView commRecyclerView = findViewById(R.id.commentSection);
                    commentAdapter = new CommentRecyclerViewAdapter(this, comments, userViewModel, commentViewModel, currentVideo, videoViewModel, currentUser);
                    commRecyclerView.setAdapter(commentAdapter);
                    commRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                } else {
                    commentAdapter.updateComments(comments);
                }
            });
        }
    }

    private void setProfilePicture(ImageView imageView, String picturePath) {
        if (picturePath == null) {
            imageView.setImageResource(R.drawable.profile);
        } else if (picturePath.startsWith("content://")) {
            try {
                imageView.setImageURI(Uri.parse(picturePath));
            } catch (SecurityException e) {
                Log.e("WatchScreenActivity", "No access to content URI for profile picture", e);
                imageView.setImageResource(R.drawable.profile);
            }
        } else {
            int resourceId = getResources().getIdentifier(picturePath, "drawable", getPackageName());
            imageView.setImageResource(resourceId != 0 ? resourceId : R.drawable.profile);
        }
    }

    private void setupFlipper() {
        RecyclerView vidRecyclerView = findViewById(R.id.video_layout);
        videoViewModel.getAllVideos().observe(this, videos -> {
            videoAdapter = new VideoRecyclerViewAdapter(this, videos, userViewModel, videoViewModel, currentUser);
            vidRecyclerView.setAdapter(videoAdapter);
            vidRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        });

        setupFlipperAnimation();
    }

    private void setupFlipperAnimation() {
        Animation slideInFromTop = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_top);
        Animation slideOutToBottom = AnimationUtils.loadAnimation(this, R.anim.slide_out_to_bottom);
        Animation slideInFromBottom = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_bottom);
        Animation slideOutToTop = AnimationUtils.loadAnimation(this, R.anim.slide_out_to_top);

        Button commentButton = findViewById(R.id.commentButton);
        ViewFlipper viewFlipper = findViewById(R.id.viewFlipper);

        commentButton.setOnClickListener(v -> {
            if (viewFlipper.getDisplayedChild() == 0) {
                viewFlipper.setInAnimation(slideInFromTop);
                viewFlipper.setOutAnimation(slideOutToBottom);
                viewFlipper.showNext();
                commentButton.setText(R.string.show_recommendations);
            } else {
                viewFlipper.setInAnimation(slideInFromBottom);
                viewFlipper.setOutAnimation(slideOutToTop);
                viewFlipper.showPrevious();
                commentButton.setText(R.string.open_comments);
            }
        });
    }

    private void setupListenersAndData() {
        ImageButton likeButton = findViewById(R.id.like);
        likeButton.setOnClickListener(v -> onLike());

        ImageButton dislikeButton = findViewById(R.id.dislike);
        dislikeButton.setOnClickListener(v -> onDislike());

        Button shareButton = findViewById(R.id.share);
        shareButton.setOnClickListener(v -> onShare());

        ImageButton submitComment = findViewById(R.id.send_comment);
        submitComment.setOnClickListener(v -> submitComment());

        Button expandDescription = findViewById(R.id.expand_description);
        expandDescription.setOnClickListener(v -> expandDescription());

        View descriptionLayout = findViewById(R.id.videoDescription);
        descriptionLayout.setVisibility(View.GONE);
    }

    private void onLike() {
        if (currentVideo != null) {
            videoViewModel.incrementLikes(currentVideo.getId());
        }
    }

    private void onDislike() {
        if (currentVideo != null) {
            videoViewModel.incrementDislikes(currentVideo.getId());
        }
    }

    private void submitComment() {
        EditText commentTextField = findViewById(R.id.comment_text_field);
        String newCommentBody = commentTextField.getText().toString();

        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to comment", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentVideo == null) {
            Toast.makeText(this, "Error: Video not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        CommentObj newComment = new CommentObj(currentVideo.getId(), currentUser.getUsername(), newCommentBody);
        commentViewModel.createComment(currentUser.getId(), currentVideo.getId(), newComment, new RepositoryCallback<CommentObj>() {
            @Override
            public void onSuccess(CommentObj result) {
                runOnUiThread(() -> {
                    commentTextField.setText("");
                    Toast.makeText(WatchScreenActivity.this, "Comment added successfully", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(WatchScreenActivity.this, "Failed to add comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void onShare() {
        if (currentVideo != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, currentVideo.getTitle());
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        }
    }

    private void expandDescription() {
        View descriptionLayout = findViewById(R.id.videoDescription);
        Animation slideInFromTop = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_top);
        Animation slideOutToTop = AnimationUtils.loadAnimation(this, R.anim.slide_out_to_top);

        if (isExpanded) {
            descriptionLayout.startAnimation(slideOutToTop);
            descriptionLayout.setVisibility(View.GONE);
        } else {
            descriptionLayout.startAnimation(slideInFromTop);
            descriptionLayout.setVisibility(View.VISIBLE);
        }
        isExpanded = !isExpanded;
    }

    private void setupEditButton() {
        Button editButton = findViewById(R.id.editButton);
        editButton.setVisibility(currentUser != null && currentVideo != null && currentUser.getId().equals(currentVideo.getOwner()) ? View.VISIBLE : View.GONE);
        editButton.setOnClickListener(v -> showEditDialog());
    }

    private void showEditDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View editVideoView = inflater.inflate(R.layout.dialog_edit_video, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(editVideoView);

        EditText editTitle = editVideoView.findViewById(R.id.editTitle);
        EditText editDescription = editVideoView.findViewById(R.id.editDescription);
        ImageButton selectThumbnailButton = editVideoView.findViewById(R.id.selectThumbnailButton);
        Button saveButton = editVideoView.findViewById(R.id.saveButton);

        editTitle.setText(currentVideo.getTitle());
        editDescription.setText(currentVideo.getDescription());
        thumbnailUri = Uri.parse(currentVideo.getThumbnail());

        selectThumbnailButton.setOnClickListener(v -> selectThumbnail());

        AlertDialog dialog = builder.create();
        dialog.show();

        saveButton.setOnClickListener(v -> {
            String newTitle = editTitle.getText().toString();
            String newDescription = editDescription.getText().toString();

            if (newTitle.isEmpty() || newDescription.isEmpty() || thumbnailUri == null) {
                Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show();
            } else {
                currentVideo.setTitle(newTitle);
                currentVideo.setDescription(newDescription);
                currentVideo.setThumbnail(thumbnailUri.toString());
                videoViewModel.updateVideo(currentVideo, new RepositoryCallback<Video>() {
                    @Override
                    public void onSuccess(Video result) {
                        runOnUiThread(() -> {
                            dialog.dismiss();
                            updateUI();
                            Toast.makeText(WatchScreenActivity.this, "Video updated successfully", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(WatchScreenActivity.this, "Failed to update video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
        });
    }

    private void selectThumbnail() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_THUMBNAIL_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_THUMBNAIL_REQUEST) {
                thumbnailUri = data.getData();
                if (thumbnailUri != null) {
                    grantUriPermission(getPackageName(), thumbnailUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            }
        }
    }

    private void updateUI() {
        if (currentVideo == null || owner == null) {
            Log.e("updateUI", "currentVideo or owner is null");
            return;
        }

        TextView tvTitle = findViewById(R.id.videoTitle);
        TextView tvVideoData = findViewById(R.id.videoData);
        TextView tvOwnerName = findViewById(R.id.comment_username);
        TextView tvSubscribers = findViewById(R.id.ownerSubs);
        TextView tvLikesNumber = findViewById(R.id.likes_number);
        TextView tvVideoDescription = findViewById(R.id.videoDescription);
        ImageView ownerProfilePicture = findViewById(R.id.owner_profile_picture);

        tvTitle.setText(currentVideo.getTitle());
        tvOwnerName.setText(owner.getUsername());
        tvSubscribers.setText(Utils.formatNumber(owner.getSubscribers()));
        tvVideoData.setText(currentVideo.getViews() + " views • " + currentVideo.getDate());
        tvLikesNumber.setText(Utils.likeBalance(currentVideo));
        tvVideoDescription.setText(currentVideo.getDescription());

        setProfilePicture(ownerProfilePicture, owner.getProfilePicture());

        ownerProfilePicture.setOnClickListener(v -> openChannelActivity(owner.getId()));


        setupVideoPlayer();
        setupEditButton();
    }

    private void setupVideoPlayer() {
        VideoView videoView = findViewById(R.id.videoView);
        MediaController mediaController = new MediaController(this);
        mediaController.setMediaPlayer(videoView);
        videoView.setMediaController(mediaController);

        Uri videoUri = Uri.parse(currentVideo.getUrl());
        videoView.setVideoURI(videoUri);
        videoView.start();
    }
    private void openChannelActivity(String userId) {
        Intent intent = new Intent(this, ChannelActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Any cleanup code if needed
    }
}