package com.alfarooj.timetable.utils;

import android.content.Context;
import android.content.SharedPreferences;
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
    private static final String PREF_NAME = "translation_prefs";
    private static final String KEY_LANGUAGE = "selected_language";
    private static Map<String, String> translationCache = new HashMap<>();
    private static String currentLanguage = "en";
    
    public static void saveLanguage(Context context, String langCode) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply();
        currentLanguage = langCode;
    }
    
    public static void loadLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        currentLanguage = prefs.getString(KEY_LANGUAGE, "en");
    }
    
    public static void setCurrentLanguage(String langCode) {
        currentLanguage = langCode;
    }
    
    public static String getCurrentLanguage() {
        return currentLanguage;
    }
    
    public interface TranslationCallback {
        void onSuccess(String translatedText);
        void onError(String error);
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
}
