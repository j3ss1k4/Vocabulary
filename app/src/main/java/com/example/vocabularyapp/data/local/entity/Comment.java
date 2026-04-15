package com.example.vocabularyapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.firebase.firestore.IgnoreExtraProperties;

@Entity(tableName = "comments")
@IgnoreExtraProperties
public class Comment {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int postId;
    public String userId;
    public String username;
    public String content;
    public long timestamp;

    public Comment() {}
}
