package com.example.vocabularyapp.ui.forum;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.vocabularyapp.data.local.entity.Comment;
import com.example.vocabularyapp.data.local.entity.Post;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class ForumViewModel extends AndroidViewModel {
    private final FirebaseFirestore db;
    private final MutableLiveData<List<Post>> allPosts = new MutableLiveData<>();
    private final MutableLiveData<List<Comment>> postComments = new MutableLiveData<>();

    public ForumViewModel(@NonNull Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
        listenToPosts();
    }

    private void listenToPosts() {
        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        List<Post> posts = new ArrayList<>();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                            Post post = doc.toObject(Post.class);
                            if (post != null) {
                                post.id = Integer.parseInt(doc.getId().hashCode() + ""); // Tạm thời map ID
                                posts.add(post);
                            }
                        }
                        allPosts.setValue(posts);
                    }
                });
    }

    public LiveData<List<Post>> getAllPosts() {
        return allPosts;
    }

    public void addPost(String userId, String username, String content) {
        Post post = new Post(userId, username, content, System.currentTimeMillis());
        db.collection("posts").add(post);
    }

    public void likePost(Post post) {
        // Trong thực tế nên dùng transaction hoặc increment
        db.collection("posts")
                .whereEqualTo("timestamp", post.timestamp)
                .whereEqualTo("userId", post.userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String docId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        db.collection("posts").document(docId)
                                .update("likeCount", post.likeCount + 1);
                    }
                });
    }

    public LiveData<List<Comment>> getCommentsForPost(int postId) {
        db.collection("comments")
                .whereEqualTo("postId", postId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        postComments.setValue(value.toObjects(Comment.class));
                    }
                });
        return postComments;
    }

    public void addComment(int postId, String userId, String username, String content) {
        Comment comment = new Comment();
        comment.postId = postId;
        comment.userId = userId;
        comment.username = username;
        comment.content = content;
        comment.timestamp = System.currentTimeMillis();
        db.collection("comments").add(comment);
    }
}
