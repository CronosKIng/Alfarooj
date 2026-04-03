package com.alfarooj.timetable.utils;

import android.content.Context;
import android.widget.TextView;
import com.alfarooj.timetable.api.ApiClient;
import com.alfarooj.timetable.models.TranslateRequest;
import com.alfarooj.timetable.models.TranslateResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TranslationHelper {
    private static Map<String, String> translationCache = new HashMap<>();
    private static String currentLanguage = "en";
    
    public interface TranslationCallback {
        void onSuccess(String translatedText);
        void onError(String error);
    }
    
    public static void setCurrentLanguage(String langCode) {
        currentLanguage = langCode;
    }
    
    public static String getCurrentLanguage() {
        return currentLanguage;
    }
    
    public static void translateText(String text, TranslationCallback callback) {
        if (currentLanguage.equals("en")) {
            callback.onSuccess(text);
            return;
        }
        
        String cacheKey = text + "_" + currentLanguage;
        if (translationCache.containsKey(cacheKey)) {
            callback.onSuccess(translationCache.get(cacheKey));
            return;
        }
        
        ApiClient.getApiService().translateText(new TranslateRequest(text, currentLanguage))
            .enqueue(new Callback<TranslateResponse>() {
                @Override
                public void onResponse(Call<TranslateResponse> call, Response<TranslateResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        String translated = response.body().getTranslated();
                        translationCache.put(cacheKey, translated);
                        callback.onSuccess(translated);
                    } else {
                        callback.onError("Translation failed");
                    }
                }
                
                @Override
                public void onFailure(Call<TranslateResponse> call, Throwable t) {
                    callback.onError(t.getMessage());
                }
            });
    }
    
    public static void translateTextView(TextView textView, String originalText) {
        translateText(originalText, new TranslationCallback() {
            @Override
            public void onSuccess(String translatedText) {
                textView.setText(translatedText);
            }
            
            @Override
            public void onError(String error) {
                textView.setText(originalText);
            }
        });
    }
    
    public static void translateMultipleTexts(List<String> texts, BatchTranslationCallback callback) {
        if (currentLanguage.equals("en")) {
            Map<String, String> results = new HashMap<>();
            for (String text : texts) {
                results.put(text, text);
            }
            callback.onSuccess(results);
            return;
        }
        
        // Check cache first
        List<String> uncachedTexts = new ArrayList<>();
        Map<String, String> cachedResults = new HashMap<>();
        
        for (String text : texts) {
            String cacheKey = text + "_" + currentLanguage;
            if (translationCache.containsKey(cacheKey)) {
                cachedResults.put(text, translationCache.get(cacheKey));
            } else {
                uncachedTexts.add(text);
            }
        }
        
        if (uncachedTexts.isEmpty()) {
            callback.onSuccess(cachedResults);
            return;
        }
        
        // Call batch translate API
        com.alfarooj.timetable.models.BatchTranslateRequest request = 
            new com.alfarooj.timetable.models.BatchTranslateRequest(uncachedTexts, currentLanguage);
        
        ApiClient.getApiService().batchTranslate(request)
            .enqueue(new Callback<com.alfarooj.timetable.models.BatchTranslateResponse>() {
                @Override
                public void onResponse(Call<com.alfarooj.timetable.models.BatchTranslateResponse> call, 
                                       Response<com.alfarooj.timetable.models.BatchTranslateResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Map<String, String> allResults = new HashMap<>(cachedResults);
                        for (Map<String, String> result : response.body().getResults()) {
                            String original = result.get("original");
                            String translated = result.get("translated");
                            allResults.put(original, translated);
                            translationCache.put(original + "_" + currentLanguage, translated);
                        }
                        callback.onSuccess(allResults);
                    } else {
                        callback.onError("Batch translation failed");
                    }
                }
                
                @Override
                public void onFailure(Call<com.alfarooj.timetable.models.BatchTranslateResponse> call, Throwable t) {
                    callback.onError(t.getMessage());
                }
            });
    }
    
    public interface BatchTranslationCallback {
        void onSuccess(Map<String, String> translatedMap);
        void onError(String error);
    }
}
