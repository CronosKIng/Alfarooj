package com.alfarooj.timetable.models;

public class TranslateResponse {
    private boolean success;
    private String original;
    private String translated;
    private String target_lang;
    private String source_lang;
    
    public boolean isSuccess() { return success; }
    public String getOriginal() { return original; }
    public String getTranslated() { return translated; }
    public String getTargetLang() { return target_lang; }
    public String getSourceLang() { return source_lang; }
}
