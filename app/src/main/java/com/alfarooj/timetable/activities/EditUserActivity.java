package com.alfarooj.timetable.activities;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        tvUserName = findViewById(R.id.tvUserName);
        tvCurrentDept = findViewById(R.id.tvCurrentDept);
        tvMessage = findViewById(R.id.tvMessage);
        spinnerDepartment = findViewById(R.id.spinnerDepartment);
        btnUpdate = findViewById(R.id.btnUpdate);

        user = (User) getIntent().getSerializableExtra("user");
        
        if (user != null) {
            tvUserName.setText("User: " + user.getFullName() + " (" + user.getUsername() + ")");
            String currentDept = getDepartmentDisplay(user.getDepartment());
            tvCurrentDept.setText("Current Department: " + currentDept);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departmentNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartment.setAdapter(adapter);

        btnUpdate.setOnClickListener(v -> updateDepartment());
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
        int position = spinnerDepartment.getSelectedItemPosition();
        String newDepartment = departments[position];
        
        btnUpdate.setText("Updating...");
        btnUpdate.setEnabled(false);
        tvMessage.setText("");
        
        UpdateDepartmentRequest request = new UpdateDepartmentRequest(user.getId(), newDepartment);
        
        ApiClient.getApiService().updateUserDepartment(request)
            .enqueue(new Callback<UpdateDepartmentResponse>() {
                @Override
                public void onResponse(Call<UpdateDepartmentResponse> call, Response<UpdateDepartmentResponse> response) {
                    btnUpdate.setText("UPDATE DEPARTMENT");
                    btnUpdate.setEnabled(true);
                    
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        tvMessage.setText("Department updated to " + departmentNames[position]);
                        tvCurrentDept.setText("Current Department: " + departmentNames[position]);
                        Toast.makeText(EditUserActivity.this, "Department updated!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        tvMessage.setText("Failed to update department");
                        Toast.makeText(EditUserActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<UpdateDepartmentResponse> call, Throwable t) {
                    btnUpdate.setText("UPDATE DEPARTMENT");
                    btnUpdate.setEnabled(true);
                    tvMessage.setText("Network error: " + t.getMessage());
                    Toast.makeText(EditUserActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                }
            });
    }
}
