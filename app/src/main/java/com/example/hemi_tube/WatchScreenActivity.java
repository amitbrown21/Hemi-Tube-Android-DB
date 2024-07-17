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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hemi_tube.database.AppDatabase;
import com.example.hemi_tube.dao.UserDao;
import com.example.hemi_tube.dao.VideoDao;
import com.example.hemi_tube.dao.CommentDao;
import com.example.hemi_tube.entities.CommentObj;
import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.entities.Video;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WatchScreenActivity extends AppCompatActivity {

    private static final int EDIT_VIDEO_REQUEST = 1;
    private static final int PICK_THUMBNAIL_REQUEST = 2;

    private Video currentVideo = null;
    private User currentUser = null;
    private User owner = null;
    private int isLiked = 0; // 0 = nothing, 1 = liked, -1 = disliked
    private boolean isExpanded = false; // For description expansion
    private Uri thumbnailUri;

    private AppDatabase database;
    private UserDao userDao;
    private VideoDao videoDao;
    private CommentDao commentDao;
    private ExecutorService executorService;

    private VideoRecyclerViewAdapter videoAdapter;
    private CommentRecyclerViewAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_screen);

        database = AppDatabase.getInstance(this);
        userDao = database.userDao();
        videoDao = database.videoDao();
        commentDao = database.commentDao();
        executorService = Executors.newSingleThreadExecutor();

        handleIntent(getIntent());
        setupFlipper();
        setupListenersAndData();
        setupEditButton();
    }

    private void handleIntent(Intent intent) {
        int videoId = intent.getIntExtra("videoId", -1);
        int currentUserId = intent.getIntExtra("currentUserId", -1);

        executorService.execute(() -> {
            currentVideo = videoDao.getVideoById(videoId);
            currentUser = currentUserId != -1 ? userDao.getUserById(currentUserId) : null;
            owner = userDao.getUserById(currentVideo.getOwnerId());
            runOnUiThread(this::updateUI);
        });
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
        executorService.execute(() -> {
            List<Video> videos = videoDao.getAllVideos();
            runOnUiThread(() -> {
                videoAdapter = new VideoRecyclerViewAdapter(this, videos, userDao, videoDao, currentUser);
                vidRecyclerView.setAdapter(videoAdapter);
                vidRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            });
        });

        RecyclerView commRecyclerView = findViewById(R.id.commentSection);
        executorService.execute(() -> {
            List<CommentObj> comments = commentDao.getCommentsForVideo(currentVideo.getId());
            runOnUiThread(() -> {
                commentAdapter = new CommentRecyclerViewAdapter(this, comments, userDao, commentDao, currentVideo, videoDao, currentUser);
                commRecyclerView.setAdapter(commentAdapter);
                commRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            });
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
        executorService.execute(() -> {
            videoDao.incrementLikes(currentVideo.getId());
            currentVideo = videoDao.getVideoById(currentVideo.getId());
            runOnUiThread(() -> {
                TextView tvLikesNumber = findViewById(R.id.likes_number);
                tvLikesNumber.setText(Utils.likeBalance(currentVideo));
            });
        });
    }

    private void onDislike() {
        executorService.execute(() -> {
            videoDao.incrementDislikes(currentVideo.getId());
            currentVideo = videoDao.getVideoById(currentVideo.getId());
            runOnUiThread(() -> {
                TextView tvLikesNumber = findViewById(R.id.likes_number);
                tvLikesNumber.setText(Utils.likeBalance(currentVideo));
            });
        });
    }

    private void submitComment() {
        EditText commentTextField = findViewById(R.id.comment_text_field);
        String newCommentBody = commentTextField.getText().toString();

        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to comment", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            CommentObj newComment = new CommentObj(currentVideo.getId(), currentUser.getUsername(), newCommentBody);
            commentDao.insert(newComment);
            runOnUiThread(() -> {
                commentTextField.setText("");
                updateComments();
            });
        });
    }

    private void onShare() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, currentVideo.getTitle());
        startActivity(Intent.createChooser(shareIntent, "Share via"));
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
        editButton.setVisibility(currentUser != null && currentUser.getId() == currentVideo.getOwnerId() ? View.VISIBLE : View.GONE);
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
                executorService.execute(() -> {
                    currentVideo.setTitle(newTitle);
                    currentVideo.setDescription(newDescription);
                    currentVideo.setThumbnail(thumbnailUri.toString());
                    videoDao.update(currentVideo);
                    runOnUiThread(() -> {
                        dialog.dismiss();
                        updateUI();
                    });
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

        setupVideoPlayer();
        updateComments();
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

    private void updateComments() {
        executorService.execute(() -> {
            List<CommentObj> comments = commentDao.getCommentsForVideo(currentVideo.getId());
            runOnUiThread(() -> {
                if (commentAdapter == null) {
                    RecyclerView commRecyclerView = findViewById(R.id.commentSection);
                    commentAdapter = new CommentRecyclerViewAdapter(this, comments, userDao, commentDao, currentVideo, videoDao, currentUser);
                    commRecyclerView.setAdapter(commentAdapter);
                    commRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                } else {
                    commentAdapter.updateComments(comments);
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