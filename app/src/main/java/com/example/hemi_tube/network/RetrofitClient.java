package com.example.hemi_tube.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.hemi_tube.entities.Video;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://10.0.2.2:3000/api/";
    private static RetrofitClient mInstance;
    private Retrofit retrofit;

    private RetrofitClient(Context context) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
                    String token = sharedPreferences.getString("jwt_token", null);
                    if (token != null) {
                        Request.Builder requestBuilder = original.newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .method(original.method(), original.body());
                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    }
                    return chain.proceed(original);
                })
                .build();

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Video.class, new JsonDeserializer<Video>() {
                    @Override
                    public Video deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        JsonObject jsonObject = json.getAsJsonObject();
                        Video video = new Gson().fromJson(jsonObject, Video.class);

                        if (jsonObject.has("owner") && jsonObject.get("owner").isJsonObject()) {
                            JsonObject ownerObject = jsonObject.getAsJsonObject("owner");
                            Video.Owner owner = new Gson().fromJson(ownerObject, Video.Owner.class);
                            video.setOwner(owner);
                        }

                        return video;
                    }
                })
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        // Log the successful creation of the Retrofit instance
        Log.d("RetrofitClient", "Retrofit instance created with base URL: " + BASE_URL);
    }

    public static synchronized RetrofitClient getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RetrofitClient(context.getApplicationContext());
        }
        return mInstance;
    }

    public ApiService getApi() {
        return retrofit.create(ApiService.class);
    }
}