package com.alfarooj.timetable.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.alfarooj.timetable.database.DatabaseHelper;
import com.alfarooj.timetable.utils.SessionManager;
import com.alfarooj.timetable.R;
import java.util.ArrayList;
import com.alfarooj.timetable.models.User;

public class SuperAdminActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private Button btnViewLogs, btnLogout;
    private DatabaseHelper db;
    private SessionManager session;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_admin);
        
        db = new DatabaseHelper(this);
        session = new SessionManager(this);
        
        recyclerView = findViewById(R.id.recyclerView);
        btnViewLogs = findViewById(R.id.btnViewLogs);
        btnLogout = findViewById(R.id.btnLogout);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        btnViewLogs.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        btnLogout.setOnClickListener(v -> {
            session.logout();
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        });
        
        loadUsers();
    }
    
    private void loadUsers() {
        ArrayList<User> users = db.getAllUsers();
        Toast.makeText(this, "Total users: " + users.size(), Toast.LENGTH_SHORT).show();
    }
}
