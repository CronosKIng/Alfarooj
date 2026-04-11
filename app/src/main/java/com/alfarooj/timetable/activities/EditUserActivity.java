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
            Toast.makeText(this, TranslationHelper.translateTextDirect("Layout error: ") + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                tvUserName.setText(TranslationHelper.translateTextDirect("User: ") + user.getFullName() + " (" + user.getUsername() + ")");
                tvCurrentDept.setText(TranslationHelper.translateTextDirect("Current Department: ") + getDepartmentDisplay(user.getDepartment()));
            } else {
                tvUserName.setText(TranslationHelper.translateTextDirect("User not found"));
                Toast.makeText(this, TranslationHelper.translateTextDirect("User data not found"), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            for (int i = 0; i < departmentNames.length; i++) {
                departmentNames[i] = TranslationHelper.translateTextDirect(departmentNames[i]);
            }
            
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departmentNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDepartment.setAdapter(adapter);
            
            btnUpdate.setText(TranslationHelper.translateTextDirect("UPDATE DEPARTMENT"));
            
            btnUpdate.setOnClickListener(v -> updateDepartment());
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, TranslationHelper.translateTextDirect("Error: ") + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private String getDepartmentDisplay(String dept) {
        if (dept == null) return TranslationHelper.translateTextDirect("None");
        switch(dept) {
            case "kitchen": return TranslationHelper.translateTextDirect("Kitchen");
            case "waiter": return TranslationHelper.translateTextDirect("Waiter");
            case "delivery": return TranslationHelper.translateTextDirect("Delivery");
            case "manager": return TranslationHelper.translateTextDirect("Manager");
            default: return dept;
        }
    }

    private void updateDepartment() {
        if (btnUpdate == null || spinnerDepartment == null) {
            Toast.makeText(this, TranslationHelper.translateTextDirect("UI not initialized"), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        try {
            int position = spinnerDepartment.getSelectedItemPosition();
            String newDepartment = departments[position];
            
            btnUpdate.setText(TranslationHelper.translateTextDirect("Updating..."));
            btnUpdate.setEnabled(false);
            if (tvMessage != null) tvMessage.setText("");
            
            UpdateDepartmentRequest request = new UpdateDepartmentRequest(user.getId(), newDepartment);
            
            ApiClient.getApiService().updateUserDepartment(request)
                .enqueue(new Callback<UpdateDepartmentResponse>() {
                    @Override
                    public void onResponse(Call<UpdateDepartmentResponse> call, Response<UpdateDepartmentResponse> response) {
                        btnUpdate.setText(TranslationHelper.translateTextDirect("UPDATE DEPARTMENT"));
                        btnUpdate.setEnabled(true);
                        
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            String successMsg = TranslationHelper.translateTextDirect("Department updated to ") + departmentNames[position];
                            if (tvMessage != null) tvMessage.setText(successMsg);
                            Toast.makeText(EditUserActivity.this, successMsg, Toast.LENGTH_SHORT).show();
                            
                            tvCurrentDept.setText(TranslationHelper.translateTextDirect("Current Department: ") + departmentNames[position]);
                            
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            String failMsg = TranslationHelper.translateTextDirect("Failed to update department");
                            if (tvMessage != null) tvMessage.setText(failMsg);
                            Toast.makeText(EditUserActivity.this, failMsg, Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<UpdateDepartmentResponse> call, Throwable t) {
                        btnUpdate.setText(TranslationHelper.translateTextDirect("UPDATE DEPARTMENT"));
                        btnUpdate.setEnabled(true);
                        
                        String errorMsg = TranslationHelper.translateTextDirect("Network error: ") + t.getMessage();
                        if (tvMessage != null) tvMessage.setText(errorMsg);
                        Toast.makeText(EditUserActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, TranslationHelper.translateTextDirect("Error: ") + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnUpdate.setText(TranslationHelper.translateTextDirect("UPDATE DEPARTMENT"));
            btnUpdate.setEnabled(true);
        }
    }
}
