package com.example.hemi_tube;

import android.content.Intent;
import android.content.res.ColorStateList;
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
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ViewFlipper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
    private boolean isLiked = false; // Track if the video is liked by the user
    private boolean isDisliked = false; // Track if the video is disliked by the user
    private boolean isExpanded = false; // For description expansion
    private Uri thumbnailUri;
    private VideoView videoView;
    private VideoViewModel videoViewModel;
    private UserViewModel userViewModel;
    private CommentViewModel commentViewModel;

    private ImageButton likeButton;
    private ImageButton dislikeButton;

    private VideoRecyclerViewAdapter videoAdapter;
    private CommentRecyclerViewAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_screen);

        videoViewModel = new ViewModelProvider(this).get(VideoViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        commentViewModel = new ViewModelProvider(this).get(CommentViewModel.class);
        likeButton = findViewById(R.id.like);
        dislikeButton = findViewById(R.id.dislike);

        setupListenersAndData();
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
                loadOwner();
                loadComments();
                updateUI(); // Ensure updateUI is called after loading owner
            } else {
                Toast.makeText(this, "Video not found", Toast.LENGTH_SHORT).show();
                finish(); // Close the activity if the video is not found
            }
        });

        if (currentUserId != null) {
            userViewModel.getUserById(currentUserId).observe(this, user -> {
                currentUser = user;
                updateUI(); // Update UI when currentUser is loaded
            });
        }
    }

    private void loadOwner() {
        if (currentVideo != null) {
            userViewModel.getUserById(currentVideo.getOwner().getId()).observe(this, user -> {
                owner = user;
                updateUI(); // Update UI when owner is loaded
            });
        }
    }

    private void loadComments() {
        if (currentVideo != null && currentUser != null) {
            commentViewModel.getCommentsForVideo(currentUser.getId(), currentVideo.getId()).observe(this, comments -> {
                if (comments != null) {
                    if (commentAdapter == null) {
                        RecyclerView commRecyclerView = findViewById(R.id.commentSection);
                        commentAdapter = new CommentRecyclerViewAdapter(this, comments, userViewModel, commentViewModel, currentVideo, videoViewModel, currentUser);
                        commRecyclerView.setAdapter(commentAdapter);
                        commRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                    } else {
                        commentAdapter.updateComments(comments);
                    }
                } else {
                    Toast.makeText(this, "Error loading comments", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Add this method to handle profile picture clicks
    public void onProfilePictureClick(View view) {
        if (currentVideo != null && currentVideo.getOwner() != null) {
            openChannelActivity(currentVideo.getOwner().getId());
        }
    }

    // Ensure the scale type is set correctly
    private void setProfilePicture(ImageView imageView, String picturePath) {
        Log.d("WatchScreenActivity", "Profile Picture Path: " + picturePath);
        if (picturePath != null && !picturePath.isEmpty()) {
            String imageUrl = "http://10.0.2.2:3000/" + picturePath.replace("\\", "/");
            Log.d("WatchScreenActivity", "Profile Picture URL: " + imageUrl);

            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.profile)
                    .into(imageView);
        } else {
            Log.d("WatchScreenActivity", "Profile picture path is null or empty");
            imageView.setImageResource(R.drawable.profile);
        }

        // Add OnClickListener to the profile picture
        imageView.setOnClickListener(v -> {
            if (currentVideo != null && currentVideo.getOwner() != null) {
                openChannelActivity(currentVideo.getOwner().getId());
            }
        });
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
        likeButton.setOnClickListener(v -> onLike());
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
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to like", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentVideo != null) {
            if (isLiked) {
                videoViewModel.decrementLikes(currentVideo.getId());
                isLiked = false;
            } else {
                if (isDisliked) {
                    videoViewModel.decrementDislikes(currentVideo.getId());
                    isDisliked = false;
                }
                videoViewModel.incrementLikes(currentVideo.getId());
                isLiked = true;
            }
            updateLikeDislikeUI();
        }
    }

    private void onDislike() {
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to dislike", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentVideo != null) {
            if (isDisliked) {
                videoViewModel.decrementDislikes(currentVideo.getId());
                isDisliked = false;
            } else {
                if (isLiked) {
                    videoViewModel.decrementLikes(currentVideo.getId());
                    isLiked = false;
                }
                videoViewModel.incrementDislikes(currentVideo.getId());
                isDisliked = true;
            }
            updateLikeDislikeUI();
        }
    }

    private void updateLikeDislikeUI() {
        likeButton.setImageResource(isLiked ? R.drawable.thumbs_up_filled : R.drawable.thumbs_up);
        dislikeButton.setImageResource(isDisliked ? R.drawable.thumbs_down_filled : R.drawable.thumbs_down);

        // Update like count
        TextView tvLikesNumber = findViewById(R.id.likes_number);
        tvLikesNumber.setText(Utils.likeBalance(currentVideo));
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

        if (newCommentBody.trim().isEmpty()) {
            Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        CommentObj newComment = new CommentObj(
                currentVideo.getId(),
                currentUser.getUsername(),
                newCommentBody,
                currentUser.getProfilePicture(),
                currentUser.getId()
        );

        commentViewModel.createComment(currentUser.getId(), currentVideo.getId(), newComment, new RepositoryCallback<CommentObj>() {
            @Override
            public void onSuccess(CommentObj result) {
                runOnUiThread(() -> {
                    commentTextField.setText("");
                    Toast.makeText(WatchScreenActivity.this, "Comment added successfully", Toast.LENGTH_SHORT).show();
                    // Refresh comments
                    loadComments();
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
        editButton.setVisibility(currentUser != null && currentVideo != null && currentUser.getId().equals(currentVideo.getOwner().getId()) ? View.VISIBLE : View.GONE);
        editButton.setOnClickListener(v -> showEditDialog());
    }

    private void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_video, null);
        builder.setView(dialogView);

        EditText editTitle = dialogView.findViewById(R.id.editTitle);
        EditText editDescription = dialogView.findViewById(R.id.editDescription);
        ImageButton selectThumbnailButton = dialogView.findViewById(R.id.selectThumbnailButton);
        Button saveButton = dialogView.findViewById(R.id.saveButton);

        // Add a cancel button to the layout
        Button cancelButton = new Button(this);
        cancelButton.setText("Cancel");
        cancelButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button_color)));
        cancelButton.setTextColor(ContextCompat.getColor(this, R.color.button_text_color));
        ((LinearLayout) dialogView).addView(cancelButton);

        editTitle.setText(currentVideo.getTitle());
        editDescription.setText(currentVideo.getDescription());
        thumbnailUri = Uri.parse(currentVideo.getThumbnail());

        AlertDialog dialog = builder.create();

        selectThumbnailButton.setOnClickListener(v -> selectThumbnail());

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

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
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
        if (currentVideo == null || currentVideo.getOwner() == null || owner == null) {
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
        //ImageView thumbnailImageView = findViewById(R.id.videoThumbnail);

        tvTitle.setText(currentVideo.getTitle());
        tvOwnerName.setText(currentVideo.getOwner().getUsername());
        tvSubscribers.setText(owner.getSubscribers() + " subscribers");
        tvVideoData.setText(currentVideo.getViews() + " views • " + currentVideo.getDate());
        tvLikesNumber.setText(Utils.likeBalance(currentVideo));
        tvVideoDescription.setText(currentVideo.getDescription());

        setProfilePicture(ownerProfilePicture, owner.getProfilePicture());
        //setThumbnail(thumbnailImageView, currentVideo.getThumbnail());

        ownerProfilePicture.setOnClickListener(v -> openChannelActivity(currentVideo.getOwner().getId()));

        setupVideoPlayer();
        setupEditButton();
        updateLikeDislikeUI();
        loadComments();
    }

    private void setupVideoPlayer() {
        videoView = findViewById(R.id.videoView);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        Uri videoUri = Uri.parse("http://10.0.2.2:3000/" + currentVideo.getUrl().replace("\\", "/"));
        videoView.setVideoURI(videoUri);

        videoView.setOnErrorListener((mp, what, extra) -> {
            Log.e("WatchScreenActivity", "Error playing video: " + what + ", " + extra);
            Toast.makeText(this, "Error playing video", Toast.LENGTH_SHORT).show();
            return true;
        });

        videoView.setOnPreparedListener(mp -> {
            mp.setOnVideoSizeChangedListener((mediaPlayer, width, height) -> {
                mediaController.setAnchorView(videoView);
            });
        });

        videoView.start();
    }

    private void openChannelActivity(String userId) {
        Intent intent = new Intent(this, ChannelActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null) {
            videoView.pause();
        }
    }
}
