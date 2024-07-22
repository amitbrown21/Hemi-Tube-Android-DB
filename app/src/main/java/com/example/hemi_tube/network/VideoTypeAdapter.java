package com.example.hemi_tube.network;

import com.example.hemi_tube.entities.Video;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class VideoTypeAdapter extends TypeAdapter<Video> {
    @Override
    public void write(JsonWriter out, Video video) throws IOException {
        // Implement this if you need to send Video objects to the server
    }

    @Override
    public Video read(JsonReader in) throws IOException {
        Video video = new Video();
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "_id":
                    video.setId(in.nextString());
                    break;
                case "url":
                    video.setUrl(in.nextString());
                    break;
                case "title":
                    video.setTitle(in.nextString());
                    break;
                case "owner":
                    if (in.peek() == JsonToken.BEGIN_OBJECT) {
                        in.beginObject();
                        String ownerId = null;
                        String ownerUsername = null;
                        while (in.hasNext()) {
                            switch (in.nextName()) {
                                case "_id":
                                    ownerId = in.nextString();
                                    break;
                                case "username":
                                    ownerUsername = in.nextString();
                                    break;
                                default:
                                    in.skipValue();
                                    break;
                            }
                        }
                        in.endObject();
                        video.setOwner(new Video.Owner(ownerId, ownerUsername));
                    } else {
                        String ownerId = in.nextString();
                        video.setOwner(new Video.Owner(ownerId, null));
                    }
                    break;
                case "views":
                    video.setViews(in.nextInt());
                    break;
                case "likes":
                    video.setLikes(in.nextInt());
                    break;
                case "dislikes":
                    video.setDislikes(in.nextInt());
                    break;
                case "thumbnail":
                    video.setThumbnail(in.nextString());
                    break;
                case "description":
                    video.setDescription(in.nextString());
                    break;
                case "duration":
                    video.setDuration(in.nextString());
                    break;
                case "date":
                    video.setDate(in.nextString());
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        in.endObject();
        return video;
    }
}