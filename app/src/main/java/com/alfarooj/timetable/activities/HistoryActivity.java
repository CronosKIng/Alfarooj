package com.alfarooj.timetable.activities;

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
import com.alfarooj.timetable.utils.SessionManager;
import com.alfarooj.timetable.R;
import java.util.ArrayList;

public class HistoryActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private Spinner spinnerFilter;
    private Button btnRefresh;
    private DatabaseHelper db;
    private SessionManager session;
    private ArrayList<AttendanceLog> logList;
    
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
        
        String[] filters = {"All", "Today"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filters);
        spinnerFilter.setAdapter(adapter);
        
        loadLogs();
        btnRefresh.setOnClickListener(v -> loadLogs());
    }
    
    private void loadLogs() {
        logList = db.getAllAttendanceLogs();
        Toast.makeText(this, "Logs: " + logList.size(), Toast.LENGTH_SHORT).show();
    }
}
