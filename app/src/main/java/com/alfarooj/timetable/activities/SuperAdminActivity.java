package com.alfarooj.timetable.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
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
import com.alfarooj.timetable.models.AttendanceLog;
import com.alfarooj.timetable.models.CreateUserRequest;
import com.alfarooj.timetable.models.CreateUserResponse;
import com.alfarooj.timetable.models.DeleteUserResponse;
import com.alfarooj.timetable.models.User;
import com.alfarooj.timetable.models.UsersResponse;
import com.alfarooj.timetable.utils.SessionManager;
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
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showCreateUserDialog(String role) {
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

            // Show loading
            Toast.makeText(this, "Creating user...", Toast.LENGTH_SHORT).show();
            
            CreateUserRequest request = new CreateUserRequest(
                fullName, username, password, role, department, session.getUserId());
            
            ApiClient.getApiService().createUser(request)
                .enqueue(new Callback<CreateUserResponse>() {
                    @Override
                    public void onResponse(Call<CreateUserResponse> call, Response<CreateUserResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().isSuccess()) {
                                Toast.makeText(SuperAdminActivity.this, "User created successfully!", Toast.LENGTH_SHORT).show();
                                loadUsers();
                            } else {
                                Toast.makeText(SuperAdminActivity.this, "Error: " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(SuperAdminActivity.this, "Error: Server error", Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<CreateUserResponse> call, Throwable t) {
                        Toast.makeText(SuperAdminActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void loadUsers() {
        setTitle("Loading users...");
        
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
                        setTitle("Manage Users (" + userList.size() + " users)");
                    } else {
                        Toast.makeText(SuperAdminActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
                        setTitle("Manage Users - Error");
                    }
                }
                
                @Override
                public void onFailure(Call<UsersResponse> call, Throwable t) {
                    Toast.makeText(SuperAdminActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    setTitle("Manage Users - Network Error");
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
        setTitle("Today's Attendance");
        logList = new ArrayList<>();
        showHistoryList();
        Toast.makeText(this, "No attendance records yet", Toast.LENGTH_SHORT).show();
    }

    private void loadAllHistory() {
        setTitle("All History");
        logList = new ArrayList<>();
        showHistoryList();
        Toast.makeText(this, "No attendance records yet", Toast.LENGTH_SHORT).show();
    }

    private void loadHistoryByDepartment(String department) {
        String title = "";
        switch(department) {
            case "kitchen": title = "Kitchen History"; break;
            case "waiter": title = "Waiter History"; break;
            case "delivery": title = "Delivery History"; break;
            case "manager": title = "Manager History"; break;
        }
        setTitle(title);
        logList = new ArrayList<>();
        showHistoryList();
        Toast.makeText(this, "No " + title + " records yet", Toast.LENGTH_SHORT).show();
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
