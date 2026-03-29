package com.alfarooj.timetable.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.alfarooj.timetable.adapters.UserAdapter;
import com.alfarooj.timetable.database.DatabaseHelper;
import com.alfarooj.timetable.models.User;
import com.alfarooj.timetable.utils.SessionManager;
import com.alfarooj.timetable.R;
import java.util.ArrayList;

public class AdminActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private Button btnCreateUser, btnViewLogs, btnLogout;
    private DatabaseHelper db;
    private SessionManager session;
    private UserAdapter userAdapter;
    private ArrayList<User> userList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        db = new DatabaseHelper(this);
        session = new SessionManager(this);
        
        recyclerView = findViewById(R.id.recyclerView);
        btnCreateUser = findViewById(R.id.btnCreateUser);
        btnViewLogs = findViewById(R.id.btnViewLogs);
        btnLogout = findViewById(R.id.btnLogout);
        
        btnCreateUser.setText(getString(R.string.create_user));
        btnViewLogs.setText(getString(R.string.history));
        btnLogout.setText(getString(R.string.logout));
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadUsers();
        
        btnCreateUser.setOnClickListener(v -> showCreateUserDialog());
        btnViewLogs.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        btnLogout.setOnClickListener(v -> {
            session.logout();
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        });
    }
    
    private void loadUsers() {
        userList = db.getAllUsers();
        userAdapter = new UserAdapter(userList, this, () -> loadUsers());
        recyclerView.setAdapter(userAdapter);
    }
    
    private void showCreateUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        
        EditText etFullName = new EditText(this);
        etFullName.setHint(getString(R.string.username));
        EditText etUsername = new EditText(this);
        etUsername.setHint(getString(R.string.username));
        EditText etPassword = new EditText(this);
        etPassword.setHint(getString(R.string.password));
        
        Spinner spinnerDept = new Spinner(this);
        String[] departments = {"kitchen", "waiters", "delivery", "managers"};
        String[] deptNames = {getString(R.string.kitchen), getString(R.string.waiters), getString(R.string.delivery), getString(R.string.managers)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, deptNames);
        spinnerDept.setAdapter(adapter);
        
        layout.addView(etFullName);
        layout.addView(etUsername);
        layout.addView(etPassword);
        layout.addView(spinnerDept);
        
        builder.setView(layout);
        builder.setPositiveButton(getString(R.string.create_user), (dialog, which) -> {
            String selectedDept = departments[spinnerDept.getSelectedItemPosition()];
            if (db.createUser(etFullName.getText().toString(), etUsername.getText().toString(), etPassword.getText().toString(), "user", selectedDept, session.getUserId())) {
                Toast.makeText(this, getString(R.string.success), Toast.LENGTH_SHORT).show();
                loadUsers();
            } else {
                Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(getString(R.string.logout), null);
        builder.show();
    }
}
