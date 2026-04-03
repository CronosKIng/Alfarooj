package com.alfarooj.timetable.models;

import java.util.List;
import java.util.Map;

public class BatchTranslateResponse {
    private boolean success;
    private List<Map<String, String>> results;
    private String target_lang;
    
    public boolean isSuccess() { return success; }
    public List<Map<String, String>> getResults() { return results; }
    public String getTargetLang() { return target_lang; }
}
