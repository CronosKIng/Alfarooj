package com.alfarooj.timetable.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.alfarooj.timetable.api.ApiClient;
import com.alfarooj.timetable.models.LoginRequest;
import com.alfarooj.timetable.models.LoginResponse;
import com.alfarooj.timetable.models.User;
import com.alfarooj.timetable.utils.SessionManager;
import com.alfarooj.timetable.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ImageButton btnTogglePassword;
    private TextView tvError;
    private ImageView ivLogo;
    private SessionManager session;
    private boolean isPasswordVisible = false;
    private static final int LOCATION_PERMISSION_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = new SessionManager(this);

        if (session.isLoggedIn()) {
            navigateToDashboard();
            return;
        }

        ivLogo = findViewById(R.id.ivLogo);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        tvError = findViewById(R.id.tvError);

        btnTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnTogglePassword.setImageResource(R.drawable.ic_eye);
                isPasswordVisible = false;
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnTogglePassword.setImageResource(R.drawable.ic_eye_off);
                isPasswordVisible = true;
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        }

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            tvError.setText("");
            tvError.setVisibility(View.GONE);
            
            if (username.isEmpty()) {
                tvError.setText("Please enter username");
                tvError.setVisibility(View.VISIBLE);
                etUsername.requestFocus();
                return;
            }
            
            if (password.isEmpty()) {
                tvError.setText("Please enter password");
                tvError.setVisibility(View.VISIBLE);
                etPassword.requestFocus();
                return;
            }

            btnLogin.setText("LOGGING IN...");
            btnLogin.setEnabled(false);

            ApiClient.getApiService().login(new LoginRequest(username, password))
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        btnLogin.setText("LOGIN");
                        btnLogin.setEnabled(true);
                        
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            User user = response.body().getUser();
                            if (user != null) {
                                session.createLoginSession(user.getId(), user.getUsername(), 
                                    user.getFullName(), user.getRole(), user.getDepartment());
                                Toast.makeText(LoginActivity.this, "Welcome " + user.getFullName() + "!", Toast.LENGTH_SHORT).show();
                                navigateToDashboard();
                            }
                        } else {
                            tvError.setText("Invalid username or password!");
                            tvError.setVisibility(View.VISIBLE);
                            etPassword.setText("");
                            etPassword.requestFocus();
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        btnLogin.setText("LOGIN");
                        btnLogin.setEnabled(true);
                        tvError.setText("Network error: " + t.getMessage());
                        tvError.setVisibility(View.VISIBLE);
                    }
                });
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission required!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void navigateToDashboard() {
        String role = session.getRole();
        Intent intent;
        try {
            if (role.equals("super_admin")) {
                intent = new Intent(this, SuperAdminActivity.class);
            } else if (role.equals("admin")) {
                intent = new Intent(this, AdminActivity.class);
            } else if (role.equals("kitchen")) {
                intent = new Intent(this, KitchenActivity.class);
            } else if (role.equals("waiter")) {
                intent = new Intent(this, WaiterActivity.class);
            } else if (role.equals("delivery")) {
                intent = new Intent(this, DeliveryActivity.class);
            } else {
                intent = new Intent(this, ManagerActivity.class);
            }
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
