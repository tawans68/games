package com.example.games;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.games.GameEngine.GameMode;

public class UIManager {
    private final Activity activity;

    public TextView tvLetters, tvResult, tvTimer, tvScore, tvLatestScore, tvHighScore, tvLoadStatus, tvDescTitle, tvDescBody;
    public EditText etInput;
    public Button btnCheck, btnBackToHome, btnRestart, btnClose, btnConfirmStart, btnCancelDesc, btnLanguage, btnComputerTerms, btnPractice, btnBackFromCategory;
    public LinearLayout homeContainer, descContainer, categoryContainer, categoryList;
    public ScrollView gameContainer;

    public UIManager(Activity activity) {
        this.activity = activity;
        initViews();
    }

    private void initViews() {
        homeContainer = activity.findViewById(R.id.homeContainer);
        descContainer = activity.findViewById(R.id.descContainer);
        gameContainer = activity.findViewById(R.id.gameContainer);
        categoryContainer = activity.findViewById(R.id.categoryContainer);
        categoryList = activity.findViewById(R.id.categoryList);

        tvLetters = activity.findViewById(R.id.tvLetters);
        tvResult = activity.findViewById(R.id.tvResult);
        tvTimer = activity.findViewById(R.id.tvTimer);
        tvScore = activity.findViewById(R.id.tvScore);
        tvLatestScore = activity.findViewById(R.id.tvLatestScore);
        tvHighScore = activity.findViewById(R.id.tvHighScore);
        tvLoadStatus = activity.findViewById(R.id.tvLoadStatus);
        tvDescTitle = activity.findViewById(R.id.tvDescTitle);
        tvDescBody = activity.findViewById(R.id.tvDescBody);

        etInput = activity.findViewById(R.id.etInput);
        btnCheck = activity.findViewById(R.id.btnCheck);
        btnBackToHome = activity.findViewById(R.id.btnBackToHome);
        btnRestart = activity.findViewById(R.id.btnRestart);
        btnClose = activity.findViewById(R.id.btnClose);
        btnConfirmStart = activity.findViewById(R.id.btnConfirmStart);
        btnCancelDesc = activity.findViewById(R.id.btnCancelDesc);
        btnLanguage = activity.findViewById(R.id.btnLanguage);
        btnComputerTerms = activity.findViewById(R.id.btnComputerTerms);
        btnPractice = activity.findViewById(R.id.btnPractice);
        btnBackFromCategory = activity.findViewById(R.id.btnBackFromCategory);
    }

    public void showHomeScreen(int latestScore, int highScore) {
        homeContainer.setVisibility(View.VISIBLE);
        descContainer.setVisibility(View.GONE);
        gameContainer.setVisibility(View.GONE);
        categoryContainer.setVisibility(View.GONE);
        tvLatestScore.setText(activity.getString(R.string.latest_score, latestScore));
        tvHighScore.setText(activity.getString(R.string.high_score, highScore));
        hideKeyboard();
    }

    public void showCategorySelection() {
        homeContainer.setVisibility(View.GONE);
        categoryContainer.setVisibility(View.VISIBLE);
        descContainer.setVisibility(View.GONE);
        gameContainer.setVisibility(View.GONE);
    }

    public void showDescription(GameMode mode, String categoryName) {
        homeContainer.setVisibility(View.GONE);
        descContainer.setVisibility(View.VISIBLE);
        gameContainer.setVisibility(View.GONE);
        categoryContainer.setVisibility(View.GONE);

        if (categoryName != null) {
            tvDescTitle.setText(categoryName);
            int bodyRes = (mode == GameMode.RELAX) ? R.string.desc_relax_body : R.string.desc_category_body;
            String body = activity.getString(bodyRes, categoryName);
            tvDescBody.setText(body);
        } else {
            // โหมดปกติ (ถ้ามี)
            switch (mode) {
                case CLASSIC:
                    tvDescTitle.setText(R.string.desc_classic_title);
                    tvDescBody.setText(R.string.desc_classic_body);
                    break;
                case RELAX:
                    tvDescTitle.setText(R.string.desc_relax_title);
                    tvDescBody.setText(R.string.desc_relax_body);
                    break;
                case SURVIVAL:
                    tvDescTitle.setText(R.string.desc_survival_title);
                    tvDescBody.setText(R.string.desc_survival_body);
                    break;
            }
        }
    }

    public void showGameScreen() {
        homeContainer.setVisibility(View.GONE);
        descContainer.setVisibility(View.GONE);
        categoryContainer.setVisibility(View.GONE);
        gameContainer.setVisibility(View.VISIBLE);
        btnBackToHome.setVisibility(View.GONE);
        etInput.requestFocus();
        showKeyboard();
    }

    public void updateScore(int score) {
        tvScore.setText(activity.getString(R.string.tv_score, score));
    }

    public void updateLetters(String letters) {
        tvLetters.setText(letters);
    }

    public void setResult(String message, int color) {
        tvResult.setText(message);
        tvResult.setTextColor(color);
    }

    public void updateTimer(String text) {
        tvTimer.setText(text);
    }

    public void setGameControlsEnabled(boolean enabled) {
        btnCheck.setEnabled(enabled);
        etInput.setEnabled(enabled);
    }

    public void showKeyboard() {
        etInput.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etInput, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etInput.getWindowToken(), 0);
        }
    }
    
    public void showToast(String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }
    
    public void showToast(int resId) {
        Toast.makeText(activity, resId, Toast.LENGTH_SHORT).show();
    }
}
