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
import com.alfarooj.timetable.models.User;
import com.alfarooj.timetable.utils.LanguageUtils;
import com.alfarooj.timetable.utils.SessionManager;
import com.alfarooj.timetable.utils.TranslationHelper;
import com.alfarooj.timetable.R;
import java.util.ArrayList;
import java.util.List;
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
    private ArrayAdapter<String> spinnerAdapter;

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
        updateUIText();

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
                tvError.setText(TranslationHelper.translateTextDirect("Please enter username"));
                tvError.setVisibility(View.VISIBLE);
                etUsername.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                tvError.setText(TranslationHelper.translateTextDirect("Please enter password"));
                tvError.setVisibility(View.VISIBLE);
                etPassword.requestFocus();
                return;
            }

            btnLogin.setText(TranslationHelper.translateTextDirect("LOGGING IN..."));
            btnLogin.setEnabled(false);

            ApiClient.getApiService().login(new LoginRequest(username, password))
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        btnLogin.setText(TranslationHelper.translateTextDirect("LOGIN"));
                        btnLogin.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            User user = response.body().getUser();
                            if (user != null) {
                                session.createLoginSession(user.getId(), user.getUsername(),
                                    user.getFullName(), user.getRole(), user.getDepartment());
                                Toast.makeText(LoginActivity.this, TranslationHelper.translateTextDirect("Welcome ") + user.getFullName() + "!", Toast.LENGTH_SHORT).show();
                                navigateToDashboard();
                            }
                        } else {
                            tvError.setText(TranslationHelper.translateTextDirect("Invalid username or password!"));
                            tvError.setVisibility(View.VISIBLE);
                            etPassword.setText("");
                            etPassword.requestFocus();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        btnLogin.setText(TranslationHelper.translateTextDirect("LOGIN"));
                        btnLogin.setEnabled(true);
                        tvError.setText(TranslationHelper.translateTextDirect("Network error: ") + t.getMessage());
                        tvError.setVisibility(View.VISIBLE);
                    }
                });
        });
    }

    protected void updateUIText() {
        tvTitle.setText(TranslationHelper.translateTextDirect("AL FAROOJ AL SHAMI"));
        tvSubtitle.setText(TranslationHelper.translateTextDirect("TIME TABLE SYSTEM"));
        tvUsernameLabel.setText(TranslationHelper.translateTextDirect("Username:"));
        tvPasswordLabel.setText(TranslationHelper.translateTextDirect("Password:"));
        tvLanguageLabel.setText(TranslationHelper.translateTextDirect("Select Language:"));
        btnLogin.setText(TranslationHelper.translateTextDirect("LOGIN"));
        
        String currentUsername = etUsername.getText().toString();
        if (currentUsername.isEmpty() || currentUsername.equals("Enter username") || currentUsername.equals(TranslationHelper.translateTextDirect("Enter username"))) {
            etUsername.setHint(TranslationHelper.translateTextDirect("Enter username"));
            if (currentUsername.equals("Enter username")) {
                etUsername.setText("");
            }
        }
        
        String currentPassword = etPassword.getText().toString();
        if (currentPassword.isEmpty() || currentPassword.equals("Enter password") || currentPassword.equals(TranslationHelper.translateTextDirect("Enter password"))) {
            etPassword.setHint(TranslationHelper.translateTextDirect("Enter password"));
            if (currentPassword.equals("Enter password")) {
                etPassword.setText("");
            }
        }
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
        languageCodes.add("tr"); languageNames.add("Turkish");
        languageCodes.add("nl"); languageNames.add("Dutch");
        languageCodes.add("el"); languageNames.add("Greek");
        languageCodes.add("vi"); languageNames.add("Vietnamese");
        languageCodes.add("th"); languageNames.add("Thai");
        languageCodes.add("pl"); languageNames.add("Polish");
        languageCodes.add("uk"); languageNames.add("Ukrainian");
    }

    protected void refreshSpinnerLanguageNames() {
        // Badilisha majina ya lugha kwenye spinner kwa lugha mpya
        List<String> translatedNames = new ArrayList<>();
        for (String langName : languageNames) {
            translatedNames.add(TranslationHelper.translateTextDirect(langName));
        }
        if (spinnerAdapter != null) {
            spinnerAdapter.clear();
            for (String name : translatedNames) {
                spinnerAdapter.add(name);
            }
            spinnerAdapter.notifyDataSetChanged();
        }
    }

    protected void setupLanguageSpinner() {
        // First create adapter with original names
        List<String> displayNames = new ArrayList<>();
        for (String langName : languageNames) {
            displayNames.add(TranslationHelper.translateTextDirect(langName));
        }
        
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, displayNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(spinnerAdapter);

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
                    // Change language
                    TranslationHelper.setCurrentLanguage(newLang);
                    TranslationHelper.saveLanguage(LoginActivity.this, newLang);
                    LanguageUtils.setLocale(LoginActivity.this, newLang);
                    
                    // Update ALL UI texts immediately
                    updateUIText();
                    
                    // Refresh spinner display names
                    refreshSpinnerLanguageNames();
                    
                    // Show confirmation
                    Toast.makeText(LoginActivity.this, TranslationHelper.translateTextDirect("Language changed to ") + TranslationHelper.translateTextDirect(languageNames.get(position)), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, TranslationHelper.translateTextDirect("Location permission granted"), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, TranslationHelper.translateTextDirect("Location permission required!"), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void refreshSpinnerIfNeeded() {
        List<String> translatedNames = new ArrayList<>();
        for (String langName : languageNames) {
            translatedNames.add(TranslationHelper.translateTextDirect(langName));
        }
        if (spinnerAdapter != null) {
            spinnerAdapter.clear();
            for (String name : translatedNames) {
                spinnerAdapter.add(name);
            }
            spinnerAdapter.notifyDataSetChanged();
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
            Toast.makeText(this, TranslationHelper.translateTextDirect("Error: ") + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}

    @Override
