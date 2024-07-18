package com.example.hemi_tube.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.hemi_tube.dao.VideoDao;
import com.example.hemi_tube.database.AppDatabase;
import com.example.hemi_tube.entities.Video;
import com.example.hemi_tube.network.ApiService;
import com.example.hemi_tube.network.RetrofitClient;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import retrofit2.Response;

public class VideoRepository {
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
        refreshVideos();
        return videoDao.getAllVideosLive();
    }

    public LiveData<Video> getVideoById(int videoId) {
        refreshVideo(videoId);
        return videoDao.getVideoByIdLive(videoId);
    }

    public LiveData<List<Video>> getVideosForUser(int userId) {
        refreshVideosForUser(userId);
        return videoDao.getVideosForUserLive(userId);
    }

    public void createVideo(int userId, Video video, final RepositoryCallback<Video> callback) {
        executor.execute(() -> {
            try {
                Response<Video> response = apiService.createVideo(userId, video).execute();
                if (response.isSuccessful() && response.body() != null) {
                    Video createdVideo = response.body();
                    videoDao.insert(createdVideo);
                    callback.onSuccess(createdVideo);
                } else {
                    callback.onError(new Exception("Failed to create video"));
                }
            } catch (IOException e) {
                callback.onError(e);
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
                } else {
                    callback.onError(new Exception("Failed to update video"));
                }
            } catch (IOException e) {
                callback.onError(e);
            }
        });
    }

    public void deleteVideo(int videoId, final RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                Response<Void> response = apiService.deleteVideo(videoId).execute();
                if (response.isSuccessful()) {
                    videoDao.deleteById(videoId);
                    callback.onSuccess(null);
                } else {
                    callback.onError(new Exception("Failed to delete video"));
                }
            } catch (IOException e) {
                callback.onError(e);
            }
        });
    }

    public void incrementViews(int videoId) {
        executor.execute(() -> {
            videoDao.incrementViews(videoId);
            try {
                apiService.incrementViews(videoId).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void incrementLikes(int videoId) {
        executor.execute(() -> {
            videoDao.incrementLikes(videoId);
            try {
                apiService.incrementLikes(videoId).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void decrementLikes(int videoId) {
        executor.execute(() -> {
            videoDao.decrementLikes(videoId);
            try {
                apiService.decrementLikes(videoId).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void incrementDislikes(int videoId) {
        executor.execute(() -> {
            videoDao.incrementDislikes(videoId);
            try {
                apiService.incrementDislikes(videoId).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void decrementDislikes(int videoId) {
        executor.execute(() -> {
            videoDao.decrementDislikes(videoId);
            try {
                apiService.decrementDislikes(videoId).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void refreshVideos() {
        executor.execute(() -> {
            try {
                Response<List<Video>> response = apiService.getAllVideos().execute();
                if (response.isSuccessful() && response.body() != null) {
                    videoDao.insertAll(response.body());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void refreshVideo(int videoId) {
        executor.execute(() -> {
            try {
                Response<Video> response = apiService.getVideoById(videoId).execute();
                if (response.isSuccessful() && response.body() != null) {
                    videoDao.insert(response.body());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void refreshVideosForUser(int userId) {
        executor.execute(() -> {
            try {
                Response<List<Video>> response = apiService.getUserVideos(userId).execute();
                if (response.isSuccessful() && response.body() != null) {
                    videoDao.insertAll(response.body());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
