package com.example.hemi_tube;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hemi_tube.entities.CommentObj;
import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.entities.Video;

import java.util.ArrayList;
import java.util.List;

public class WatchScreenActivity extends AppCompatActivity {

    private static final int EDIT_VIDEO_REQUEST = 1;
    private static final int PICK_THUMBNAIL_REQUEST = 2;

    private List<Video> videoList = null;
    private ArrayList<User> userList = null;
    private Video currentVideo = null;
    private User currentUser = null;
    private User owner = null;
    private int isLiked = 0; // 0 = nothing, 1 = liked, -1 = disliked
    private boolean isExpanded = false; // For description expansion
    private Uri thumbnailUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_screen);

        handleIntent(getIntent());
        iniFields();
        setupFlipper();
        setupListenersAndData();
        startVideo();
        setupEditButton();
    }

    private void handleIntent(Intent intent) {
        this.currentVideo = (Video) intent.getSerializableExtra("currentVideo");
        if (this.currentVideo == null) {
            Log.e("handleIntent", "currentVideo is null");
            return;
        }

        this.videoList = (ArrayList<Video>) intent.getSerializableExtra("videoList");
        this.currentUser = (User) intent.getSerializableExtra("currentUser");
        this.userList = (ArrayList<User>) intent.getSerializableExtra("userList");

        Log.d("ShonLog", "In WatchScreen, videoList: " + videoList);

        if (this.videoList == null) {
            Log.e("handleIntent", "videoList is null");
        }
        if (this.currentUser == null) {
            Log.e("handleIntent", "currentUser is null");
        }
        if (this.userList == null) {
            Log.e("handleIntent", "userList is null");
        }
    }

    private void iniFields() {
        if (currentVideo != null && userList != null) {
            for (User user : userList) {
                if (user.getId() == currentVideo.getOwnerId()) {
                    this.owner = user;
                    break;
                }
            }
        }
        if (this.owner == null) {
            Log.e("iniFields", "Couldn't find video's owner");
        }
    }

    private void setupEditButton() {
        Button editButton = findViewById(R.id.editButton);
        editButton.setVisibility(View.VISIBLE);
        editButton.setOnClickListener(v -> {
            if(currentUser != null && currentVideo != null)
                showEditDialog();
            else
                Toast.makeText(this, "Please sign in", Toast.LENGTH_SHORT).show();
        });
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

                    updateVideoList(currentVideo);

                    dialog.dismiss();
                    startVideo(); // Refresh the video details
                }
            });
    }

    private void selectThumbnail() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_THUMBNAIL_REQUEST);
    }

    private void updateVideoList(Video updatedVideo) {
        for (int i = 0; i < videoList.size(); i++) {
            if (videoList.get(i).getId() == updatedVideo.getId()) {
                videoList.set(i, updatedVideo);
                break;
            }
        }
    }

    private void startVideo() {
        if (this.currentVideo == null || this.owner == null) {
            Log.e("startVideo", "currentVideo or owner is null");
            return;
        }

        TextView tvTitle = findViewById(R.id.videoTitle);
        TextView tvVideoData = findViewById(R.id.videoData);
        TextView tvOwnerName = findViewById(R.id.comment_username);
        TextView tvSubscribers = findViewById(R.id.ownerSubs);
        TextView tvLikesNumber = findViewById(R.id.likes_number);
        TextView tvVideoDescription = findViewById(R.id.videoDescription);
        ImageView ownerProfilePicture = findViewById(R.id.owner_profile_picture);

        String data = this.currentVideo.getViews() + " " + this.currentVideo.getDate();
        tvTitle.setText(this.currentVideo.getTitle());
        tvOwnerName.setText(this.owner.getUsername());
        tvSubscribers.setText(Utils.formatNumber(this.owner.getSubscribers()));
        tvVideoData.setText(data);
        tvLikesNumber.setText(Utils.likeBalance(this.currentVideo));
        tvVideoDescription.setText(this.currentVideo.getDescription());

        // Set the profile picture based on the owner's profile picture path
        String profilePicturePath = owner.getProfilePicture();

        if (profilePicturePath == null) {
            ownerProfilePicture.setImageResource(R.drawable.profile); // Default profile picture
        } else if (profilePicturePath.startsWith("content://")) {
            try {
                ownerProfilePicture.setImageURI(Uri.parse(profilePicturePath));
            } catch (SecurityException e) {
                Log.e("WatchScreenActivity", "No access to content URI for profile picture", e);
                ownerProfilePicture.setImageResource(R.drawable.profile); // Default profile picture
            }
        } else if (profilePicturePath.contains("/")) {
            int lastSlash = profilePicturePath.lastIndexOf('/');
            int lastDot = profilePicturePath.lastIndexOf('.');
            if (lastSlash >= 0 && lastDot > lastSlash) {
                String profilePictureName = profilePicturePath.substring(lastSlash + 1, lastDot);
                int profilePictureResourceId = getResources().getIdentifier(profilePictureName, "drawable", getPackageName());
                if (profilePictureResourceId != 0) {
                    ownerProfilePicture.setImageResource(profilePictureResourceId);
                } else {
                    ownerProfilePicture.setImageResource(R.drawable.profile); // Default profile picture
                }
            } else {
                ownerProfilePicture.setImageResource(R.drawable.profile); // Default profile picture
            }
        } else {
            ownerProfilePicture.setImageResource(R.drawable.profile); // Default profile picture
        }

        VideoView videoView = findViewById(R.id.videoView);
        MediaController mediaController = new MediaController(this);
        mediaController.setMediaPlayer(videoView);
        videoView.setMediaController(mediaController);

        Uri videoUri;
        String videoUrl = this.currentVideo.getUrl();
        if (videoUrl.startsWith("content://")) {
            videoUri = Uri.parse(videoUrl);
        } else {
            int lastDot = videoUrl.lastIndexOf('.');
            if (lastDot > 0) {
                String videoResourceName = videoUrl.substring(0, lastDot);
                int videoResourceId = getResources().getIdentifier(videoResourceName, "raw", getPackageName());
                videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + videoResourceId);
            } else {
                Log.e("startVideo", "Invalid video URL: " + videoUrl);
                return;
            }
        }

        videoView.setVideoURI(videoUri);
        videoView.start();
    }

    private void setupFlipper() {
        RecyclerView vidRecyclerView = findViewById(R.id.video_layout);
        VideoRecyclerViewAdapter vidAdapter = new VideoRecyclerViewAdapter(this, this.videoList, this.userList, this.currentUser, this.videoList);
        vidRecyclerView.setAdapter(vidAdapter);
        vidRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        RecyclerView commRecyclerView = findViewById(R.id.commentSection);
        CommentRecyclerViewAdapter commAdapter = new CommentRecyclerViewAdapter(this, currentVideo.getComments(), this.userList, this.currentVideo, this.videoList, vidAdapter, this.currentUser);
        commRecyclerView.setAdapter(commAdapter);
        commRecyclerView.setLayoutManager(new LinearLayoutManager(this));

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
        //this.currentVideo.increaseViews();

        TextView likesNumber = findViewById(R.id.likes_number);
        likesNumber.setText(Utils.likeBalance((this.currentVideo)));

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

    private void onLike() {
        if (this.isLiked == 1) {
            this.isLiked = 0;
            currentVideo.setLikes(this.currentVideo.getLikes() - 1);
            TextView tvLikesNumber = findViewById(R.id.likes_number);
            tvLikesNumber.setText(Utils.likeBalance(this.currentVideo));
            return;
        }

        if (this.isLiked == -1) {
            onDislike();
        }

        this.isLiked = 1;
        currentVideo.setLikes(this.currentVideo.getLikes() + 1);
        TextView tvLikesNumber = findViewById(R.id.likes_number);
        tvLikesNumber.setText(Utils.likeBalance(this.currentVideo));
    }

    private void onDislike() {
        if (this.isLiked == -1) {
            this.isLiked = 0;
            currentVideo.setDislikes(this.currentVideo.getDislikes() - 1);
            TextView tvLikesNumber = findViewById(R.id.likes_number);
            tvLikesNumber.setText(Utils.likeBalance(this.currentVideo));
            return;
        }

        if (this.isLiked == 1) {
            onLike();
        }

        this.isLiked = -1;
        currentVideo.setDislikes(this.currentVideo.getDislikes() + 1);
        TextView tvLikesNumber = findViewById(R.id.likes_number);
        tvLikesNumber.setText(Utils.likeBalance(this.currentVideo));
    }

    private void submitComment() {
        EditText comment_text_field = findViewById(R.id.comment_text_field);
        String newCommentBody = comment_text_field.getText().toString();

        // Get the comment adapter
        RecyclerView commRecyclerView = findViewById(R.id.commentSection);
        CommentRecyclerViewAdapter commAdapter = (CommentRecyclerViewAdapter) commRecyclerView.getAdapter();
        if (commAdapter != null) {
            commAdapter.submitComment(newCommentBody, currentUser);
        }

        // Hide keyboard and clear text field
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(comment_text_field.getWindowToken(), 0);
        comment_text_field.setText("");
    }

    private void onShare() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        String linkToVid = this.currentVideo.getTitle();
        shareIntent.putExtra(Intent.EXTRA_TEXT, linkToVid);

        Intent chooserIntent = Intent.createChooser(shareIntent, "Share via");
        startActivity(chooserIntent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
        iniFields();
        setupFlipper();
        setupListenersAndData();
        startVideo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == EDIT_VIDEO_REQUEST) {
                Video updatedVideo = (Video) data.getSerializableExtra("video");
                if (updatedVideo != null) {
                    currentVideo.setTitle(updatedVideo.getTitle());
                    currentVideo.setDescription(updatedVideo.getDescription());
                    currentVideo.setThumbnail(updatedVideo.getThumbnail());

                    startVideo(); // Refresh the video details
                }
            } else if (requestCode == PICK_THUMBNAIL_REQUEST) {
                thumbnailUri = data.getData();
                if (thumbnailUri != null) {
                    // Grant URI permission
                    grantUriPermission(getPackageName(), thumbnailUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    // Persist the URI permission if supported
                    if (Intent.FLAG_GRANT_READ_URI_PERMISSION != 0 || Intent.FLAG_GRANT_WRITE_URI_PERMISSION != 0) {
                        final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        /*try {
                            getContentResolver().takePersistableUriPermission(thumbnailUri, takeFlags);
                        } catch (SecurityException e) {
                            Log.e("WatchScreenActivity", "Failed to take persistable URI permission", e);
                        }*/
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra("videos", (ArrayList<Video>) videoList);
        resultIntent.putExtra("originalVideos", (ArrayList<Video>) videoList);
        resultIntent.putExtra("users", (ArrayList<User>) userList);
        resultIntent.putExtra("currentUser", currentUser);
        resultIntent.putExtra("isSignedIn", currentUser != null ? true : false);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.d("ShonLog", "In WatchScreen, onDestroy, videos: " + videoList);
        startActivity(resultIntent);
        finish();
    }
}
