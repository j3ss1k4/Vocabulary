package com.example.vocabularyapp.ui.vocabulary;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.vocabularyapp.data.local.AppDatabase;
import com.example.vocabularyapp.data.local.dao.WordDao;
import com.example.vocabularyapp.data.local.entity.Word;
import com.example.vocabularyapp.data.local.model.CategoryInfo;

import java.util.List;
import java.util.concurrent.Executors;

public class VocabularyViewModel extends AndroidViewModel {
    private final WordDao wordDao;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> selectedCategory = new MutableLiveData<>(null);
    private final LiveData<List<Word>> displayWords;

    public VocabularyViewModel(@NonNull Application application) {
        super(application);
        wordDao = AppDatabase.getInstance(application).wordDao();
        
        displayWords = Transformations.switchMap(searchQuery, query -> {
            if (query != null && !query.isEmpty()) {
                selectedCategory.setValue(null);
                return wordDao.searchWords("%" + query + "%");
            } else {
                return Transformations.switchMap(selectedCategory, category -> {
                    if (category == null) return wordDao.getAllWords();
                    return wordDao.getWordsByCategory(category);
                });
            }
        });
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void setSelectedCategory(String category) {
        selectedCategory.setValue(category);
    }

    public LiveData<List<Word>> getDisplayWords() {
        return displayWords;
    }

    public LiveData<List<CategoryInfo>> getCategoryStats() {
        return wordDao.getCategoryStats();
    }

    public LiveData<List<String>> getAllCategories() {
        return wordDao.getAllCategories();
    }

    public void insertWords(List<Word> words) {
        Executors.newSingleThreadExecutor().execute(() -> {
            wordDao.insertAll(words);
        });
    }

    public void deleteCategory(String category) {
        Executors.newSingleThreadExecutor().execute(() -> {
            wordDao.deleteByCategory(category);
        });
    }

    public void updateSpacedRepetition(Word word, SpacedRepetitionHelper.Quality quality) {
        Executors.newSingleThreadExecutor().execute(() -> {
            SpacedRepetitionHelper.updateWordReview(word, quality);
            wordDao.update(word);
        });
    }
}
