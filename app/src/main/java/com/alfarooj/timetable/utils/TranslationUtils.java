package com.alfarooj.timetable.utils;

import android.os.AsyncTask;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class TranslationUtils {
    private static final String API_URL = "https://api.mymemory.translated.net/get";
    private static Map<String, String> cache = new HashMap<>();
    
    public interface TranslateCallback {
        void onResult(String translatedText);
        void onError(String error);
    }
    
    public static void translate(String text, String targetLang, TranslateCallback callback) {
        translate(text, "en", targetLang, callback);
    }
    
    public static void translate(String text, String sourceLang, String targetLang, TranslateCallback callback) {
        // Skip if empty
        if (text == null || text.isEmpty()) {
            callback.onResult(text);
            return;
        }
        
        // Check cache
        String cacheKey = sourceLang + "_" + targetLang + "_" + text;
        if (cache.containsKey(cacheKey)) {
            callback.onResult(cache.get(cacheKey));
            return;
        }
        
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
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
                    
                    JSONObject json = new JSONObject(response.toString());
                    String translated = json.getJSONObject("responseData").getString("translatedText");
                    
                    // Fix HTML entities
                    translated = translated.replace("&#39;", "'").replace("&quot;", "\"");
                    
                    cache.put(cacheKey, translated);
                    return translated;
                } catch (Exception e) {
                    e.printStackTrace();
                    return text; // Return original on error
                }
            }
            
            @Override
            protected void onPostExecute(String result) {
                callback.onResult(result);
            }
        }.execute();
    }
}
