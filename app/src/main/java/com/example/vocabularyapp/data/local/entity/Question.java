package com.example.vocabularyapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "questions",
        indices = {@Index(value = {"testId", "questionText", "type"}, unique = true)})
public class Question {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int testId;
    public String type; // MULTIPLE_CHOICE, FILL_BLANK, LISTENING_MC, LISTENING_DICTATION
    public String questionText;
    public String optionA;
    public String optionB;
    public String optionC;
    public String optionD;
    public String correctOption; // A, B, C, D or the text
    public String explanation;
    public String audioPath; // Path in assets or URL
}
