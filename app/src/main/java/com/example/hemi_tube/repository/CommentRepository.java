package com.example.hemi_tube.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.hemi_tube.dao.CommentDao;
import com.example.hemi_tube.database.AppDatabase;
import com.example.hemi_tube.entities.CommentObj;
import com.example.hemi_tube.network.ApiService;
import com.example.hemi_tube.network.RetrofitClient;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import retrofit2.Response;

public class CommentRepository {
    private CommentDao commentDao;
    private ApiService apiService;
    private Executor executor;

    public CommentRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        commentDao = db.commentDao();
        apiService = RetrofitClient.getInstance(context).getApi();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<CommentObj>> getCommentsForVideo(int videoId) {
        refreshComments(videoId);
        return commentDao.getCommentsForVideoLive(videoId);
    }

    public void createComment(int userId, int videoId, CommentObj comment, final RepositoryCallback<CommentObj> callback) {
        executor.execute(() -> {
            try {
                Response<CommentObj> response = apiService.createComment(userId, videoId, comment).execute();
                if (response.isSuccessful() && response.body() != null) {
                    CommentObj createdComment = response.body();
                    commentDao.insert(createdComment);
                    callback.onSuccess(createdComment);
                } else {
                    callback.onError(new Exception("Failed to create comment"));
                }
            } catch (IOException e) {
                callback.onError(e);
            }
        });
    }

    public void updateComment(int userId, int videoId, CommentObj comment, final RepositoryCallback<CommentObj> callback) {
        executor.execute(() -> {
            try {
                Response<CommentObj> response = apiService.updateComment(userId, videoId, comment.getId(), comment).execute();
                if (response.isSuccessful() && response.body() != null) {
                    CommentObj updatedComment = response.body();
                    commentDao.update(updatedComment);
                    callback.onSuccess(updatedComment);
                } else {
                    callback.onError(new Exception("Failed to update comment"));
                }
            } catch (IOException e) {
                callback.onError(e);
            }
        });
    }

    public void deleteComment(int userId, int videoId, int commentId, final RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                Response<Void> response = apiService.deleteComment(userId, videoId, commentId).execute();
                if (response.isSuccessful()) {
                    commentDao.deleteById(commentId);
                    callback.onSuccess(null);
                } else {
                    callback.onError(new Exception("Failed to delete comment"));
                }
            } catch (IOException e) {
                callback.onError(e);
            }
        });
    }

    private void refreshComments(int videoId) {
        executor.execute(() -> {
            try {
                Response<List<CommentObj>> response = apiService.getCommentsByVideoId(videoId).execute();
                if (response.isSuccessful() && response.body() != null) {
                    commentDao.insertAll(response.body());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}