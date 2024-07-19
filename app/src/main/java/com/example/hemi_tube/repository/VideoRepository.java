package com.example.hemi_tube.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.hemi_tube.dao.VideoDao;
import com.example.hemi_tube.database.AppDatabase;
import com.example.hemi_tube.entities.Video;
import com.example.hemi_tube.network.ApiService;
import com.example.hemi_tube.network.RetrofitClient;
import com.example.hemi_tube.repository.RepositoryCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class VideoRepository {
    private static final String TAG = "VideoRepository";
    private VideoDao videoDao;
    private ApiService apiService;
    private Executor executor;

    public VideoRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        videoDao = db.videoDao();
        apiService = RetrofitClient.getInstance(context).getApi();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Video>> getAllVideos() {
        Log.d(TAG, "Getting all videos from repository");
        refreshVideos();
        return videoDao.getAllVideosLive();
    }

    public LiveData<Video> getVideoById(String videoId) {
        refreshVideo(videoId);
        return videoDao.getVideoByIdLive(videoId);
    }

    public LiveData<List<Video>> getVideosForUser(String userId) {
        refreshVideosForUser(userId);
        return videoDao.getVideosForUserLive(userId);
    }

    public void createVideo(String userId, Video video, final RepositoryCallback<Video> callback) {
        executor.execute(() -> {
            try {
                Response<Video> response = apiService.createVideo(userId, video).execute();
                if (response.isSuccessful() && response.body() != null) {
                    Video createdVideo = response.body();
                    videoDao.insert(createdVideo);
                    callback.onSuccess(createdVideo);
                    Log.d(TAG, "Video created successfully: " + createdVideo.getId());
                } else {
                    callback.onError(new Exception("Failed to create video"));
                    Log.e(TAG, "Failed to create video: " + response.message());
                }
            } catch (IOException e) {
                callback.onError(e);
                Log.e(TAG, "Error creating video", e);
            }
        });
    }

    public void updateVideo(Video video, final RepositoryCallback<Video> callback) {
        executor.execute(() -> {
            try {
                Response<Video> response = apiService.updateVideo(video.getId(), video).execute();
                if (response.isSuccessful() && response.body() != null) {
                    Video updatedVideo = response.body();
                    videoDao.update(updatedVideo);
                    callback.onSuccess(updatedVideo);
                    Log.d(TAG, "Video updated successfully: " + updatedVideo.getId());
                } else {
                    callback.onError(new Exception("Failed to update video"));
                    Log.e(TAG, "Failed to update video: " + response.message());
                }
            } catch (IOException e) {
                callback.onError(e);
                Log.e(TAG, "Error updating video", e);
            }
        });
    }

    public void deleteVideo(String videoId, final RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                Response<Void> response = apiService.deleteVideo(videoId).execute();
                if (response.isSuccessful()) {
                    videoDao.deleteById(videoId);
                    callback.onSuccess(null);
                    Log.d(TAG, "Video deleted successfully: " + videoId);
                } else {
                    callback.onError(new Exception("Failed to delete video"));
                    Log.e(TAG, "Failed to delete video: " + response.message());
                }
            } catch (IOException e) {
                callback.onError(e);
                Log.e(TAG, "Error deleting video", e);
            }
        });
    }

    public void incrementViews(String videoId) {
        executor.execute(() -> {
            videoDao.incrementViews(videoId);
            try {
                apiService.incrementViews(videoId).execute();
                Log.d(TAG, "Views incremented successfully for video: " + videoId);
            } catch (IOException e) {
                Log.e(TAG, "Error incrementing views for video: " + videoId, e);
            }
        });
    }

    public void incrementLikes(String videoId) {
        executor.execute(() -> {
            videoDao.incrementLikes(videoId);
            try {
                apiService.incrementLikes(videoId).execute();
                Log.d(TAG, "Likes incremented successfully for video: " + videoId);
            } catch (IOException e) {
                Log.e(TAG, "Error incrementing likes for video: " + videoId, e);
            }
        });
    }

    public void decrementLikes(String videoId) {
        executor.execute(() -> {
            videoDao.decrementLikes(videoId);
            try {
                apiService.decrementLikes(videoId).execute();
                Log.d(TAG, "Likes decremented successfully for video: " + videoId);
            } catch (IOException e) {
                Log.e(TAG, "Error decrementing likes for video: " + videoId, e);
            }
        });
    }

    public void incrementDislikes(String videoId) {
        executor.execute(() -> {
            videoDao.incrementDislikes(videoId);
            try {
                apiService.incrementDislikes(videoId).execute();
                Log.d(TAG, "Dislikes incremented successfully for video: " + videoId);
            } catch (IOException e) {
                Log.e(TAG, "Error incrementing dislikes for video: " + videoId, e);
            }
        });
    }

    public void decrementDislikes(String videoId) {
        executor.execute(() -> {
            videoDao.decrementDislikes(videoId);
            try {
                apiService.decrementDislikes(videoId).execute();
                Log.d(TAG, "Dislikes decremented successfully for video: " + videoId);
            } catch (IOException e) {
                Log.e(TAG, "Error decrementing dislikes for video: " + videoId, e);
            }
        });
    }

    private void refreshVideos() {
        executor.execute(() -> {
            try {
                Log.d(TAG, "Refreshing videos from server");
                Response<ApiService.VideoResponse> response = apiService.getAllVideos().execute();
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.VideoResponse videoResponse = response.body();
                    Log.d(TAG, "Received VideoResponse: " + videoResponse);
                    List<Video> allVideos = new ArrayList<>();
                    allVideos.addAll(videoResponse.getTopVideos());
                    allVideos.addAll(videoResponse.getOtherVideos());

                    if (!allVideos.isEmpty()) {
                        videoDao.insertAll(allVideos);
                        Log.d(TAG, "Videos refreshed successfully, count: " + allVideos.size());
                    } else {
                        Log.w(TAG, "No valid videos received from server");
                    }
                } else {
                    Log.e(TAG, "Failed to refresh videos: " + response.message());
                }
            } catch (IOException e) {
                Log.e(TAG, "Error refreshing videos", e);
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error refreshing videos", e);
            }
        });
    }

    private void refreshVideo(String videoId) {
        executor.execute(() -> {
            try {
                Response<Video> response = apiService.getVideoById(videoId).execute();
                if (response.isSuccessful() && response.body() != null) {
                    videoDao.insert(response.body());
                    Log.d(TAG, "Video refreshed successfully: " + videoId);
                } else {
                    Log.e(TAG, "Failed to refresh video: " + response.message());
                }
            } catch (IOException e) {
                Log.e(TAG, "Error refreshing video", e);
            }
        });
    }

    private void refreshVideosForUser(String userId) {
        executor.execute(() -> {
            try {
                Response<List<Video>> response = apiService.getUserVideos(userId).execute();
                if (response.isSuccessful() && response.body() != null) {
                    videoDao.insertAll(response.body());
                    Log.d(TAG, "User videos refreshed successfully: " + userId);
                } else {
                    Log.e(TAG, "Failed to refresh user videos: " + response.message());
                }
            } catch (IOException e) {
                Log.e(TAG, "Error refreshing user videos", e);
            }
        });
    }

    public LiveData<List<Video>> searchVideos(String query) {
        refreshSearchVideos(query);
        return videoDao.searchVideosLive("%" + query + "%");
    }

    private void refreshSearchVideos(String query) {
        executor.execute(() -> {
            try {
                Response<List<Video>> response = apiService.searchVideos(query).execute();
                if (response.isSuccessful() && response.body() != null) {
                    videoDao.insertAll(response.body());
                    Log.d(TAG, "Search videos refreshed successfully");
                } else {
                    Log.e(TAG, "Failed to refresh search videos: " + response.message());
                }
            } catch (IOException e) {
                Log.e(TAG, "Error refreshing search videos", e);
            }
        });
    }

    public Call<Video> uploadVideo(RequestBody userIdPart, RequestBody titlePart, RequestBody descriptionPart, MultipartBody.Part videoPart, MultipartBody.Part thumbnailPart) {
        return apiService.uploadVideo(userIdPart, titlePart, descriptionPart, videoPart, thumbnailPart);
    }
}
