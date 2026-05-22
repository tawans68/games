package com.example.games;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class DictionaryManager {
    private final Map<String, List<String>> categoryWords = new HashMap<>();
    private final Map<String, Set<String>> categorySets = new HashMap<>();
    private final List<String> categoryNames = new ArrayList<>();
    private String selectedCategory = null;
    
    private volatile boolean isLoaded = false;
    private final Random random = new Random();

    public interface LoadCallback {
        void onLoaded(int size);
        void onError(String message);
    }

    public void loadAsync(Context context, LoadCallback callback) {
        new Thread(() -> {
            try {
                java.io.InputStream is = context.getAssets().open("complete_computer_vocabulary.txt");
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8), 65536);
                
                categoryWords.clear();
                categorySets.clear();
                categoryNames.clear();
                
                String line;
                String currentCategory = "General"; // Default category if none found at start
                
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) continue;

                    if (trimmed.startsWith("==========")) {
                        // Extract category name: "========== PROGRAMMING ==========" -> "PROGRAMMING"
                        currentCategory = trimmed.replace("=", "").trim();
                        if (!categoryNames.contains(currentCategory)) {
                            categoryNames.add(currentCategory);
                            categoryWords.put(currentCategory, new ArrayList<>());
                            categorySets.put(currentCategory, new HashSet<>());
                        }
                    } else {
                        String fullPhrase = trimmed.toLowerCase();
                        if (fullPhrase.length() > 1) {
                            if (!categoryWords.containsKey(currentCategory)) {
                                categoryNames.add(currentCategory);
                                categoryWords.put(currentCategory, new ArrayList<>());
                                categorySets.put(currentCategory, new HashSet<>());
                            }
                            
                            // เก็บวลีเต็มไว้เช็คความถูกต้อง
                            categorySets.get(currentCategory).add(fullPhrase);
                            
                            // แยกเป็นคำๆ เพื่อเอามาเป็นตัวสุ่มโจทย์ (ป้องกันการสุ่มเจอช่องว่าง)
                            String[] parts = fullPhrase.split("[\\s-]+");
                            for (String part : parts) {
                                if (part.length() >= 3) {
                                    categoryWords.get(currentCategory).add(part);
                                }
                            }
                        }
                    }
                }
                reader.close();
                isLoaded = true;
                
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (callback != null) callback.onLoaded(categoryNames.size());
                });
                
            } catch (Exception e) {
                Log.e("DictionaryManager", "Error loading dictionary", e);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (callback != null) callback.onError("ไม่พบไฟล์คำศัพท์หรือโหลดไฟล์ล้มเหลว");
                });
            }
        }).start();
    }

    public void setSelectedCategory(String category) {
        this.selectedCategory = category;
    }

    public List<String> getCategoryNames() {
        return categoryNames;
    }

    public String getRandomPrompt() {
        if (!isLoaded) return "TH";
        
        List<String> wordList = (selectedCategory != null) ? categoryWords.get(selectedCategory) : null;
        if (wordList == null || wordList.isEmpty()) {
            // If no category selected or empty, pick from any category
            if (categoryNames.isEmpty()) return "TH";
            wordList = categoryWords.get(categoryNames.get(0));
        }
        
        String randomWord = "";
        int attempts = 0;
        while (randomWord.length() < 3 && attempts < 100) {
            randomWord = wordList.get(random.nextInt(wordList.size())).toUpperCase();
            attempts++;
        }
        
        if (randomWord.length() < 2) return "TH";

        int len = 2;
        int start = random.nextInt(randomWord.length() - len + 1);
        return randomWord.substring(start, start + len);
    }

    public boolean contains(String word) {
        String target = word.toLowerCase();
        if (selectedCategory != null) {
            Set<String> set = categorySets.get(selectedCategory);
            return set != null && set.contains(target);
        }
        // If no category selected, check all categories
        for (Set<String> set : categorySets.values()) {
            if (set.contains(target)) return true;
        }
        return false;
    }

    public boolean isReady() {
        return isLoaded;
    }

    public int getSize() {
        int totalSize = 0;
        for (Set<String> set : categorySets.values()) {
            totalSize += set.size();
        }
        return totalSize;
    }
}
