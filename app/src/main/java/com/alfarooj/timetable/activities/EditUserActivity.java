package com.alfarooj.timetable.activities;

import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.alfarooj.timetable.api.ApiClient;
import com.alfarooj.timetable.models.UpdateDepartmentResponse;
import com.alfarooj.timetable.models.UpdateDepartmentRequest;
import com.alfarooj.timetable.models.User;
import com.alfarooj.timetable.utils.LanguageUtils;
import com.alfarooj.timetable.utils.TranslationHelper;
import com.alfarooj.timetable.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditUserActivity extends AppCompatActivity {
    private TextView tvUserName, tvCurrentDept, tvMessage;
    private Spinner spinnerDepartment;
    private Button btnUpdate;
    private User user;
    private String[] departments = {"kitchen", "waiter", "delivery", "manager"};
    private String[] departmentNames = {"Kitchen", "Waiter", "Delivery", "Manager"};

    @Override
    protected void attachBaseContext(Context newBase) {
        LanguageUtils.applyLanguage(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_edit_user);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Layout error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        try {
            tvUserName = findViewById(R.id.tvUserName);
            tvCurrentDept = findViewById(R.id.tvCurrentDept);
            tvMessage = findViewById(R.id.tvMessage);
            spinnerDepartment = findViewById(R.id.spinnerDepartment);
            btnUpdate = findViewById(R.id.btnUpdate);
            
            user = (User) getIntent().getSerializableExtra("user");
            
            if (user != null) {
                String userText = "User: " + user.getFullName() + " (" + user.getUsername() + ")";
                TranslationHelper.translateTextView(tvUserName, userText);
                
                String currentDeptText = "Current Department: " + getDepartmentDisplay(user.getDepartment());
                TranslationHelper.translateTextView(tvCurrentDept, currentDeptText);
            } else {
                TranslationHelper.translateTextView(tvUserName, "User not found");
                Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Translate department names for spinner
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
            
            TranslationHelper.translateButtonText(btnUpdate, "UPDATE DEPARTMENT");
            
            btnUpdate.setOnClickListener(v -> updateDepartment());
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private String getDepartmentDisplay(String dept) {
        if (dept == null) return "None";
        switch(dept) {
            case "kitchen": return "Kitchen";
            case "waiter": return "Waiter";
            case "delivery": return "Delivery";
            case "manager": return "Manager";
            default: return dept;
        }
    }

    private void updateDepartment() {
        if (btnUpdate == null || spinnerDepartment == null) {
            Toast.makeText(this, "UI not initialized", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        try {
            int position = spinnerDepartment.getSelectedItemPosition();
            String newDepartment = departments[position];
            
            TranslationHelper.translateText("Updating...", new TranslationHelper.TranslationCallback() {
                @Override public void onSuccess(String s) { btnUpdate.setText(s); }
                @Override public void onError(String e) { btnUpdate.setText("Updating..."); }
            });
            btnUpdate.setEnabled(false);
            if (tvMessage != null) tvMessage.setText("");
            
            UpdateDepartmentRequest request = new UpdateDepartmentRequest(user.getId(), newDepartment);
            
            ApiClient.getApiService().updateUserDepartment(request)
                .enqueue(new Callback<UpdateDepartmentResponse>() {
                    @Override
                    public void onResponse(Call<UpdateDepartmentResponse> call, Response<UpdateDepartmentResponse> response) {
                        TranslationHelper.translateButtonText(btnUpdate, "UPDATE DEPARTMENT");
                        btnUpdate.setEnabled(true);
                        
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            String successMsg = "Department updated to " + departmentNames[position];
                            TranslationHelper.translateText(successMsg, new TranslationHelper.TranslationCallback() {
                                @Override public void onSuccess(String s) { 
                                    if (tvMessage != null) tvMessage.setText(s);
                                    Toast.makeText(EditUserActivity.this, s, Toast.LENGTH_SHORT).show();
                                }
                                @Override public void onError(String e) { 
                                    if (tvMessage != null) tvMessage.setText(successMsg);
                                    Toast.makeText(EditUserActivity.this, "Department updated!", Toast.LENGTH_SHORT).show();
                                }
                            });
                            
                            String currentDeptText = "Current Department: " + departmentNames[position];
                            TranslationHelper.translateTextView(tvCurrentDept, currentDeptText);
                            
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            TranslationHelper.translateText("Failed to update department", new TranslationHelper.TranslationCallback() {
                                @Override public void onSuccess(String s) { 
                                    if (tvMessage != null) tvMessage.setText(s);
                                    Toast.makeText(EditUserActivity.this, s, Toast.LENGTH_SHORT).show();
                                }
                                @Override public void onError(String e) { 
                                    if (tvMessage != null) tvMessage.setText("Failed to update department");
                                    Toast.makeText(EditUserActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<UpdateDepartmentResponse> call, Throwable t) {
                        TranslationHelper.translateButtonText(btnUpdate, "UPDATE DEPARTMENT");
                        btnUpdate.setEnabled(true);
                        
                        String errorMsg = "Network error: " + t.getMessage();
                        TranslationHelper.translateText(errorMsg, new TranslationHelper.TranslationCallback() {
                            @Override public void onSuccess(String s) { 
                                if (tvMessage != null) tvMessage.setText(s);
                                Toast.makeText(EditUserActivity.this, s, Toast.LENGTH_SHORT).show();
                            }
                            @Override public void onError(String e) { 
                                if (tvMessage != null) tvMessage.setText(errorMsg);
                                Toast.makeText(EditUserActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            TranslationHelper.translateButtonText(btnUpdate, "UPDATE DEPARTMENT");
            btnUpdate.setEnabled(true);
        }
    }
}
