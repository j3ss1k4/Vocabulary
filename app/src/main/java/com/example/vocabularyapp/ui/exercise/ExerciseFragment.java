package com.example.vocabularyapp.ui.exercise;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.util.Log;
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
import com.example.vocabularyapp.databinding.FragmentExerciseBinding;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ExerciseFragment extends BaseFragment<FragmentExerciseBinding> {

    private ExerciseViewModel viewModel;
    private TestAdapter testAdapter;
    private List<Question> currentQuestions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int correctCount = 0;
    private final String THEME_COLOR = "#0D8ADB";
    private MediaPlayer mediaPlayer;
    private TextToSpeech tts;
    private boolean isTtsReady = false;

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
            stopAudio();
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

        binding.btnPlayAudio.setOnClickListener(v -> playAudio());
        setupOptionClickListeners();
        initTts();
    }

    // Sửa trong ExerciseFragment.java tại method initData()
    @Override
    protected void initData() {
        viewModel.getAllTests().observe(getViewLifecycleOwner(), tests -> {
            if (tests != null && !tests.isEmpty()) {
                Log.d("DEBUG_EXERCISE", "Tests received: " + tests.size());
                testAdapter.setTests(tests);
                binding.rvTests.setVisibility(View.VISIBLE);
            } else {
                Log.d("DEBUG_EXERCISE", "No tests found in DB");
            }
        });
    }

    private void initTts() {
        tts = new TextToSpeech(requireActivity(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                tts.setSpeechRate(0.85f);
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    isTtsReady = true;
                }
            }
        });
    }

    private void loadQuestions(int testId) {
        viewModel.getQuestionsForTest(testId).observe(getViewLifecycleOwner(), questions -> {
            if (questions != null && !questions.isEmpty()) {
                List<Question> allQuestions = new ArrayList<>(questions);
                Collections.shuffle(allQuestions);
                int limit = Math.min(10, allQuestions.size());
                currentQuestions = new ArrayList<>(allQuestions.subList(0, limit));
                currentQuestionIndex = 0;
                correctCount = 0;
                startTest();
            } else {
                Toast.makeText(requireContext(), "No questions found for this test", Toast.LENGTH_SHORT).show();
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
        
        binding.tvQuestionText.setText("Question " + (currentQuestionIndex + 1) + ": " + q.questionText);
        binding.tvQuestionText.setTextColor(Color.parseColor(THEME_COLOR));
        binding.progressBar.setProgress((int) (((float) (currentQuestionIndex + 1) / currentQuestions.size()) * 100));
        
        binding.btnNextQuestion.setVisibility(View.GONE);
        binding.btnNextQuestion.setText("Next");
        
        binding.layoutFeedback.setVisibility(View.GONE);
        binding.btnShowExplanation.setVisibility(View.GONE);
        binding.tvExplanation.setVisibility(View.GONE);
        enableOptions(true);

        binding.layoutAudioPlayer.setVisibility((q.type != null && q.type.startsWith("LISTENING")) ? View.VISIBLE : View.GONE);

        if ("MULTIPLE_CHOICE".equals(q.type) || "LISTENING_MC".equals(q.type)) {
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
            binding.btnCheckFill.setText("Check");
            binding.btnCheckFill.setBackgroundColor(Color.parseColor(THEME_COLOR));
            binding.btnCheckFill.setTextColor(Color.WHITE);
        }
    }

    private void checkMultipleChoice(String selected) {
        Question q = currentQuestions.get(currentQuestionIndex);
        boolean isCorrect = selected.equals(q.correctOption);
        
        Button correctBtn = getOptionButton(q.correctOption);
        Button selectedBtn = getOptionButton(selected);

        if (isCorrect) {
            correctCount++;
            if (selectedBtn != null) selectedBtn.setBackgroundColor(Color.parseColor("#4CAF50"));
        } else {
            if (selectedBtn != null) selectedBtn.setBackgroundColor(Color.parseColor("#F44336"));
            if (correctBtn != null) correctBtn.setBackgroundColor(Color.parseColor("#4CAF50"));
        }
        
        handleAnswer(isCorrect, q);
        enableOptions(false);
    }

    private Button getOptionButton(String option) {
        if ("A".equals(option)) return binding.btnOptionA;
        if ("B".equals(option)) return binding.btnOptionB;
        if ("C".equals(option)) return binding.btnOptionC;
        if ("D".equals(option)) return binding.btnOptionD;
        return null;
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
        
        handleAnswer(isCorrect, q);
        binding.etFillAnswer.setEnabled(false);
        binding.btnCheckFill.setEnabled(false);
    }

    private void handleAnswer(boolean isCorrect, Question q) {
        binding.layoutFeedback.setVisibility(View.VISIBLE);
        binding.btnNextQuestion.setVisibility(View.VISIBLE);
        binding.btnNextQuestion.setBackgroundColor(Color.parseColor(THEME_COLOR));
        binding.btnNextQuestion.setTextColor(Color.WHITE);
        
        if (isCorrect) {
            binding.tvFeedback.setText("Correct! 🎉");
            binding.tvFeedback.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            binding.tvFeedback.setText("Incorrect! 😕");
            binding.tvFeedback.setTextColor(Color.parseColor("#F44336"));
        }

        StringBuilder detailBuilder = new StringBuilder();
        
        if (q.type != null && q.type.startsWith("LISTENING") && q.audioPath != null && !q.audioPath.isEmpty()) {
            detailBuilder.append("🎧 Context: ").append(q.audioPath).append("\n\n");
        }

        if (!isCorrect) {
            String correctText = "";
            if ("MULTIPLE_CHOICE".equals(q.type) || "LISTENING_MC".equals(q.type)) {
                if ("A".equals(q.correctOption)) correctText = q.optionA;
                else if ("B".equals(q.correctOption)) correctText = q.optionB;
                else if ("C".equals(q.correctOption)) correctText = q.optionC;
                else if ("D".equals(q.correctOption)) correctText = q.optionD;
            } else {
                correctText = q.correctOption;
            }
            detailBuilder.append("👉 Correct answer: ").append(correctText).append("\n\n");
        }

        if (q.explanation != null && !q.explanation.isEmpty()) {
            detailBuilder.append("Giải thích:\n").append(q.explanation);
        }

        if (detailBuilder.length() > 0) {
            binding.tvExplanation.setText(detailBuilder.toString());
            binding.tvExplanation.setVisibility(View.VISIBLE);
            binding.btnShowExplanation.setVisibility(View.GONE);
        } else {
            binding.tvExplanation.setVisibility(View.GONE);
            binding.btnShowExplanation.setVisibility(View.GONE);
        }
    }

    private void playAudio() {
        if (currentQuestions.isEmpty()) return;
        Question q = currentQuestions.get(currentQuestionIndex);
        String textToSpeak = "";

        if ("LISTENING_MC".equals(q.type)) {
            textToSpeak = (q.audioPath != null && !q.audioPath.isEmpty()) ? q.audioPath : q.questionText;
        } else if ("LISTENING_DICTATION".equals(q.type)) {
            textToSpeak = (q.audioPath != null && !q.audioPath.isEmpty()) ? q.audioPath : q.questionText.replace("_______", q.correctOption);
        }

        if (textToSpeak.isEmpty()) return;

        AudioManager am = (AudioManager) requireContext().getSystemService(Context.AUDIO_SERVICE);
        if (am.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
            Toast.makeText(requireContext(), "Please turn up the volume!", Toast.LENGTH_SHORT).show();
        }

        binding.btnPlayAudio.setEnabled(false);
        if (isTtsReady) {
            tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "exercise_tts");
            binding.getRoot().postDelayed(() -> { if (binding != null) binding.btnPlayAudio.setEnabled(true); }, 3000);
        } else {
            playOnlineAudio(textToSpeak);
        }
    }

    private void playOnlineAudio(String text) {
        try {
            String url = "https://translate.google.com/translate_tts?ie=UTF-8&tl=en&client=tw-ob&q=" + URLEncoder.encode(text, "UTF-8");
            if (mediaPlayer != null) mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA).build());
            mediaPlayer.setDataSource(requireContext(), Uri.parse(url));
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
            mediaPlayer.setOnCompletionListener(mp -> { if (binding != null) binding.btnPlayAudio.setEnabled(true); });
        } catch (Exception e) {
            binding.btnPlayAudio.setEnabled(true);
        }
    }

    private void showResult() {
        binding.layoutQuestionContainer.setVisibility(View.GONE);
        binding.layoutResult.setVisibility(View.VISIBLE);
        binding.tvScore.setText(correctCount + "/" + currentQuestions.size());
        
        String feedback = "Keep practicing!";
        if (correctCount == currentQuestions.size()) feedback = "Perfect! Excellent work! 🏆";
        else if (correctCount >= 8) feedback = "Great job! Almost perfect! 🌟";
        else if (correctCount >= 5) feedback = "Good effort! Keep it up! 👍";
        
        binding.tvResultFeedback.setText(feedback);
    }

    private void setButtonStyle(Button btn, int bgColor, int textColor) {
        btn.setBackgroundColor(bgColor);
        btn.setTextColor(textColor);
    }

    private void stopAudio() {
        if (mediaPlayer != null) { try { mediaPlayer.stop(); } catch (Exception e) {} mediaPlayer.release(); mediaPlayer = null; }
        if (tts != null && tts.isSpeaking()) tts.stop();
        if (binding != null) binding.btnPlayAudio.setEnabled(true);
    }

    private void enableOptions(boolean enabled) {
        binding.btnOptionA.setEnabled(enabled);
        binding.btnOptionB.setEnabled(enabled);
        binding.btnOptionC.setEnabled(enabled);
        binding.btnOptionD.setEnabled(enabled);
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
    public void onDestroyView() {
        stopAudio();
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroyView();
    }
}
