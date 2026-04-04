package com.alfarooj.timetable.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.TextView;
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
            translateNavigationMenu();
            translateTitle();
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        translateNavigationMenu();
        translateTitle();
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
    
    private void translateTitle() {
        String lang = TranslationHelper.getCurrentLanguage();
        if (lang.equals("en")) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Super Admin Dashboard");
            }
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
    }
    
    private void translateNavigationMenu() {
        Menu menu = navigationView.getMenu();
        String lang = TranslationHelper.getCurrentLanguage();
        
        String[] menuItems = {
            "Today's Attendance", "All History", "Kitchen History", 
            "Waiter History", "Delivery History", "Manager History",
            "Create Admin", "Create User", "Manage Users", "Logout"
        };
        
        if (lang.equals("en")) {
            for (int i = 0; i < menu.size() && i < menuItems.length; i++) {
                menu.getItem(i).setTitle(menuItems[i]);
            }
            return;
        }
        
        for (int i = 0; i < menu.size() && i < menuItems.length; i++) {
            final int index = i;
            TranslationHelper.translateText(menuItems[i], new TranslationHelper.TranslationCallback() {
                @Override
                public void onSuccess(String translated) {
                    menu.getItem(index).setTitle(translated);
                }
                @Override
                public void onError(String error) {}
            });
        }
    }

    private void showCreateUserDialog(String role) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_user, null);
        EditText etFullName = dialogView.findViewById(R.id.etFullName);
        EditText etUsername = dialogView.findViewById(R.id.etUsername);
        EditText etPassword = dialogView.findViewById(R.id.etPassword);
        Spinner spinnerDepartment = dialogView.findViewById(R.id.spinnerDepartment);
        
        TextView tvFullNameLabel = dialogView.findViewById(R.id.tvFullNameLabel);
        TextView tvUsernameLabel = dialogView.findViewById(R.id.tvUsernameLabel);
        TextView tvPasswordLabel = dialogView.findViewById(R.id.tvPasswordLabel);
        TextView tvDepartmentLabel = dialogView.findViewById(R.id.tvDepartmentLabel);

        String[] departments = {"kitchen", "waiter", "delivery", "manager"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departments);
        spinnerDepartment.setAdapter(adapter);

        String lang = TranslationHelper.getCurrentLanguage();
        String dialogTitle = role.equals("admin") ? "Create Admin" : "Create User";
        
        if (lang.equals("en")) {
            if (tvFullNameLabel != null) tvFullNameLabel.setText("Full Name");
            if (tvUsernameLabel != null) tvUsernameLabel.setText("Username");
            if (tvPasswordLabel != null) tvPasswordLabel.setText("Password");
            if (tvDepartmentLabel != null) tvDepartmentLabel.setText("Department");
            etFullName.setHint("Enter full name");
            etUsername.setHint("Enter username");
            etPassword.setHint("Enter password");
        } else {
            TranslationHelper.translateText("Full Name", new TranslationHelper.TranslationCallback() {
                @Override public void onSuccess(String translated) { if (tvFullNameLabel != null) tvFullNameLabel.setText(translated); }
                @Override public void onError(String error) {}
            });
            TranslationHelper.translateText("Username", new TranslationHelper.TranslationCallback() {
                @Override public void onSuccess(String translated) { if (tvUsernameLabel != null) tvUsernameLabel.setText(translated); }
                @Override public void onError(String error) {}
            });
            TranslationHelper.translateText("Password", new TranslationHelper.TranslationCallback() {
                @Override public void onSuccess(String translated) { if (tvPasswordLabel != null) tvPasswordLabel.setText(translated); }
                @Override public void onError(String error) {}
            });
            TranslationHelper.translateText("Department", new TranslationHelper.TranslationCallback() {
                @Override public void onSuccess(String translated) { if (tvDepartmentLabel != null) tvDepartmentLabel.setText(translated); }
                @Override public void onError(String error) {}
            });
            TranslationHelper.translateText("Enter full name", new TranslationHelper.TranslationCallback() {
                @Override public void onSuccess(String translated) { etFullName.setHint(translated); }
                @Override public void onError(String error) {}
            });
            TranslationHelper.translateText("Enter username", new TranslationHelper.TranslationCallback() {
                @Override public void onSuccess(String translated) { etUsername.setHint(translated); }
                @Override public void onError(String error) {}
            });
            TranslationHelper.translateText("Enter password", new TranslationHelper.TranslationCallback() {
                @Override public void onSuccess(String translated) { etPassword.setHint(translated); }
                @Override public void onError(String error) {}
            });
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        if (lang.equals("en")) {
            builder.setTitle(dialogTitle);
        } else {
            TranslationHelper.translateText(dialogTitle, new TranslationHelper.TranslationCallback() {
                @Override public void onSuccess(String translated) { builder.setTitle(translated); }
                @Override public void onError(String error) { builder.setTitle(dialogTitle); }
            });
        }
        
        builder.setView(dialogView);
        
        if (lang.equals("en")) {
            builder.setPositiveButton("Create", null);
        } else {
            TranslationHelper.translateText("Create", new TranslationHelper.TranslationCallback() {
                @Override public void onSuccess(String translated) { builder.setPositiveButton(translated, null); }
                @Override public void onError(String error) { builder.setPositiveButton("Create", null); }
            });
        }
        
        if (lang.equals("en")) {
            builder.setNegativeButton("Cancel", null);
        } else {
            TranslationHelper.translateText("Cancel", new TranslationHelper.TranslationCallback() {
                @Override public void onSuccess(String translated) { builder.setNegativeButton(translated, null); }
                @Override public void onError(String error) { builder.setNegativeButton("Cancel", null); }
            });
        }
        
        AlertDialog dialog = builder.create();
        
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String fullName = etFullName.getText().toString().trim();
                        String username = etUsername.getText().toString().trim();
                        String password = etPassword.getText().toString().trim();
                        String department = spinnerDepartment.getSelectedItem().toString();

                        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
                            String errorMsg = "Please fill all fields";
                            String currentLang = TranslationHelper.getCurrentLanguage();
                            if (currentLang.equals("en")) {
                                Toast.makeText(SuperAdminActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                            } else {
                                TranslationHelper.translateText(errorMsg, new TranslationHelper.TranslationCallback() {
                                    @Override public void onSuccess(String translated) { Toast.makeText(SuperAdminActivity.this, translated, Toast.LENGTH_SHORT).show(); }
                                    @Override public void onError(String error) { Toast.makeText(SuperAdminActivity.this, errorMsg, Toast.LENGTH_SHORT).show(); }
                                });
                            }
                            return;
                        }

                        com.alfarooj.timetable.models.CreateUserRequest request = 
                            new com.alfarooj.timetable.models.CreateUserRequest(
                                fullName, username, password, role, department, session.getUserId());
                        
                        ApiClient.getApiService().createUser(request)
                            .enqueue(new retrofit2.Callback<com.alfarooj.timetable.models.CreateUserResponse>() {
                                @Override
                                public void onResponse(retrofit2.Call<com.alfarooj.timetable.models.CreateUserResponse> call,
                                                       retrofit2.Response<com.alfarooj.timetable.models.CreateUserResponse> response) {
                                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                        String successMsg = "User created successfully!";
                                        String currentLang = TranslationHelper.getCurrentLanguage();
                                        if (currentLang.equals("en")) {
                                            Toast.makeText(SuperAdminActivity.this, successMsg, Toast.LENGTH_SHORT).show();
                                        } else {
                                            TranslationHelper.translateText(successMsg, new TranslationHelper.TranslationCallback() {
                                                @Override public void onSuccess(String translated) { Toast.makeText(SuperAdminActivity.this, translated, Toast.LENGTH_SHORT).show(); }
                                                @Override public void onError(String error) { Toast.makeText(SuperAdminActivity.this, successMsg, Toast.LENGTH_SHORT).show(); }
                                            });
                                        }
                                        loadUsers();
                                        dialog.dismiss();
                                    } else {
                                        String errorMsg = "Error: Username already exists";
                                        String currentLang = TranslationHelper.getCurrentLanguage();
                                        if (currentLang.equals("en")) {
                                            Toast.makeText(SuperAdminActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                                        } else {
                                            TranslationHelper.translateText(errorMsg, new TranslationHelper.TranslationCallback() {
                                                @Override public void onSuccess(String translated) { Toast.makeText(SuperAdminActivity.this, translated, Toast.LENGTH_SHORT).show(); }
                                                @Override public void onError(String error) { Toast.makeText(SuperAdminActivity.this, errorMsg, Toast.LENGTH_SHORT).show(); }
                                            });
                                        }
                                    }
                                }
                                
                                @Override
                                public void onFailure(retrofit2.Call<com.alfarooj.timetable.models.CreateUserResponse> call, Throwable t) {
                                    String errorMsg = "Network error: " + t.getMessage();
                                    String currentLang = TranslationHelper.getCurrentLanguage();
                                    if (currentLang.equals("en")) {
                                        Toast.makeText(SuperAdminActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                                    } else {
                                        TranslationHelper.translateText(errorMsg, new TranslationHelper.TranslationCallback() {
                                            @Override public void onSuccess(String translated) { Toast.makeText(SuperAdminActivity.this, translated, Toast.LENGTH_SHORT).show(); }
                                            @Override public void onError(String error) { Toast.makeText(SuperAdminActivity.this, errorMsg, Toast.LENGTH_SHORT).show(); }
                                        });
                                    }
                                }
                            });
                    }
                });
            }
        });
        
        dialog.show();
    }

    private void loadUsers() {
        ApiClient.getApiService().getUsers()
            .enqueue(new retrofit2.Callback<com.alfarooj.timetable.models.UsersResponse>() {
                @Override
                public void onResponse(retrofit2.Call<com.alfarooj.timetable.models.UsersResponse> call,
                                       retrofit2.Response<com.alfarooj.timetable.models.UsersResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        userList = new ArrayList<>();
                        for (com.alfarooj.timetable.models.User apiUser : response.body().getUsers()) {
                            User localUser = new User(
                                apiUser.getId(), apiUser.getFullName(), apiUser.getUsername(),
                                "", apiUser.getRole(), apiUser.getDepartment(), 0, ""
                            );
                            userList.add(localUser);
                        }
                        displayUsers();
                    } else {
                        String errorMsg = "Failed to load users";
                        String currentLang = TranslationHelper.getCurrentLanguage();
                        if (currentLang.equals("en")) {
                            Toast.makeText(SuperAdminActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        } else {
                            TranslationHelper.translateText(errorMsg, new TranslationHelper.TranslationCallback() {
                                @Override public void onSuccess(String translated) { Toast.makeText(SuperAdminActivity.this, translated, Toast.LENGTH_SHORT).show(); }
                                @Override public void onError(String error) { Toast.makeText(SuperAdminActivity.this, errorMsg, Toast.LENGTH_SHORT).show(); }
                            });
                        }
                    }
                }
                @Override
                public void onFailure(retrofit2.Call<com.alfarooj.timetable.models.UsersResponse> call, Throwable t) {
                    String errorMsg = "Network error: " + t.getMessage();
                    Toast.makeText(SuperAdminActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            });
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
        ApiClient.getApiService().getTodayAttendance()
            .enqueue(new retrofit2.Callback<com.alfarooj.timetable.models.AttendanceLogsResponse>() {
                @Override
                public void onResponse(retrofit2.Call<com.alfarooj.timetable.models.AttendanceLogsResponse> call,
                                       retrofit2.Response<com.alfarooj.timetable.models.AttendanceLogsResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        logList = new ArrayList<>();
                        for (com.alfarooj.timetable.models.AttendanceLog apiLog : response.body().getLogs()) {
                            logList.add(new AttendanceLog(apiLog.getId(), apiLog.getUserId(), apiLog.getUsername(),
                                apiLog.getFullName(), apiLog.getDepartment(), apiLog.getEventType(),
                                apiLog.getEventName(), apiLog.getLocation(), apiLog.getLatitude(),
                                apiLog.getLongitude(), apiLog.getTimestamp()));
                        }
                        showHistoryList();
                        translateHistoryTitle("Today's Attendance");
                    } else {
                        String errorMsg = "No attendance records for today";
                        String currentLang = TranslationHelper.getCurrentLanguage();
                        if (currentLang.equals("en")) {
                            Toast.makeText(SuperAdminActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        } else {
                            TranslationHelper.translateText(errorMsg, new TranslationHelper.TranslationCallback() {
                                @Override public void onSuccess(String translated) { Toast.makeText(SuperAdminActivity.this, translated, Toast.LENGTH_SHORT).show(); }
                                @Override public void onError(String error) { Toast.makeText(SuperAdminActivity.this, errorMsg, Toast.LENGTH_SHORT).show(); }
                            });
                        }
                    }
                }
                @Override
                public void onFailure(retrofit2.Call<com.alfarooj.timetable.models.AttendanceLogsResponse> call, Throwable t) {}
            });
    }

    private void loadAllHistory() {
        ApiClient.getApiService().getAttendanceLogs(null)
            .enqueue(new retrofit2.Callback<com.alfarooj.timetable.models.AttendanceLogsResponse>() {
                @Override
                public void onResponse(retrofit2.Call<com.alfarooj.timetable.models.AttendanceLogsResponse> call,
                                       retrofit2.Response<com.alfarooj.timetable.models.AttendanceLogsResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        logList = new ArrayList<>();
                        for (com.alfarooj.timetable.models.AttendanceLog apiLog : response.body().getLogs()) {
                            logList.add(new AttendanceLog(apiLog.getId(), apiLog.getUserId(), apiLog.getUsername(),
                                apiLog.getFullName(), apiLog.getDepartment(), apiLog.getEventType(),
                                apiLog.getEventName(), apiLog.getLocation(), apiLog.getLatitude(),
                                apiLog.getLongitude(), apiLog.getTimestamp()));
                        }
                        showHistoryList();
                        translateHistoryTitle("All History");
                    }
                }
                @Override
                public void onFailure(retrofit2.Call<com.alfarooj.timetable.models.AttendanceLogsResponse> call, Throwable t) {}
            });
    }

    private void loadHistoryByDepartment(String department) {
        ApiClient.getApiService().getAttendanceLogs(department)
            .enqueue(new retrofit2.Callback<com.alfarooj.timetable.models.AttendanceLogsResponse>() {
                @Override
                public void onResponse(retrofit2.Call<com.alfarooj.timetable.models.AttendanceLogsResponse> call,
                                       retrofit2.Response<com.alfarooj.timetable.models.AttendanceLogsResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        logList = new ArrayList<>();
                        for (com.alfarooj.timetable.models.AttendanceLog apiLog : response.body().getLogs()) {
                            logList.add(new AttendanceLog(apiLog.getId(), apiLog.getUserId(), apiLog.getUsername(),
                                apiLog.getFullName(), apiLog.getDepartment(), apiLog.getEventType(),
                                apiLog.getEventName(), apiLog.getLocation(), apiLog.getLatitude(),
                                apiLog.getLongitude(), apiLog.getTimestamp()));
                        }
                        showHistoryList();
                        String title = department.substring(0, 1).toUpperCase() + department.substring(1) + " History";
                        translateHistoryTitle(title);
                    }
                }
                @Override
                public void onFailure(retrofit2.Call<com.alfarooj.timetable.models.AttendanceLogsResponse> call, Throwable t) {}
            });
    }
    
    private void translateHistoryTitle(String title) {
        String lang = TranslationHelper.getCurrentLanguage();
        if (lang.equals("en")) {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle(title);
            return;
        }
        TranslationHelper.translateText(title, new TranslationHelper.TranslationCallback() {
            @Override
            public void onSuccess(String translated) {
                if (getSupportActionBar() != null) getSupportActionBar().setTitle(translated);
            }
            @Override
            public void onError(String error) {}
        });
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
