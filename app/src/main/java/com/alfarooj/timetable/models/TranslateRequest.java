package com.alfarooj.timetable.models;

public class TranslateRequest {
    private String text;
    private String target_lang;
    private String source_lang;
    
    public TranslateRequest(String text, String target_lang) {
        this.text = text;
        this.target_lang = target_lang;
        this.source_lang = "en";
    }
    
    public TranslateRequest(String text, String target_lang, String source_lang) {
        this.text = text;
        this.target_lang = target_lang;
        this.source_lang = source_lang;
    }
}
