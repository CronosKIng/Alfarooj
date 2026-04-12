package com.alfarooj.timetable.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.TextView;
import com.alfarooj.timetable.api.ApiClient;
import com.alfarooj.timetable.models.TranslateRequest;
import com.alfarooj.timetable.models.TranslateResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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
        // Clear cache when language changes for fresh translations
        translationCache.clear();
    }

    public static void loadLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        currentLanguage = prefs.getString(KEY_LANGUAGE, "en");
    }

    public static void setCurrentLanguage(String langCode) {
        currentLanguage = langCode;
        // Clear cache when language changes
        translationCache.clear();
    }

    public static String getCurrentLanguage() {
        return currentLanguage;
    }

    // INSTANT TRANSLATION - inarudisha tafsiri mara moja
    public static String translateTextDirect(String text) {
        if (text == null || text.isEmpty()) return text;
        if (currentLanguage.equals("en")) return text;
        
        // Angalia cache kwanza
        String cacheKey = text + "_" + currentLanguage;
        if (translationCache.containsKey(cacheKey)) {
            return translationCache.get(cacheKey);
        }
        
        // Jaribu kupata tafsiri kutoka cache ya pending
        // Kama haipo, rudisha text asili kwa sasa (itatafsiriwa baadaye)
        return text;
    }
    
    // Tafsiri ya synchronous (inangoja mpaka tafsiri ipatikane)
    public static String translateTextSync(String text) {
        if (text == null || text.isEmpty()) return text;
        if (currentLanguage.equals("en")) return text;
        
        String cacheKey = text + "_" + currentLanguage;
        if (translationCache.containsKey(cacheKey)) {
            return translationCache.get(cacheKey);
        }
        
        try {
            final String[] translated = {text};
            final CountDownLatch latch = new CountDownLatch(1);
            
            ApiClient.getApiService().translateText(new TranslateRequest(text, currentLanguage))
                .enqueue(new Callback<TranslateResponse>() {
                    @Override
                    public void onResponse(Call<TranslateResponse> call, Response<TranslateResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            translated[0] = response.body().getTranslated();
                            translationCache.put(cacheKey, translated[0]);
                        }
                        latch.countDown();
                    }
                    
                    @Override
                    public void onFailure(Call<TranslateResponse> call, Throwable t) {
                        latch.countDown();
                    }
                });
            
            latch.await(2, TimeUnit.SECONDS);
            return translated[0];
        } catch (Exception e) {
            return text;
        }
    }

    public static void translateTextView(TextView textView, String originalText) {
        if (textView == null) return;
        String translated = translateTextDirect(originalText);
        textView.setText(translated);
        
        // Ikiwa haijatafsiriwa na lugha si English, jaribu synchronous
        if (!currentLanguage.equals("en") && translated.equals(originalText)) {
            new Thread(() -> {
                String synced = translateTextSync(originalText);
                if (textView != null && !synced.equals(originalText)) {
                    textView.post(() -> textView.setText(synced));
                }
            }).start();
        }
    }

    public static void translateButtonText(android.widget.Button button, String originalText) {
        if (button == null) return;
        String translated = translateTextDirect(originalText);
        button.setText(translated);
        
        if (!currentLanguage.equals("en") && translated.equals(originalText)) {
            new Thread(() -> {
                String synced = translateTextSync(originalText);
                if (button != null && !synced.equals(originalText)) {
                    button.post(() -> button.setText(synced));
                }
            }).start();
        }
    }

    public static void translateHint(TextView textView, String originalHint) {
        if (textView == null) return;
        String translated = translateTextDirect(originalHint);
        textView.setHint(translated);
        
        if (!currentLanguage.equals("en") && translated.equals(originalHint)) {
            new Thread(() -> {
                String synced = translateTextSync(originalHint);
                if (textView != null && !synced.equals(originalHint)) {
                    textView.post(() -> textView.setHint(synced));
                }
            }).start();
        }
    }

    public interface TranslationCallback {
        void onSuccess(String translatedText);
        void onError(String error);
    }
}
