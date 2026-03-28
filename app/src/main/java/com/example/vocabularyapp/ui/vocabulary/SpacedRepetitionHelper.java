package com.example.vocabularyapp.ui.vocabulary;

import com.example.vocabularyapp.data.local.entity.Word;

import java.util.Calendar;

public class SpacedRepetitionHelper {

    // Định nghĩa mức độ đánh giá của người dùng
    public enum Quality {
        AGAIN(0), // Quên hoàn toàn (Khó)
        HARD(1),  // Nhớ mang máng (Trung bình)
        GOOD(2),  // Nhớ (Dễ)
        EASY(3);  // Thuộc làu (Rất dễ)

        private final int value;
        Quality(int value) { this.value = value; }
        public int getValue() { return value; }
    }

    /**
     * Thuật toán SuperMemo-2 (đơn giản hóa)
     * Tính toán khoảng thời gian (interval) và hệ số dễ (easeFactor) tiếp theo.
     */
    public static void updateWordReview(Word word, Quality quality) {
        int q = quality.getValue();
        
        // Cập nhật Ease Factor (EF)
        // EF' = EF + (0.1 - (3-q) * (0.08 + (3-q) * 0.02))
        float newEaseFactor = word.easeFactor + (0.1f - (3 - q) * (0.08f + (3 - q) * 0.02f));
        if (newEaseFactor < 1.3f) newEaseFactor = 1.3f;
        word.easeFactor = newEaseFactor;

        // Cập nhật Interval (I) - Số ngày
        if (q < 1) { // Nếu chọn AGAIN (Khó/Quên)
            word.interval = 1;
        } else {
            if (word.interval == 0) word.interval = 1;
            else if (word.interval == 1) word.interval = 3; // Lần 2 ôn tập sau 3 ngày
            else word.interval = Math.round(word.interval * word.easeFactor);
        }

        // Cập nhật Mastery Level (0-100)
        int currentMastery = word.masteryLevel;
        if (q == 3) currentMastery += 20; // Rất dễ
        else if (q == 2) currentMastery += 10; // Dễ
        else if (q == 1) currentMastery += 5;  // Trung bình
        else currentMastery -= 10; // Khó (quên)

        if (currentMastery > 100) currentMastery = 100;
        if (currentMastery < 0) currentMastery = 0;
        word.masteryLevel = currentMastery;

        // Cập nhật thời gian review tiếp theo (Timestamp)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, word.interval);
        word.nextReviewTime = calendar.getTimeInMillis();
    }
}
