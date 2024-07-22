package com.example.hemi_tube.repository;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.hemi_tube.CommentRecyclerViewAdapter;
import com.example.hemi_tube.WatchScreenActivity;
import com.example.hemi_tube.dao.CommentDao;
import com.example.hemi_tube.database.AppDatabase;
import com.example.hemi_tube.entities.CommentObj;
import com.example.hemi_tube.network.ApiService;
import com.example.hemi_tube.network.RetrofitClient;
import com.example.hemi_tube.viewmodel.CommentViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import retrofit2.Response;

public class CommentRepository {
    private static final String TAG = "CommentRepository";
    private CommentDao commentDao;
    private ApiService apiService;
    private Executor executor;

    public CommentRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        commentDao = db.commentDao();
        apiService = RetrofitClient.getInstance(context).getApi();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<CommentObj>> getCommentsForVideo(String videoId) {
        refreshComments(videoId);
        LiveData<List<CommentObj>> comments = commentDao.getCommentsForVideoLive(videoId);
        if (comments == null) {
            comments = new MutableLiveData<>(new ArrayList<>()); // Return an empty list instead of null
        }
        return comments;
    }

    public void createComment(CommentObj comment, RepositoryCallback<CommentObj> callback) {
        executor.execute(() -> {
            try {
                Log.d("CommentRepository", "Creating comment for video: " + comment.getVideoId());
                Log.d("CommentRepository", "Comment data: " + comment.toString());

                Response<CommentObj> response = apiService.createComment(comment.getUserId(), comment.getVideoId(), comment).execute();

                if (response.isSuccessful() && response.body() != null) {
                    CommentObj createdComment = response.body();
                    Log.d("CommentRepository", "New comment created: " + createdComment.toString());

                    createdComment.setId(createdComment.getId());
                    commentDao.insert(createdComment);
                    callback.onSuccess(createdComment);
                } else {
                    Log.e("CommentRepository", "Failed to create comment: " + response.message());
                    if (response.errorBody() != null) {
                        Log.e("CommentRepository", "Error body: " + response.errorBody().string());
                    }
                    callback.onError(new Exception("Failed to create comment"));
                }
            } catch (IOException e) {
                Log.e("CommentRepository", "IOException while creating comment", e);
                callback.onError(e);
            }
        });
    }

    public void updateComment(String userId, String videoId, CommentObj comment, final RepositoryCallback<CommentObj> callback) {
        executor.execute(() -> {
            try {
                Response<CommentObj> response = apiService.updateComment(userId, videoId, comment.getId(), comment).execute();
                if (response.isSuccessful() && response.body() != null) {
                    CommentObj updatedComment = response.body();
                    commentDao.update(updatedComment);
                    if (callback != null) {
                        callback.onSuccess(updatedComment);
                    }
                    Log.d(TAG, "Comment updated successfully: " + updatedComment.getId());
                } else {
                    if (callback != null) {
                        callback.onError(new Exception("Failed to update comment"));
                    }
                    Log.e(TAG, "Failed to update comment: " + response.message());
                }
            } catch (IOException e) {
                if (callback != null) {
                    callback.onError(e);
                }
                Log.e(TAG, "Error updating comment", e);
            }
        });
    }


    public void deleteComment(String userId, String videoId, String commentId, Context context, final RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                Response<Void> response = apiService.deleteComment(userId, videoId, commentId).execute();
                if (response.isSuccessful()) {
                    commentDao.deleteById(commentId);
                    ((WatchScreenActivity) context).runOnUiThread(() -> {
                        CommentViewModel commentViewModel = new ViewModelProvider((WatchScreenActivity) context).get(CommentViewModel.class);
                        commentViewModel.getCommentsForVideo(videoId).observe((LifecycleOwner) context, comments -> {
                            if (comments != null) {
                                ((WatchScreenActivity) context).updateComments(comments);
                            } else {
                                ((WatchScreenActivity) context).updateComments(new ArrayList<>());
                            }
                            Toast.makeText(context, "Comment deleted successfully", Toast.LENGTH_SHORT).show();
                        });
                    });
                    if (callback != null) {
                        callback.onSuccess(null);
                    }
                    Log.d(TAG, "Comment deleted successfully: " + commentId);
                } else {
                    if (callback != null) {
                        callback.onError(new Exception("Failed to delete comment"));
                    }
                    Log.e(TAG, "Failed to delete comment: " + response.message());
                }
            } catch (IOException e) {
                if (callback != null) {
                    callback.onError(e);
                }
                Log.e(TAG, "Error deleting comment", e);
            }
        });
    }




    private void refreshComments(String videoId) {
        executor.execute(() -> {
            try {
                Response<List<CommentObj>> response = apiService.getCommentsByVideoId(videoId).execute();
                if (response.isSuccessful() && response.body() != null) {
                    commentDao.insertAll(response.body());
                    Log.d(TAG, "Comments refreshed successfully for video: " + videoId);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error refreshing comments for video", e);
            }
        });
    }
}
