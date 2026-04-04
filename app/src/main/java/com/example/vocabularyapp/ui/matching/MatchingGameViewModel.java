package com.example.vocabularyapp.ui.matching;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.vocabularyapp.data.local.AppDatabase;
import com.example.vocabularyapp.data.local.dao.WordDao;
import com.example.vocabularyapp.data.local.entity.Word;

import java.util.List;

public class MatchingGameViewModel extends AndroidViewModel {
    private WordDao wordDao;

    public MatchingGameViewModel(@NonNull Application application) {
        super(application);
        wordDao = AppDatabase.getInstance(application).wordDao();
    }

    public LiveData<List<Word>> getAllWords() {
        return wordDao.getAllWords();
    }
}
