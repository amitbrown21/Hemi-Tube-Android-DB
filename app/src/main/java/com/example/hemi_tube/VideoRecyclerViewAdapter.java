package com.example.hemi_tube;

import android.content.Context;
import android.content.Intent;
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
import com.example.hemi_tube.network.ApiService;
import com.example.hemi_tube.network.RetrofitClient;
import com.example.hemi_tube.viewmodel.UserViewModel;
import com.example.hemi_tube.viewmodel.VideoViewModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoRecyclerViewAdapter extends RecyclerView.Adapter<VideoRecyclerViewAdapter.VideoViewHolder> {

    private static final String TAG = "VideoRecyclerViewAdapter";
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
        holder.bind(currVideo);
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public void updateList(List<Video> newVideoList) {
        this.videoList = newVideoList;
        notifyDataSetChanged();
    }

    public void updateCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    class VideoViewHolder extends RecyclerView.ViewHolder {
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

        void bind(Video video) {
            title.setText(video.getTitle());
            UserHolder userHolder = new UserHolder();

            userViewModel.getUserById(video.getOwner().getId()).observe((LifecycleOwner) context, owner -> {
                if (owner != null) {
                    String views = Utils.formatNumber(video.getViews());
                    String metadata = owner.getUsername() + "  " + views + " views  " + video.getDate();
                    metaData.setText(metadata);

                    loadImage(thumbnail, video.getThumbnail(), R.drawable.thumbnail_placeholder);
                    loadImage(profilePicture, owner.getProfilePicture(), R.drawable.profile);

                    userHolder.user = owner;

                }
            });

            thumbnail.setOnClickListener(v -> {
                incrementViews(video.getId());
                Intent watchVideo = new Intent(context, WatchScreenActivity.class);
                watchVideo.putExtra("videoId", video.getId());
                watchVideo.putExtra("currentUserId", userHolder.user != null ? userHolder.user.getId() : null); // Pass currentUserId
                context.startActivity(watchVideo);
            });
        }

        private void loadImage(ImageView imageView, String imagePath, int placeholderResId) {
            if (imagePath != null && !imagePath.isEmpty()) {
                String imageUrl = "http://10.0.2.2:3000/" + imagePath.replace("\\", "/");
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(placeholderResId)
                        .error(placeholderResId)
                        .into(imageView);
            } else {
                imageView.setImageResource(placeholderResId);
            }
        }

        private void incrementViews(String videoId) {
            ApiService apiService = RetrofitClient.getInstance(context).getApi();
            apiService.incrementViews(videoId).enqueue(new Callback<Video>() {
                @Override
                public void onResponse(Call<Video> call, Response<Video> response) {
                    if (response.isSuccessful()) {
                        // Handle the response if needed
                    } else {
                        // Handle the error if needed
                    }
                }

                @Override
                public void onFailure(Call<Video> call, Throwable t) {
                    // Handle the failure if needed
                }
            });
        }
    }

    class UserHolder {
        User user;
    }
}
