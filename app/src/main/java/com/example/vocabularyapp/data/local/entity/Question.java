package com.example.vocabularyapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "questions")
public class Question {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int testId;
    public String type; // MULTIPLE_CHOICE or FILL_BLANK
    public String questionText;
    public String optionA;
    public String optionB;
    public String optionC;
    public String optionD;
    public String correctOption; // A, B, C, D or the text for FILL_BLANK
    public String explanation;
}
