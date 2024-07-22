package com.example.hemi_tube.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.hemi_tube.entities.CommentObj;
import com.example.hemi_tube.repository.CommentRepository;
import com.example.hemi_tube.repository.RepositoryCallback;

import java.util.List;

public class CommentViewModel extends AndroidViewModel {
    private CommentRepository commentRepository;
    private MutableLiveData<List<CommentObj>> commentsLiveData = new MutableLiveData<>();

    public CommentViewModel(Application application) {
        super(application);
        commentRepository = new CommentRepository(application);
    }

    public LiveData<List<CommentObj>> getCommentsForVideo(String userId, String videoId) {
        refreshComments(userId, videoId);
        return commentsLiveData;
    }

    private void refreshComments(String userId, String videoId) {
        commentRepository.refreshComments(userId, videoId, new RepositoryCallback<List<CommentObj>>() {
            @Override
            public void onSuccess(List<CommentObj> result) {
                commentsLiveData.postValue(result);
            }

            @Override
            public void onError(Exception e) {
                // Handle error, maybe log it or show a message to the user
            }
        });
    }

    public void createComment(String userId, String videoId, CommentObj comment, RepositoryCallback<CommentObj> callback) {
        commentRepository.createComment(userId, videoId, comment, new RepositoryCallback<CommentObj>() {
            @Override
            public void onSuccess(CommentObj result) {
                callback.onSuccess(result);
                refreshComments(userId, videoId);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void updateComment(String userId, String videoId, CommentObj comment, RepositoryCallback<CommentObj> callback) {
        commentRepository.updateComment(userId, videoId, comment, new RepositoryCallback<CommentObj>() {
            @Override
            public void onSuccess(CommentObj result) {
                callback.onSuccess(result);
                refreshComments(userId, videoId);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void deleteComment(String userId, String videoId, String commentId, RepositoryCallback<Void> callback) {
        commentRepository.deleteComment(userId, videoId, commentId, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(result);
                refreshComments(userId, videoId);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }
}