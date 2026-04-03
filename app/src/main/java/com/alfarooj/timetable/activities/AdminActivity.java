package com.alfarooj.timetable.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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
import java.util.List;

public class AdminActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private Button btnCreateUser, btnViewLogs, btnLogout;
    private DatabaseHelper db;
    private SessionManager session;
    private ArrayList<User> userList;
    private Spinner spinnerLanguage;
    private TextView tvLanguageLabel;
    private Toolbar toolbar;
    private List<String> languageCodes = new ArrayList<>();
    private List<String> languageNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        try {
            db = new DatabaseHelper(this);
            session = new SessionManager(this);

            toolbar = findViewById(R.id.toolbar);
            recyclerView = findViewById(R.id.recyclerView);
            btnCreateUser = findViewById(R.id.btnCreateUser);
            btnViewLogs = findViewById(R.id.btnViewLogs);
            btnLogout = findViewById(R.id.btnLogout);
            spinnerLanguage = findViewById(R.id.spinnerLanguage);
            tvLanguageLabel = findViewById(R.id.tvLanguageLabel);

            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Admin Dashboard");
            }

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
            setupLanguageSpinner();
            translateUI();
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void setupLanguageSpinner() {
        languageCodes.add("en"); languageNames.add("English");
        languageCodes.add("sw"); languageNames.add("Kiswahili");
        languageCodes.add("ar"); languageNames.add("Arabic");
        languageCodes.add("fr"); languageNames.add("French");
        languageCodes.add("es"); languageNames.add("Spanish");
        languageCodes.add("de"); languageNames.add("German");
        languageCodes.add("it"); languageNames.add("Italian");
        languageCodes.add("pt"); languageNames.add("Portuguese");
        languageCodes.add("ru"); languageNames.add("Russian");
        languageCodes.add("zh"); languageNames.add("Chinese");
        languageCodes.add("ja"); languageNames.add("Japanese");
        languageCodes.add("ko"); languageNames.add("Korean");
        languageCodes.add("hi"); languageNames.add("Hindi");
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, languageNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);
        
        String savedLang = TranslationHelper.getCurrentLanguage();
        int position = languageCodes.indexOf(savedLang);
        if (position >= 0) {
            spinnerLanguage.setSelection(position);
        }
        
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newLang = languageCodes.get(position);
                if (!newLang.equals(TranslationHelper.getCurrentLanguage())) {
                    TranslationHelper.setCurrentLanguage(newLang);
                    translateUI();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private void translateUI() {
        String targetLang = TranslationHelper.getCurrentLanguage();
        if (targetLang.equals("en")) {
            tvLanguageLabel.setText("Language:");
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Admin Dashboard");
            }
            btnCreateUser.setText("CREATE USER");
            btnViewLogs.setText("VIEW HISTORY");
            btnLogout.setText("LOGOUT");
            return;
        }
        TranslationHelper.translateText("Language:", new TranslationHelper.TranslationCallback() {
            @Override
            public void onSuccess(String translated) { tvLanguageLabel.setText(translated); }
            @Override
            public void onError(String error) {}
        });
        TranslationHelper.translateText("Admin Dashboard", new TranslationHelper.TranslationCallback() {
            @Override
            public void onSuccess(String translated) { 
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(translated);
                }
            }
            @Override
            public void onError(String error) {}
        });
        TranslationHelper.translateText("CREATE USER", new TranslationHelper.TranslationCallback() {
            @Override
            public void onSuccess(String translated) { btnCreateUser.setText(translated); }
            @Override
            public void onError(String error) {}
        });
        TranslationHelper.translateText("VIEW HISTORY", new TranslationHelper.TranslationCallback() {
            @Override
            public void onSuccess(String translated) { btnViewLogs.setText(translated); }
            @Override
            public void onError(String error) {}
        });
        TranslationHelper.translateText("LOGOUT", new TranslationHelper.TranslationCallback() {
            @Override
            public void onSuccess(String translated) { btnLogout.setText(translated); }
            @Override
            public void onError(String error) {}
        });
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
