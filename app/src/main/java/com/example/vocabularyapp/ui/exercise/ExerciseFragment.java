package com.example.vocabularyapp.ui.exercise;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
    private double totalAccuracySum = 0; // Cộng dồn điểm để tính trung bình
    private final String THEME_COLOR = "#0D8ADB";
    private MediaPlayer mediaPlayer;
    private TextToSpeech tts;
    private boolean isTtsReady = false;

    // Speech Recognition
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private boolean isListening = false;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

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
        
        binding.btnMic.setOnClickListener(v -> {
            if (isListening) {
                stopListening();
            } else {
                startListening();
            }
        });

        setupOptionClickListeners();
        initTts();
        initSpeechRecognizer();
    }

    @Override
    protected void initData() {
        viewModel.getAllTests().observe(getViewLifecycleOwner(), tests -> {
            if (tests != null && !tests.isEmpty()) {
                testAdapter.setTests(tests);
                binding.rvTests.setVisibility(View.VISIBLE);
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

    private void initSpeechRecognizer() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }

        if (SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext());
            speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, true);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, requireContext().getPackageName());

            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    binding.tvSpeakStatus.setText("Listening...");
                    binding.btnMic.setColorFilter(Color.RED);
                }

                @Override
                public void onBeginningOfSpeech() {}

                @Override
                public void onRmsChanged(float rmsdB) {}

                @Override
                public void onBufferReceived(byte[] buffer) {}

                @Override
                public void onEndOfSpeech() {
                    isListening = false;
                    binding.btnMic.setColorFilter(Color.WHITE);
                    binding.tvSpeakStatus.setText("Processing...");
                }

                @Override
                public void onError(int error) {
                    isListening = false;
                    binding.btnMic.setColorFilter(Color.WHITE);
                    String message;
                    switch (error) {
                        case SpeechRecognizer.ERROR_NO_MATCH: message = "I didn't hear anything. Try again."; break;
                        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: message = "No speech detected. Try again."; break;
                        case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: 
                            message = "Busy, restarting..."; 
                            initSpeechRecognizer();
                            break;
                        default: message = "Speech error. Try again."; break;
                    }
                    binding.tvSpeakStatus.setText(message);
                    binding.btnMic.setEnabled(true);
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    float[] scores = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
                    
                    if (matches != null && !matches.isEmpty()) {
                        float confidence = (scores != null && scores.length > 0) ? scores[0] : 1.0f;
                        checkSpeakingAnswer(matches.get(0), confidence);
                    } else {
                        binding.tvSpeakStatus.setText("Could not recognize. Try again.");
                        binding.btnMic.setEnabled(true);
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        binding.tvRecognizedText.setText(matches.get(0));
                    }
                }

                @Override
                public void onEvent(int eventType, Bundle params) {}
            });
        }
    }

    private void startListening() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            isListening = true;
            binding.tvRecognizedText.setText("");
            binding.tvSpeakingScore.setVisibility(View.GONE);
            try {
                speechRecognizer.startListening(speechRecognizerIntent);
            } catch (Exception e) {
                initSpeechRecognizer();
                speechRecognizer.startListening(speechRecognizerIntent);
            }
        }
    }

    private void stopListening() {
        isListening = false;
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
        binding.btnMic.setColorFilter(Color.WHITE);
    }

    private void checkSpeakingAnswer(String recognizedText, float confidence) {
        Question q = currentQuestions.get(currentQuestionIndex);
        String targetText = q.correctOption.toLowerCase().replaceAll("[^a-z0-9 ]", "").trim();
        String inputBox = recognizedText.toLowerCase().replaceAll("[^a-z0-9 ]", "").trim();
        
        double similarityScore = calculateSimilarity(targetText, inputBox);
        
        double finalScore = similarityScore * (confidence > 0 ? (0.4 + 0.6 * confidence) : 1.0);
        double lengthRatio = (double) Math.min(targetText.length(), inputBox.length()) / Math.max(targetText.length(), inputBox.length());
        finalScore *= (0.5 + 0.5 * lengthRatio);

        int percentage = (int) (finalScore * 100);
        totalAccuracySum += percentage; // Lưu điểm vào tổng số

        binding.tvRecognizedText.setText(getHighlightedText(targetText, recognizedText));
        
        binding.tvSpeakingScore.setVisibility(View.VISIBLE);
        binding.tvSpeakingScore.setText(percentage + "% Accuracy");
        
        if (percentage >= 90) {
            binding.tvSpeakingScore.setTextColor(Color.parseColor("#4CAF50"));
            correctCount++;
            handleAnswer(true, q);
        } else if (percentage >= 65) {
            binding.tvSpeakingScore.setTextColor(Color.parseColor("#FF9800"));
            handleAnswer(false, q);
        } else {
            binding.tvSpeakingScore.setTextColor(Color.parseColor("#F44336"));
            handleAnswer(false, q);
        }
        
        binding.btnMic.setEnabled(false);
        binding.tvSpeakStatus.setText("Done!");
    }

    private SpannableString getHighlightedText(String target, String input) {
        SpannableString spannable = new SpannableString(input);
        String targetClean = target.toLowerCase();
        String inputClean = input.toLowerCase();

        String[] targetWords = targetClean.split("\\s+");
        String[] inputWords = inputClean.split("\\s+");

        if (targetWords.length <= 1) {
            for (int i = 0; i < input.length(); i++) {
                if (i >= target.length() || inputClean.charAt(i) != targetClean.charAt(i)) {
                    spannable.setSpan(new ForegroundColorSpan(Color.RED), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        } else {
            int currentPos = 0;
            for (int i = 0; i < inputWords.length; i++) {
                int start = inputClean.indexOf(inputWords[i], currentPos);
                if (start == -1) continue;
                int end = start + inputWords[i].length();
                
                boolean found = false;
                for (String tw : targetWords) {
                    if (tw.equals(inputWords[i])) {
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    spannable.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                currentPos = end;
            }
        }
        return spannable;
    }

    private double calculateSimilarity(String target, String input) {
        if (target.isEmpty() || input.isEmpty()) return 0.0;
        
        int distance = levenshteinDistance(target, input);
        double charScore = 1.0 - ((double) distance / Math.max(target.length(), input.length()));

        String[] targetWords = target.split("\\s+");
        String[] inputWords = input.split("\\s+");
        
        if (targetWords.length <= 1) {
            return charScore;
        }

        int matches = 0;
        for (String tw : targetWords) {
            for (String iw : inputWords) {
                if (tw.equals(iw)) {
                    matches++;
                    break;
                }
            }
        }
        double wordScore = (double) matches / targetWords.length;
        
        return (wordScore * 0.7) + (charScore * 0.3);
    }

    private int levenshteinDistance(String s1, String s2) {
        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0) costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
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
                totalAccuracySum = 0; // Reset điểm tổng
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
        
        binding.tvQuestionText.setText(q.questionText);
        binding.tvQuestionText.setTextColor(Color.parseColor(THEME_COLOR));
        binding.progressBar.setProgress((int) (((float) (currentQuestionIndex + 1) / currentQuestions.size()) * 100));
        
        binding.btnNextQuestion.setVisibility(View.GONE);
        binding.layoutFeedback.setVisibility(View.GONE);
        binding.btnShowExplanation.setVisibility(View.GONE);
        binding.tvExplanation.setVisibility(View.GONE);
        
        binding.layoutAudioPlayer.setVisibility((q.type != null && q.type.startsWith("LISTENING")) ? View.VISIBLE : View.GONE);
        
        binding.layoutMultipleChoice.setVisibility(View.GONE);
        binding.layoutFillBlank.setVisibility(View.GONE);
        binding.layoutSpeaking.setVisibility(View.GONE);

        if ("MULTIPLE_CHOICE".equals(q.type) || "LISTENING_MC".equals(q.type)) {
            binding.layoutMultipleChoice.setVisibility(View.VISIBLE);
            binding.btnOptionA.setText(q.optionA);
            binding.btnOptionB.setText(q.optionB);
            binding.btnOptionC.setText(q.optionC);
            binding.btnOptionD.setText(q.optionD);
            enableOptions(true);
            resetOptionButtons();
        } else if ("FILL_BLANK".equals(q.type) || "LISTENING_DICTATION".equals(q.type)) {
            binding.layoutFillBlank.setVisibility(View.VISIBLE);
            binding.etFillAnswer.setText("");
            binding.etFillAnswer.setEnabled(true);
            binding.btnCheckFill.setEnabled(true);
            binding.btnCheckFill.setBackgroundColor(Color.parseColor(THEME_COLOR));
        } else if ("SPEAKING_WORD".equals(q.type) || "SPEAKING_SENTENCE".equals(q.type)) {
            binding.layoutSpeaking.setVisibility(View.VISIBLE);
            binding.tvPronunciation.setText(q.explanation != null ? q.explanation.split("\\|")[0].trim() : "");
            binding.tvSpeakStatus.setText("Tap the mic to start speaking");
            binding.tvRecognizedText.setText("");
            binding.tvSpeakingScore.setVisibility(View.GONE);
            binding.btnMic.setEnabled(true);
            binding.btnMic.setColorFilter(Color.WHITE);
        }
    }

    private void resetOptionButtons() {
        int themeColorInt = Color.parseColor(THEME_COLOR);
        setButtonStyle(binding.btnOptionA, themeColorInt, Color.WHITE);
        setButtonStyle(binding.btnOptionB, themeColorInt, Color.WHITE);
        setButtonStyle(binding.btnOptionC, themeColorInt, Color.WHITE);
        setButtonStyle(binding.btnOptionD, themeColorInt, Color.WHITE);
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
            binding.tvFeedback.setText("Nice try!");
            binding.tvFeedback.setTextColor(Color.parseColor("#FF9800"));
        }

        StringBuilder detailBuilder = new StringBuilder();
        // Hiển thị câu ngữ cảnh cho bài NGHE (Listening)
        if (q.type != null && q.type.startsWith("LISTENING")) {
            String context = (q.audioPath != null && !q.audioPath.isEmpty()) ? q.audioPath : "";
            if (!context.isEmpty()) {
                detailBuilder.append("Transcript:\n\"").append(context).append("\"\n\n");
            }
        }

        if (q.type != null && q.type.startsWith("SPEAKING")) {
            String translation = q.explanation != null && q.explanation.contains("|") ? q.explanation.split("\\|")[1].trim() : "";
            detailBuilder.append("Meaning: ").append(translation).append("\n\n");
        }

        if (q.explanation != null && !q.explanation.isEmpty() && !q.type.startsWith("SPEAKING")) {
            detailBuilder.append("Explanation:\n").append(q.explanation);
        }

        if (detailBuilder.length() > 0) {
            binding.tvExplanation.setText(detailBuilder.toString());
            binding.tvExplanation.setVisibility(View.VISIBLE);
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
        } else if (q.type != null && q.type.startsWith("SPEAKING")) {
            textToSpeak = q.correctOption;
        }

        if (textToSpeak.isEmpty()) return;

        binding.btnPlayAudio.setEnabled(false);
        if (isTtsReady) {
            tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "exercise_tts");
            binding.getRoot().postDelayed(() -> { if (binding != null) binding.btnPlayAudio.setEnabled(true); }, 2000);
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

        Question firstQ = currentQuestions.isEmpty() ? null : currentQuestions.get(0);
        boolean isSpeakingTest = firstQ != null && firstQ.type != null && firstQ.type.startsWith("SPEAKING");

        if (isSpeakingTest) {
            int averageAccuracy = (int) (totalAccuracySum / currentQuestions.size());
            binding.tvScore.setText(averageAccuracy + "% Native-like");
            
            String feedback = "Needs more effort!";
            if (averageAccuracy >= 90) feedback = "Excellent! Sounding like a local! 🏆";
            else if (averageAccuracy >= 75) feedback = "Well done! Pretty good pronunciation 🌟";
            else if (averageAccuracy >= 50) feedback = "Not bad! Needs a bit more practice! 👍";
            binding.tvResultFeedback.setText(feedback);
        } else {
            binding.tvScore.setText(correctCount + "/" + currentQuestions.size());
            String feedback = "Keep practicing!";
            if (correctCount == currentQuestions.size()) feedback = "Perfect! Excellent work! 🏆";
            else if (correctCount >= 8) feedback = "Great job! Almost perfect! 🌟";
            else if (correctCount >= 5) feedback = "Good effort! Keep it up! 👍";
            binding.tvResultFeedback.setText(feedback);
        }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening();
            } else {
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        stopAudio();
        if (tts != null) { tts.stop(); tts.shutdown(); }
        if (speechRecognizer != null) { speechRecognizer.destroy(); }
        super.onDestroyView();
    }
}
