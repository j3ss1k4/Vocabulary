package com.example.vocabularyapp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.vocabularyapp.data.local.entity.Comment;

import java.util.List;

@Dao
public interface CommentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Comment comment);

    @Update
    void update(Comment comment);

    @Delete
    void delete(Comment comment);

    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp ASC")
    LiveData<List<Comment>> getCommentsForPost(int postId);
}
