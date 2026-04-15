package com.example.vocabularyapp.ui.forum;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vocabularyapp.base.BaseFragment;
import com.example.vocabularyapp.data.local.entity.Comment;
import com.example.vocabularyapp.data.local.entity.Post;
import com.example.vocabularyapp.databinding.FragmentForumBinding;
import com.example.vocabularyapp.databinding.ItemCommentBinding;
import java.util.ArrayList;
import java.util.List;

public class ForumFragment extends BaseFragment<FragmentForumBinding> implements ForumAdapter.OnPostInteractionListener {

    private ForumViewModel viewModel;
    private ForumAdapter adapter;

    @Override
    protected FragmentForumBinding inflateViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentForumBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        viewModel = new ViewModelProvider(this).get(ForumViewModel.class);
        adapter = new ForumAdapter(this);
        binding.rvPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPosts.setAdapter(adapter);

        binding.fabAddPost.setOnClickListener(v -> showCreatePostDialog());
    }

    @Override
    protected void initData() {
        viewModel.getAllPosts().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                adapter.setPosts(posts);
                binding.rvPosts.setVisibility(posts.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void showCreatePostDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Tạo bài viết mới");

        final EditText input = new EditText(requireContext());
        input.setHint("Bạn đang nghĩ gì?");
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);
        builder.setView(input);

        builder.setPositiveButton("Đăng", (dialog, which) -> {
            String content = input.getText().toString().trim();
            if (!content.isEmpty()) {
                // Lấy thông tin người dùng từ Session/Auth nếu có, ở đây dùng tạm "User"
                viewModel.addPost("user_id_123", "Người dùng", content);
                Toast.makeText(requireContext(), "Đã đăng bài viết", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Nội dung không được để trống", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public void onLikeClick(Post post) {
        viewModel.likePost(post);
    }

    @Override
    public void onCommentClick(Post post) {
        showCommentsDialog(post);
    }

    private void showCommentsDialog(Post post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        // Tạo View cho Dialog bình luận
        View dialogView = LayoutInflater.from(requireContext()).inflate(android.R.layout.list_content, null);
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int p = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(p, p, p, p);

        TextView title = new TextView(requireContext());
        title.setText("Bình luận (" + post.username + ")");
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(18);
        layout.addView(title);

        RecyclerView rvComments = new RecyclerView(requireContext());
        rvComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        LinearLayout.LayoutParams rvParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 400); // Chiều cao cố định
        rvComments.setLayoutParams(rvParams);
        layout.addView(rvComments);

        final EditText etComment = new EditText(requireContext());
        etComment.setHint("Viết bình luận...");
        layout.addView(etComment);

        builder.setView(layout);
        builder.setPositiveButton("Gửi", (dialog, which) -> {
            String commentText = etComment.getText().toString().trim();
            if (!commentText.isEmpty()) {
                viewModel.addComment(post.id, "user_id_123", "Người dùng", commentText);
            }
        });
        builder.setNegativeButton("Đóng", null);

        CommentsAdapter commentsAdapter = new CommentsAdapter(new ArrayList<>());
        rvComments.setAdapter(commentsAdapter);

        viewModel.getCommentsForPost(post.id).observe(getViewLifecycleOwner(), comments -> {
            if (comments != null) {
                commentsAdapter.setComments(comments);
                rvComments.scrollToPosition(comments.size() - 1);
            }
        });

        builder.show();
    }

    // Adapter nội bộ cho bình luận
    private static class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
        private List<Comment> comments;

        CommentsAdapter(List<Comment> comments) {
            this.comments = comments;
        }

        void setComments(List<Comment> comments) {
            this.comments = comments;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemCommentBinding binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new CommentViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
            Comment comment = comments.get(position);
            holder.binding.tvUsername.setText(comment.username);
            holder.binding.tvContent.setText(comment.content);
            
            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                    comment.timestamp, 
                    System.currentTimeMillis(), 
                    DateUtils.MINUTE_IN_MILLIS);
            holder.binding.tvTimestamp.setText(timeAgo);
        }

        @Override
        public int getItemCount() {
            return comments != null ? comments.size() : 0;
        }

        static class CommentViewHolder extends RecyclerView.ViewHolder {
            final ItemCommentBinding binding;
            CommentViewHolder(ItemCommentBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
