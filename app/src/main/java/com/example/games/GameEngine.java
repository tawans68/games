package com.example.games;

import java.util.HashSet;
import java.util.Set;

public class GameEngine {
    public enum GameMode { CLASSIC, RELAX, SURVIVAL }
    
    private String currentLetters;
    private int score = 0;
    private int lives = 3;
    private int correctStreak = 0;
    private final Set<String> usedWords = new HashSet<>();
    private boolean isGameRunning = false;
    private GameMode currentMode = GameMode.CLASSIC;

    public void start(DictionaryManager dictionaryManager, GameMode mode) {
        this.currentMode = mode;
        this.isGameRunning = true;
        this.score = 0;
        this.lives = 3;
        this.correctStreak = 0;
        this.usedWords.clear();
        generateNextRound(dictionaryManager);
    }

    public void generateNextRound(DictionaryManager dictionaryManager) {
        currentLetters = dictionaryManager.getRandomPrompt();
    }

    public enum WordResult {
        CORRECT,
        NOT_IN_DICTIONARY,
        ALREADY_USED,
        MISSING_LETTERS,
        GAME_NOT_RUNNING
    }

    public WordResult validateWord(String input, DictionaryManager dictionaryManager) {
        if (!isGameRunning) return WordResult.GAME_NOT_RUNNING;
        
        String word = input.trim().toLowerCase();
        
        if (!word.contains(currentLetters.toLowerCase())) {
            return WordResult.MISSING_LETTERS;
        }
        
        if (usedWords.contains(word)) {
            return WordResult.ALREADY_USED;
        }
        
        if (dictionaryManager.contains(word)) {
            usedWords.add(word);
            score += 10;
            
            if (currentMode == GameMode.SURVIVAL) {
                correctStreak++;
                if (correctStreak >= 10) {
                    lives++;
                    correctStreak = 0;
                }
            }
            
            return WordResult.CORRECT;
        } else {
            if (currentMode == GameMode.SURVIVAL) {
                lives--;
                correctStreak = 0;
            }
            return WordResult.NOT_IN_DICTIONARY;
        }
    }

    public void decrementLife() {
        lives--;
        correctStreak = 0;
    }

    public void stop() {
        isGameRunning = false;
    }

    public String getCurrentLetters() {
        return currentLetters;
    }

    public int getScore() {
        return score;
    }

    public int getLives() {
        return lives;
    }

    public int getCorrectStreak() {
        return correctStreak;
    }

    public boolean isGameRunning() {
        return isGameRunning;
    }
    
    public GameMode getCurrentMode() {
        return currentMode;
    }
}
