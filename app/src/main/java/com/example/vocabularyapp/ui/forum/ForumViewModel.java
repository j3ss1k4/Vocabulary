package com.example.vocabularyapp.ui.forum;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.vocabularyapp.data.local.AppDatabase;
import com.example.vocabularyapp.data.local.entity.Comment;
import com.example.vocabularyapp.data.local.entity.Post;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ForumViewModel extends AndroidViewModel {
    private final AppDatabase db;
    private final ExecutorService executorService;
    private final LiveData<List<Post>> allPosts;

    public ForumViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        executorService = Executors.newSingleThreadExecutor();
        allPosts = db.postDao().getAllPosts();
    }

    public LiveData<List<Post>> getAllPosts() {
        return allPosts;
    }

    public void addPost(String userId, String username, String content) {
        executorService.execute(() -> {
            Post post = new Post(userId, username, content, System.currentTimeMillis());
            db.postDao().insert(post);
        });
    }

    public void likePost(Post post) {
        executorService.execute(() -> {
            post.likeCount++;
            db.postDao().update(post);
        });
    }

    public LiveData<List<Comment>> getCommentsForPost(int postId) {
        return db.commentDao().getCommentsForPost(postId);
    }

    public void addComment(int postId, String userId, String username, String content) {
        executorService.execute(() -> {
            Comment comment = new Comment();
            comment.postId = postId;
            comment.userId = userId;
            comment.username = username;
            comment.content = content;
            comment.timestamp = System.currentTimeMillis();
            db.commentDao().insert(comment);
        });
    }
}
