package com.example.hemi_tube.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.hemi_tube.entities.Video;
import com.example.hemi_tube.repository.VideoRepository;
import com.example.hemi_tube.repository.RepositoryCallback;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

public class VideoViewModel extends AndroidViewModel {
    private VideoRepository videoRepository;

    public VideoViewModel(Application application) {
        super(application);
        videoRepository = new VideoRepository(application);
    }

    public LiveData<List<Video>> getAllVideos() {
        return videoRepository.getAllVideos();
    }

    public LiveData<Video> getVideoById(String videoId) {
        return videoRepository.getVideoById(videoId);
    }

    public LiveData<List<Video>> getVideosForUser(String userId) {
        return videoRepository.getVideosForUser(userId);
    }

    public void createVideo(String userId, Video video, RepositoryCallback<Video> callback) {
        videoRepository.createVideo(userId, video, callback);
    }


    public void updateVideo(String userId, String videoId, RequestBody title, RequestBody description, MultipartBody.Part thumbnail, RepositoryCallback<Video> callback) {
        videoRepository.updateVideo(userId, videoId, title, description, thumbnail, callback);

    }

    public void deleteVideo(String videoId, RepositoryCallback<Void> callback) {
        videoRepository.deleteVideo(videoId, callback);
    }

    public LiveData<List<Video>> searchVideos(String query) {
        return videoRepository.searchVideos(query);
    }

    public void incrementViews(String videoId) {
        videoRepository.incrementViews(videoId);
    }

    public void incrementLikes(String videoId) {
        videoRepository.incrementLikes(videoId);
    }

    public void decrementLikes(String videoId) {
        videoRepository.decrementLikes(videoId);
    }

    public void incrementDislikes(String videoId) {
        videoRepository.incrementDislikes(videoId);
    }

    public void decrementDislikes(String videoId) {
        videoRepository.decrementDislikes(videoId);
    }

    public Call<Video> uploadVideo(RequestBody userIdPart, RequestBody titlePart, RequestBody descriptionPart, MultipartBody.Part videoBody, MultipartBody.Part thumbnailBody) {
        return videoRepository.uploadVideo(userIdPart, titlePart, descriptionPart, videoBody, thumbnailBody);
    }
}
