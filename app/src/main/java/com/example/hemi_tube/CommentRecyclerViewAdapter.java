package com.example.hemi_tube;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hemi_tube.entities.CommentObj;
import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.entities.Video;

import java.util.ArrayList;
import java.util.List;

public class CommentRecyclerViewAdapter extends RecyclerView.Adapter<CommentRecyclerViewAdapter.commentViewHolder> {

    Context context;
    List<CommentObj> commentObjList;
    List<User> userList;
    Video currentVideo;
    List<Video> videoList;
    VideoRecyclerViewAdapter videoAdapter;
    User currentUser;

    public CommentRecyclerViewAdapter(Context context, List<CommentObj> commentList, List<User> userList, Video currentVideo, List<Video> videoList, VideoRecyclerViewAdapter videoAdapter, User currentUser) {
        this.context = context;
        this.commentObjList = commentList;
        this.userList = userList;
        this.currentVideo = currentVideo;
        this.videoList = videoList;
        this.videoAdapter = videoAdapter;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public commentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.comment_item, parent, false);
        return new commentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull commentViewHolder holder, int position) {
        CommentObj currentComment = commentObjList.get(position);
        holder.username.setText(currentComment.getUsername());
        holder.body.setText(currentComment.getBody());

        // Find the user associated with the comment's username
        User commenter = null;
        for (User user : userList) {
            if (user.getUsername().equals(currentComment.getUsername())) {
                commenter = user;
                break;
            }
        }

        if (commenter != null) {
            String profilePicturePath = commenter.getProfilePicture();
            if (profilePicturePath != null && profilePicturePath.startsWith("content://")) {
                try {
                    holder.profilePic.setImageURI(Uri.parse(profilePicturePath));
                } catch (SecurityException e) {
                    Log.e("CommentRecyclerViewAdapter", "No access to content URI for profile picture", e);
                    holder.profilePic.setImageResource(R.drawable.profile); // Default profile picture
                }
            } else if (profilePicturePath != null && profilePicturePath.contains("/")) {
                int lastSlash = profilePicturePath.lastIndexOf('/');
                int lastDot = profilePicturePath.lastIndexOf('.');
                if (lastSlash >= 0 && lastDot > lastSlash) {
                    String profilePictureName = profilePicturePath.substring(lastSlash + 1, lastDot);
                    int profilePictureResourceId = context.getResources().getIdentifier(profilePictureName, "drawable", context.getPackageName());
                    if (profilePictureResourceId != 0) {
                        holder.profilePic.setImageResource(profilePictureResourceId);
                    } else {
                        holder.profilePic.setImageResource(R.drawable.profile); // Default profile picture
                    }
                } else {
                    holder.profilePic.setImageResource(R.drawable.profile); // Default profile picture
                }
            } else {
                holder.profilePic.setImageResource(R.drawable.profile); // Default profile picture
            }
        } else {
            holder.profilePic.setImageResource(R.drawable.profile); // Default profile picture
        }

        // Check if the current user is not null
        if (currentUser != null) {
            holder.editComment.setVisibility(View.VISIBLE);
            holder.editComment.setOnClickListener(v -> editComment(currentComment.getId()));
            holder.deleteComment.setVisibility(View.VISIBLE);
            holder.deleteComment.setOnClickListener(v -> deleteComment(currentComment.getId()));
        } else {
            holder.editComment.setVisibility(View.GONE);
            holder.deleteComment.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return commentObjList.size();
    }

    public static class commentViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        TextView body;
        ImageView profilePic;
        TextView editComment;
        TextView deleteComment;

        public commentViewHolder(@NonNull View CommentView) {
            super(CommentView);
            username = CommentView.findViewById(R.id.comment_username);
            body = CommentView.findViewById(R.id.comment_body);
            profilePic = CommentView.findViewById(R.id.profilePicture);
            editComment = CommentView.findViewById(R.id.editComment);
            deleteComment = CommentView.findViewById(R.id.deleteComment);
        }
    }

    private void deleteComment(int commentId) {
        for (int i = 0; i < commentObjList.size(); i++) {
            CommentObj comment = commentObjList.get(i);
            if (comment.getId() == commentId) {
                commentObjList.remove(i);
                notifyItemRemoved(i);
                notifyItemRangeChanged(i, commentObjList.size());

                // Update the current video's comments
                currentVideo.setComments(new ArrayList<>(commentObjList));

                // Update the video in the videoList
                for (int j = 0; j < videoList.size(); j++) {
                    if (videoList.get(j).getId() == currentVideo.getId()) {
                        videoList.set(j, currentVideo);
                        break;
                    }
                }

                // Notify the video adapter that the data has changed
                videoAdapter.notifyDataSetChanged();

                break;
            }
        }
    }

    private void editComment(int commentId) {
        for (CommentObj comment : commentObjList) {
            if (comment.getId() == commentId) {
                LayoutInflater inflater = LayoutInflater.from(context);
                View editCommentView = inflater.inflate(R.layout.edit_comment, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(editCommentView);

                EditText editText = editCommentView.findViewById(R.id.edit_comment_text);
                Button confirmButton = editCommentView.findViewById(R.id.confirm_edit_button);
                Button cancelButton = editCommentView.findViewById(R.id.cancel_edit_button);

                editText.setText(comment.getBody());

                AlertDialog dialog = builder.create();
                dialog.show();

                confirmButton.setOnClickListener(v -> {
                    String updatedText = editText.getText().toString();
                    if (!updatedText.isEmpty()) {
                        comment.setBody(updatedText);
                        notifyItemChanged(commentObjList.indexOf(comment));
                    }
                    dialog.dismiss();
                    updateVideoComments();
                });

                cancelButton.setOnClickListener(v -> {
                    dialog.dismiss();
                });

                break;
            }
        }
    }

    private void updateVideoComments() {
        currentVideo.setComments(new ArrayList<>(commentObjList));
        for (int i = 0; i < videoList.size(); i++) {
            if (videoList.get(i).getId() == currentVideo.getId()) {
                videoList.set(i, currentVideo);
                break;
            }
        }
        videoAdapter.notifyDataSetChanged(); // Update the video list display
    }

    public void submitComment(String newCommentBody, User currentUser) {
        if (newCommentBody.isEmpty()) {
            return;
        }

        if (currentUser == null) {
            Toast.makeText(context, "Sign in to comment", Toast.LENGTH_SHORT).show();
            return;
        }

        CommentObj newComment = new CommentObj(currentUser.getUsername(), newCommentBody);
        commentObjList.add(newComment);

        // Update the current video's comments
        currentVideo.setComments(new ArrayList<>(commentObjList));

        // Update the video in the videoList
        for (int j = 0; j < videoList.size(); j++) {
            if (videoList.get(j).getId() == currentVideo.getId()) {
                videoList.set(j, currentVideo);
                break;
            }
        }

        // Notify the adapter of the change
        notifyDataSetChanged();
        videoAdapter.notifyDataSetChanged();
    }
}
