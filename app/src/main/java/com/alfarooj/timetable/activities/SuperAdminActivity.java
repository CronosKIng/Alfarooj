package com.alfarooj.timetable.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import com.alfarooj.timetable.R;
import com.alfarooj.timetable.adapters.LogAdapter;
import com.alfarooj.timetable.adapters.UserAdapter;
import com.alfarooj.timetable.api.ApiClient;
import com.alfarooj.timetable.models.AttendanceLog;
import com.alfarooj.timetable.models.CreateUserRequest;
import com.alfarooj.timetable.models.User;
import com.alfarooj.timetable.utils.SessionManager;
import com.alfarooj.timetable.utils.TranslationHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class SuperAdminActivity extends BaseActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private SessionManager session;
    private ArrayList<User> userList = new ArrayList<>();
    private ArrayList<AttendanceLog> logList = new ArrayList<>();
    private UserAdapter userAdapter;
    private LogAdapter logAdapter;
    private String currentView = "users";
    private String currentDepartment = null;
    private Calendar selectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_admin);

        session = new SessionManager(this);
        
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        // tvEmpty = findViewById(R.id.tvEmpty);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setupNavigationView();
        loadUsers();

        translateAllUIElements();
        translateToolbar(toolbar);
        translateNavigationView(navigationView);
    }

    private void setupNavigationView() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            currentDepartment = null;
            
            if (id == R.id.nav_users) {
                currentView = "users";
                toolbar.setTitle(TranslationHelper.translateTextDirect("Manage Users"));
                loadUsers();
            } else if (id == R.id.nav_today_attendance) {
                currentView = "attendance";
                toolbar.setTitle(TranslationHelper.translateTextDirect("Today Attendance"));
                loadTodayAttendance();
            } else if (id == R.id.nav_all_history) {
                currentView = "attendance";
                toolbar.setTitle(TranslationHelper.translateTextDirect("All History"));
                loadAttendanceLogs(null);
            } else if (id == R.id.nav_kitchen_history) {
                currentView = "attendance";
                currentDepartment = "kitchen";
                toolbar.setTitle(TranslationHelper.translateTextDirect("Kitchen History"));
                loadAttendanceLogs("kitchen");
            } else if (id == R.id.nav_waiter_history) {
                currentView = "attendance";
                currentDepartment = "waiter";
                toolbar.setTitle(TranslationHelper.translateTextDirect("Waiter History"));
                loadAttendanceLogs("waiter");
            } else if (id == R.id.nav_delivery_history) {
                currentView = "attendance";
                currentDepartment = "delivery";
                toolbar.setTitle(TranslationHelper.translateTextDirect("Delivery History"));
                loadAttendanceLogs("delivery");
            } else if (id == R.id.nav_manager_history) {
                currentView = "attendance";
                currentDepartment = "manager";
                toolbar.setTitle(TranslationHelper.translateTextDirect("Manager History"));
                loadAttendanceLogs("manager");
            } else if (id == R.id.nav_create_user) {
                showCreateUserDialog("user");
            } else if (id == R.id.nav_create_admin) {
                showCreateUserDialog("admin");
            } else if (id == R.id.nav_logout) {
                logout();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void loadUsers() {
        tvEmpty != null ? tvEmpty.setVisibility : null;(View.GONE);
        ApiClient.getApiService().getUsers().enqueue(new Callback<com.alfarooj.timetable.models.UsersResponse>() {
            @Override
            public void onResponse(Call<com.alfarooj.timetable.models.UsersResponse> call, 
                                   Response<com.alfarooj.timetable.models.UsersResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    userList.clear();
                    userList.addAll(response.body().getUsers());
                    userAdapter = new UserAdapter(userList, SuperAdminActivity.this, () -> loadUsers());
                    recyclerView.setAdapter(userAdapter);
                    tvEmpty != null ? tvEmpty.setVisibility : null;(userList.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<com.alfarooj.timetable.models.UsersResponse> call, Throwable t) {
                Toast.makeText(SuperAdminActivity.this, 
                    TranslationHelper.translateTextDirect("📡 Hakuna mtandao"), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTodayAttendance() {
        tvEmpty != null ? tvEmpty.setVisibility : null;(View.GONE);
        ApiClient.getApiService().getTodayAttendance().enqueue(new Callback<com.alfarooj.timetable.models.AttendanceLogsResponse>() {
            @Override
            public void onResponse(Call<com.alfarooj.timetable.models.AttendanceLogsResponse> call,
                                   Response<com.alfarooj.timetable.models.AttendanceLogsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    logList.clear();
                    logList.addAll(response.body().getLogs());
                    logAdapter = new LogAdapter(logList, SuperAdminActivity.this, 
                        log -> showDeleteLogDialog(log));
                    recyclerView.setAdapter(logAdapter);
                    tvEmpty != null ? tvEmpty.setVisibility : null;(logList.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<com.alfarooj.timetable.models.AttendanceLogsResponse> call, Throwable t) {
                Toast.makeText(SuperAdminActivity.this, 
                    TranslationHelper.translateTextDirect("📡 Hakuna mtandao"), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAttendanceLogs(String department) {
        tvEmpty != null ? tvEmpty.setVisibility : null;(View.GONE);
        
        Call<com.alfarooj.timetable.models.AttendanceLogsResponse> call;
        if (department != null) {
            call = ApiClient.getApiService().getAttendanceLogs(department);
        } else {
            call = ApiClient.getApiService().getAttendanceLogs(null);
        }

        call.enqueue(new Callback<com.alfarooj.timetable.models.AttendanceLogsResponse>() {
            @Override
            public void onResponse(Call<com.alfarooj.timetable.models.AttendanceLogsResponse> call,
                                   Response<com.alfarooj.timetable.models.AttendanceLogsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    logList.clear();
                    logList.addAll(response.body().getLogs());
                    logAdapter = new LogAdapter(logList, SuperAdminActivity.this,
                        log -> showDeleteLogDialog(log));
                    recyclerView.setAdapter(logAdapter);
                    tvEmpty != null ? tvEmpty.setVisibility : null;(logList.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<com.alfarooj.timetable.models.AttendanceLogsResponse> call, Throwable t) {
                Toast.makeText(SuperAdminActivity.this, 
                    TranslationHelper.translateTextDirect("📡 Hakuna mtandao"), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAttendanceByDate(String date) {
        tvEmpty != null ? tvEmpty.setVisibility : null;(View.GONE);
        ApiClient.getApiService().getAttendanceByDate(date).enqueue(new Callback<com.alfarooj.timetable.models.AttendanceLogsResponse>() {
            @Override
            public void onResponse(Call<com.alfarooj.timetable.models.AttendanceLogsResponse> call,
                                   Response<com.alfarooj.timetable.models.AttendanceLogsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    logList.clear();
                    logList.addAll(response.body().getLogs());
                    logAdapter = new LogAdapter(logList, SuperAdminActivity.this,
                        log -> showDeleteLogDialog(log));
                    recyclerView.setAdapter(logAdapter);
                    tvEmpty != null ? tvEmpty.setVisibility : null;(logList.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<com.alfarooj.timetable.models.AttendanceLogsResponse> call, Throwable t) {
                Toast.makeText(SuperAdminActivity.this, 
                    TranslationHelper.translateTextDirect("📡 Hakuna mtandao"), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteLogDialog(AttendanceLog log) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(TranslationHelper.translateTextDirect("Delete Record"));
        builder.setMessage(TranslationHelper.translateTextDirect("Delete this attendance record?"));
        builder.setPositiveButton(TranslationHelper.translateTextDirect("Delete"), (dialog, which) -> {
            deleteAttendanceLog(log.getId());
        });
        builder.setNegativeButton(TranslationHelper.translateTextDirect("Cancel"), null);
        builder.show();
    }

    private void deleteAttendanceLog(int logId) {
        ApiClient.getApiService().deleteAttendanceLog(logId).enqueue(new Callback<com.alfarooj.timetable.models.SimpleResponse>() {
            @Override
            public void onResponse(Call<com.alfarooj.timetable.models.SimpleResponse> call,
                                   Response<com.alfarooj.timetable.models.SimpleResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(SuperAdminActivity.this, 
                        TranslationHelper.translateTextDirect("✅ Imefutwa"), 
                        Toast.LENGTH_SHORT).show();
                    if (currentView.equals("attendance")) {
                        if (currentDepartment != null) {
                            loadAttendanceLogs(currentDepartment);
                        } else {
                            loadAttendanceLogs(null);
                        }
                    }
                } else {
                    Toast.makeText(SuperAdminActivity.this, 
                        TranslationHelper.translateTextDirect("❌ Imeshindwa"), 
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.alfarooj.timetable.models.SimpleResponse> call, Throwable t) {
                Toast.makeText(SuperAdminActivity.this, 
                    TranslationHelper.translateTextDirect("📡 Hakuna mtandao"), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(this,
            (view, year, month, dayOfMonth) -> {
                selectedDate.set(year, month, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String dateStr = sdf.format(selectedDate.getTime());
                toolbar.setTitle(TranslationHelper.translateTextDirect("Attendance for ") + dateStr);
                loadAttendanceByDate(dateStr);
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void showCreateUserDialog(String role) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(TranslationHelper.translateTextDirect(role.equals("admin") ? "Create Admin" : "Create User"));

        View view = getLayoutInflater().inflate(R.layout.dialog_create_user, null);
        EditText etFullName = view.findViewById(R.id.etFullName);
        EditText etUsername = view.findViewById(R.id.etUsername);
        EditText etPassword = view.findViewById(R.id.etPassword);
        Spinner spinnerDepartment = view.findViewById(R.id.spinnerDepartment);

        etFullName.setHint(TranslationHelper.translateTextDirect("Full Name"));
        etUsername.setHint(TranslationHelper.translateTextDirect("Username"));
        etPassword.setHint(TranslationHelper.translateTextDirect("Password"));

        String[] departments = {"kitchen", "waiter", "delivery", "manager"};
        String[] deptNames = {
            TranslationHelper.translateTextDirect("Kitchen"),
            TranslationHelper.translateTextDirect("Waiter"),
            TranslationHelper.translateTextDirect("Delivery"),
            TranslationHelper.translateTextDirect("Manager")
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, deptNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartment.setAdapter(adapter);

        builder.setView(view);
        builder.setPositiveButton(TranslationHelper.translateTextDirect("Create"), (dialog, which) -> {
            String fullName = etFullName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            int pos = spinnerDepartment.getSelectedItemPosition();
            String department = departments[pos];

            if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, 
                    TranslationHelper.translateTextDirect("Please fill all fields"), 
                    Toast.LENGTH_SHORT).show();
                return;
            }

            CreateUserRequest request = new CreateUserRequest(
                fullName, username, password, role, department, session.getUserId());
            ApiClient.getApiService().createUser(request).enqueue(new Callback<com.alfarooj.timetable.models.CreateUserResponse>() {
                @Override
                public void onResponse(Call<com.alfarooj.timetable.models.CreateUserResponse> call,
                                       Response<com.alfarooj.timetable.models.CreateUserResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(SuperAdminActivity.this, 
                            TranslationHelper.translateTextDirect("✅ User created"), 
                            Toast.LENGTH_SHORT).show();
                        if (currentView.equals("users")) loadUsers();
                    } else {
                        Toast.makeText(SuperAdminActivity.this, 
                            TranslationHelper.translateTextDirect("❌ Failed"), 
                            Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<com.alfarooj.timetable.models.CreateUserResponse> call, Throwable t) {
                    Toast.makeText(SuperAdminActivity.this, 
                        TranslationHelper.translateTextDirect("📡 Hakuna mtandao"), 
                        Toast.LENGTH_SHORT).show();
                }
            });
        });
        builder.setNegativeButton(TranslationHelper.translateTextDirect("Cancel"), null);
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        TranslationHelper.translateMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_language) {
            showLanguageDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        session.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
