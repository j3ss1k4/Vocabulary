package com.example.vocabularyapp.ui.forum;

import android.app.AlertDialog;import android.view.LayoutInflater;
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
            }
        });
    }

    private void showCreatePostDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Tạo bài viết mới");

        final EditText input = new EditText(requireContext());
        input.setHint("Bạn đang nghĩ gì?");
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);
        builder.setView(input);

        builder.setPositiveButton("Đăng", (dialog, which) -> {
            String content = input.getText().toString().trim();
            if (!content.isEmpty()) {
                // Sử dụng thông tin mặc định, có thể thay thế bằng thông tin người dùng thật
                viewModel.addPost("1", "User Name", content);
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

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        root.setPadding(padding, padding, padding, padding);

        TextView title = new TextView(requireContext());
        title.setText("Bình luận cho bài viết của " + post.username);
        title.setTextSize(18);
        title.setPadding(0, 0, 0, padding);
        root.addView(title);

        RecyclerView rvComments = new RecyclerView(requireContext());
        rvComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        LinearLayout.LayoutParams rvParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        rvComments.setLayoutParams(rvParams);
        root.addView(rvComments);

        EditText etComment = new EditText(requireContext());
        etComment.setHint("Viết bình luận...");
        root.addView(etComment);

        builder.setView(root);
        builder.setPositiveButton("Gửi", (dialog, which) -> {
            String commentText = etComment.getText().toString().trim();
            if (!commentText.isEmpty()) {
                viewModel.addComment(post.id, "1", "User Name", commentText);
            }
        });
        builder.setNegativeButton("Đóng", null);

        AlertDialog dialog = builder.create();
        CommentsAdapter commentsAdapter = new CommentsAdapter(new ArrayList<>());
        rvComments.setAdapter(commentsAdapter);

        viewModel.getCommentsForPost(post.id).observe(getViewLifecycleOwner(), comments -> {
            if (comments != null) {
                commentsAdapter.setComments(comments);
            }
        });

        dialog.show();
    }

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
            holder.binding.tvTimestamp.setText("Vừa xong");
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