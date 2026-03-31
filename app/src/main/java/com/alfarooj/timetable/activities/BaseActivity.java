package com.alfarooj.timetable.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.alfarooj.timetable.utils.LanguageUtils;
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
        builder.setTitle("Select Language / Chagua Lugha");
        builder.setItems(languages, (dialog, which) -> {
            String selectedCode = languageCodes[which];
            LanguageUtils.setLocale(this, selectedCode);
            Toast.makeText(this, "Language changed to " + languages[which], Toast.LENGTH_SHORT).show();
            recreate();
        });
        builder.show();
    }
}
