package com.example.hemi_tube;

import static com.example.hemi_tube.UploadVideoActivity.PICK_THUMBNAIL_REQUEST;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.entities.Video;

import java.util.ArrayList;
import java.util.List;

public class VideoRecyclerViewAdapter extends RecyclerView.Adapter<VideoRecyclerViewAdapter.VideoViewHolder> {

    private Context context;
    private List<Video> videoList;
    private List<User> userList;
    private User currentUser;
    private List<Video> originalVideoList;

    public VideoRecyclerViewAdapter(Context context, List<Video> videoList, List<User> userList, User currentUser, List<Video> originalVideoList) {
        this.context = context;
        this.videoList = videoList;
        this.userList = userList;
        this.currentUser = currentUser;
        this.originalVideoList = originalVideoList;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.video_item, parent, false);
        return new VideoViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Video currVideo = videoList.get(position);
        holder.title.setText(currVideo.getTitle());

        User owner = Utils.getVideoOwner(currVideo, userList);
        if (owner != null) {
            String views = Utils.formatNumber(currVideo.getViews());
            String metadata = owner.getUsername() + "  " + views + " views  " + currVideo.getDate();
            holder.metaData.setText(metadata);

            String thumbnail = currVideo.getThumbnail();
            if (thumbnail != null && thumbnail.startsWith("content://")) {
                try {
                    Uri thumbnailUri = Uri.parse(thumbnail);
                    holder.thumbnail.setImageURI(thumbnailUri);
                } catch (SecurityException e) {
                    Log.e("VideoRecyclerViewAdapter", "No access to content URI for thumbnail", e);
                    holder.thumbnail.setImageResource(R.drawable.thumbnail_placeholder); // Default thumbnail
                }
            } else if (thumbnail != null && thumbnail.contains("/") && thumbnail.contains(".")) {
                int lastSlash = thumbnail.lastIndexOf('/');
                int lastDot = thumbnail.lastIndexOf('.');
                if (lastSlash >= 0 && lastDot > lastSlash) {
                    String thumbnailName = thumbnail.substring(lastSlash + 1, lastDot);
                    int resourceId = context.getResources().getIdentifier(thumbnailName, "drawable", context.getPackageName());
                    holder.thumbnail.setImageResource(resourceId);
                }
            }

            String profilePicturePath = owner.getProfilePicture();
            if (profilePicturePath != null && profilePicturePath.startsWith("content://")) {
                try {
                    holder.profilePicture.setImageURI(Uri.parse(profilePicturePath));
                } catch (SecurityException e) {
                    Log.e("VideoRecyclerViewAdapter", "No access to content URI for profile picture", e);
                    holder.profilePicture.setImageResource(R.drawable.profile); // Default profile picture
                }
            } else if (profilePicturePath != null && profilePicturePath.contains("/")) {
                int lastSlash = profilePicturePath.lastIndexOf('/');
                int lastDot = profilePicturePath.lastIndexOf('.');
                if (lastSlash >= 0 && lastDot > lastSlash) {
                    String profilePictureName = profilePicturePath.substring(lastSlash + 1, lastDot);
                    int profilePictureResourceId = context.getResources().getIdentifier(profilePictureName, "drawable", context.getPackageName());
                    if (profilePictureResourceId != 0) {
                        holder.profilePicture.setImageResource(profilePictureResourceId);
                    } else {
                        holder.profilePicture.setImageResource(R.drawable.profile); // Default profile picture
                    }
                }
            } else {
                holder.profilePicture.setImageResource(R.drawable.profile); // Default profile picture
            }

            Log.d("ShonLog", "In VideoRecycler, videoList: " + videoList);
            Log.d("ShonLog", "In VideoRecycler, originalVideoList: " + originalVideoList);

            holder.thumbnail.setOnClickListener(v -> {
                // Increase the view count
                currVideo.increaseViews();

                // Notify the adapter about the change
                notifyItemChanged(position);

                // Start the WatchScreenActivity
                Intent watchVideo = new Intent(context, WatchScreenActivity.class);
                watchVideo.putExtra("currentVideo", currVideo);
                watchVideo.putExtra("videoList", new ArrayList<>(originalVideoList)); // Pass the updated video list
                watchVideo.putExtra("currentUser", currentUser);
                watchVideo.putExtra("userList", new ArrayList<>(userList));
                watchVideo.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(watchVideo);
            });
        } else {
            Log.e("VideoRecyclerViewAdapter", "No owner found for video: " + currVideo.getTitle());
        }
    }


    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public void updateList(List<Video> newVideoList) {
        this.videoList.clear();
        this.videoList.addAll(newVideoList);
        //this.originalVideoList.clear();
        //this.originalVideoList.addAll(newVideoList);
        notifyDataSetChanged();
    }

    public void updateUserList(List<User> newUserList) {
        this.userList.clear();
        this.userList.addAll(newUserList);
        notifyDataSetChanged();
    }

    public void updateCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        notifyDataSetChanged();
    }


    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageButton thumbnail;
        ImageView profilePicture;
        TextView title;
        TextView metaData;

        public VideoViewHolder(@NonNull View videoView) {
            super(videoView);
            thumbnail = videoView.findViewById(R.id.thumbnail);
            profilePicture = videoView.findViewById(R.id.profilePicture);
            title = videoView.findViewById(R.id.title);
            metaData = videoView.findViewById(R.id.metaData);
        }
    }
}
