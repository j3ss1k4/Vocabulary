package com.example.vocabularyapp.ui.test;

import android.content.Intent;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.vocabularyapp.base.BaseActivity;
import com.example.vocabularyapp.data.local.AppDatabase;
import com.example.vocabularyapp.data.local.entity.Question;
import com.example.vocabularyapp.data.local.entity.Test;
import com.example.vocabularyapp.databinding.ActivityTestSessionBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
        AppDatabase.getInstance(this).questionDao().getQuestionsByTestId(currentTest.id).observe(this, questionList -> {
            if (questionList != null && !questionList.isEmpty()) {
                questions = new ArrayList<>(questionList);
                // Logic tổng hợp câu hỏi ngẫu nhiên từ kho
                Collections.shuffle(questions);
                if (questions.size() > currentTest.totalQuestions) {
                    questions = questions.subList(0, currentTest.totalQuestions);
                }
                startTest();
            } else {
                Toast.makeText(this, "Không có câu hỏi cho bài thi này", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
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

        if (q.type.equals("MULTIPLE_CHOICE") || q.type.equals("LISTENING_MC")) {
            binding.rgOptions.setVisibility(View.VISIBLE);
            binding.tilAnswer.setVisibility(View.GONE);
            binding.rbOptionA.setText(q.optionA);
            binding.rbOptionB.setText(q.optionB);
            binding.rbOptionC.setText(q.optionC);
            binding.rbOptionD.setText(q.optionD);
        } else {
            binding.rgOptions.setVisibility(View.GONE);
            binding.tilAnswer.setVisibility(View.VISIBLE);
        }

        binding.pbProgress.setProgress((currentQuestionIndex + 1) * 100 / questions.size());
    }

    private void checkAnswerAndNext() {
        Question q = questions.get(currentQuestionIndex);
        boolean isCorrect = false;

        if (q.type.equals("MULTIPLE_CHOICE") || q.type.equals("LISTENING_MC")) {
            int selectedId = binding.rgOptions.getCheckedRadioButtonId();
            if (selectedId != -1) {
                RadioButton selectedRb = findViewById(selectedId);
                String selectedText = "";
                if (selectedId == binding.rbOptionA.getId()) selectedText = "A";
                else if (selectedId == binding.rbOptionB.getId()) selectedText = "B";
                else if (selectedId == binding.rbOptionC.getId()) selectedText = "C";
                else if (selectedId == binding.rbOptionD.getId()) selectedText = "D";
                
                if (selectedText.equals(q.correctOption)) {
                    isCorrect = true;
                }
            }
        } else {
            String answer = binding.etAnswer.getText().toString().trim();
            if (answer.equalsIgnoreCase(q.correctOption)) {
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
