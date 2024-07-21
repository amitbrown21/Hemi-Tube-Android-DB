package com.example.hemi_tube;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

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

        userViewModel.getUserByUsername(comment.getUsername()).observe((LifecycleOwner) context, commenter -> {
            if (commenter != null) {
                setProfilePicture(holder.profilePic, commenter.getProfilePicture());

                // Add click listener to the profile picture
                holder.profilePic.setOnClickListener(v -> openChannelActivity(commenter.getId()));

                // Add click listener to the username text
                holder.username.setOnClickListener(v -> openChannelActivity(commenter.getId()));

                // Store the commenter's ID in the holder's tag for later use if needed
                holder.itemView.setTag(commenter.getId());
            }
        });

        boolean isCurrentUserComment = currentUser != null && currentUser.getUsername().equals(comment.getUsername());
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

        TextView editText = dialogView.findViewById(R.id.edit_comment_text);
        editText.setText(comment.getBody());

        builder.setPositiveButton("Save", (dialog, which) -> {
            String updatedText = editText.getText().toString();
            if (!updatedText.isEmpty()) {
                comment.setBody(updatedText);
                commentViewModel.updateComment(currentUser.getId(), currentVideo.getId(), comment, new RepositoryCallback<CommentObj>() {
                    @Override
                    public void onSuccess(CommentObj result) {
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(context, "Failed to update comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteComment(CommentObj comment) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Comment")
                .setMessage("Are you sure you want to delete this comment?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    commentViewModel.deleteComment(currentUser.getId(), currentVideo.getId(), comment.getId(), new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            commentViewModel.getCommentsForVideo(currentVideo.getId()).observe((LifecycleOwner) context, comments -> {
                                CommentRecyclerViewAdapter.this.updateComments(comments);
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(context, "Failed to delete comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void submitComment(String newCommentBody) {
        if (newCommentBody.isEmpty()) {
            return;
        }

        if (currentUser == null) {
            Toast.makeText(context, "Please sign in to comment", Toast.LENGTH_SHORT).show();
            return;
        }

        CommentObj newComment = new CommentObj(null, currentVideo.getId(), currentUser.getUsername(), newCommentBody, currentUser.getProfilePicture(), currentUser.getId());

        commentViewModel.createComment(newComment, new RepositoryCallback<CommentObj>() {
            @Override
            public void onSuccess(CommentObj result) {
                commentViewModel.getCommentsForVideo(currentVideo.getId()).observe((LifecycleOwner) context, comments -> {
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
        if (picturePath == null) {
            imageView.setImageResource(R.drawable.profile);
        } else if (picturePath.startsWith("content://")) {
            try {
                imageView.setImageURI(Uri.parse(picturePath));
            } catch (SecurityException e) {
                Log.e("CommentRecyclerViewAdapter", "No access to content URI for profile picture", e);
                imageView.setImageResource(R.drawable.profile);
            }
        } else {
            int resourceId = context.getResources().getIdentifier(picturePath, "drawable", context.getPackageName());
            imageView.setImageResource(resourceId != 0 ? resourceId : R.drawable.profile);
        }
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
