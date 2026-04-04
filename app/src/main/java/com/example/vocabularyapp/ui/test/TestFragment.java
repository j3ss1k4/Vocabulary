package com.example.vocabularyapp.ui.test;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.vocabularyapp.base.BaseFragment;
import com.example.vocabularyapp.data.local.AppDatabase;
import com.example.vocabularyapp.data.local.entity.Test;
import com.example.vocabularyapp.data.local.entity.Word;
import com.example.vocabularyapp.databinding.FragmentTestBinding;
import com.example.vocabularyapp.ui.vocabulary.CategoryAdapter;
import com.example.vocabularyapp.ui.vocabulary.VocabularyViewModel;

import java.util.ArrayList;
import java.util.List;

public class TestFragment extends BaseFragment<FragmentTestBinding> {
    
    private VocabularyViewModel viewModel;
    private CategoryAdapter adapter;

    @Override
    protected FragmentTestBinding inflateViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentTestBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        adapter = new CategoryAdapter();
        binding.rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCategories.setAdapter(adapter);

        // Khi click vào một chủ đề, lấy từ vựng của chủ đề đó để làm bài test
        adapter.setOnCategoryClickListener(categoryName -> {
            AppDatabase.getInstance(requireContext()).wordDao().getWordsByCategory(categoryName)
                    .observe(getViewLifecycleOwner(), words -> {
                        if (words != null && words.size() >= 4) {
                            startTestWithWords(categoryName, words);
                        } else {
                            Toast.makeText(getContext(), "Chủ đề này cần ít nhất 4 từ để làm bài test", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void startTestWithWords(String categoryName, List<Word> words) {
        Test mockTest = new Test();
        mockTest.title = "Kiểm tra: " + categoryName;
        mockTest.durationMinutes = 10;
        mockTest.totalQuestions = Math.min(words.size(), 20);

        Intent intent = new Intent(getActivity(), TestSessionActivity.class);
        intent.putExtra("test", mockTest);
        intent.putExtra("user_words", new ArrayList<>(words));
        startActivity(intent);
    }

    @Override
    protected void initData() {
        viewModel = new ViewModelProvider(this).get(VocabularyViewModel.class);
        viewModel.getCategoryStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null && !stats.isEmpty()) {
                adapter.setCategories(stats);
                binding.tvEmptyHint.setVisibility(View.GONE);
                binding.rvCategories.setVisibility(View.VISIBLE);
            } else {
                binding.tvEmptyHint.setVisibility(View.VISIBLE);
                binding.rvCategories.setVisibility(View.GONE);
            }
        });
    }
}
