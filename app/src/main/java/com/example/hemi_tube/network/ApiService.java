package com.example.hemi_tube.network;

import com.example.hemi_tube.entities.CommentObj;
import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.entities.Video;
import com.example.hemi_tube.entities.VideoResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // User endpoints
    @GET("users")
    Call<List<User>> getAllUsers();

    @GET("users/{id}")
    Call<User> getUserById(@Path("id") String userId);

    @GET("users/{username}")
    Call<User> getUserByUsername(@Path("username") String username);

    @GET("videos/{videoId}/comments")
    Call<List<CommentObj>> getCommentsByVideoId(@Path("videoId") String videoId);

    @POST("users")
    Call<User> createUser(@Body User user);

    @PUT("users/{id}")
    Call<User> updateUser(@Path("id") String userId, @Body User user);

    @DELETE("users/{id}")
    Call<Void> deleteUser(@Path("id") String userId);

    @GET("users/{id}/videos")
    Call<List<Video>> getUserVideos(@Path("id") String userId);

    @POST("users/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @GET("users/verify-token")
    Call<User> verifyToken();

    // Video endpoints
    @GET("videos")
    Call<VideoResponse> getAllVideos();

    @GET("videos/{pid}")
    Call<Video> getVideoById(@Path("pid") String videoId);

    @POST("users/{id}/videos")
    Call<Video> createVideo(@Path("id") String userId, @Body Video video);

    @PUT("videos/{pid}")
    Call<Video> updateVideo(@Path("pid") String videoId, @Body Video video);

    @DELETE("videos/{pid}")
    Call<Void> deleteVideo(@Path("pid") String videoId);

    @POST("videos/{pid}/incrementViews")
    Call<Video> incrementViews(@Path("pid") String videoId);

    @POST("videos/{pid}/incrementLikes")
    Call<Video> incrementLikes(@Path("pid") String videoId);

    @POST("videos/{pid}/decrementLikes")
    Call<Video> decrementLikes(@Path("pid") String videoId);

    @POST("videos/{pid}/incrementDislikes")
    Call<Video> incrementDislikes(@Path("pid") String videoId);

    @POST("videos/{pid}/decrementDislikes")
    Call<Video> decrementDislikes(@Path("pid") String videoId);

    @GET("videos/all")
    Call<List<Video>> getAllVideosWithTopAndRandom();

    @GET("videos/search")
    Call<List<Video>> searchVideos(@Query("query") String query);

    // Comment endpoints
    @GET("users/{id}/videos/{pid}/comments")
    Call<List<CommentObj>> getCommentsByVideoId(@Path("id") int userId, @Path("pid") int videoId);

    @POST("users/{id}/videos/{pid}/comments")
    Call<CommentObj> createComment(@Path("id") String userId, @Path("pid") String videoId, @Body CommentObj comment);

    @GET("users/{id}/videos/{pid}/comments/{commentId}")
    Call<CommentObj> getCommentById(@Path("id") String userId, @Path("pid") String videoId, @Path("commentId") String commentId);

    @PUT("users/{id}/videos/{pid}/comments/{commentId}")
    Call<CommentObj> updateComment(@Path("id") String userId, @Path("pid") String videoId, @Path("commentId") String commentId, @Body CommentObj comment);

    @DELETE("users/{id}/videos/{pid}/comments/{commentId}")
    Call<Void> deleteComment(@Path("id") String userId, @Path("pid") String videoId, @Path("commentId") String commentId);

    // Additional classes for login
    class LoginRequest {
        String username;
        String password;

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    class SignUpResponse {
        User user;
        String token;
    }

    public class LoginResponse {
        public String token;
        public String userId;

        public LoginResponse(String token, String userId) {
            this.token = token;
            this.userId = userId;
        }
    }
}