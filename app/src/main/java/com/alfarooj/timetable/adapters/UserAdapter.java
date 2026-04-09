package com.alfarooj.timetable.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.alfarooj.timetable.api.ApiClient;
import com.alfarooj.timetable.models.DeleteUserResponse;
import com.alfarooj.timetable.models.User;
import com.alfarooj.timetable.R;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private ArrayList<User> userList;
    private Context context;
    private OnUserDeleteListener deleteListener;

    public interface OnUserDeleteListener {
        void onUserDeleted();
    }

    public UserAdapter(ArrayList<User> userList, Context context, OnUserDeleteListener listener) {
        this.userList = userList;
        this.context = context;
        this.deleteListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = userList.get(position);
        
        String departmentDisplay = "";
        if (user.getDepartment() != null) {
            switch(user.getDepartment()) {
                case "kitchen": departmentDisplay = "Kitchen"; break;
                case "waiter": departmentDisplay = "Waiter"; break;
                case "delivery": departmentDisplay = "Delivery"; break;
                case "manager": departmentDisplay = "Manager"; break;
                default: departmentDisplay = user.getDepartment();
            }
        }
        
        holder.tvFullName.setText("Name: " + user.getFullName());
        holder.tvUsername.setText("Username: " + user.getUsername());
        holder.tvPassword.setText("Password: ******");
        holder.tvDepartment.setText("Department: " + departmentDisplay);
        holder.tvRole.setText("Role: " + user.getRole());

        holder.btnShowPassword.setOnClickListener(v -> {
            Toast.makeText(context, "Password for " + user.getUsername() + " is hidden", Toast.LENGTH_LONG).show();
        });

        holder.btnEdit, btnDelete.setOnClickListener(v -> {
            Toast.makeText(context, "Deleting user...", Toast.LENGTH_SHORT).show();
            
            ApiClient.getApiService().deleteUser(user.getId())
                .enqueue(new Callback<DeleteUserResponse>() {
                    @Override
                    public void onResponse(Call<DeleteUserResponse> call, Response<DeleteUserResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(context, "User deleted successfully", Toast.LENGTH_SHORT).show();
                            userList.remove(position);
                            notifyItemRemoved(position);
                            if (deleteListener != null) deleteListener.onUserDeleted();
                        } else {
                            Toast.makeText(context, "Failed to delete user", Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<DeleteUserResponse> call, Throwable t) {
                        Toast.makeText(context, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvUsername, tvPassword, tvDepartment, tvRole;
        ImageButton btnShowPassword;
        Button btnEdit, btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvPassword = itemView.findViewById(R.id.tvPassword);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            tvRole = itemView.findViewById(R.id.tvRole);
            btnShowPassword = itemView.findViewById(R.id.btnShowPassword);
            btnEdit, btnDelete = itemView.findViewById(R.id.btnEdit, btnDelete);
        }
    }
}
