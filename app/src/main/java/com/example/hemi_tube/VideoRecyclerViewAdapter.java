package com.example.hemi_tube;

import android.app.Activity;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.hemi_tube.dao.UserDao;
import com.example.hemi_tube.dao.VideoDao;
import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.entities.Video;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoRecyclerViewAdapter extends RecyclerView.Adapter<VideoRecyclerViewAdapter.VideoViewHolder> {

    private Context context;
    private List<Video> videoList;
    private UserDao userDao;
    private VideoDao videoDao;
    private User currentUser;
    private ExecutorService executorService;

    public VideoRecyclerViewAdapter(Context context, List<Video> videoList, UserDao userDao, VideoDao videoDao, User currentUser) {
        this.context = context;
        this.videoList = videoList;
        this.userDao = userDao;
        this.videoDao = videoDao;
        this.currentUser = currentUser;
        this.executorService = Executors.newSingleThreadExecutor();
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

        executorService.execute(() -> {
            User owner = userDao.getUserById(currVideo.getOwnerId());
            if (owner != null) {
                String views = Utils.formatNumber(currVideo.getViews());
                String metadata = owner.getUsername() + "  " + views + " views  " + currVideo.getDate();
                ((Activity) context).runOnUiThread(() -> {
                    holder.metaData.setText(metadata);
                    setThumbnail(holder.thumbnail, currVideo.getThumbnail());
                    setProfilePicture(holder.profilePicture, owner.getProfilePicture());
                });
            }
        });

        holder.thumbnail.setOnClickListener(v -> {
            executorService.execute(() -> {
                videoDao.incrementViews(currVideo.getId());
                Video updatedVideo = videoDao.getVideoById(currVideo.getId());
                ((Activity) context).runOnUiThread(() -> {
                    Intent watchVideo = new Intent(context, WatchScreenActivity.class);
                    watchVideo.putExtra("videoId", updatedVideo.getId());
                    watchVideo.putExtra("currentUserId", currentUser != null ? currentUser.getId() : -1);
                    watchVideo.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(watchVideo);
                });
            });
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

    private void setThumbnail(ImageButton imageButton, String thumbnailPath) {
        if (thumbnailPath != null && thumbnailPath.startsWith("content://")) {
            try {
                imageButton.setImageURI(Uri.parse(thumbnailPath));
            } catch (SecurityException e) {
                Log.e("VideoRecyclerViewAdapter", "No access to content URI for thumbnail", e);
                imageButton.setImageResource(R.drawable.thumbnail_placeholder);
            }
        } else if (thumbnailPath != null && thumbnailPath.contains("/")) {
            int resourceId = context.getResources().getIdentifier(
                    thumbnailPath.substring(thumbnailPath.lastIndexOf("/") + 1, thumbnailPath.lastIndexOf(".")),
                    "drawable",
                    context.getPackageName()
            );
            imageButton.setImageResource(resourceId != 0 ? resourceId : R.drawable.thumbnail_placeholder);
        } else {
            imageButton.setImageResource(R.drawable.thumbnail_placeholder);
        }
    }

    private void setProfilePicture(ImageView imageView, String picturePath) {
        if (picturePath != null && picturePath.startsWith("content://")) {
            try {
                imageView.setImageURI(Uri.parse(picturePath));
            } catch (SecurityException e) {
                Log.e("VideoRecyclerViewAdapter", "No access to content URI for profile picture", e);
                imageView.setImageResource(R.drawable.profile);
            }
        } else if (picturePath != null && picturePath.contains("/")) {
            int resourceId = context.getResources().getIdentifier(
                    picturePath.substring(picturePath.lastIndexOf("/") + 1, picturePath.lastIndexOf(".")),
                    "drawable",
                    context.getPackageName()
            );
            imageView.setImageResource(resourceId != 0 ? resourceId : R.drawable.profile);
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

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}