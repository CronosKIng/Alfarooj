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
            
            // Set title directly
            String title = "Super Admin Dashboard";
            TranslationHelper.translateText(title, new TranslationHelper.TranslationCallback() {
                @Override
                public void onSuccess(String translatedText) {
                    setTitle(translatedText);
                }
                @Override
                public void onError(String error) {
                    setTitle(title);
                }
            });
            
            // Tafsiri navigation menu yote
            translateNavigationMenu();

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
                    startActivity(new Intent(SuperAdminActivity.this, LoginActivity.class));
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
    
    private void translateNavigationMenu() {
        // Tafsiri menu items zote
        Menu menu = navigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            String originalTitle = item.getTitle().toString();
            TranslationHelper.translateText(originalTitle, new TranslationHelper.TranslationCallback() {
                @Override
                public void onSuccess(String translatedText) {
                    item.setTitle(translatedText);
                }
                @Override
                public void onError(String error) {}
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh title
        String title = "Super Admin Dashboard";
        TranslationHelper.translateText(title, new TranslationHelper.TranslationCallback() {
            @Override
            public void onSuccess(String translatedText) {
                setTitle(translatedText);
            }
            @Override
            public void onError(String error) {
                setTitle(title);
            }
        });
        translateNavigationMenu();
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

    private void showCreateUserDialog(String role) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        String title = role.equals("admin") ? "Create Admin" : "Create User";
        TranslationHelper.translateText(title, new TranslationHelper.TranslationCallback() {
            @Override
            public void onSuccess(String translatedText) {
                builder.setTitle(translatedText);
            }
            @Override
            public void onError(String error) {
                builder.setTitle(title);
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
        String[] departmentNames = {"Kitchen", "Waiter", "Delivery", "Manager"};
        
        // Tafsiri department names
        for (int i = 0; i < departmentNames.length; i++) {
            final int index = i;
            TranslationHelper.translateText(departmentNames[i], new TranslationHelper.TranslationCallback() {
                @Override
                public void onSuccess(String translatedText) {
                    departmentNames[index] = translatedText;
                }
                @Override
                public void onError(String error) {}
            });
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departmentNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartment.setAdapter(adapter);
        
        builder.setView(view);
        
        // Tafsiri button texts
        TranslationHelper.translateText("Create", new TranslationHelper.TranslationCallback() {
            @Override
            public void onSuccess(String translatedText) {
                builder.setPositiveButton(translatedText, (dialog, which) -> {
                    createUserLogic(etFullName, etUsername, etPassword, spinnerDepartment, departments, role);
                });
            }
            @Override
            public void onError(String error) {
                builder.setPositiveButton("Create", (dialog, which) -> {
                    createUserLogic(etFullName, etUsername, etPassword, spinnerDepartment, departments, role);
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
    }
    
    private void createUserLogic(EditText etFullName, EditText etUsername, EditText etPassword, 
                                  Spinner spinnerDepartment, String[] departments, String role) {
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        int selectedPos = spinnerDepartment.getSelectedItemPosition();
        String department = departments[selectedPos];
        
        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            TranslationHelper.translateText("Please fill all fields", new TranslationHelper.TranslationCallback() {
                @Override
                public void onSuccess(String s) { Toast.makeText(SuperAdminActivity.this, s, Toast.LENGTH_SHORT).show(); }
                @Override
                public void onError(String e) { Toast.makeText(SuperAdminActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show(); }
            });
            return;
        }
        
        CreateUserRequest request = new CreateUserRequest(
            fullName, username, password, role, department, session.getUserId());

        ApiClient.getApiService().createUser(request)
            .enqueue(new Callback<CreateUserResponse>() {
                @Override
                public void onResponse(Call<CreateUserResponse> call, Response<CreateUserResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        TranslationHelper.translateText("User created successfully!", new TranslationHelper.TranslationCallback() {
                            @Override
                            public void onSuccess(String s) { Toast.makeText(SuperAdminActivity.this, s, Toast.LENGTH_SHORT).show(); }
                            @Override
                            public void onError(String e) { Toast.makeText(SuperAdminActivity.this, "User created successfully!", Toast.LENGTH_SHORT).show(); }
                        });
                        loadUsers();
                    } else {
                        TranslationHelper.translateText("Error: Username already exists", new TranslationHelper.TranslationCallback() {
                            @Override
                            public void onSuccess(String s) { Toast.makeText(SuperAdminActivity.this, s, Toast.LENGTH_SHORT).show(); }
                            @Override
                            public void onError(String e) { Toast.makeText(SuperAdminActivity.this, "Error: Username already exists", Toast.LENGTH_SHORT).show(); }
                        });
                    }
                }
                
                @Override
                public void onFailure(Call<CreateUserResponse> call, Throwable t) {
                    TranslationHelper.translateText("Network error: " + t.getMessage(), new TranslationHelper.TranslationCallback() {
                        @Override
                        public void onSuccess(String s) { Toast.makeText(SuperAdminActivity.this, s, Toast.LENGTH_SHORT).show(); }
                        @Override
                        public void onError(String e) { Toast.makeText(SuperAdminActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show(); }
                    });
                }
            });
    }
    
    private void loadUsers() {
        TranslationHelper.translateText("Loading users...", new TranslationHelper.TranslationCallback() {
            @Override
            public void onSuccess(String translatedText) {
                setTitle(translatedText);
            }
            @Override
            public void onError(String error) {
                setTitle("Loading users...");
            }
        });
        
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
                        TranslationHelper.translateText("👥 Manage Users (" + userList.size() + " users)", new TranslationHelper.TranslationCallback() {
                            @Override
                            public void onSuccess(String translatedText) {
                                setTitle(translatedText);
                            }
                            @Override
                            public void onError(String error) {
                                setTitle("👥 Manage Users (" + userList.size() + " users)");
                            }
                        });
                    } else {
                        TranslationHelper.translateText("Failed to load users", new TranslationHelper.TranslationCallback() {
                            @Override public void onSuccess(String s) { setTitle(s); }
                            @Override public void onError(String e) { setTitle("Failed to load users"); }
                        });
                    }
                }
                
                @Override
                public void onFailure(Call<UsersResponse> call, Throwable t) {
                    TranslationHelper.translateText("Network error: " + t.getMessage(), new TranslationHelper.TranslationCallback() {
                        @Override public void onSuccess(String s) { Toast.makeText(SuperAdminActivity.this, s, Toast.LENGTH_LONG).show(); }
                        @Override public void onError(String e) { Toast.makeText(SuperAdminActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show(); }
                    });
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
        TranslationHelper.translateText("Today's Attendance", new TranslationHelper.TranslationCallback() {
            @Override public void onSuccess(String s) { setTitle(s); }
            @Override public void onError(String e) { setTitle("Today's Attendance"); }
        });
        
        ApiClient.getApiService().getTodayAttendance()
            .enqueue(new Callback<AttendanceLogsResponse>() {
                @Override
                public void onResponse(Call<AttendanceLogsResponse> call, Response<AttendanceLogsResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        logList = new ArrayList<>(response.body().getLogs());
                        if (logList.isEmpty()) {
                            TranslationHelper.translateText("No attendance records for today", new TranslationHelper.TranslationCallback() {
                                @Override public void onSuccess(String s) { Toast.makeText(SuperAdminActivity.this, s, Toast.LENGTH_SHORT).show(); }
                                @Override public void onError(String e) { Toast.makeText(SuperAdminActivity.this, "No attendance records for today", Toast.LENGTH_SHORT).show(); }
                            });
                        }
                        showHistoryList();
                    }
                }
                
                @Override
                public void onFailure(Call<AttendanceLogsResponse> call, Throwable t) {
                    TranslationHelper.translateText("Failed to load attendance", new TranslationHelper.TranslationCallback() {
                        @Override public void onSuccess(String s) { Toast.makeText(SuperAdminActivity.this, s, Toast.LENGTH_SHORT).show(); }
                        @Override public void onError(String e) { Toast.makeText(SuperAdminActivity.this, "Failed to load attendance", Toast.LENGTH_SHORT).show(); }
                    });
                }
            });
    }

    private void loadAllHistory() {
        TranslationHelper.translateText("📖 📖 All History", new TranslationHelper.TranslationCallback() {
            @Override public void onSuccess(String s) { setTitle(s); }
            @Override public void onError(String e) { setTitle("📖 📖 All History"); }
        });
        
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
                    TranslationHelper.translateText("Failed to load history", new TranslationHelper.TranslationCallback() {
                        @Override public void onSuccess(String s) { Toast.makeText(SuperAdminActivity.this, s, Toast.LENGTH_SHORT).show(); }
                        @Override public void onError(String e) { Toast.makeText(SuperAdminActivity.this, "Failed to load history", Toast.LENGTH_SHORT).show(); }
                    });
                }
            });
    }

    private void loadHistoryByDepartment(String department) {
        String title = "";
        switch(department) {
            case "kitchen": title = "🧑‍🍳 Kitchen History"; break;
            case "waiter": title = "🍱 Waiter History"; break;
            case "delivery": title = "🚗 Delivery History"; break;
            case "manager": title = "💼 Manager History"; break;
        }
        final String finalTitle = title;
        
        TranslationHelper.translateText(finalTitle, new TranslationHelper.TranslationCallback() {
            @Override public void onSuccess(String s) { setTitle(s); }
            @Override public void onError(String e) { setTitle(finalTitle); }
        });
        
        ApiClient.getApiService().getAttendanceLogs(department)
            .enqueue(new Callback<AttendanceLogsResponse>() {
                @Override
                public void onResponse(Call<AttendanceLogsResponse> call, Response<AttendanceLogsResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        logList = new ArrayList<>(response.body().getLogs());
                        if (logList.isEmpty()) {
                            TranslationHelper.translateText("No " + finalTitle + " records found", new TranslationHelper.TranslationCallback() {
                                @Override public void onSuccess(String s) { Toast.makeText(SuperAdminActivity.this, s, Toast.LENGTH_SHORT).show(); }
                                @Override public void onError(String e) { Toast.makeText(SuperAdminActivity.this, "No " + finalTitle + " records found", Toast.LENGTH_SHORT).show(); }
                            });
                        }
                        showHistoryList();
                    }
                }
                
                @Override
                public void onFailure(Call<AttendanceLogsResponse> call, Throwable t) {
                    TranslationHelper.translateText("Failed to load history", new TranslationHelper.TranslationCallback() {
                        @Override public void onSuccess(String s) { Toast.makeText(SuperAdminActivity.this, s, Toast.LENGTH_SHORT).show(); }
                        @Override public void onError(String e) { Toast.makeText(SuperAdminActivity.this, "Failed to load history", Toast.LENGTH_SHORT).show(); }
                    });
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
