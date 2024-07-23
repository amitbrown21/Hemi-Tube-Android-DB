package com.example.hemi_tube;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hemi_tube.entities.CommentObj;
import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.entities.Video;
import com.example.hemi_tube.repository.RepositoryCallback;
import com.example.hemi_tube.viewmodel.CommentViewModel;
import com.example.hemi_tube.viewmodel.UserViewModel;
import com.example.hemi_tube.viewmodel.VideoViewModel;

import java.util.List;

public class CommentRecyclerViewAdapter extends RecyclerView.Adapter<CommentRecyclerViewAdapter.CommentViewHolder> {

    private Context context;
    private List<CommentObj> commentList;
    private UserViewModel userViewModel;
    private CommentViewModel commentViewModel;
    private Video currentVideo;
    private VideoViewModel videoViewModel;
    private User currentUser;

    public CommentRecyclerViewAdapter(Context context, List<CommentObj> commentList, UserViewModel userViewModel, CommentViewModel commentViewModel, Video currentVideo, VideoViewModel videoViewModel, User currentUser) {
        this.context = context;
        this.commentList = commentList;
        this.userViewModel = userViewModel;
        this.commentViewModel = commentViewModel;
        this.currentVideo = currentVideo;
        this.videoViewModel = videoViewModel;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentObj comment = commentList.get(position);
        holder.username.setText(comment.getUsername());
        holder.body.setText(comment.getBody());

        setProfilePicture(holder.profilePic, comment.getProfilePicture());

        holder.profilePic.setOnClickListener(v -> openChannelActivity(comment.getUserId()));
        holder.username.setOnClickListener(v -> openChannelActivity(comment.getUserId()));

        boolean isCurrentUserComment = currentUser != null && currentUser.getId().equals(comment.getUserId());
        holder.editComment.setVisibility(isCurrentUserComment ? View.VISIBLE : View.GONE);
        holder.deleteComment.setVisibility(isCurrentUserComment ? View.VISIBLE : View.GONE);

        holder.editComment.setOnClickListener(v -> editComment(comment));
        holder.deleteComment.setOnClickListener(v -> deleteComment(comment));
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public void updateComments(List<CommentObj> newComments) {
        this.commentList.clear();
        this.commentList.addAll(newComments);
        notifyDataSetChanged();
    }

    private void editComment(CommentObj comment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.edit_comment, null);
        builder.setView(dialogView);

        EditText editText = dialogView.findViewById(R.id.edit_comment_text);
        Button confirmButton = dialogView.findViewById(R.id.confirm_edit_button);
        Button cancelButton = dialogView.findViewById(R.id.cancel_edit_button);

        editText.setText(comment.getBody());

        AlertDialog dialog = builder.create();

        confirmButton.setOnClickListener(v -> {
            String updatedText = editText.getText().toString();
            if (!updatedText.isEmpty()) {
                comment.setBody(updatedText);
                commentViewModel.updateComment(currentUser.getId(), currentVideo.getId(), comment, new RepositoryCallback<CommentObj>() {
                    @Override
                    public void onSuccess(CommentObj result) {
                        ((Activity) context).runOnUiThread(() -> {
                            notifyDataSetChanged();
                            Toast.makeText(context, "Comment updated successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        ((Activity) context).runOnUiThread(() -> {
                            Toast.makeText(context, "Failed to update comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            } else {
                Toast.makeText(context, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void deleteComment(CommentObj comment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.delete_comment_dialog, null);
        builder.setView(dialogView);

        Button confirmButton = dialogView.findViewById(R.id.confirm_delete_button);
        Button cancelButton = dialogView.findViewById(R.id.cancel_delete_button);

        AlertDialog dialog = builder.create();

        confirmButton.setOnClickListener(v -> {
            commentViewModel.deleteComment(currentUser.getId(), currentVideo.getId(), comment.getId(), new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    ((Activity) context).runOnUiThread(() -> {
                        commentViewModel.getCommentsForVideo(currentUser.getId(), currentVideo.getId()).observe((LifecycleOwner) context, comments -> {
                            CommentRecyclerViewAdapter.this.updateComments(comments);
                        });
                        Toast.makeText(context, "Comment deleted successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                }

                @Override
                public void onError(Exception e) {
                    ((Activity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "Failed to delete comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    public void submitComment(String newCommentBody) {
        if (newCommentBody.isEmpty()) {
            return;
        }

        if (currentUser == null) {
            Toast.makeText(context, "Please sign in to comment", Toast.LENGTH_SHORT).show();
            return;
        }

        CommentObj newComment = new CommentObj(
                currentVideo.getId(),
                currentUser.getUsername(),
                newCommentBody,
                currentUser.getProfilePicture(),
                currentUser.getId()
        );

        commentViewModel.createComment(currentUser.getId(), currentVideo.getId(), newComment, new RepositoryCallback<CommentObj>() {
            @Override
            public void onSuccess(CommentObj result) {
                commentViewModel.getCommentsForVideo(currentUser.getId(), currentVideo.getId()).observe((LifecycleOwner) context, comments -> {
                    CommentRecyclerViewAdapter.this.updateComments(comments);
                });
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(context, "Failed to add comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setProfilePicture(ImageView imageView, String picturePath) {
        if (picturePath == null || picturePath.isEmpty()) {
            imageView.setImageResource(R.drawable.profile);
            return;
        }

        String imageUrl = picturePath;
        if (!picturePath.startsWith("http://") && !picturePath.startsWith("https://")) {
            imageUrl = "http://10.0.2.2:3000/" + picturePath.replace("\\", "/");
        }

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .circleCrop()  // This will make the image circular
                .into(imageView);

        Log.d("CommentAdapter", "Loading profile picture from: " + imageUrl);
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        TextView body;
        ImageView profilePic;
        TextView editComment;
        TextView deleteComment;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.comment_username);
            body = itemView.findViewById(R.id.comment_body);
            profilePic = itemView.findViewById(R.id.profilePicture);
            editComment = itemView.findViewById(R.id.editComment);
            deleteComment = itemView.findViewById(R.id.deleteComment);
        }
    }

    private void openChannelActivity(String userId) {
        Intent intent = new Intent(context, ChannelActivity.class);
        intent.putExtra("userId", userId);
        context.startActivity(intent);
    }
}