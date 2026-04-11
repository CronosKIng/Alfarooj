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
import com.alfarooj.timetable.models.AttendanceLog;
import com.alfarooj.timetable.models.AttendanceLogsResponse;
import com.alfarooj.timetable.models.CreateUserRequest;
import com.alfarooj.timetable.models.CreateUserResponse;
import com.alfarooj.timetable.models.User;
import com.alfarooj.timetable.models.UsersResponse;
import com.alfarooj.timetable.utils.LanguageUtils;
import com.alfarooj.timetable.utils.SessionManager;
import com.alfarooj.timetable.utils.TranslationHelper;
import com.alfarooj.timetable.R;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SuperAdminActivity extends BaseActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FrameLayout contentFrame;
    private SessionManager session;
    private RecyclerView recyclerView;
    private ArrayList<User> userList;
    private ArrayList<AttendanceLog> logList;

    @Override
    protected void attachBaseContext(Context newBase) {
        LanguageUtils.applyLanguage(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_admin);

        try {
            session = new SessionManager(this);
            drawerLayout = findViewById(R.id.drawerLayout);
            navigationView = findViewById(R.id.navView);
            toolbar = findViewById(R.id.toolbar);
            contentFrame = findViewById(R.id.contentFrame);
            
            setSupportActionBar(toolbar);
            
            String title = TranslationHelper.translateTextDirect("Super Admin Dashboard");
            setTitle(title);

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
                    loadHistoryByDepartment(TranslationHelper.translateTextDirect("kitchen"));
                } else if (id == R.id.nav_waiter_history) {
                    loadHistoryByDepartment(TranslationHelper.translateTextDirect("waiter"));
                } else if (id == R.id.nav_delivery_history) {
                    loadHistoryByDepartment(TranslationHelper.translateTextDirect("delivery"));
                } else if (id == R.id.nav_manager_history) {
                    loadHistoryByDepartment(TranslationHelper.translateTextDirect("manager"));
                } else if (id == R.id.nav_create_admin) {
                    showCreateUserDialog(TranslationHelper.translateTextDirect("admin"));
                } else if (id == R.id.nav_create_user) {
                    showCreateUserDialog(TranslationHelper.translateTextDirect("user"));
                } else if (id == R.id.nav_users) {
                    loadUsers();
                } else if (id == R.id.nav_logout) {
                    session.logout();
                    startActivity(new Intent(SuperAdminActivity.this, LoginActivity.class));
                    finish();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });
            loadUsers();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, TranslationHelper.translateTextDirect("Error: ") + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String title = TranslationHelper.translateTextDirect("Super Admin Dashboard");
        setTitle(title);
        loadUsers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            item.setTitle(TranslationHelper.translateTextDirect(item.getTitle().toString()));
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

    private void showCreateUserDialog(String role) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String title = role.equals(TranslationHelper.translateTextDirect("admin")) ? TranslationHelper.translateTextDirect("Create Admin") : TranslationHelper.translateTextDirect("Create User");
        builder.setTitle(title);

        View view = getLayoutInflater().inflate(R.layout.dialog_create_user, null);
        EditText etFullName = view.findViewById(R.id.etFullName);
        EditText etUsername = view.findViewById(R.id.etUsername);
        EditText etPassword = view.findViewById(R.id.etPassword);
        Spinner spinnerDepartment = view.findViewById(R.id.spinnerDepartment);
        
        etFullName.setHint(TranslationHelper.translateTextDirect("Full Name"));
        etUsername.setHint(TranslationHelper.translateTextDirect("Username"));
        etPassword.setHint(TranslationHelper.translateTextDirect("Password"));
        
        String[] departments = {"kitchen", "waiter", "delivery", "manager"};
        String[] departmentNames = {
            TranslationHelper.translateTextDirect("Kitchen"),
            TranslationHelper.translateTextDirect("Waiter"),
            TranslationHelper.translateTextDirect("Delivery"),
            TranslationHelper.translateTextDirect("Manager")
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departmentNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartment.setAdapter(adapter);
        
        builder.setView(view);
        
        builder.setPositiveButton(TranslationHelper.translateTextDirect("Create"), (dialog, which) -> {
            createUserLogic(etFullName, etUsername, etPassword, spinnerDepartment, departments, role);
        });
        
        builder.setNegativeButton(TranslationHelper.translateTextDirect("Cancel"), null);
        builder.show();
    }
    
    private void createUserLogic(EditText etFullName, EditText etUsername, EditText etPassword, 
                                  Spinner spinnerDepartment, String[] departments, String role) {
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        int selectedPos = spinnerDepartment.getSelectedItemPosition();
        String department = departments[selectedPos];
        
        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(SuperAdminActivity.this, TranslationHelper.translateTextDirect("Please fill all fields"), Toast.LENGTH_SHORT).show();
            return;
        }
        
        CreateUserRequest request = new CreateUserRequest(
            fullName, username, password, role, department, session.getUserId());

        ApiClient.getApiService().createUser(request)
            .enqueue(new Callback<CreateUserResponse>() {
                @Override
                public void onResponse(Call<CreateUserResponse> call, Response<CreateUserResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(SuperAdminActivity.this, TranslationHelper.translateTextDirect("User created successfully!"), Toast.LENGTH_SHORT).show();
                        loadUsers();
                    } else {
                        Toast.makeText(SuperAdminActivity.this, TranslationHelper.translateTextDirect("Error: Username already exists"), Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<CreateUserResponse> call, Throwable t) {
                    Toast.makeText(SuperAdminActivity.this, TranslationHelper.translateTextDirect("Network error: ") + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void loadUsers() {
        setTitle(TranslationHelper.translateTextDirect("Loading users..."));
        
        ApiClient.getApiService().getUsers()
            .enqueue(new Callback<UsersResponse>() {
                @Override
                public void onResponse(Call<UsersResponse> call, Response<UsersResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        userList = new ArrayList<>();
                        List<User> apiUsers = response.body().getUsers();
                        for (User apiUser : apiUsers) {
                            userList.add(apiUser);
                        }
                        displayUsers();
                        setTitle(TranslationHelper.translateTextDirect("Manage Users (") + userList.size() + " " + TranslationHelper.translateTextDirect("users") + ")");
                    } else {
                        setTitle(TranslationHelper.translateTextDirect("Failed to load users"));
                    }
                }
                
                @Override
                public void onFailure(Call<UsersResponse> call, Throwable t) {
                    Toast.makeText(SuperAdminActivity.this, TranslationHelper.translateTextDirect("Network error: ") + t.getMessage(), Toast.LENGTH_LONG).show();
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
        setTitle(TranslationHelper.translateTextDirect("Today's Attendance"));
        
        ApiClient.getApiService().getTodayAttendance()
            .enqueue(new Callback<AttendanceLogsResponse>() {
                @Override
                public void onResponse(Call<AttendanceLogsResponse> call, Response<AttendanceLogsResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        logList = new ArrayList<>(response.body().getLogs());
                        if (logList.isEmpty()) {
                            Toast.makeText(SuperAdminActivity.this, TranslationHelper.translateTextDirect("No attendance records for today"), Toast.LENGTH_SHORT).show();
                        }
                        showHistoryList();
                    }
                }
                
                @Override
                public void onFailure(Call<AttendanceLogsResponse> call, Throwable t) {
                    Toast.makeText(SuperAdminActivity.this, TranslationHelper.translateTextDirect("Failed to load attendance"), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void loadAllHistory() {
        setTitle(TranslationHelper.translateTextDirect("All History"));
        
        ApiClient.getApiService().getAttendanceLogs(null)
            .enqueue(new Callback<AttendanceLogsResponse>() {
                @Override
                public void onResponse(Call<AttendanceLogsResponse> call, Response<AttendanceLogsResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        logList = new ArrayList<>(response.body().getLogs());
                        showHistoryList();
                    }
                }
                
                @Override
                public void onFailure(Call<AttendanceLogsResponse> call, Throwable t) {
                    Toast.makeText(SuperAdminActivity.this, TranslationHelper.translateTextDirect("Failed to load history"), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void loadHistoryByDepartment(String department) {
        String title = "";
        switch(department) {
            case "kitchen": title = TranslationHelper.translateTextDirect("Kitchen History"); break;
            case "waiter": title = TranslationHelper.translateTextDirect("Waiter History"); break;
            case "delivery": title = TranslationHelper.translateTextDirect("Delivery History"); break;
            case "manager": title = TranslationHelper.translateTextDirect("Manager History"); break;
        }
        setTitle(title);
        
        ApiClient.getApiService().getAttendanceLogs(department)
            .enqueue(new Callback<AttendanceLogsResponse>() {
                @Override
                public void onResponse(Call<AttendanceLogsResponse> call, Response<AttendanceLogsResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        logList = new ArrayList<>(response.body().getLogs());
                        if (logList.isEmpty()) {
                            Toast.makeText(SuperAdminActivity.this, TranslationHelper.translateTextDirect("No ") + title + " " + TranslationHelper.translateTextDirect("records found"), Toast.LENGTH_SHORT).show();
                        }
                        showHistoryList();
                    }
                }
                
                @Override
                public void onFailure(Call<AttendanceLogsResponse> call, Throwable t) {
                    Toast.makeText(SuperAdminActivity.this, TranslationHelper.translateTextDirect("Failed to load history"), Toast.LENGTH_SHORT).show();
                }
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
