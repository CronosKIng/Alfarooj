package com.alfarooj.timetable.activities;

import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.alfarooj.timetable.adapters.LogAdapter;
import com.alfarooj.timetable.database.DatabaseHelper;
import com.alfarooj.timetable.models.AttendanceLog;
import com.alfarooj.timetable.utils.LanguageUtils;
import com.alfarooj.timetable.utils.SessionManager;
import com.alfarooj.timetable.utils.TranslationHelper;
import com.alfarooj.timetable.R;
import java.util.ArrayList;

public class HistoryActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private Spinner spinnerFilter;
    private Button btnRefresh;
    private DatabaseHelper db;
    private SessionManager session;
    private ArrayList<AttendanceLog> logList;
    private String[] filters = {"All", "Today"};
    private String[] translatedFilters = {"All", "Today"};

    @Override
    protected void attachBaseContext(Context newBase) {
        LanguageUtils.applyLanguage(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        recyclerView = findViewById(R.id.recyclerView);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        btnRefresh = findViewById(R.id.btnRefresh);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        translateUI();
        
        loadLogs();
        btnRefresh.setOnClickListener(v -> loadLogs());
    }
    
    private void translateUI() {
        // Translate filter options
        for (int i = 0; i < filters.length; i++) {
            final int index = i;
            TranslationHelper.translateText(filters[i], new TranslationHelper.TranslationCallback() {
                @Override
                public void onSuccess(String translatedText) {
                    translatedFilters[index] = translatedText;
                }
                @Override
                public void onError(String error) {
                    translatedFilters[index] = filters[index];
                }
            });
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, translatedFilters);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);
        
        TranslationHelper.translateButtonText(btnRefresh, "Refresh");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        translateUI();
        loadLogs();
    }

    private void loadLogs() {
        logList = db.getAllAttendanceLogs();
        LogAdapter adapter = new LogAdapter(logList);
        recyclerView.setAdapter(adapter);
        
        String logMsg = "Logs: " + logList.size();
        TranslationHelper.translateText(logMsg, new TranslationHelper.TranslationCallback() {
            @Override public void onSuccess(String s) { Toast.makeText(HistoryActivity.this, s, Toast.LENGTH_SHORT).show(); }
            @Override public void onError(String e) { Toast.makeText(HistoryActivity.this, logMsg, Toast.LENGTH_SHORT).show(); }
        });
    }
}
