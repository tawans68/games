package com.example.games;

import android.content.Context;
import android.content.SharedPreferences;

public class ScoreManager {
    private static final String PREFS_NAME = "GamePrefs";
    private static final String KEY_LATEST_SCORE = "latest_score";
    private static final String KEY_HIGH_SCORE = "high_score";
    private final SharedPreferences prefs;

    public ScoreManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getLatestScore() {
        return prefs.getInt(KEY_LATEST_SCORE, 0);
    }

    public int getHighScore() {
        return prefs.getInt(KEY_HIGH_SCORE, 0);
    }

    public boolean saveScore(int score, boolean isClassicMode) {
        SharedPreferences.Editor editor = prefs.edit();
        boolean isNewHighScore = false;

        if (isClassicMode) {
            editor.putInt(KEY_LATEST_SCORE, score);
            if (score > getHighScore()) {
                editor.putInt(KEY_HIGH_SCORE, score);
                isNewHighScore = true;
            }
        }
        editor.apply();
        return isNewHighScore;
    }
}
