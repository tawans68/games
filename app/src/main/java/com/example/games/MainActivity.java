package com.example.games;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.inputmethod.EditorInfo;

// ใช้ GameMode จาก GameEngine
import com.example.games.GameEngine.GameMode;

public class MainActivity extends AppCompatActivity {

    private DictionaryManager dictionaryManager;
    private GameEngine gameEngine;
    private ScoreManager scoreManager;
    private VibrationManager vibrationManager;
    private LanguageManager languageManager;
    private UIManager uiManager;
    private CountDownTimer timer;

    private GameMode selectedMode = GameMode.CLASSIC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        languageManager = new LanguageManager(this);
        languageManager.updateResource(languageManager.getLanguage());
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scoreManager = new ScoreManager(this);
        vibrationManager = new VibrationManager(this);
        uiManager = new UIManager(this);
        
        initUI();
        initGame();
        uiManager.showHomeScreen(scoreManager.getLatestScore(), scoreManager.getHighScore());
    }

    private void initUI() {
        uiManager.btnCheck.setOnClickListener(v -> handleCheck());
        uiManager.btnStart.setOnClickListener(v -> showDescription(GameMode.CLASSIC));
        uiManager.btnRelax.setOnClickListener(v -> showDescription(GameMode.RELAX));
        uiManager.btnSurvival.setOnClickListener(v -> showDescription(GameMode.SURVIVAL));
        uiManager.btnBackToHome.setOnClickListener(v -> uiManager.showHomeScreen(scoreManager.getLatestScore(), scoreManager.getHighScore()));
        uiManager.btnClose.setOnClickListener(v -> quitGame());
        uiManager.btnConfirmStart.setOnClickListener(v -> showGameScreen());
        uiManager.btnCancelDesc.setOnClickListener(v -> uiManager.showHomeScreen(scoreManager.getLatestScore(), scoreManager.getHighScore()));
        
        uiManager.btnLanguage.setOnClickListener(v -> {
            languageManager.toggleLanguage();
            recreate();
        });

        uiManager.etInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                handleCheck();
                return true;
            }
            return false;
        });
    }

    private void initGame() {
        dictionaryManager = new DictionaryManager();
        gameEngine = new GameEngine();

        uiManager.btnStart.setEnabled(false);
        uiManager.btnRelax.setEnabled(false);
        uiManager.btnSurvival.setEnabled(false);
        uiManager.tvLoadStatus.setText(R.string.loading);

        dictionaryManager.loadAsync(this, new DictionaryManager.LoadCallback() {
            @Override
            public void onLoaded(int size) {
                uiManager.btnStart.setEnabled(true);
                uiManager.btnRelax.setEnabled(true);
                uiManager.btnSurvival.setEnabled(true);
                uiManager.tvLoadStatus.setText(getString(R.string.load_success, size));
            }

            @Override
            public void onError(String message) {
                uiManager.tvLoadStatus.setText(getString(R.string.load_error, message));
                uiManager.btnStart.setEnabled(true);
                uiManager.btnRelax.setEnabled(true);
                uiManager.btnSurvival.setEnabled(true);
            }
        });
    }

    private void showDescription(GameMode mode) {
        this.selectedMode = mode;
        uiManager.showDescription(mode);
    }

    private void showGameScreen() {
        uiManager.showGameScreen();
        startNewGame(selectedMode);
    }

    private void startNewGame(GameMode mode) {
        gameEngine.start(dictionaryManager, mode);
        uiManager.setGameControlsEnabled(true);
        uiManager.etInput.setText("");
        uiManager.tvResult.setText("");
        
        uiManager.updateScore(gameEngine.getScore());
        uiManager.updateLetters(gameEngine.getCurrentLetters());
        
        if (mode == GameMode.RELAX) {
            uiManager.updateTimer(getString(R.string.relax_mode_active));
        } else {
            startTimer();
        }
    }

    private void handleCheck() {
        String input = uiManager.etInput.getText().toString().trim();
        if (input.isEmpty()) return;

        int oldLives = gameEngine.getLives();
        GameEngine.WordResult result = gameEngine.validateWord(input, dictionaryManager);

        if (result == GameEngine.WordResult.CORRECT) {
            uiManager.setResult(getString(R.string.correct), 0xFF4CAF50);
            
            if (gameEngine.getCurrentMode() == GameMode.SURVIVAL && gameEngine.getLives() > oldLives) {
                uiManager.showToast(R.string.survival_heart_bonus);
            }
            
            uiManager.updateScore(gameEngine.getScore());
            gameEngine.generateNextRound(dictionaryManager);
            uiManager.updateLetters(gameEngine.getCurrentLetters());
            
            if (gameEngine.getCurrentMode() != GameMode.RELAX) {
                startTimer();
            }
        } else {
            vibrationManager.vibrate(200);
            int redColor = 0xFFF44336;
            
            if (gameEngine.getCurrentMode() == GameMode.SURVIVAL) {
                if (gameEngine.getLives() <= 0) {
                    endGame(getString(R.string.survival_game_over));
                    return;
                }
                uiManager.setResult(getString(R.string.survival_wrong), redColor);
                gameEngine.generateNextRound(dictionaryManager);
                uiManager.updateLetters(gameEngine.getCurrentLetters());
                startTimer();
            } else {
                switch (result) {
                    case MISSING_LETTERS:
                        uiManager.setResult(getString(R.string.must_contain, gameEngine.getCurrentLetters()), redColor);
                        break;
                    case ALREADY_USED:
                        uiManager.setResult(getString(R.string.already_used), redColor);
                        break;
                    case NOT_IN_DICTIONARY:
                        uiManager.setResult(getString(R.string.no_dictionary), redColor);
                        break;
                }
            }
        }
        uiManager.etInput.setText("");
    }

    private void quitGame() {
        if (timer != null) timer.cancel();
        gameEngine.stop();
        saveAndNotifyScore();
        uiManager.showHomeScreen(scoreManager.getLatestScore(), scoreManager.getHighScore());
    }

    private void saveAndNotifyScore() {
        boolean isClassic = gameEngine.getCurrentMode() == GameMode.CLASSIC;
        boolean newHigh = scoreManager.saveScore(gameEngine.getScore(), isClassic);
        if (newHigh) {
            uiManager.showToast(R.string.new_high_score);
        }
    }

    private void startTimer() {
        if (timer != null) timer.cancel();
        timer = new CountDownTimer(15000, 100) {
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                if (gameEngine.getCurrentMode() == GameMode.SURVIVAL) {
                    StringBuilder hearts = new StringBuilder();
                    for (int i = 0; i < gameEngine.getLives(); i++) hearts.append("❤️");
                    uiManager.updateTimer(getString(R.string.survival_mode_active, hearts.toString(), seconds));
                } else {
                    uiManager.updateTimer(getString(R.string.tv_timer, seconds));
                }
            }

            public void onFinish() {
                if (gameEngine.getCurrentMode() == GameMode.SURVIVAL) {
                    gameEngine.decrementLife();
                    if (gameEngine.getLives() > 0) {
                        vibrationManager.vibrate(200);
                        uiManager.setResult(getString(R.string.survival_timeout), 0xFFF44336);
                        gameEngine.generateNextRound(dictionaryManager);
                        uiManager.updateLetters(gameEngine.getCurrentLetters());
                        startTimer();
                    } else {
                        endGame(getString(R.string.survival_game_over));
                    }
                } else {
                    endGame(getString(R.string.game_over));
                }
            }
        }.start();
    }

    private void endGame(String message) {
        vibrationManager.vibrate(500);
        if (timer != null) timer.cancel();
        gameEngine.stop();
        uiManager.setResult(message, 0xFFF44336);
        uiManager.setGameControlsEnabled(false);
        uiManager.btnBackToHome.setVisibility(View.VISIBLE);
        
        saveAndNotifyScore();
        uiManager.hideKeyboard();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }
}
