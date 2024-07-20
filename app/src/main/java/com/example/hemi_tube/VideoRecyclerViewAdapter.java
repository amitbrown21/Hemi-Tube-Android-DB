package com.example.hemi_tube;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.entities.Video;
import com.example.hemi_tube.viewmodel.UserViewModel;
import com.example.hemi_tube.viewmodel.VideoViewModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class VideoRecyclerViewAdapter extends RecyclerView.Adapter<VideoRecyclerViewAdapter.VideoViewHolder> {

    private Context context;
    private List<Video> videoList;
    private UserViewModel userViewModel;
    private VideoViewModel videoViewModel;
    private User currentUser;

    public VideoRecyclerViewAdapter(Context context, List<Video> videoList, UserViewModel userViewModel, VideoViewModel videoViewModel, User currentUser) {
        this.context = context;
        this.videoList = videoList;
        this.userViewModel = userViewModel;
        this.videoViewModel = videoViewModel;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.video_item, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Video currVideo = videoList.get(position);
        holder.title.setText(currVideo.getTitle());

        userViewModel.getUserById(currVideo.getOwner().getId()).observe((LifecycleOwner) context, owner -> {
            if (owner != null) {
                String views = Utils.formatNumber(currVideo.getViews());
                String metadata = owner.getUsername() + "  " + views + " views  " + currVideo.getDate();
                holder.metaData.setText(metadata);
                setThumbnail(holder.thumbnail, currVideo.getThumbnail());
                setProfilePicture(holder.profilePicture, owner.getProfilePicture());
            }
        });

        holder.thumbnail.setOnClickListener(v -> {
            videoViewModel.incrementViews(currVideo.getId());
            Intent watchVideo = new Intent(context, WatchScreenActivity.class);
            watchVideo.putExtra("videoId", currVideo.getId());
            watchVideo.putExtra("currentUserId", currentUser != null ? currentUser.getId() : null);
            watchVideo.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(watchVideo);
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public void updateList(List<Video> newVideoList) {
        this.videoList.clear();
        this.videoList.addAll(newVideoList);
        notifyDataSetChanged();
    }

    public void updateCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        notifyDataSetChanged();
    }

    private void setThumbnail(ImageButton imageButton, String thumbnailUri) {
        Log.d("VideoRecyclerViewAdapter", "Thumbnail URI: " + thumbnailUri);
        if (thumbnailUri != null && !thumbnailUri.isEmpty()) {
            // Construct the full URL to the image on the server
            String imageUrl = "http://10.0.2.2:3000/" + thumbnailUri.replace("\\", "/");
            Log.d("VideoRecyclerViewAdapter", "Thumbnail URL: " + imageUrl);

            // Use an image loading library like Picasso or Glide to load the image
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.thumbnail_placeholder)
                    .into(imageButton);
        } else {
            Log.d("VideoRecyclerViewAdapter", "Thumbnail URI is null or empty");
            imageButton.setImageResource(R.drawable.thumbnail_placeholder);
        }
    }
    private void setProfilePicture(ImageView imageView, String picturePath) {
        Log.d("VideoRecyclerViewAdapter", "Profile Picture Path: " + picturePath);
        if (picturePath != null && !picturePath.isEmpty()) {
            File imgFile = new File(picturePath);
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imageView.setImageBitmap(myBitmap);
            } else {
                Log.d("VideoRecyclerViewAdapter", "Profile picture file does not exist: " + imgFile.getAbsolutePath());
                imageView.setImageResource(R.drawable.profile);
            }
        } else {
            Log.d("VideoRecyclerViewAdapter", "Profile picture path is null or empty");
            imageView.setImageResource(R.drawable.profile);
        }
    }


    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageButton thumbnail;
        ImageView profilePicture;
        TextView title;
        TextView metaData;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            title = itemView.findViewById(R.id.title);
            metaData = itemView.findViewById(R.id.metaData);
        }
    }
}
