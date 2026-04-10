package com.alfarooj.timetable.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.alfarooj.timetable.api.ApiClient;
import com.alfarooj.timetable.models.LoginRequest;
import com.alfarooj.timetable.models.LoginResponse;
import com.alfarooj.timetable.models.User;
import com.alfarooj.timetable.utils.LanguageUtils;
import com.alfarooj.timetable.utils.SessionManager;
import com.alfarooj.timetable.utils.TranslationHelper;
import com.alfarooj.timetable.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ImageButton btnTogglePassword;
    private TextView tvError, tvTitle, tvSubtitle, tvUsernameLabel, tvPasswordLabel, tvLanguageLabel;
    private ImageView ivLogo;
    private Spinner spinnerLanguage;
    private SessionManager session;
    private boolean isPasswordVisible = false;
    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private List<String> languageCodes = new ArrayList<>();
    private List<String> languageNames = new ArrayList<>();

    @Override
    protected void attachBaseContext(Context newBase) {
        LanguageUtils.applyLanguage(newBase);
        super.attachBaseContext(newBase);
    }

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
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvUsernameLabel = findViewById(R.id.tvUsernameLabel);
        tvPasswordLabel = findViewById(R.id.tvPasswordLabel);
        tvLanguageLabel = findViewById(R.id.tvLanguageLabel);
        spinnerLanguage = findViewById(R.id.spinnerLanguage);

        setupLanguages();
        setupLanguageSpinner();
        translateUI();

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
    protected void onResume() {
        super.onResume();
        translateUI();
    }

    protected void setupLanguages() {
        languageCodes.clear();
        languageNames.clear();
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
    }

    private void setupLanguageSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, languageNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);

        String savedLang = TranslationHelper.getCurrentLanguage();
        int position = languageCodes.indexOf(savedLang);
        if (position >= 0) {
            spinnerLanguage.setSelection(position);
        }

        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newLang = languageCodes.get(position);
                if (!newLang.equals(TranslationHelper.getCurrentLanguage())) {
                    TranslationHelper.setCurrentLanguage(newLang);
                    TranslationHelper.saveLanguage(LoginActivity.this, newLang);
                    LanguageUtils.setLocale(LoginActivity.this, newLang);
                    Toast.makeText(LoginActivity.this, "Language changed to " + languageNames.get(position), Toast.LENGTH_SHORT).show();
                    recreate();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void translateUI() {
        String currentLang = TranslationHelper.getCurrentLanguage();
        
        TranslationHelper.translateTextView(tvTitle, "AL Farooj Timetable");
        TranslationHelper.translateTextView(tvSubtitle, "Login to continue");
        TranslationHelper.translateTextView(tvUsernameLabel, "Username");
        TranslationHelper.translateTextView(tvPasswordLabel, "Password");
        TranslationHelper.translateTextView(tvLanguageLabel, "Select Language");
        TranslationHelper.translateButtonText(btnLogin, "LOGIN");
        TranslationHelper.translateHint(etUsername, "Enter username");
        TranslationHelper.translateHint(etPassword, "Enter password");
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
