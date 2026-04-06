package com.example.vocabularyapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "comments",
        foreignKeys = @ForeignKey(entity = Post.class,
                parentColumns = "id",
                childColumns = "postId",
                onDelete = ForeignKey.CASCADE))
public class Comment {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int postId;
    public String userId;
    public String username;
    public String content;
    public long timestamp;
}
