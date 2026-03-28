package com.example.vocabularyapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "words")
public class Word {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String term;
    public String definition;
    public String example;
    public String pronunciation;
    public String category; // e.g., Travel, Business, Medical
    
    public int masteryLevel; // 0 to 100
    public long nextReviewTime; // Timestamp tính bằng milliseconds
    public int interval; // Số ngày cho lần ôn tập tiếp theo
    public float easeFactor; // Hệ số dễ (mặc định 2.5)
}
