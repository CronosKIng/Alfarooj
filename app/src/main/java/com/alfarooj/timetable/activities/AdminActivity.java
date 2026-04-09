package com.alfarooj.timetable.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.alfarooj.timetable.adapters.UserAdapter;
import com.alfarooj.timetable.database.DatabaseHelper;
import com.alfarooj.timetable.models.User;
import com.alfarooj.timetable.utils.SessionManager;
import com.alfarooj.timetable.utils.TranslationHelper;
import com.alfarooj.timetable.R;
import java.util.ArrayList;

public class AdminActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private Button btnCreateUser, btnViewLogs, btnLogout;
    private DatabaseHelper db;
    private SessionManager session;
    private ArrayList<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        try {
            db = new DatabaseHelper(this);
            session = new SessionManager(this);

            recyclerView = findViewById(R.id.recyclerView);
            btnCreateUser = findViewById(R.id.btnCreateUser);
            btnViewLogs = findViewById(R.id.btnViewLogs);
            btnLogout = findViewById(R.id.btnLogout);

            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            btnCreateUser.setOnClickListener(v -> showCreateUserDialog());
            btnViewLogs.setOnClickListener(v -> {
                Intent intent = new Intent(AdminActivity.this, HistoryActivity.class);
                startActivity(intent);
            });
            btnLogout.setOnClickListener(v -> {
                session.logout();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });

            loadUsers();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
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

    private void showCreateUserDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Create User");

            View view = getLayoutInflater().inflate(R.layout.dialog_create_user, null);
            EditText etFullName = view.findViewById(R.id.etFullName);
            EditText etUsername = view.findViewById(R.id.etUsername);
            EditText etPassword = view.findViewById(R.id.etPassword);
            Spinner spinnerDepartment = view.findViewById(R.id.spinnerDepartment);

            String[] departments = {"kitchen", "waiter", "delivery", "manager"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departments);
            spinnerDepartment.setAdapter(adapter);

            builder.setView(view);
            builder.setPositiveButton("Create", (dialog, which) -> {
                String fullName = etFullName.getText().toString().trim();
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String department = spinnerDepartment.getSelectedItem().toString();

                if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean success = db.createUser(fullName, username, password, "user", department, session.getUserId());
                if (success) {
                    Toast.makeText(this, "User created successfully!", Toast.LENGTH_SHORT).show();
                    loadUsers();
                } else {
                    Toast.makeText(this, "Error: Username already exists", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadUsers() {
        try {
            userList = db.getAllUsers();
            UserAdapter adapter = new UserAdapter(userList, this, () -> loadUsers());
            recyclerView.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
