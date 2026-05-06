package com.example.games;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class DictionaryManager {
    private final Set<String> dictionary = new HashSet<>(200000); 
    private final java.util.List<String> wordList = new java.util.ArrayList<>(200000);
    private volatile boolean isLoaded = false;
    private final java.util.Random random = new java.util.Random();

    public interface LoadCallback {
        void onLoaded(int size);
        void onError(String message);
    }

    public void loadAsync(Context context, LoadCallback callback) {
        new Thread(() -> {
            try {
                java.io.InputStream is = context.getAssets().open("words.txt");
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8), 65536);
                
                dictionary.clear();
                wordList.clear();
                String line;
                while ((line = reader.readLine()) != null) {
                    String word = line.trim().toLowerCase();
                    if (word.length() > 1) {
                        dictionary.add(word);
                        wordList.add(word);
                    }
                }
                reader.close();
                isLoaded = true;
                
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (callback != null) callback.onLoaded(dictionary.size());
                });
                
            } catch (Exception e) {
                Log.e("DictionaryManager", "Error loading dictionary", e);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (callback != null) callback.onError("ไม่พบไฟล์คำศัพท์หรือโหลดไฟล์ล้มเหลว");
                });
            }
        }).start();
    }

    public String getRandomPrompt() {
        if (!isLoaded || wordList.isEmpty()) return "TH";
        
        
        String randomWord = "";
        int attempts = 0;
        while (randomWord.length() < 3 && attempts < 100) {
            randomWord = wordList.get(random.nextInt(wordList.size())).toUpperCase();
            attempts++;
        }
        
        if (randomWord.length() < 2) return "TH"; // Fallback

    
        int len = 2;
        int start = random.nextInt(randomWord.length() - len + 1);
        return randomWord.substring(start, start + len);
    }

    public boolean contains(String word) {
        return dictionary.contains(word.toLowerCase());
    }

    public boolean isReady() {
        return isLoaded;
    }

    public int getSize() {
        return dictionary.size();
    }
}
