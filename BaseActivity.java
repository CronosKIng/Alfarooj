package com.alfarooj.timetable.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.alfarooj.timetable.utils.LanguageUtils;
import com.alfarooj.timetable.utils.TranslationHelper;
import com.alfarooj.timetable.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BaseActivity extends AppCompatActivity {

    protected List<String> languageCodes = new ArrayList<>();
    protected List<String> languageNames = new ArrayList<>();

    @Override
    protected void attachBaseContext(Context newBase) {
        LanguageUtils.applyLanguage(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LanguageUtils.applyLanguage(this);
        TranslationHelper.loadLanguage(this);
        setupLanguages();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LanguageUtils.applyLanguage(this);
        TranslationHelper.loadLanguage(this);
    }

    protected void setupLanguages() {
        languageCodes.add("en"); languageNames.add("English");
        languageCodes.add("sw"); languageNames.add("Kiswahili");
        languageCodes.add("ar"); languageNames.add("Arabic");
        languageCodes.add("fr"); languageNames.add("French");
        languageCodes.add("es"); languageNames.add("Spanish");
        languageCodes.add("de"); languageNames.add("German");
        languageCodes.add("it"); languageNames.add("Italian");
        languageCodes.add("pt"); languageNames.add("Portuguese");
        languageCodes.add("ru"); languageNames.add("Russian");
        languageCodes.add("zh"); languageNames.add("Chinese");
        languageCodes.add("ja"); languageNames.add("Japanese");
        languageCodes.add("ko"); languageNames.add("Korean");
        languageCodes.add("hi"); languageNames.add("Hindi");
    }

    // METHOD MPYA: Ongeza comment kwa ajili ya kuchelewa
    protected void showCommentDialog(String eventType, String eventName, Runnable onSuccess) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(TranslationHelper.translateText("Reason for lateness / Comment"));
        
        final EditText input = new EditText(this);
        input.setHint(TranslationHelper.translateText("Enter your reason here..."));
        input.setPadding(50, 30, 50, 30);
        builder.setView(input);
        
        builder.setPositiveButton(TranslationHelper.translateText("Submit"), (dialog, which) -> {
            String comment = input.getText().toString().trim();
            if (comment.isEmpty()) {
                Toast.makeText(this, TranslationHelper.translateText("Please enter a reason"), Toast.LENGTH_SHORT).show();
            } else {
                onSuccess.run();
            }
        });
        builder.setNegativeButton(TranslationHelper.translateText("Cancel"), null);
        builder.show();
    }

    protected void showLanguageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(TranslationHelper.translateText("Select Language / Chagua Lugha"));

        String[] languages = languageNames.toArray(new String[0]);

        builder.setItems(languages, (dialog, which) -> {
            String selectedCode = languageCodes.get(which);
            TranslationHelper.setCurrentLanguage(selectedCode);
            TranslationHelper.saveLanguage(this, selectedCode);
            Toast.makeText(this, TranslationHelper.translateText("Language changed to ") + languages[which], Toast.LENGTH_SHORT).show();
            recreate();
        });
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
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
}
