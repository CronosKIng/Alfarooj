package com.alfarooj.timetable.models;

import java.util.List;
import java.util.Map;

public class LanguagesResponse {
    private boolean success;
    private List<Map<String, String>> languages;
    
    public boolean isSuccess() { return success; }
    public List<Map<String, String>> getLanguages() { return languages; }
}
