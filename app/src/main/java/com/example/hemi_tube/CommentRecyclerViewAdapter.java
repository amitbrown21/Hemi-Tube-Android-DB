package com.example.hemi_tube;

import android.app.Activity;
import android.content.Context;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.hemi_tube.dao.CommentDao;
import com.example.hemi_tube.dao.UserDao;
import com.example.hemi_tube.dao.VideoDao;
import com.example.hemi_tube.entities.CommentObj;
import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.entities.Video;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommentRecyclerViewAdapter extends RecyclerView.Adapter<CommentRecyclerViewAdapter.CommentViewHolder> {

    private Context context;
    private List<CommentObj> commentList;
    private UserDao userDao;
    private CommentDao commentDao;
    private Video currentVideo;
    private VideoDao videoDao;
    private User currentUser;
    private ExecutorService executorService;

    public CommentRecyclerViewAdapter(Context context, List<CommentObj> commentList, UserDao userDao, CommentDao commentDao, Video currentVideo, VideoDao videoDao, User currentUser) {
        this.context = context;
        this.commentList = commentList;
        this.userDao = userDao;
        this.commentDao = commentDao;
        this.currentVideo = currentVideo;
        this.videoDao = videoDao;
        this.currentUser = currentUser;
        this.executorService = Executors.newSingleThreadExecutor();
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

        executorService.execute(() -> {
            User commenter = userDao.getUserByUsername(comment.getUsername());
            if (commenter != null) {
                ((Activity) context).runOnUiThread(() ->
                        setProfilePicture(holder.profilePic, commenter.getProfilePicture())
                );
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
                executorService.execute(() -> {
                    comment.setBody(updatedText);
                    commentDao.update(comment);
                    ((Activity) context).runOnUiThread(this::notifyDataSetChanged);
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
                    executorService.execute(() -> {
                        commentDao.delete(comment);
                        List<CommentObj> updatedComments = commentDao.getCommentsForVideo(currentVideo.getId());
                        ((Activity) context).runOnUiThread(() -> updateComments(updatedComments));
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

        executorService.execute(() -> {
            CommentObj newComment = new CommentObj(currentVideo.getId(), currentUser.getUsername(), newCommentBody);
            commentDao.insert(newComment);
            List<CommentObj> updatedComments = commentDao.getCommentsForVideo(currentVideo.getId());
            ((Activity) context).runOnUiThread(() -> updateComments(updatedComments));
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

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}