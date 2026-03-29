package com.alfarooj.timetable.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.alfarooj.timetable.adapters.UserAdapter;
import com.alfarooj.timetable.database.DatabaseHelper;
import com.alfarooj.timetable.models.User;
import com.alfarooj.timetable.utils.SessionManager;
import com.alfarooj.timetable.R;
import java.util.ArrayList;

public class SuperAdminActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private Button btnCreateAdmin, btnCreateUser, btnViewLogs, btnLogout;
    private DatabaseHelper db;
    private SessionManager session;
    private UserAdapter userAdapter;
    private ArrayList<User> userList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_admin);
        
        db = new DatabaseHelper(this);
        session = new SessionManager(this);
        
        recyclerView = findViewById(R.id.recyclerView);
        btnCreateAdmin = findViewById(R.id.btnCreateAdmin);
        btnCreateUser = findViewById(R.id.btnCreateUser);
        btnViewLogs = findViewById(R.id.btnViewLogs);
        btnLogout = findViewById(R.id.btnLogout);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadUsers();
        
        btnCreateAdmin.setText(getString(R.string.create_admin));
        btnCreateUser.setText(getString(R.string.create_user));
        btnViewLogs.setText(getString(R.string.history));
        btnLogout.setText(getString(R.string.logout));
        
        btnCreateAdmin.setOnClickListener(v -> showCreateUserDialog("admin", null));
        btnCreateUser.setOnClickListener(v -> showCreateUserDialog("user", null));
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
    
    private void showCreateUserDialog(String role, String department) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        
        EditText etFullName = new EditText(this);
        etFullName.setHint(getString(R.string.username));
        EditText etUsername = new EditText(this);
        etUsername.setHint(getString(R.string.username));
        EditText etPassword = new EditText(this);
        etPassword.setHint(getString(R.string.password));
        
        layout.addView(etFullName);
        layout.addView(etUsername);
        layout.addView(etPassword);
        
        if (role.equals("user")) {
            Spinner spinnerDept = new Spinner(this);
            String[] departments = {"kitchen", "waiters", "delivery", "managers"};
            String[] deptNames = {getString(R.string.kitchen), getString(R.string.waiters), getString(R.string.delivery), getString(R.string.managers)};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, deptNames);
            spinnerDept.setAdapter(adapter);
            layout.addView(spinnerDept);
            
            builder.setPositiveButton(getString(R.string.create_user), (dialog, which) -> {
                String selectedDept = departments[spinnerDept.getSelectedItemPosition()];
                if (db.createUser(etFullName.getText().toString(), etUsername.getText().toString(), etPassword.getText().toString(), role, selectedDept, session.getUserId())) {
                    Toast.makeText(this, getString(R.string.success), Toast.LENGTH_SHORT).show();
                    loadUsers();
                } else {
                    Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            builder.setPositiveButton(getString(R.string.create_admin), (dialog, which) -> {
                if (db.createUser(etFullName.getText().toString(), etUsername.getText().toString(), etPassword.getText().toString(), role, null, session.getUserId())) {
                    Toast.makeText(this, getString(R.string.success), Toast.LENGTH_SHORT).show();
                    loadUsers();
                } else {
                    Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        builder.setView(layout);
        builder.setNegativeButton(getString(R.string.logout), null);
        builder.show();
    }
}
