package com.alfarooj.timetable.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.alfarooj.timetable.utils.LanguageUtils;
import com.alfarooj.timetable.utils.TranslationUtils;
import com.alfarooj.timetable.R;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        LanguageUtils.applyLanguage(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageUtils.applyLanguage(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LanguageUtils.applyLanguage(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        
        // Translate menu item based on current language
        String currentLang = LanguageUtils.getSavedLanguage(this);
        if (!currentLang.equals("en")) {
            TranslationUtils.translate("Change Language", currentLang, new TranslationUtils.TranslateCallback() {
                @Override
                public void onResult(String translatedText) {
                    MenuItem item = menu.findItem(R.id.action_language);
                    if (item != null) {
                        item.setTitle(translatedText);
                    }
                }
                @Override
                public void onError(String error) {}
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_language) {
            showLanguageDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLanguageDialog() {
        String[] languages = LanguageUtils.getAllLanguages();
        String[] languageCodes = LanguageUtils.getAllLanguageCodes();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        // Translate dialog title
        String currentLang = LanguageUtils.getSavedLanguage(this);
        if (!currentLang.equals("en")) {
            TranslationUtils.translate("Select Language", currentLang, new TranslationUtils.TranslateCallback() {
                @Override
                public void onResult(String translatedText) {
                    builder.setTitle(translatedText);
                }
                @Override
                public void onError(String error) {
                    builder.setTitle("Select Language");
                }
            });
        } else {
            builder.setTitle("Select Language");
        }
        
        builder.setItems(languages, (dialog, which) -> {
            String selectedCode = languageCodes[which];
            LanguageUtils.setLocale(this, selectedCode);
            String message = languages[which];
            Toast.makeText(this, "Language changed to " + message, Toast.LENGTH_SHORT).show();
            recreate();
        });
        builder.show();
    }
}
