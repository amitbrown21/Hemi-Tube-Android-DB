package com.example.hemi_tube.network;

import com.example.hemi_tube.entities.CommentObj;
import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.entities.Video;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    // User endpoints
    @GET("users")
    Call<List<User>> getAllUsers();

    @GET("users/{id}")
    Call<User> getUserById(@Path("id") int userId);

    @GET("users/{username}")
    Call<User> getUserByUsername(@Path("username") String username);

    @POST("users")
    Call<User> createUser(@Body User user);

    @PUT("users/{id}")
    Call<User> updateUser(@Path("id") int userId, @Body User user);

    @DELETE("users/{id}")
    Call<Void> deleteUser(@Path("id") int userId);

    @GET("users/{id}/videos")
    Call<List<Video>> getUserVideos(@Path("id") int userId);

    @POST("users/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @GET("users/verify-token")
    Call<User> verifyToken();

    // Video endpoints
    @GET("videos")
    Call<List<Video>> getAllVideos();

    @GET("videos/{pid}")
    Call<Video> getVideoById(@Path("pid") int videoId);

    @POST("users/{id}/videos")
    Call<Video> createVideo(@Path("id") int userId, @Body Video video);

    @PUT("videos/{pid}")
    Call<Video> updateVideo(@Path("pid") int videoId, @Body Video video);

    @DELETE("videos/{pid}")
    Call<Void> deleteVideo(@Path("pid") int videoId);

    @POST("videos/{pid}/incrementViews")
    Call<Video> incrementViews(@Path("pid") int videoId);

    @POST("videos/{pid}/incrementLikes")
    Call<Video> incrementLikes(@Path("pid") int videoId);

    @POST("videos/{pid}/decrementLikes")
    Call<Video> decrementLikes(@Path("pid") int videoId);

    @POST("videos/{pid}/incrementDislikes")
    Call<Video> incrementDislikes(@Path("pid") int videoId);

    @POST("videos/{pid}/decrementDislikes")
    Call<Video> decrementDislikes(@Path("pid") int videoId);

    @GET("videos/all")
    Call<List<Video>> getAllVideosWithTopAndRandom();

    // Comment endpoints
    @GET("users/{id}/videos/{pid}/comments")
    Call<List<CommentObj>> getCommentsByVideoId(@Path("id") int userId, @Path("pid") int videoId);

    @POST("users/{id}/videos/{pid}/comments")
    Call<CommentObj> createComment(@Path("id") int userId, @Path("pid") int videoId, @Body CommentObj comment);

    @GET("users/{id}/videos/{pid}/comments/{commentId}")
    Call<CommentObj> getCommentById(@Path("id") int userId, @Path("pid") int videoId, @Path("commentId") int commentId);

    @PUT("users/{id}/videos/{pid}/comments/{commentId}")
    Call<CommentObj> updateComment(@Path("id") int userId, @Path("pid") int videoId, @Path("commentId") int commentId, @Body CommentObj comment);

    @DELETE("users/{id}/videos/{pid}/comments/{commentId}")
    Call<Void> deleteComment(@Path("id") int userId, @Path("pid") int videoId, @Path("commentId") int commentId);

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

    class LoginResponse {
        String token;
        int userId;
    }
}