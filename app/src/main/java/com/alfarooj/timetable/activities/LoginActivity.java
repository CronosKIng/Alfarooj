package com.alfarooj.timetable.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.alfarooj.timetable.database.DatabaseHelper;
import com.alfarooj.timetable.models.User;
import com.alfarooj.timetable.utils.LanguageUtils;
import com.alfarooj.timetable.utils.SessionManager;
import com.alfarooj.timetable.utils.TranslationUtils;
import com.alfarooj.timetable.R;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends BaseActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ImageButton btnTogglePassword;
    private TextView tvError, tvTitle, tvSubtitle, tvUsernameLabel, tvPasswordLabel, tvVersion;
    private ImageView ivLogo;
    private DatabaseHelper db;
    private SessionManager session;
    private boolean isPasswordVisible = false;
    private static final int LOCATION_PERMISSION_REQUEST = 100;

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

        // Initialize views
        ivLogo = findViewById(R.id.ivLogo);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        tvError = findViewById(R.id.tvError);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvUsernameLabel = findViewById(R.id.tvUsernameLabel);
        tvPasswordLabel = findViewById(R.id.tvPasswordLabel);
        tvVersion = findViewById(R.id.tvAppVersion);

        // Load logo
        loadLogoFromUrl("https://i.ibb.co/MxRVbVR0/IMG-20260322-WA0016-1.jpg");

        // Translate UI based on current language
        translateUI();

        // Password toggle
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

        // Location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 
                LOCATION_PERMISSION_REQUEST);
        }

        // Login button
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            tvError.setText("");
            
            if (username.isEmpty()) {
                translateAndSetText(tvError, "Please enter username");
                etUsername.requestFocus();
                return;
            }
            
            if (password.isEmpty()) {
                translateAndSetText(tvError, "Please enter password");
                etPassword.requestFocus();
                return;
            }

            btnLogin.setText("LOGGING IN...");
            btnLogin.setEnabled(false);

            new Handler().postDelayed(() -> {
                if (db.login(username, password)) {
                    User user = db.getUser(username);
                    if (user != null) {
                        session.createLoginSession(user.getId(), user.getUsername(), user.getFullName(), user.getRole(), user.getDepartment());
                        String welcomeMsg = "Welcome " + user.getFullName() + "!";
                        translateAndToast(welcomeMsg);
                        navigateToDashboard();
                    } else {
                        translateAndSetText(tvError, "User not found!");
                        btnLogin.setText("LOGIN");
                        btnLogin.setEnabled(true);
                    }
                } else {
                    translateAndSetText(tvError, "Invalid username or password!");
                    btnLogin.setText("LOGIN");
                    btnLogin.setEnabled(true);
                    etPassword.setText("");
                    etPassword.requestFocus();
                }
            }, 500);
        });
    }

    private void translateUI() {
        String currentLang = LanguageUtils.getSavedLanguage(this);
        if (currentLang.equals("en")) {
            tvTitle.setText("AL FAROOJ AL SHAMI");
            tvSubtitle.setText("TIME TABLE SYSTEM");
            tvUsernameLabel.setText("Username");
            tvPasswordLabel.setText("Password");
            etUsername.setHint("Enter your username");
            etPassword.setHint("Enter your password");
            btnLogin.setText("LOGIN");
            tvVersion.setText("Version 2.0");
            return;
        }
        
        // Translate each text
        TranslationUtils.translate("AL FAROOJ AL SHAMI", currentLang, result -> tvTitle.setText(result));
        TranslationUtils.translate("TIME TABLE SYSTEM", currentLang, result -> tvSubtitle.setText(result));
        TranslationUtils.translate("Username", currentLang, result -> tvUsernameLabel.setText(result));
        TranslationUtils.translate("Password", currentLang, result -> tvPasswordLabel.setText(result));
        TranslationUtils.translate("Enter your username", currentLang, result -> etUsername.setHint(result));
        TranslationUtils.translate("Enter your password", currentLang, result -> etPassword.setHint(result));
        TranslationUtils.translate("LOGIN", currentLang, result -> btnLogin.setText(result));
        TranslationUtils.translate("Version 2.0", currentLang, result -> tvVersion.setText(result));
    }
    
    private void translateAndSetText(TextView textView, String text) {
        String currentLang = LanguageUtils.getSavedLanguage(this);
        if (currentLang.equals("en")) {
            textView.setText(text);
        } else {
            TranslationUtils.translate(text, currentLang, result -> textView.setText(result));
        }
    }
    
    private void translateAndToast(String message) {
        String currentLang = LanguageUtils.getSavedLanguage(this);
        if (currentLang.equals("en")) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else {
            TranslationUtils.translate(message, currentLang, result -> 
                Toast.makeText(LoginActivity.this, result, Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                translateAndToast("Location permission granted");
            } else {
                translateAndToast("Location permission required for attendance!");
            }
        }
    }

    private void loadLogoFromUrl(String urlString) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        
        executor.execute(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                
                handler.post(() -> {
                    if (bitmap != null) {
                        ivLogo.setImageBitmap(bitmap);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
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
