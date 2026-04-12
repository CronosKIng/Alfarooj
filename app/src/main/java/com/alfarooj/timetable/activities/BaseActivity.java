package com.alfarooj.timetable.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.alfarooj.timetable.utils.LanguageUtils;
import com.alfarooj.timetable.utils.TranslationHelper;
import com.alfarooj.timetable.R;
import java.util.ArrayList;
import java.util.List;

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
        // Tafsiri UI yote kila inaporudi
        translateAllUIElements();
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
        languageCodes.add("tr"); languageNames.add("Turkish");
        languageCodes.add("nl"); languageNames.add("Dutch");
        languageCodes.add("el"); languageNames.add("Greek");
        languageCodes.add("vi"); languageNames.add("Vietnamese");
        languageCodes.add("th"); languageNames.add("Thai");
        languageCodes.add("pl"); languageNames.add("Polish");
        languageCodes.add("uk"); languageNames.add("Ukrainian");
    }

    // Tafsiri UI yote kwa kutumia API - neno kwa neno
    protected void translateAllUIElements() {
        View rootView = findViewById(android.R.id.content);
        if (rootView instanceof ViewGroup) {
            translateViewGroup((ViewGroup) rootView);
        }
    }

    private void translateViewGroup(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof TextView && !(child instanceof EditText)) {
                TextView tv = (TextView) child;
                String text = tv.getText().toString();
                if (text != null && !text.isEmpty()) {
                    TranslationHelper.translateTextView(tv, text);
                }
                String hint = tv.getHint() != null ? tv.getHint().toString() : "";
                if (!hint.isEmpty()) {
                    TranslationHelper.translateHint(tv, hint);
                }
            } else if (child instanceof Button) {
                Button btn = (Button) child;
                String text = btn.getText().toString();
                if (text != null && !text.isEmpty()) {
                    TranslationHelper.translateButtonText(btn, text);
                }
            } else if (child instanceof ViewGroup) {
                translateViewGroup((ViewGroup) child);
            }
        }
    }

    protected void showLanguageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(TranslationHelper.translateTextDirect("Select Language / Chagua Lugha"));
        String[] languages = languageNames.toArray(new String[0]);

        builder.setItems(languages, (dialog, which) -> {
            String selectedCode = languageCodes.get(which);
            TranslationHelper.setCurrentLanguage(selectedCode);
            TranslationHelper.saveLanguage(this, selectedCode);
            LanguageUtils.setLocale(this, selectedCode);
            // Badala ya recreate, tafsiri UI yote tena
            translateAllUIElements();
            Toast.makeText(this, TranslationHelper.translateTextDirect("Language changed to ") + languages[which], Toast.LENGTH_SHORT).show();
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
