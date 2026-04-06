package com.example.vocabularyapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "posts")
public class Post {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String userId;
    public String username;
    public String content;
    public long timestamp;
    public int likeCount;

    public Post() {}

    public Post(String userId, String username, String content, long timestamp) {
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.timestamp = timestamp;
        this.likeCount = 0;
    }
}
