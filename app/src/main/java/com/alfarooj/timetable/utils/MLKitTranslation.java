package com.alfarooj.timetable.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.TextView;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import java.util.HashMap;
import java.util.Map;

public class MLKitTranslation {
    private static final String PREF_NAME = "translation_cache";
    private static Map<String, String> cache = new HashMap<>();
    
    public interface TranslationCallback {
        void onSuccess(String translatedText);
        void onFailure(String error);
    }
    
    public static void translateText(Context context, String text, String targetLang, TranslationCallback callback) {
        if (text == null || text.isEmpty()) {
            callback.onSuccess(text);
            return;
        }
        
        // Check cache
        String cacheKey = "en_" + targetLang + "_" + text;
        if (cache.containsKey(cacheKey)) {
            callback.onSuccess(cache.get(cacheKey));
            return;
        }
        
        // If target is English, return original
        if (targetLang.equals("en")) {
            callback.onSuccess(text);
            return;
        }
        
        // Get language codes
        TranslateLanguage sourceLanguage = getLanguageCode("en");
        TranslateLanguage targetLanguage = getLanguageCode(targetLang);
        
        if (sourceLanguage == null || targetLanguage == null) {
            callback.onSuccess(text);
            return;
        }
        
        // Initialize translator
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguage)
                .setTargetLanguage(targetLanguage)
                .build();
        
        Translator translator = Translation.getClient(options);
        
        // Download model if needed
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();
        
        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(unused -> {
                    translator.translate(text)
                            .addOnSuccessListener(translatedText -> {
                                cache.put(cacheKey, translatedText);
                                callback.onSuccess(translatedText);
                                translator.close();
                            })
                            .addOnFailureListener(e -> {
                                callback.onFailure(e.getMessage());
                                translator.close();
                            });
                })
                .addOnFailureListener(e -> {
                    callback.onFailure("Model download failed: " + e.getMessage());
                    translator.close();
                });
    }
    
    public static void translateTextView(Context context, TextView textView, String targetLang) {
        String originalText = textView.getText().toString();
        translateText(context, originalText, targetLang, new TranslationCallback() {
            @Override
            public void onSuccess(String translatedText) {
                textView.setText(translatedText);
            }
            @Override
            public void onFailure(String error) {
                // Keep original text
            }
        });
    }
    
    private static TranslateLanguage getLanguageCode(String langCode) {
        switch (langCode) {
            case "en": return TranslateLanguage.ENGLISH;
            case "sw": return TranslateLanguage.SWAHILI;
            case "ar": return TranslateLanguage.ARABIC;
            case "fr": return TranslateLanguage.FRENCH;
            case "es": return TranslateLanguage.SPANISH;
            case "de": return TranslateLanguage.GERMAN;
            case "it": return TranslateLanguage.ITALIAN;
            case "pt": return TranslateLanguage.PORTUGUESE;
            case "ru": return TranslateLanguage.RUSSIAN;
            case "zh": return TranslateLanguage.CHINESE;
            case "ja": return TranslateLanguage.JAPANESE;
            case "ko": return TranslateLanguage.KOREAN;
            case "hi": return TranslateLanguage.HINDI;
            default: return null;
        }
    }
}
