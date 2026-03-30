package com.example.vocabularyapp.ui.exercise;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.vocabularyapp.base.BaseFragment;
import com.example.vocabularyapp.data.local.entity.Question;
import com.example.vocabularyapp.data.local.entity.Test;
import com.example.vocabularyapp.databinding.FragmentExerciseBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExerciseFragment extends BaseFragment<FragmentExerciseBinding> {

    private ExerciseViewModel viewModel;
    private TestAdapter testAdapter;
    private List<Question> currentQuestions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int correctCount = 0;
    private final String THEME_COLOR = "#0D8ADB";

    @Override
    protected FragmentExerciseBinding inflateViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentExerciseBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        viewModel = new ViewModelProvider(this).get(ExerciseViewModel.class);

        testAdapter = new TestAdapter();
        binding.rvTests.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTests.setAdapter(testAdapter);

        testAdapter.setOnTestClickListener(test -> loadQuestions(test.id));

        binding.btnNextQuestion.setOnClickListener(v -> {
            currentQuestionIndex++;
            showNextQuestion();
        });

        binding.btnCheckFill.setOnClickListener(v -> checkFillAnswer());

        binding.btnShowExplanation.setOnClickListener(v -> {
            binding.tvExplanation.setVisibility(View.VISIBLE);
            binding.btnShowExplanation.setVisibility(View.GONE);
        });

        binding.btnFinish.setOnClickListener(v -> {
            binding.layoutResult.setVisibility(View.GONE);
            binding.rvTests.setVisibility(View.VISIBLE);
            binding.tvTitle.setVisibility(View.VISIBLE);
        });
        
        setupOptionClickListeners();
    }

    private void setupOptionClickListeners() {
        View.OnClickListener listener = v -> {
            String selected = "";
            if (v.getId() == binding.btnOptionA.getId()) selected = "A";
            else if (v.getId() == binding.btnOptionB.getId()) selected = "B";
            else if (v.getId() == binding.btnOptionC.getId()) selected = "C";
            else if (v.getId() == binding.btnOptionD.getId()) selected = "D";

            checkMultipleChoice(selected);
        };
        binding.btnOptionA.setOnClickListener(listener);
        binding.btnOptionB.setOnClickListener(listener);
        binding.btnOptionC.setOnClickListener(listener);
        binding.btnOptionD.setOnClickListener(listener);
    }

    @Override
    protected void initData() {
        viewModel.getAllTests().observe(getViewLifecycleOwner(), tests -> {
            if (tests != null) {
                testAdapter.setTests(tests);
            }
        });
    }

    private void loadQuestions(int testId) {
        viewModel.getQuestionsForTest(testId).observe(getViewLifecycleOwner(), questions -> {
            if (questions != null && !questions.isEmpty()) {
                List<Question> shuffled = new ArrayList<>(questions);
                Collections.shuffle(shuffled);
                currentQuestions = shuffled.subList(0, Math.min(10, shuffled.size()));
                
                currentQuestionIndex = 0;
                correctCount = 0;
                startTest();
            } else {
                Toast.makeText(requireContext(), "Không có câu hỏi cho bài tập này", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startTest() {
        binding.rvTests.setVisibility(View.GONE);
        binding.tvTitle.setVisibility(View.GONE);
        binding.layoutResult.setVisibility(View.GONE);
        binding.layoutQuestionContainer.setVisibility(View.VISIBLE);
        showNextQuestion();
    }

    private void showNextQuestion() {
        if (currentQuestionIndex >= currentQuestions.size()) {
            showResult();
            return;
        }

        Question q = currentQuestions.get(currentQuestionIndex);
        binding.tvQuestionText.setText("Câu " + (currentQuestionIndex + 1) + ": " + q.questionText);
        binding.tvQuestionText.setTextColor(Color.parseColor(THEME_COLOR));
        
        binding.progressBar.setProgress((int) (((float) (currentQuestionIndex + 1) / currentQuestions.size()) * 100));
        
        binding.btnNextQuestion.setVisibility(View.GONE);
        binding.layoutFeedback.setVisibility(View.GONE);
        binding.btnShowExplanation.setVisibility(View.GONE);
        binding.tvExplanation.setVisibility(View.GONE);
        enableOptions(true);

        if ("MULTIPLE_CHOICE".equals(q.type)) {
            binding.layoutMultipleChoice.setVisibility(View.VISIBLE);
            binding.layoutFillBlank.setVisibility(View.GONE);
            binding.btnOptionA.setText(q.optionA);
            binding.btnOptionB.setText(q.optionB);
            binding.btnOptionC.setText(q.optionC);
            binding.btnOptionD.setText(q.optionD);
            
            int themeColorInt = Color.parseColor(THEME_COLOR);
            setButtonStyle(binding.btnOptionA, themeColorInt, Color.WHITE);
            setButtonStyle(binding.btnOptionB, themeColorInt, Color.WHITE);
            setButtonStyle(binding.btnOptionC, themeColorInt, Color.WHITE);
            setButtonStyle(binding.btnOptionD, themeColorInt, Color.WHITE);
        } else {
            binding.layoutMultipleChoice.setVisibility(View.GONE);
            binding.layoutFillBlank.setVisibility(View.VISIBLE);
            binding.etFillAnswer.setText("");
            binding.etFillAnswer.setEnabled(true);
            binding.btnCheckFill.setEnabled(true);
            binding.btnCheckFill.setBackgroundColor(Color.parseColor(THEME_COLOR));
            binding.btnCheckFill.setTextColor(Color.WHITE);
        }
    }

    private void showResult() {
        binding.layoutQuestionContainer.setVisibility(View.GONE);
        binding.layoutResult.setVisibility(View.VISIBLE);
        
        binding.tvScore.setText("Đúng: " + correctCount + "/" + currentQuestions.size());
        
        if (correctCount >= 8) {
            binding.tvResultFeedback.setText("Tuyệt vời! Bạn nắm vững kiến thức rất tốt.");
            binding.tvResultFeedback.setTextColor(Color.parseColor("#4CAF50"));
        } else if (correctCount >= 5) {
            binding.tvResultFeedback.setText("Khá tốt! Hãy cố gắng thêm chút nữa nhé.");
            binding.tvResultFeedback.setTextColor(Color.parseColor("#FF9800"));
        } else {
            binding.tvResultFeedback.setText("Cố gắng lên! Bạn cần luyện tập nhiều hơn.");
            binding.tvResultFeedback.setTextColor(Color.RED);
        }
    }

    private void setButtonStyle(Button btn, int bgColor, int textColor) {
        btn.setBackgroundColor(bgColor);
        btn.setTextColor(textColor);
    }

    private void checkMultipleChoice(String selected) {
        Question q = currentQuestions.get(currentQuestionIndex);
        boolean isCorrect = selected.equals(q.correctOption);
        
        Button clickedButton = null;
        if (selected.equals("A")) clickedButton = binding.btnOptionA;
        else if (selected.equals("B")) clickedButton = binding.btnOptionB;
        else if (selected.equals("C")) clickedButton = binding.btnOptionC;
        else if (selected.equals("D")) clickedButton = binding.btnOptionD;

        if (isCorrect) {
            correctCount++;
            if (clickedButton != null) clickedButton.setBackgroundColor(Color.parseColor("#4CAF50"));
        } else {
            if (clickedButton != null) clickedButton.setBackgroundColor(Color.parseColor("#F44336"));
            // Highlight the correct answer
            if ("A".equals(q.correctOption)) binding.btnOptionA.setBackgroundColor(Color.parseColor("#4CAF50"));
            else if ("B".equals(q.correctOption)) binding.btnOptionB.setBackgroundColor(Color.parseColor("#4CAF50"));
            else if ("C".equals(q.correctOption)) binding.btnOptionC.setBackgroundColor(Color.parseColor("#4CAF50"));
            else if ("D".equals(q.correctOption)) binding.btnOptionD.setBackgroundColor(Color.parseColor("#4CAF50"));
        }
        
        handleAnswer(isCorrect, q.explanation);
        enableOptions(false); // Disable all options after selection
    }

    private void checkFillAnswer() {
        Question q = currentQuestions.get(currentQuestionIndex);
        String answer = binding.etFillAnswer.getText().toString().trim();
        if (answer.isEmpty()) return;
        
        boolean isCorrect = answer.equalsIgnoreCase(q.correctOption);
        if (isCorrect) {
            correctCount++;
            binding.btnCheckFill.setBackgroundColor(Color.parseColor("#4CAF50"));
        } else {
            binding.btnCheckFill.setBackgroundColor(Color.parseColor("#F44336"));
        }

        handleAnswer(isCorrect, q.explanation);
        
        binding.etFillAnswer.setEnabled(false);
        binding.btnCheckFill.setEnabled(false);
    }

    private void handleAnswer(boolean isCorrect, String explanation) {
        binding.layoutFeedback.setVisibility(View.VISIBLE);
        binding.btnNextQuestion.setVisibility(View.VISIBLE); // Always show next button
        binding.btnNextQuestion.setBackgroundColor(Color.parseColor(THEME_COLOR));
        binding.btnNextQuestion.setTextColor(Color.WHITE);
        
        if (isCorrect) {
            binding.tvFeedback.setText("Chính xác! 🎉");
            binding.tvFeedback.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            binding.tvFeedback.setText("Chưa chính xác! 😕");
            binding.tvFeedback.setTextColor(Color.parseColor("#F44336"));
        }

        if (explanation != null && !explanation.isEmpty()) {
            binding.tvExplanation.setText("Giải thích: " + explanation);
            binding.tvExplanation.setVisibility(View.VISIBLE);
        } else {
            binding.tvExplanation.setVisibility(View.GONE);
        }
        binding.btnShowExplanation.setVisibility(View.GONE);
    }

    private void enableOptions(boolean enabled) {
        binding.btnOptionA.setEnabled(enabled);
        binding.btnOptionB.setEnabled(enabled);
        binding.btnOptionC.setEnabled(enabled);
        binding.btnOptionD.setEnabled(enabled);
    }
}
