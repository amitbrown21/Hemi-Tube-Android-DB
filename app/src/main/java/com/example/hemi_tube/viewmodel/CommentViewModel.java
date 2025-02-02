package com.example.hemi_tube.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.hemi_tube.entities.CommentObj;
import com.example.hemi_tube.repository.CommentRepository;
import com.example.hemi_tube.repository.RepositoryCallback;
import java.util.List;

public class CommentViewModel extends AndroidViewModel {
    private CommentRepository commentRepository;

    public CommentViewModel(Application application) {
        super(application);
        commentRepository = new CommentRepository(application);
    }

    public LiveData<List<CommentObj>> getCommentsForVideo(String videoId) {
        return commentRepository.getCommentsForVideo(videoId);
    }

    public void createComment(CommentObj comment, RepositoryCallback<CommentObj> callback) {
        commentRepository.createComment(comment, callback);
    }

    public void updateComment(String userId, String videoId, CommentObj comment, RepositoryCallback<CommentObj> callback) {
        commentRepository.updateComment(userId, videoId, comment, callback);
    }

    public void deleteComment(String userId, String videoId, String commentId, Context context, RepositoryCallback<Void> callback) {
        commentRepository.deleteComment(userId, videoId, commentId, context, callback);
    }

}
