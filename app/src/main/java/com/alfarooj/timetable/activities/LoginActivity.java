package com.alfarooj.timetable.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.alfarooj.timetable.database.DatabaseHelper;
import com.alfarooj.timetable.models.User;
import com.alfarooj.timetable.utils.SessionManager;
import com.alfarooj.timetable.R;

public class LoginActivity extends BaseActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvError;
    private DatabaseHelper db;
    private SessionManager session;
    private ImageView ivLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        if (session.isLoggedIn()) {
            navigateToDashboard();
            return;
        }

        ivLogo = findViewById(R.id.ivLogo);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvError = findViewById(R.id.tvError);

        // Set default logo (will be loaded from URL in background)
        ivLogo.setImageResource(android.R.drawable.ic_dialog_info);

        // Request location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                tvError.setText("Please fill all fields");
                return;
            }

            if (db.login(username, password)) {
                User user = db.getUser(username);
                if (user != null) {
                    session.createLoginSession(user.getId(), user.getUsername(), user.getFullName(), user.getRole(), user.getDepartment());
                    navigateToDashboard();
                }
            } else {
                tvError.setText("Invalid username or password");
            }
        });
    }

    private void navigateToDashboard() {
        String role = session.getRole();
        Intent intent;
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
    }
}
