package com.example.vocabularyapp.ui.vocabulary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.vocabularyapp.base.BaseFragment;
import com.example.vocabularyapp.data.local.entity.Word;
import com.example.vocabularyapp.databinding.FragmentVocabularyBinding;
import com.example.vocabularyapp.databinding.DialogAddWordBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VocabularyFragment extends BaseFragment<FragmentVocabularyBinding> {
    private VocabularyViewModel viewModel;
    private CategoryAdapter categoryAdapter;
    private boolean isFrontVisible = true;
    private List<Word> currentWords;
    private List<Word> sessionWords;
    private List<Word> weakWords = new ArrayList<>();
    private int currentIndex = 0;
    private boolean shouldResetIndex = true;

    private int understandCount = 0;
    private int notUnderstandCount = 0;
    private int skippedCount = 0;

    @Override
    protected FragmentVocabularyBinding inflateViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentVocabularyBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(VocabularyViewModel.class);
        initRecyclerView();
        initObservers();
    }

    private void initRecyclerView() {
        categoryAdapter = new CategoryAdapter();
        binding.rvCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCategories.setAdapter(categoryAdapter);

        categoryAdapter.setOnCategoryClickListener(categoryName -> {
            resetSessionData();
            shouldResetIndex = true;
            viewModel.setSelectedCategory(categoryName);
            binding.layoutFlashcard.setVisibility(View.VISIBLE);
            binding.rvCategories.setVisibility(View.GONE);
            binding.includeReport.layoutReport.setVisibility(View.GONE);
        });

        categoryAdapter.setOnCategoryLongClickListener(categoryName -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xóa chủ đề")
                    .setMessage("Bạn có chắc chắn muốn xóa chủ đề '" + categoryName + "'?")
                    .setPositiveButton("Xóa", (dialog, which) -> viewModel.deleteCategory(categoryName))
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void resetSessionData() {
        understandCount = 0;
        notUnderstandCount = 0;
        skippedCount = 0;
        weakWords.clear();
    }

    @Override
    protected void initView() {
        binding.cvFlashcard.setOnClickListener(v -> flipCard());

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    shouldResetIndex = true;
                    viewModel.setSearchQuery(query);
                    binding.layoutFlashcard.setVisibility(View.VISIBLE);
                    binding.rvCategories.setVisibility(View.GONE);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    binding.layoutFlashcard.setVisibility(View.GONE);
                    binding.rvCategories.setVisibility(View.VISIBLE);
                    binding.includeReport.layoutReport.setVisibility(View.GONE);
                } else {
                    shouldResetIndex = true;
                    viewModel.setSearchQuery(newText);
                }
                return true;
            }
        });

        binding.btnCloseFlashcard.setOnClickListener(v -> {
            binding.layoutFlashcard.setVisibility(View.GONE);
            binding.rvCategories.setVisibility(View.VISIBLE);
            viewModel.setSearchQuery("");
            viewModel.setSelectedCategory(null);
            shouldResetIndex = true;
        });

        binding.btnAgain.setOnClickListener(v -> {
            notUnderstandCount++;
            if (currentWords != null) weakWords.add(currentWords.get(currentIndex));
            submitReview(SpacedRepetitionHelper.Quality.AGAIN);
        });
        binding.btnHard.setOnClickListener(v -> {
            skippedCount++;
            if (currentWords != null) weakWords.add(currentWords.get(currentIndex));
            submitReview(SpacedRepetitionHelper.Quality.HARD);
        });
        binding.btnGood.setOnClickListener(v -> {
            understandCount++;
            submitReview(SpacedRepetitionHelper.Quality.GOOD);
        });
        binding.btnEasy.setOnClickListener(v -> {
            understandCount++;
            submitReview(SpacedRepetitionHelper.Quality.EASY);
        });

        binding.includeReport.btnRestartAll.setOnClickListener(v -> {
            resetSessionData();
            currentWords = new ArrayList<>(sessionWords);
            startNewStudySession();
        });

        binding.includeReport.btnRestartWeakOnly.setOnClickListener(v -> {
            if (weakWords.isEmpty()) {
                Toast.makeText(requireContext(), "Tuyệt vời! Bạn không có từ nào chưa thuộc.", Toast.LENGTH_SHORT).show();
                return;
            }
            List<Word> wordsToRetry = new ArrayList<>(weakWords);
            resetSessionData();
            currentWords = wordsToRetry;
            startNewStudySession();
        });

        binding.includeReport.btnBackToMenu.setOnClickListener(v -> {
            binding.includeReport.layoutReport.setVisibility(View.GONE);
            binding.rvCategories.setVisibility(View.VISIBLE);
            viewModel.setSelectedCategory(null);
        });

        binding.fabAddWord.setOnClickListener(v -> showAddWordsDialog());
    }

    private void startNewStudySession() {
        currentIndex = 0;
        if (currentWords != null && !currentWords.isEmpty()) {
            displayWord(currentWords.get(currentIndex));
            binding.layoutFlashcard.setVisibility(View.VISIBLE);
            binding.includeReport.layoutReport.setVisibility(View.GONE);
        }
    }

    private void showAddWordsDialog() {
        DialogAddWordBinding dialogBinding = DialogAddWordBinding.inflate(getLayoutInflater());
        List<Word> pendingWords = new ArrayList<>();

        // Sử dụng ô nhập liệu mới trong fragment (không còn AutoComplete cũ)
        dialogBinding.btnAddToList.setOnClickListener(v -> {
            String category = dialogBinding.etCategory.getText().toString().trim();
            String term = dialogBinding.etTerm.getText().toString().trim();
            String definition = dialogBinding.etDefinition.getText().toString().trim();
            String example = dialogBinding.etExample.getText().toString().trim();

            if (category.isEmpty() || term.isEmpty() || definition.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập Chủ đề, Thuật ngữ và Định nghĩa", Toast.LENGTH_SHORT).show();
                return;
            }

            Word word = new Word();
            word.term = term;
            word.definition = definition;
            word.category = category;
            word.example = example;
            word.masteryLevel = 0;
            word.easeFactor = 2.5f;
            word.nextReviewTime = System.currentTimeMillis();

            pendingWords.add(word);
            dialogBinding.tvAddedWordsCount.setText("Đã chuẩn bị: " + pendingWords.size() + " từ");
            String preview = pendingWords.stream().map(w -> w.term).collect(Collectors.joining(", "));
            dialogBinding.tvAddedWordsPreview.setText(preview);

            dialogBinding.etTerm.setText("");
            dialogBinding.etDefinition.setText("");
            dialogBinding.etExample.setText("");
            dialogBinding.etTerm.requestFocus();
        });

        new AlertDialog.Builder(requireContext())
                .setTitle("Tạo học phần")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Lưu", (dialog, which) -> {
                    if (!pendingWords.isEmpty()) {
                        viewModel.insertWords(pendingWords);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void initObservers() {
        viewModel.getCategoryStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) categoryAdapter.setCategories(stats);
        });

        viewModel.getDisplayWords().observe(getViewLifecycleOwner(), words -> {
            if (shouldResetIndex) {
                this.sessionWords = words;
                this.currentWords = words;
                currentIndex = 0;
                if (words != null && !words.isEmpty()) {
                    displayWord(words.get(currentIndex));
                }
                shouldResetIndex = false;
            }
        });
    }

    private void displayWord(Word word) {
        binding.tvWordFront.setText(word.term);
        binding.tvWordBack.setText(word.definition);
        binding.tvExample.setText(word.example != null ? word.example : "");
        binding.layoutFront.setVisibility(View.VISIBLE);
        binding.layoutBack.setVisibility(View.GONE);
        binding.layoutDifficulty.setVisibility(View.INVISIBLE);
        isFrontVisible = true;
    }

    private void submitReview(SpacedRepetitionHelper.Quality quality) {
        if (currentWords != null && currentIndex < currentWords.size()) {
            Word word = currentWords.get(currentIndex);
            viewModel.updateSpacedRepetition(word, quality);
            
            currentIndex++;
            if (currentIndex < currentWords.size()) {
                displayWord(currentWords.get(currentIndex));
            } else {
                showReport();
            }
        }
    }

    private void showReport() {
        binding.layoutFlashcard.setVisibility(View.GONE);
        binding.includeReport.layoutReport.setVisibility(View.VISIBLE);

        int total = understandCount + notUnderstandCount + skippedCount;
        if (total == 0) total = 1;

        binding.includeReport.tvUnderstandCount.setText(String.valueOf(understandCount));
        binding.includeReport.tvNotUnderstandCount.setText(String.valueOf(notUnderstandCount));
        binding.includeReport.tvSkippedCount.setText(String.valueOf(skippedCount));
        
        binding.includeReport.tvCorrectRatio.setText(understandCount + "/" + total);
        int percent = (understandCount * 100) / total;
        binding.includeReport.tvPercentage.setText(percent + "%");
        binding.includeReport.pbReportCircle.setMax(100);
        binding.includeReport.pbReportCircle.setProgress(percent);
    }

    private void flipCard() {
        final ObjectAnimator oa1 = ObjectAnimator.ofFloat(binding.cvFlashcard, "scaleX", 1f, 0f);
        final ObjectAnimator oa2 = ObjectAnimator.ofFloat(binding.cvFlashcard, "scaleX", 0f, 1f);
        oa1.setDuration(150);
        oa2.setDuration(150);
        oa1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (isFrontVisible) {
                    binding.layoutFront.setVisibility(View.GONE);
                    binding.layoutBack.setVisibility(View.VISIBLE);
                    binding.layoutDifficulty.setVisibility(View.VISIBLE);
                } else {
                    binding.layoutFront.setVisibility(View.VISIBLE);
                    binding.layoutBack.setVisibility(View.GONE);
                    binding.layoutDifficulty.setVisibility(View.INVISIBLE);
                }
                isFrontVisible = !isFrontVisible;
                oa2.start();
            }
        });
        oa1.start();
    }

    @Override protected void initData() {}
}
