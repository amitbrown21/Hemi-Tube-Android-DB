package com.example.hemi_tube;

import android.content.Context;
import android.content.Intent;
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

import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.entities.Video;
import com.example.hemi_tube.viewmodel.UserViewModel;
import com.example.hemi_tube.viewmodel.VideoViewModel;
import com.squareup.picasso.Picasso;

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

        userViewModel.getUserById(currVideo.getOwner()).observe((LifecycleOwner) context, owner -> {
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
        if (thumbnailUri != null && !thumbnailUri.isEmpty()) {
            Picasso.get()
                    .load(thumbnailUri.startsWith("http") ? thumbnailUri : "file://" + thumbnailUri)
                    .resize(200, 200) // Resize image to reduce memory usage
                    .centerCrop()
                    .into(imageButton);
        } else {
            imageButton.setImageResource(R.drawable.thumbnail_placeholder);
        }
    }

    private void setProfilePicture(ImageView imageView, String picturePath) {
        if (picturePath != null && !picturePath.isEmpty()) {
            Picasso.get()
                    .load(picturePath.startsWith("http") ? picturePath : "file://" + picturePath)
                    .resize(100, 100) // Resize image to reduce memory usage
                    .centerCrop()
                    .into(imageView);
        } else {
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
