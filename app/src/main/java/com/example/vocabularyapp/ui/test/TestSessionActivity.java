package com.example.vocabularyapp.ui.test;

import android.content.Intent;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Toast;

import com.example.vocabularyapp.base.BaseActivity;
import com.example.vocabularyapp.data.local.entity.Question;
import com.example.vocabularyapp.data.local.entity.Test;
import com.example.vocabularyapp.data.local.entity.Word;
import com.example.vocabularyapp.databinding.ActivityTestSessionBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class TestSessionActivity extends BaseActivity<ActivityTestSessionBinding> {

    private Test currentTest;
    private List<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;

    @Override
    protected ActivityTestSessionBinding inflateViewBinding() {
        return ActivityTestSessionBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        currentTest = (Test) getIntent().getSerializableExtra("test");
        if (currentTest == null) {
            finish();
            return;
        }

        binding.btnNext.setOnClickListener(v -> checkAnswerAndNext());
    }

    @Override
    protected void initData() {
        // Nhận dữ liệu từ Intent thay vì Database
        ArrayList<Word> userWords = (ArrayList<Word>) getIntent().getSerializableExtra("user_words");

        if (userWords != null && userWords.size() >= 4) {
            generateQuestionsFromWords(userWords);
            startTest();
        } else {
            Toast.makeText(this, "Không có đủ dữ liệu để tạo bài kiểm tra", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void generateQuestionsFromWords(List<Word> words) {
        questions = new ArrayList<>();
        List<Word> shuffledWords = new ArrayList<>(words);
        Collections.shuffle(shuffledWords);

        // Giới hạn số câu hỏi
        int numQuestions = Math.min(shuffledWords.size(), currentTest.totalQuestions);
        if (numQuestions > 20) numQuestions = 20;

        for (int i = 0; i < numQuestions; i++) {
            Word correctWord = shuffledWords.get(i);
            Question q = new Question();
            q.type = "MULTIPLE_CHOICE";
            q.questionText = "Nghĩa của từ \"" + correctWord.term + "\" là gì?";
            
            // Tạo 4 lựa chọn
            List<String> options = new ArrayList<>();
            options.add(correctWord.definition); // Đáp án đúng
            
            // Lấy 3 định nghĩa sai ngẫu nhiên từ chính danh sách người dùng nhập
            List<Word> otherWords = new ArrayList<>(words);
            otherWords.remove(correctWord);
            Collections.shuffle(otherWords);
            for (int j = 0; j < 3 && j < otherWords.size(); j++) {
                options.add(otherWords.get(j).definition);
            }

            // Đảm bảo đủ 4 phương án nếu có thể
            while (options.size() < 4 && options.size() < words.size()) {
                // Trường hợp này hiếm khi xảy ra nếu words.size() >= 4
                break; 
            }

            Collections.shuffle(options);
            
            if (options.size() >= 1) q.optionA = options.get(0);
            if (options.size() >= 2) q.optionB = options.get(1);
            if (options.size() >= 3) q.optionC = options.get(2);
            if (options.size() >= 4) q.optionD = options.get(3);
            
            // Xác định correctOption
            if (q.optionA != null && q.optionA.equals(correctWord.definition)) q.correctOption = "A";
            else if (q.optionB != null && q.optionB.equals(correctWord.definition)) q.correctOption = "B";
            else if (q.optionC != null && q.optionC.equals(correctWord.definition)) q.correctOption = "C";
            else if (q.optionD != null && q.optionD.equals(correctWord.definition)) q.correctOption = "D";

            q.explanation = "Từ \"" + correctWord.term + "\" có nghĩa là: " + correctWord.definition;
            questions.add(q);
        }
    }

    private void startTest() {
        timeLeftInMillis = currentTest.durationMinutes * 60 * 1000L;
        startTimer();
        showQuestion();
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                finishTest();
            }
        }.start();
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        binding.tvTimer.setText(timeLeftFormatted);
    }

    private void showQuestion() {
        binding.rgOptions.clearCheck();
        binding.etAnswer.setText("");

        Question q = questions.get(currentQuestionIndex);
        binding.tvQuestionCount.setText("Câu " + (currentQuestionIndex + 1) + "/" + questions.size());
        binding.tvQuestionText.setText(q.questionText);

        binding.rgOptions.setVisibility(View.VISIBLE);
        binding.tilAnswer.setVisibility(View.GONE);
        
        binding.rbOptionA.setVisibility(q.optionA != null ? View.VISIBLE : View.GONE);
        binding.rbOptionA.setText(q.optionA);
        
        binding.rbOptionB.setVisibility(q.optionB != null ? View.VISIBLE : View.GONE);
        binding.rbOptionB.setText(q.optionB);
        
        binding.rbOptionC.setVisibility(q.optionC != null ? View.VISIBLE : View.GONE);
        binding.rbOptionC.setText(q.optionC);
        
        binding.rbOptionD.setVisibility(q.optionD != null ? View.VISIBLE : View.GONE);
        binding.rbOptionD.setText(q.optionD);

        binding.pbProgress.setProgress((currentQuestionIndex + 1) * 100 / questions.size());
    }

    private void checkAnswerAndNext() {
        Question q = questions.get(currentQuestionIndex);
        boolean isCorrect = false;

        int selectedId = binding.rgOptions.getCheckedRadioButtonId();
        if (selectedId != -1) {
            String selectedText = "";
            if (selectedId == binding.rbOptionA.getId()) selectedText = "A";
            else if (selectedId == binding.rbOptionB.getId()) selectedText = "B";
            else if (selectedId == binding.rbOptionC.getId()) selectedText = "C";
            else if (selectedId == binding.rbOptionD.getId()) selectedText = "D";
            
            if (selectedText.equals(q.correctOption)) {
                isCorrect = true;
            }
        }

        if (isCorrect) correctAnswers++;

        currentQuestionIndex++;
        if (currentQuestionIndex < questions.size()) {
            showQuestion();
        } else {
            finishTest();
        }
    }

    private void finishTest() {
        if (countDownTimer != null) countDownTimer.cancel();
        Intent intent = new Intent(this, TestResultActivity.class);
        intent.putExtra("correct", correctAnswers);
        intent.putExtra("total", questions.size());
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
