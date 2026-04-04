package com.alfarooj.timetable.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.alfarooj.timetable.adapters.UserAdapter;
import com.alfarooj.timetable.api.ApiClient;
import com.alfarooj.timetable.database.DatabaseHelper;
import com.alfarooj.timetable.models.User;
import com.alfarooj.timetable.utils.SessionManager;
import com.alfarooj.timetable.utils.TranslationHelper;
import com.alfarooj.timetable.R;
import java.util.ArrayList;

public class AdminActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private Button btnCreateUser, btnViewLogs, btnLogout;
    private TextView tvTitle;
    private DatabaseHelper db;
    private SessionManager session;
    private ArrayList<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        recyclerView = findViewById(R.id.recyclerView);
        btnCreateUser = findViewById(R.id.btnCreateUser);
        btnViewLogs = findViewById(R.id.btnViewLogs);
        btnLogout = findViewById(R.id.btnLogout);
        tvTitle = findViewById(R.id.tvTitle);

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
        translateUI();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        translateUI();
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
    
    private void translateUI() {
        String lang = TranslationHelper.getCurrentLanguage();
        
        if (lang.equals("en")) {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Admin Dashboard");
            tvTitle.setText("ADMIN DASHBOARD");
            btnCreateUser.setText("CREATE USER");
            btnViewLogs.setText("VIEW HISTORY");
            btnLogout.setText("LOGOUT");
        } else {
            TranslationHelper.translateText("Admin Dashboard", new TranslationHelper.TranslationCallback() {
                @Override public void onSuccess(String translated) {
                    if (getSupportActionBar() != null) getSupportActionBar().setTitle(translated);
                }
                @Override public void onError(String error) {}
            });
            TranslationHelper.translateText("ADMIN DASHBOARD", new TranslationHelper.TranslationCallback() {
                @Override public void onSuccess(String translated) { tvTitle.setText(translated); }
                @Override public void onError(String error) {}
            });
            TranslationHelper.translateText("CREATE USER", new TranslationHelper.TranslationCallback() {
                @Override public void onSuccess(String translated) { btnCreateUser.setText(translated); }
                @Override public void onError(String error) {}
            });
            TranslationHelper.translateText("VIEW HISTORY", new TranslationHelper.TranslationCallback() {
                @Override public void onSuccess(String translated) { btnViewLogs.setText(translated); }
                @Override public void onError(String error) {}
            });
            TranslationHelper.translateText("LOGOUT", new TranslationHelper.TranslationCallback() {
                @Override public void onSuccess(String translated) { btnLogout.setText(translated); }
                @Override public void onError(String error) {}
            });
        }
        
        TextView tvUsersList = findViewById(R.id.tvUsersList);
        if (tvUsersList != null) {
            if (lang.equals("en")) {
                tvUsersList.setText("USERS LIST");
            } else {
                TranslationHelper.translateText("USERS LIST", new TranslationHelper.TranslationCallback() {
                    @Override public void onSuccess(String translated) { tvUsersList.setText(translated); }
                    @Override public void onError(String error) {}
                });
            }
        }
    }

    private void showCreateUserDialog() {
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
            builder.setTitle("Create User");
        } else {
            TranslationHelper.translateText("Create User", new TranslationHelper.TranslationCallback() {
                @Override public void onSuccess(String translated) { builder.setTitle(translated); }
                @Override public void onError(String error) { builder.setTitle("Create User"); }
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
                                Toast.makeText(AdminActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                            } else {
                                TranslationHelper.translateText(errorMsg, new TranslationHelper.TranslationCallback() {
                                    @Override public void onSuccess(String translated) { Toast.makeText(AdminActivity.this, translated, Toast.LENGTH_SHORT).show(); }
                                    @Override public void onError(String error) { Toast.makeText(AdminActivity.this, errorMsg, Toast.LENGTH_SHORT).show(); }
                                });
                            }
                            return;
                        }

                        com.alfarooj.timetable.models.CreateUserRequest request = 
                            new com.alfarooj.timetable.models.CreateUserRequest(
                                fullName, username, password, "user", department, session.getUserId());
                        
                        ApiClient.getApiService().createUser(request)
                            .enqueue(new retrofit2.Callback<com.alfarooj.timetable.models.CreateUserResponse>() {
                                @Override
                                public void onResponse(retrofit2.Call<com.alfarooj.timetable.models.CreateUserResponse> call,
                                                       retrofit2.Response<com.alfarooj.timetable.models.CreateUserResponse> response) {
                                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                        String successMsg = "User created successfully!";
                                        String currentLang = TranslationHelper.getCurrentLanguage();
                                        if (currentLang.equals("en")) {
                                            Toast.makeText(AdminActivity.this, successMsg, Toast.LENGTH_SHORT).show();
                                        } else {
                                            TranslationHelper.translateText(successMsg, new TranslationHelper.TranslationCallback() {
                                                @Override public void onSuccess(String translated) { Toast.makeText(AdminActivity.this, translated, Toast.LENGTH_SHORT).show(); }
                                                @Override public void onError(String error) { Toast.makeText(AdminActivity.this, successMsg, Toast.LENGTH_SHORT).show(); }
                                            });
                                        }
                                        loadUsers();
                                        dialog.dismiss();
                                    } else {
                                        String errorMsg = "Error: Username already exists";
                                        String currentLang = TranslationHelper.getCurrentLanguage();
                                        if (currentLang.equals("en")) {
                                            Toast.makeText(AdminActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                                        } else {
                                            TranslationHelper.translateText(errorMsg, new TranslationHelper.TranslationCallback() {
                                                @Override public void onSuccess(String translated) { Toast.makeText(AdminActivity.this, translated, Toast.LENGTH_SHORT).show(); }
                                                @Override public void onError(String error) { Toast.makeText(AdminActivity.this, errorMsg, Toast.LENGTH_SHORT).show(); }
                                            });
                                        }
                                    }
                                }
                                
                                @Override
                                public void onFailure(retrofit2.Call<com.alfarooj.timetable.models.CreateUserResponse> call, Throwable t) {
                                    String errorMsg = "Network error: " + t.getMessage();
                                    String currentLang = TranslationHelper.getCurrentLanguage();
                                    if (currentLang.equals("en")) {
                                        Toast.makeText(AdminActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                                    } else {
                                        TranslationHelper.translateText(errorMsg, new TranslationHelper.TranslationCallback() {
                                            @Override public void onSuccess(String translated) { Toast.makeText(AdminActivity.this, translated, Toast.LENGTH_SHORT).show(); }
                                            @Override public void onError(String error) { Toast.makeText(AdminActivity.this, errorMsg, Toast.LENGTH_SHORT).show(); }
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
                            Toast.makeText(AdminActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        } else {
                            TranslationHelper.translateText(errorMsg, new TranslationHelper.TranslationCallback() {
                                @Override public void onSuccess(String translated) { Toast.makeText(AdminActivity.this, translated, Toast.LENGTH_SHORT).show(); }
                                @Override public void onError(String error) { Toast.makeText(AdminActivity.this, errorMsg, Toast.LENGTH_SHORT).show(); }
                            });
                        }
                    }
                }
                @Override
                public void onFailure(retrofit2.Call<com.alfarooj.timetable.models.UsersResponse> call, Throwable t) {
                    String errorMsg = "Network error: " + t.getMessage();
                    Toast.makeText(AdminActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void displayUsers() {
        UserAdapter adapter = new UserAdapter(userList, this, () -> loadUsers());
        recyclerView.setAdapter(adapter);
    }
}
