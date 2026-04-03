package com.alfarooj.timetable.activities;

import android.Manifest;
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
import com.alfarooj.timetable.models.LanguagesResponse;
import com.alfarooj.timetable.database.DatabaseHelper;
import com.alfarooj.timetable.models.User;
import com.alfarooj.timetable.utils.SessionManager;
import com.alfarooj.timetable.utils.TranslationHelper;
import com.alfarooj.timetable.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private DatabaseHelper db;
    private SessionManager session;
    private boolean isPasswordVisible = false;
    private static final int LOCATION_PERMISSION_REQUEST = 100;
    
    private List<String> languageCodes = new ArrayList<>();
    private List<String> languageNames = new ArrayList<>();

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
        tvLanguageLabel = findViewById(R.id.tvLanguageLabel);
        spinnerLanguage = findViewById(R.id.spinnerLanguage);

        // Load languages from API
        loadLanguagesFromApi();

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

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            tvError.setText("");
            
            if (username.isEmpty()) {
                tvError.setText("Please enter username");
                etUsername.requestFocus();
                return;
            }
            
            if (password.isEmpty()) {
                tvError.setText("Please enter password");
                etPassword.requestFocus();
                return;
            }

            btnLogin.setText("LOGGING IN...");
            btnLogin.setEnabled(false);

            // Call API login
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
                            String errorMsg = "Invalid username or password!";
                            tvError.setText(errorMsg);
                            etPassword.setText("");
                            etPassword.requestFocus();
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        btnLogin.setText("LOGIN");
                        btnLogin.setEnabled(true);
                        tvError.setText("Network error: " + t.getMessage());
                    }
                });
        });
    }
    
    private void loadLanguagesFromApi() {
        ApiClient.getApiService().getLanguages().enqueue(new Callback<LanguagesResponse>() {
            @Override
            public void onResponse(Call<LanguagesResponse> call, Response<LanguagesResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    languageCodes.clear();
                    languageNames.clear();
                    
                    for (Map<String, String> lang : response.body().getLanguages()) {
                        languageCodes.add(lang.get("code"));
                        languageNames.add(lang.get("name"));
                    }
                    setupLanguageSpinner();
                    
                    // Translate UI after loading languages
                    String savedLang = TranslationHelper.getCurrentLanguage();
                    if (!savedLang.equals("en")) {
                        int position = languageCodes.indexOf(savedLang);
                        if (position >= 0) {
                            spinnerLanguage.setSelection(position);
                        }
                        translateAllUITexts();
                    }
                } else {
                    setupFallbackLanguages();
                }
            }
            
            @Override
            public void onFailure(Call<LanguagesResponse> call, Throwable t) {
                setupFallbackLanguages();
            }
        });
    }
    
    private void setupFallbackLanguages() {
        languageCodes.clear();
        languageNames.clear();
        
        // Lugha zote kama ilivyo kwenye app.py
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
        languageCodes.add("tr"); languageNames.add("Turkish");
        languageCodes.add("nl"); languageNames.add("Dutch");
        languageCodes.add("el"); languageNames.add("Greek");
        languageCodes.add("vi"); languageNames.add("Vietnamese");
        languageCodes.add("th"); languageNames.add("Thai");
        languageCodes.add("pl"); languageNames.add("Polish");
        languageCodes.add("uk"); languageNames.add("Ukrainian");
        
        setupLanguageSpinner();
    }
    
    private void setupLanguageSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, languageNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);
        
        // Set current language
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
                    translateAllUITexts();
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private void translateAllUITexts() {
        String targetLang = TranslationHelper.getCurrentLanguage();
        
        if (targetLang.equals("en")) {
            // Reset to English
            tvTitle.setText("AL FAROOJ AL SHAMI");
            tvSubtitle.setText("TIME TABLE SYSTEM");
            tvUsernameLabel.setText("Username");
            tvPasswordLabel.setText("Password");
            tvLanguageLabel.setText("Language:");
            etUsername.setHint("Enter your username");
            etPassword.setHint("Enter your password");
            btnLogin.setText("LOGIN");
            return;
        }
        
        // Translate each text using API
        translateSingleText("AL FAROOJ AL SHAMI", tvTitle);
        translateSingleText("TIME TABLE SYSTEM", tvSubtitle);
        translateSingleText("Username", tvUsernameLabel);
        translateSingleText("Password", tvPasswordLabel);
        translateSingleText("Language:", tvLanguageLabel);
        translateSingleText("Enter your username", new TranslationHelper.TranslationCallback() {
            @Override
            public void onSuccess(String translated) { etUsername.setHint(translated); }
            @Override
            public void onError(String error) {}
        });
        translateSingleText("Enter your password", new TranslationHelper.TranslationCallback() {
            @Override
            public void onSuccess(String translated) { etPassword.setHint(translated); }
            @Override
            public void onError(String error) {}
        });
        translateSingleText("LOGIN", new TranslationHelper.TranslationCallback() {
            @Override
            public void onSuccess(String translated) { btnLogin.setText(translated); }
            @Override
            public void onError(String error) {}
        });
    }
    
    private void translateSingleText(String text, TextView textView) {
        TranslationHelper.translateText(text, new TranslationHelper.TranslationCallback() {
            @Override
            public void onSuccess(String translated) {
                textView.setText(translated);
            }
            @Override
            public void onError(String error) {
                textView.setText(text);
            }
        });
    }
    
    private void translateSingleText(String text, TranslationHelper.TranslationCallback callback) {
        TranslationHelper.translateText(text, callback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission required for attendance!", Toast.LENGTH_LONG).show();
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
