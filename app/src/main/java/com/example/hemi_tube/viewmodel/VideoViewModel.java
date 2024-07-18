package com.example.hemi_tube.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.hemi_tube.entities.Video;
import com.example.hemi_tube.repository.VideoRepository;
import com.example.hemi_tube.repository.RepositoryCallback;
import java.util.List;

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

    public void updateVideo(Video video, RepositoryCallback<Video> callback) {
        videoRepository.updateVideo(video, callback);
    }

    public void deleteVideo(String videoId, RepositoryCallback<Void> callback) {
        videoRepository.deleteVideo(videoId, callback);
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
}