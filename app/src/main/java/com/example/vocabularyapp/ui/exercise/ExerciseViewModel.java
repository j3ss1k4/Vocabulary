package com.example.vocabularyapp.ui.exercise;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.vocabularyapp.data.local.AppDatabase;
import com.example.vocabularyapp.data.local.dao.QuestionDao;
import com.example.vocabularyapp.data.local.dao.TestDao;
import com.example.vocabularyapp.data.local.entity.Question;
import com.example.vocabularyapp.data.local.entity.Test;

import java.util.List;

public class ExerciseViewModel extends AndroidViewModel {
    private final TestDao testDao;
    private final QuestionDao questionDao;

    public ExerciseViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        testDao = db.testDao();
        questionDao = db.questionDao();
    }

    public LiveData<List<Test>> getAllTests() {
        return testDao.getAllTests();
    }

    public LiveData<List<Question>> getQuestionsForTest(int testId) {
        return questionDao.getQuestionsByTestId(testId);
    }

    // Use QuestionDao for random fetch directly from DB
    public LiveData<List<Question>> getRandomQuestions(int testId, int limit) {
        return questionDao.getRandomQuestionsByTestId(testId, limit);
    }
}
