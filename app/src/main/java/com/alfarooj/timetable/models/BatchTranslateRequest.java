package com.alfarooj.timetable.models;

import java.util.List;

public class BatchTranslateRequest {
    private List<String> texts;
    private String target_lang;
    private String source_lang;
    
    public BatchTranslateRequest(List<String> texts, String target_lang) {
        this.texts = texts;
        this.target_lang = target_lang;
        this.source_lang = "en";
    }
}
