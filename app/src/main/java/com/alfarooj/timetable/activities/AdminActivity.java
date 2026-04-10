package com.alfarooj.timetable.activities;

import android.app.AlertDialog;
import android.content.Context;
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
import com.alfarooj.timetable.utils.LanguageUtils;
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
    private Toolbar toolbar;

    @Override
    protected void attachBaseContext(Context newBase) {
        LanguageUtils.applyLanguage(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        
        try {
            db = new DatabaseHelper(this);
            session = new SessionManager(this);
            toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            
            recyclerView = findViewById(R.id.recyclerView);
            btnCreateUser = findViewById(R.id.btnCreateUser);
            btnViewLogs = findViewById(R.id.btnViewLogs);
            btnLogout = findViewById(R.id.btnLogout);
            
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            
            // Tafsiri kila kitu
            translateUI();

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
    protected void onResume() {
        super.onResume();
        translateUI();
        loadUsers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        // Tafsiri options menu
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            String title = item.getTitle().toString();
            TranslationHelper.translateText(title, new TranslationHelper.TranslationCallback() {
                @Override
                public void onSuccess(String translatedText) {
                    item.setTitle(translatedText);
                }
                @Override
                public void onError(String error) {}
            });
        }
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

    private void translateUI() {
        // Tafsiri toolbar title
        TranslationHelper.translateTextView(toolbar, "Admin Dashboard");
        
        // Tafsiri button zote
        TranslationHelper.translateButtonText(btnCreateUser, "Create User");
        TranslationHelper.translateButtonText(btnViewLogs, "View Logs");
        TranslationHelper.translateButtonText(btnLogout, "Logout");
    }

    private void showCreateUserDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            
            // Tafsiri dialog title
            TranslationHelper.translateText("Create User", new TranslationHelper.TranslationCallback() {
                @Override
                public void onSuccess(String translatedText) {
                    builder.setTitle(translatedText);
                }
                @Override
                public void onError(String error) {
                    builder.setTitle("Create User");
                }
            });

            View view = getLayoutInflater().inflate(R.layout.dialog_create_user, null);
            EditText etFullName = view.findViewById(R.id.etFullName);
            EditText etUsername = view.findViewById(R.id.etUsername);
            EditText etPassword = view.findViewById(R.id.etPassword);
            Spinner spinnerDepartment = view.findViewById(R.id.spinnerDepartment);
            
            // Tafsiri hints za input fields
            TranslationHelper.translateHint(etFullName, "Full Name");
            TranslationHelper.translateHint(etUsername, "Username");
            TranslationHelper.translateHint(etPassword, "Password");
            
            String[] departments = {"kitchen", "waiter", "delivery", "manager"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departments);
            spinnerDepartment.setAdapter(adapter);
            
            builder.setView(view);
            
            // Tafsiri button za dialog
            TranslationHelper.translateText("Create", new TranslationHelper.TranslationCallback() {
                @Override
                public void onSuccess(String translatedText) {
                    builder.setPositiveButton(translatedText, (dialog, which) -> {
                        createUserLogic(etFullName, etUsername, etPassword, spinnerDepartment);
                    });
                }
                @Override
                public void onError(String error) {
                    builder.setPositiveButton("Create", (dialog, which) -> {
                        createUserLogic(etFullName, etUsername, etPassword, spinnerDepartment);
                    });
                }
            });
            
            TranslationHelper.translateText("Cancel", new TranslationHelper.TranslationCallback() {
                @Override
                public void onSuccess(String translatedText) {
                    builder.setNegativeButton(translatedText, null);
                }
                @Override
                public void onError(String error) {
                    builder.setNegativeButton("Cancel", null);
                }
            });
            
            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void createUserLogic(EditText etFullName, EditText etUsername, EditText etPassword, Spinner spinnerDepartment) {
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String department = spinnerDepartment.getSelectedItem().toString();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            TranslationHelper.translateText("Please fill all fields", new TranslationHelper.TranslationCallback() {
                @Override
                public void onSuccess(String s) { Toast.makeText(AdminActivity.this, s, Toast.LENGTH_SHORT).show(); }
                @Override
                public void onError(String e) { Toast.makeText(AdminActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show(); }
            });
            return;
        }
        
        boolean success = db.createUser(fullName, username, password, "user", department, session.getUserId());
        if (success) {
            TranslationHelper.translateText("User created successfully!", new TranslationHelper.TranslationCallback() {
                @Override
                public void onSuccess(String s) { Toast.makeText(AdminActivity.this, s, Toast.LENGTH_SHORT).show(); }
                @Override
                public void onError(String e) { Toast.makeText(AdminActivity.this, "User created successfully!", Toast.LENGTH_SHORT).show(); }
            });
            loadUsers();
        } else {
            TranslationHelper.translateText("Error: Username already exists", new TranslationHelper.TranslationCallback() {
                @Override
                public void onSuccess(String s) { Toast.makeText(AdminActivity.this, s, Toast.LENGTH_SHORT).show(); }
                @Override
                public void onError(String e) { Toast.makeText(AdminActivity.this, "Error: Username already exists", Toast.LENGTH_SHORT).show(); }
            });
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
