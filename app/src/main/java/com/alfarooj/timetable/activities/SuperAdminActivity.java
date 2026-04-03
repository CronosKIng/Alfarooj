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
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.alfarooj.timetable.adapters.UserAdapter;
import com.alfarooj.timetable.adapters.LogAdapter;
import com.alfarooj.timetable.api.ApiClient;
import com.alfarooj.timetable.database.DatabaseHelper;
import com.alfarooj.timetable.models.User;
import com.alfarooj.timetable.models.AttendanceLog;
import com.alfarooj.timetable.utils.SessionManager;
import com.alfarooj.timetable.utils.TranslationHelper;
import com.alfarooj.timetable.R;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;
import java.util.List;

public class SuperAdminActivity extends BaseActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FrameLayout contentFrame;
    private DatabaseHelper db;
    private SessionManager session;
    private RecyclerView recyclerView;
    private ArrayList<User> userList;
    private ArrayList<AttendanceLog> logList;
    private List<String> languageCodes = new ArrayList<>();
    private List<String> languageNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_admin);

        try {
            db = new DatabaseHelper(this);
            session = new SessionManager(this);

            drawerLayout = findViewById(R.id.drawerLayout);
            navigationView = findViewById(R.id.navView);
            toolbar = findViewById(R.id.toolbar);
            contentFrame = findViewById(R.id.contentFrame);

            setSupportActionBar(toolbar);

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();

            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_today_attendance) {
                    loadTodayAttendance();
                } else if (id == R.id.nav_all_history) {
                    loadAllHistory();
                } else if (id == R.id.nav_kitchen_history) {
                    loadHistoryByDepartment("kitchen");
                } else if (id == R.id.nav_waiter_history) {
                    loadHistoryByDepartment("waiter");
                } else if (id == R.id.nav_delivery_history) {
                    loadHistoryByDepartment("delivery");
                } else if (id == R.id.nav_manager_history) {
                    loadHistoryByDepartment("manager");
                } else if (id == R.id.nav_create_admin) {
                    showCreateUserDialog("admin");
                } else if (id == R.id.nav_create_user) {
                    showCreateUserDialog("user");
                } else if (id == R.id.nav_users) {
                    loadUsers();
                } else if (id == R.id.nav_logout) {
                    session.logout();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });

            loadUsers();
            setupLanguages();
            translateCurrentUI();
            
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
    
    private void setupLanguages() {
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
        languageCodes.add("tr"); languageNames.add("Turkish");
        languageCodes.add("nl"); languageNames.add("Dutch");
        languageCodes.add("el"); languageNames.add("Greek");
        languageCodes.add("vi"); languageNames.add("Vietnamese");
        languageCodes.add("th"); languageNames.add("Thai");
        languageCodes.add("pl"); languageNames.add("Polish");
        languageCodes.add("uk"); languageNames.add("Ukrainian");
    }
    
    private void showLanguageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Language / Chagua Lugha");
        
        String[] languages = languageNames.toArray(new String[0]);
        
        builder.setItems(languages, (dialog, which) -> {
            String selectedCode = languageCodes.get(which);
            TranslationHelper.setCurrentLanguage(selectedCode);
            translateCurrentUI();
            Toast.makeText(this, "Language changed to " + languages[which], Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }
    
    private void translateCurrentUI() {
        String targetLang = TranslationHelper.getCurrentLanguage();
        if (targetLang.equals("en")) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Super Admin Dashboard");
            }
            translateNavigationMenu();
            return;
        }
        
        TranslationHelper.translateText("Super Admin Dashboard", new TranslationHelper.TranslationCallback() {
            @Override
            public void onSuccess(String translated) { 
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(translated);
                }
            }
            @Override
            public void onError(String error) {}
        });
        
        translateNavigationMenu();
    }
    
    private void translateNavigationMenu() {
        Menu menu = navigationView.getMenu();
        String targetLang = TranslationHelper.getCurrentLanguage();
        if (targetLang.equals("en")) return;
        
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            String originalTitle = item.getTitle().toString();
            TranslationHelper.translateText(originalTitle, new TranslationHelper.TranslationCallback() {
                @Override
                public void onSuccess(String translated) {
                    item.setTitle(translated);
                }
                @Override
                public void onError(String error) {}
            });
        }
    }

    private void showCreateUserDialog(String role) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(role.equals("admin") ? "Create Admin" : "Create User");

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

                // Call API to create user (sends to PythonAnywhere)
                com.alfarooj.timetable.models.CreateUserRequest request = 
                    new com.alfarooj.timetable.models.CreateUserRequest(
                        fullName, username, password, role, department, session.getUserId());
                
                ApiClient.getApiService().createUser(request)
                    .enqueue(new retrofit2.Callback<com.alfarooj.timetable.models.CreateUserResponse>() {
                        @Override
                        public void onResponse(retrofit2.Call<com.alfarooj.timetable.models.CreateUserResponse> call,
                                               retrofit2.Response<com.alfarooj.timetable.models.CreateUserResponse> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Toast.makeText(SuperAdminActivity.this, "User created successfully!", Toast.LENGTH_SHORT).show();
                                loadUsers();
                            } else {
                                Toast.makeText(SuperAdminActivity.this, "Error: Username already exists", Toast.LENGTH_SHORT).show();
                            }
                        }
                        
                        @Override
                        public void onFailure(retrofit2.Call<com.alfarooj.timetable.models.CreateUserResponse> call, Throwable t) {
                            Toast.makeText(SuperAdminActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadUsers() {
        try {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Manage Users");
            }
            
            // Get users from API
            ApiClient.getApiService().getUsers()
                .enqueue(new retrofit2.Callback<com.alfarooj.timetable.models.UsersResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.alfarooj.timetable.models.UsersResponse> call,
                                           retrofit2.Response<com.alfarooj.timetable.models.UsersResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            // Convert API users to local User objects
                            userList = new ArrayList<>();
                            for (com.alfarooj.timetable.models.User apiUser : response.body().getUsers()) {
                                User localUser = new User(
                                    apiUser.getId(),
                                    apiUser.getFullName(),
                                    apiUser.getUsername(),
                                    "",
                                    apiUser.getRole(),
                                    apiUser.getDepartment(),
                                    0,
                                    ""
                                );
                                userList.add(localUser);
                            }
                            displayUsers();
                        } else {
                            Toast.makeText(SuperAdminActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    @Override
                    public void onFailure(retrofit2.Call<com.alfarooj.timetable.models.UsersResponse> call, Throwable t) {
                        Toast.makeText(SuperAdminActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void displayUsers() {
        if (contentFrame.getChildCount() > 0) {
            contentFrame.removeAllViews();
        }
        
        View view = getLayoutInflater().inflate(R.layout.fragment_user_list, null);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        UserAdapter userAdapter = new UserAdapter(userList, this, () -> loadUsers());
        recyclerView.setAdapter(userAdapter);
        
        contentFrame.addView(view);
    }

    private void loadTodayAttendance() {
        try {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Today's Attendance");
            }
            // Get attendance from API
            ApiClient.getApiService().getTodayAttendance()
                .enqueue(new retrofit2.Callback<com.alfarooj.timetable.models.AttendanceLogsResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.alfarooj.timetable.models.AttendanceLogsResponse> call,
                                           retrofit2.Response<com.alfarooj.timetable.models.AttendanceLogsResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            logList = new ArrayList<>();
                            for (com.alfarooj.timetable.models.AttendanceLog apiLog : response.body().getLogs()) {
                                AttendanceLog localLog = new AttendanceLog(
                                    apiLog.getId(), apiLog.getUserId(), apiLog.getUsername(),
                                    apiLog.getFullName(), apiLog.getDepartment(), apiLog.getEventType(),
                                    apiLog.getEventName(), apiLog.getLocation(), apiLog.getLatitude(),
                                    apiLog.getLongitude(), apiLog.getTimestamp()
                                );
                                logList.add(localLog);
                            }
                            if (logList.isEmpty()) {
                                Toast.makeText(SuperAdminActivity.this, "No attendance records for today", Toast.LENGTH_SHORT).show();
                            }
                            showHistoryList();
                        }
                    }
                    
                    @Override
                    public void onFailure(retrofit2.Call<com.alfarooj.timetable.models.AttendanceLogsResponse> call, Throwable t) {
                        Toast.makeText(SuperAdminActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAllHistory() {
        try {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("All History");
            }
            ApiClient.getApiService().getAttendanceLogs(null)
                .enqueue(new retrofit2.Callback<com.alfarooj.timetable.models.AttendanceLogsResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.alfarooj.timetable.models.AttendanceLogsResponse> call,
                                           retrofit2.Response<com.alfarooj.timetable.models.AttendanceLogsResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            logList = new ArrayList<>();
                            for (com.alfarooj.timetable.models.AttendanceLog apiLog : response.body().getLogs()) {
                                AttendanceLog localLog = new AttendanceLog(
                                    apiLog.getId(), apiLog.getUserId(), apiLog.getUsername(),
                                    apiLog.getFullName(), apiLog.getDepartment(), apiLog.getEventType(),
                                    apiLog.getEventName(), apiLog.getLocation(), apiLog.getLatitude(),
                                    apiLog.getLongitude(), apiLog.getTimestamp()
                                );
                                logList.add(localLog);
                            }
                            showHistoryList();
                        }
                    }
                    
                    @Override
                    public void onFailure(retrofit2.Call<com.alfarooj.timetable.models.AttendanceLogsResponse> call, Throwable t) {
                        Toast.makeText(SuperAdminActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadHistoryByDepartment(String department) {
        try {
            String title = department.substring(0, 1).toUpperCase() + department.substring(1);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title + " History");
            }
            ApiClient.getApiService().getAttendanceLogs(department)
                .enqueue(new retrofit2.Callback<com.alfarooj.timetable.models.AttendanceLogsResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.alfarooj.timetable.models.AttendanceLogsResponse> call,
                                           retrofit2.Response<com.alfarooj.timetable.models.AttendanceLogsResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            logList = new ArrayList<>();
                            for (com.alfarooj.timetable.models.AttendanceLog apiLog : response.body().getLogs()) {
                                AttendanceLog localLog = new AttendanceLog(
                                    apiLog.getId(), apiLog.getUserId(), apiLog.getUsername(),
                                    apiLog.getFullName(), apiLog.getDepartment(), apiLog.getEventType(),
                                    apiLog.getEventName(), apiLog.getLocation(), apiLog.getLatitude(),
                                    apiLog.getLongitude(), apiLog.getTimestamp()
                                );
                                logList.add(localLog);
                            }
                            showHistoryList();
                        }
                    }
                    
                    @Override
                    public void onFailure(retrofit2.Call<com.alfarooj.timetable.models.AttendanceLogsResponse> call, Throwable t) {
                        Toast.makeText(SuperAdminActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showHistoryList() {
        if (contentFrame.getChildCount() > 0) {
            contentFrame.removeAllViews();
        }
        
        View view = getLayoutInflater().inflate(R.layout.fragment_history_list, null);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        LogAdapter logAdapter = new LogAdapter(logList);
        recyclerView.setAdapter(logAdapter);
        
        contentFrame.addView(view);
    }
}
