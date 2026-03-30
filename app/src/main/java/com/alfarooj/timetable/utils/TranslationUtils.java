package com.alfarooj.timetable.utils;

import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TranslationUtils {
    private static final String API_URL = "https://api.mymemory.translated.net/get";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler handler = new Handler(Looper.getMainLooper());
    
    public interface TranslateCallback {
        void onResult(String translatedText);
    }
    
    public static void translate(String text, String targetLang, TranslateCallback callback) {
        translate(text, "en", targetLang, callback);
    }
    
    public static void translate(String text, String sourceLang, String targetLang, TranslateCallback callback) {
        if (text == null || text.isEmpty()) {
            if (callback != null) callback.onResult(text);
            return;
        }
        
        // If target is English, return original
        if (targetLang.equals("en")) {
            if (callback != null) callback.onResult(text);
            return;
        }
        
        executor.execute(() -> {
            try {
                String encodedText = URLEncoder.encode(text, "UTF-8");
                String urlString = API_URL + "?q=" + encodedText + "&langpair=" + sourceLang + "|" + targetLang;
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                // Parse JSON manually (without org.json to avoid dependencies)
                String responseStr = response.toString();
                String translated = text;
                
                // Extract translatedText from response
                int startIndex = responseStr.indexOf("\"translatedText\":\"");
                if (startIndex != -1) {
                    startIndex += 18;
                    int endIndex = responseStr.indexOf("\"", startIndex);
                    if (endIndex != -1) {
                        translated = responseStr.substring(startIndex, endIndex);
                        // Fix HTML entities
                        translated = translated.replace("&#39;", "'").replace("&quot;", "\"");
                    }
                }
                
                final String finalTranslated = translated;
                handler.post(() -> {
                    if (callback != null) callback.onResult(finalTranslated);
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> {
                    if (callback != null) callback.onResult(text);
                });
            }
        });
    }
}
