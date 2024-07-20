package com.example.hemi_tube.repository;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.hemi_tube.dao.VideoDao;
import com.example.hemi_tube.database.AppDatabase;
import com.example.hemi_tube.entities.Video;
import com.example.hemi_tube.network.ApiService;
import com.example.hemi_tube.network.RetrofitClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

public class VideoRepository {
    private static final String TAG = "VideoRepository";
    private VideoDao videoDao;
    private ApiService apiService;
    private Executor executor;
    private Context context;

    public VideoRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        videoDao = db.videoDao();
        apiService = RetrofitClient.getInstance(context).getApi();
        executor = Executors.newSingleThreadExecutor();
        this.context = context.getApplicationContext();
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

    public void createVideo(String userId, Video video, Uri videoUri, final RepositoryCallback<Video> callback) {
        executor.execute(() -> {
            try {
                Log.d(TAG, "Creating video with URI: " + videoUri);

                // Use ContentResolver to open an input stream
                InputStream inputStream = context.getContentResolver().openInputStream(videoUri);
                if (inputStream == null) {
                    Log.e(TAG, "Failed to open input stream for video URI");
                    callback.onError(new Exception("Failed to open input stream for video"));
                    return;
                }

                // Create a temporary file
                File videoFile = File.createTempFile("upload_video", ".mp4", context.getCacheDir());
                FileOutputStream fos = new FileOutputStream(videoFile);

                // Copy the content of the input stream to the temporary file
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
                fos.close();
                inputStream.close();

                RequestBody videoBody = RequestBody.create(MediaType.parse("video/*"), videoFile);
                MultipartBody.Part videoPart = MultipartBody.Part.createFormData("video", "video.mp4", videoBody);

                RequestBody titleBody = RequestBody.create(MediaType.parse("text/plain"), video.getTitle());
                RequestBody descriptionBody = RequestBody.create(MediaType.parse("text/plain"), video.getDescription());
                RequestBody thumbnailBody = RequestBody.create(MediaType.parse("text/plain"), video.getThumbnail());
                RequestBody durationBody = RequestBody.create(MediaType.parse("text/plain"), video.getDuration());

                Response<Video> response = apiService.createVideo(userId, titleBody, descriptionBody, thumbnailBody, durationBody, videoPart).execute();
                if (response.isSuccessful() && response.body() != null) {
                    Video createdVideo = response.body();
                    videoDao.insert(createdVideo);
                    callback.onSuccess(createdVideo);
                    Log.d(TAG, "Video created successfully: " + createdVideo.getId());
                } else {
                    callback.onError(new Exception("Failed to create video: " + response.message()));
                    Log.e(TAG, "Failed to create video: " + response.message());
                }

                // Delete the temporary file
                videoFile.delete();

            } catch (IOException e) {
                Log.e(TAG, "Error creating video", e);
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
    private String getRealPathFromURI(Context context, Uri uri) {
        String filePath = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                if (id.startsWith("msf:")) {
                    return getDataColumn(context, uri, null, null);
                }
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                filePath = getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                // ... (keep the existing code for MediaProvider)
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            filePath = getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        }

        if (filePath == null) {
            // If we couldn't get the file path, return the URI string
            return uri.toString();
        }

        return filePath;
    }

    private String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int columnIndex = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting data column", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
