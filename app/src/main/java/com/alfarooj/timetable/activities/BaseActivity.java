package com.alfarooj.timetable.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.navigation.NavigationView;
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
        translateAllUIElements();
    }

    protected void setupLanguages() {
        languageCodes.clear();
        languageNames.clear();
        languageCodes.add("en"); languageNames.add("English");
        languageCodes.add("bn"); languageNames.add("Bengali");
        languageCodes.add("sw"); languageNames.add("Kiswahili");
        languageCodes.add("ar"); languageNames.add("Arabic");
        languageCodes.add("hi"); languageNames.add("Hindi");
        languageCodes.add("ur"); languageNames.add("Urdu");
        languageCodes.add("ne"); languageNames.add("Nepali");
        languageCodes.add("am"); languageNames.add("Amharic");
    }

    protected void translateAllUIElements() {
        ViewGroup rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            TranslationHelper.translateViewGroup(rootView);
        }
    }

    protected void translateToolbar(Toolbar toolbar) {
        TranslationHelper.translateToolbar(toolbar);
    }

    protected void translateNavigationView(NavigationView navView) {
        TranslationHelper.translateNavigationView(navView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        TranslationHelper.translateMenu(menu);
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

    protected void showLanguageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(TranslationHelper.translateTextDirect("Select Language"));
        String[] languages = languageNames.toArray(new String[0]);
        for (int i = 0; i < languages.length; i++) {
            languages[i] = TranslationHelper.translateTextDirect(languages[i]);
        }

        builder.setItems(languages, (dialog, which) -> {
            String selectedCode = languageCodes.get(which);
            TranslationHelper.clearCache(); // Futa cache ya lugha ya zamani
            TranslationHelper.setCurrentLanguage(selectedCode);
            TranslationHelper.saveLanguage(this, selectedCode);
            LanguageUtils.setLocale(this, selectedCode);
            translateAllUIElements();
            Toast.makeText(this, 
                TranslationHelper.translateTextDirect("Language changed to ") + languageNames.get(which), 
                Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }
}
