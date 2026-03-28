package com.example.vocabularyapp.ui.vocabulary;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.vocabularyapp.data.local.AppDatabase;
import com.example.vocabularyapp.data.local.dao.WordDao;
import com.example.vocabularyapp.data.local.entity.Word;
import com.example.vocabularyapp.databinding.ActivityAddFlashcardBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class AddFlashcardActivity extends AppCompatActivity {

    private ActivityAddFlashcardBinding binding;
    private FlashcardAddAdapter adapter;
    private String editCategoryName = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddFlashcardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editCategoryName = getIntent().getStringExtra("category_name");
        initView();
        
        if (editCategoryName != null) {
            loadExistingWords();
        }
    }

    private void initView() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new FlashcardAddAdapter();
        binding.rvFlashcards.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFlashcards.setAdapter(adapter);

        adapter.setOnCountChangeListener(count -> {
            binding.tvCount.setText(count + "/" + count);
        });

        binding.fabAdd.setOnClickListener(v -> {
            adapter.addNewCard();
            binding.rvFlashcards.post(() -> 
                binding.rvFlashcards.smoothScrollToPosition(adapter.getItemCount() - 1));
        });

        binding.btnSave.setOnClickListener(v -> saveFlashcards());

        if (editCategoryName != null) {
            binding.etCategory.setText(editCategoryName);
        }
    }

    private void loadExistingWords() {
        Executors.newSingleThreadExecutor().execute(() -> {
            WordDao dao = AppDatabase.getInstance(this).wordDao();
            // Lấy danh sách từ của category này. 
            // Vì getWordsByCategory trả về LiveData, ở đây ta lấy list 1 lần để edit.
            // Để đơn giản, ta có thể query trực tiếp list trong background thread.
            // Nhưng hiện tại WordDao chỉ có LiveData. 
            // Tôi sẽ dùng phương pháp observe 1 lần hoặc thêm method mới vào DAO.
            // Ở đây tôi sẽ sử dụng database.runInTransaction hoặc tương tự nếu cần, 
            // nhưng hãy thử dùng LiveData observeForever rồi remove ngay.
            runOnUiThread(() -> {
                dao.getWordsByCategory(editCategoryName).observe(this, words -> {
                    if (words != null && !words.isEmpty()) {
                        adapter.setInitialWords(words);
                    }
                });
            });
        });
    }

    private void saveFlashcards() {
        String category = binding.etCategory.getText().toString().trim();
        if (category.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập chủ đề", Toast.LENGTH_SHORT).show();
            return;
        }

        List<FlashcardAddAdapter.FlashcardInput> inputs = adapter.getInputs();
        List<Word> wordsToSave = new ArrayList<>();

        for (FlashcardAddAdapter.FlashcardInput input : inputs) {
            if (!input.term.trim().isEmpty() && !input.definition.trim().isEmpty()) {
                Word word = new Word();
                word.id = input.id; // Giữ ID cũ nếu là sửa, 0 nếu là mới
                word.term = input.term.trim();
                word.definition = input.definition.trim();
                word.category = category;
                word.masteryLevel = 0;
                word.easeFactor = 2.5f;
                word.nextReviewTime = System.currentTimeMillis();
                wordsToSave.add(word);
            }
        }

        if (wordsToSave.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập ít nhất một thuật ngữ và định nghĩa", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            WordDao dao = AppDatabase.getInstance(this).wordDao();
            
            // Nếu là sửa, ta xóa các từ cũ của category cũ trước (phòng trường hợp đổi tên category hoặc xóa bớt thẻ)
            if (editCategoryName != null) {
                dao.deleteByCategory(editCategoryName);
            }
            
            dao.insertAll(wordsToSave);
            
            runOnUiThread(() -> {
                Toast.makeText(this, "Đã lưu học phần!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
