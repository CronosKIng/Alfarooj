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
import com.alfarooj.timetable.models.User;
import com.alfarooj.timetable.utils.TranslationHelper;
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
    public void onBindViewHolder(ViewHolder holder, int pos) {
        User user = userList.get(pos);
        String dept = user.getDepartmentDisplay();

        holder.tvFullName.setText("Name: " + user.getFullName());
        holder.tvUsername.setText("Username: " + user.getUsername());
        holder.tvPassword.setText("Password: ******");
        holder.tvDepartment.setText("Department: " + dept);
        holder.tvRole.setText("Role: " + user.getRole());

        // Show password when eye button clicked
        holder.btnShowPassword.setOnClickListener(v -> {
            Toast.makeText(context, "Password for " + user.getUsername() + " is: " + user.getPassword(),
                    Toast.LENGTH_LONG).show();
        });

        // Delete user using API
        holder.btnDelete.setOnClickListener(v -> {
            String deletingMsg = "Deleting user...";
            Toast.makeText(context, deletingMsg, Toast.LENGTH_SHORT).show();
            
            ApiClient.getApiService().deleteUser(user.getId())
                .enqueue(new Callback<com.alfarooj.timetable.models.DeleteUserResponse>() {
                    @Override
                    public void onResponse(Call<com.alfarooj.timetable.models.DeleteUserResponse> call,
                                           Response<com.alfarooj.timetable.models.DeleteUserResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            String successMsg = "User deleted successfully";
                            Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show();
                            userList.remove(pos);
                            notifyItemRemoved(pos);
                            if (deleteListener != null) deleteListener.onUserDeleted();
                        } else {
                            String errorMsg = "Failed to delete user";
                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<com.alfarooj.timetable.models.DeleteUserResponse> call, Throwable t) {
                        String errorMsg = "Network error: " + t.getMessage();
                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
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
        Button btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvPassword = itemView.findViewById(R.id.tvPassword);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            tvRole = itemView.findViewById(R.id.tvRole);
            btnShowPassword = itemView.findViewById(R.id.btnShowPassword);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
