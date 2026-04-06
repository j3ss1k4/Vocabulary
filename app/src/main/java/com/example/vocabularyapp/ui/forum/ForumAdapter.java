package com.example.vocabularyapp.ui.forum;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vocabularyapp.data.local.entity.Post;
import com.example.vocabularyapp.databinding.ItemPostBinding;
import java.util.ArrayList;
import java.util.List;

public class ForumAdapter extends RecyclerView.Adapter<ForumAdapter.PostViewHolder> {

    private List<Post> posts = new ArrayList<>();
    private final OnPostInteractionListener listener;

    public interface OnPostInteractionListener {
        void onLikeClick(Post post);
        void onCommentClick(Post post);
    }

    public ForumAdapter(OnPostInteractionListener listener) {
        this.listener = listener;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPostBinding binding = ItemPostBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PostViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        private final ItemPostBinding binding;

        PostViewHolder(ItemPostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Post post) {
            binding.tvUsername.setText(post.username);
            binding.tvContent.setText(post.content);
            binding.tvTimestamp.setText(String.valueOf(post.timestamp)); // Simplified, should format date
            binding.btnLike.setText("Like (" + post.likeCount + ")");

            binding.btnLike.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLikeClick(post);
                }
            });

            binding.btnComment.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCommentClick(post);
                }
            });
        }
    }
}
