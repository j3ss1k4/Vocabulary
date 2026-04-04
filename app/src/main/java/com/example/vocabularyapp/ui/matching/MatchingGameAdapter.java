package com.example.vocabularyapp.ui.matching;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vocabularyapp.databinding.ItemMatchingCardBinding;

import java.util.ArrayList;
import java.util.List;

public class MatchingGameAdapter extends RecyclerView.Adapter<MatchingGameAdapter.ViewHolder> {

    private List<MatchingCard> cards = new ArrayList<>();
    private OnCardClickListener listener;

    public interface OnCardClickListener {
        void onCardClick(MatchingCard card, int position);
    }

    public void setOnCardClickListener(OnCardClickListener listener) {
        this.listener = listener;
    }

    public void setCards(List<MatchingCard> cards) {
        this.cards = cards;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMatchingCardBinding binding = ItemMatchingCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MatchingCard card = cards.get(position);
        holder.binding.tvContent.setText(card.getContent());

        if (card.isMatched()) {
            holder.binding.cardView.setVisibility(View.INVISIBLE);
        } else {
            holder.binding.cardView.setVisibility(View.VISIBLE);
            if (card.isSelected()) {
                holder.binding.cardView.setStrokeColor(Color.parseColor("#0D8ADB"));
                holder.binding.cardView.setCardBackgroundColor(Color.parseColor("#E3F2FD"));
            } else {
                holder.binding.cardView.setStrokeColor(Color.TRANSPARENT);
                holder.binding.cardView.setCardBackgroundColor(Color.WHITE);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && !card.isMatched()) {
                listener.onCardClick(card, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemMatchingCardBinding binding;

        public ViewHolder(ItemMatchingCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
