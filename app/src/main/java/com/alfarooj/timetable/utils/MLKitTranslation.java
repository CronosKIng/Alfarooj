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
    private static Translator translator;
    private static String currentTargetLang = "en";
    
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
        
        // Initialize translator
        String sourceLang = "en";
        String mlKitTargetLang = getMLKitLanguageCode(targetLang);
        
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.fromLanguageCode(sourceLang))
                .setTargetLanguage(TranslateLanguage.fromLanguageCode(mlKitTargetLang))
                .build();
        
        translator = Translation.getClient(options);
        
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
                            })
                            .addOnFailureListener(e -> {
                                callback.onFailure(e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    callback.onFailure("Model download failed: " + e.getMessage());
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
    
    private static String getMLKitLanguageCode(String langCode) {
        switch (langCode) {
            case "sw": return "sw";
            case "ar": return "ar";
            case "fr": return "fr";
            case "es": return "es";
            case "de": return "de";
            case "it": return "it";
            case "pt": return "pt";
            case "ru": return "ru";
            case "zh": return "zh";
            case "ja": return "ja";
            case "ko": return "ko";
            case "hi": return "hi";
            default: return "en";
        }
    }
}
