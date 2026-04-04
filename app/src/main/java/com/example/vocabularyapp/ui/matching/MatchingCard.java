package com.example.vocabularyapp.ui.matching;

public class MatchingCard {
    private int wordId;
    private String content;
    private boolean isTerm;
    private boolean isMatched = false;
    private boolean isSelected = false;

    public MatchingCard(int wordId, String content, boolean isTerm) {
        this.wordId = wordId;
        this.content = content;
        this.isTerm = isTerm;
    }

    public int getWordId() { return wordId; }
    public String getContent() { return content; }
    public boolean isTerm() { return isTerm; }
    public boolean isMatched() { return isMatched; }
    public void setMatched(boolean matched) { isMatched = matched; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}
