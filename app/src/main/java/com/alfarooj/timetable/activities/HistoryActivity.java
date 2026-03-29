package com.alfarooj.timetable.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.alfarooj.timetable.adapters.LogAdapter;
import com.alfarooj.timetable.database.DatabaseHelper;
import com.alfarooj.timetable.models.AttendanceLog;
import com.alfarooj.timetable.utils.SessionManager;
import com.alfarooj.timetable.R;
import java.util.ArrayList;

public class HistoryActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private Spinner spinnerFilter;
    private Button btnRefresh;
    private DatabaseHelper db;
    private SessionManager session;
    private LogAdapter adapter;
    private ArrayList<AttendanceLog> logList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        db = new DatabaseHelper(this);
        session = new SessionManager(this);
        
        recyclerView = findViewById(R.id.recyclerView);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        btnRefresh = findViewById(R.id.btnRefresh);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        String[] filters = {"Leo Tu", "Zote", "Zangu Tu"};
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filters);
        spinnerFilter.setAdapter(filterAdapter);
        
        btnRefresh.setText(getString(R.string.history));
        
        loadLogs();
        
        btnRefresh.setOnClickListener(v -> loadLogs());
    }
    
    private void loadLogs() {
        String filter = spinnerFilter.getSelectedItem().toString();
        String role = session.getRole();
        
        if (filter.equals("Leo Tu")) {
            logList = db.getTodayAttendanceLogs();
        } else if (filter.equals("Zangu Tu") && !role.equals("super_admin")) {
            logList = db.getUserAttendanceLogs(session.getUserId());
        } else {
            logList = db.getAllAttendanceLogs();
        }
        
        adapter = new LogAdapter(logList);
        recyclerView.setAdapter(adapter);
        
        Toast.makeText(this, "Logs: " + logList.size() + " records", Toast.LENGTH_SHORT).show();
    }
}
