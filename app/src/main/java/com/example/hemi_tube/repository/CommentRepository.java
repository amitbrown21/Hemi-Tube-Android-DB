package com.example.hemi_tube.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

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
    private static final String TAG = "CommentRepository";
    private static final String PREF_NAME = "AuthPrefs";
    private static final String KEY_AUTH_TOKEN = "auth_token";

    private Context context;
    private CommentDao commentDao;
    private ApiService apiService;
    private Executor executor;

    public CommentRepository(Context context) {
        this.context = context.getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(context);
        commentDao = db.commentDao();
        apiService = RetrofitClient.getInstance(context).getApi();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<CommentObj>> getCommentsForVideo(String userId, String videoId) {
        refreshComments(userId, videoId, new RepositoryCallback<List<CommentObj>>() {
            @Override
            public void onSuccess(List<CommentObj> result) {
                // Comments are already saved to the database in refreshComments,
                // so we don't need to do anything here.
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error refreshing comments: " + e.getMessage());
            }
        });
        return commentDao.getCommentsForVideoLive(videoId);
    }

    public void createComment(String userId, String videoId, CommentObj comment, final RepositoryCallback<CommentObj> callback) {
        executor.execute(() -> {
            try {
                String token = getAuthToken();
                if (token == null) {
                    callback.onError(new Exception("User is not authenticated"));
                    return;
                }
                Response<CommentObj> response = apiService.createComment(userId, videoId, comment, "Bearer " + token).execute();
                if (response.isSuccessful() && response.body() != null) {
                    CommentObj createdComment = response.body();
                    if (createdComment.getId() == null) {
                        // If the id is null, it means the server didn't return an id
                        // In this case, we should not save the comment to the local database
                        callback.onError(new Exception("Server did not return a valid id for the comment"));
                        return;
                    }
                    commentDao.insert(createdComment);
                    callback.onSuccess(createdComment);
                    Log.d(TAG, "Comment created successfully: " + createdComment.getId());
                } else {
                    callback.onError(new Exception("Failed to create comment: " + response.message()));
                    Log.e(TAG, "Failed to create comment: " + response.code() + " " + response.message());
                }
            } catch (IOException e) {
                callback.onError(e);
                Log.e(TAG, "Error creating comment", e);
            }
        });
    }

    public void refreshComments(String userId, String videoId, final RepositoryCallback<List<CommentObj>> callback) {
        executor.execute(() -> {
            try {
                Response<List<CommentObj>> response = apiService.getCommentsByVideoId(userId, videoId).execute();
                if (response.isSuccessful() && response.body() != null) {
                    List<CommentObj> comments = response.body();
                    commentDao.deleteAllCommentsForVideo(videoId);
                    commentDao.insertAll(comments);
                    callback.onSuccess(comments);
                    Log.d(TAG, "Comments refreshed successfully for video: " + videoId);
                } else {
                    callback.onError(new Exception("Failed to refresh comments: " + response.message()));
                }
            } catch (IOException e) {
                callback.onError(e);
                Log.e(TAG, "Error refreshing comments for video", e);
            }
        });
    }

    public void updateComment(String userId, String videoId, CommentObj comment, final RepositoryCallback<CommentObj> callback) {
        executor.execute(() -> {
            try {
                String token = getAuthToken();
                if (token == null) {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError(new Exception("User is not authenticated")));
                    return;
                }
                Response<CommentObj> response = apiService.updateComment(userId, videoId, comment.getId(), comment, "Bearer " + token).execute();
                if (response.isSuccessful() && response.body() != null) {
                    CommentObj updatedComment = response.body();
                    commentDao.update(updatedComment);
                    new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(updatedComment));
                    Log.d(TAG, "Comment updated successfully: " + updatedComment.getId());
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError(new Exception("Failed to update comment: " + response.message())));
                    Log.e(TAG, "Failed to update comment: " + response.message());
                }
            } catch (IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
                Log.e(TAG, "Error updating comment", e);
            }
        });
    }

    public void deleteComment(String userId, String videoId, String commentId, final RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                String token = getAuthToken();
                if (token == null) {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError(new Exception("User is not authenticated")));
                    return;
                }
                Response<Void> response = apiService.deleteComment(userId, videoId, commentId, "Bearer " + token).execute();
                if (response.isSuccessful()) {
                    commentDao.deleteById(commentId);
                    new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(null));
                    Log.d(TAG, "Comment deleted successfully: " + commentId);
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError(new Exception("Failed to delete comment: " + response.message())));
                    Log.e(TAG, "Failed to delete comment: " + response.message());
                }
            } catch (IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
                Log.e(TAG, "Error deleting comment", e);
            }
        });
    }

    private String getAuthToken() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String token = prefs.getString(KEY_AUTH_TOKEN, null);
        if (token == null) {
            Log.e(TAG, "Authentication token not found");
        }
        return token;
    }

    public static void saveAuthToken(Context context, String token) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.apply();
    }
}