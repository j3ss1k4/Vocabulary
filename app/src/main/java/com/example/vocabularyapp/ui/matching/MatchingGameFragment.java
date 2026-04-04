package com.example.vocabularyapp.ui.matching;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.vocabularyapp.base.BaseFragment;
import com.example.vocabularyapp.data.local.entity.Word;
import com.example.vocabularyapp.databinding.FragmentMatchingGameBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MatchingGameFragment extends BaseFragment<FragmentMatchingGameBinding> {

    private MatchingGameViewModel viewModel;
    private MatchingGameAdapter adapter;
    private List<MatchingCard> cards = new ArrayList<>();
    private MatchingCard firstSelectedCard = null;
    private int firstSelectedPosition = -1;
    private int matchedPairs = 0;
    private int totalPairs = 0;

    private Timer timer;
    private int secondsElapsed = 0;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected FragmentMatchingGameBinding inflateViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentMatchingGameBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        viewModel = new ViewModelProvider(this).get(MatchingGameViewModel.class);
        adapter = new MatchingGameAdapter();
        binding.rvCards.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvCards.setAdapter(adapter);

        adapter.setOnCardClickListener(this::handleCardClick);

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.btnPlayAgain.setOnClickListener(v -> startGame());
    }

    @Override
    protected void initData() {
        startGame();
    }

    private void startGame() {
        viewModel.getAllWords().observe(getViewLifecycleOwner(), words -> {
            if (words != null && words.size() >= 4) {
                setupGame(words);
            } else if (words != null) {
                Toast.makeText(requireContext(), "Cần ít nhất 4 từ vựng để chơi", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(binding.getRoot()).navigateUp();
            }
        });
    }

    private void setupGame(List<Word> allWords) {
        stopTimer();
        secondsElapsed = 0;
        binding.tvTimer.setText("00:00");
        binding.layoutGameComplete.setVisibility(View.GONE);
        binding.rvCards.setVisibility(View.VISIBLE);

        List<Word> gameWords = new ArrayList<>(allWords);
        Collections.shuffle(gameWords);
        int numWords = Math.min(8, gameWords.size()); // Chơi với tối đa 8 cặp
        gameWords = gameWords.subList(0, numWords);

        cards.clear();
        for (Word word : gameWords) {
            cards.add(new MatchingCard(word.id, word.term, true));
            cards.add(new MatchingCard(word.id, word.definition, false));
        }

        Collections.shuffle(cards);
        totalPairs = numWords;
        matchedPairs = 0;
        firstSelectedCard = null;
        firstSelectedPosition = -1;

        adapter.setCards(cards);
        startTimer();
    }

    private void handleCardClick(MatchingCard card, int position) {
        if (card.isSelected() || card.isMatched()) return;

        card.setSelected(true);
        adapter.notifyItemChanged(position);

        if (firstSelectedCard == null) {
            firstSelectedCard = card;
            firstSelectedPosition = position;
        } else {
            if (firstSelectedCard.getWordId() == card.getWordId() && firstSelectedCard.isTerm() != card.isTerm()) {
                // Match found
                firstSelectedCard.setMatched(true);
                card.setMatched(true);
                matchedPairs++;

                int pos1 = firstSelectedPosition;
                int pos2 = position;

                mainHandler.postDelayed(() -> {
                    adapter.notifyItemChanged(pos1);
                    adapter.notifyItemChanged(pos2);
                    checkGameComplete();
                }, 300);

                firstSelectedCard = null;
                firstSelectedPosition = -1;
            } else {
                // No match
                int pos1 = firstSelectedPosition;
                int pos2 = position;
                MatchingCard card1 = firstSelectedCard;
                MatchingCard card2 = card;

                mainHandler.postDelayed(() -> {
                    card1.setSelected(false);
                    card2.setSelected(false);
                    adapter.notifyItemChanged(pos1);
                    adapter.notifyItemChanged(pos2);
                }, 500);

                firstSelectedCard = null;
                firstSelectedPosition = -1;
            }
        }
    }

    private void checkGameComplete() {
        if (matchedPairs == totalPairs) {
            stopTimer();
            binding.rvCards.setVisibility(View.GONE);
            binding.layoutGameComplete.setVisibility(View.VISIBLE);
            binding.tvFinalTime.setText(String.format(Locale.getDefault(), "Thời gian: %s", binding.tvTimer.getText()));
        }
    }

    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                secondsElapsed++;
                mainHandler.post(() -> {
                    int minutes = secondsElapsed / 60;
                    int seconds = secondsElapsed % 60;
                    binding.tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
                });
            }
        }, 1000, 1000);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onDestroyView() {
        stopTimer();
        super.onDestroyView();
    }
}
