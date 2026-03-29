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
        String[] languages = {"English", "Kiswahili", "العربية", "हिन्दी", "中文"};
        String[] languageCodes = {LanguageUtils.ENGLISH, LanguageUtils.SWAHILI, LanguageUtils.ARABIC, LanguageUtils.HINDI, LanguageUtils.CHINESE};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_language);
        builder.setItems(languages, (dialog, which) -> {
            String selectedCode = languageCodes[which];
            LanguageUtils.setLocale(this, selectedCode);
            Toast.makeText(this, "Language changed to " + languages[which], Toast.LENGTH_SHORT).show();
            recreate();
        });
        builder.show();
    }
    
    protected void restartApp() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
